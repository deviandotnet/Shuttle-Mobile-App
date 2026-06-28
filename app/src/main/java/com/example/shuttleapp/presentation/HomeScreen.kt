package com.example.shuttleapp.presentation

import com.example.shuttleapp.presentation.components.CardsContainer
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.shuttleapp.data.local.ShuttlePassWithPassengerEntity
import com.example.shuttleapp.ui.theme.*
import com.example.shuttleapp.presentation.components.BottomBarComponent
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.Route
import com.example.shuttleapp.presentation.components.ConfirmBasicDialog
import com.example.shuttleapp.util.displayMonthYear
import com.example.shuttleapp.util.formatTimeHHmm
import com.example.shuttleapp.presentation.components.DrawerContent
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.util.displayMonthYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    userId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ShuttleViewModel = hiltViewModel()
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
    LocalContext.current

    var showConfirmDialog by remember { mutableStateOf(false) }

    if(!autoDeleteShuttlePassHasTriggered){
        LaunchedEffect(Unit) {
            val itemDueDeleted = viewModel.deleteShuttlePassOnDue(userId) // auto delete due shuttle pass
            val itemDraftDeleted = viewModel.deleteAllDraftedShuttlePass(userId)
            Log.i("AutoDeleteShuttlePass", "Deleted on due ShuttlePass: $itemDueDeleted")
            Log.i("AutoDeleteDraftedShuttlePass", "Deleted on due ShuttlePass: $itemDraftDeleted")
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
        ConfirmBasicDialog(
            content = "Are you sure you want to logout?",
            onConfirm = {
                scope.launch(Dispatchers.IO) {
                    drawerState.apply {
                        drawerState.close()
                    }
                    viewModel.logOut()
                    showConfirmDialog = false

                    withContext(Dispatchers.Main) {
                        navController.navigate(Route.LoginScreen.route) {
                            popUpTo(0) { inclusive = true}
                            launchSingleTop = true
                        }
                    }
                }
            },
            onDismiss =  {
                showConfirmDialog = false
            }
        )
    }

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
                        navController=navController
                    )
                }
            }
        }
    ) {

        Scaffold(
            topBar = {
                userState.userData?.let {
                    TopBarComponent(
                        openDrawer = {
                            scope.launch {
                                //drawerState.open()
                                drawerState.apply {
                                    if(isOpen) drawerState.close() else drawerState.open()
                                }
                            }
                        },
                        title = "Good day, ${it.firstName.uppercase()}!",
                        navController = navController
                    )
                }
            },

            bottomBar = {
                BottomBarComponent(
                    accountId = userId,
                    navController = navController
                )
            }
        ) {
                innerPadding ->

            Box(modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)) {

                // Full-screen gradient background (navy → slightly lighter)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    NavyBlue,
                                    Color(0xFF0D1550)
                                )
                            )
                        ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    CardsContainer(
                        lateShuttleCount = cardState.lateShuttleCount,
                        unsyncedShuttleCount = cardState.unsyncedShuttleCount,
                        passengerCount = draftedPassengerListState.list.size,
                        tripType = if (draftedShuttlePassState.data != null) draftedShuttlePassState.data!!.tripType else "",
                        currentRoute = draftedShuttlePassState.data?.let { cardState.currentRoute } ?: ""
                    )

                    Spacer(Modifier.height(16.dp))

                    // --- Completed Shuttle Pass panel ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = Color(0xFFF6F8FC),
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp, end = 16.dp, top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Panel header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NavyBlue.copy(alpha = 0.10f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            tint = NavyBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Completed Passes",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyBlue
                                    )
                                }

                                Icon(
                                    contentDescription = "Navigate to Shuttle Pass List",
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    tint = Blue,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable {
                                            navController.navigate(Route.ShuttlePassListScreen.route + "/${userId}")
                                        }
                                )
                            }

                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = LightGray)
                            Spacer(Modifier.height(8.dp))

                            when {
                                viewState.loading -> {
                                    Spacer(Modifier.height(32.dp))
                                    CircularProgressIndicator(color = NavyBlue)
                                }
                                viewState.error != null -> {
                                    Spacer(Modifier.height(32.dp))
                                    Text("Error occurred. Please try again.", color = Red)
                                }
                                viewState.list.isEmpty() -> {
                                    Spacer(Modifier.height(32.dp))
                                    Text(
                                        "No completed passes yet.",
                                        color = Color.Gray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                                else -> {
                                    ShuttlePassList(userId, viewState.list, navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun ShuttlePassList(id: String, items: List<ShuttlePassWithPassengerEntity>, navController: NavController) {
    LazyColumn {
        items(
            items = items.takeLast(2).reversed(),
            key = { it.shuttlePass.id }
        ) { item ->
            ShuttlePassListItem(item, navController)
        }
    }

    if (items.size > 2) {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = LightGray)
        TextButton(
            onClick = { navController.navigate(Route.ShuttlePassListScreen.route + "/${id}") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "See all passes →",
                color = Blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ShuttlePassListItem(shuttlePassenger: ShuttlePassWithPassengerEntity, navController: NavController) {

    var route by remember { mutableStateOf("") }
    val viewModel: ShuttleViewModel = hiltViewModel()

    LaunchedEffect(shuttlePassenger) {
        route = viewModel.unflowGetRouteById(shuttlePassenger.shuttlePass.routeId)
    }

    val isLate = shuttlePassenger.shuttlePass.isLateShuttle
    val isSynced = shuttlePassenger.shuttlePass.isSynced

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable {
                val id = shuttlePassenger.shuttlePass.id
                navController.navigate(Route.ShuttlePassItemScreen.route + "/$id")
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLate) Color(0xFFFFF3F3) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: coloured indicator bar + content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Coloured left indicator
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(if (isLate) Red else Green)
                )
                Spacer(Modifier.width(12.dp))

                Column {
                    // Date + time line
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLate) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Red.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        tint = Red,
                                        contentDescription = null,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        "LATE",
                                        color = Red,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = "${shuttlePassenger.shuttlePass.date}  ${formatTimeHHmm(shuttlePassenger.shuttlePass.departure)} — ${formatTimeHHmm(shuttlePassenger.shuttlePass.arrival)}",
                            color = if (isLate) Red else Color(0xFF5A6473),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    // Pass ID
                    Text(
                        text = "SPT-${displayMonthYear()}-${shuttlePassenger.shuttlePass.id.takeLast(12).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(2.dp))

                    // Route + trip type
                    Text(
                        text = "$route · ${shuttlePassenger.shuttlePass.tripType}",
                        color = Color(0xFF8A94A0),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: sync status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSynced) Green.copy(alpha = 0.12f)
                        else Color(0xFFF0F0F0)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        tint = if (isSynced) Green else Color.LightGray,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isSynced) "Synced" else "Unsynced",
                        color = if (isSynced) Green else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

//@Composable
//@Preview(showBackground = true, showSystemUi = true)
//fun HomeScreenPreview() {
//
//    CardsContainer(1,2,3,"Mamatid", "Incoming")
//}



