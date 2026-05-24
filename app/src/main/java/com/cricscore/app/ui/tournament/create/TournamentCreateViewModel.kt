package com.cricscore.app.ui.tournament.create

import androidx.lifecycle.ViewModel
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TournamentCreateViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _venue = MutableStateFlow("")
    val venue: StateFlow<String> = _venue.asStateFlow()

    private val _totalTeams = MutableStateFlow(4)
    val totalTeams: StateFlow<Int> = _totalTeams.asStateFlow()

    private val _oversPerMatch = MutableStateFlow(5)
    val oversPerMatch: StateFlow<Int> = _oversPerMatch.asStateFlow()

    private val _playersPerSide = MutableStateFlow(11)
    val playersPerSide: StateFlow<Int> = _playersPerSide.asStateFlow()

    fun setName(value: String) {
        _name.value = value
    }

    fun setVenue(value: String) {
        _venue.value = value
    }

    fun setTotalTeams(value: Int) {
        _totalTeams.value = value
    }

    fun setOversPerMatch(value: Int) {
        _oversPerMatch.value = value
    }

    fun setPlayersPerSide(value: Int) {
        _playersPerSide.value = value
    }

    suspend fun saveTournament(): Long {
        val tournament = Tournament(
            name = _name.value.trim(),
            venue = _venue.value.trim(),
            totalTeams = _totalTeams.value,
            oversPerMatch = _oversPerMatch.value,
            playersPerSide = _playersPerSide.value
        )
        return tournamentRepository.createTournament(tournament)
    }
}
