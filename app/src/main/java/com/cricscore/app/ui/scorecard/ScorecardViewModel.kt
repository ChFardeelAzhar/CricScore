package com.cricscore.app.ui.scorecard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScorecardViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository
) : ViewModel() {

    private val _matchId = MutableStateFlow<Long>(0)
    private val _inningsNumber = MutableStateFlow<Int>(1)

    val matchId: StateFlow<Long> = _matchId.asStateFlow()
    val inningsNumber: StateFlow<Int> = _inningsNumber.asStateFlow()

    val match: StateFlow<Match?> = _matchId
        .flatMapLatest { id -> if (id == 0L) flowOf(null) else matchRepository.getMatchById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val innings: StateFlow<Innings?> = combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }
        .flatMapLatest { (id, num) ->
            if (id == 0L) flowOf(null) else inningsRepository.getInningsForMatch(id).map { list ->
                list.firstOrNull { it.inningsNumber == num }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val batsmen: StateFlow<List<BatsmanInnings>> = combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }
        .flatMapLatest { (id, num) ->
            if (id == 0L) flowOf(emptyList()) else inningsRepository.getBatsmenForInnings(id, num)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bowlers: StateFlow<List<BowlerInnings>> = combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }
        .flatMapLatest { (id, num) ->
            if (id == 0L) flowOf(emptyList()) else inningsRepository.getBowlersForInnings(id, num)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun initMatch(matchId: Long, initialInnings: Int) {
        _matchId.value = matchId
        _inningsNumber.value = initialInnings
    }

    fun setInningsNumber(num: Int) {
        _inningsNumber.value = num
    }
}
