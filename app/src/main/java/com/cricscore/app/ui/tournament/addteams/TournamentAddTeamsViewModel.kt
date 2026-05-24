package com.cricscore.app.ui.tournament.addteams

import androidx.lifecycle.ViewModel
import com.cricscore.app.domain.model.TournamentTeam
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.usecase.tournament.GenerateFixturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TournamentAddTeamsViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val generateFixturesUseCase: GenerateFixturesUseCase
) : ViewModel() {

    private val _teams = MutableStateFlow<List<TournamentTeam>>(emptyList())
    val teams: StateFlow<List<TournamentTeam>> = _teams.asStateFlow()

    fun addTeam(tournamentId: Long, name: String, colorHex: String): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || trimmedName.length > 20) return false
        
        // Case-insensitive duplicate check
        if (_teams.value.any { it.teamName.equals(trimmedName, ignoreCase = true) }) return false

        val newTeam = TournamentTeam(
            tournamentId = tournamentId,
            teamName = trimmedName,
            colorHex = colorHex,
            logoEmoji = "🏏"
        )
        _teams.value = _teams.value + newTeam
        return true
    }

    fun removeTeam(teamName: String) {
        _teams.value = _teams.value.filterNot { it.teamName == teamName }
    }

    suspend fun generateFixtures(tournamentId: Long) {
        val currentTeams = _teams.value
        // Save teams to repository
        tournamentRepository.insertTeams(currentTeams)
        
        // Load them back with their generated IDs
        val savedTeams = tournamentRepository.getTeamsByTournamentSync(tournamentId)
        
        // Generate fixtures
        generateFixturesUseCase(tournamentId, savedTeams)
    }
}
