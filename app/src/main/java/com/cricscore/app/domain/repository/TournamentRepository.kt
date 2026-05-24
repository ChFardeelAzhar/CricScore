package com.cricscore.app.domain.repository

import com.cricscore.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TournamentRepository {
    // Tournaments
    suspend fun createTournament(t: Tournament): Long
    suspend fun updateTournament(t: Tournament)
    suspend fun deleteTournament(t: Tournament)
    fun getAllTournaments(): Flow<List<Tournament>>
    suspend fun getTournamentById(id: Long): Tournament?
    fun getOngoingTournaments(): Flow<List<Tournament>>

    // Teams
    suspend fun insertTeam(team: TournamentTeam): Long
    suspend fun insertTeams(teams: List<TournamentTeam>)
    suspend fun updateTeam(team: TournamentTeam)
    suspend fun updateTeams(teams: List<TournamentTeam>)
    fun getTeamsByTournament(tId: Long): Flow<List<TournamentTeam>>
    suspend fun getTeamsByTournamentSync(tId: Long): List<TournamentTeam>
    suspend fun getTeamById(id: Long): TournamentTeam?

    // Fixtures
    suspend fun insertFixture(f: Fixture): Long
    suspend fun insertFixtures(fixtures: List<Fixture>)
    suspend fun updateFixture(f: Fixture)
    fun getFixturesByTournament(tId: Long): Flow<List<Fixture>>
    suspend fun getFixturesByTournamentSync(tId: Long): List<Fixture>
    suspend fun getFixtureById(id: Long): Fixture?
    suspend fun getFixtureByLinkedMatchId(matchId: Long): Fixture?
    suspend fun getNextScheduledFixture(tId: Long): Fixture?
    suspend fun getPendingFixtureCount(tId: Long): Int

    // Player Stats
    suspend fun insertPlayerStat(stat: TournamentPlayerStat): Long
    suspend fun updatePlayerStat(stat: TournamentPlayerStat)
    fun getStatsByTournament(tId: Long): Flow<List<TournamentPlayerStat>>
    suspend fun getPlayerStat(tId: Long, name: String, teamId: Long): TournamentPlayerStat?
    
    // Top Performers
    suspend fun getTopRunScorers(tId: Long, limit: Int = 10): List<TournamentPlayerStat>
    suspend fun getTopWicketTakers(tId: Long, limit: Int = 10): List<TournamentPlayerStat>
    suspend fun getTopSixHitters(tId: Long, limit: Int = 10): List<TournamentPlayerStat>
    suspend fun getTopFourHitters(tId: Long, limit: Int = 10): List<TournamentPlayerStat>
    suspend fun getTopHighestScores(tId: Long, limit: Int = 10): List<TournamentPlayerStat>
    suspend fun getBestStrikeRateBowlers(tId: Long, limit: Int = 10): List<TournamentPlayerStat>
}
