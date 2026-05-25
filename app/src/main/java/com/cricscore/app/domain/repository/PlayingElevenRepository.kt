package com.cricscore.app.domain.repository

import com.cricscore.app.domain.model.PlayingElevenPlayer
import kotlinx.coroutines.flow.Flow

interface PlayingElevenRepository {
    suspend fun savePlayingEleven(players: List<PlayingElevenPlayer>)
    suspend fun updatePlayingElevenPlayer(player: PlayingElevenPlayer)
    fun getPlayingEleven(fixtureId: Long, teamId: Long): Flow<List<PlayingElevenPlayer>>
    suspend fun getPlayingElevenSync(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer>
    suspend fun getAvailableBatsmen(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer>
    suspend fun getAvailableBowlers(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer>
    suspend fun getCurrentBatsmen(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer>
    suspend fun markPlayerOut(fixtureId: Long, playerId: Long)
    suspend fun markPlayerBatting(fixtureId: Long, playerId: Long)
    suspend fun getSelectedCount(fixtureId: Long, teamId: Long): Int
    suspend fun clearPlayingEleven(fixtureId: Long, teamId: Long)
    suspend fun getPlayerSelectionCount(playerId: Long): Int
    suspend fun getPlayingElevenPlayerById(id: Long): PlayingElevenPlayer?
}
