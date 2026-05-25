package com.cricscore.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "team_players",
    foreignKeys = [
        ForeignKey(
            entity = TournamentTeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("teamId"),
        Index(value = ["teamId", "playerName"], unique = true)
    ]
)
data class TeamPlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val tournamentId: Long,
    val playerName: String,
    val jerseyNumber: Int = 0,
    val role: String = "ALL_ROUNDER",
    val isCaptain: Boolean = false,
    val isViceCaptain: Boolean = false,
    val isWicketKeeper: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

fun TeamPlayerEntity.toDomain() = com.cricscore.app.domain.model.TeamPlayer(
    id = id,
    teamId = teamId,
    tournamentId = tournamentId,
    playerName = playerName,
    jerseyNumber = jerseyNumber,
    role = role,
    isCaptain = isCaptain,
    isViceCaptain = isViceCaptain,
    isWicketKeeper = isWicketKeeper,
    addedAt = addedAt
)

fun com.cricscore.app.domain.model.TeamPlayer.toEntity() = TeamPlayerEntity(
    id = id,
    teamId = teamId,
    tournamentId = tournamentId,
    playerName = playerName,
    jerseyNumber = jerseyNumber,
    role = role,
    isCaptain = isCaptain,
    isViceCaptain = isViceCaptain,
    isWicketKeeper = isWicketKeeper,
    addedAt = addedAt
)
