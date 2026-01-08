package com.nfctools.reader.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nfctools.reader.presentation.history.HistoryScreen
import com.nfctools.reader.presentation.home.HomeScreen
import com.nfctools.reader.presentation.read.ReadScreen
import com.nfctools.reader.presentation.settings.SettingsScreen
import com.nfctools.reader.presentation.write.WriteScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Read : Screen("read")
    data object Write : Screen("write?mode={mode}") {
        fun createRoute(mode: String = "text") = "write?mode=$mode"
    }
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = "home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "首页"
    ),
    BottomNavItem(
        route = "read",
        selectedIcon = Icons.Filled.Label,
        unselectedIcon = Icons.Outlined.Label,
        label = "读取"
    ),
    BottomNavItem(
        route = "write?mode=text",
        selectedIcon = Icons.Filled.Edit,
        unselectedIcon = Icons.Outlined.Edit,
        label = "写入"
    ),
    BottomNavItem(
        route = "history",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        label = "历史"
    ),
    BottomNavItem(
        route = "settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "设置"
    )
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = when {
                item.route.startsWith("write") -> currentRoute?.startsWith("write") == true
                else -> currentRoute == item.route
            }
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NFCApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // 判断是否显示底部导航栏
    val showBottomBar = currentRoute in listOf("home", "read", "history", "settings") ||
            currentRoute?.startsWith("write") == true
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            
            composable("read") {
                ReadScreen(navController)
            }
            
            composable(
                route = "write?mode={mode}",
                arguments = listOf(
                    navArgument("mode") {
                        type = NavType.StringType
                        defaultValue = "text"
                    }
                )
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "text"
                WriteScreen(
                    navController = navController,
                    initialMode = mode
                )
            }
            
            composable("history") {
                HistoryScreen(navController)
            }
            
            composable("settings") {
                SettingsScreen(navController)
            }
        }
    }
}
