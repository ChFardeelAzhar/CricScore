package com.cricscore.app.domain.usecase

import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecordBallUseCaseTest {

    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var inningsRepository: FakeInningsRepository
    private lateinit var recordBallUseCase: RecordBallUseCase

    private val matchId = 1L
    private val inningsNumber = 1

    @Before
    fun setUp() {
        matchRepository = FakeMatchRepository()
        inningsRepository = FakeInningsRepository()
        recordBallUseCase = RecordBallUseCase(matchRepository, inningsRepository)

        // Seed initial data
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
                    BatsmanInnings(matchId = matchId, inningsNumber = inningsNumber, playerName = "Non-Striker"),
                    BatsmanInnings(matchId = matchId, inningsNumber = inningsNumber, playerName = "New Batsman")
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
    fun testRecordNormalBallFourRuns() = runBlocking {
        val ball = recordBallUseCase(
            matchId = matchId,
            inningsNumber = inningsNumber,
            runsBatsman = 4,
            runsExtra = 0,
            ballType = BallType.NORMAL
        )

        assertNotNull(ball)
        assertEquals(0, ball.overNumber)
        assertEquals(1, ball.ballNumber)
        assertEquals(4, ball.runsBatsman)
        assertEquals(0, ball.runsExtra)
        assertEquals("Striker", ball.strikerName)
        assertEquals("Non-Striker", ball.nonStrikerName)

        val striker = inningsRepository.getBatsmanSync(matchId, inningsNumber, "Striker")
        assertNotNull(striker)
        assertEquals(4, striker!!.runs)
        assertEquals(1, striker.balls)
        assertEquals(1, striker.fours)

        val bowler = inningsRepository.getBowlerSync(matchId, inningsNumber, "Bowler")
        assertNotNull(bowler)
        assertEquals(1, bowler!!.ballsBowled)
        assertEquals(4, bowler.runsConceded)

        val innings = inningsRepository.getInningsByNumberSync(matchId, inningsNumber)
        assertNotNull(innings)
        assertEquals(4, innings!!.totalRuns)
        assertEquals(1, innings.ballsBowled)
    }

    @Test
    fun testStrikeRotationOnOddRuns() = runBlocking {
        val ball = recordBallUseCase(
            matchId = matchId,
            inningsNumber = inningsNumber,
            runsBatsman = 1,
            runsExtra = 0,
            ballType = BallType.NORMAL
        )

        // Strike should switch. So the next ball's saved configuration will have Non-Striker facing next.
        // Wait, RecordBallUseCase updates the ball's striker/nonStriker field to represent the state *after* the ball is bowled
        assertEquals("Non-Striker", ball.strikerName)
        assertEquals("Striker", ball.nonStrikerName)
    }

    @Test
    fun testWicketDismissal() = runBlocking {
        val ball = recordBallUseCase(
            matchId = matchId,
            inningsNumber = inningsNumber,
            runsBatsman = 0,
            runsExtra = 0,
            ballType = BallType.NORMAL,
            isWicket = true,
            dismissalType = DismissalType.BOWLED,
            dismissedPlayerName = "Striker"
        )

        val dismissed = inningsRepository.getBatsmanSync(matchId, inningsNumber, "Striker")
        assertNotNull(dismissed)
        assertTrue(dismissed!!.isOut)
        assertEquals("b Bowler", dismissed.dismissalDescription)

        // Recording next ball will replace the dismissed striker with "New Batsman"
        val nextBall = recordBallUseCase(
            matchId = matchId,
            inningsNumber = inningsNumber,
            runsBatsman = 0,
            runsExtra = 0,
            ballType = BallType.NORMAL
        )
        assertEquals("New Batsman", nextBall.strikerName)
    }

    @Test
    fun testRecordBallWithExplicitBowlerName() = runBlocking {
        // Record 1st over (6 legal balls) with the default bowler
        for (i in 1..6) {
            recordBallUseCase(
                matchId = matchId,
                inningsNumber = inningsNumber,
                runsBatsman = 0,
                runsExtra = 0,
                ballType = BallType.NORMAL
            )
        }

        // Now record the next ball of the new over with an explicitly selected bowler "NewBowler"
        val ball = recordBallUseCase(
            matchId = matchId,
            inningsNumber = inningsNumber,
            runsBatsman = 1,
            runsExtra = 0,
            ballType = BallType.NORMAL,
            bowlerName = "NewBowler"
        )

        assertEquals("NewBowler", ball.bowlerName)

        val bowler = inningsRepository.getBowlerSync(matchId, inningsNumber, "NewBowler")
        assertNotNull(bowler)
        assertEquals(1, bowler!!.ballsBowled)
        assertEquals(1, bowler.runsConceded)
    }

    // --- Fake Repositories ---

    class FakeMatchRepository : MatchRepository {
        private val matches = mutableMapOf<Long, Match>()

        override suspend fun createMatch(match: Match): Long {
            matches[match.id] = match
            return match.id
        }

        override suspend fun updateMatch(match: Match) {
            matches[match.id] = match
        }

        override fun getMatchById(id: Long): Flow<Match?> = flowOf(matches[id])

        override suspend fun getMatchByIdSync(id: Long): Match? = matches[id]

        override fun getRecentMatches(): Flow<List<Match>> = flowOf(matches.values.toList())
    }

    class FakeInningsRepository : InningsRepository {
        private val inningsList = mutableMapOf<String, Innings>()
        private val batsmenList = mutableMapOf<String, BatsmanInnings>()
        private val bowlersList = mutableMapOf<String, BowlerInnings>()
        private val ballsList = mutableListOf<Ball>()
        private var ballIdCounter = 1L

        private fun key(matchId: Long, num: Int) = "$matchId-$num"
        private fun playerKey(matchId: Long, num: Int, name: String) = "$matchId-$num-$name"

        override suspend fun createInnings(innings: Innings): Long {
            inningsList[key(innings.matchId, innings.inningsNumber)] = innings
            return 1L
        }

        override suspend fun updateInnings(innings: Innings) {
            inningsList[key(innings.matchId, innings.inningsNumber)] = innings
        }

        override fun getInningsForMatch(matchId: Long): Flow<List<Innings>> {
            return flowOf(inningsList.values.filter { it.matchId == matchId })
        }

        override suspend fun getInningsForMatchSync(matchId: Long): List<Innings> {
            return inningsList.values.filter { it.matchId == matchId }
        }

        override suspend fun getInningsByNumberSync(matchId: Long, inningsNumber: Int): Innings? {
            return inningsList[key(matchId, inningsNumber)]
        }

        override suspend fun saveBatsmenInnings(batsmen: List<BatsmanInnings>) {
            batsmen.forEach {
                batsmenList[playerKey(it.matchId, it.inningsNumber, it.playerName)] = it
            }
        }

        override suspend fun updateBatsmanInnings(batsman: BatsmanInnings) {
            batsmenList[playerKey(batsman.matchId, batsman.inningsNumber, batsman.playerName)] = batsman
        }

        override fun getBatsmenForInnings(matchId: Long, inningsNumber: Int): Flow<List<BatsmanInnings>> {
            return flowOf(batsmenList.values.filter { it.matchId == matchId && it.inningsNumber == inningsNumber })
        }

        override suspend fun getBatsmenForInningsSync(matchId: Long, inningsNumber: Int): List<BatsmanInnings> {
            return batsmenList.values.filter { it.matchId == matchId && it.inningsNumber == inningsNumber }
        }

        override suspend fun getBatsmanSync(matchId: Long, inningsNumber: Int, playerName: String): BatsmanInnings? {
            return batsmenList[playerKey(matchId, inningsNumber, playerName)]
        }

        override suspend fun saveBowlersInnings(bowlers: List<BowlerInnings>) {
            bowlers.forEach {
                bowlersList[playerKey(it.matchId, it.inningsNumber, it.playerName)] = it
            }
        }

        override suspend fun updateBowlerConcededRuns(
            matchId: Long,
            inningsNumber: Int,
            playerName: String,
            runsConceded: Int
        ) {
            val b = bowlersList[playerKey(matchId, inningsNumber, playerName)]
            if (b != null) {
                bowlersList[playerKey(matchId, inningsNumber, playerName)] = b.copy(runsConceded = runsConceded)
            }
        }

        override suspend fun updateBowlerInnings(bowler: BowlerInnings) {
            bowlersList[playerKey(bowler.matchId, bowler.inningsNumber, bowler.playerName)] = bowler
        }

        override fun getBowlersForInnings(matchId: Long, inningsNumber: Int): Flow<List<BowlerInnings>> {
            return flowOf(bowlersList.values.filter { it.matchId == matchId && it.inningsNumber == inningsNumber })
        }

        override suspend fun getBowlersForInningsSync(matchId: Long, inningsNumber: Int): List<BowlerInnings> {
            return bowlersList.values.filter { it.matchId == matchId && it.inningsNumber == inningsNumber }
        }

        override suspend fun getBowlerSync(matchId: Long, inningsNumber: Int, playerName: String): BowlerInnings? {
            return bowlersList[playerKey(matchId, inningsNumber, playerName)]
        }

        override suspend fun recordBall(ball: Ball): Long {
            val existingIdx = ballsList.indexOfFirst { it.id == ball.id && ball.id != 0L }
            if (existingIdx != -1) {
                ballsList[existingIdx] = ball
                return ball.id
            } else {
                val b = ball.copy(id = ballIdCounter++)
                ballsList.add(b)
                return b.id
            }
        }

        override suspend fun deleteBall(ball: Ball) {
            ballsList.removeIf { it.id == ball.id }
        }

        override fun getBallsForInnings(matchId: Long, inningsNumber: Int): Flow<List<Ball>> {
            return flowOf(ballsList.filter { it.matchId == matchId && it.inningsNumber == inningsNumber })
        }

        override suspend fun getBallsForInningsSync(matchId: Long, inningsNumber: Int): List<Ball> {
            return ballsList.filter { it.matchId == matchId && it.inningsNumber == inningsNumber }
        }
    }
}
