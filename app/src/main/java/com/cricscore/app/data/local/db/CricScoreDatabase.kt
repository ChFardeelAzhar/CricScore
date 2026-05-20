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
        BowlerInningsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CricScoreDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun inningsDao(): InningsDao
    abstract fun ballDao(): BallDao
    abstract fun batsmanInningsDao(): BatsmanInningsDao
    abstract fun bowlerInningsDao(): BowlerInningsDao
}
