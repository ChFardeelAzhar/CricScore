package com.cricscore.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playing_eleven",
    foreignKeys = [
        ForeignKey(
            entity = FixtureEntity::class,
            parentColumns = ["id"],
            childColumns = ["fixtureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TournamentTeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamPlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fixtureId"), Index("teamId"), Index("playerId")]
)
data class PlayingElevenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fixtureId: Long,
    val teamId: Long,
    val playerId: Long,
    val playerName: String,
    val battingOrder: Int = 0,
    val isCaptain: Boolean = false,
    val isWicketKeeper: Boolean = false,
    val hasAlreadyBatted: Boolean = false,
    val isCurrentlyBatting: Boolean = false,
    val isOut: Boolean = false,
    val hasBowled: Boolean = false
)

fun PlayingElevenEntity.toDomain() = com.cricscore.app.domain.model.PlayingElevenPlayer(
    id = id,
    fixtureId = fixtureId,
    teamId = teamId,
    playerId = playerId,
    playerName = playerName,
    battingOrder = battingOrder,
    isCaptain = isCaptain,
    isWicketKeeper = isWicketKeeper,
    hasAlreadyBatted = hasAlreadyBatted,
    isCurrentlyBatting = isCurrentlyBatting,
    isOut = isOut,
    hasBowled = hasBowled
)

fun com.cricscore.app.domain.model.PlayingElevenPlayer.toEntity() = PlayingElevenEntity(
    id = id,
    fixtureId = fixtureId,
    teamId = teamId,
    playerId = playerId,
    playerName = playerName,
    battingOrder = battingOrder,
    isCaptain = isCaptain,
    isWicketKeeper = isWicketKeeper,
    hasAlreadyBatted = hasAlreadyBatted,
    isCurrentlyBatting = isCurrentlyBatting,
    isOut = isOut,
    hasBowled = hasBowled
)
