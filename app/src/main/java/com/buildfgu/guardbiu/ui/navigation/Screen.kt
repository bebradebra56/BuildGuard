package com.buildfgu.guardbiu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Projects : Screen("projects")
    data object Rooms : Screen("rooms/{projectId}") {
        fun createRoute(projectId: Long) = "rooms/$projectId"
    }
    data object RoomScan : Screen("room_scan/{roomId}") {
        fun createRoute(roomId: Long) = "room_scan/$roomId"
    }
    data object ScanResult : Screen("scan_result/{roomId}") {
        fun createRoute(roomId: Long) = "scan_result/$roomId"
    }
    data object LayoutEditor : Screen("layout_editor/{roomId}") {
        fun createRoute(roomId: Long) = "layout_editor/$roomId"
    }
    data object Measurements : Screen("measurements/{roomId}") {
        fun createRoute(roomId: Long) = "measurements/$roomId"
    }
    data object Furniture : Screen("furniture/{roomId}") {
        fun createRoute(roomId: Long) = "furniture/$roomId"
    }
    data object FurniturePlacement : Screen("furniture_placement/{roomId}") {
        fun createRoute(roomId: Long) = "furniture_placement/$roomId"
    }
    data object Materials : Screen("materials")
    data object MaterialCalculator : Screen("material_calculator/{roomId}") {
        fun createRoute(roomId: Long) = "material_calculator/$roomId"
    }
    data object ShoppingList : Screen("shopping/{projectId}") {
        fun createRoute(projectId: Long) = "shopping/$projectId"
    }
    data object Reports : Screen("reports/{projectId}") {
        fun createRoute(projectId: Long) = "reports/$projectId"
    }
    data object ActivityHistory : Screen("activity_history")
    data object Calendar : Screen("calendar")
    data object Tasks : Screen("tasks")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Rounded.Home, Screen.Dashboard.route),
    BottomNavItem("Projects", Icons.Rounded.FolderOpen, Screen.Projects.route),
    BottomNavItem("Materials", Icons.Rounded.Build, Screen.Materials.route),
    BottomNavItem("Tasks", Icons.Rounded.Checklist, Screen.Tasks.route),
    BottomNavItem("Settings", Icons.Rounded.Settings, Screen.Settings.route)
)
