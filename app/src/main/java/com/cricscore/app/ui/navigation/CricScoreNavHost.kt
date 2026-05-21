package com.cricscore.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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

@Composable
fun CricScoreNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
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
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onViewScorecardClick = {
                    navController.navigate("scorecard/$matchId/$inningsNumber/false")
                },
                onMatchCompleted = { mId, innNum, showStartNext ->
                    navController.navigate("scorecard/$mId/$innNum/$showStartNext") {
                        popUpTo("home")
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
                    navController.popBackStack()
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
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onViewScorecardClick = {
                    // Navigate back to scorecard, but with showStartNext = false and innings number = 1 (default)
                    navController.navigate("scorecard/$matchId/1/false") {
                        popUpTo("home")
                    }
                }
            )
        }
    }
}
