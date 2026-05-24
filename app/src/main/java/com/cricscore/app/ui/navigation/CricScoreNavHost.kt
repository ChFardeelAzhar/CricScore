package com.cricscore.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cricscore.app.domain.model.MatchStatus
import com.cricscore.app.ui.home.HomeScreen
import com.cricscore.app.ui.home.HomeViewModel
import com.cricscore.app.ui.inningssetup.InningsSetupScreen
import com.cricscore.app.ui.inningssetup.InningsSetupViewModel
import com.cricscore.app.ui.result.ResultScreen
import com.cricscore.app.ui.result.ResultViewModel
import com.cricscore.app.ui.scorecard.ScorecardScreen
import com.cricscore.app.ui.scorecard.ScorecardViewModel
import com.cricscore.app.ui.scoring.ScoringScreen
import com.cricscore.app.ui.scoring.ScoringViewModel
import com.cricscore.app.ui.setup.MatchSetupScreen
import com.cricscore.app.ui.setup.MatchSetupViewModel
import com.cricscore.app.ui.toss.TossScreen
import com.cricscore.app.ui.toss.TossViewModel
import com.cricscore.app.ui.tournament.list.TournamentListScreen
import com.cricscore.app.ui.tournament.list.TournamentListViewModel
import com.cricscore.app.ui.tournament.create.TournamentCreateScreen
import com.cricscore.app.ui.tournament.create.TournamentCreateViewModel
import com.cricscore.app.ui.tournament.addteams.TournamentAddTeamsScreen
import com.cricscore.app.ui.tournament.addteams.TournamentAddTeamsViewModel
import com.cricscore.app.ui.tournament.overview.TournamentOverviewScreen
import com.cricscore.app.ui.tournament.overview.TournamentOverviewViewModel
import com.cricscore.app.ui.tournament.result.TournamentResultScreen
import com.cricscore.app.ui.tournament.result.TournamentResultViewModel
import kotlinx.coroutines.launch

@Composable
fun CricScoreNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val coroutineScope = rememberCoroutineScope()
    val integrationViewModel: TournamentIntegrationViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier.fillMaxSize(),
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onStartNewMatchClick = {
                    navController.navigate("match_setup")
                },
                onNavigateToTournaments = {
                    navController.navigate("tournament_list")
                },
                onNavigateToTournamentOverview = { tournamentId ->
                    navController.navigate("tournament_overview/$tournamentId")
                },
                onMatchClick = { match ->
                    when (match.status) {
                        MatchStatus.UPCOMING -> {
                            navController.navigate("toss/${match.id}")
                        }
                        MatchStatus.FIRST_INNINGS -> {
                            navController.navigate("scorecard/${match.id}/1/false")
                        }
                        MatchStatus.SECOND_INNINGS -> {
                            navController.navigate("scorecard/${match.id}/2/false")
                        }
                        MatchStatus.COMPLETED -> {
                            navController.navigate("scorecard/${match.id}/1/false")
                        }
                    }
                }
            )
        }

        composable("match_setup") {
            val viewModel: MatchSetupViewModel = hiltViewModel()
            MatchSetupScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSetupSuccess = { matchId ->
                    navController.navigate("toss/$matchId") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "toss/{matchId}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
            val viewModel: TossViewModel = hiltViewModel()
            TossScreen(
                viewModel = viewModel,
                matchId = matchId,
                onBackClick = {
                    navController.popBackStack()
                },
                onTossSaved = { mId ->
                    navController.navigate("innings_setup/$mId/1") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "innings_setup/{matchId}/{inningsNumber}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.LongType },
                navArgument("inningsNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
            val inningsNumber = backStackEntry.arguments?.getInt("inningsNumber") ?: 1
            val viewModel: InningsSetupViewModel = hiltViewModel()
            InningsSetupScreen(
                viewModel = viewModel,
                matchId = matchId,
                inningsNumber = inningsNumber,
                onBackClick = {
                    navController.popBackStack()
                },
                onInningsStarted = { mId, innNum ->
                    navController.navigate("scoring/$mId/$innNum") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "scoring/{matchId}/{inningsNumber}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.LongType },
                navArgument("inningsNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
            val inningsNumber = backStackEntry.arguments?.getInt("inningsNumber") ?: 1
            val viewModel: ScoringViewModel = hiltViewModel()
            ScoringScreen(
                viewModel = viewModel,
                matchId = matchId,
                inningsNumber = inningsNumber,
                onBackClick = {
                    coroutineScope.launch {
                        val fixture = integrationViewModel.getFixtureByMatchId(matchId)
                        if (fixture != null) {
                            navController.navigate("tournament_overview/${fixture.tournamentId}") {
                                popUpTo("home")
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                },
                onViewScorecardClick = {
                    navController.navigate("scorecard/$matchId/$inningsNumber/false")
                },
                onMatchCompleted = { mId, innNum, showStartNext ->
                    if (innNum == 2) {
                        integrationViewModel.completeFixtureIfNeeded(mId) {
                            navController.navigate("result/$mId") {
                                popUpTo("home")
                            }
                        }
                    } else {
                        navController.navigate("scorecard/$mId/$innNum/$showStartNext") {
                            popUpTo("home")
                        }
                    }
                }
            )
        }

        composable(
            route = "scorecard/{matchId}/{inningsNumber}/{showStartNext}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.LongType },
                navArgument("inningsNumber") { type = NavType.IntType },
                navArgument("showStartNext") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
            val inningsNumber = backStackEntry.arguments?.getInt("inningsNumber") ?: 1
            val showStartNext = backStackEntry.arguments?.getBoolean("showStartNext") ?: false
            val viewModel: ScorecardViewModel = hiltViewModel()
            ScorecardScreen(
                viewModel = viewModel,
                matchId = matchId,
                initialInnings = inningsNumber,
                showStartNext = showStartNext,
                onBackClick = {
                    coroutineScope.launch {
                        val fixture = integrationViewModel.getFixtureByMatchId(matchId)
                        if (fixture != null) {
                            navController.navigate("tournament_overview/${fixture.tournamentId}") {
                                popUpTo("home")
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                onStartSecondInningsClick = { mId ->
                    navController.navigate("innings_setup/$mId/2")
                },
                onViewResultClick = { mId ->
                    navController.navigate("result/$mId") {
                        popUpTo("home")
                    }
                },
                onResumeScoringClick = { mId, innNum ->
                    navController.navigate("scoring/$mId/$innNum") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "result/{matchId}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
            val viewModel: ResultViewModel = hiltViewModel()
            ResultScreen(
                viewModel = viewModel,
                matchId = matchId,
                onHomeClick = {
                    coroutineScope.launch {
                        val fixture = integrationViewModel.getFixtureByMatchId(matchId)
                        if (fixture != null) {
                            navController.navigate("tournament_overview/${fixture.tournamentId}") {
                                popUpTo("home")
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                },
                onViewScorecardClick = {
                    navController.navigate("scorecard/$matchId/1/false") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable("tournament_list") {
            val viewModel: TournamentListViewModel = hiltViewModel()
            TournamentListScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToCreate = {
                    navController.navigate("tournament_create")
                },
                onNavigateToOverview = { tournamentId ->
                    navController.navigate("tournament_overview/$tournamentId")
                }
            )
        }

        composable("tournament_create") {
            val viewModel: TournamentCreateViewModel = hiltViewModel()
            TournamentCreateScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onNextClick = { tournamentId, totalTeams ->
                    navController.navigate("tournament_add_teams/$tournamentId/$totalTeams")
                }
            )
        }

        composable(
            route = "tournament_add_teams/{tournamentId}/{totalTeams}",
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.LongType },
                navArgument("totalTeams") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getLong("tournamentId") ?: 0L
            val totalTeams = backStackEntry.arguments?.getInt("totalTeams") ?: 2
            val viewModel: TournamentAddTeamsViewModel = hiltViewModel()
            TournamentAddTeamsScreen(
                viewModel = viewModel,
                tournamentId = tournamentId,
                totalTeamsLimit = totalTeams,
                onBackClick = {
                    navController.popBackStack()
                },
                onFixturesGenerated = { tId ->
                    navController.navigate("tournament_overview/$tId") {
                        popUpTo("tournament_list")
                    }
                }
            )
        }

        composable(
            route = "tournament_overview/{tournamentId}",
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getLong("tournamentId") ?: 0L
            val viewModel: TournamentOverviewViewModel = hiltViewModel()
            TournamentOverviewScreen(
                viewModel = viewModel,
                tournamentId = tournamentId,
                onBackClick = {
                    navController.navigate("tournament_list") {
                        popUpTo("tournament_list") { inclusive = true }
                    }
                },
                onStartMatch = { matchId, _ ->
                    navController.navigate("toss/$matchId")
                },
                onResumeMatch = { matchId ->
                    navController.navigate("scorecard/$matchId/1/false")
                },
                onViewScorecard = { matchId ->
                    navController.navigate("scorecard/$matchId/1/false")
                },
                onNavigateToResult = { tId ->
                    navController.navigate("tournament_result/$tId")
                }
            )
        }

        composable(
            route = "tournament_result/{tournamentId}",
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getLong("tournamentId") ?: 0L
            val viewModel: TournamentResultViewModel = hiltViewModel()
            TournamentResultScreen(
                viewModel = viewModel,
                tournamentId = tournamentId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
