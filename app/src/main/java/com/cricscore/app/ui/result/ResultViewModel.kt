package com.cricscore.app.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.core.util.CricketCalculator
import com.cricscore.app.domain.model.Innings
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerPerformance(
    val name: String,
    val runs: Int,
    val balls: Int,
    val wickets: Int,
    val oversBowled: String,
    val runsConceded: Int,
    val performanceSummary: String,
    val description: String
)

@OptIn(ExperimentalCoroutinesApi::class)
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

    val playerPerformances: StateFlow<List<PlayerPerformance>> = _matchId
        .flatMapLatest { id ->
            if (id == 0L) flowOf(emptyList()) else {
                combine(
                    inningsRepository.getBatsmenForInnings(id, 1),
                    inningsRepository.getBowlersForInnings(id, 1),
                    inningsRepository.getBatsmenForInnings(id, 2),
                    inningsRepository.getBowlersForInnings(id, 2)
                ) { b1, bo1, b2, bo2 ->
                    val map = mutableMapOf<String, PlayerPerformanceBuilder>()
                    fun getOrCreate(name: String) = map.getOrPut(name) { PlayerPerformanceBuilder(name) }
                    
                    b1.forEach { getOrCreate(it.playerName).runs += it.runs; getOrCreate(it.playerName).balls += it.balls }
                    b2.forEach { getOrCreate(it.playerName).runs += it.runs; getOrCreate(it.playerName).balls += it.balls }
                    bo1.forEach { getOrCreate(it.playerName).wickets += it.wickets; getOrCreate(it.playerName).runsConceded += it.runsConceded; getOrCreate(it.playerName).ballsBowled += it.ballsBowled; getOrCreate(it.playerName).maidens += it.maidens }
                    bo2.forEach { getOrCreate(it.playerName).wickets += it.wickets; getOrCreate(it.playerName).runsConceded += it.runsConceded; getOrCreate(it.playerName).ballsBowled += it.ballsBowled; getOrCreate(it.playerName).maidens += it.maidens }
                    
                    map.values.map { it.build() }.sortedWith(
                        compareByDescending<PlayerPerformance> { it.wickets }
                            .thenByDescending { it.runs }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadMatch(matchId: Long) {
        _matchId.value = matchId
    }

    fun updatePlayerOfMatch(playerName: String) {
        viewModelScope.launch {
            val currentMatch = match.value ?: return@launch
            val updatedMatch = currentMatch.copy(playerOfMatch = playerName)
            matchRepository.updateMatch(updatedMatch)
        }
    }

    fun selectDefaultPlayerOfMatchIfNeeded(players: List<PlayerPerformance>) {
        val currentMatch = match.value ?: return
        if (players.isNotEmpty() && currentMatch.playerOfMatch.isNullOrBlank()) {
            updatePlayerOfMatch(players.first().name)
        }
    }
}

private class PlayerPerformanceBuilder(val name: String) {
    var runs: Int = 0
    var balls: Int = 0
    var wickets: Int = 0
    var maidens: Int = 0
    var runsConceded: Int = 0
    var ballsBowled: Int = 0
    
    fun build(): PlayerPerformance {
        val oversStr = CricketCalculator.ballsToOversString(ballsBowled)
        val performanceSummary = buildString {
            if (runs > 0 || balls > 0) {
                append("$runs ($balls)")
            }
            if (ballsBowled > 0) {
                if (isNotEmpty()) append("  ·  ")
                append("$oversStr-$maidens-$runsConceded-$wickets")
            }
        }
        
        val desc = buildString {
            if (runs > 15 && wickets > 0) {
                append("Bat + Ball all-round performance")
            } else if (runs > 25) {
                append("Excellent batting display")
            } else if (wickets >= 2) {
                append("Brilliant bowling spell")
            } else if (runs > 0 && ballsBowled > 0) {
                append("Good all-round contribution")
            } else if (runs > 0) {
                append("Batting contribution")
            } else if (ballsBowled > 0) {
                append("Bowling contribution")
            } else {
                append("Match participant")
            }
        }

        return PlayerPerformance(
            name = name,
            runs = runs,
            balls = balls,
            wickets = wickets,
            oversBowled = oversStr,
            runsConceded = runsConceded,
            performanceSummary = performanceSummary,
            description = desc
        )
    }
}
