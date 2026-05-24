package com.cricscore.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Fixture
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.usecase.tournament.CompleteFixtureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentIntegrationViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val completeFixtureUseCase: CompleteFixtureUseCase
) : ViewModel() {

    suspend fun getFixtureByMatchId(matchId: Long): Fixture? {
        return tournamentRepository.getFixtureByLinkedMatchId(matchId)
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
