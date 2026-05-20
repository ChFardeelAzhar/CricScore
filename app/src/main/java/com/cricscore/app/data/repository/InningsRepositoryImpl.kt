package com.cricscore.app.data.repository

import com.cricscore.app.data.local.dao.*
import com.cricscore.app.data.local.entity.*
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.InningsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InningsRepositoryImpl @Inject constructor(
    private val inningsDao: InningsDao,
    private val ballDao: BallDao,
    private val batsmanInningsDao: BatsmanInningsDao,
    private val bowlerInningsDao: BowlerInningsDao
) : InningsRepository {

    override suspend fun createInnings(innings: Innings): Long {
        return inningsDao.insert(innings.toEntity())
    }

    override suspend fun updateInnings(innings: Innings) {
        inningsDao.update(innings.toEntity())
    }

    override fun getInningsForMatch(matchId: Long): Flow<List<Innings>> {
        return inningsDao.getInningsForMatch(matchId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getInningsForMatchSync(matchId: Long): List<Innings> {
        return inningsDao.getInningsForMatchSync(matchId).map { it.toDomain() }
    }

    override suspend fun getInningsByNumberSync(matchId: Long, inningsNumber: Int): Innings? {
        return inningsDao.getInningsByNumberSync(matchId, inningsNumber)?.toDomain()
    }

    override suspend fun saveBatsmenInnings(batsmen: List<BatsmanInnings>) {
        batsmanInningsDao.insertAll(batsmen.map { it.toEntity() })
    }

    override suspend fun updateBatsmanInnings(batsman: BatsmanInnings) {
        batsmanInningsDao.update(batsman.toEntity())
    }

    override fun getBatsmenForInnings(matchId: Long, inningsNumber: Int): Flow<List<BatsmanInnings>> {
        return batsmanInningsDao.getBatsmanInnings(matchId, inningsNumber).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getBatsmenForInningsSync(matchId: Long, inningsNumber: Int): List<BatsmanInnings> {
        return batsmanInningsDao.getBatsmanInningsSync(matchId, inningsNumber).map { it.toDomain() }
    }

    override suspend fun getBatsmanSync(matchId: Long, inningsNumber: Int, playerName: String): BatsmanInnings? {
        return batsmanInningsDao.getBatsmanSync(matchId, inningsNumber, playerName)?.toDomain()
    }

    override suspend fun saveBowlersInnings(bowlers: List<BowlerInnings>) {
        bowlerInningsDao.insertAll(bowlers.map { it.toEntity() })
    }

    override suspend fun updateBowlerConcededRuns(
        matchId: Long,
        inningsNumber: Int,
        playerName: String,
        runsConceded: Int
    ) {
        val bowler = bowlerInningsDao.getBowlerSync(matchId, inningsNumber, playerName)
        if (bowler != null) {
            bowlerInningsDao.update(bowler.copy(runsConceded = runsConceded))
        }
    }

    override suspend fun updateBowlerInnings(bowler: BowlerInnings) {
        bowlerInningsDao.update(bowler.toEntity())
    }

    override fun getBowlersForInnings(matchId: Long, inningsNumber: Int): Flow<List<BowlerInnings>> {
        return bowlerInningsDao.getBowlerInnings(matchId, inningsNumber).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getBowlersForInningsSync(matchId: Long, inningsNumber: Int): List<BowlerInnings> {
        return bowlerInningsDao.getBowlerInningsSync(matchId, inningsNumber).map { it.toDomain() }
    }

    override suspend fun getBowlerSync(matchId: Long, inningsNumber: Int, playerName: String): BowlerInnings? {
        return bowlerInningsDao.getBowlerSync(matchId, inningsNumber, playerName)?.toDomain()
    }

    override suspend fun recordBall(ball: Ball): Long {
        return ballDao.insert(ball.toEntity())
    }

    override suspend fun deleteBall(ball: Ball) {
        ballDao.delete(ball.toEntity())
    }

    override fun getBallsForInnings(matchId: Long, inningsNumber: Int): Flow<List<Ball>> {
        return ballDao.getBallsForInnings(matchId, inningsNumber).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getBallsForInningsSync(matchId: Long, inningsNumber: Int): List<Ball> {
        return ballDao.getBallsForInningsSync(matchId, inningsNumber).map { it.toDomain() }
    }
}
