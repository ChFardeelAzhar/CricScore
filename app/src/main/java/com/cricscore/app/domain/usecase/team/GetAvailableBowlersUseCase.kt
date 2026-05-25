package com.cricscore.app.domain.usecase.team

import com.cricscore.app.domain.model.PlayingElevenPlayer
import com.cricscore.app.domain.repository.PlayingElevenRepository
import javax.inject.Inject

class GetAvailableBowlersUseCase @Inject constructor(
    private val playingElevenRepository: PlayingElevenRepository
) {
    suspend operator fun invoke(fixtureId: Long, teamId: Long): List<PlayingElevenPlayer> {
        return playingElevenRepository.getAvailableBowlers(fixtureId, teamId)
    }
}
