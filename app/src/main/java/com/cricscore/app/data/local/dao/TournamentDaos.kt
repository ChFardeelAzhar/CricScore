package com.cricscore.app.data.local.dao

import androidx.room.*
import com.cricscore.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    @Insert suspend fun insert(t: TournamentEntity): Long
    @Update suspend fun update(t: TournamentEntity)
    @Delete suspend fun delete(t: TournamentEntity)

    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getById(id: Long): TournamentEntity?

    @Query("SELECT * FROM tournaments WHERE status = 'ONGOING'")
    fun getOngoingTournaments(): Flow<List<TournamentEntity>>
}

@Dao
interface TournamentTeamDao {
    @Insert suspend fun insert(team: TournamentTeamEntity): Long
    @Insert suspend fun insertAll(teams: List<TournamentTeamEntity>)
    @Update suspend fun update(team: TournamentTeamEntity)
    @Update suspend fun updateAll(teams: List<TournamentTeamEntity>)

    @Query("SELECT * FROM tournament_teams WHERE tournamentId = :tId ORDER BY points DESC, netRunRate DESC")
    fun getTeamsByTournament(tId: Long): Flow<List<TournamentTeamEntity>>

    @Query("SELECT * FROM tournament_teams WHERE tournamentId = :tId ORDER BY points DESC, netRunRate DESC")
    suspend fun getTeamsByTournamentSync(tId: Long): List<TournamentTeamEntity>

    @Query("SELECT * FROM tournament_teams WHERE id = :id")
    suspend fun getById(id: Long): TournamentTeamEntity?
}

@Dao
interface FixtureDao {
    @Insert suspend fun insert(f: FixtureEntity): Long
    @Insert suspend fun insertAll(fixtures: List<FixtureEntity>)
    @Update suspend fun update(f: FixtureEntity)

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tId ORDER BY matchNumber ASC")
    fun getFixturesByTournament(tId: Long): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tId ORDER BY matchNumber ASC")
    suspend fun getFixturesByTournamentSync(tId: Long): List<FixtureEntity>

    @Query("SELECT * FROM fixtures WHERE id = :id")
    suspend fun getById(id: Long): FixtureEntity?

    @Query("SELECT * FROM fixtures WHERE linkedMatchId = :matchId LIMIT 1")
    suspend fun getByLinkedMatchId(matchId: Long): FixtureEntity?

    @Query("SELECT * FROM fixtures WHERE tournamentId = :tId AND status = 'SCHEDULED' ORDER BY matchNumber ASC LIMIT 1")
    suspend fun getNextScheduledFixture(tId: Long): FixtureEntity?

    @Query("SELECT COUNT(*) FROM fixtures WHERE tournamentId = :tId AND status != 'COMPLETED' AND status != 'NO_RESULT'")
    suspend fun getPendingFixtureCount(tId: Long): Int
}

@Dao
interface TournamentPlayerStatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(stat: TournamentPlayerStatEntity): Long
    @Update suspend fun update(stat: TournamentPlayerStatEntity)

    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId ORDER BY totalRuns DESC")
    fun getStatsByTournament(tId: Long): Flow<List<TournamentPlayerStatEntity>>

    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId AND playerName = :name AND teamId = :teamId LIMIT 1")
    suspend fun getPlayerStat(tId: Long, name: String, teamId: Long): TournamentPlayerStatEntity?

    // Top performers queries
    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId ORDER BY totalRuns DESC LIMIT :limit")
    suspend fun getTopRunScorers(tId: Long, limit: Int = 10): List<TournamentPlayerStatEntity>

    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId ORDER BY wicketsTaken DESC, runsConceded ASC LIMIT :limit")
    suspend fun getTopWicketTakers(tId: Long, limit: Int = 10): List<TournamentPlayerStatEntity>

    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId ORDER BY totalSixes DESC LIMIT :limit")
    suspend fun getTopSixHitters(tId: Long, limit: Int = 10): List<TournamentPlayerStatEntity>

    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId ORDER BY totalFours DESC LIMIT :limit")
    suspend fun getTopFourHitters(tId: Long, limit: Int = 10): List<TournamentPlayerStatEntity>

    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId ORDER BY highestScore DESC LIMIT :limit")
    suspend fun getTopHighestScores(tId: Long, limit: Int = 10): List<TournamentPlayerStatEntity>

    @Query("SELECT * FROM tournament_player_stats WHERE tournamentId = :tId ORDER BY (CAST(wicketsTaken AS REAL) / MAX(ballsBowled,1)) DESC LIMIT :limit")
    suspend fun getBestStrikeRateBowlers(tId: Long, limit: Int = 10): List<TournamentPlayerStatEntity>
}
