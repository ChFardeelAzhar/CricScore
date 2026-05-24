package com.cricscore.app.ui.tournament.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TournamentResultUiState(
    val isLoading: Boolean = true,
    val tournament: Tournament? = null,
    val champion: TournamentTeam? = null,
    val runnerUp: TournamentTeam? = null,
    val thirdPlace: TournamentTeam? = null,
    val orangeCap: TournamentPlayerStat? = null,
    val purpleCap: TournamentPlayerStat? = null,
    val topSixes: TournamentPlayerStat? = null,
    val topFours: TournamentPlayerStat? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TournamentResultViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TournamentResultUiState())
    val uiState: StateFlow<TournamentResultUiState> = _uiState.asStateFlow()

    fun loadTournamentResult(tournamentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tournament = tournamentRepository.getTournamentById(tournamentId)
                if (tournament == null) {
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = "Tournament not found") 
                    }
                    return@launch
                }

                val teams = tournamentRepository.getTeamsByTournamentSync(tournamentId)
                val champion = teams.getOrNull(0)
                val runnerUp = teams.getOrNull(1)
                val thirdPlace = teams.getOrNull(2)

                val topRunsList = tournamentRepository.getTopRunScorers(tournamentId, 1)
                val orangeCap = topRunsList.firstOrNull()

                val topWicketsList = tournamentRepository.getTopWicketTakers(tournamentId, 1)
                val purpleCap = topWicketsList.firstOrNull()

                val topSixesList = tournamentRepository.getTopSixHitters(tournamentId, 1)
                val topSixes = topSixesList.firstOrNull()

                val topFoursList = tournamentRepository.getTopFourHitters(tournamentId, 1)
                val topFours = topFoursList.firstOrNull()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tournament = tournament,
                        champion = champion,
                        runnerUp = runnerUp,
                        thirdPlace = thirdPlace,
                        orangeCap = orangeCap,
                        purpleCap = purpleCap,
                        topSixes = topSixes,
                        topFours = topFours,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Unknown error occurred") 
                }
            }
        }
    }
}
