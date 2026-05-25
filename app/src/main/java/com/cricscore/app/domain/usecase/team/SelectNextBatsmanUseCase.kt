package com.cricscore.app.domain.usecase.team

import com.cricscore.app.domain.repository.PlayingElevenRepository
import javax.inject.Inject

class SelectNextBatsmanUseCase @Inject constructor(
    private val playingElevenRepository: PlayingElevenRepository
) {
    suspend operator fun invoke(
        fixtureId: Long,
        playingElevenId: Long,
        previousBatsmanId: Long,
        isStriker: Boolean
    ): Result<String> {
        val player = playingElevenRepository.getPlayingElevenPlayerById(playingElevenId)
            ?: return Result.failure(Exception("Player not found"))
            
        playingElevenRepository.markPlayerBatting(fixtureId, player.playerId)
        
        return Result.success(player.playerName)
    }
}
