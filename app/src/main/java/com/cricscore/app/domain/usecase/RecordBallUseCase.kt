package com.cricscore.app.domain.usecase

import com.cricscore.app.core.util.OversHelper
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import javax.inject.Inject

class RecordBallUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository
) {
    suspend operator fun invoke(
        matchId: Long,
        inningsNumber: Int,
        runsBatsman: Int,
        runsExtra: Int,
        ballType: BallType,
        isWicket: Boolean = false,
        dismissalType: DismissalType? = null,
        fielderName: String? = null,
        dismissedPlayerName: String? = null,
        bowlerName: String? = null,
        nextBatsmanName: String? = null
    ): Ball {
        // 1. Fetch current Innings & Match state
        if (!nextBatsmanName.isNullOrBlank()) {
            val existing = inningsRepository.getBatsmanSync(matchId, inningsNumber, nextBatsmanName.trim())
            if (existing == null) {
                val newBatsman = BatsmanInnings(
                    matchId = matchId,
                    inningsNumber = inningsNumber,
                    playerName = nextBatsmanName.trim()
                )
                inningsRepository.saveBatsmenInnings(listOf(newBatsman))
            }
        }
        val innings = requireNotNull(inningsRepository.getInningsByNumberSync(matchId, inningsNumber)) {
            "Innings not found for matchId: $matchId, inningsNumber: $inningsNumber"
        }
        val match = requireNotNull(matchRepository.getMatchByIdSync(matchId)) {
            "Match not found for matchId: $matchId"
        }

        // Find last ball to get current over/ball number or active batsman/bowler names
        val ballsList = inningsRepository.getBallsForInningsSync(matchId, inningsNumber)
        val currentOverNo = if (ballsList.isEmpty()) 0 else {
            val last = ballsList.last()
            val legalBallsThisOver = ballsList.filter { it.overNumber == last.overNumber && OversHelper.isLegalBall(it.ballType) }.size
            if (legalBallsThisOver == 6) last.overNumber + 1 else last.overNumber
        }

        val legalBallsInCurrentOver = ballsList.filter { it.overNumber == currentOverNo && OversHelper.isLegalBall(it.ballType) }.size
        val ballNo = if (OversHelper.isLegalBall(ballType)) legalBallsInCurrentOver + 1 else legalBallsInCurrentOver

        // Get striker, non-striker, bowler names from the latest state
        // If balls list is empty, fetch them from the batsman/bowler tables
        val batsmen = inningsRepository.getBatsmenForInningsSync(matchId, inningsNumber)
        val activeBatsmen = batsmen.filter { !it.isOut }
        
        var currentStrikerName = ""
        var currentNonStrikerName = ""
        
        if (ballsList.isEmpty()) {
            // First ball of innings: striker is the one who faces first
            // Usually the UI saves them, we can find who is not out.
            // Let's assume the first batsman in list is striker and second is non-striker, or sort/filter them.
            // Let's fetch them
            currentStrikerName = batsmen.firstOrNull()?.playerName ?: "Striker"
            currentNonStrikerName = batsmen.getOrNull(1)?.playerName ?: "Non-Striker"
        } else {
            // Get striker/non-striker from the last ball
            val lastBall = ballsList.last()
            currentStrikerName = lastBall.strikerName
            currentNonStrikerName = lastBall.nonStrikerName

            // If the last ball caused a dismissal, we need to replace the dismissed batsman
            if (lastBall.isWicket && lastBall.dismissedPlayerName != null) {
                val replacedPlayer = lastBall.dismissedPlayerName
                // Find a batsman in the list who hasn't batted yet (isOut = false, and balls = 0/runs = 0 and is not currently striker/non-striker)
                val newBatsman = batsmen.firstOrNull { 
                    !it.isOut && it.playerName != currentStrikerName && it.playerName != currentNonStrikerName && it.playerName != replacedPlayer 
                }
                val newName = newBatsman?.playerName ?: "New Batsman"
                if (replacedPlayer == currentStrikerName) {
                    currentStrikerName = newName
                } else if (replacedPlayer == currentNonStrikerName) {
                    currentNonStrikerName = newName
                }
            }

            // If the last ball completed the over, we rotate ends BEFORE this new ball is bowled
            val totalLegalInLastOver = ballsList.filter { it.overNumber == lastBall.overNumber && OversHelper.isLegalBall(it.ballType) }.size
            if (totalLegalInLastOver == 6 && lastBall.id == ballsList.last().id) {
                val temp = currentStrikerName
                currentStrikerName = currentNonStrikerName
                currentNonStrikerName = temp
            }
        }

        // Get bowler name
        val resolvedBowlerName = if (!bowlerName.isNullOrBlank()) {
            bowlerName
        } else if (ballsList.isEmpty()) {
            val bowlers = inningsRepository.getBowlersForInningsSync(matchId, inningsNumber)
            bowlers.firstOrNull()?.playerName ?: "Bowler"
        } else {
            val lastBall = ballsList.last()
            // If the last ball completed the over, we need a new bowler
            val totalLegalInLastOver = ballsList.filter { it.overNumber == lastBall.overNumber && OversHelper.isLegalBall(it.ballType) }.size
            if (totalLegalInLastOver == 6) {
                val bowlers = inningsRepository.getBowlersForInningsSync(matchId, inningsNumber)
                val lastBowler = lastBall.bowlerName
                val nextBowler = bowlers.firstOrNull { it.playerName != lastBowler } ?: bowlers.firstOrNull()
                nextBowler?.playerName ?: "Bowler"
            } else {
                lastBall.bowlerName
            }
        }

        // Ensure bowler exists in DB, insert if missing
        if (inningsRepository.getBowlerSync(matchId, inningsNumber, resolvedBowlerName) == null) {
            val newBowler = BowlerInnings(
                matchId = matchId,
                inningsNumber = inningsNumber,
                playerName = resolvedBowlerName
            )
            inningsRepository.saveBowlersInnings(listOf(newBowler))
        }

        // 2. Determine Ball details
        val isLegal = OversHelper.isLegalBall(ballType)
        val runsAdded = runsBatsman + runsExtra

        // 3. Construct Ball object
        val ball = Ball(
            matchId = matchId,
            inningsNumber = inningsNumber,
            overNumber = currentOverNo,
            ballNumber = ballNo,
            strikerName = currentStrikerName,
            nonStrikerName = currentNonStrikerName,
            bowlerName = resolvedBowlerName,
            runsBatsman = runsBatsman,
            runsExtra = runsExtra,
            ballType = ballType,
            isWicket = isWicket,
            dismissalType = dismissalType,
            fielderName = fielderName,
            dismissedPlayerName = dismissedPlayerName ?: if (isWicket && dismissalType != DismissalType.RUN_OUT) currentStrikerName else null
        )
        val ballId = inningsRepository.recordBall(ball)
        val savedBall = ball.copy(id = ballId)

        // 4. Update Striker stats
        val strikerInnings = requireNotNull(inningsRepository.getBatsmanSync(matchId, inningsNumber, currentStrikerName)) {
            "Striker batsman innings not found: $currentStrikerName"
        }
        var updatedStriker = strikerInnings.copy(
            runs = strikerInnings.runs + runsBatsman,
            balls = strikerInnings.balls + (if (ballType != BallType.WIDE) 1 else 0),
            fours = strikerInnings.fours + (if (runsBatsman == 4) 1 else 0),
            sixes = strikerInnings.sixes + (if (runsBatsman == 6) 1 else 0)
        )

        // 5. Update Wicket states
        var updatedNonStriker: BatsmanInnings? = null
        if (isWicket && savedBall.dismissedPlayerName != null) {
            val dpName = savedBall.dismissedPlayerName
            val desc = when (dismissalType) {
                DismissalType.BOWLED -> "b $resolvedBowlerName"
                DismissalType.CAUGHT -> if (!fielderName.isNullOrBlank()) "c $fielderName b $resolvedBowlerName" else "c b $resolvedBowlerName"
                DismissalType.LBW -> "lbw b $resolvedBowlerName"
                DismissalType.STUMPED -> if (!fielderName.isNullOrBlank()) "st $fielderName b $resolvedBowlerName" else "st b $resolvedBowlerName"
                DismissalType.HIT_WICKET -> "hit wicket b $resolvedBowlerName"
                DismissalType.RUN_OUT -> if (!fielderName.isNullOrBlank()) "run out ($fielderName)" else "run out"
                DismissalType.RETIRED_HURT -> "retired hurt"
                null -> "out"
            }

            if (dpName == currentStrikerName) {
                updatedStriker = updatedStriker.copy(isOut = true, dismissalDescription = desc)
            } else if (dpName == currentNonStrikerName) {
                val nonStrikerInnings = requireNotNull(inningsRepository.getBatsmanSync(matchId, inningsNumber, currentNonStrikerName))
                updatedNonStriker = nonStrikerInnings.copy(isOut = true, dismissalDescription = desc)
            }
        }
        
        // Save batsman states
        inningsRepository.updateBatsmanInnings(updatedStriker)
        updatedNonStriker?.let { inningsRepository.updateBatsmanInnings(it) }

        // 6. Update Bowler stats
        val bowlerInnings = requireNotNull(inningsRepository.getBowlerSync(matchId, inningsNumber, resolvedBowlerName)) {
            "Bowler innings not found: $resolvedBowlerName"
        }
        
        // Bowler concedes batsman runs, plus wide runs, plus no-ball penalty (1). Byes and legbyes do not count against bowler.
        val conceded = runsBatsman + when (ballType) {
            BallType.WIDE -> runsExtra
            BallType.NO_BALL -> 1
            else -> 0
        }

        // Bowler gets credit for wicket if not run out / retired hurt
        val isBowlerWicket = isWicket && dismissalType != DismissalType.RUN_OUT && dismissalType != DismissalType.RETIRED_HURT

        val updatedBowler = bowlerInnings.copy(
            ballsBowled = bowlerInnings.ballsBowled + (if (isLegal) 1 else 0),
            runsConceded = bowlerInnings.runsConceded + conceded,
            wickets = bowlerInnings.wickets + (if (isBowlerWicket) 1 else 0),
            wides = bowlerInnings.wides + (if (ballType == BallType.WIDE) runsExtra else 0),
            noBalls = bowlerInnings.noBalls + (if (ballType == BallType.NO_BALL) 1 else 0)
        )
        // Note: Maidens will be calculated at the end of the over in UI or database.
        // For simplicity, we can update maidens when over completes.
        inningsRepository.updateBowlerInnings(updatedBowler)

        // 7. Update Innings totals
        var newWide = innings.extrasWide
        var newNoBall = innings.extrasNoBall
        var newBye = innings.extrasBye
        var newLegBye = innings.extrasLegBye

        when (ballType) {
            BallType.WIDE -> newWide += runsExtra
            BallType.NO_BALL -> {
                newNoBall += 1
                if (runsExtra > 1) {
                    newBye += (runsExtra - 1)
                }
            }
            BallType.BYE -> newBye += runsExtra
            BallType.LEG_BYE -> newLegBye += runsExtra
            BallType.NORMAL -> {}
        }

        var updatedInnings = innings.copy(
            totalRuns = innings.totalRuns + runsAdded,
            totalWickets = innings.totalWickets + (if (isWicket) 1 else 0),
            ballsBowled = innings.ballsBowled + (if (isLegal) 1 else 0),
            extrasWide = newWide,
            extrasNoBall = newNoBall,
            extrasBye = newBye,
            extrasLegBye = newLegBye
        )

        // 8. Strike rotation on the ball
        var finalStriker = currentStrikerName
        var finalNonStriker = currentNonStrikerName

        if (isWicket && dismissedPlayerName != null && !nextBatsmanName.isNullOrBlank()) {
            val newPlayer = nextBatsmanName.trim()
            if (dismissedPlayerName == finalStriker) {
                finalStriker = newPlayer
            } else if (dismissedPlayerName == finalNonStriker) {
                finalNonStriker = newPlayer
            }
        }

        if (OversHelper.shouldSwitchStrike(runsBatsman, runsExtra, ballType)) {
            val temp = finalStriker
            finalStriker = finalNonStriker
            finalNonStriker = temp
        }

        // If the over is now complete, swap striker/non-striker ends so that next over starts correctly
        if (isLegal && (legalBallsInCurrentOver + 1) == 6) {
            val temp = finalStriker
            finalStriker = finalNonStriker
            finalNonStriker = temp

            // Also check if bowler bowled a maiden over
            val ballsThisOver = ballsList.filter { it.overNumber == currentOverNo } + savedBall
            val runsConcededThisOver = ballsThisOver.sumOf { b ->
                b.runsBatsman + when (b.ballType) {
                    BallType.WIDE -> b.runsExtra
                    BallType.NO_BALL -> 1
                    else -> 0
                }
            }
            if (runsConcededThisOver == 0) {
                inningsRepository.updateBowlerInnings(updatedBowler.copy(maidens = updatedBowler.maidens + 1))
            }
        }

        // Save new strike arrangement back to the ball record (update it)
        val finalBall = savedBall.copy(strikerName = finalStriker, nonStrikerName = finalNonStriker)
        inningsRepository.recordBall(finalBall)

        // 9. Check Innings Complete
        val totalBatsmen = match.playersPerSide
        val allWicketsDown = updatedInnings.totalWickets >= (totalBatsmen - 1)
        val oversCompleted = updatedInnings.ballsBowled >= (match.oversLimit * 6)
        
        var targetPassed = false
        if (inningsNumber == 2) {
            val firstInnings = inningsRepository.getInningsByNumberSync(matchId, 1)
            if (firstInnings != null) {
                val target = firstInnings.totalRuns + 1
                if (updatedInnings.totalRuns >= target) {
                    targetPassed = true
                }
            }
        }

        if (allWicketsDown || oversCompleted || targetPassed) {
            updatedInnings = updatedInnings.copy(isCompleted = true)
            
            // If 2nd innings is complete, or 1st innings is complete and no more innings can start
            if (inningsNumber == 2) {
                // Match is complete
                val firstInnings = requireNotNull(inningsRepository.getInningsByNumberSync(matchId, 1))
                val runs1 = firstInnings.totalRuns
                val runs2 = updatedInnings.totalRuns
                
                var winner: String? = null
                var winRuns = 0
                var winWkts = 0
                var tied = false

                if (runs2 > runs1) {
                    winner = updatedInnings.battingTeam
                    winWkts = totalBatsmen - updatedInnings.totalWickets
                } else if (runs1 > runs2) {
                    winner = updatedInnings.bowlingTeam
                    winRuns = runs1 - runs2
                } else {
                    tied = true
                }

                // Determine Player of Match: bowler with most wickets or batsman with most runs
                val matchBatsmen = inningsRepository.getBatsmenForInningsSync(matchId, 1) + inningsRepository.getBatsmenForInningsSync(matchId, 2)
                val matchBowlers = inningsRepository.getBowlersForInningsSync(matchId, 1) + inningsRepository.getBowlersForInningsSync(matchId, 2)
                
                val topBatsman = matchBatsmen.maxByOrNull { it.runs }
                val topBowler = matchBowlers.maxByOrNull { it.wickets }
                val pom = if (topBatsman != null && topBowler != null) {
                    if (topBatsman.runs > (topBowler.wickets * 15)) topBatsman.playerName else topBowler.playerName
                } else {
                    topBatsman?.playerName ?: topBowler?.playerName
                }

                matchRepository.updateMatch(
                    match.copy(
                        status = MatchStatus.COMPLETED,
                        winnerTeam = winner,
                        winMarginRuns = winRuns,
                        winMarginWickets = winWkts,
                        isTied = tied,
                        playerOfMatch = pom
                    )
                )
            } else {
                // 1st innings completed, but match is not completed yet (waiting for 2nd innings)
                // Just update match status if needed or handle it
            }
        }

        inningsRepository.updateInnings(updatedInnings)
        return finalBall
    }
}
