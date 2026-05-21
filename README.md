# CricScore — Offline Cricket Scoring App

Assalam-o-Alaikum! CricScore app ke flow, features, aur directory structure ko behtar tareeqe se samajhne ke liye yeh README guide aapki madad karegi. Is guide mein technical architecture se lekar UI flows aur key logic classes ki locations tak har cheez detailed tariqe se likhi gayi hai.

---

## 🛠 Tech Stack & Core Libraries

- **Language:** Kotlin
- **Minimum SDK:** 24 | **Target/Compile SDK:** 34
- **Architecture:** MVVM + Repository Pattern + Clean Domain Use Cases
- **Local Database (Room 2.8.4):** Completely offline storage for matches, innings, batsman/bowler statistics, and ball-by-ball events.
- **Dependency Injection:** Hilt
- **UI Architecture:** ViewBinding with Jetpack Navigation Component (Single Activity architecture).
- **Asynchronous Operations:** Kotlin Coroutines & reactive `StateFlow` for UI state updates.

---

## 📁 Codebase Directory Structure (Where is what?)

Aapko project ke andar code yahan milega:
`app/src/main/java/com/cricscore/app/`

```
├── core/
│   ├── base/                  # Base classes for Fragments (BaseFragment)
│   ├── extensions/            # Kotlin extension functions (setSafeOnClickListener, etc.)
│   └── util/
│       ├── CricketCalculator.kt # Run Rate, Strike Rate, Economy Rate formulas
│       └── OversHelper.kt     # Legal balls, over completion, and strike rotation rules
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── Daos.kt        # Room DAOs (MatchDao, InningsDao, BallDao, etc.)
│   │   ├── database/
│   │   │   └── CricScoreDatabase.kt # Main Room database setup
│   │   └── entity/
│   │       └── Entities.kt    # Room database entities (MatchEntity, BallEntity, etc.)
│   └── repository/
│       ├── InningsRepositoryImpl.kt # InningsRepository interface implementation
│       └── MatchRepositoryImpl.kt   # MatchRepository interface implementation
│
├── di/
│   └── DatabaseModule.kt      # Hilt modules to inject database and repositories
│
├── domain/
│   ├── model/
│   │   └── Models.kt          # Domain data models & enums (Match, Ball, BallType, DismissalType)
│   ├── repository/
│   │   ├── InningsRepository.kt # Repository interface definition
│   │   └── MatchRepository.kt   # Repository interface definition
│   └── usecase/
│       ├── RecordBallUseCase.kt # Core business logic to record a ball, update stats, rotate strike
│       └── UndoLastBallUseCase.kt # Rolling back the last recorded ball and restoring stats
│
└── ui/                        # UI Fragments, ViewModels, and Adapters
    ├── home/                  # Home screen (list of matches)
    ├── setup/                 # Match details input form
    ├── toss/                  # Toss decision screen (3D-like flip coin animation)
    ├── inningssetup/          # Choosing openers & bowlers to start an innings
    ├── scoring/               # Interactive score scoring dashboard
    │   └── bottomsheet/       # Wicket details & Over Complete dialogs
    ├── scorecard/             # Tabbed detailed scorecard (Batsmen, Bowlers, Fall of Wickets)
    └── result/                # Match summary screen with Player of the Match
```

---

## 🔄 Step-by-Step App Flow

CricScore app ka scoring flow bilkul aasan aur realistic cricket matches ki tarah design kiya gaya hai:

### 1. Home Screen (`HomeFragment`)
- **Visuals:** Ek premium navy-blue styled theme display hota hai.
- **Flow:** 
  - Past matches aur active matches ki list check kar sakte hain.
  - "Start New Match" button par click kar ke setup flow trigger kar sakte hain.

### 2. Match Setup (`MatchSetupFragment`)
- **Flow:** 
  - Team A aur Team B ke names enter karein.
  - Overs Limit (e.g., 5, 20, 50 overs) specify karein.
  - Number of players per side (minimum 2, default 11) set karein.
  - Form validation ke baad user **Toss Screen** par navigate hota hai.

### 3. Toss Screen (`TossFragment`)
- **Flow:** 
  - User can tap "Flip Coin". Ek animated, realistic coin 3D spin rotation aur vertical bouncing translation (jump effect) ke sath coin throw simulate karta hai.
  - Randomly heads/tails result choose hota hai aur winner select ho jata hai.
  - Toss winner team chose karti hai: "Bat" or "Bowl".

### 4. Innings Setup (`InningsSetupFragment`)
- **Flow:**
  - **First Innings Start:** Batting side ke do opening batsmen choose kiye jate hain (Striker aur Non-Striker).
  - Bowling side ka opening bowler select kiya jata hai.

### 5. Scoring Dashboard (`ScoringFragment`)
In-play scoring screen cricket scoring ke tamam complex features provide karti hai:
- **Scoring Buttons:** Add runs (0, 1, 2, 3, 4, 6) or custom runs.
- **Extras Support:** Wide, No-Ball, Bye, Leg-Bye.
- **Wicket Handler:** "Out" button trigger karne par, `DismissalBottomSheet` open hota hai jahan user choose kar sakta hai:
  - Wicket Type (Bowled, Caught, LBW, Run Out, Stumped, Hit Wicket, Retired Hurt).
  - Fielder name and dismissed batsman name.
- **Strike Rotation:** Rules-based automatic strike rotation (e.g., odd runs complete karne par ya over completed hone par automatically strikers switch ho jate hain).
- **Over Transition:** Jab 6 legal balls bowl ho jati hain, automatic `OverCompleteBottomSheet` popup open hota hai jisme next bowler select kiya jata hai aur strike end-of-over rule ke mutabiq rotate hoti hai.
- **Undo Button:** Kisi bhi scoring mistake ko correct karne ke liye "Undo" button setup hai, jo state reset kar ke dynamic calculation rollback perform karta hai.

### 6. Scorecard View (`ScorecardFragment`)
- Scoring screen par rehte hue ya match end hone par "View Scorecard" par click kar sakte hain.
- **Tabs:** 
  - **Batting Tab:** Har batsman ke exact Runs, Balls faced, Fours, Sixes, aur out state (dismissal detail) display hoti hain.
  - **Bowling Tab:** Bowler ke Overs bowled, Maidens, Runs conceded, Wickets taken, aur economy rate metrics visual show hote hain.
  - **Extras & Fall of Wickets:** Clear match event timeline.

### 7. Match Result (`ResultFragment`)
- Innings 2 end hone par app automatically result page par chala jata hai.
- Kis team ne match kitne runs ya wickets se win kiya woh details highlight hoti hain.
- Match stats analyze kar ke **Player of the Match** determine kiya jata hai.

---

## ⚡ Core Business Logic (Domain & Helpers)

CricScore ke logical engines bilkul clear rules implement karte hain:

1. **`OversHelper.kt`**
   - `isLegalBall(ballType: BallType)`: Returns `true` for normal, bye, and leg-bye. Returns `false` for wide and no-ball (extra balls to be bowled).
   - `shouldSwitchStrike(runsBatsman: Int, runsExtra: Int, ballType: BallType)`: Determines if batsman runs rotate the strike (e.g., single/three runs, or standard wide singles).

2. **`CricketCalculator.kt`**
   - Calculate Strike Rate: `(runs / balls) * 100` (rounded to 1 decimal place).
   - Calculate Economy Rate: `(runsConceded / ballsBowled) * 6` (rounded to 2 decimal places).
   - Convert balls to overs string representation (e.g., `16` balls = `"2.4"` overs).

3. **`RecordBallUseCase.kt`**
   - Room repository se active match setup pull karta hai.
   - Ball info (runs, wickets, extras) database mein write karta hai.
   - Strike state automatically rotate karta hai (including over completion swaps).
   - Check details: Check karta hai ke total overs over limit tak to nahi pohanche, wickets down hain, ya target second innings mein clear ho chuka hai to innings complete marks karta hai.

4. **`UndoLastBallUseCase.kt`**
   - Database se last delivery fetch kar ke safely delete karta hai.
   - Tabdeel shuda/remaining balls ki help se batsman aur bowler statistics ko clear rollback (re-evaluation) kar ke recalculate karta hai aur saved state restore karta hai.

---

## 💾 Local Database Schema

Room local database complete entities structure follow karta hai:
- **`MatchEntity`:** Match configuration records saving (`team1`, `team2`, `oversLimit`, `winnerTeam`, margin details, status, etc.).
- **`InningsEntity`:** Runs, wickets, balls bowled, and extras breakdown (`extrasWide`, `extrasNoBall`, `extrasBye`, `extrasLegBye`) status tracking.
- **`BallEntity`:** History list keeping track of who bowled/faced which ball, run details, type, and dismissals.
- **`BatsmanInningsEntity`:** Player runs, balls, fours, sixes, and out statuses.
- **`BowlerInningsEntity`:** Bowler analysis records (balls, runs, wickets, maidens, wides, no-balls).

---

## 🚀 How to Run & Verify

Project ke testing and packaging verification ke liye ye commands compile aur execute karein:

```bash
# Clean the project build directories
./gradlew clean

# Run all unit tests (CricketCalculatorTest, OversHelperTest, UseCases)
./gradlew :app:testDebugUnitTest

# Assemble debug app package (APK)
./gradlew :app:assembleDebug
```
All compilation workflows aur database mappings are stable, clean, aur unit test coverage verified hain!
