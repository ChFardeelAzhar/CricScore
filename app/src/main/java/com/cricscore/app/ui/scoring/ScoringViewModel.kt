package com.cricscore.app.ui.scoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricscore.app.core.util.CricketCalculator
import com.cricscore.app.core.util.OversHelper
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import com.cricscore.app.domain.repository.TournamentRepository
import com.cricscore.app.domain.repository.PlayingElevenRepository
import com.cricscore.app.domain.repository.TeamPlayerRepository
import com.cricscore.app.domain.usecase.RecordBallUseCase
import com.cricscore.app.domain.usecase.UndoLastBallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository,
    private val recordBallUseCase: RecordBallUseCase,
    private val undoLastBallUseCase: UndoLastBallUseCase,
    private val tournamentRepository: TournamentRepository,
    private val playingElevenRepository: PlayingElevenRepository,
    private val teamPlayerRepository: TeamPlayerRepository
) : ViewModel() {

    private val _matchId = MutableStateFlow<Long>(0)
    private val _inningsNumber = MutableStateFlow<Int>(1)

    val matchId: StateFlow<Long> = _matchId.asStateFlow()
    val inningsNumber: StateFlow<Int> = _inningsNumber.asStateFlow()

    // Primary data flows from DB
    val match: StateFlow<Match?> = _matchId
        .flatMapLatest { id -> if (id == 0L) flowOf(null) else matchRepository.getMatchById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val innings: StateFlow<Innings?> = combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }
        .flatMapLatest { (id, num) -> 
            if (id == 0L) flowOf(null) else inningsRepository.getInningsForMatch(id).map { list ->
                list.firstOrNull { it.inningsNumber == num }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val batsmen: StateFlow<List<BatsmanInnings>> = combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }
        .flatMapLatest { (id, num) ->
            if (id == 0L) flowOf(emptyList()) else inningsRepository.getBatsmenForInnings(id, num)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bowlers: StateFlow<List<BowlerInnings>> = combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }
        .flatMapLatest { (id, num) ->
            if (id == 0L) flowOf(emptyList()) else inningsRepository.getBowlersForInnings(id, num)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balls: StateFlow<List<Ball>> = combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }
        .flatMapLatest { (id, num) ->
            if (id == 0L) flowOf(emptyList()) else inningsRepository.getBallsForInnings(id, num)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived states
    val activeBatsmen: StateFlow<List<BatsmanInnings>> = batsmen.map { list ->
        list.filter { !it.isOut }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStriker: StateFlow<BatsmanInnings?> = combine(balls, activeBatsmen) { ballList, active ->
        if (active.isEmpty()) return@combine null
        if (ballList.isEmpty()) {
            active.firstOrNull()
        } else {
            val lastBall = ballList.last()
            val sName = lastBall.strikerName
            active.firstOrNull { it.playerName == sName } ?: active.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentNonStriker: StateFlow<BatsmanInnings?> = combine(balls, activeBatsmen, currentStriker) { ballList, active, striker ->
        if (active.size < 2) return@combine null
        val strikerName = striker?.playerName
        active.firstOrNull { it.playerName != strikerName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Keep track of the bowler explicitly selected for the upcoming over before any ball is recorded.
    private val _selectedBowlerForNewOver = MutableStateFlow<BowlerInnings?>(null)

    val currentBowler: StateFlow<BowlerInnings?> = combine(
        balls,
        bowlers,
        _selectedBowlerForNewOver
    ) { ballList, bowlerList, selectedBowler ->
        if (bowlerList.isEmpty()) return@combine null
        if (ballList.isEmpty()) {
            bowlerList.firstOrNull()
        } else {
            val lastBall = ballList.last()
            val legalInLastOver = ballList.filter { it.overNumber == lastBall.overNumber && OversHelper.isLegalBall(it.ballType) }.size
            if (legalInLastOver == 6) {
                selectedBowler
            } else {
                bowlerList.firstOrNull { it.playerName == lastBall.bowlerName }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val thisOverBalls: StateFlow<List<Ball>> = combine(
        balls,
        currentBowler
    ) { ballList, activeBowler ->
        if (ballList.isEmpty()) return@combine emptyList()
        val lastBall = ballList.last()
        val legalInLastOver = ballList.filter { it.overNumber == lastBall.overNumber && OversHelper.isLegalBall(it.ballType) }.size
        
        if (legalInLastOver == 6) {
            if (activeBowler != null) {
                emptyList()
            } else {
                ballList.filter { it.overNumber == lastBall.overNumber }
            }
        } else {
            ballList.filter { it.overNumber == lastBall.overNumber }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scoringButtonsEnabled: StateFlow<Boolean> = combine(
        balls,
        currentBowler,
        innings,
        match
    ) { ballList, activeBowler, inn, m ->
        if (inn == null || m == null || inn.isCompleted || m.status == MatchStatus.COMPLETED) {
            return@combine false
        }
        if (ballList.isEmpty()) {
            activeBowler != null
        } else {
            val lastBall = ballList.last()
            val legalInLastOver = ballList.filter { it.overNumber == lastBall.overNumber && OversHelper.isLegalBall(it.ballType) }.size
            if (legalInLastOver == 6) {
                activeBowler != null
            } else {
                true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)



    // Target information for 2nd Innings
    private val _firstInningsRuns = MutableStateFlow<Int>(0)
    val firstInningsRuns: StateFlow<Int> = _firstInningsRuns.asStateFlow()

    private val _isTournamentMatch = MutableStateFlow(false)
    val isTournamentMatch: StateFlow<Boolean> = _isTournamentMatch.asStateFlow()

    private val _availableBatsmen = MutableStateFlow<List<TeamPlayer>>(emptyList())
    val availableBatsmen: StateFlow<List<TeamPlayer>> = _availableBatsmen.asStateFlow()

    private val _availableBowlers = MutableStateFlow<List<TeamPlayer>>(emptyList())
    val availableBowlers: StateFlow<List<TeamPlayer>> = _availableBowlers.asStateFlow()

    private var linkedFixture: Fixture? = null

    init {
        // Collect first innings runs once inningsNumber becomes 2
        viewModelScope.launch {
            combine(_matchId, _inningsNumber) { id, num -> Pair(id, num) }.collect { (id, num) ->
                if (id != 0L && num == 2) {
                    val firstInn = inningsRepository.getInningsByNumberSync(id, 1)
                    _firstInningsRuns.value = firstInn?.totalRuns ?: 0
                }
            }
        }

        // Collect innings changes to load available players
        viewModelScope.launch {
            innings.collect { inn ->
                if (inn != null && _isTournamentMatch.value) {
                    loadAvailablePlayers()
                }
            }
        }
    }

    fun loadAvailablePlayers() {
        val fixture = linkedFixture ?: return
        val inn = innings.value ?: return
        val batting = inn.battingTeam
        val bowling = inn.bowlingTeam

        viewModelScope.launch {
            val battingTeamId = if (batting == fixture.team1Name) fixture.team1Id else fixture.team2Id
            val bowlingTeamId = if (bowling == fixture.team1Name) fixture.team1Id else fixture.team2Id

            val p11Batsmen = playingElevenRepository.getAvailableBatsmen(fixture.id, battingTeamId)
            val p11Bowlers = playingElevenRepository.getAvailableBowlers(fixture.id, bowlingTeamId)

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
        }
    }

    fun initMatch(matchId: Long, inningsNumber: Int) {
        _matchId.value = matchId
        _inningsNumber.value = inningsNumber
        _selectedBowlerForNewOver.value = null

        viewModelScope.launch {
            val fixture = tournamentRepository.getFixtureByLinkedMatchId(matchId)
            linkedFixture = fixture
            if (fixture != null) {
                _isTournamentMatch.value = true
                loadAvailablePlayers()
            } else {
                _isTournamentMatch.value = false
                _availableBatsmen.value = emptyList()
                _availableBowlers.value = emptyList()
            }
        }
    }

    fun recordNormalBall(runs: Int) {
        viewModelScope.launch {
            recordBall(runs, 0, BallType.NORMAL)
        }
    }

    fun recordExtraBall(runsExtra: Int, ballType: BallType) {
        viewModelScope.launch {
            recordBall(0, runsExtra, ballType)
        }
    }

    fun recordNoBall(runsBatsman: Int, runsExtra: Int) {
        viewModelScope.launch {
            recordBall(runsBatsman, runsExtra, BallType.NO_BALL)
        }
    }

    suspend fun recordBall(
        runsBatsman: Int,
        runsExtra: Int,
        ballType: BallType,
        isWicket: Boolean = false,
        dismissalType: DismissalType? = null,
        fielderName: String? = null,
        dismissedPlayerName: String? = null,
        nextBatsmanName: String? = null
    ) {
        val mid = _matchId.value
        val inum = _inningsNumber.value
        val currentBowlerName = currentBowler.value?.playerName
        recordBallUseCase(
            matchId = mid,
            inningsNumber = inum,
            runsBatsman = runsBatsman,
            runsExtra = runsExtra,
            ballType = ballType,
            isWicket = isWicket,
            dismissalType = dismissalType,
            fielderName = fielderName,
            dismissedPlayerName = dismissedPlayerName,
            bowlerName = currentBowlerName,
            nextBatsmanName = nextBatsmanName
        )
        _selectedBowlerForNewOver.value = null
        if (_isTournamentMatch.value) {
            loadAvailablePlayers()
        }
    }

    fun undoLastBall() {
        viewModelScope.launch {
            val mid = _matchId.value
            val inum = _inningsNumber.value
            undoLastBallUseCase(mid, inum)
            if (_isTournamentMatch.value) {
                loadAvailablePlayers()
            }
        }
    }

    fun selectNextBowler(bowlerName: String) {
        val mid = _matchId.value
        val inum = _inningsNumber.value
        val bName = bowlerName.trim()
        if (bName.isEmpty()) return

        viewModelScope.launch {
            val existing = inningsRepository.getBowlerSync(mid, inum, bName)
            val bowler = if (existing == null) {
                val newBowler = BowlerInnings(
                    matchId = mid,
                    inningsNumber = inum,
                    playerName = bName
                )
                inningsRepository.saveBowlersInnings(listOf(newBowler))
                newBowler
            } else {
                inningsRepository.updateBowlerInnings(existing)
                existing
            }
            _selectedBowlerForNewOver.value = bowler
        }
    }

    fun introduceNewBatsman(batsmanName: String) {
        val mid = _matchId.value
        val inum = _inningsNumber.value
        val batName = batsmanName.trim()
        if (batName.isEmpty()) return

        viewModelScope.launch {
            val existing = inningsRepository.getBatsmanSync(mid, inum, batName)
            if (existing == null) {
                val newBatsman = BatsmanInnings(
                    matchId = mid,
                    inningsNumber = inum,
                    playerName = batName
                )
                inningsRepository.saveBatsmenInnings(listOf(newBatsman))
            }
        }
    }

    fun switchStrike() {
        val mid = _matchId.value
        val inum = _inningsNumber.value
        viewModelScope.launch {
            val ballList = inningsRepository.getBallsForInningsSync(mid, inum)
            if (ballList.isNotEmpty()) {
                val lastBall = ballList.last()
                val updatedBall = lastBall.copy(
                    strikerName = lastBall.nonStrikerName,
                    nonStrikerName = lastBall.strikerName
                )
                inningsRepository.recordBall(updatedBall)
            } else {
                val active = activeBatsmen.value
                if (active.size >= 2) {
                    val striker = active[0]
                    val nonStriker = active[1]
                    val updatedStriker = striker.copy(playerName = nonStriker.playerName)
                    val updatedNonStriker = nonStriker.copy(playerName = striker.playerName)
                    inningsRepository.updateBatsmanInnings(updatedStriker)
                    inningsRepository.updateBatsmanInnings(updatedNonStriker)
                }
            }
        }
    }
}
