package com.cricscore.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cricscore.app.data.local.dao.*
import com.cricscore.app.data.local.entity.*

@Database(
    entities = [
        MatchEntity::class,
        InningsEntity::class,
        BallEntity::class,
        BatsmanInningsEntity::class,
        BowlerInningsEntity::class,
        TournamentEntity::class,
        TournamentTeamEntity::class,
        FixtureEntity::class,
        TournamentPlayerStatEntity::class,
        TeamPlayerEntity::class,
        PlayingElevenEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CricScoreDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun inningsDao(): InningsDao
    abstract fun ballDao(): BallDao
    abstract fun batsmanInningsDao(): BatsmanInningsDao
    abstract fun bowlerInningsDao(): BowlerInningsDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun tournamentTeamDao(): TournamentTeamDao
    abstract fun fixtureDao(): FixtureDao
    abstract fun tournamentPlayerStatDao(): TournamentPlayerStatDao
    abstract fun teamPlayerDao(): TeamPlayerDao
    abstract fun playingElevenDao(): PlayingElevenDao
}
