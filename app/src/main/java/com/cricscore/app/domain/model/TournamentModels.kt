package com.cricscore.app.domain.model

enum class TournamentStatus {
    UPCOMING,
    ONGOING,
    COMPLETED
}

data class Tournament(
    val id: Long = 0,
    val name: String,
    val venue: String = "",
    val totalTeams: Int,
    val oversPerMatch: Int,
    val playersPerSide: Int,
    val format: String = "ROUND_ROBIN",
    val status: TournamentStatus = TournamentStatus.UPCOMING,
    val winnerId: Long = -1L,
    val pointsForWin: Int = 2,
    val pointsForTie: Int = 1,
    val pointsForLoss: Int = 0,
    val pointsForNoResult: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long = 0L,
    val completedAt: Long = 0L
)

data class TournamentTeam(
    val id: Long = 0,
    val tournamentId: Long,
    val teamName: String,
    val colorHex: String = "#4A90D9",
    val logoEmoji: String = "🏏",
    val matchesPlayed: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val tied: Int = 0,
    val noResult: Int = 0,
    val points: Int = 0,
    val runsFor: Int = 0,
    val runsAgainst: Int = 0,
    val ballsFaced: Int = 0,
    val ballsBowled: Int = 0,
    val netRunRate: Double = 0.0
)

data class Fixture(
    val id: Long = 0,
    val tournamentId: Long,
    val matchNumber: Int,
    val roundNumber: Int,
    val team1Id: Long,
    val team2Id: Long,
    val team1Name: String,
    val team2Name: String,
    val status: String = "SCHEDULED",
    val linkedMatchId: Long = -1L,
    val winnerId: Long = -1L,
    val loserId: Long = -1L,
    val isTied: Boolean = false,
    val team1Score: String = "",
    val team2Score: String = "",
    val team1Overs: String = "",
    val team2Overs: String = "",
    val resultSummary: String = "",
    val scheduledDate: Long = 0L,
    val playedAt: Long = 0L
)

data class TournamentPlayerStat(
    val id: Long = 0,
    val tournamentId: Long,
    val teamId: Long,
    val teamName: String,
    val playerName: String,
    val matchesPlayed: Int = 0,
    val innings: Int = 0,
    val totalRuns: Int = 0,
    val totalBalls: Int = 0,
    val totalFours: Int = 0,
    val totalSixes: Int = 0,
    val highestScore: Int = 0,
    val highestScoreNotOut: Boolean = false,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val timesOut: Int = 0,
    val notOutCount: Int = 0,
    val oversBowled: Int = 0,
    val ballsBowled: Int = 0,
    val runsConceded: Int = 0,
    val wicketsTaken: Int = 0,
    val maidens: Int = 0,
    val bestBowlingWickets: Int = 0,
    val bestBowlingRuns: Int = 0,
    val fiveWicketHauls: Int = 0,
    val catches: Int = 0,
    val runOuts: Int = 0,
    val stumpings: Int = 0
)
