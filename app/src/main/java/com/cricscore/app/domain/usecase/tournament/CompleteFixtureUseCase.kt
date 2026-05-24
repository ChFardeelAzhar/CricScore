package com.cricscore.app.domain.usecase.tournament

import com.cricscore.app.domain.model.TournamentStatus
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import com.cricscore.app.domain.repository.TournamentRepository
import javax.inject.Inject

class CompleteFixtureUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository,
    private val updatePlayerTournamentStatsUseCase: UpdatePlayerTournamentStatsUseCase
) {
    suspend fun execute(fixtureId: Long, matchId: Long) {
        val match = matchRepository.getMatchByIdSync(matchId) ?: return
        val innings1 = inningsRepository.getInningsByNumberSync(matchId, 1) ?: return
        val innings2 = inningsRepository.getInningsByNumberSync(matchId, 2)

        val fixture = tournamentRepository.getFixtureById(fixtureId) ?: return
        val team1 = tournamentRepository.getTeamById(fixture.team1Id) ?: return
        val team2 = tournamentRepository.getTeamById(fixture.team2Id) ?: return

        // 1. Determine winner/loser/tie
        val team1Runs = innings1.totalRuns
        val team2Runs = innings2?.totalRuns ?: 0
        val isTied = match.isTied
        val winnerId = when {
            isTied -> -1L
            match.winnerTeam == team1.teamName -> team1.id
            match.winnerTeam == team2.teamName -> team2.id
            else -> -1L
        }
        val loserId = when {
            isTied -> -1L
            winnerId == team1.id -> team2.id
            winnerId == team2.id -> team1.id
            else -> -1L
        }

        // 2. Build Result Summary
        val resultSummaryText = if (isTied) {
            "Match Tied!"
        } else if (match.winnerTeam != null) {
            val winnerName = match.winnerTeam
            if (match.winMarginRuns > 0) {
                "$winnerName won by ${match.winMarginRuns} runs"
            } else if (match.winMarginWickets > 0) {
                "$winnerName won by ${match.winMarginWickets} wickets"
            } else {
                "$winnerName won"
            }
        } else {
            "Match Completed"
        }

        // Helper to format overs display
        fun oversDisplay(balls: Int): String {
            val completedOvers = balls / 6
            val remainingBalls = balls % 6
            return "$completedOvers.$remainingBalls"
        }

        // 3. Update Fixture details
        val updatedFixture = fixture.copy(
            status = "COMPLETED",
            linkedMatchId = matchId,
            winnerId = winnerId,
            loserId = loserId,
            isTied = isTied,
            team1Score = "${innings1.totalRuns}/${innings1.totalWickets}",
            team2Score = innings2?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "-",
            team1Overs = oversDisplay(innings1.ballsBowled),
            team2Overs = innings2?.let { oversDisplay(it.ballsBowled) } ?: "-",
            resultSummary = resultSummaryText,
            playedAt = System.currentTimeMillis()
        )
        tournamentRepository.updateFixture(updatedFixture)

        // 4. Update Points Table Stats
        val tournament = tournamentRepository.getTournamentById(fixture.tournamentId) ?: return

        // Update Team 1 Stats
        val t1Played = team1.matchesPlayed + 1
        val t1Won = team1.won + if (winnerId == team1.id) 1 else 0
        val t1Lost = team1.lost + if (loserId == team1.id) 1 else 0
        val t1Tied = team1.tied + if (isTied) 1 else 0
        val t1Points = team1.points + when {
            winnerId == team1.id -> tournament.pointsForWin
            isTied -> tournament.pointsForTie
            else -> tournament.pointsForLoss
        }
        val t1RunsFor = team1.runsFor + team1Runs
        val t1RunsAgainst = team1.runsAgainst + team2Runs
        val t1BallsFaced = team1.ballsFaced + (innings1.ballsBowled)
        val t1BallsBowled = team1.ballsBowled + (innings2?.ballsBowled ?: 0)
        val t1Nrr = calculateNRR(t1RunsFor, t1BallsFaced, t1RunsAgainst, t1BallsBowled)

        val updatedTeam1 = team1.copy(
            matchesPlayed = t1Played,
            won = t1Won,
            lost = t1Lost,
            tied = t1Tied,
            points = t1Points,
            runsFor = t1RunsFor,
            runsAgainst = t1RunsAgainst,
            ballsFaced = t1BallsFaced,
            ballsBowled = t1BallsBowled,
            netRunRate = t1Nrr
        )
        tournamentRepository.updateTeam(updatedTeam1)

        // Update Team 2 Stats
        val t2Played = team2.matchesPlayed + 1
        val t2Won = team2.won + if (winnerId == team2.id) 1 else 0
        val t2Lost = team2.lost + if (loserId == team2.id) 1 else 0
        val t2Tied = team2.tied + if (isTied) 1 else 0
        val t2Points = team2.points + when {
            winnerId == team2.id -> tournament.pointsForWin
            isTied -> tournament.pointsForTie
            else -> tournament.pointsForLoss
        }
        val t2RunsFor = team2.runsFor + team2Runs
        val t2RunsAgainst = team2.runsAgainst + team1Runs
        val t2BallsFaced = team2.ballsFaced + (innings2?.ballsBowled ?: 0)
        val t2BallsBowled = team2.ballsBowled + (innings1.ballsBowled)
        val t2Nrr = calculateNRR(t2RunsFor, t2BallsFaced, t2RunsAgainst, t2BallsBowled)

        val updatedTeam2 = team2.copy(
            matchesPlayed = t2Played,
            won = t2Won,
            lost = t2Lost,
            tied = t2Tied,
            points = t2Points,
            runsFor = t2RunsFor,
            runsAgainst = t2RunsAgainst,
            ballsFaced = t2BallsFaced,
            ballsBowled = t2BallsBowled,
            netRunRate = t2Nrr
        )
        tournamentRepository.updateTeam(updatedTeam2)

        // 5. Update Player Tournament Stats
        updatePlayerTournamentStatsUseCase.execute(fixture.tournamentId, matchId, fixture.team1Id, fixture.team2Id)

        // 6. Check Tournament Completion
        val pendingCount = tournamentRepository.getPendingFixtureCount(fixture.tournamentId)
        if (pendingCount == 0) {
            val sortedTeams = tournamentRepository.getTeamsByTournamentSync(fixture.tournamentId)
            val winner = sortedTeams.firstOrNull() // Ranked by points desc then NRR desc
            tournamentRepository.updateTournament(
                tournament.copy(
                    status = TournamentStatus.COMPLETED,
                    winnerId = winner?.id ?: -1L,
                    completedAt = System.currentTimeMillis()
                )
            )
        } else if (tournament.status == TournamentStatus.UPCOMING) {
            // Mark tournament as ongoing when the first match is completed
            tournamentRepository.updateTournament(
                tournament.copy(
                    status = TournamentStatus.ONGOING,
                    startedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun calculateNRR(runsFor: Int, ballsFaced: Int, runsAgainst: Int, ballsBowled: Int): Double {
        if (ballsFaced == 0 || ballsBowled == 0) return 0.0
        return (runsFor.toDouble() / ballsFaced * 6.0) - (runsAgainst.toDouble() / ballsBowled * 6.0)
    }
}
