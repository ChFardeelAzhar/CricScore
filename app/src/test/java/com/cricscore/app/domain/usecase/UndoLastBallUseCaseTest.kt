package com.cricscore.app.domain.usecase

import com.cricscore.app.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class UndoLastBallUseCaseTest {

    private lateinit var matchRepository: RecordBallUseCaseTest.FakeMatchRepository
    private lateinit var inningsRepository: RecordBallUseCaseTest.FakeInningsRepository
    private lateinit var recordBallUseCase: RecordBallUseCase
    private lateinit var undoLastBallUseCase: UndoLastBallUseCase

    private val matchId = 1L
    private val inningsNumber = 1

    @Before
    fun setUp() {
        matchRepository = RecordBallUseCaseTest.FakeMatchRepository()
        inningsRepository = RecordBallUseCaseTest.FakeInningsRepository()
        recordBallUseCase = RecordBallUseCase(matchRepository, inningsRepository)
        undoLastBallUseCase = UndoLastBallUseCase(matchRepository, inningsRepository)

        runBlocking {
            val match = Match(
                id = matchId,
                team1 = "Team A",
                team2 = "Team B",
                oversLimit = 5,
                playersPerSide = 11,
                tossWinner = "Team A",
                tossDecision = TossResult.BAT,
                status = MatchStatus.FIRST_INNINGS
            )
            matchRepository.createMatch(match)

            val innings = Innings(
                matchId = matchId,
                inningsNumber = inningsNumber,
                battingTeam = "Team A",
                bowlingTeam = "Team B"
            )
            inningsRepository.createInnings(innings)
            inningsRepository.saveBatsmenInnings(
                listOf(
                    BatsmanInnings(matchId = matchId, inningsNumber = inningsNumber, playerName = "Striker"),
                    BatsmanInnings(matchId = matchId, inningsNumber = inningsNumber, playerName = "Non-Striker")
                )
            )
            inningsRepository.saveBowlersInnings(
                listOf(
                    BowlerInnings(matchId = matchId, inningsNumber = inningsNumber, playerName = "Bowler")
                )
            )
        }
    }

    @Test
    fun testUndoLastBallRestoresState() = runBlocking {
        // 1. Record Ball 1: 4 runs by Striker
        recordBallUseCase(
            matchId = matchId,
            inningsNumber = inningsNumber,
            runsBatsman = 4,
            runsExtra = 0,
            ballType = BallType.NORMAL
        )

        // 2. Record Ball 2: 1 run by Striker (strike switches)
        recordBallUseCase(
            matchId = matchId,
            inningsNumber = inningsNumber,
            runsBatsman = 1,
            runsExtra = 0,
            ballType = BallType.NORMAL
        )

        // Verify state before Undo
        var innings = inningsRepository.getInningsByNumberSync(matchId, inningsNumber)
        assertNotNull(innings)
        assertEquals(5, innings!!.totalRuns)
        assertEquals(2, innings.ballsBowled)

        var striker = inningsRepository.getBatsmanSync(matchId, inningsNumber, "Striker")
        assertNotNull(striker)
        assertEquals(5, striker!!.runs)
        assertEquals(2, striker.balls)

        // 3. Perform Undo
        undoLastBallUseCase(matchId, inningsNumber)

        // Verify state after Undo (should match Ball 1 state)
        innings = inningsRepository.getInningsByNumberSync(matchId, inningsNumber)
        assertNotNull(innings)
        assertEquals(4, innings!!.totalRuns)
        assertEquals(1, innings.ballsBowled)

        striker = inningsRepository.getBatsmanSync(matchId, inningsNumber, "Striker")
        assertNotNull(striker)
        assertEquals(4, striker!!.runs)
        assertEquals(1, striker.balls)

        val nonStriker = inningsRepository.getBatsmanSync(matchId, inningsNumber, "Non-Striker")
        assertNotNull(nonStriker)
        assertEquals(0, nonStriker!!.runs)
        assertEquals(0, nonStriker.balls)

        val bowler = inningsRepository.getBowlerSync(matchId, inningsNumber, "Bowler")
        assertNotNull(bowler)
        assertEquals(1, bowler!!.ballsBowled)
        assertEquals(4, bowler.runsConceded)
    }
}
