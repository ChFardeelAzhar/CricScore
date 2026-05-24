package com.cricscore.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cricscore.app.domain.model.*

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                        // "Gully Premier League 2025"
    val venue: String = "",                  // optional
    val totalTeams: Int,                     // min 2, max 16
    val oversPerMatch: Int,                  // 5/10/15/20
    val playersPerSide: Int,                 // 6-11
    val format: String = "ROUND_ROBIN",      // only ROUND_ROBIN for now
    val status: String = "UPCOMING",         // UPCOMING / ONGOING / COMPLETED
    val winnerId: Long = -1L,                // TournamentTeamEntity id of winner
    val pointsForWin: Int = 2,
    val pointsForTie: Int = 1,
    val pointsForLoss: Int = 0,
    val pointsForNoResult: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long = 0L,
    val completedAt: Long = 0L
)

@Entity(
    tableName = "tournament_teams",
    foreignKeys = [ForeignKey(
        entity = TournamentEntity::class,
        parentColumns = ["id"],
        childColumns = ["tournamentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("tournamentId")]
)
data class TournamentTeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val teamName: String,
    val colorHex: String = "#4A90D9",        // team color for UI dot indicator
    val logoEmoji: String = "🏏",            // placeholder until custom logo supported
    // Points table fields
    val matchesPlayed: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val tied: Int = 0,
    val noResult: Int = 0,
    val points: Int = 0,
    val runsFor: Int = 0,                    // total runs scored across all matches
    val runsAgainst: Int = 0,               // total runs conceded
    val ballsFaced: Int = 0,                // total legal balls faced
    val ballsBowled: Int = 0,               // total legal balls bowled
    val netRunRate: Double = 0.0             // calculated: (runsFor/ballsFaced*6) - (runsAgainst/ballsBowled*6)
)

@Entity(
    tableName = "fixtures",
    foreignKeys = [
        ForeignKey(entity = TournamentEntity::class, parentColumns = ["id"], childColumns = ["tournamentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TournamentTeamEntity::class, parentColumns = ["id"], childColumns = ["team1Id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TournamentTeamEntity::class, parentColumns = ["id"], childColumns = ["team2Id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("tournamentId"), Index("team1Id"), Index("team2Id")]
)
data class FixtureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val matchNumber: Int,                    // 1, 2, 3... display order
    val roundNumber: Int,                    // round-robin round
    val team1Id: Long,
    val team2Id: Long,
    val team1Name: String,                   // denormalized for display speed
    val team2Name: String,
    val status: String = "SCHEDULED",        // SCHEDULED / LIVE / COMPLETED / NO_RESULT
    val linkedMatchId: Long = -1L,           // links to existing MatchEntity when scoring starts
    val winnerId: Long = -1L,               // TournamentTeamEntity id
    val loserId: Long = -1L,
    val isTied: Boolean = false,
    // Result summary (denormalized for fast points table display)
    val team1Score: String = "",             // "127/8"
    val team2Score: String = "",             // "93/10"
    val team1Overs: String = "",             // "10.0"
    val team2Overs: String = "",             // "9.2"
    val resultSummary: String = "",          // "Team A won by 34 runs"
    val scheduledDate: Long = 0L,
    val playedAt: Long = 0L
)

@Entity(
    tableName = "tournament_player_stats",
    foreignKeys = [
        ForeignKey(entity = TournamentEntity::class, parentColumns = ["id"], childColumns = ["tournamentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TournamentTeamEntity::class, parentColumns = ["id"], childColumns = ["teamId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("tournamentId"),
        Index("teamId"),
        Index(value = ["tournamentId", "playerName", "teamId"], unique = true)
    ]
)
data class TournamentPlayerStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val teamId: Long,
    val teamName: String,
    val playerName: String,
    // Batting
    val matchesPlayed: Int = 0,
    val innings: Int = 0,
    val totalRuns: Int = 0,
    val totalBalls: Int = 0,
    val totalFours: Int = 0,
    val totalSixes: Int = 0,
    val highestScore: Int = 0,
    val highestScoreNotOut: Boolean = false,
    val fifties: Int = 0,                   // 50-99
    val hundreds: Int = 0,                  // 100+
    val timesOut: Int = 0,
    val notOutCount: Int = 0,
    // Bowling
    val oversBowled: Int = 0,               // complete overs
    val ballsBowled: Int = 0,               // balls in current incomplete over
    val runsConceded: Int = 0,
    val wicketsTaken: Int = 0,
    val maidens: Int = 0,
    val bestBowlingWickets: Int = 0,        // best bowling: wickets
    val bestBowlingRuns: Int = 0,           // best bowling: runs (lower is better)
    val fiveWicketHauls: Int = 0,
    // Fielding
    val catches: Int = 0,
    val runOuts: Int = 0,
    val stumpings: Int = 0
)

// Mapping functions

fun TournamentEntity.toDomain() = Tournament(
    id = id,
    name = name,
    venue = venue,
    totalTeams = totalTeams,
    oversPerMatch = oversPerMatch,
    playersPerSide = playersPerSide,
    format = format,
    status = TournamentStatus.valueOf(status),
    winnerId = winnerId,
    pointsForWin = pointsForWin,
    pointsForTie = pointsForTie,
    pointsForLoss = pointsForLoss,
    pointsForNoResult = pointsForNoResult,
    createdAt = createdAt,
    startedAt = startedAt,
    completedAt = completedAt
)

fun Tournament.toEntity() = TournamentEntity(
    id = id,
    name = name,
    venue = venue,
    totalTeams = totalTeams,
    oversPerMatch = oversPerMatch,
    playersPerSide = playersPerSide,
    format = format,
    status = status.name,
    winnerId = winnerId,
    pointsForWin = pointsForWin,
    pointsForTie = pointsForTie,
    pointsForLoss = pointsForLoss,
    pointsForNoResult = pointsForNoResult,
    createdAt = createdAt,
    startedAt = startedAt,
    completedAt = completedAt
)

fun TournamentTeamEntity.toDomain() = TournamentTeam(
    id = id,
    tournamentId = tournamentId,
    teamName = teamName,
    colorHex = colorHex,
    logoEmoji = logoEmoji,
    matchesPlayed = matchesPlayed,
    won = won,
    lost = lost,
    tied = tied,
    noResult = noResult,
    points = points,
    runsFor = runsFor,
    runsAgainst = runsAgainst,
    ballsFaced = ballsFaced,
    ballsBowled = ballsBowled,
    netRunRate = netRunRate
)

fun TournamentTeam.toEntity() = TournamentTeamEntity(
    id = id,
    tournamentId = tournamentId,
    teamName = teamName,
    colorHex = colorHex,
    logoEmoji = logoEmoji,
    matchesPlayed = matchesPlayed,
    won = won,
    lost = lost,
    tied = tied,
    noResult = noResult,
    points = points,
    runsFor = runsFor,
    runsAgainst = runsAgainst,
    ballsFaced = ballsFaced,
    ballsBowled = ballsBowled,
    netRunRate = netRunRate
)

fun FixtureEntity.toDomain() = Fixture(
    id = id,
    tournamentId = tournamentId,
    matchNumber = matchNumber,
    roundNumber = roundNumber,
    team1Id = team1Id,
    team2Id = team2Id,
    team1Name = team1Name,
    team2Name = team2Name,
    status = status,
    linkedMatchId = linkedMatchId,
    winnerId = winnerId,
    loserId = loserId,
    isTied = isTied,
    team1Score = team1Score,
    team2Score = team2Score,
    team1Overs = team1Overs,
    team2Overs = team2Overs,
    resultSummary = resultSummary,
    scheduledDate = scheduledDate,
    playedAt = playedAt
)

fun Fixture.toEntity() = FixtureEntity(
    id = id,
    tournamentId = tournamentId,
    matchNumber = matchNumber,
    roundNumber = roundNumber,
    team1Id = team1Id,
    team2Id = team2Id,
    team1Name = team1Name,
    team2Name = team2Name,
    status = status,
    linkedMatchId = linkedMatchId,
    winnerId = winnerId,
    loserId = loserId,
    isTied = isTied,
    team1Score = team1Score,
    team2Score = team2Score,
    team1Overs = team1Overs,
    team2Overs = team2Overs,
    resultSummary = resultSummary,
    scheduledDate = scheduledDate,
    playedAt = playedAt
)

fun TournamentPlayerStatEntity.toDomain() = TournamentPlayerStat(
    id = id,
    tournamentId = tournamentId,
    teamId = teamId,
    teamName = teamName,
    playerName = playerName,
    matchesPlayed = matchesPlayed,
    innings = innings,
    totalRuns = totalRuns,
    totalBalls = totalBalls,
    totalFours = totalFours,
    totalSixes = totalSixes,
    highestScore = highestScore,
    highestScoreNotOut = highestScoreNotOut,
    fifties = fifties,
    hundreds = hundreds,
    timesOut = timesOut,
    notOutCount = notOutCount,
    oversBowled = oversBowled,
    ballsBowled = ballsBowled,
    runsConceded = runsConceded,
    wicketsTaken = wicketsTaken,
    maidens = maidens,
    bestBowlingWickets = bestBowlingWickets,
    bestBowlingRuns = bestBowlingRuns,
    fiveWicketHauls = fiveWicketHauls,
    catches = catches,
    runOuts = runOuts,
    stumpings = stumpings
)

fun TournamentPlayerStat.toEntity() = TournamentPlayerStatEntity(
    id = id,
    tournamentId = tournamentId,
    teamId = teamId,
    teamName = teamName,
    playerName = playerName,
    matchesPlayed = matchesPlayed,
    innings = innings,
    totalRuns = totalRuns,
    totalBalls = totalBalls,
    totalFours = totalFours,
    totalSixes = totalSixes,
    highestScore = highestScore,
    highestScoreNotOut = highestScoreNotOut,
    fifties = fifties,
    hundreds = hundreds,
    timesOut = timesOut,
    notOutCount = notOutCount,
    oversBowled = oversBowled,
    ballsBowled = ballsBowled,
    runsConceded = runsConceded,
    wicketsTaken = wicketsTaken,
    maidens = maidens,
    bestBowlingWickets = bestBowlingWickets,
    bestBowlingRuns = bestBowlingRuns,
    fiveWicketHauls = fiveWicketHauls,
    catches = catches,
    runOuts = runOuts,
    stumpings = stumpings
)
