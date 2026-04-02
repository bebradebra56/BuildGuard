package com.buildfgu.guardbiu.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.buildfgu.guardbiu.ui.screens.calendar.CalendarScreen
import com.buildfgu.guardbiu.ui.screens.dashboard.DashboardScreen
import com.buildfgu.guardbiu.ui.screens.furniture.FurniturePlacementScreen
import com.buildfgu.guardbiu.ui.screens.furniture.FurnitureScreen
import com.buildfgu.guardbiu.ui.screens.history.HistoryScreen
import com.buildfgu.guardbiu.ui.screens.layout.LayoutEditorScreen
import com.buildfgu.guardbiu.ui.screens.materials.MaterialCalculatorScreen
import com.buildfgu.guardbiu.ui.screens.materials.MaterialsScreen
import com.buildfgu.guardbiu.ui.screens.measurements.MeasurementsScreen
import com.buildfgu.guardbiu.ui.screens.onboarding.OnboardingScreen
import com.buildfgu.guardbiu.ui.screens.profile.ProfileScreen
import com.buildfgu.guardbiu.ui.screens.projects.ProjectsScreen
import com.buildfgu.guardbiu.ui.screens.reports.ReportsScreen
import com.buildfgu.guardbiu.ui.screens.rooms.RoomsScreen
import com.buildfgu.guardbiu.ui.screens.scan.RoomScanScreen
import com.buildfgu.guardbiu.ui.screens.scan.ScanResultScreen
import com.buildfgu.guardbiu.ui.screens.settings.SettingsScreen
import com.buildfgu.guardbiu.ui.screens.shopping.ShoppingScreen
import com.buildfgu.guardbiu.ui.screens.tasks.TasksScreen
import com.buildfgu.guardbiu.data.preferences.UserPreferences
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch

private const val ANIM_DURATION = 300

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    innerPadding: PaddingValues
) {
    val koin = org.koin.java.KoinJavaComponent.getKoin()
    val preferences = remember { koin.get<UserPreferences>() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_DURATION))
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                scope.launch { preferences.setOnboardingCompleted(true) }
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.Projects.route) {
            ProjectsScreen(navController = navController)
        }

        composable(
            route = Screen.Rooms.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            RoomsScreen(projectId = projectId, navController = navController)
        }

        composable(
            route = Screen.RoomScan.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            RoomScanScreen(roomId = roomId, navController = navController)
        }

        composable(
            route = Screen.ScanResult.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            ScanResultScreen(roomId = roomId, navController = navController)
        }

        composable(
            route = Screen.LayoutEditor.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            LayoutEditorScreen(roomId = roomId, navController = navController)
        }

        composable(
            route = Screen.Measurements.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            MeasurementsScreen(roomId = roomId, navController = navController)
        }

        composable(
            route = Screen.Furniture.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            FurnitureScreen(roomId = roomId, navController = navController)
        }

        composable(
            route = Screen.FurniturePlacement.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            FurniturePlacementScreen(roomId = roomId, navController = navController)
        }

        composable(Screen.Materials.route) {
            MaterialsScreen(navController = navController)
        }

        composable(
            route = Screen.MaterialCalculator.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            MaterialCalculatorScreen(roomId = roomId, navController = navController)
        }

        composable(
            route = Screen.ShoppingList.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            ShoppingScreen(projectId = projectId, navController = navController)
        }

        composable(
            route = Screen.Reports.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            ReportsScreen(projectId = projectId, navController = navController)
        }

        composable(Screen.ActivityHistory.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }

        composable(Screen.Tasks.route) {
            TasksScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
