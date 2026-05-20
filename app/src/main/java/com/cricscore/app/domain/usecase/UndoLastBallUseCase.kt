package com.cricscore.app.domain.usecase

import com.cricscore.app.core.util.OversHelper
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import javax.inject.Inject

class UndoLastBallUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository
) {
    suspend operator fun invoke(matchId: Long, inningsNumber: Int) {
        val balls = inningsRepository.getBallsForInningsSync(matchId, inningsNumber)
        if (balls.isEmpty()) return

        // 1. Delete the last ball
        val lastBall = balls.last()
        inningsRepository.deleteBall(lastBall)

        // 2. Fetch remaining balls
        val remainingBalls = balls.dropLast(1)

        // 3. Reset Innings
        val innings = requireNotNull(inningsRepository.getInningsByNumberSync(matchId, inningsNumber))
        var updatedInnings = innings.copy(
            totalRuns = 0,
            totalWickets = 0,
            ballsBowled = 0,
            extrasWide = 0,
            extrasNoBall = 0,
            extrasBye = 0,
            extrasLegBye = 0,
            isCompleted = false
        )

        // 4. Reset all Batsmen for this innings
        val batsmen = inningsRepository.getBatsmenForInningsSync(matchId, inningsNumber)
        val resetBatsmen = batsmen.map {
            it.copy(runs = 0, balls = 0, fours = 0, sixes = 0, isOut = false, dismissalDescription = null)
        }.associateBy { it.playerName }.toMutableMap()

        // 5. Reset all Bowlers for this innings
        val bowlers = inningsRepository.getBowlersForInningsSync(matchId, inningsNumber)
        val resetBowlers = bowlers.map {
            it.copy(ballsBowled = 0, runsConceded = 0, wickets = 0, maidens = 0, wides = 0, noBalls = 0)
        }.associateBy { it.playerName }.toMutableMap()

        // 6. Reapply remaining balls
        for (b in remainingBalls) {
            val isLegal = OversHelper.isLegalBall(b.ballType)
            val runsAdded = b.runsBatsman + b.runsExtra

            // Update Innings
            var newWide = updatedInnings.extrasWide
            var newNoBall = updatedInnings.extrasNoBall
            var newBye = updatedInnings.extrasBye
            var newLegBye = updatedInnings.extrasLegBye

            when (b.ballType) {
                BallType.WIDE -> newWide += b.runsExtra
                BallType.NO_BALL -> {
                    newNoBall += 1
                    if (b.runsExtra > 1) {
                        newBye += (b.runsExtra - 1)
                    }
                }
                BallType.BYE -> newBye += b.runsExtra
                BallType.LEG_BYE -> newLegBye += b.runsExtra
                BallType.NORMAL -> {}
            }

            updatedInnings = updatedInnings.copy(
                totalRuns = updatedInnings.totalRuns + runsAdded,
                totalWickets = updatedInnings.totalWickets + (if (b.isWicket) 1 else 0),
                ballsBowled = updatedInnings.ballsBowled + (if (isLegal) 1 else 0),
                extrasWide = newWide,
                extrasNoBall = newNoBall,
                extrasBye = newBye,
                extrasLegBye = newLegBye
            )

            // Update Batsman
            val bat = resetBatsmen[b.strikerName]
            if (bat != null) {
                resetBatsmen[b.strikerName] = bat.copy(
                    runs = bat.runs + b.runsBatsman,
                    balls = bat.balls + (if (b.ballType != BallType.WIDE) 1 else 0),
                    fours = bat.fours + (if (b.runsBatsman == 4) 1 else 0),
                    sixes = bat.sixes + (if (b.runsBatsman == 6) 1 else 0)
                )
            }

            // Update Bowler
            val bowl = resetBowlers[b.bowlerName]
            if (bowl != null) {
                val conceded = b.runsBatsman + when (b.ballType) {
                    BallType.WIDE -> b.runsExtra
                    BallType.NO_BALL -> 1
                    else -> 0
                }
                val isBowlerWicket = b.isWicket && b.dismissalType != DismissalType.RUN_OUT && b.dismissalType != DismissalType.RETIRED_HURT

                resetBowlers[b.bowlerName] = bowl.copy(
                    ballsBowled = bowl.ballsBowled + (if (isLegal) 1 else 0),
                    runsConceded = bowl.runsConceded + conceded,
                    wickets = bowl.wickets + (if (isBowlerWicket) 1 else 0),
                    wides = bowl.wides + (if (b.ballType == BallType.WIDE) b.runsExtra else 0),
                    noBalls = bowl.noBalls + (if (b.ballType == BallType.NO_BALL) 1 else 0)
                )
            }

            // Handle Wicket
            if (b.isWicket && b.dismissedPlayerName != null) {
                val dpName = b.dismissedPlayerName
                val desc = when (b.dismissalType) {
                    DismissalType.BOWLED -> "b ${b.bowlerName}"
                    DismissalType.CAUGHT -> if (!b.fielderName.isNullOrBlank()) "c ${b.fielderName} b ${b.bowlerName}" else "c b ${b.bowlerName}"
                    DismissalType.LBW -> "lbw b ${b.bowlerName}"
                    DismissalType.STUMPED -> if (!b.fielderName.isNullOrBlank()) "st ${b.fielderName} b ${b.bowlerName}" else "st b ${b.bowlerName}"
                    DismissalType.HIT_WICKET -> "hit wicket b ${b.bowlerName}"
                    DismissalType.RUN_OUT -> if (!b.fielderName.isNullOrBlank()) "run out (${b.fielderName})" else "run out"
                    DismissalType.RETIRED_HURT -> "retired hurt"
                    null -> "out"
                }

                val batOut = resetBatsmen[dpName]
                if (batOut != null) {
                    resetBatsmen[dpName] = batOut.copy(isOut = true, dismissalDescription = desc)
                }
            }
        }

        // 7. Calculate maidens for each bowler based on remaining balls
        val ballsByOver = remainingBalls.groupBy { it.overNumber }
        for ((overNo, overBalls) in ballsByOver) {
            val legalInOver = overBalls.filter { OversHelper.isLegalBall(it.ballType) }
            if (legalInOver.size == 6) {
                val bowlerName = legalInOver.first().bowlerName
                val runsConceded = overBalls.sumOf { ob ->
                    ob.runsBatsman + when (ob.ballType) {
                        BallType.WIDE -> ob.runsExtra
                        BallType.NO_BALL -> 1
                        else -> 0
                    }
                }
                if (runsConceded == 0) {
                    val bowl = resetBowlers[bowlerName]
                    if (bowl != null) {
                        resetBowlers[bowlerName] = bowl.copy(maidens = bowl.maidens + 1)
                    }
                }
            }
        }

        // 8. Save all updated entities
        inningsRepository.saveBatsmenInnings(resetBatsmen.values.toList())
        inningsRepository.saveBowlersInnings(resetBowlers.values.toList())
        inningsRepository.updateInnings(updatedInnings)

        // 9. Revert Match status if it was completed
        val match = requireNotNull(matchRepository.getMatchByIdSync(matchId))
        if (match.status == MatchStatus.COMPLETED) {
            val restoredStatus = if (inningsNumber == 1) MatchStatus.FIRST_INNINGS else MatchStatus.SECOND_INNINGS
            matchRepository.updateMatch(
                match.copy(
                    status = restoredStatus,
                    winnerTeam = null,
                    winMarginRuns = 0,
                    winMarginWickets = 0,
                    isTied = false,
                    playerOfMatch = null
                )
            )
        }
    }
}
