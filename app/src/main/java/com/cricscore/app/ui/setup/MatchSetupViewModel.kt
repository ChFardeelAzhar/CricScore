package com.cricscore.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.usecase.CreateMatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchSetupViewModel @Inject constructor(
    private val createMatchUseCase: CreateMatchUseCase
) : ViewModel() {

    private val _setupEvent = MutableSharedFlow<SetupEvent>()
    val setupEvent: SharedFlow<SetupEvent> = _setupEvent.asSharedFlow()

    fun createMatch(team1: String, team2: String, overs: String, players: String) {
        val t1 = team1.trim()
        val t2 = team2.trim()
        
        if (t1.isEmpty()) {
            sendEvent(SetupEvent.ValidationError("Team 1 name cannot be empty"))
            return
        }
        if (t2.isEmpty()) {
            sendEvent(SetupEvent.ValidationError("Team 2 name cannot be empty"))
            return
        }
        if (t1.equals(t2, ignoreCase = true)) {
            sendEvent(SetupEvent.ValidationError("Teams must have different names"))
            return
        }

        val oversInt = overs.toIntOrNull()
        if (oversInt == null || oversInt <= 0 || oversInt > 90) {
            sendEvent(SetupEvent.ValidationError("Overs must be between 1 and 90"))
            return
        }

        val playersInt = players.toIntOrNull()
        if (playersInt == null || playersInt < 2 || playersInt > 16) {
            sendEvent(SetupEvent.ValidationError("Players per side must be between 2 and 16"))
            return
        }

        viewModelScope.launch {
            try {
                val matchId = createMatchUseCase(t1, t2, oversInt, playersInt)
                _setupEvent.emit(SetupEvent.Success(matchId))
            } catch (e: Exception) {
                _setupEvent.emit(SetupEvent.Error(e.message ?: "Failed to create match"))
            }
        }
    }

    private fun sendEvent(event: SetupEvent) {
        viewModelScope.launch {
            _setupEvent.emit(event)
        }
    }

    sealed interface SetupEvent {
        data class ValidationError(val message: String) : SetupEvent
        data class Success(val matchId: Long) : SetupEvent
        data class Error(val message: String) : SetupEvent
    }
}
