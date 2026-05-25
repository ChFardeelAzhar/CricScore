package com.cricscore.app.domain.usecase.team

import com.cricscore.app.domain.repository.TeamPlayerRepository
import javax.inject.Inject

class SetCaptainUseCase @Inject constructor(
    private val teamPlayerRepository: TeamPlayerRepository
) {
    suspend operator fun invoke(teamId: Long, playerId: Long) {
        teamPlayerRepository.clearCaptain(teamId)
        val player = teamPlayerRepository.getPlayerById(playerId) ?: return
        teamPlayerRepository.updatePlayer(player.copy(isCaptain = true, isViceCaptain = false))
    }
}
