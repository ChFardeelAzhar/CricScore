package com.cricscore.app.domain.usecase.tournament

import com.cricscore.app.domain.model.TournamentStatus
import com.cricscore.app.domain.model.TournamentTeam
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
    private data class TeamStats(
        val team: TournamentTeam,
        val runs: Int,
        val wickets: Int,
        val ballsFaced: Int,
        val ballsBowled: Int
    )

    suspend fun execute(fixtureId: Long, matchId: Long) {
        val match = matchRepository.getMatchByIdSync(matchId) ?: return
        val innings1 = inningsRepository.getInningsByNumberSync(matchId, 1) ?: return
        val innings2 = inningsRepository.getInningsByNumberSync(matchId, 2)

        val fixture = tournamentRepository.getFixtureById(fixtureId) ?: return
        val team1 = tournamentRepository.getTeamById(fixture.team1Id) ?: return
        val team2 = tournamentRepository.getTeamById(fixture.team2Id) ?: return

        val tournament = tournamentRepository.getTournamentById(fixture.tournamentId) ?: return
        val playersPerSide = tournament.playersPerSide
        val maxOvers = tournament.oversPerMatch

        // Identify which tournament team corresponds to which innings
        val teamAStats = if (innings1.battingTeam == team1.teamName) {
            // Team 1 batted first
            val t1Runs = innings1.totalRuns
            val t1Wickets = innings1.totalWickets
            val t1BallsFaced = innings1.ballsBowled
            val t1BallsBowled = innings2?.ballsBowled ?: 0
            
            // Effective balls for NRR: if all out, use full quota
            val effBallsFaced = if (t1Wickets >= playersPerSide - 1) maxOvers * 6 else t1BallsFaced
            val effBallsBowled = if (innings2 != null && innings2.totalWickets >= playersPerSide - 1) maxOvers * 6 else t1BallsBowled

            TeamStats(team1, t1Runs, t1Wickets, effBallsFaced, effBallsBowled)
        } else {
            // Team 2 batted first
            val t2Runs = innings1.totalRuns
            val t2Wickets = innings1.totalWickets
            val t2BallsFaced = innings1.ballsBowled
            val t2BallsBowled = innings2?.ballsBowled ?: 0

            val effBallsFaced = if (t2Wickets >= playersPerSide - 1) maxOvers * 6 else t2BallsFaced
            val effBallsBowled = if (innings2 != null && innings2.totalWickets >= playersPerSide - 1) maxOvers * 6 else t2BallsBowled

            TeamStats(team2, t2Runs, t2Wickets, effBallsFaced, effBallsBowled)
        }

        val teamBStats = if (innings1.battingTeam == team1.teamName) {
            // Team 2 batted second
            val t2Runs = innings2?.totalRuns ?: 0
            val t2Wickets = innings2?.totalWickets ?: 0
            val t2BallsFaced = innings2?.ballsBowled ?: 0
            val t2BallsBowled = innings1.ballsBowled

            val effBallsFaced = if (t2Wickets >= playersPerSide - 1) maxOvers * 6 else t2BallsFaced
            val effBallsBowled = if (innings1.totalWickets >= playersPerSide - 1) maxOvers * 6 else t2BallsBowled

            TeamStats(team2, t2Runs, t2Wickets, effBallsFaced, effBallsBowled)
        } else {
            // Team 1 batted second
            val t1Runs = innings2?.totalRuns ?: 0
            val t1Wickets = innings2?.totalWickets ?: 0
            val t1BallsFaced = innings2?.ballsBowled ?: 0
            val t1BallsBowled = innings1.ballsBowled

            val effBallsFaced = if (t1Wickets >= playersPerSide - 1) maxOvers * 6 else t1BallsFaced
            val effBallsBowled = if (innings1.totalWickets >= playersPerSide - 1) maxOvers * 6 else t1BallsBowled

            TeamStats(team1, t1Runs, t1Wickets, effBallsFaced, effBallsBowled)
        }

        // 1. Determine winner/loser/tie
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
            team1Score = if (innings1.battingTeam == team1.teamName) "${innings1.totalRuns}/${innings1.totalWickets}" else innings2?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "-",
            team2Score = if (innings1.battingTeam == team2.teamName) "${innings1.totalRuns}/${innings1.totalWickets}" else innings2?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "-",
            team1Overs = if (innings1.battingTeam == team1.teamName) oversDisplay(innings1.ballsBowled) else innings2?.let { oversDisplay(it.ballsBowled) } ?: "-",
            team2Overs = if (innings1.battingTeam == team2.teamName) oversDisplay(innings1.ballsBowled) else innings2?.let { oversDisplay(it.ballsBowled) } ?: "-",
            resultSummary = resultSummaryText,
            playedAt = System.currentTimeMillis()
        )
        tournamentRepository.updateFixture(updatedFixture)

        // 4. Update Points Table Stats
        
        // Update Team 1 Stats
        val t1RunsFor = team1.runsFor + (if (team1.id == teamAStats.team.id) teamAStats.runs else teamBStats.runs)
        val t1RunsAgainst = team1.runsAgainst + (if (team1.id == teamAStats.team.id) teamBStats.runs else teamAStats.runs)
        val t1BallsFaced = team1.ballsFaced + (if (team1.id == teamAStats.team.id) teamAStats.ballsFaced else teamBStats.ballsFaced)
        val t1BallsBowled = team1.ballsBowled + (if (team1.id == teamAStats.team.id) teamAStats.ballsBowled else teamBStats.ballsBowled)
        val t1Nrr = calculateNRR(t1RunsFor, t1BallsFaced, t1RunsAgainst, t1BallsBowled)

        val updatedTeam1 = team1.copy(
            matchesPlayed = team1.matchesPlayed + 1,
            won = team1.won + if (winnerId == team1.id) 1 else 0,
            lost = team1.lost + if (loserId == team1.id) 1 else 0,
            tied = team1.tied + if (isTied) 1 else 0,
            points = team1.points + when {
                winnerId == team1.id -> tournament.pointsForWin
                isTied -> tournament.pointsForTie
                else -> tournament.pointsForLoss
            },
            runsFor = t1RunsFor,
            runsAgainst = t1RunsAgainst,
            ballsFaced = t1BallsFaced,
            ballsBowled = t1BallsBowled,
            netRunRate = t1Nrr
        )
        tournamentRepository.updateTeam(updatedTeam1)

        // Update Team 2 Stats
        val t2RunsFor = team2.runsFor + (if (team2.id == teamAStats.team.id) teamAStats.runs else teamBStats.runs)
        val t2RunsAgainst = team2.runsAgainst + (if (team2.id == teamAStats.team.id) teamBStats.runs else teamAStats.runs)
        val t2BallsFaced = team2.ballsFaced + (if (team2.id == teamAStats.team.id) teamAStats.ballsFaced else teamBStats.ballsFaced)
        val t2BallsBowled = team2.ballsBowled + (if (team2.id == teamAStats.team.id) teamAStats.ballsBowled else teamBStats.ballsBowled)
        val t2Nrr = calculateNRR(t2RunsFor, t2BallsFaced, t2RunsAgainst, t2BallsBowled)

        val updatedTeam2 = team2.copy(
            matchesPlayed = team2.matchesPlayed + 1,
            won = team2.won + if (winnerId == team2.id) 1 else 0,
            lost = team2.lost + if (loserId == team2.id) 1 else 0,
            tied = team2.tied + if (isTied) 1 else 0,
            points = team2.points + when {
                winnerId == team2.id -> tournament.pointsForWin
                isTied -> tournament.pointsForTie
                else -> tournament.pointsForLoss
            },
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
        val oversFaced = ballsFaced / 6.0
        val oversBowled = ballsBowled / 6.0
        
        val rateFor = if (oversFaced > 0) runsFor / oversFaced else 0.0
        val rateAgainst = if (oversBowled > 0) runsAgainst / oversBowled else 0.0
        
        return rateFor - rateAgainst
    }
}
