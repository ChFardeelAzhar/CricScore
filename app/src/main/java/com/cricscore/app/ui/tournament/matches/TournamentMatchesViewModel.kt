package com.cricscore.app.ui.tournament.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.MatchStatus
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.usecase.tournament.GetTournamentMatchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TournamentMatchesViewModel @Inject constructor(
    private val getTournamentMatchesUseCase: GetTournamentMatchesUseCase,
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _tournamentId = MutableStateFlow<Long>(0)

    val tournament: StateFlow<Tournament?> = _tournamentId
        .flatMapLatest { id -> 
            if (id == 0L) flowOf<Tournament?>(null) else flow { emit(tournamentRepository.getTournamentById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val matches: StateFlow<List<Match>> = _tournamentId
        .flatMapLatest { id -> 
            if (id == 0L) flowOf(emptyList()) else getTournamentMatchesUseCase(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMatch: StateFlow<Match?> = matches.map { list ->
        list.firstOrNull { it.status != MatchStatus.COMPLETED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun initTournament(id: Long) {
        _tournamentId.value = id
    }
}
