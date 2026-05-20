package com.cricscore.app.di

import android.content.Context
import androidx.room.Room
import com.cricscore.app.data.local.dao.*
import com.cricscore.app.data.local.db.CricScoreDatabase
import com.cricscore.app.data.repository.InningsRepositoryImpl
import com.cricscore.app.data.repository.MatchRepositoryImpl
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CricScoreDatabase {
        return Room.databaseBuilder(
            context,
            CricScoreDatabase::class.java,
            "cricscore_db"
        ).fallbackToDestructiveMigration().build()
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
}
