package com.cricscore.app.domain.usecase

import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentMatchesUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    operator fun invoke(): Flow<List<Match>> {
        return matchRepository.getRecentMatches()
    }
}
