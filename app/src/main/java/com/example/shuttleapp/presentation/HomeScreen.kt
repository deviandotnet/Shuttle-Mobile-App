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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NavyBlue),/*.systemBarsPadding(),*/
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                )

                {
                    Spacer(Modifier.weight(1f))
                    CardsContainer(
                        lateShuttleCount = cardState.lateShuttleCount,
                        unsyncedShuttleCount = cardState.unsyncedShuttleCount,
                        passengerCount = draftedPassengerListState.list.size,
                        tripType = if(draftedShuttlePassState.data != null) draftedShuttlePassState.data!!.tripType else "",
                        currentRoute = draftedShuttlePassState.data?.let { cardState.currentRoute } ?: ""
                    )

                    Spacer(Modifier.weight(1f))
                    //card white for shuttle pass list
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally)
                        {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .background(
                                        White,
                                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                    )
                                    .padding(start = 12.dp, end = 12.dp, top = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top

                            ) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically)
                                {
                                    Text(text = "Completed Shuttle Pass", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

                                    Spacer(Modifier.weight(1f))

                                    Icon(
                                        contentDescription = "Navigate to Shuttle Pass List",
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                        tint = Blue,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clickable {
                                                //navigate to shuttle lists
                                                navController.navigate(Route.ShuttlePassListScreen.route + "/${userId}")
                                            }

                                    )

                                }

                                Spacer(modifier = Modifier.padding(4.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(LightGray) // Apply border color
                                )

                                //ShuttlePassList(viewState.list, navController, viewModel)

                                when {
                                    viewState.loading -> {
                                        CircularProgressIndicator(modifier.align(Alignment.CenterHorizontally))
                                    }
                                    viewState.error != null -> {
                                        Text("Error occurred. Please try again.")
                                    }
                                    viewState.list.isEmpty() -> {
                                        Text("No items to display.")
                                    }
                                    else -> {
                                        ShuttlePassList(userId,viewState.list, navController) //get
                                    }
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
fun ShuttlePassList(id: String, items: List<ShuttlePassWithPassengerEntity>, navController: NavController){

    LazyColumn {

        items(items.takeLast(2).reversed()) {item ->
            ShuttlePassListItem(item, navController)
        }

    }

    if(items.size > 2) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LightGray) // Apply border color
        )

        TextButton(
            onClick = {
                navController.navigate(Route.ShuttlePassListScreen.route + "/${id}")
            },
            modifier = Modifier
        ) {

            Text(
                text = "See more items", color = Blue, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            )
        }



    }

}

@Composable//1st param, para sa display, 2nd param, for passing instance, para may display dun sa navigation
fun ShuttlePassListItem(shuttlePassenger: ShuttlePassWithPassengerEntity, navController: NavController) {

    var route by remember { mutableStateOf("") }

    val viewModel: ShuttleViewModel = hiltViewModel<ShuttleViewModel>()

    LaunchedEffect(shuttlePassenger) {
        route = viewModel.unflowGetRouteById(shuttlePassenger.shuttlePass.routeId)

    }



    Row(modifier = Modifier
        .padding(start = 8.dp, end = 8.dp)
        .clickable {
            val id = shuttlePassenger.shuttlePass.id
            navController.navigate(Route.ShuttlePassItemScreen.route + "/$id")
        }
        .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween)
    {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center)
        {
            Row(verticalAlignment = Alignment.CenterVertically) {

                if(shuttlePassenger.shuttlePass.isLateShuttle) {

                    Icon(
                        contentDescription = "status",
                        imageVector = Icons.Filled.AccessTime,
                        tint = Red,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(" Late", color = Red, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text(
                        " ${shuttlePassenger.shuttlePass.date} (${formatTimeHHmm(shuttlePassenger.shuttlePass.departure)} - ${formatTimeHHmm(shuttlePassenger.shuttlePass.arrival)})",
                        color = Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                else {

                    Text(
                        "${shuttlePassenger.shuttlePass.date} (${formatTimeHHmm(shuttlePassenger.shuttlePass.departure)} - ${formatTimeHHmm(shuttlePassenger.shuttlePass.arrival)})",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


            }

            Text(text = "SPT-${displayMonthYear()}-${shuttlePassenger.shuttlePass.id.takeLast(12).uppercase()}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp)

            Text(text = "$route - ${shuttlePassenger.shuttlePass.tripType}",
                color = Black,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp)

        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (shuttlePassenger.shuttlePass.isSynced) "Synced" else "Not Synced",

                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (shuttlePassenger.shuttlePass.isSynced) Color.Unspecified else Gray,
                    fontStyle = if (shuttlePassenger.shuttlePass.isSynced) FontStyle.Italic else FontStyle.Italic
                )
                Icon(
                    contentDescription = "status",
                    imageVector = Icons.Filled.CheckCircle,
                    tint = if (shuttlePassenger.shuttlePass.isSynced) Green else Gray,
                    modifier = Modifier.size(12.dp)
                )
            }

        }

    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LightGray) // Apply border color
    )

}

//@Composable
//@Preview(showBackground = true, showSystemUi = true)
//fun HomeScreenPreview() {
//
//    CardsContainer(1,2,3,"Mamatid", "Incoming")
//}



