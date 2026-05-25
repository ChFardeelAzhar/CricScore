package com.cricscore.app.domain.model

data class PlayingElevenPlayer(
    val id: Long = 0,
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
