package com.cricscore.app.ui.tournament.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.MatchRepository
import com.cricscore.app.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TournamentOverviewViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _tournamentId = MutableStateFlow<Long>(0)
    val tournamentId: StateFlow<Long> = _tournamentId.asStateFlow()

    val tournament: StateFlow<Tournament?> = _tournamentId
        .flatMapLatest { id -> 
            if (id == 0L) flowOf<Tournament?>(null) else flow { 
                emit(tournamentRepository.getTournamentById(id)) 
            } 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fixtures: StateFlow<List<Fixture>> = _tournamentId
        .flatMapLatest { id -> if (id == 0L) flowOf(emptyList()) else tournamentRepository.getFixturesByTournament(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pointsTable: StateFlow<List<TournamentTeam>> = _tournamentId
        .flatMapLatest { id -> if (id == 0L) flowOf(emptyList()) else tournamentRepository.getTeamsByTournament(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerStats: StateFlow<List<TournamentPlayerStat>> = _tournamentId
        .flatMapLatest { id -> if (id == 0L) flowOf(emptyList()) else tournamentRepository.getStatsByTournament(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun initTournament(id: Long) {
        _tournamentId.value = id
    }

    fun startFixtureMatch(fixture: Fixture, onMatchCreated: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val hasLive = fixtures.value.any { it.status == "LIVE" }
            if (hasLive) {
                onError("Another match is already live. Complete it first.")
                return@launch
            }

            val t = tournament.value ?: return@launch

            val match = Match(
                team1 = fixture.team1Name,
                team2 = fixture.team2Name,
                oversLimit = t.oversPerMatch,
                playersPerSide = t.playersPerSide,
                status = MatchStatus.UPCOMING,
                tournamentId = t.id
            )

            val mId = matchRepository.createMatch(match)

            // Update fixture
            tournamentRepository.updateFixture(
                fixture.copy(
                    status = "LIVE",
                    linkedMatchId = mId
                )
            )

            // Update tournament started if UPCOMING
            if (t.status == TournamentStatus.UPCOMING) {
                tournamentRepository.updateTournament(
                    t.copy(
                        status = TournamentStatus.ONGOING,
                        startedAt = System.currentTimeMillis()
                    )
                )
            }

            onMatchCreated(mId)
        }
    }
}
