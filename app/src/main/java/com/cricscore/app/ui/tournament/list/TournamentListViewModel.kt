package com.cricscore.app.ui.tournament.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentListViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    val tournaments: StateFlow<List<Tournament>> = tournamentRepository.getAllTournaments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteTournament(tournament: Tournament) {
        viewModelScope.launch {
            tournamentRepository.deleteTournament(tournament)
        }
    }
}
