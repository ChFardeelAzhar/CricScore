package com.cricscore.app.ui.tournament.squadmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.domain.repository.TeamPlayerRepository
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.usecase.team.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPlayerState(
    val name: String = "",
    val jerseyNumber: String = "",
    val selectedRole: String = "ALL_ROUNDER",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SquadEvent {
    data class PlayerAdded(val name: String) : SquadEvent()
    data class Error(val message: String) : SquadEvent()
    object CaptainSet : SquadEvent()
    object ViceCaptainSet : SquadEvent()
    object WicketKeeperSet : SquadEvent()
    object PlayerRemoved : SquadEvent()
}

data class SquadUiState(
    val teamName: String = "",
    val teamColor: String = "#4A90D9",
    val players: List<TeamPlayer> = emptyList(),
    val captain: TeamPlayer? = null,
    val viceCaptain: TeamPlayer? = null,
    val wicketKeeper: TeamPlayer? = null,
    val playerCount: Int = 0,
    val requiredCount: Int = 11,
    val isSquadComplete: Boolean = false,
    val addPlayerState: AddPlayerState = AddPlayerState(),
    val event: SquadEvent? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SquadManagementViewModel @Inject constructor(
    private val teamPlayerRepository: TeamPlayerRepository,
    private val tournamentRepository: TournamentRepository,
    private val addPlayerToTeamUseCase: AddPlayerToTeamUseCase,
    private val removePlayerFromTeamUseCase: RemovePlayerFromTeamUseCase,
    private val setCaptainUseCase: SetCaptainUseCase,
    private val setViceCaptainUseCase: SetViceCaptainUseCase,
    private val setWicketKeeperUseCase: SetWicketKeeperUseCase
) : ViewModel() {

    private val _teamId = MutableStateFlow<Long>(0L)
    private val _tournamentId = MutableStateFlow<Long>(0L)
    private val _addPlayerState = MutableStateFlow(AddPlayerState())
    private val _event = MutableStateFlow<SquadEvent?>(null)

    val teamId: StateFlow<Long> = _teamId.asStateFlow()
    val tournamentId: StateFlow<Long> = _tournamentId.asStateFlow()
    val addPlayerState: StateFlow<AddPlayerState> = _addPlayerState.asStateFlow()
    val event: StateFlow<SquadEvent?> = _event.asStateFlow()

    private val team = _teamId.flatMapLatest { id ->
        if (id == 0L) flowOf<com.cricscore.app.domain.model.TournamentTeam?>(null) else flow { emit(tournamentRepository.getTeamById(id)) }
    }

    private val tournament = _tournamentId.flatMapLatest { id ->
        if (id == 0L) flowOf<com.cricscore.app.domain.model.Tournament?>(null) else flow { emit(tournamentRepository.getTournamentById(id)) }
    }

    val players: StateFlow<List<TeamPlayer>> = _teamId
        .flatMapLatest { id ->
            if (id == 0L) flowOf(emptyList()) else teamPlayerRepository.getPlayersByTeam(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<SquadUiState> = combine(
        team,
        tournament,
        players,
        _addPlayerState,
        _event
    ) { teamObj, tournamentObj, playerList, addState, ev ->
        val required = tournamentObj?.playersPerSide ?: 11
        val cap = playerList.firstOrNull { it.isCaptain }
        val vc = playerList.firstOrNull { it.isViceCaptain }
        val wk = playerList.firstOrNull { it.isWicketKeeper }
        SquadUiState(
            teamName = teamObj?.teamName ?: "",
            teamColor = teamObj?.colorHex ?: "#4A90D9",
            players = playerList,
            captain = cap,
            viceCaptain = vc,
            wicketKeeper = wk,
            playerCount = playerList.size,
            requiredCount = required,
            isSquadComplete = playerList.size >= required,
            addPlayerState = addState,
            event = ev
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SquadUiState())

    fun initTeam(tId: Long, tourId: Long) {
        _teamId.value = tId
        _tournamentId.value = tourId
    }

    fun updateAddPlayerName(name: String) {
        _addPlayerState.value = _addPlayerState.value.copy(name = name, error = null)
    }

    fun updateAddPlayerJersey(jersey: String) {
        _addPlayerState.value = _addPlayerState.value.copy(jerseyNumber = jersey, error = null)
    }

    fun updateAddPlayerRole(role: String) {
        _addPlayerState.value = _addPlayerState.value.copy(selectedRole = role, error = null)
    }

    fun addPlayer() {
        val state = _addPlayerState.value
        val name = state.name.trim()
        val jersey = state.jerseyNumber.trim().toIntOrNull() ?: 0
        val role = state.selectedRole
        val teamVal = _teamId.value
        val tourVal = _tournamentId.value

        if (name.isEmpty()) return

        viewModelScope.launch {
            _addPlayerState.value = _addPlayerState.value.copy(isLoading = true)
            val result = addPlayerToTeamUseCase(
                teamId = teamVal,
                tournamentId = tourVal,
                name = name,
                jerseyNumber = jersey,
                role = role
            )
            result.onSuccess {
                _addPlayerState.value = AddPlayerState() // Reset
                _event.value = SquadEvent.PlayerAdded(name)
            }.onFailure { error ->
                _addPlayerState.value = _addPlayerState.value.copy(isLoading = false, error = error.message)
                _event.value = SquadEvent.Error(error.message ?: "Failed to add player")
            }
        }
    }

    fun bulkAddPlayers(namesText: String, defaultRole: String) {
        val lines = namesText.split("\n")
        val names = lines.map { it.trim() }.filter { it.isNotEmpty() }
        if (names.isEmpty()) return

        val teamVal = _teamId.value
        val tourVal = _tournamentId.value

        viewModelScope.launch {
            var addedCount = 0
            var duplicateCount = 0
            var errorMsg: String? = null

            names.forEach { name ->
                val result = addPlayerToTeamUseCase(
                    teamId = teamVal,
                    tournamentId = tourVal,
                    name = name,
                    jerseyNumber = 0,
                    role = defaultRole
                )
                result.onSuccess {
                    addedCount++
                }.onFailure { err ->
                    if (err.message?.contains("already in squad", ignoreCase = true) == true) {
                        duplicateCount++
                    } else {
                        errorMsg = err.message
                    }
                }
            }

            if (addedCount > 0) {
                _event.value = SquadEvent.PlayerAdded("Added $addedCount players" + (if (duplicateCount > 0) " (skipped $duplicateCount duplicates)" else ""))
            } else if (duplicateCount > 0) {
                _event.value = SquadEvent.Error("Skipped $duplicateCount duplicate players already in squad")
            } else if (errorMsg != null) {
                _event.value = SquadEvent.Error(errorMsg ?: "Failed to add players")
            }
        }
    }

    fun removePlayer(playerId: Long) {
        viewModelScope.launch {
            val result = removePlayerFromTeamUseCase(playerId)
            result.onSuccess {
                _event.value = SquadEvent.PlayerRemoved
            }.onFailure { error ->
                _event.value = SquadEvent.Error(error.message ?: "Failed to remove player")
            }
        }
    }

    fun setCaptain(playerId: Long) {
        viewModelScope.launch {
            setCaptainUseCase(teamId = _teamId.value, playerId = playerId)
            _event.value = SquadEvent.CaptainSet
        }
    }

    fun setViceCaptain(playerId: Long) {
        viewModelScope.launch {
            setViceCaptainUseCase(teamId = _teamId.value, playerId = playerId)
            _event.value = SquadEvent.ViceCaptainSet
        }
    }

    fun setWicketKeeper(playerId: Long) {
        viewModelScope.launch {
            setWicketKeeperUseCase(teamId = _teamId.value, playerId = playerId)
            _event.value = SquadEvent.WicketKeeperSet
        }
    }

    fun clearEvent() {
        _event.value = null
    }
}
