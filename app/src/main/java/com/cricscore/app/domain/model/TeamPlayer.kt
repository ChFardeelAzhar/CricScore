package com.cricscore.app.domain.model

data class TeamPlayer(
    val id: Long = 0,
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
