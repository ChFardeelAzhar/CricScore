package com.cricscore.app.data.repository

import com.cricscore.app.data.local.dao.*
import com.cricscore.app.data.local.entity.*
import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.TournamentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TournamentRepositoryImpl @Inject constructor(
    private val tournamentDao: TournamentDao,
    private val tournamentTeamDao: TournamentTeamDao,
    private val fixtureDao: FixtureDao,
    private val playerStatDao: TournamentPlayerStatDao
) : TournamentRepository {

    override suspend fun createTournament(t: Tournament): Long {
        return tournamentDao.insert(t.toEntity())
    }

    override suspend fun updateTournament(t: Tournament) {
        tournamentDao.update(t.toEntity())
    }

    override suspend fun deleteTournament(t: Tournament) {
        tournamentDao.delete(t.toEntity())
    }

    override fun getAllTournaments(): Flow<List<Tournament>> {
        return tournamentDao.getAllTournaments().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTournamentById(id: Long): Tournament? {
        return tournamentDao.getById(id)?.toDomain()
    }

    override fun getOngoingTournaments(): Flow<List<Tournament>> {
        return tournamentDao.getOngoingTournaments().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTeam(team: TournamentTeam): Long {
        return tournamentTeamDao.insert(team.toEntity())
    }

    override suspend fun insertTeams(teams: List<TournamentTeam>) {
        tournamentTeamDao.insertAll(teams.map { it.toEntity() })
    }

    override suspend fun updateTeam(team: TournamentTeam) {
        tournamentTeamDao.update(team.toEntity())
    }

    override suspend fun updateTeams(teams: List<TournamentTeam>) {
        tournamentTeamDao.updateAll(teams.map { it.toEntity() })
    }

    override fun getTeamsByTournament(tId: Long): Flow<List<TournamentTeam>> {
        return tournamentTeamDao.getTeamsByTournament(tId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTeamsByTournamentSync(tId: Long): List<TournamentTeam> {
        return tournamentTeamDao.getTeamsByTournamentSync(tId).map { it.toDomain() }
    }

    override suspend fun getTeamById(id: Long): TournamentTeam? {
        return tournamentTeamDao.getById(id)?.toDomain()
    }

    override suspend fun insertFixture(f: Fixture): Long {
        return fixtureDao.insert(f.toEntity())
    }

    override suspend fun insertFixtures(fixtures: List<Fixture>) {
        fixtureDao.insertAll(fixtures.map { it.toEntity() })
    }

    override suspend fun updateFixture(f: Fixture) {
        fixtureDao.update(f.toEntity())
    }

    override fun getFixturesByTournament(tId: Long): Flow<List<Fixture>> {
        return fixtureDao.getFixturesByTournament(tId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getFixturesByTournamentSync(tId: Long): List<Fixture> {
        return fixtureDao.getFixturesByTournamentSync(tId).map { it.toDomain() }
    }

    override suspend fun getFixtureById(id: Long): Fixture? {
        return fixtureDao.getById(id)?.toDomain()
    }

    override suspend fun getFixtureByLinkedMatchId(matchId: Long): Fixture? {
        return fixtureDao.getByLinkedMatchId(matchId)?.toDomain()
    }

    override suspend fun getNextScheduledFixture(tId: Long): Fixture? {
        return fixtureDao.getNextScheduledFixture(tId)?.toDomain()
    }

    override suspend fun getPendingFixtureCount(tId: Long): Int {
        return fixtureDao.getPendingFixtureCount(tId)
    }

    override suspend fun insertPlayerStat(stat: TournamentPlayerStat): Long {
        return playerStatDao.insert(stat.toEntity())
    }

    override suspend fun updatePlayerStat(stat: TournamentPlayerStat) {
        playerStatDao.update(stat.toEntity())
    }

    override fun getStatsByTournament(tId: Long): Flow<List<TournamentPlayerStat>> {
        return playerStatDao.getStatsByTournament(tId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPlayerStat(tId: Long, name: String, teamId: Long): TournamentPlayerStat? {
        return playerStatDao.getPlayerStat(tId, name, teamId)?.toDomain()
    }

    override suspend fun getTopRunScorers(tId: Long, limit: Int): List<TournamentPlayerStat> {
        return playerStatDao.getTopRunScorers(tId, limit).map { it.toDomain() }
    }

    override suspend fun getTopWicketTakers(tId: Long, limit: Int): List<TournamentPlayerStat> {
        return playerStatDao.getTopWicketTakers(tId, limit).map { it.toDomain() }
    }

    override suspend fun getTopSixHitters(tId: Long, limit: Int): List<TournamentPlayerStat> {
        return playerStatDao.getTopSixHitters(tId, limit).map { it.toDomain() }
    }

    override suspend fun getTopFourHitters(tId: Long, limit: Int): List<TournamentPlayerStat> {
        return playerStatDao.getTopFourHitters(tId, limit).map { it.toDomain() }
    }

    override suspend fun getTopHighestScores(tId: Long, limit: Int): List<TournamentPlayerStat> {
        return playerStatDao.getTopHighestScores(tId, limit).map { it.toDomain() }
    }

    override suspend fun getBestStrikeRateBowlers(tId: Long, limit: Int): List<TournamentPlayerStat> {
        return playerStatDao.getBestStrikeRateBowlers(tId, limit).map { it.toDomain() }
    }
}
