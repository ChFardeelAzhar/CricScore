package com.cricscore.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cricscore.app.domain.model.*

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val team1: String,
    val team2: String,
    val oversLimit: Int,
    val playersPerSide: Int,
    val tossWinner: String?,
    val tossDecision: String?, // TossResult name
    val status: String, // MatchStatus name
    val winnerTeam: String?,
    val winMarginRuns: Int,
    val winMarginWickets: Int,
    val isTied: Boolean,
    val tournamentId: Long?,
    val createdAt: Long,
    val playerOfMatch: String?
)

@Entity(tableName = "innings")
data class InningsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val battingTeam: String,
    val bowlingTeam: String,
    val totalRuns: Int,
    val totalWickets: Int,
    val ballsBowled: Int,
    val extrasWide: Int,
    val extrasNoBall: Int,
    val extrasBye: Int,
    val extrasLegBye: Int,
    val isCompleted: Boolean
)

@Entity(tableName = "balls")
data class BallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val overNumber: Int,
    val ballNumber: Int,
    val strikerName: String,
    val nonStrikerName: String,
    val bowlerName: String,
    val runsBatsman: Int,
    val runsExtra: Int,
    val ballType: String, // BallType name
    val isWicket: Boolean,
    val dismissalType: String?, // DismissalType name
    val fielderName: String?,
    val dismissedPlayerName: String?,
    val timestamp: Long
)

@Entity(tableName = "batsman_innings")
data class BatsmanInningsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val playerName: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val isOut: Boolean,
    val dismissalDescription: String?
)

@Entity(tableName = "bowler_innings")
data class BowlerInningsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val playerName: String,
    val ballsBowled: Int,
    val runsConceded: Int,
    val wickets: Int,
    val maidens: Int,
    val wides: Int,
    val noBalls: Int
)

// Mappers

fun MatchEntity.toDomain() = Match(
    id = id,
    team1 = team1,
    team2 = team2,
    oversLimit = oversLimit,
    playersPerSide = playersPerSide,
    tossWinner = tossWinner,
    tossDecision = tossDecision?.let { TossResult.valueOf(it) },
    status = MatchStatus.valueOf(status),
    winnerTeam = winnerTeam,
    winMarginRuns = winMarginRuns,
    winMarginWickets = winMarginWickets,
    isTied = isTied,
    tournamentId = tournamentId,
    createdAt = createdAt,
    playerOfMatch = playerOfMatch
)

fun Match.toEntity() = MatchEntity(
    id = id,
    team1 = team1,
    team2 = team2,
    oversLimit = oversLimit,
    playersPerSide = playersPerSide,
    tossWinner = tossWinner,
    tossDecision = tossDecision?.name,
    status = status.name,
    winnerTeam = winnerTeam,
    winMarginRuns = winMarginRuns,
    winMarginWickets = winMarginWickets,
    isTied = isTied,
    tournamentId = tournamentId,
    createdAt = createdAt,
    playerOfMatch = playerOfMatch
)

fun InningsEntity.toDomain() = Innings(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    battingTeam = battingTeam,
    bowlingTeam = bowlingTeam,
    totalRuns = totalRuns,
    totalWickets = totalWickets,
    ballsBowled = ballsBowled,
    extrasWide = extrasWide,
    extrasNoBall = extrasNoBall,
    extrasBye = extrasBye,
    extrasLegBye = extrasLegBye,
    isCompleted = isCompleted
)

fun Innings.toEntity() = InningsEntity(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    battingTeam = battingTeam,
    bowlingTeam = bowlingTeam,
    totalRuns = totalRuns,
    totalWickets = totalWickets,
    ballsBowled = ballsBowled,
    extrasWide = extrasWide,
    extrasNoBall = extrasNoBall,
    extrasBye = extrasBye,
    extrasLegBye = extrasLegBye,
    isCompleted = isCompleted
)

fun BallEntity.toDomain() = Ball(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    overNumber = overNumber,
    ballNumber = ballNumber,
    strikerName = strikerName,
    nonStrikerName = nonStrikerName,
    bowlerName = bowlerName,
    runsBatsman = runsBatsman,
    runsExtra = runsExtra,
    ballType = BallType.valueOf(ballType),
    isWicket = isWicket,
    dismissalType = dismissalType?.let { DismissalType.valueOf(it) },
    fielderName = fielderName,
    dismissedPlayerName = dismissedPlayerName,
    timestamp = timestamp
)

fun Ball.toEntity() = BallEntity(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    overNumber = overNumber,
    ballNumber = ballNumber,
    strikerName = strikerName,
    nonStrikerName = nonStrikerName,
    bowlerName = bowlerName,
    runsBatsman = runsBatsman,
    runsExtra = runsExtra,
    ballType = ballType.name,
    isWicket = isWicket,
    dismissalType = dismissalType?.name,
    fielderName = fielderName,
    dismissedPlayerName = dismissedPlayerName,
    timestamp = timestamp
)

fun BatsmanInningsEntity.toDomain() = BatsmanInnings(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    playerName = playerName,
    runs = runs,
    balls = balls,
    fours = fours,
    sixes = sixes,
    isOut = isOut,
    dismissalDescription = dismissalDescription
)

fun BatsmanInnings.toEntity() = BatsmanInningsEntity(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    playerName = playerName,
    runs = runs,
    balls = balls,
    fours = fours,
    sixes = sixes,
    isOut = isOut,
    dismissalDescription = dismissalDescription
)

fun BowlerInningsEntity.toDomain() = BowlerInnings(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    playerName = playerName,
    ballsBowled = ballsBowled,
    runsConceded = runsConceded,
    wickets = wickets,
    maidens = maidens,
    wides = wides,
    noBalls = noBalls
)

fun BowlerInnings.toEntity() = BowlerInningsEntity(
    id = id,
    matchId = matchId,
    inningsNumber = inningsNumber,
    playerName = playerName,
    ballsBowled = ballsBowled,
    runsConceded = runsConceded,
    wickets = wickets,
    maidens = maidens,
    wides = wides,
    noBalls = noBalls
)
