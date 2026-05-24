package com.cricscore.app.domain.usecase.tournament

import com.cricscore.app.domain.model.Fixture
import com.cricscore.app.domain.model.TournamentTeam
import com.cricscore.app.domain.repository.TournamentRepository
import javax.inject.Inject

class GenerateFixturesUseCase @Inject constructor(
    private val tournamentRepository: TournamentRepository
) {
    suspend operator fun invoke(tournamentId: Long, teams: List<TournamentTeam>): List<Fixture> {
        val n = teams.size
        val teamList = teams.toMutableList()
        
        // If odd number of teams, add a dummy team representing a BYE
        val hasBye = n % 2 != 0
        if (hasBye) {
            teamList.add(
                TournamentTeam(
                    id = -1L,
                    tournamentId = tournamentId,
                    teamName = "BYE"
                )
            )
        }

        val totalTeams = teamList.size
        val rounds = totalTeams - 1
        val halfSize = totalTeams / 2
        val fixtures = mutableListOf<Fixture>()
        var matchNumber = 1

        val rotatingTeams = teamList.toMutableList()

        for (round in 0 until rounds) {
            for (match in 0 until halfSize) {
                val home = rotatingTeams[match]
                val away = rotatingTeams[rotatingTeams.size - 1 - match]
                
                // Skip if either team is the dummy BYE team
                if (home.id != -1L && away.id != -1L) {
                    fixtures.add(
                        Fixture(
                            tournamentId = tournamentId,
                            matchNumber = matchNumber++,
                            roundNumber = round + 1,
                            team1Id = home.id,
                            team2Id = away.id,
                            team1Name = home.teamName,
                            team2Name = away.teamName,
                            status = "SCHEDULED"
                        )
                    )
                }
            }
            // Rotate teams: fix first team, rotate rest clockwise
            val last = rotatingTeams.removeAt(rotatingTeams.size - 1)
            rotatingTeams.add(1, last)
        }

        tournamentRepository.insertFixtures(fixtures)
        return fixtures
    }
}
