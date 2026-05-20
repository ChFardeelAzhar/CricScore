package com.cricscore.app.domain.repository

import com.cricscore.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface InningsRepository {
    suspend fun createInnings(innings: Innings): Long
    suspend fun updateInnings(innings: Innings)
    fun getInningsForMatch(matchId: Long): Flow<List<Innings>>
    suspend fun getInningsForMatchSync(matchId: Long): List<Innings>
    suspend fun getInningsByNumberSync(matchId: Long, inningsNumber: Int): Innings?
    
    // Batsmen
    suspend fun saveBatsmenInnings(batsmen: List<BatsmanInnings>)
    suspend fun updateBatsmanInnings(batsman: BatsmanInnings)
    fun getBatsmenForInnings(matchId: Long, inningsNumber: Int): Flow<List<BatsmanInnings>>
    suspend fun getBatsmenForInningsSync(matchId: Long, inningsNumber: Int): List<BatsmanInnings>
    suspend fun getBatsmanSync(matchId: Long, inningsNumber: Int, playerName: String): BatsmanInnings?

    // Bowlers
    suspend fun saveBowlersInnings(bowlers: List<BowlerInnings>)
    suspend fun updateBowlerConcededRuns(matchId: Long, inningsNumber: Int, playerName: String, runsConceded: Int) // optional helper
    suspend fun updateBowlerInnings(bowler: BowlerInnings)
    fun getBowlersForInnings(matchId: Long, inningsNumber: Int): Flow<List<BowlerInnings>>
    suspend fun getBowlersForInningsSync(matchId: Long, inningsNumber: Int): List<BowlerInnings>
    suspend fun getBowlerSync(matchId: Long, inningsNumber: Int, playerName: String): BowlerInnings?

    // Balls
    suspend fun recordBall(ball: Ball): Long
    suspend fun deleteBall(ball: Ball)
    fun getBallsForInnings(matchId: Long, inningsNumber: Int): Flow<List<Ball>>
    suspend fun getBallsForInningsSync(matchId: Long, inningsNumber: Int): List<Ball>
}
