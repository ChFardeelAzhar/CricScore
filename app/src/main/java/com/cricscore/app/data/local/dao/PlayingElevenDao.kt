package com.cricscore.app.data.local.dao

import androidx.room.*
import com.cricscore.app.data.local.entity.PlayingElevenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayingElevenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: PlayingElevenEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayingElevenEntity>)

    @Update
    suspend fun update(p: PlayingElevenEntity)

    @Query("SELECT * FROM playing_eleven WHERE fixtureId = :fixtureId AND teamId = :teamId ORDER BY battingOrder ASC")
    fun getPlayingEleven(fixtureId: Long, teamId: Long): Flow<List<PlayingElevenEntity>>

    @Query("SELECT * FROM playing_eleven WHERE fixtureId = :fixtureId AND teamId = :teamId ORDER BY battingOrder ASC")
    suspend fun getPlayingElevenSync(fixtureId: Long, teamId: Long): List<PlayingElevenEntity>

    @Query("""
        SELECT * FROM playing_eleven 
        WHERE fixtureId = :fixtureId 
        AND teamId = :teamId 
        AND hasAlreadyBatted = 0 
        AND isCurrentlyBatting = 0
        AND isOut = 0
        ORDER BY battingOrder ASC
    """)
    suspend fun getAvailableBatsmen(fixtureId: Long, teamId: Long): List<PlayingElevenEntity>

    @Query("""
        SELECT * FROM playing_eleven 
        WHERE fixtureId = :fixtureId 
        AND teamId = :teamId
        ORDER BY playerName ASC
    """)
    suspend fun getAvailableBowlers(fixtureId: Long, teamId: Long): List<PlayingElevenEntity>

    @Query("SELECT * FROM playing_eleven WHERE fixtureId = :fixtureId AND teamId = :teamId AND isCurrentlyBatting = 1")
    suspend fun getCurrentBatsmen(fixtureId: Long, teamId: Long): List<PlayingElevenEntity>

    @Query("UPDATE playing_eleven SET isOut = 1, isCurrentlyBatting = 0, hasAlreadyBatted = 1 WHERE fixtureId = :fixtureId AND playerId = :playerId")
    suspend fun markPlayerOut(fixtureId: Long, playerId: Long)

    @Query("UPDATE playing_eleven SET isCurrentlyBatting = 1, hasAlreadyBatted = 1 WHERE fixtureId = :fixtureId AND playerId = :playerId")
    suspend fun markPlayerBatting(fixtureId: Long, playerId: Long)

    @Query("SELECT COUNT(*) FROM playing_eleven WHERE fixtureId = :fixtureId AND teamId = :teamId")
    suspend fun getSelectedCount(fixtureId: Long, teamId: Long): Int

    @Query("DELETE FROM playing_eleven WHERE fixtureId = :fixtureId AND teamId = :teamId")
    suspend fun clearPlayingEleven(fixtureId: Long, teamId: Long)

    @Query("SELECT COUNT(*) FROM playing_eleven WHERE playerId = :playerId")
    suspend fun getPlayerSelectionCount(playerId: Long): Int

    @Query("SELECT * FROM playing_eleven WHERE id = :id")
    suspend fun getById(id: Long): PlayingElevenEntity?
}
