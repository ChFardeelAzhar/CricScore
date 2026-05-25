package com.cricscore.app.domain.usecase.team

import com.cricscore.app.domain.repository.TeamPlayerRepository
import javax.inject.Inject

class SetViceCaptainUseCase @Inject constructor(
    private val teamPlayerRepository: TeamPlayerRepository
) {
    suspend operator fun invoke(teamId: Long, playerId: Long) {
        teamPlayerRepository.clearViceCaptain(teamId)
        val player = teamPlayerRepository.getPlayerById(playerId) ?: return
        teamPlayerRepository.updatePlayer(player.copy(isViceCaptain = true, isCaptain = false))
    }
}
