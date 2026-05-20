package com.cricscore.app.domain.repository

import com.cricscore.app.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun createMatch(match: Match): Long
    suspend fun updateMatch(match: Match)
    fun getMatchById(id: Long): Flow<Match?>
    suspend fun getMatchByIdSync(id: Long): Match?
    fun getRecentMatches(): Flow<List<Match>>
}
