package com.example.shuttleapp.presentation

import android.util.Log
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.shuttleapp.Route
import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.presentation.components.DrawerContent
import com.example.shuttleapp.presentation.components.LogoutConfirmDialog
import com.example.shuttleapp.presentation.components.home.HomeDashboard
import com.example.shuttleapp.presentation.components.home.HomeNavItem
import com.example.shuttleapp.presentation.state.HomeDashboardUi
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.util.formatTimeHHmm
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HomeScreen(
    userId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ShuttleViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {

    val viewState by viewModel.shuttlePassListState.collectAsState()
    val userState by viewModel.userState.collectAsState()
    val cardState by viewModel.cardState.collectAsState()
    val draftedPassengerListState by viewModel.draftedPassengerListState.collectAsState()
    val draftedShuttlePassState by viewModel.draftedShuttlePassState.collectAsState()

    //This persists the value across recompositions and configuration changes as long as the composable stays in the navigation backstack.
    var autoDeleteShuttlePassHasTriggered by rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showConfirmDialog by remember { mutableStateOf(false) }

    if(!autoDeleteShuttlePassHasTriggered){
        LaunchedEffect(Unit) {
            val itemDeleted = viewModel.purgeExpired(userId) // auto delete due shuttle pass and stale drafts
            Log.i("AutoDeleteShuttlePass", "Deleted expired ShuttlePass: $itemDeleted")
            autoDeleteShuttlePassHasTriggered = true
        }
    }

    LaunchedEffect(userId) { //with param id para controlled
        viewModel.getUserById(userId) //fill the userState
        viewModel.loadShuttleWithPassenger(userId)
        viewModel.loadShuttlePassDraft(userId) //fill the draftedShuttlePassState
        viewModel.countUnsyncedShuttlePass(userId)
        viewModel.countLateShuttlePass(userId)
        Log.i("loadShuttleWithPassenger", "load shuttle pass with passenger done")
    }
    LaunchedEffect(userState) {
        userState.userData?.providerId?.let {
            viewModel.getAllShuttleByProviderId(it, false)

        }
    }

    LaunchedEffect(draftedShuttlePassState) {
        draftedShuttlePassState.data?.let {
            viewModel.getCurrentRouteNameById(it.routeId)
            viewModel.loadDraftedPassenger(draftedShuttlePassState.data!!.id)
        }
    }



    if(showConfirmDialog) {
        LogoutConfirmDialog(
            unsyncedCount = cardState.unsyncedShuttleCount,
            onLogout = {
                showConfirmDialog = false
                scope.launch {
                    drawerState.close()
                    viewModel.logOut()
                    navController.navigate(Route.LoginScreen.route) {
                        popUpTo(0) { inclusive = true}
                        launchSingleTop = true
                    }
                }
            },
            onUploadFirst = {
                showConfirmDialog = false
                scope.launch { drawerState.close() }
                navController.navigate(Route.ShuttlePassListScreen.route + "/${userId}")
            },
            onDismiss =  {
                showConfirmDialog = false
            }
        )
    }

    val dashboard = remember(userState, cardState, draftedPassengerListState, draftedShuttlePassState, viewState) {
        val user = userState.userData
        val draft = draftedShuttlePassState.data
        HomeDashboardUi(
            greeting = greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), user?.firstName.orEmpty()),
            initials = initialsOf(user?.firstName.orEmpty(), user?.lastName.orEmpty()),
            hasActiveTrip = draft != null,
            routeTitle = draft?.let { listOf(cardState.currentRoute.orEmpty(), it.tripType).filter(String::isNotBlank).joinToString(" · ") }.orEmpty(),
            departure = draft?.departure?.let(::formatTimeHHmm).orEmpty(),
            passengerCount = draftedPassengerListState.list.size,
            pendingSyncCount = cardState.unsyncedShuttleCount,
            lateCount = cardState.lateShuttleCount,
            recentPasses = viewState.items.take(RECENT_PASS_COUNT),
            totalPassCount = viewState.items.size,
            isLoadingPasses = viewState.loading,
            passesError = viewState.error
        )
    }

    val toggleDrawer: () -> Unit = {
        scope.launch { if (drawerState.isOpen) drawerState.close() else drawerState.open() }
    }
    val openPassList = { navController.navigate(Route.ShuttlePassListScreen.route + "/${userId}") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                userState.userData?.let {userData ->
                    DrawerContent(
                        onLogOut = {
                            showConfirmDialog = true
                        },
                        userData = userData,
                        navController=navController,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange
                    )
                }
            }
        }
    ) {
        HomeDashboard(
            ui = dashboard,
            onOpenProfile = toggleDrawer,
            // Resumes the draft when there is one; the pass screen starts a new pass otherwise.
            onPrimaryAction = { navController.navigate(Route.AVCurrentPassScreen.route + "/$userId") },
            onOpenPass = { id -> navController.navigate(Route.ShuttlePassItemScreen.route + "/$id") },
            onViewAll = openPassList,
            onNavSelect = { item ->
                when (item) {
                    HomeNavItem.Home -> Unit
                    HomeNavItem.Passes -> openPassList()
                    HomeNavItem.Profile -> toggleDrawer()
                }
            }
        )
    }
}

private const val RECENT_PASS_COUNT = 5

private fun greetingFor(hour: Int, firstName: String): String {
    val salutation = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    val name = firstName.trim().lowercase().split(" ").filter(String::isNotBlank)
        .joinToString(" ") { part -> part.replaceFirstChar { it.titlecase() } }
    return if (name.isEmpty()) salutation else "$salutation, $name"
}

private fun initialsOf(firstName: String, lastName: String): String =
    listOf(firstName, lastName).mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }.joinToString("")
