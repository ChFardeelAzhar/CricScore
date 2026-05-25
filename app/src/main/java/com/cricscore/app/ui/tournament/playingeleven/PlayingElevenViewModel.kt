package com.cricscore.app.ui.tournament.playingeleven

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.domain.repository.TeamPlayerRepository
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.usecase.team.SavePlayingElevenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PlayingElevenEvent {
    object SavedSuccessfully : PlayingElevenEvent()
    data class Error(val message: String) : PlayingElevenEvent()
}

data class PlayingElevenUiState(
    val teamName: String = "",
    val allSquadPlayers: List<TeamPlayer> = emptyList(),
    val selectedPlayers: List<TeamPlayer> = emptyList(),
    val requiredCount: Int = 11,
    val captainIncluded: Boolean = false,
    val wicketKeeperIncluded: Boolean = false,
    val canConfirm: Boolean = false,
    val isLoading: Boolean = false,
    val event: PlayingElevenEvent? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayingElevenViewModel @Inject constructor(
    private val teamPlayerRepository: TeamPlayerRepository,
    private val tournamentRepository: TournamentRepository,
    private val savePlayingElevenUseCase: SavePlayingElevenUseCase
) : ViewModel() {

    private val _fixtureId = MutableStateFlow<Long>(0L)
    private val _teamId = MutableStateFlow<Long>(0L)
    private val _requiredCount = MutableStateFlow<Int>(11)
    private val _selectedPlayers = MutableStateFlow<List<TeamPlayer>>(emptyList())
    private val _isLoading = MutableStateFlow<Boolean>(false)
    private val _event = MutableStateFlow<PlayingElevenEvent?>(null)

    val fixtureId: StateFlow<Long> = _fixtureId.asStateFlow()
    val teamId: StateFlow<Long> = _teamId.asStateFlow()
    val requiredCount: StateFlow<Int> = _requiredCount.asStateFlow()
    val selectedPlayers: StateFlow<List<TeamPlayer>> = _selectedPlayers.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val event: StateFlow<PlayingElevenEvent?> = _event.asStateFlow()

    private val team = _teamId.flatMapLatest { id ->
        if (id == 0L) flowOf<com.cricscore.app.domain.model.TournamentTeam?>(null) else flow { emit(tournamentRepository.getTeamById(id)) }
    }

    val allSquadPlayers: StateFlow<List<TeamPlayer>> = _teamId
        .flatMapLatest { id ->
            if (id == 0L) flowOf(emptyList()) else teamPlayerRepository.getPlayersByTeam(id).map { list ->
                list.sortedWith(
                    compareByDescending<TeamPlayer> { it.isCaptain }
                        .thenByDescending { it.jerseyNumber > 0 }
                        .thenBy { it.jerseyNumber }
                        .thenBy { it.playerName.lowercase() }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<PlayingElevenUiState> = combine(
        combine(team, allSquadPlayers) { t, s -> Pair(t, s) },
        _selectedPlayers,
        _requiredCount,
        _isLoading,
        _event
    ) { teamAndSquad, selected, required, loading, ev ->
        val teamObj = teamAndSquad.first
        val squad = teamAndSquad.second
        PlayingElevenUiState(
            teamName = teamObj?.teamName ?: "",
            allSquadPlayers = squad,
            selectedPlayers = selected,
            requiredCount = required,
            captainIncluded = selected.any { it.isCaptain },
            wicketKeeperIncluded = selected.any { it.isWicketKeeper },
            canConfirm = selected.size == required,
            isLoading = loading,
            event = ev
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayingElevenUiState())

    fun initSetup(fixId: Long, tId: Long, count: Int) {
        _fixtureId.value = fixId
        _teamId.value = tId
        _requiredCount.value = count
        _selectedPlayers.value = emptyList()
    }

    fun togglePlayerSelection(player: TeamPlayer) {
        val current = _selectedPlayers.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == player.id }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            if (current.size < _requiredCount.value) {
                current.add(player)
            }
        }
        _selectedPlayers.value = current
    }

    fun selectAll() {
        val squad = allSquadPlayers.value
        val req = _requiredCount.value
        _selectedPlayers.value = squad.take(req)
    }

    fun clearAll() {
        _selectedPlayers.value = emptyList()
    }

    fun confirmSelection() {
        val list = _selectedPlayers.value
        val fixId = _fixtureId.value
        val tId = _teamId.value
        
        if (list.size != _requiredCount.value) {
            _event.value = PlayingElevenEvent.Error("Select exactly ${_requiredCount.value} players")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = savePlayingElevenUseCase(
                fixtureId = fixId,
                teamId = tId,
                selectedPlayers = list
            )
            _isLoading.value = false
            result.onSuccess {
                _event.value = PlayingElevenEvent.SavedSuccessfully
            }.onFailure { err ->
                _event.value = PlayingElevenEvent.Error(err.message ?: "Failed to save selection")
            }
        }
    }

    fun clearEvent() {
        _event.value = null
    }
}
