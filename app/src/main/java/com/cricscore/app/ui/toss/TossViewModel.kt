package com.cricscore.app.ui.toss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.TossResult
import com.cricscore.app.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TossViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match.asStateFlow()

    private val _tossSaved = MutableSharedFlow<Boolean>()
    val tossSaved: SharedFlow<Boolean> = _tossSaved.asSharedFlow()

    fun loadMatch(matchId: Long) {
        viewModelScope.launch {
            matchRepository.getMatchById(matchId).collect {
                _match.value = it
            }
        }
    }

    fun saveTossResult(tossWinner: String, tossDecision: TossResult) {
        val currentMatch = _match.value ?: return
        viewModelScope.launch {
            val updatedMatch = currentMatch.copy(
                tossWinner = tossWinner,
                tossDecision = tossDecision
            )
            matchRepository.updateMatch(updatedMatch)
            _tossSaved.emit(true)
        }
    }
}
