package com.example.shuttleapp

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.presentation.LoginScreen
import com.example.shuttleapp.presentation.RegisterScreen
import com.example.shuttleapp.presentation.CurrentPassRoot
import com.example.shuttleapp.presentation.EditShuttlePassInformationScreen
import com.example.shuttleapp.presentation.HomeScreen
import com.example.shuttleapp.presentation.QrScannerRoot
import com.example.shuttleapp.presentation.SettingsRoot
import com.example.shuttleapp.presentation.ShuttlePassItemScreen
import com.example.shuttleapp.presentation.ShuttlePassListRoot
import com.example.shuttleapp.presentation.SyncRoot
import com.example.shuttleapp.presentation.components.SystemBarsFrame
import com.example.shuttleapp.presentation.components.home.HomeNavItem
import com.example.shuttleapp.presentation.viewmodel.ShuttlePassListViewModel
import com.example.shuttleapp.presentation.viewmodel.CurrentPassViewModel
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.presentation.viewmodel.AppearanceViewModel
import com.example.shuttleapp.presentation.viewmodel.SyncViewModel

@Composable
fun Navigation(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {

    NavHost(navController = navController, startDestination = startDestination) {

        composable(route = Route.RegisterScreen.route) {
            SystemBarsFrame { RegisterScreen(registerViewModel = hiltViewModel(), navController = navController) }
        }

        composable(route = Route.LoginScreen.route) {
            SystemBarsFrame { LoginScreen(loginViewModel = hiltViewModel(), navController = navController) }
        }

        // Home, Passes and Sync are the bottom-bar tabs. They draw edge to edge and handle insets themselves.
        composable(
            route = Route.HomeScreen.pattern,
            arguments = listOf(idArgument())
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            HomeScreen(
                userId = id,
                navController = navController,
                viewModel = hiltViewModel<ShuttleViewModel>(),
                syncViewModel = hiltViewModel<SyncViewModel>(),
                onOpenSettings = {
                    navController.navigate(Route.SettingsScreen.route) { launchSingleTop = true }
                },
                onOpenPassList = { filter -> navController.navigateToTab(HomeNavItem.Passes, id, filter) },
                onNavigate = { item -> navController.navigateToTab(item, id) }
            )
        }

        composable(
            route = Route.ShuttlePassListScreen.pattern,
            arguments = listOf(
                idArgument(),
                navArgument(ShuttlePassListViewModel.FILTER_ARG) {
                    type = NavType.StringType
                    defaultValue = PassFilter.All.name
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            ShuttlePassListRoot(
                viewModel = hiltViewModel<ShuttlePassListViewModel>(),
                onBack = { navController.navigateUp() },
                onOpenPass = { passId -> navController.navigate(Route.ShuttlePassItemScreen.route + "/$passId") },
                onNavigate = { item -> navController.navigateToTab(item, id) }
            )
        }

        composable(
            route = Route.SyncScreen.pattern,
            arguments = listOf(idArgument())
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            SyncRoot(
                viewModel = hiltViewModel<SyncViewModel>(),
                onOpenPass = { passId -> navController.navigate(Route.ShuttlePassItemScreen.route + "/$passId") },
                onNavigate = { item -> navController.navigateToTab(item, id) }
            )
        }

        composable(route = Route.SettingsScreen.route) {
            SystemBarsFrame {
                SettingsRoot(
                    viewModel = hiltViewModel<AppearanceViewModel>(),
                    onBack = { navController.navigateUp() }
                )
            }
        }

        composable(
            route = Route.AVCurrentPassScreen.route + "/{id}",
            arguments = listOf(idArgument())
        ) {
            // CurrentPassViewModel reads the driver id from SavedStateHandle["id"].
            SystemBarsFrame {
                CurrentPassRoot(
                    viewModel = hiltViewModel<CurrentPassViewModel>(),
                    onBack = { navController.navigateUp() },
                    onOpenScanner = { passId -> navController.navigate(Route.QrScannerScreen.route + "/$passId") },
                    onEditPass = { passId -> navController.navigate(Route.EditShuttlePassInfoScreen.route + "/$passId") }
                )
            }
        }

        composable(
            route = Route.ShuttlePassItemScreen.route + "/{id}",
            arguments = listOf(idArgument())
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            SystemBarsFrame {
                ShuttlePassItemScreen(
                    id = id,
                    viewModel = hiltViewModel<ShuttleViewModel>(),
                    navController = navController
                )
            }
        }

        composable(
            route = Route.EditShuttlePassInfoScreen.route + "/{id}",
            arguments = listOf(idArgument())
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            SystemBarsFrame {
                EditShuttlePassInformationScreen(
                    id = id,
                    viewModel = hiltViewModel<ShuttleViewModel>(),
                    navController = navController
                )
            }
        }

        composable(
            route = Route.QrScannerScreen.route + "/{id}",
            arguments = listOf(idArgument())
        ) {
            // The scanner reads the pass id ("id") from its SavedStateHandle.
            SystemBarsFrame { QrScannerRoot(viewModel = hiltViewModel(), onBack = { navController.navigateUp() }) }
        }
    }
}

private fun idArgument() = navArgument("id") {
    type = NavType.StringType
    defaultValue = ""
}

/**
 * Switches between the bottom-bar tabs. Home stays at the root of the back stack and Passes / Sync replace
 * each other on top of it, so Back from either tab returns Home.
 */
private fun NavController.navigateToTab(item: HomeNavItem, driverId: String, filter: PassFilter = PassFilter.All) {
    val target = when (item) {
        HomeNavItem.Home -> {
            popBackStack(Route.HomeScreen.pattern, inclusive = false)
            return
        }
        HomeNavItem.Passes -> Route.ShuttlePassListScreen.route + "/$driverId?${ShuttlePassListViewModel.FILTER_ARG}=${filter.name}"
        HomeNavItem.Sync -> Route.SyncScreen.route + "/$driverId"
    }
    navigate(target) {
        popUpTo(Route.HomeScreen.pattern)
        launchSingleTop = true
    }
}

sealed class Route(val route: String) {
    object HomeScreen: Route("homescreen") {
        const val pattern = "homescreen/{id}"
    }
    object ShuttlePassListScreen: Route("shuttlepasslistscreen") {
        const val pattern = "shuttlepasslistscreen/{id}?filter={filter}"
    }
    object SyncScreen: Route("syncscreen") {
        const val pattern = "syncscreen/{id}"
    }
    object AVCurrentPassScreen: Route("shuttlepassscreen")
    object ShuttlePassItemScreen: Route("shuttlepassitemscreen")
    object EditShuttlePassInfoScreen: Route("editshuttlepassinfoscreen")
    object QrScannerScreen: Route("qrscannerscreen")
    object LoginScreen: Route("loginscreen")
    object RegisterScreen: Route("registerscreen")
    object SettingsScreen: Route("settingsscreen")
}
