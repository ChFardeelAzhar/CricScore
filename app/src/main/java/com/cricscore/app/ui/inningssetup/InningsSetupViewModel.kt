package com.cricscore.app.ui.inningssetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.TossResult
import com.cricscore.app.domain.model.PlayingElevenPlayer
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.repository.PlayingElevenRepository
import com.cricscore.app.domain.repository.TeamPlayerRepository
import com.cricscore.app.domain.usecase.StartInningsUseCase
import com.cricscore.app.domain.usecase.team.GetAvailableBatsmenUseCase
import com.cricscore.app.domain.usecase.team.GetAvailableBowlersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InningsSetupViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository,
    private val startInningsUseCase: StartInningsUseCase,
    private val tournamentRepository: TournamentRepository,
    private val getAvailableBatsmenUseCase: GetAvailableBatsmenUseCase,
    private val getAvailableBowlersUseCase: GetAvailableBowlersUseCase,
    private val playingElevenRepository: PlayingElevenRepository,
    private val teamPlayerRepository: TeamPlayerRepository
) : ViewModel() {

    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match.asStateFlow()

    private val _setupState = MutableStateFlow<InningsSetupState?>(null)
    val setupState: StateFlow<InningsSetupState?> = _setupState.asStateFlow()

    private val _isTournamentMatch = MutableStateFlow(false)
    val isTournamentMatch: StateFlow<Boolean> = _isTournamentMatch.asStateFlow()

    private val _availableBatsmen = MutableStateFlow<List<TeamPlayer>>(emptyList())
    val availableBatsmen: StateFlow<List<TeamPlayer>> = _availableBatsmen.asStateFlow()

    private val _availableBowlers = MutableStateFlow<List<TeamPlayer>>(emptyList())
    val availableBowlers: StateFlow<List<TeamPlayer>> = _availableBowlers.asStateFlow()

    private val _inningsStarted = MutableSharedFlow<Boolean>()
    val inningsStarted: SharedFlow<Boolean> = _inningsStarted.asSharedFlow()

    private val _validationError = MutableSharedFlow<String>()
    val validationError: SharedFlow<String> = _validationError.asSharedFlow()

    fun loadInningsSetup(matchId: Long, inningsNumber: Int) {
        viewModelScope.launch {
            val m = matchRepository.getMatchByIdSync(matchId) ?: return@launch
            _match.value = m

            val (batting, bowling) = if (inningsNumber == 1) {
                val winner = m.tossWinner
                val decision = m.tossDecision
                if (winner == m.team1) {
                    if (decision == TossResult.BAT) Pair(m.team1, m.team2) else Pair(m.team2, m.team1)
                } else {
                    if (decision == TossResult.BAT) Pair(m.team2, m.team1) else Pair(m.team1, m.team2)
                }
            } else {
                val firstInnings = inningsRepository.getInningsByNumberSync(matchId, 1)
                if (firstInnings != null) {
                    Pair(firstInnings.bowlingTeam, firstInnings.battingTeam)
                } else {
                    Pair(m.team2, m.team1) // Fallback
                }
            }

            _setupState.value = InningsSetupState(
                battingTeam = batting,
                bowlingTeam = bowling
            )

            val fixture = tournamentRepository.getFixtureByLinkedMatchId(matchId)
            if (fixture != null) {
                _isTournamentMatch.value = true
                val battingTeamId = if (batting.trim().equals(fixture.team1Name.trim(), ignoreCase = true)) fixture.team1Id else fixture.team2Id
                val bowlingTeamId = if (bowling.trim().equals(fixture.team1Name.trim(), ignoreCase = true)) fixture.team1Id else fixture.team2Id

                // Check and initialize playing eleven if empty
                val battingCount = playingElevenRepository.getSelectedCount(fixture.id, battingTeamId)
                val squadBatting = teamPlayerRepository.getPlayersByTeamSync(battingTeamId)
                if (battingCount == 0 && squadBatting.isNotEmpty()) {
                    val playingEleven = squadBatting.mapIndexed { index, player ->
                        PlayingElevenPlayer(
                            fixtureId = fixture.id,
                            teamId = battingTeamId,
                            playerId = player.id,
                            playerName = player.playerName,
                            battingOrder = index + 1,
                            isCaptain = player.isCaptain,
                            isWicketKeeper = player.isWicketKeeper
                        )
                    }
                    playingElevenRepository.savePlayingEleven(playingEleven)
                }

                val bowlingCount = playingElevenRepository.getSelectedCount(fixture.id, bowlingTeamId)
                val squadBowling = teamPlayerRepository.getPlayersByTeamSync(bowlingTeamId)
                if (bowlingCount == 0 && squadBowling.isNotEmpty()) {
                    val playingEleven = squadBowling.mapIndexed { index, player ->
                        PlayingElevenPlayer(
                            fixtureId = fixture.id,
                            teamId = bowlingTeamId,
                            playerId = player.id,
                            playerName = player.playerName,
                            battingOrder = index + 1,
                            isCaptain = player.isCaptain,
                            isWicketKeeper = player.isWicketKeeper
                        )
                    }
                    playingElevenRepository.savePlayingEleven(playingEleven)
                }

                val p11Batsmen = getAvailableBatsmenUseCase(fixture.id, battingTeamId)
                val p11Bowlers = getAvailableBowlersUseCase(fixture.id, bowlingTeamId)

                // Map to TeamPlayer objects for jersey and role details
                val freshSquadBatting = teamPlayerRepository.getPlayersByTeamSync(battingTeamId)
                val freshSquadBowling = teamPlayerRepository.getPlayersByTeamSync(bowlingTeamId)

                val batsmenTeamPlayers = p11Batsmen.mapNotNull { p11 ->
                    freshSquadBatting.find { it.id == p11.playerId } ?: TeamPlayer(
                        id = p11.playerId,
                        teamId = battingTeamId,
                        tournamentId = fixture.tournamentId,
                        playerName = p11.playerName,
                        isCaptain = p11.isCaptain,
                        isWicketKeeper = p11.isWicketKeeper
                    )
                }

                val bowlersTeamPlayers = p11Bowlers.mapNotNull { p11 ->
                    freshSquadBowling.find { it.id == p11.playerId } ?: TeamPlayer(
                        id = p11.playerId,
                        teamId = bowlingTeamId,
                        tournamentId = fixture.tournamentId,
                        playerName = p11.playerName,
                        isCaptain = p11.isCaptain,
                        isWicketKeeper = p11.isWicketKeeper
                    )
                }

                _availableBatsmen.value = batsmenTeamPlayers
                _availableBowlers.value = bowlersTeamPlayers
            } else {
                _isTournamentMatch.value = false
                _availableBatsmen.value = emptyList()
                _availableBowlers.value = emptyList()
            }
        }
    }

    fun startInnings(
        matchId: Long,
        inningsNumber: Int,
        striker: String,
        nonStriker: String,
        bowler: String
    ) {
        val state = _setupState.value ?: return
        val s = striker.trim()
        val ns = nonStriker.trim()
        val b = bowler.trim()

        if (s.isEmpty()) {
            sendError("Striker name cannot be empty")
            return
        }
        if (ns.isEmpty()) {
            sendError("Non-Striker name cannot be empty")
            return
        }
        if (b.isEmpty()) {
            sendError("Opening Bowler name cannot be empty")
            return
        }
        if (s.equals(ns, ignoreCase = true)) {
            sendError("Striker and Non-Striker must be different players")
            return
        }
        if (b.equals(s, ignoreCase = true) || b.equals(ns, ignoreCase = true)) {
            sendError("Bowler cannot be the striker or non-striker")
            return
        }

        viewModelScope.launch {
            try {
                startInningsUseCase(
                    matchId = matchId,
                    inningsNumber = inningsNumber,
                    battingTeam = state.battingTeam,
                    bowlingTeam = state.bowlingTeam,
                    strikerName = s,
                    nonStrikerName = ns,
                    bowlerName = b
                )
                _inningsStarted.emit(true)
            } catch (e: Exception) {
                sendError(e.message ?: "Failed to start innings")
            }
        }
    }

    private fun sendError(msg: String) {
        viewModelScope.launch {
            _validationError.emit(msg)
        }
    }

    data class InningsSetupState(
        val battingTeam: String,
        val bowlingTeam: String
    )
}
