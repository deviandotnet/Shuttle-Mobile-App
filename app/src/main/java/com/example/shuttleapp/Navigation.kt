package com.example.shuttleapp

import androidx.compose.runtime.Composable
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
import com.example.shuttleapp.presentation.ShuttlePassListScreen
import com.example.shuttleapp.presentation.viewmodel.FilterViewModel
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {

    NavHost(navController = navController, startDestination = Route.LoginScreen.route) {

        composable(route = Route.RegisterScreen.route) {
            RegisterScreen(navController = navController)
        }

        composable(route = Route.LoginScreen.route) {
            LoginScreen(navController = navController)
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
            HomeScreen(
                userId = id,
                navController = navController,
                viewModel = hiltViewModel<ShuttleViewModel>()
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
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            ShuttlePassListScreen(
                driverId = id,
                shuttleViewModel = hiltViewModel<ShuttleViewModel>(),
                filterViewModel = hiltViewModel<FilterViewModel>(),
                navController = navController
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
            QRScannerScreen(id, navController)
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
