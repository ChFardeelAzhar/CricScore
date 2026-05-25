package com.cricscore.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Fixture
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.repository.PlayingElevenRepository
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.usecase.tournament.CompleteFixtureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentIntegrationViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val playingElevenRepository: PlayingElevenRepository,
    private val completeFixtureUseCase: CompleteFixtureUseCase
) : ViewModel() {

    suspend fun getFixtureByMatchId(matchId: Long): Fixture? {
        return tournamentRepository.getFixtureByLinkedMatchId(matchId)
    }

    suspend fun getTournamentByFixtureId(fixtureId: Long): Tournament? {
        val fixture = tournamentRepository.getFixtureById(fixtureId) ?: return null
        return tournamentRepository.getTournamentById(fixture.tournamentId)
    }

    suspend fun getSelectedPlayingElevenCount(fixtureId: Long, teamId: Long): Int {
        return playingElevenRepository.getSelectedCount(fixtureId, teamId)
    }

    fun completeFixtureIfNeeded(matchId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val fixture = tournamentRepository.getFixtureByLinkedMatchId(matchId)
            if (fixture != null && fixture.status != "COMPLETED") {
                completeFixtureUseCase.execute(fixture.id, matchId)
            }
            onComplete()
        }
    }
}
