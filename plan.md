# CricScore — AI Agent Build Prompt
### Full-Stack Android Cricket Scoring App (Offline · MVVM · Room)
---

> **Instructions for Agent:** You are a senior Android engineer with 8+ years of production experience. Build a complete, offline-first cricket scoring app called **CricScore** using the exact specifications below. Every screen, every edge case, every database interaction must be handled properly. Do not skip any section. Do not leave TODOs. Deliver working, production-ready Kotlin code.

---

## 1. PROJECT OVERVIEW

**App Name:** CricScore  
**Package:** `com.cricscore.app`  
**Platform:** Android (minSdk 24, targetSdk 34, compileSdk 34)  
**Language:** Kotlin  
**Architecture:** MVVM + Repository Pattern + Use Cases  
**Database:** Room 2.6.x (fully offline, no internet required)  
**DI:** Hilt  
**Async:** Kotlin Coroutines + StateFlow  
**Navigation:** Jetpack Navigation Component (Single Activity)  
**Build System:** Gradle with Version Catalogs (`libs.versions.toml`)

**Purpose:** A fast, tap-optimized cricket scorer for local/gully cricket teams. Used mid-match on a single phone. Must be blazing fast, impossible to misuse, and visually clean.

---

## 2. DESIGN SYSTEM & THEME

### Color Palette
```kotlin
// All colors defined in res/values/colors.xml + MaterialTheme

// Backgrounds
ColorBackground     = #0D1117   // deep navy canvas
ColorSurface        = #161B22   // card surfaces
ColorSurfaceVariant = #1C2333   // elevated cards / input fields

// Accents
ColorPrimary        = #7ED321   // lime green — live states, CTAs, active
ColorSecondary      = #4A90D9   // blue — selection, toss, secondary actions
ColorTertiary       = #F5A623   // amber/orange — partnership, coin toss, warnings
ColorError          = #E53935   // red — wicket, OUT button, danger

// Text
ColorOnBackground   = #FFFFFF   // primary text
ColorTextSecondary  = #8B949E   // secondary labels, metadata
ColorTextMuted      = #484F58   // disabled / placeholder

// Specials
ColorWicket         = #E53935   // wicket markers
ColorFour           = #4A90D9   // boundary 4 indicator
ColorSix            = #7ED321   // boundary 6 indicator
ColorDot            = #8B949E   // dot ball
ColorNoBall         = #F5A623
ColorWide           = #9B59B6
ColorLive           = #7ED321   // pulsing dot for LIVE indicator
```

### Typography
```xml
<!-- res/font/ — Download and embed these .ttf files -->
<!-- Primary: DM Sans (weights: 400, 500, 600, 700) -->
<!-- Condensed numbers: Barlow Condensed (weights: 600, 700) -->
<!-- Fallback: sans-serif-condensed (system) -->

<!-- TextAppearances to define in themes.xml -->
TextAppearance.CricScore.ScoreLarge     → BarloCondensed 72sp Bold   (main score)
TextAppearance.CricScore.ScoreMedium    → BarlowCondensed 48sp Bold  (player runs)
TextAppearance.CricScore.ScoreSmall     → BarlowCondensed 28sp SemiBold
TextAppearance.CricScore.Label          → DM Sans 10sp Medium CAPS letter-spacing 1.5
TextAppearance.CricScore.Body           → DM Sans 14sp Regular
TextAppearance.CricScore.BodyMedium     → DM Sans 14sp Medium
TextAppearance.CricScore.Button         → DM Sans 16sp SemiBold
TextAppearance.CricScore.Caption        → DM Sans 11sp Regular
```

### Shape & Spacing
```kotlin
// CornerRadius
RadiusSmall  = 8dp
RadiusMedium = 12dp
RadiusLarge  = 16dp
RadiusXL     = 24dp

// Spacing scale (use consistently)
Space4  = 4dp
Space8  = 8dp
Space12 = 12dp
Space16 = 16dp
Space20 = 20dp
Space24 = 24dp

// Scoring buttons: 72dp × 72dp min touch target
// Primary CTA buttons: match_parent width, 56dp height
```

### Theme
```xml
<!-- themes.xml — Dark only, no light theme needed -->
Theme.CricScore → DayNight.NoActionBar, dark forced
StatusBar: transparent, dark icons = false (light icons on dark)
NavigationBar: match background (#0D1117)
```

---

## 3. FOLDER STRUCTURE

```
app/
├── src/main/
│   ├── java/com/cricscore/app/
│   │   ├── CricScoreApp.kt                    ← Hilt Application
│   │   ├── MainActivity.kt                     ← Single activity, NavHost
│   │   │
│   │   ├── core/
│   │   │   ├── base/
│   │   │   │   ├── BaseFragment.kt
│   │   │   │   └── BaseViewModel.kt
│   │   │   ├── extensions/
│   │   │   │   ├── ViewExtensions.kt           ← gone/visible helpers, click debounce
│   │   │   │   ├── FlowExtensions.kt           ← launchWhenStarted, collectIn
│   │   │   │   ├── NumberExtensions.kt         ← formatSR(), formatOvers(), twoDecimal()
│   │   │   │   └── FragmentExtensions.kt       ← navArgs shorthand, snackbar
│   │   │   ├── util/
│   │   │   │   ├── CricketCalculator.kt        ← ALL cricket math (SR, CRR, RRR, NRR)
│   │   │   │   ├── OversHelper.kt              ← ball ↔ over conversion, legal ball check
│   │   │   │   ├── DismissalHelper.kt          ← dismissal string builder
│   │   │   │   └── Constants.kt
│   │   │   └── di/
│   │   │       ├── DatabaseModule.kt
│   │   │       ├── RepositoryModule.kt
│   │   │       └── UseCaseModule.kt
│   │   │
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── CricScoreDatabase.kt        ← Room DB, version 1
│   │   │   │   ├── dao/
│   │   │   │   │   ├── MatchDao.kt
│   │   │   │   │   ├── InningsDao.kt
│   │   │   │   │   ├── BallDao.kt
│   │   │   │   │   ├── BatsmanInningsDao.kt
│   │   │   │   │   └── BowlerInningsDao.kt
│   │   │   │   ├── entity/
│   │   │   │   │   ├── MatchEntity.kt
│   │   │   │   │   ├── InningsEntity.kt
│   │   │   │   │   ├── BallEntity.kt
│   │   │   │   │   ├── BatsmanInningsEntity.kt
│   │   │   │   │   └── BowlerInningsEntity.kt
│   │   │   │   └── relations/
│   │   │   │       ├── MatchWithInnings.kt
│   │   │   │       └── InningsWithBalls.kt
│   │   │   └── repository/
│   │   │       ├── MatchRepository.kt          ← interface
│   │   │       ├── MatchRepositoryImpl.kt
│   │   │       ├── InningsRepository.kt
│   │   │       └── InningsRepositoryImpl.kt
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Match.kt                    ← pure domain models (no Room annotations)
│   │   │   │   ├── Innings.kt
│   │   │   │   ├── Ball.kt
│   │   │   │   ├── BatsmanInnings.kt
│   │   │   │   ├── BowlerInnings.kt
│   │   │   │   ├── BallType.kt                 ← enum: NORMAL, WIDE, NO_BALL, BYE, LEG_BYE
│   │   │   │   ├── DismissalType.kt            ← enum: BOWLED, CAUGHT, LBW, RUN_OUT, STUMPED, HIT_WICKET, RETIRED_HURT
│   │   │   │   ├── TossResult.kt
│   │   │   │   └── MatchStatus.kt              ← enum: SETUP, TOSS, INNINGS_1, INNINGS_2, COMPLETED
│   │   │   └── usecase/
│   │   │       ├── match/
│   │   │       │   ├── CreateMatchUseCase.kt
│   │   │       │   ├── GetRecentMatchesUseCase.kt
│   │   │       │   └── GetMatchByIdUseCase.kt
│   │   │       ├── innings/
│   │   │       │   ├── StartInningsUseCase.kt
│   │   │       │   ├── RecordBallUseCase.kt    ← CORE: all ball recording logic
│   │   │       │   ├── UndoLastBallUseCase.kt
│   │   │       │   ├── RecordWicketUseCase.kt
│   │   │       │   ├── CompleteInningsUseCase.kt
│   │   │       │   └── GetLiveInningsStateUseCase.kt
│   │   │       └── scoring/
│   │   │           ├── CalculateScorecardUseCase.kt
│   │   │           └── DetermineMatchResultUseCase.kt
│   │   │
│   │   └── ui/
│   │       ├── home/
│   │       │   ├── HomeFragment.kt
│   │       │   ├── HomeViewModel.kt
│   │       │   └── adapter/
│   │       │       └── RecentMatchAdapter.kt
│   │       ├── setup/
│   │       │   ├── MatchSetupFragment.kt
│   │       │   └── MatchSetupViewModel.kt
│   │       ├── toss/
│   │       │   ├── TossFragment.kt
│   │       │   └── TossViewModel.kt
│   │       ├── inningssetup/
│   │       │   ├── InningsSetupFragment.kt
│   │       │   └── InningsSetupViewModel.kt
│   │       ├── scoring/
│   │       │   ├── ScoringFragment.kt
│   │       │   └── ScoringViewModel.kt
│   │       ├── dismissal/
│   │       │   ├── DismissalBottomSheet.kt
│   │       │   └── DismissalViewModel.kt
│   │       ├── overcomplete/
│   │       │   ├── OverCompleteBottomSheet.kt
│   │       │   └── OverCompleteViewModel.kt
│   │       ├── scorecard/
│   │       │   ├── ScorecardFragment.kt
│   │       │   └── ScorecardViewModel.kt
│   │       └── result/
│   │           ├── ResultFragment.kt
│   │           └── ResultViewModel.kt
│   │
│   └── res/
│       ├── drawable/                           ← see Section 9 for full list
│       ├── font/
│       │   ├── dm_sans_regular.ttf
│       │   ├── dm_sans_medium.ttf
│       │   ├── dm_sans_semibold.ttf
│       │   ├── dm_sans_bold.ttf
│       │   ├── barlow_condensed_semibold.ttf
│       │   └── barlow_condensed_bold.ttf
│       ├── layout/
│       ├── menu/
│       ├── navigation/
│       │   └── nav_graph.xml
│       ├── values/
│       │   ├── colors.xml
│       │   ├── dimens.xml
│       │   ├── strings.xml
│       │   ├── themes.xml
│       │   └── font_families.xml
│       └── anim/
│           ├── slide_in_right.xml
│           ├── slide_out_left.xml
│           ├── fade_in.xml
│           └── fade_out.xml
```

---

## 4. DATABASE SCHEMA (Room)

### Entities

```kotlin
// MatchEntity
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val team1Name: String,
    val team2Name: String,
    val totalOvers: Int,
    val playersPerSide: Int,
    val tossWonByTeam: String,          // team name
    val tossDecision: String,           // "BAT" | "BOWL"
    val battingFirstTeam: String,
    val status: String,                 // MatchStatus enum name
    val team1Score: Int = 0,
    val team1Wickets: Int = 0,
    val team1Overs: String = "0.0",
    val team2Score: Int = 0,
    val team2Wickets: Int = 0,
    val team2Overs: String = "0.0",
    val resultText: String = "",
    val playerOfMatch: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// InningsEntity
@Entity(tableName = "innings", foreignKeys = [ForeignKey(entity = MatchEntity::class, ...)])
data class InningsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,             // 1 or 2
    val battingTeam: String,
    val bowlingTeam: String,
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val totalBalls: Int = 0,           // legal balls only
    val totalWides: Int = 0,
    val totalNoBalls: Int = 0,
    val totalByes: Int = 0,
    val totalLegByes: Int = 0,
    val isCompleted: Boolean = false,
    val targetScore: Int = 0           // set after innings 1
)

// BallEntity  ← Single source of truth for all scoring
@Entity(tableName = "balls", foreignKeys = [...])
data class BallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inningsId: Long,
    val overNumber: Int,               // 0-indexed
    val ballInOver: Int,               // 0-indexed legal balls
    val ballSequence: Int,             // absolute sequence (including extras)
    val batsmanName: String,
    val bowlerName: String,
    val runsScored: Int,               // runs off bat (not extras)
    val ballType: String,              // BallType enum name
    val extraRuns: Int = 0,
    val isWicket: Boolean = false,
    val dismissalType: String = "",    // DismissalType enum name
    val dismissedBatsmanName: String = "",
    val fielderName: String = "",
    val isLegalBall: Boolean = true,   // false for Wide/NoBall
    val timestamp: Long = System.currentTimeMillis()
)

// BatsmanInningsEntity
@Entity(tableName = "batsman_innings")
data class BatsmanInningsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inningsId: Long,
    val matchId: Long,
    val playerName: String,
    val teamName: String,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissalText: String = "",    // "c Sharma b Bumrah"
    val dismissalType: String = "",
    val battingOrder: Int = 0,
    val isOnStrike: Boolean = false,
    val isCurrentlyBatting: Boolean = false
)

// BowlerInningsEntity
@Entity(tableName = "bowler_innings")
data class BowlerInningsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inningsId: Long,
    val matchId: Long,
    val playerName: String,
    val teamName: String,
    val overs: Int = 0,               // complete overs bowled
    val ballsInCurrentOver: Int = 0,
    val maidens: Int = 0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0,
    val isCurrentBowler: Boolean = false
)
```

### DAOs — Key Methods

```kotlin
// MatchDao
@Query("SELECT * FROM matches ORDER BY createdAt DESC LIMIT 20")
fun getRecentMatches(): Flow<List<MatchEntity>>

@Query("SELECT * FROM matches WHERE id = :id")
suspend fun getMatchById(id: Long): MatchEntity?

@Insert suspend fun insertMatch(match: MatchEntity): Long
@Update suspend fun updateMatch(match: MatchEntity)

// InningsDao
@Query("SELECT * FROM innings WHERE matchId = :matchId AND inningsNumber = :number")
suspend fun getInnings(matchId: Long, number: Int): InningsEntity?

// BallDao — UNDO uses this
@Query("SELECT * FROM balls WHERE inningsId = :inningsId ORDER BY ballSequence DESC LIMIT 1")
suspend fun getLastBall(inningsId: Long): BallEntity?

@Delete suspend fun deleteBall(ball: BallEntity)

@Query("SELECT * FROM balls WHERE inningsId = :inningsId AND overNumber = :over ORDER BY ballSequence")
suspend fun getBallsForOver(inningsId: Long, over: Int): List<BallEntity>
```

---

## 5. DOMAIN MODELS & CRICKET LOGIC

### CricketCalculator.kt — All Formulas

```kotlin
object CricketCalculator {

    // Strike rate = (runs / balls) * 100, rounded to 1 decimal
    fun strikeRate(runs: Int, balls: Int): Double

    // Current Run Rate = (totalRuns / legalBallsBowled) * 6
    fun currentRunRate(runs: Int, legalBalls: Int): Double

    // Required Run Rate = (runsNeeded / ballsRemaining) * 6
    fun requiredRunRate(runsNeeded: Int, ballsRemaining: Int): Double

    // Balls remaining = (totalOvers * 6) - legalBallsBowled
    fun ballsRemaining(totalOvers: Int, legalBallsBowled: Int): Int

    // Overs display: 8 legal balls → "1.2", 18 → "3.0"
    fun oversDisplay(legalBalls: Int): String

    // Convert legal ball count to (overs, ballsInOver)
    fun ballToOverBreakdown(legalBalls: Int): Pair<Int, Int>

    // Is innings complete: wickets >= playersPerSide-1 OR legalBalls >= totalOvers*6
    fun isInningsComplete(wickets: Int, legalBalls: Int, playersPerSide: Int, totalOvers: Int): Boolean

    // Is over complete: ballsInCurrentOver == 6 (legal balls only)
    fun isOverComplete(legalBallsInOver: Int): Boolean

    // Extras total
    fun totalExtras(wides: Int, noBalls: Int, byes: Int, legByes: Int): Int

    // Fall of wicket string: "43/1 (4.2)"
    fun fowString(runs: Int, wicketNumber: Int, legalBalls: Int): String

    // Economy rate for bowler
    fun economyRate(runs: Int, legalBalls: Int): Double
}
```

### OversHelper.kt

```kotlin
object OversHelper {
    // A Wide or No Ball does NOT count as a legal ball
    fun isLegalDelivery(ballType: BallType): Boolean =
        ballType != BallType.WIDE && ballType != BallType.NO_BALL

    // Striker faces: normal ball, bye, leg bye (NOT wide)
    fun strikerFacesBall(ballType: BallType): Boolean =
        ballType != BallType.WIDE

    // Strike rotates on odd runs scored (including extras for wide/noBall)
    fun shouldRotateStrike(runsTotal: Int): Boolean = runsTotal % 2 == 1

    // After over completes, strike always rotates
    fun rotateStrikeEndOfOver()
}
```

### RecordBallUseCase — Complete Logic

```kotlin
// This is the MOST CRITICAL use case. Handle ALL edge cases:

suspend fun execute(params: RecordBallParams): Result<BallResult> {
    // 1. Validate innings is active (not complete)
    // 2. Determine isLegalBall (Wide/NoBall = not legal)
    // 3. Calculate total runs for this delivery:
    //    - Normal: runsScored (off bat)
    //    - Wide: extraRuns (default 1) — no bat runs
    //    - No Ball: extraRuns (1) + runsScored (bat runs or leg bye)
    //    - Bye: extraRuns (runsScored treated as bye)
    //    - Leg Bye: extraRuns (runsScored treated as legbye)
    // 4. Update BatsmanInnings:
    //    - Increment balls only if isLegalBall AND not Wide
    //    - Increment runs: only bat runs (not extras)
    //    - Increment fours/sixes if normal delivery AND runs==4/6
    // 5. Update BowlerInnings:
    //    - Increment legalBalls if isLegalBall
    //    - Increment runs = totalRunsThisBall (wides + noBall extras count against bowler)
    //    - Check maiden: if over complete and 0 runs → increment maiden
    // 6. Update InningsEntity: totalRuns, wickets, totalBalls
    // 7. Check strike rotation:
    //    - Odd runs: rotate
    //    - Wide: no rotation unless runs=1 (wide+run=1 still odd)
    //    - End of over: always rotate
    // 8. Check over complete → trigger OverComplete event
    // 9. Check innings complete → trigger InningsComplete event
    // 10. Insert BallEntity record
    // 11. Return BallResult with updated state
}
```

### UndoLastBallUseCase

```kotlin
// 1. Fetch last BallEntity from DB
// 2. Reverse ALL changes from that ball:
//    - Subtract runs from InningsEntity
//    - If was wicket: restore batsman (isOut=false, restore stats)
//    - Subtract from BatsmanInnings (runs, balls, 4s, 6s)
//    - Subtract from BowlerInnings (runs, balls, wickets)
//    - Reverse strike position
// 3. Delete the BallEntity
// 4. Emit updated state
// Only allow undo of the immediately last ball (single level undo)
```

---

## 6. VIEWMODELS & UI STATE

### ScoringUiState (most complex)

```kotlin
data class ScoringUiState(
    val matchId: Long = 0,
    val inningsNumber: Int = 1,
    val battingTeamName: String = "",
    val bowlingTeamName: String = "",
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val legalBallsBowled: Int = 0,
    val totalOvers: Int = 10,
    val currentRunRate: Double = 0.0,
    val extras: ExtrasState = ExtrasState(),

    // Striker
    val striker: BatsmanUiState? = null,
    // Non-striker
    val nonStriker: BatsmanUiState? = null,
    // Current bowler
    val currentBowler: BowlerUiState? = null,

    // Partnership
    val partnershipRuns: Int = 0,
    val partnershipBalls: Int = 0,

    // Current over balls (for over strip display)
    val currentOverBalls: List<BallDisplayItem> = emptyList(),

    // Chase panel (innings 2 only)
    val isChasing: Boolean = false,
    val targetScore: Int = 0,
    val runsNeeded: Int = 0,
    val ballsRemaining: Int = 0,
    val requiredRunRate: Double = 0.0,
    val isAheadOfRRR: Boolean = false,

    // Events
    val event: ScoringEvent? = null,
    val isLoading: Boolean = false
)

data class BatsmanUiState(
    val name: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: Double,
    val isOnStrike: Boolean
)

data class BowlerUiState(
    val name: String,
    val overs: Int,
    val ballsInOver: Int,   // legal balls in current over
    val maidens: Int,
    val runs: Int,
    val wickets: Int
)

data class ExtrasState(
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0
)

data class BallDisplayItem(
    val display: String,        // "·", "1", "4", "6", "W", "WD", "NB"
    val ballType: BallType,
    val isWicket: Boolean = false
)

sealed class ScoringEvent {
    object WicketFallen : ScoringEvent()
    data class OverComplete(val overNumber: Int, val bowlerName: String) : ScoringEvent()
    object InningsComplete : ScoringEvent()
    object UndoSuccess : ScoringEvent()
}
```

### ScoringViewModel

```kotlin
@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val recordBallUseCase: RecordBallUseCase,
    private val undoLastBallUseCase: UndoLastBallUseCase,
    private val recalculateLiveStateUseCase: GetLiveInningsStateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringUiState())
    val uiState: StateFlow<ScoringUiState> = _uiState.asStateFlow()

    fun onRunScored(runs: Int)
    fun onWide(extraRuns: Int = 1)
    fun onNoBall(batRuns: Int = 0)
    fun onBye(runs: Int)
    fun onLegBye(runs: Int)
    fun onWicket()           // triggers WicketFallen event → DismissalBottomSheet
    fun onUndoTapped()
    fun onDismissalConfirmed(params: DismissalParams)
    fun onNextBowlerSelected(bowlerName: String)
    fun onSwitchStrikeTapped()

    // Rebuild full state from DB after any change
    private suspend fun refreshState()
}
```

---

## 7. SCREENS — LAYOUT SPECIFICATIONS

### SCREEN 1 — HomeFragment (fragment_home.xml)

```
Root: CoordinatorLayout, background=#0D1117
├── ConstraintLayout (content)
│   ├── ImageView: app logo (ic_cricscore_logo, 80×80dp, centered top area)
│   ├── TextView: "CricScore" — DM Sans Bold 32sp, white, below logo
│   ├── TextView: "LIVE MATCH SCORER" — Label style, ColorPrimary, letter-spacing 2
│   ├── MaterialButton: "▶  Start New Match"
│   │   style=Widget.Material3.Button, fullWidth, height=56dp
│   │   icon=ic_play, backgroundTint=ColorPrimary, textColor=black
│   │   margin top 40dp from subtitle
│   ├── MaterialButton: "📋  Recent Matches"
│   │   style=Widget.Material3.Button.OutlinedButton, fullWidth, height=52dp
│   │   strokeColor=ColorSurfaceVariant, textColor=white
│   ├── TextView: "Designed for gully cricket · No ads · No fluff"
│   │   Caption style, ColorTextMuted, centered
│   └── RecyclerView: recent matches (gone if empty, visible after data loads)
│       └── item_recent_match.xml:
│           ├── MaterialCardView: background=#161B22, radius=12dp, margin 8dp
│           ├── Row: [Team1 (dot•) vs Team2 (dot•)] [DATE right-aligned]
│           ├── Row: Result text (e.g., "Hawks won by 34 runs") [Overs label]
│           └── Bottom: [Score1 / Score2] chip row
```

### SCREEN 2 — MatchSetupFragment (fragment_match_setup.xml)

```
Root: NestedScrollView, background=#0D1117
├── LinearLayout (vertical, padding 16dp)
│   ├── [Back arrow] STEP 1 OF 3 / "Match Setup" header
│   ├── MaterialCardView (surface): "TEAM NAMES"
│   │   ├── TextInputLayout + TextInputEditText: Team 1 name
│   │   │   prefixIcon: ic_circle_blue (colored dot)
│   │   └── TextInputLayout + TextInputEditText: Team 2 name
│   │       prefixIcon: ic_circle_red
│   ├── MaterialCardView: "NUMBER OF OVERS"
│   │   └── ChipGroup (singleSelection):
│   │       Chips: [5] [10] [15] [20] [✏ Custom]
│   │       selected chip: background=ColorPrimary, textColor=black
│   │       unselected: background=ColorSurfaceVariant, textColor=white
│   ├── MaterialCardView: "PLAYERS PER SIDE"
│   │   └── ChipGroup: [6] [7] [8] [9] [10] [11]
│   │       same chip styling
│   └── MaterialButton: "Proceed to Toss →"
│       fullWidth, 56dp, ColorSecondary background

Validation rules:
- Both team names required (non-empty, max 20 chars)
- Default overs: 10, default players: 9
- Custom overs: show NumberPicker dialog (1–50)
- Button disabled until both names filled
```

### SCREEN 3 — TossFragment (fragment_toss.xml)

```
Root: ConstraintLayout, background=#0D1117
├── [Back] STEP 2 OF 3 / "Coin Toss" header
├── FrameLayout: coin animation area (180×180dp, centered)
│   ├── ImageView: coin face (ic_coin_heads / ic_coin_tails)
│   │   animates with ObjectAnimator on scaleX (0→1→0→1) for flip effect
│   └── View: glow effect (circular, semi-transparent amber, blurred via elevation)
├── TextView: "HEADS" / "TAILS" — result display, below coin
├── MaterialCardView: "WHO CALLS?"
│   └── Two buttons side by side (toggle): [Team1] [Team2]
│       selected: ColorSecondary background
├── MaterialCardView: "TEAM_X CALLS..."  (shown after team selected)
│   └── Two buttons: [HEADS] [TAILS]
│       selected: ColorTertiary background
├── MaterialCardView: TOSS RESULT (shown after call)  ← green background
│   ├── "🎉 TOSS RESULT" label
│   ├── "Team X won the toss" — Bold 20sp
│   └── "and elected to BAT/BOWL" — Body, colored
├── MaterialCardView: "TEAM_X ELECTS TO..."
│   └── Two buttons: [🏏 BAT] [🎳 BOWL]
│       BAT selected: ColorPrimary background
└── MaterialButton: "Confirm & Setup Innings →"
    ColorSecondary, fullWidth, 56dp

Toss logic: Math.random() < 0.5 = HEADS, else TAILS. Auto-flip animation 600ms.
```

### SCREEN 4 — InningsSetupFragment (fragment_innings_setup.xml)

```
Root: NestedScrollView, background=#0D1117
├── [Back] STEP 3 OF 3 / "Innings Setup" header
├── MaterialCardView: batting team banner
│   ├── Label "🏏 BATTING"
│   └── TextView: battingTeamName, Bold 20sp, ColorPrimary border
├── MaterialCardView: "OPENING BATSMEN"
│   ├── "Striker (facing first)"
│   │   ├── TextInputLayout: player name input with "⭐ STRIKE" chip badge (right)
│   │   │   TextInputEditText: type to fill striker name
│   ├── "Non-Striker"
│   │   └── AutoCompleteTextView (dropdown): non-striker name
├── MaterialCardView: "🎳 BOWLING" — bowling team banner
│   └── "OPENING BOWLER"
│       └── AutoCompleteTextView: bowler name from bowling team players
├── MaterialCardView: "ADD PLAYERS (OPTIONAL)"  ← to pre-populate player list
│   └── TextInputLayout + [+] button row
│       Chips displayed below for added players (dismissible)
└── MaterialButton: "🏏 Start Match"
    fullWidth, 56dp, ColorPrimary

Validation: striker + non-striker must be different names, both non-empty.
Bowler name required.
```

### SCREEN 5 — ScoringFragment (fragment_scoring.xml) ← MOST IMPORTANT

```
Root: ConstraintLayout, background=#0D1117
│
├── TOP HEADER (inningsInfo strip, height ~96dp):
│   ├── Row 1: [🟢 LIVE · 1ST INNINGS] [10 Overs · 9 Players right-aligned]
│   │   LIVE = pulsing green dot (AnimatedDrawable)
│   ├── Row 2: TextView score: "87" — Barlow Condensed 72sp Bold white
│   │           "/3" — 36sp secondary
│   ├── Row 3 right: "OVERS" label 10sp + "8.4" 28sp Bold + "CRR: 10.04" green 12sp
│   └── Row 4: Current over strip (horizontal, scrollable if needed)
│       Each ball: small circle/chip 28×28dp
│       Colors: dot=gray, 1/2/3=white, 4=blue, 6=lime, W=red, WD=purple, NB=amber
│
├── BATSMEN CARDS ROW (two cards side by side, equal width):
│   ├── Striker card (border=ColorPrimary when on strike):
│   │   ├── "STRIKER" label + "⭐ ON STRIKE" badge (blue chip)
│   │   ├── Name: DM Sans SemiBold 16sp
│   │   ├── Runs: Barlow 42sp Bold white
│   │   ├── "(31)" balls: Body, muted
│   │   └── "SR: 135.5" — Caption, ColorPrimary
│   └── Non-Striker card (dimmer, no border):
│       ├── "NON-STRIKER" label
│       ├── Name, Runs (36sp), (balls), SR
│
├── BOWLER + PARTNERSHIP ROW (two cards side by side):
│   ├── Bowler card:
│   │   ├── "🎳 BOWLER" label
│   │   ├── Name: SemiBold 14sp
│   │   └── "3-0-18-1" — SemiBold 16sp + "O-M-R-W" caption below, muted
│   └── Partnership card:
│       ├── "🤝 PARTNERSHIP" label
│       ├── "54" Barlow 32sp + "(41)" muted
│       └── "Runs (Balls)" caption
│
├── EXTRAS ROW (compact, single line):
│   "EXTRAS:  Wd 3   NB 1   B 0   LB 2"
│   background=ColorSurfaceVariant, padding 8dp, radius 8dp
│
├── SCORING GRID (6 big buttons in 3×2 or 2×3):
│   Row 1: [0] [1] [2]  — each 72dp min height
│   Row 2: [3] [4-blue] [6-green]
│   MaterialButton each, style=rounded, bold text
│   0/1/2/3: background=ColorSurfaceVariant
│   4: background=ColorSecondary (blue)
│   6: background=ColorPrimary (lime green)
│
├── EXTRAS ROW 2 (4 small buttons):
│   [+1 WIDE] [+1 NO BALL] [B BYE] [LB LEG BYE]
│   each ~25% width, height 44dp
│   WIDE: purple tint, NO BALL: amber, BYE/LB: gray
│
├── WICKET BUTTON:
│   MaterialButton fullWidth, height=52dp, background=ColorError
│   "🔴 WICKET — OUT" — Bold 16sp, white
│
└── BOTTOM ACTION BAR (elevation above, height 56dp):
    [← Undo] [⇄ Switch Strike] [📋 Scorecard]
    equal weight, icon+text, compact

2nd INNINGS CHASE PANEL (inserted between header and batsmen cards):
    MaterialCardView (border=ColorSecondary):
    2×2 grid:
    [TARGET: 128]  [NEED: 41 \n in 18 balls]
    [CRR: 8.17 ▲AHEAD (green)] [RRR: 13.67 ▲NEEDED (red)]
    Colors: CRR green if ahead, red if behind. RRR inverse.
```

### SCREEN 6 — DismissalBottomSheet (layout_bottomsheet_dismissal.xml)

```
BottomSheetDialogFragment, peek full content (not draggable away accidentally)
├── Header: "WICKET! 🔴  Select dismissal type"
│   + [✕] close button top-right (only closes if not mid-confirmation)
├── DismissalType grid (3×2):
│   [🏏 Bowled]   [🙌 Caught]
│   [🦵 LBW]      [🏃 Run Out]
│   [🧤 Stumped]  [🪵 Hit Wicket]
│   full-width: [Retired Hurt / Out] — secondary style
│   Selected type: ColorError background (red)
├── ConditionalSection (show for: Caught, Run Out, Stumped):
│   "CAUGHT BY (FIELDER NAME)" or "RUN OUT BY"
│   TextInputLayout: fielder name input
├── ConditionalSection (show for Caught, Stumped, Run Out):
│   optional non-striker dismissal toggle for run-out of non-striker
└── MaterialButton: "Confirm Dismissal →"
    fullWidth, ColorError, 52dp
    Disabled until dismissal type selected

Also show: 2nd innings chase panel preview at top of bottomsheet (for context while picking dismissal)
```

### SCREEN 7 — OverCompleteBottomSheet (layout_bottomsheet_over.xml)

```
BottomSheetDialogFragment (non-cancellable)
├── Header: "End of Over {N}" — Bold 20sp, ColorTertiary
├── Bowler line: "J. Bumrah — 3-0-18-1" — Body muted
├── Over summary strip: same ball indicator as scoring screen
├── "7 runs · 1 wicket this over" — summary text
├── "SELECT NEXT BOWLER" label
├── RecyclerView/LinearLayout: available bowlers list
│   item_bowler_select.xml:
│   ├── [Name left] [figures right: "2-0-14-0"]
│   └── Selected: border=ColorPrimary
│   Excluded: last bowler (greyed out + "JUST BOWLED" badge, non-tappable)
│   Excluded: bowlers who have bowled their quota (totalOvers/playersPerSide max)
└── MaterialButton: "Start Over {N+1} →"
    Disabled until bowler selected
```

### SCREEN 8 — ScorecardFragment (fragment_scorecard.xml)

```
Root: NestedScrollView
├── Header: "INNINGS COMPLETE / Mumbai Strikers"
├── Score card: big score box
│   ├── "127/8" — Barlow 64sp Bold
│   ├── "10 Overs · CRR: 12.70" — Body muted
│   └── "Extras: 8 (Wd:3, NB:1, B:2, LB:2)" — colored chip
│
├── RecyclerView: BATTING table
│   Columns: Batsman | R | B | 4s | SR
│   - Name bold, dismissal text italic muted below name
│   - "★" for not out
│   - Row: colorize if 4s>0 or sixes>0
│   Extras row at bottom
│
├── MaterialCardView: FALL OF WICKETS
│   Scrollable horizontal chips: "43/1 (4.2)" "79/2 (7.1)" etc.
│
├── RecyclerView: BOWLING table
│   Columns: Bowler | O | M | R | W | Econ
│   - W column: highlight non-zero in ColorPrimary
│
├── MaterialCardView: TARGET SET (if innings 1 complete)
│   "Delhi Kings need 128 runs"
│   "in 10 overs · RRR: 12.80"
│   background=ColorSecondary tint
│
└── MaterialButton: "Start 2nd Innings →"
    or "View Result" if innings 2 complete
```

### SCREEN 9 — 2nd Innings Scoring
Same as Screen 5 but with chase panel always visible (not collapsible). RRR/CRR update after every ball. RRR red if higher than CRR, green if lower. "Balls remaining" countdown visible.

### SCREEN 10 — ResultFragment (fragment_result.xml)

```
Root: NestedScrollView, background=#0D1117
├── WIN BANNER: MaterialCardView (ColorPrimary/ColorError border depending on winner)
│   ├── "🏆 MATCH RESULT" label
│   ├── "Mumbai Strikers" — Bold 28sp
│   ├── "won by 34 runs!" — ColorPrimary/green 22sp (or "won by 5 wickets!")
│   └── "127/8 vs 93/10 (9.2 ov)" — muted subtitle
│
├── SCORE COMPARISON ROW: two cards
│   [TEAM 1: 127/8 \n 10 Overs] [TEAM 2: 93/10 \n 9.2 Overs]
│   Winner card: brighter
│
├── PLAYER OF THE MATCH:
│   MaterialCardView, border=ColorTertiary (amber)
│   ├── "🏅 PLAYER OF THE MATCH"  + [Change] button
│   ├── Avatar placeholder (ic_player_avatar, 48dp)
│   ├── Player name Bold 18sp
│   ├── Stats: "28 (14) · 2-0-12-2"
│   └── "Bat + Ball all-round performance" — muted italic
│
├── SELECT DIFFERENT PLAYER: ChipGroup
│   All notable players as selectable chips
│
└── BUTTON ROW:
    [▶ New Match] (ColorPrimary) [📋 Full Scorecard] (outlined)
    "CricScore · Local Cricket Scorer · No ads · No fluff" — muted footer
```

---

## 8. NAVIGATION GRAPH (nav_graph.xml)

```xml
Destinations:
home_fragment          → startDestination
match_setup_fragment   → args: none
toss_fragment          → args: matchId: Long
innings_setup_fragment → args: matchId: Long, inningsNumber: Int
scoring_fragment       → args: matchId: Long, inningsNumber: Int
scorecard_fragment     → args: matchId: Long, inningsNumber: Int, showStartNext: Boolean
result_fragment        → args: matchId: Long

Actions:
home → match_setup (Start New Match click)
home → scorecard (resume in-progress match)
match_setup → toss (with matchId)
toss → innings_setup
innings_setup → scoring
scoring → scorecard (innings complete OR scorecard button)
scorecard → scoring (Start 2nd innings)
scorecard → result (match complete)
result → home (New Match)

Dialogs (BottomSheets, NOT in nav graph, shown directly):
- DismissalBottomSheet: shown from ScoringFragment on wicket event
- OverCompleteBottomSheet: shown from ScoringFragment on over complete event
```

---

## 9. DRAWABLE RESOURCES

```
All drawables in res/drawable/ as Vector Drawable XML (no PNGs required):

ic_cricscore_logo.xml       → cricket bat + ball composition, colorful
ic_play.xml                 → filled triangle (play)
ic_recent_matches.xml       → clipboard icon
ic_back_arrow.xml           → left arrow
ic_star_strike.xml          → star for on-strike badge
ic_coin_heads.xml           → circular coin, lion face emoji-style
ic_coin_tails.xml           → circular coin, back side
ic_cricket_bat.xml          → bat silhouette
ic_cricket_ball.xml         → ball with seam lines
ic_bowling.xml              → person bowling silhouette
ic_wicket.xml               → three stumps
ic_player_avatar.xml        → generic person silhouette
ic_trophy.xml               → trophy cup
ic_medal.xml                → medal (for POTM)
ic_undo.xml                 → curved left arrow
ic_switch.xml               → two arrows rotating
ic_scorecard.xml            → grid/table icon
ic_check.xml                → checkmark
ic_close.xml                → X
ic_dot_live.xml             → animated pulsing green circle (AnimatedVectorDrawable)
ic_plus.xml                 → plus sign

Shapes (backgrounds):
bg_chip_selected.xml        → filled rounded rect, ColorPrimary fill
bg_chip_unselected.xml      → filled rounded rect, ColorSurfaceVariant fill
bg_button_primary.xml       → ColorPrimary fill, radius 12dp
bg_button_secondary.xml     → ColorSecondary fill, radius 12dp
bg_button_error.xml         → ColorError fill, radius 12dp
bg_button_outline.xml       → stroke ColorSurfaceVariant, transparent fill
bg_scoring_button.xml       → ColorSurfaceVariant fill, radius 12dp
bg_scoring_button_4.xml     → ColorSecondary fill, radius 12dp
bg_scoring_button_6.xml     → ColorPrimary fill, radius 12dp
bg_card_surface.xml         → ColorSurface fill, radius 16dp
bg_striker_card.xml         → ColorSurface fill, stroke ColorPrimary 1.5dp, radius 12dp
bg_ball_indicator_*.xml     → per ball type colored circle backgrounds
```

---

## 10. GRADLE CONFIGURATION

### libs.versions.toml

```toml
[versions]
kotlin = "1.9.22"
agp = "8.3.0"
hilt = "2.51"
room = "2.6.1"
navigation = "2.7.7"
coroutines = "1.7.3"
lifecycle = "2.7.0"
material = "1.11.0"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
navigation-fragment = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
navigation-ui = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "1.9.22-1.0.17" }
```

---

## 11. EDGE CASES & RULES — MUST HANDLE ALL

```
Cricket Logic:
✅ Wide: +1 run, not a legal ball, bowled again, striker doesn't face (no balls increment)
✅ No Ball: +1 run, not a legal ball, bowled again, bat runs count for batsman
✅ Bye: extras, not off bat, legal ball, balls count for striker (not runs)
✅ Leg Bye: same as Bye
✅ Last ball of over + Wide: over NOT complete, bowl again
✅ Last ball of over + No Ball: over NOT complete, bowl again
✅ Last wicket: if playersPerSide - 1 wickets → innings over (10 players, 9 down = all out)
✅ Consecutive bowling: same bowler cannot bowl two consecutive overs
✅ Undo a wicket: restore batsman stats, remove from dismissed list
✅ Undo across over boundary: if undoing the first ball of a new over, restore to previous over
✅ Run-out on a no-ball: no-ball runs still count, wicket counts (run-out only)
✅ Byes/Leg-byes on no-ball: no-ball (1 extra) + bye runs (additional extras)
✅ Overthrows: handled by entering total runs scored including overthrows
✅ Strike at end of over: always rotates regardless of runs
✅ Strike rotation: odd runs = rotate; even runs = stay; wide = only if running (handled as 1 run)
✅ Partnership resets: every time a wicket falls, partnership counter resets
✅ Bowler quota: max overs per bowler = totalOvers / playersPerSide (rounded down)
✅ Super over not required (future enhancement placeholder only)
✅ DLS not required (gully cricket, simple format)

UI Edge Cases:
✅ Match in progress (app killed): resume from DB on home screen as "Continue Match" button
✅ Single tap debounce on scoring buttons (150ms) to prevent double scoring
✅ Confirm dialog before undo if it would undo a wicket
✅ Innings complete dialog auto-shows with full summary before going to scorecard
✅ If all players out before overs: innings completes normally
✅ Empty player name → use "Player {n}" as default (never crash on null name)
✅ Team name too long: truncate with ellipsis in header, full name in setup
✅ 0 extras all types: show "Extras: 0" cleanly
✅ Perfect over (maiden): highlight bowler card briefly in green
✅ Back press during match: confirmation dialog "Are you sure? Match progress will be lost"
✅ Scorecard during match: accessible via bottom bar, pressing back returns to scoring
```

---

## 12. ANIMATIONS & POLISH

```
All animations: lightweight, sub-200ms where possible

1. Coin flip (TossFragment):
   ObjectAnimator on ImageView scaleX: 1.0 → 0.0 → 1.0
   Duration: 600ms, interpolator: AccelerateDecelerate
   Switch image at 0.0 point to simulate flip

2. Run scored: scoring button scale pulse (1.0 → 0.95 → 1.0, 100ms)

3. Wicket: red flash overlay on striker card, shake animation on score
   ViewPropertyAnimator: translationX ±8dp, 3 cycles, 200ms total

4. Six scored: brief green glow on 6 button via StateListAnimator

5. Live dot: AnimatedVectorDrawable, alpha 1.0→0.3→1.0, infinite, 1000ms period

6. BottomSheet: default Material BottomSheet enter animation (slide up)

7. Navigation transitions: slide_in_right / slide_out_left (200ms)

8. Toss result banner: fade in + slide up, 300ms

9. Chase panel: RRR text color transition (green↔red) animated with ArgbEvaluator

10. Score update: TextSwitcher with slide up animation on score text change
```

---

## 13. STRINGS (res/values/strings.xml) — KEY STRINGS

```xml
<!-- App -->
<string name="app_name">CricScore</string>
<string name="tagline">LIVE MATCH SCORER</string>
<string name="designed_for">Designed for gully cricket · No ads · No fluff</string>

<!-- Home -->
<string name="start_new_match">Start New Match</string>
<string name="recent_matches">Recent Matches</string>
<string name="continue_match">Continue Match</string>
<string name="no_recent_matches">No matches yet. Start your first match!</string>

<!-- Setup -->
<string name="match_setup">Match Setup</string>
<string name="step_n_of_m">Step %1$d of 3</string>
<string name="team_names">TEAM NAMES</string>
<string name="team_1_hint">Team 1 name</string>
<string name="team_2_hint">Team 2 name</string>
<string name="number_of_overs">NUMBER OF OVERS</string>
<string name="players_per_side">PLAYERS PER SIDE</string>
<string name="proceed_to_toss">Proceed to Toss →</string>
<string name="custom_overs">Custom</string>

<!-- Toss -->
<string name="coin_toss">Coin Toss</string>
<string name="who_calls">WHO CALLS?</string>
<string name="calls_label">%s CALLS...</string>
<string name="toss_result_won">%s won the toss</string>
<string name="toss_elected_to">and elected to %s</string>
<string name="elects_to">%s ELECTS TO...</string>
<string name="confirm_setup_innings">Confirm &amp; Setup Innings →</string>
<string name="heads">HEADS</string>
<string name="tails">TAILS</string>
<string name="bat">BAT</string>
<string name="bowl">BOWL</string>

<!-- Innings Setup -->
<string name="innings_setup">Innings Setup</string>
<string name="batting_label">BATTING</string>
<string name="bowling_label">BOWLING</string>
<string name="opening_batsmen">OPENING BATSMEN</string>
<string name="striker_facing_first">Striker (facing first)</string>
<string name="non_striker">Non-Striker</string>
<string name="opening_bowler">OPENING BOWLER</string>
<string name="add_players_optional">ADD PLAYERS (OPTIONAL)</string>
<string name="start_match">Start Match</string>

<!-- Scoring -->
<string name="live_innings_1">LIVE · 1ST INNINGS</string>
<string name="live_innings_2">LIVE · 2ND INNINGS</string>
<string name="overs_label">OVERS</string>
<string name="crr_label">CRR: %s</string>
<string name="striker_label">STRIKER</string>
<string name="on_strike_badge">⭐ ON STRIKE</string>
<string name="non_striker_label">NON-STRIKER</string>
<string name="sr_label">SR: %s</string>
<string name="bowler_label">BOWLER</string>
<string name="partnership_label">PARTNERSHIP</string>
<string name="runs_balls_format">%1$d (%2$d)</string>
<string name="extras_format">Wd %1$d   NB %2$d   B %3$d   LB %4$d</string>
<string name="wicket_out">WICKET — OUT</string>
<string name="undo">Undo</string>
<string name="switch_strike">Switch</string>

<!-- Chase -->
<string name="target_label">TARGET</string>
<string name="need_label">NEED</string>
<string name="in_n_balls">in %d balls</string>
<string name="rrr_label">RRR</string>
<string name="ahead_label">▲ AHEAD</string>
<string name="needed_label">▲ NEEDED</string>

<!-- Dismissal -->
<string name="wicket_title">WICKET! 🔴</string>
<string name="select_dismissal">Select dismissal type</string>
<string name="bowled">Bowled</string>
<string name="caught">Caught</string>
<string name="lbw">LBW</string>
<string name="run_out">Run Out</string>
<string name="stumped">Stumped</string>
<string name="hit_wicket">Hit Wicket</string>
<string name="retired_hurt">Retired Hurt / Out</string>
<string name="caught_by">CAUGHT BY (FIELDER NAME)</string>
<string name="fielder_hint">Fielder name...</string>
<string name="confirm_dismissal">Confirm Dismissal →</string>

<!-- Over Complete -->
<string name="end_of_over">End of Over %d</string>
<string name="select_next_bowler">SELECT NEXT BOWLER</string>
<string name="start_next_over">Start Over %d →</string>
<string name="just_bowled">JUST BOWLED</string>
<string name="over_summary">%1$d runs · %2$d wicket(s) this over</string>

<!-- Scorecard -->
<string name="innings_complete">INNINGS COMPLETE</string>
<string name="batting_table_header">BATTING</string>
<string name="bowling_table_header">BOWLING</string>
<string name="fall_of_wickets">FALL OF WICKETS</string>
<string name="target_set">TARGET SET</string>
<string name="team_needs_runs">%1$s need %2$d runs</string>
<string name="in_overs_rrr">in %1$d overs · RRR: %2$s</string>
<string name="start_2nd_innings">Start 2nd Innings →</string>

<!-- Result -->
<string name="match_result">MATCH RESULT</string>
<string name="won_by_runs">won by %d runs</string>
<string name="won_by_wickets">won by %d wickets</string>
<string name="match_tied">Match Tied!</string>
<string name="player_of_match">PLAYER OF THE MATCH</string>
<string name="new_match">New Match</string>
<string name="full_scorecard">Full Scorecard</string>
<string name="footer_tagline">CricScore · Local Cricket Scorer · No ads · No fluff</string>
```

---

## 14. TESTING CONSIDERATIONS

```kotlin
// Unit tests required for:
CricketCalculatorTest     → all formula methods
OversHelperTest           → legal ball detection, over completion
RecordBallUseCaseTest     → every ball type, edge cases
UndoLastBallUseCaseTest   → undo across overs, undo wickets
DismissalHelperTest       → dismissal string formatting

// Instrument tests (optional but recommended):
MatchDaoTest              → Room in-memory DB tests
ScorecardCalculationTest  → end-to-end innings calculation
```

---

## 15. IMPLEMENTATION ORDER

```
Phase 1 — Foundation
  1. Project setup (Hilt, Room, Navigation, Version Catalog)
  2. Database schema + all entities + DAOs
  3. Domain models + enums
  4. CricketCalculator + OversHelper utilities
  5. Theme, colors, fonts, shape system

Phase 2 — Core Data Layer
  6. All repositories (interfaces + implementations)
  7. Match & innings use cases (create, get)
  8. RecordBallUseCase (MOST CRITICAL — get this right first)
  9. UndoLastBallUseCase

Phase 3 — UI Screens (in order of user flow)
  10. HomeFragment + ViewModel + RecentMatchAdapter
  11. MatchSetupFragment + ViewModel
  12. TossFragment + ViewModel + coin animation
  13. InningsSetupFragment + ViewModel
  14. ScoringFragment + ScoringViewModel (most complex)
  15. DismissalBottomSheet + ViewModel
  16. OverCompleteBottomSheet + ViewModel
  17. ScorecardFragment + ViewModel
  18. ResultFragment + ViewModel

Phase 4 — Polish
  19. All animations
  20. Edge case handling
  21. Navigation transitions
  22. In-progress match resume from home screen
```

---

## 16. FINAL NOTES FOR AGENT

- **Never use mock data in production code.** All dummy data only in `@Preview` composables or debug `@Module`.
- **Every suspend function** in use cases must be wrapped in try-catch with proper error propagation.
- **StateFlow** — never use LiveData. Use `collectIn(viewLifecycleOwner)` pattern.
- **No hardcoded strings** in Kotlin files — all go through `strings.xml`.
- **All IDs** must use `@+id/` prefix and match the `snake_case` naming from this spec.
- **Don't forget** the `android:exported="false"` flag on all non-launcher Activities/Services.
- **ProGuard rules** must keep all Room entities and Hilt components.
- **The scoring screen** must never crash. Surround every ball recording with a full try-catch. On error, show a Snackbar with "Something went wrong — please undo and retry."
- **Portrait mode only** — add `android:screenOrientation="portrait"` to MainActivity in manifest.
- If a drawable asset is not available as a vector, use a **MaterialIcon equivalent** and note it with `<!-- TODO: Replace with custom drawable -->` comment.

---

*Prompt version: 1.0 | App: CricScore | Architecture: MVVM + Room + Hilt | Target: Android 7.0+*