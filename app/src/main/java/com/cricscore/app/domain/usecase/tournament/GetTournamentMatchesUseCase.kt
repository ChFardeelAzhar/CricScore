package com.cricscore.app.domain.usecase.tournament

import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTournamentMatchesUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    operator fun invoke(tournamentId: Long): Flow<List<Match>> {
        return matchRepository.getTournamentMatches(tournamentId)
    }
}
