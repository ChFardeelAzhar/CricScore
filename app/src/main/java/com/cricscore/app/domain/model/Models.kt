package com.cricscore.app.domain.model

enum class MatchStatus {
    UPCOMING,
    FIRST_INNINGS,
    SECOND_INNINGS,
    COMPLETED
}

enum class TossResult {
    BAT,
    BOWL
}

enum class BallType {
    NORMAL,
    WIDE,
    NO_BALL,
    BYE,
    LEG_BYE
}

enum class DismissalType {
    BOWLED,
    CAUGHT,
    LBW,
    RUN_OUT,
    STUMPED,
    HIT_WICKET,
    RETIRED_HURT
}

data class Match(
    val id: Long = 0,
    val team1: String,
    val team2: String,
    val oversLimit: Int,
    val playersPerSide: Int,
    val tossWinner: String? = null,
    val tossDecision: TossResult? = null,
    val status: MatchStatus = MatchStatus.UPCOMING,
    val winnerTeam: String? = null,
    val winMarginRuns: Int = 0,
    val winMarginWickets: Int = 0,
    val isTied: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val playerOfMatch: String? = null
)

data class Innings(
    val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int, // 1 or 2
    val battingTeam: String,
    val bowlingTeam: String,
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val ballsBowled: Int = 0,
    val extrasWide: Int = 0,
    val extrasNoBall: Int = 0,
    val extrasBye: Int = 0,
    val extrasLegBye: Int = 0,
    val isCompleted: Boolean = false
)

data class Ball(
    val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val overNumber: Int,       // 0-based index of the over
    val ballNumber: Int,       // 1-based legal ball count in over, or delivery index
    val strikerName: String,
    val nonStrikerName: String,
    val bowlerName: String,
    val runsBatsman: Int,
    val runsExtra: Int,
    val ballType: BallType,
    val isWicket: Boolean = false,
    val dismissalType: DismissalType? = null,
    val fielderName: String? = null,
    val dismissedPlayerName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class BatsmanInnings(
    val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val playerName: String,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissalDescription: String? = null
)

data class BowlerInnings(
    val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val playerName: String,
    val ballsBowled: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val maidens: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0
)
