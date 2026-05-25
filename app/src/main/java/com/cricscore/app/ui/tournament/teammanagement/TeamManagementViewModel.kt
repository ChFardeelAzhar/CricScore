package com.cricscore.app.ui.tournament.teammanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.model.TournamentTeam
import com.cricscore.app.domain.repository.TeamPlayerRepository
import com.cricscore.app.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TeamDetail(
    val team: TournamentTeam,
    val playerCount: Int,
    val captainName: String?
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamManagementViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val teamPlayerRepository: TeamPlayerRepository
) : ViewModel() {

    private val _tournamentId = MutableStateFlow<Long>(0)
    val tournamentId: StateFlow<Long> = _tournamentId.asStateFlow()

    val tournament: StateFlow<Tournament?> = _tournamentId
        .flatMapLatest { id ->
            if (id == 0L) flowOf<com.cricscore.app.domain.model.Tournament?>(null) else flow { emit(tournamentRepository.getTournamentById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val teams: StateFlow<List<TournamentTeam>> = _tournamentId
        .flatMapLatest { id ->
            if (id == 0L) flowOf(emptyList()) else tournamentRepository.getTeamsByTournament(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamDetails: StateFlow<List<TeamDetail>> = teams.flatMapLatest { teamList ->
        if (teamList.isEmpty()) {
            flowOf(emptyList())
        } else {
            val flows = teamList.map { team ->
                teamPlayerRepository.getPlayersByTeam(team.id).map { players ->
                    TeamDetail(
                        team = team,
                        playerCount = players.size,
                        captainName = players.firstOrNull { it.isCaptain }?.playerName
                    )
                }
            }
            combine(flows) { it.toList() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun initTournament(id: Long) {
        _tournamentId.value = id
    }
}
