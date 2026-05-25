package com.cricscore.app.data.local.dao

import androidx.room.*
import com.cricscore.app.data.local.entity.TeamPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamPlayerDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(player: TeamPlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(players: List<TeamPlayerEntity>)

    @Update
    suspend fun update(player: TeamPlayerEntity)

    @Delete
    suspend fun delete(player: TeamPlayerEntity)

    @Query("SELECT * FROM team_players WHERE teamId = :teamId ORDER BY jerseyNumber ASC, playerName ASC")
    fun getPlayersByTeam(teamId: Long): Flow<List<TeamPlayerEntity>>

    @Query("SELECT * FROM team_players WHERE teamId = :teamId ORDER BY jerseyNumber ASC, playerName ASC")
    suspend fun getPlayersByTeamSync(teamId: Long): List<TeamPlayerEntity>

    @Query("SELECT * FROM team_players WHERE tournamentId = :tournamentId")
    suspend fun getPlayersByTournament(tournamentId: Long): List<TeamPlayerEntity>

    @Query("SELECT * FROM team_players WHERE teamId = :teamId AND isCaptain = 1 LIMIT 1")
    suspend fun getCaptain(teamId: Long): TeamPlayerEntity?

    @Query("SELECT COUNT(*) FROM team_players WHERE teamId = :teamId AND LOWER(playerName) = LOWER(:name)")
    suspend fun countByName(teamId: Long, name: String): Int

    @Query("SELECT COUNT(*) FROM team_players WHERE teamId = :teamId")
    suspend fun getPlayerCount(teamId: Long): Int

    @Query("UPDATE team_players SET isCaptain = 0 WHERE teamId = :teamId")
    suspend fun clearCaptain(teamId: Long)

    @Query("UPDATE team_players SET isViceCaptain = 0 WHERE teamId = :teamId")
    suspend fun clearViceCaptain(teamId: Long)

    @Query("UPDATE team_players SET isWicketKeeper = 0 WHERE teamId = :teamId")
    suspend fun clearWicketKeeper(teamId: Long)

    @Query("SELECT * FROM team_players WHERE id = :id")
    suspend fun getById(id: Long): TeamPlayerEntity?
}
