package com.cricscore.app.domain.usecase.tournament

import com.cricscore.app.domain.model.DismissalType
import com.cricscore.app.domain.model.TournamentPlayerStat
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.TournamentRepository
import javax.inject.Inject

class UpdatePlayerTournamentStatsUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val inningsRepository: InningsRepository
) {
    suspend fun execute(tournamentId: Long, matchId: Long, team1Id: Long, team2Id: Long) {
        val inn1 = inningsRepository.getInningsByNumberSync(matchId, 1)
        val inn2 = inningsRepository.getInningsByNumberSync(matchId, 2)

        val inningsList = listOfNotNull(inn1, inn2)

        // Pre-aggregate fielding stats (catches, run outs, stumpings) from all balls of the match
        val fieldingStats = mutableMapOf<String, FieldingCount>()
        inningsList.forEach { innings ->
            val balls = inningsRepository.getBallsForInningsSync(matchId, innings.inningsNumber)
            balls.forEach { ball ->
                if (ball.isWicket && !ball.fielderName.isNullOrBlank()) {
                    val count = fieldingStats.getOrPut(ball.fielderName) { FieldingCount() }
                    when (ball.dismissalType) {
                        DismissalType.CAUGHT -> count.catches++
                        DismissalType.RUN_OUT -> count.runOuts++
                        DismissalType.STUMPED -> count.stumpings++
                        else -> { /* no-op */ }
                    }
                }
            }
        }

        inningsList.forEach { innings ->
            val battingTeamId = if (innings.battingTeam == inn1?.battingTeam) team1Id else team2Id
            val bowlingTeamId = if (battingTeamId == team1Id) team2Id else team1Id

            // 1. Update batting stats
            val batsmen = inningsRepository.getBatsmenForInningsSync(matchId, innings.inningsNumber)
            batsmen.forEach { b ->
                if (b.balls > 0 || b.runs > 0) {
                    val existing = tournamentRepository.getPlayerStat(tournamentId, b.playerName, battingTeamId)
                    val fielderFielding = fieldingStats[b.playerName] ?: FieldingCount()

                    if (existing == null) {
                        val newStat = TournamentPlayerStat(
                            tournamentId = tournamentId,
                            teamId = battingTeamId,
                            teamName = innings.battingTeam,
                            playerName = b.playerName,
                            matchesPlayed = 1,
                            innings = 1,
                            totalRuns = b.runs,
                            totalBalls = b.balls,
                            totalFours = b.fours,
                            totalSixes = b.sixes,
                            highestScore = b.runs,
                            highestScoreNotOut = !b.isOut,
                            fifties = if (b.runs in 50..99) 1 else 0,
                            hundreds = if (b.runs >= 100) 1 else 0,
                            timesOut = if (b.isOut) 1 else 0,
                            notOutCount = if (!b.isOut) 1 else 0,
                            catches = fielderFielding.catches,
                            runOuts = fielderFielding.runOuts,
                            stumpings = fielderFielding.stumpings
                        )
                        tournamentRepository.insertPlayerStat(newStat)
                    } else {
                        val newHighest = if (b.runs > existing.highestScore) b.runs else existing.highestScore
                        val isCurrentNotOut = !b.isOut
                        val newHighestNotOut = if (b.runs == newHighest) isCurrentNotOut else existing.highestScoreNotOut

                        val updated = existing.copy(
                            matchesPlayed = existing.matchesPlayed + 1,
                            innings = existing.innings + 1,
                            totalRuns = existing.totalRuns + b.runs,
                            totalBalls = existing.totalBalls + b.balls,
                            totalFours = existing.totalFours + b.fours,
                            totalSixes = existing.totalSixes + b.sixes,
                            highestScore = newHighest,
                            highestScoreNotOut = newHighestNotOut,
                            fifties = existing.fifties + if (b.runs in 50..99) 1 else 0,
                            hundreds = existing.hundreds + if (b.runs >= 100) 1 else 0,
                            timesOut = existing.timesOut + if (b.isOut) 1 else 0,
                            notOutCount = existing.notOutCount + if (!b.isOut) 1 else 0,
                            catches = existing.catches + fielderFielding.catches,
                            runOuts = existing.runOuts + fielderFielding.runOuts,
                            stumpings = existing.stumpings + fielderFielding.stumpings
                        )
                        tournamentRepository.updatePlayerStat(updated)
                    }
                }
            }

            // 2. Update bowling stats
            val bowlers = inningsRepository.getBowlersForInningsSync(matchId, innings.inningsNumber)
            bowlers.forEach { bw ->
                if (bw.ballsBowled > 0) {
                    val existing = tournamentRepository.getPlayerStat(tournamentId, bw.playerName, bowlingTeamId)
                    val fielderFielding = fieldingStats[bw.playerName] ?: FieldingCount()

                    val totalBallsBowled = bw.ballsBowled
                    val initialOvers = totalBallsBowled / 6
                    val initialBalls = totalBallsBowled % 6

                    if (existing == null) {
                        val newStat = TournamentPlayerStat(
                            tournamentId = tournamentId,
                            teamId = bowlingTeamId,
                            teamName = innings.bowlingTeam,
                            playerName = bw.playerName,
                            // If they didn't bat, we count matches played here
                            matchesPlayed = if (batsmen.none { it.playerName == bw.playerName }) 1 else 0,
                            oversBowled = initialOvers,
                            ballsBowled = initialBalls,
                            runsConceded = bw.runsConceded,
                            wicketsTaken = bw.wickets,
                            maidens = bw.maidens,
                            bestBowlingWickets = bw.wickets,
                            bestBowlingRuns = bw.runsConceded,
                            fiveWicketHauls = if (bw.wickets >= 5) 1 else 0,
                            catches = fielderFielding.catches,
                            runOuts = fielderFielding.runOuts,
                            stumpings = fielderFielding.stumpings
                        )
                        tournamentRepository.insertPlayerStat(newStat)
                    } else {
                        val isBestBowling = bw.wickets > existing.bestBowlingWickets ||
                                (bw.wickets == existing.bestBowlingWickets && bw.runsConceded < existing.bestBowlingRuns)

                        val newBestWickets = if (isBestBowling) bw.wickets else existing.bestBowlingWickets
                        val newBestRuns = if (isBestBowling) bw.runsConceded else existing.bestBowlingRuns

                        val totalBallsBowledAcc = existing.oversBowled * 6 + existing.ballsBowled + bw.ballsBowled
                        val newOversBowled = totalBallsBowledAcc / 6
                        val newBallsBowled = totalBallsBowledAcc % 6

                        val updated = existing.copy(
                            matchesPlayed = if (batsmen.none { it.playerName == bw.playerName && (it.balls > 0 || it.runs > 0) }) existing.matchesPlayed + 1 else existing.matchesPlayed,
                            oversBowled = newOversBowled,
                            ballsBowled = newBallsBowled,
                            runsConceded = existing.runsConceded + bw.runsConceded,
                            wicketsTaken = existing.wicketsTaken + bw.wickets,
                            maidens = existing.maidens + bw.maidens,
                            bestBowlingWickets = newBestWickets,
                            bestBowlingRuns = newBestRuns,
                            fiveWicketHauls = existing.fiveWicketHauls + if (bw.wickets >= 5) 1 else 0,
                            catches = existing.catches + fielderFielding.catches,
                            runOuts = existing.runOuts + fielderFielding.runOuts,
                            stumpings = existing.stumpings + fielderFielding.stumpings
                        )
                        tournamentRepository.updatePlayerStat(updated)
                    }
                }
            }

            // 3. Update fielding-only players
            fieldingStats.forEach { (playerName, count) ->
                val alreadyBat = batsmen.any { it.playerName == playerName && (it.balls > 0 || it.runs > 0) }
                val alreadyBowl = bowlers.any { it.playerName == playerName && it.ballsBowled > 0 }
                if (!alreadyBat && !alreadyBowl) {
                    val existing = tournamentRepository.getPlayerStat(tournamentId, playerName, bowlingTeamId)
                    if (existing == null) {
                        val newStat = TournamentPlayerStat(
                            tournamentId = tournamentId,
                            teamId = bowlingTeamId,
                            teamName = innings.bowlingTeam,
                            playerName = playerName,
                            matchesPlayed = 1,
                            catches = count.catches,
                            runOuts = count.runOuts,
                            stumpings = count.stumpings
                        )
                        tournamentRepository.insertPlayerStat(newStat)
                    } else {
                        val updated = existing.copy(
                            matchesPlayed = existing.matchesPlayed + 1,
                            catches = existing.catches + count.catches,
                            runOuts = existing.runOuts + count.runOuts,
                            stumpings = existing.stumpings + count.stumpings
                        )
                        tournamentRepository.updatePlayerStat(updated)
                    }
                }
            }
        }
    }

    private class FieldingCount {
        var catches = 0
        var runOuts = 0
        var stumpings = 0
    }
}
