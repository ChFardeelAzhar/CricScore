package com.cricscore.app.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Innings
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository
) : ViewModel() {

    private val _matchId = MutableStateFlow<Long>(0)

    val match: StateFlow<Match?> = _matchId
        .flatMapLatest { id -> if (id == 0L) flowOf(null) else matchRepository.getMatchById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inningsList: StateFlow<List<Innings>> = _matchId
        .flatMapLatest { id -> if (id == 0L) flowOf(emptyList()) else inningsRepository.getInningsForMatch(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadMatch(matchId: Long) {
        _matchId.value = matchId
    }
}
