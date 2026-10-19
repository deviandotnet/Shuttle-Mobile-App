package com.example.shuttleapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shuttleapp.presentation.LoginScreen
import com.example.shuttleapp.presentation.RegisterScreen
import com.example.shuttleapp.presentation.AVCurrentPassScreenTest
import com.example.shuttleapp.presentation.EditShuttlePassInformationScreen
import com.example.shuttleapp.presentation.HomeScreen
import com.example.shuttleapp.presentation.QRScannerScreen
import com.example.shuttleapp.presentation.ShuttlePassItemScreen
import com.example.shuttleapp.presentation.ShuttlePassListRoot
import com.example.shuttleapp.presentation.viewmodel.ShuttlePassListViewModel
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.presentation.viewmodel.AppearanceViewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {

    NavHost(navController = navController, startDestination = startDestination) {

        composable(route = Route.RegisterScreen.route) {
            RegisterScreen(registerViewModel = hiltViewModel(), navController = navController)
        }

        composable(route = Route.LoginScreen.route) {
            LoginScreen(loginViewModel = hiltViewModel(), navController = navController)
        }

        composable(
            route = Route.HomeScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            val appearanceViewModel = hiltViewModel<AppearanceViewModel>()
            val themeMode by appearanceViewModel.themeMode.collectAsStateWithLifecycle()
            HomeScreen(
                userId = id,
                navController = navController,
                viewModel = hiltViewModel<ShuttleViewModel>(),
                themeMode = themeMode,
                onThemeModeChange = appearanceViewModel::onThemeModeChange
            )
        }

        composable(
            route = Route.AVCurrentPassScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            AVCurrentPassScreenTest(
                userId = id,
                viewModel = hiltViewModel<ShuttleViewModel>(),
                navController = navController
            )
        }

        composable(
            route = Route.ShuttlePassListScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ShuttlePassListRoot(
                viewModel = hiltViewModel<ShuttlePassListViewModel>(),
                onBack = { navController.navigateUp() },
                onOpenPass = { id -> navController.navigate(Route.ShuttlePassItemScreen.route + "/$id") }
            )
        }

        composable(
            route = Route.ShuttlePassItemScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                })
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            ShuttlePassItemScreen(
                id = id, 
                viewModel = hiltViewModel<ShuttleViewModel>(), 
                navController = navController
            )
        }

        composable(
            route = Route.EditShuttlePassInfoScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            EditShuttlePassInformationScreen(
                id = id,
                viewModel = hiltViewModel<ShuttleViewModel>(),
                navController = navController
            )
        }

        composable(
            route = Route.QrScannerScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            QRScannerScreen(id, navController, qrAnalyzerViewModel = hiltViewModel())
        }
    }
}

sealed class Route(val route: String) {
    object HomeScreen: Route("homescreen")
    object ShuttlePassListScreen: Route("shuttlepasslistscreen")
    object AVCurrentPassScreen: Route("shuttlepassscreen")
    object ShuttlePassItemScreen: Route("shuttlepassitemscreen")
    object EditShuttlePassInfoScreen: Route("editshuttlepassinfoscreen")
    object QrScannerScreen: Route("qrscannerscreen")
    object LoginScreen: Route("loginscreen")
    object RegisterScreen: Route("registerscreen")
}
