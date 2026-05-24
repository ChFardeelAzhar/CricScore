package com.cricscore.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.MatchStatus
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.usecase.GetRecentMatchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getRecentMatchesUseCase: GetRecentMatchesUseCase,
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    val recentMatches: StateFlow<List<Match>> = getRecentMatchesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeMatch: StateFlow<Match?> = recentMatches.map { list ->
        list.firstOrNull { it.status != MatchStatus.COMPLETED }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val ongoingTournament: StateFlow<Tournament?> = tournamentRepository.getOngoingTournaments()
        .map { list -> list.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

