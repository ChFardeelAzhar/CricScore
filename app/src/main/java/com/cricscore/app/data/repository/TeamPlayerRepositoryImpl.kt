package com.cricscore.app.data.repository

import com.cricscore.app.data.local.dao.TeamPlayerDao
import com.cricscore.app.data.local.entity.toDomain
import com.cricscore.app.data.local.entity.toEntity
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.domain.repository.TeamPlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TeamPlayerRepositoryImpl @Inject constructor(
    private val teamPlayerDao: TeamPlayerDao
) : TeamPlayerRepository {

    override suspend fun savePlayer(player: TeamPlayer): Long {
        return teamPlayerDao.insert(player.toEntity())
    }

    override suspend fun savePlayers(players: List<TeamPlayer>) {
        teamPlayerDao.insertAll(players.map { it.toEntity() })
    }

    override suspend fun updatePlayer(player: TeamPlayer) {
        teamPlayerDao.update(player.toEntity())
    }

    override suspend fun deletePlayer(player: TeamPlayer) {
        teamPlayerDao.delete(player.toEntity())
    }

    override fun getPlayersByTeam(teamId: Long): Flow<List<TeamPlayer>> {
        return teamPlayerDao.getPlayersByTeam(teamId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPlayersByTeamSync(teamId: Long): List<TeamPlayer> {
        return teamPlayerDao.getPlayersByTeamSync(teamId).map { it.toDomain() }
    }

    override suspend fun getPlayersByTournament(tournamentId: Long): List<TeamPlayer> {
        return teamPlayerDao.getPlayersByTournament(tournamentId).map { it.toDomain() }
    }

    override suspend fun getCaptain(teamId: Long): TeamPlayer? {
        return teamPlayerDao.getCaptain(teamId)?.toDomain()
    }

    override suspend fun getPlayerCount(teamId: Long): Int {
        return teamPlayerDao.getPlayerCount(teamId)
    }

    override suspend fun countByName(teamId: Long, name: String): Int {
        return teamPlayerDao.countByName(teamId, name)
    }

    override suspend fun clearCaptain(teamId: Long) {
        teamPlayerDao.clearCaptain(teamId)
    }

    override suspend fun clearViceCaptain(teamId: Long) {
        teamPlayerDao.clearViceCaptain(teamId)
    }

    override suspend fun clearWicketKeeper(teamId: Long) {
        teamPlayerDao.clearWicketKeeper(teamId)
    }

    override suspend fun getPlayerById(id: Long): TeamPlayer? {
        return teamPlayerDao.getById(id)?.toDomain()
    }
}
