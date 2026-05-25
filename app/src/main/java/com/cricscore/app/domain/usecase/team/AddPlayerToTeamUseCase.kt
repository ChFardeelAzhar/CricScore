package com.cricscore.app.domain.usecase.team

import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.domain.repository.TeamPlayerRepository
import javax.inject.Inject

class AddPlayerToTeamUseCase @Inject constructor(
    private val teamPlayerRepository: TeamPlayerRepository
) {
    suspend operator fun invoke(
        teamId: Long,
        tournamentId: Long,
        name: String,
        jerseyNumber: Int,
        role: String
    ): Result<TeamPlayer> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return Result.failure(Exception("Name cannot be empty"))
        if (trimmedName.length > 25) return Result.failure(Exception("Name too long"))
        
        val count = teamPlayerRepository.countByName(teamId, trimmedName)
        if (count > 0) return Result.failure(Exception("Player '$trimmedName' already in squad"))
        
        val current = teamPlayerRepository.getPlayerCount(teamId)
        if (current >= 20) return Result.failure(Exception("Squad is full (max 20 players)"))
        
        val player = TeamPlayer(
            teamId = teamId,
            tournamentId = tournamentId,
            playerName = trimmedName,
            jerseyNumber = jerseyNumber,
            role = role
        )
        val id = teamPlayerRepository.savePlayer(player)
        return Result.success(player.copy(id = id))
    }
}
