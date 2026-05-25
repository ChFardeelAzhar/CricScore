package com.cricscore.app.domain.usecase.team

import com.cricscore.app.domain.repository.TeamPlayerRepository
import com.cricscore.app.domain.repository.PlayingElevenRepository
import javax.inject.Inject

class RemovePlayerFromTeamUseCase @Inject constructor(
    private val teamPlayerRepository: TeamPlayerRepository,
    private val playingElevenRepository: PlayingElevenRepository
) {
    suspend operator fun invoke(playerId: Long): Result<Unit> {
        val player = teamPlayerRepository.getPlayerById(playerId)
            ?: return Result.failure(Exception("Player not found"))

        val count = playingElevenRepository.getPlayerSelectionCount(playerId)
        if (count > 0) {
            return Result.failure(Exception("Cannot remove — player has match history"))
        }

        teamPlayerRepository.deletePlayer(player)
        return Result.success(Unit)
    }
}
