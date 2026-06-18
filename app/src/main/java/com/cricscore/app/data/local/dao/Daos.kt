package com.cricscore.app.data.local.dao

import androidx.room.*
import com.cricscore.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: MatchEntity): Long

    @Update
    suspend fun update(match: MatchEntity): Int

    @Query("SELECT * FROM matches WHERE id = :id")
    fun getMatchById(id: Long): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchByIdSync(id: Long): MatchEntity?

    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun getRecentMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId IS NULL ORDER BY createdAt DESC")
    fun getStandaloneMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tId ORDER BY createdAt DESC")
    fun getTournamentMatches(tId: Long): Flow<List<MatchEntity>>
}

@Dao
interface InningsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(innings: InningsEntity): Long

    @Update
    suspend fun update(innings: InningsEntity): Int

    @Query("SELECT * FROM innings WHERE matchId = :matchId")
    fun getInningsForMatch(matchId: Long): Flow<List<InningsEntity>>

    @Query("SELECT * FROM innings WHERE matchId = :matchId")
    suspend fun getInningsForMatchSync(matchId: Long): List<InningsEntity>

    @Query("SELECT * FROM innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber LIMIT 1")
    suspend fun getInningsByNumberSync(matchId: Long, inningsNumber: Int): InningsEntity?
}

@Dao
interface BallDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ball: BallEntity): Long

    @Delete
    suspend fun delete(ball: BallEntity): Int

    @Query("SELECT * FROM balls WHERE matchId = :matchId AND inningsNumber = :inningsNumber ORDER BY timestamp ASC")
    fun getBallsForInnings(matchId: Long, inningsNumber: Int): Flow<List<BallEntity>>

    @Query("SELECT * FROM balls WHERE matchId = :matchId AND inningsNumber = :inningsNumber ORDER BY timestamp ASC")
    suspend fun getBallsForInningsSync(matchId: Long, inningsNumber: Int): List<BallEntity>

    @Query("DELETE FROM balls WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}

@Dao
interface BatsmanInningsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batsman: BatsmanInningsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batsmen: List<BatsmanInningsEntity>): List<Long>

    @Update
    suspend fun update(batsman: BatsmanInningsEntity): Int

    @Query("SELECT * FROM batsman_innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber")
    fun getBatsmanInnings(matchId: Long, inningsNumber: Int): Flow<List<BatsmanInningsEntity>>

    @Query("SELECT * FROM batsman_innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber")
    suspend fun getBatsmanInningsSync(matchId: Long, inningsNumber: Int): List<BatsmanInningsEntity>

    @Query("SELECT * FROM batsman_innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber AND playerName = :playerName LIMIT 1")
    suspend fun getBatsmanSync(matchId: Long, inningsNumber: Int, playerName: String): BatsmanInningsEntity?
}

@Dao
interface BowlerInningsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bowler: BowlerInningsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bowlers: List<BowlerInningsEntity>): List<Long>

    @Update
    suspend fun update(bowler: BowlerInningsEntity): Int

    @Query("SELECT * FROM bowler_innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber")
    fun getBowlerInnings(matchId: Long, inningsNumber: Int): Flow<List<BowlerInningsEntity>>

    @Query("SELECT * FROM bowler_innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber")
    suspend fun getBowlerInningsSync(matchId: Long, inningsNumber: Int): List<BowlerInningsEntity>

    @Query("SELECT * FROM bowler_innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber AND playerName = :playerName LIMIT 1")
    suspend fun getBowlerSync(matchId: Long, inningsNumber: Int, playerName: String): BowlerInningsEntity?
}
