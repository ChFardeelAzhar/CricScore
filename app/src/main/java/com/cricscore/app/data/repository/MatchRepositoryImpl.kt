package com.cricscore.app.data.repository

import com.cricscore.app.data.local.dao.MatchDao
import com.cricscore.app.data.local.entity.toDomain
import com.cricscore.app.data.local.entity.toEntity
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao
) : MatchRepository {

    override suspend fun createMatch(match: Match): Long {
        return matchDao.insert(match.toEntity())
    }

    override suspend fun updateMatch(match: Match) {
        matchDao.update(match.toEntity())
    }

    override fun getMatchById(id: Long): Flow<Match?> {
        return matchDao.getMatchById(id).map { it?.toDomain() }
    }

    override suspend fun getMatchByIdSync(id: Long): Match? {
        return matchDao.getMatchByIdSync(id)?.toDomain()
    }

    override fun getRecentMatches(): Flow<List<Match>> {
        return matchDao.getRecentMatches().map { list -> list.map { it.toDomain() } }
    }
}
