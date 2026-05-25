package com.cricscore.app.data.repository

import com.cricscore.app.data.local.dao.PlayingElevenDao
import com.cricscore.app.data.local.entity.toDomain
import com.cricscore.app.data.local.entity.toEntity
import com.cricscore.app.domain.model.PlayingElevenPlayer
import com.cricscore.app.domain.repository.PlayingElevenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayingElevenRepositoryImpl @Inject constructor(
    private val playingElevenDao: PlayingElevenDao
) : PlayingElevenRepository {

    override suspend fun savePlayingEleven(players: List<PlayingElevenPlayer>) {
        playingElevenDao.insertAll(players.map { it.toEntity() })
    }

    override suspend fun updatePlayingElevenPlayer(player: PlayingElevenPlayer) {
        playingElevenDao.update(player.toEntity())
    }

    override fun getPlayingEleven(fixtureId: Long, teamId: Long): Flow<List<PlayingElevenPlayer>> {
        return playingElevenDao.getPlayingEleven(fixtureId, teamId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPlayingElevenSync(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer> {
        return playingElevenDao.getPlayingElevenSync(fixtureId, teamId).map { it.toDomain() }
    }

    override suspend fun getAvailableBatsmen(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer> {
        return playingElevenDao.getAvailableBatsmen(fixtureId, teamId).map { it.toDomain() }
    }

    override suspend fun getAvailableBowlers(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer> {
        return playingElevenDao.getAvailableBowlers(fixtureId, teamId).map { it.toDomain() }
    }

    override suspend fun getCurrentBatsmen(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer> {
        return playingElevenDao.getCurrentBatsmen(fixtureId, teamId).map { it.toDomain() }
    }

    override suspend fun markPlayerOut(fixtureId: Long, playerId: Long) {
        playingElevenDao.markPlayerOut(fixtureId, playerId)
    }

    override suspend fun markPlayerBatting(fixtureId: Long, playerId: Long) {
        playingElevenDao.markPlayerBatting(fixtureId, playerId)
    }

    override suspend fun getSelectedCount(fixtureId: Long, teamId: Long): Int {
        return playingElevenDao.getSelectedCount(fixtureId, teamId)
    }

    override suspend fun clearPlayingEleven(fixtureId: Long, teamId: Long) {
        playingElevenDao.clearPlayingEleven(fixtureId, teamId)
    }

    override suspend fun getPlayerSelectionCount(playerId: Long): Int {
        return playingElevenDao.getPlayerSelectionCount(playerId)
    }

    override suspend fun getPlayingElevenPlayerById(id: Long): PlayingElevenPlayer? {
        return playingElevenDao.getById(id)?.toDomain()
    }
}
