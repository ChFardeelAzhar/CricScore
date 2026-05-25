package com.cricscore.app.domain.usecase.team

import com.cricscore.app.domain.model.PlayingElevenPlayer
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.domain.repository.PlayingElevenRepository
import com.cricscore.app.domain.repository.TournamentRepository
import javax.inject.Inject

class SavePlayingElevenUseCase @Inject constructor(
    private val playingElevenRepository: PlayingElevenRepository,
    private val tournamentRepository: TournamentRepository
) {
    suspend operator fun invoke(
        fixtureId: Long,
        teamId: Long,
        selectedPlayers: List<TeamPlayer>
    ): Result<Unit> {
        val fixture = tournamentRepository.getFixtureById(fixtureId)
            ?: return Result.failure(Exception("Fixture not found"))
        val tournament = tournamentRepository.getTournamentById(fixture.tournamentId)
            ?: return Result.failure(Exception("Tournament not found"))
            
        if (selectedPlayers.size != tournament.playersPerSide) {
            return Result.failure(Exception("Must select exactly ${tournament.playersPerSide} players"))
        }

        // Clear existing XI
        playingElevenRepository.clearPlayingEleven(fixtureId, teamId)

        // Insert new XI
        val entities = selectedPlayers.mapIndexed { index, player ->
            PlayingElevenPlayer(
                fixtureId = fixtureId,
                teamId = teamId,
                playerId = player.id,
                playerName = player.playerName,
                battingOrder = index + 1,
                isCaptain = player.isCaptain,
                isWicketKeeper = player.isWicketKeeper
            )
        }
        
        playingElevenRepository.savePlayingEleven(entities)
        return Result.success(Unit)
    }
}
