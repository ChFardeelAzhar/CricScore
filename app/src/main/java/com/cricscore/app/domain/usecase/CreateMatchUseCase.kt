package com.cricscore.app.domain.usecase

import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.repository.MatchRepository
import javax.inject.Inject

class CreateMatchUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(team1: String, team2: String, oversLimit: Int, playersPerSide: Int): Long {
        val match = Match(
            team1 = team1.trim(),
            team2 = team2.trim(),
            oversLimit = oversLimit,
            playersPerSide = playersPerSide
        )
        return matchRepository.createMatch(match)
    }
}
