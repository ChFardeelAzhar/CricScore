package com.cricscore.app.domain.usecase

import com.cricscore.app.domain.model.*
import com.cricscore.app.domain.repository.InningsRepository
import com.cricscore.app.domain.repository.MatchRepository
import javax.inject.Inject

class StartInningsUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
    private val inningsRepository: InningsRepository
) {
    suspend operator fun invoke(
        matchId: Long,
        inningsNumber: Int,
        battingTeam: String,
        bowlingTeam: String,
        strikerName: String,
        nonStrikerName: String,
        bowlerName: String
    ): Long {
        // 1. Create and insert the Innings
        val innings = Innings(
            matchId = matchId,
            inningsNumber = inningsNumber,
            battingTeam = battingTeam.trim(),
            bowlingTeam = bowlingTeam.trim()
        )
        val inningsId = inningsRepository.createInnings(innings)

        // 2. Initialize batsmen
        val striker = BatsmanInnings(
            matchId = matchId,
            inningsNumber = inningsNumber,
            playerName = strikerName.trim()
        )
        val nonStriker = BatsmanInnings(
            matchId = matchId,
            inningsNumber = inningsNumber,
            playerName = nonStrikerName.trim()
        )
        inningsRepository.saveBatsmenInnings(listOf(striker, nonStriker))

        // 3. Initialize bowler
        val bowler = BowlerInnings(
            matchId = matchId,
            inningsNumber = inningsNumber,
            playerName = bowlerName.trim()
        )
        inningsRepository.saveBowlersInnings(listOf(bowler))

        // 4. Update match status
        val match = matchRepository.getMatchByIdSync(matchId)
        if (match != null) {
            val updatedStatus = if (inningsNumber == 1) MatchStatus.FIRST_INNINGS else MatchStatus.SECOND_INNINGS
            matchRepository.updateMatch(match.copy(status = updatedStatus))
        }

        return inningsId
    }
}
