package com.example.shuttleapp.presentation

import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.shuttleapp.BuildConfig
import com.example.shuttleapp.Route
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.presentation.components.UploadDialogs
import com.example.shuttleapp.presentation.events.HomeAction
import com.example.shuttleapp.presentation.events.SyncAction
import com.example.shuttleapp.presentation.mapper.DashboardFormat
import com.example.shuttleapp.presentation.viewmodel.SyncViewModel
import com.example.shuttleapp.presentation.components.DrawerContent
import com.example.shuttleapp.presentation.components.LogoutConfirmDialog
import com.example.shuttleapp.presentation.components.home.HomeDashboard
import com.example.shuttleapp.presentation.components.home.HomeNavItem
import com.example.shuttleapp.presentation.state.DrawerUi
import com.example.shuttleapp.presentation.state.HomeDashboardUi
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.util.formatTimeHHmm
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HomeScreen(
    userId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ShuttleViewModel,
    syncViewModel: SyncViewModel,
    onOpenSettings: () -> Unit,
    onOpenPassList: (PassFilter) -> Unit,
    onNavigate: (HomeNavItem) -> Unit
) {

    val viewState by viewModel.shuttlePassListState.collectAsState()
    val syncState by syncViewModel.state.collectAsStateWithLifecycle()
    val names by viewModel.toNameState.collectAsState()
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
            viewModel.getProviderNameById(it)
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
                onNavigate(HomeNavItem.Sync)
            },
            onDismiss =  {
                showConfirmDialog = false
            }
        )
    }

    val dashboard = remember(userState, cardState, draftedPassengerListState, draftedShuttlePassState, viewState, syncState) {
        val user = userState.userData
        val draft = draftedShuttlePassState.data
        val now = System.currentTimeMillis()
        HomeDashboardUi(
            greeting = greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), user?.firstName.orEmpty()),
            subtitle = DashboardFormat.headerSubtitle(
                nowMillis = now,
                passesToday = DashboardFormat.countCreatedToday(viewState.list.map { it.shuttlePass.dateCreated }, now)
            ),
            initials = initialsOf(user?.firstName.orEmpty(), user?.lastName.orEmpty()),
            hasActiveTrip = draft != null,
            routeTitle = draft?.let { listOf(cardState.currentRoute.orEmpty(), it.tripType).filter(String::isNotBlank).joinToString(" · ") }.orEmpty(),
            departure = draft?.departure?.let(::formatTimeHHmm).orEmpty(),
            passengerCount = draftedPassengerListState.list.size,
            pendingSyncCount = cardState.unsyncedShuttleCount,
            isSyncing = syncState.isSyncing,
            lateCount = cardState.lateShuttleCount,
            recentPasses = viewState.items.take(RECENT_PASS_COUNT),
            isLoadingPasses = viewState.loading,
            passesError = viewState.error
        )
    }

    val drawerUi = remember(userState, names, cardState, syncState) {
        val user = userState.userData
        DrawerUi(
            name = titleCase("${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}"),
            employeeNo = user?.empNo?.takeIf(String::isNotBlank)?.let { "Employee no. $it" }.orEmpty(),
            // ShuttleViewModel reports a missing provider as "no data".
            providerName = names.providerName?.takeUnless { it.isBlank() || it == "no data" }.orEmpty(),
            initials = initialsOf(user?.firstName.orEmpty(), user?.lastName.orEmpty()),
            pendingCount = cardState.unsyncedShuttleCount,
            lastSynced = syncState.lastSynced,
            isSyncing = syncState.isSyncing,
            versionLine = "Version ${BuildConfig.VERSION_NAME} · Offline data saved on this device"
        )
    }

    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ShuttleTheme.colors.brandBand,
                drawerShape = RoundedCornerShape(topEnd = Dimens.RadiusCard, bottomEnd = Dimens.RadiusCard),
                // The drawer draws behind the status bar; its brand row pads itself.
                windowInsets = WindowInsets(0)
            ) {
                DrawerContent(
                    ui = drawerUi,
                    onClose = closeDrawer,
                    onSyncNow = { syncViewModel.onAction(SyncAction.SyncNow) },
                    onOpenSettings = {
                        closeDrawer()
                        onOpenSettings()
                    },
                    onSignOut = { showConfirmDialog = true }
                )
            }
        }
    ) {
        HomeDashboard(
            ui = dashboard,
            modifier = modifier,
            onAction = { action ->
                when (action) {
                    HomeAction.OpenProfile -> scope.launch { drawerState.open() }
                    // Resumes the draft when there is one; the pass screen starts a new pass otherwise.
                    HomeAction.PrimaryAction -> navController.navigate(Route.AVCurrentPassScreen.route + "/$userId")
                    is HomeAction.OpenPass -> navController.navigate(Route.ShuttlePassItemScreen.route + "/${action.id}")
                    is HomeAction.OpenPassList -> onOpenPassList(action.filter)
                    HomeAction.SyncNow -> syncViewModel.onAction(SyncAction.SyncNow)
                    HomeAction.RetryPasses -> viewModel.loadShuttleWithPassenger(userId)
                    is HomeAction.Navigate -> if (action.item != HomeNavItem.Home) onNavigate(action.item)
                }
            }
        )
    }

    UploadDialogs(
        upload = syncState.upload,
        onDismissResult = { syncViewModel.onAction(SyncAction.DismissSyncResult) },
        onSubmitReauth = { username, password -> syncViewModel.onAction(SyncAction.SubmitReauth(username, password)) },
        onDismissReauth = { syncViewModel.onAction(SyncAction.DismissReauth) }
    )
}

private const val RECENT_PASS_COUNT = 5

private fun greetingFor(hour: Int, firstName: String): String {
    val salutation = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    val name = titleCase(firstName)
    return if (name.isEmpty()) salutation else "$salutation, $name"
}

private fun titleCase(text: String): String =
    text.trim().lowercase().split(" ").filter(String::isNotBlank)
        .joinToString(" ") { part -> part.replaceFirstChar { it.titlecase() } }

private fun initialsOf(firstName: String, lastName: String): String =
    listOf(firstName, lastName).mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }.joinToString("")
