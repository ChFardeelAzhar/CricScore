package com.cricscore.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cricscore.app.data.local.dao.*
import com.cricscore.app.data.local.db.CricScoreDatabase
import com.cricscore.app.data.repository.InningsRepositoryImpl
import com.cricscore.app.data.repository.MatchRepositoryImpl
import com.cricscore.app.data.repository.TournamentRepositoryImpl
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import com.cricscore.app.domain.repository.TournamentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `tournaments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `venue` TEXT NOT NULL, `totalTeams` INTEGER NOT NULL, `oversPerMatch` INTEGER NOT NULL, `playersPerSide` INTEGER NOT NULL, `format` TEXT NOT NULL, `status` TEXT NOT NULL, `winnerId` INTEGER NOT NULL, `pointsForWin` INTEGER NOT NULL, `pointsForTie` INTEGER NOT NULL, `pointsForLoss` INTEGER NOT NULL, `pointsForNoResult` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `completedAt` INTEGER NOT NULL)")
            
            database.execSQL("CREATE TABLE IF NOT EXISTS `tournament_teams` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tournamentId` INTEGER NOT NULL, `teamName` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `logoEmoji` TEXT NOT NULL, `matchesPlayed` INTEGER NOT NULL, `won` INTEGER NOT NULL, `lost` INTEGER NOT NULL, `tied` INTEGER NOT NULL, `noResult` INTEGER NOT NULL, `points` INTEGER NOT NULL, `runsFor` INTEGER NOT NULL, `runsAgainst` INTEGER NOT NULL, `ballsFaced` INTEGER NOT NULL, `ballsBowled` INTEGER NOT NULL, `netRunRate` REAL NOT NULL, FOREIGN KEY(`tournamentId`) REFERENCES `tournaments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_teams_tournamentId` ON `tournament_teams` (`tournamentId`)")
            
            database.execSQL("CREATE TABLE IF NOT EXISTS `fixtures` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tournamentId` INTEGER NOT NULL, `matchNumber` INTEGER NOT NULL, `roundNumber` INTEGER NOT NULL, `team1Id` INTEGER NOT NULL, `team2Id` INTEGER NOT NULL, `team1Name` TEXT NOT NULL, `team2Name` TEXT NOT NULL, `status` TEXT NOT NULL, `linkedMatchId` INTEGER NOT NULL, `winnerId` INTEGER NOT NULL, `loserId` INTEGER NOT NULL, `isTied` INTEGER NOT NULL, `team1Score` TEXT NOT NULL, `team2Score` TEXT NOT NULL, `team1Overs` TEXT NOT NULL, `team2Overs` TEXT NOT NULL, `resultSummary` TEXT NOT NULL, `scheduledDate` INTEGER NOT NULL, `playedAt` INTEGER NOT NULL, FOREIGN KEY(`tournamentId`) REFERENCES `tournaments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`team1Id`) REFERENCES `tournament_teams`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`team2Id`) REFERENCES `tournament_teams`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fixtures_tournamentId` ON `fixtures` (`tournamentId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fixtures_team1Id` ON `fixtures` (`team1Id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fixtures_team2Id` ON `fixtures` (`team2Id`)")
            
            database.execSQL("CREATE TABLE IF NOT EXISTS `tournament_player_stats` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tournamentId` INTEGER NOT NULL, `teamId` INTEGER NOT NULL, `teamName` TEXT NOT NULL, `playerName` TEXT NOT NULL, `matchesPlayed` INTEGER NOT NULL, `innings` INTEGER NOT NULL, `totalRuns` INTEGER NOT NULL, `totalBalls` INTEGER NOT NULL, `totalFours` INTEGER NOT NULL, `totalSixes` INTEGER NOT NULL, `highestScore` INTEGER NOT NULL, `highestScoreNotOut` INTEGER NOT NULL, `fifties` INTEGER NOT NULL, `hundreds` INTEGER NOT NULL, `timesOut` INTEGER NOT NULL, `notOutCount` INTEGER NOT NULL, `oversBowled` INTEGER NOT NULL, `ballsBowled` INTEGER NOT NULL, `runsConceded` INTEGER NOT NULL, `wicketsTaken` INTEGER NOT NULL, `maidens` INTEGER NOT NULL, `bestBowlingWickets` INTEGER NOT NULL, `bestBowlingRuns` INTEGER NOT NULL, `fiveWicketHauls` INTEGER NOT NULL, `catches` INTEGER NOT NULL, `runOuts` INTEGER NOT NULL, `stumpings` INTEGER NOT NULL, FOREIGN KEY(`tournamentId`) REFERENCES `tournaments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`teamId`) REFERENCES `tournament_teams`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_player_stats_tournamentId` ON `tournament_player_stats` (`tournamentId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_player_stats_teamId` ON `tournament_player_stats` (`teamId`)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tournament_player_stats_tournamentId_playerName_teamId` ON `tournament_player_stats` (`tournamentId`, `playerName`, `teamId`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CricScoreDatabase {
        return Room.databaseBuilder(
            context,
            CricScoreDatabase::class.java,
            "cricscore_db"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMatchDao(db: CricScoreDatabase): MatchDao = db.matchDao()

    @Provides
    fun provideInningsDao(db: CricScoreDatabase): InningsDao = db.inningsDao()

    @Provides
    fun provideBallDao(db: CricScoreDatabase): BallDao = db.ballDao()

    @Provides
    fun provideBatsmanInningsDao(db: CricScoreDatabase): BatsmanInningsDao = db.batsmanInningsDao()

    @Provides
    fun provideBowlerInningsDao(db: CricScoreDatabase): BowlerInningsDao = db.bowlerInningsDao()

    @Provides
    fun provideTournamentDao(db: CricScoreDatabase): TournamentDao = db.tournamentDao()

    @Provides
    fun provideTournamentTeamDao(db: CricScoreDatabase): TournamentTeamDao = db.tournamentTeamDao()

    @Provides
    fun provideFixtureDao(db: CricScoreDatabase): FixtureDao = db.fixtureDao()

    @Provides
    fun provideTournamentPlayerStatDao(db: CricScoreDatabase): TournamentPlayerStatDao = db.tournamentPlayerStatDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository

    @Binds
    @Singleton
    abstract fun bindInningsRepository(impl: InningsRepositoryImpl): InningsRepository

    @Binds
    @Singleton
    abstract fun bindTournamentRepository(impl: TournamentRepositoryImpl): TournamentRepository
}
