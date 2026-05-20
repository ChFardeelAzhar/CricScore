package com.cricscore.app.domain.usecase

import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMatchByIdUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    operator fun invoke(matchId: Long): Flow<Match?> {
        return matchRepository.getMatchById(matchId)
    }
}
