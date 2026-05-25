package com.cricscore.app.domain.repository

import com.cricscore.app.domain.model.TeamPlayer
import kotlinx.coroutines.flow.Flow

interface TeamPlayerRepository {
    suspend fun savePlayer(player: TeamPlayer): Long
    suspend fun savePlayers(players: List<TeamPlayer>)
    suspend fun updatePlayer(player: TeamPlayer)
    suspend fun deletePlayer(player: TeamPlayer)
    fun getPlayersByTeam(teamId: Long): Flow<List<TeamPlayer>>
    suspend fun getPlayersByTeamSync(teamId: Long): List<TeamPlayer>
    suspend fun getPlayersByTournament(tournamentId: Long): List<TeamPlayer>
    suspend fun getCaptain(teamId: Long): TeamPlayer?
    suspend fun getPlayerCount(teamId: Long): Int
    suspend fun countByName(teamId: Long, name: String): Int
    suspend fun clearCaptain(teamId: Long)
    suspend fun clearViceCaptain(teamId: Long)
    suspend fun clearWicketKeeper(teamId: Long)
    suspend fun getPlayerById(id: Long): TeamPlayer?
}
