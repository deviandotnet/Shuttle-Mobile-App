package com.example.shuttleapp.presentation

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.presentation.components.EditDeleteChoiceDialog
import com.example.shuttleapp.presentation.components.ModifiedTextField
import com.example.shuttleapp.ui.theme.*
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.events.QRInputEvent
import com.example.shuttleapp.presentation.events.ShuttlePassInputEvent
import com.example.shuttleapp.Route
import com.example.shuttleapp.presentation.components.AutoResizeText
import com.example.shuttleapp.presentation.components.ConfirmBasicDialog
import com.example.shuttleapp.presentation.components.ExpandedDropDown
import com.example.shuttleapp.presentation.components.ShuttleDetailsCard
import com.example.shuttleapp.presentation.components.StyledSnackBarHost
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.util.displayTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AVCurrentPassScreenTest(
    userId: String,
    navController: NavController, 
    viewModel: ShuttleViewModel = hiltViewModel()
) {

    val routesState by viewModel.routeState.collectAsState()
    val shuttleState by viewModel.shuttleState.collectAsState()
    val userState by viewModel.userState.collectAsState()

    val shuttlePassInputState by viewModel.shuttlePassInputState.collectAsState()
    val shuttlePass by viewModel.shuttlePass.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val draftedShuttlePassState by viewModel.draftedShuttlePassState.collectAsState()
    val draftPassengerListState by viewModel.draftedPassengerListState.collectAsState() //passenger draft

    // 1. Fetch the user profile for this screen instance
    LaunchedEffect(userId) {
        viewModel.getUserById(userId)
    }

    // 2. Once user data is ready, fetch shuttles and the current draft
    LaunchedEffect(userState.userData) {
        userState.userData?.let { userData ->
            userData.providerId?.let { providerId ->
                Log.d("FetchShuttleData", "Fetching shuttles for provider: $providerId")
                viewModel.getAllShuttleByProviderId(providerId, false)
            }
            viewModel.loadShuttlePassDraft(userData.accountId)
        }
    }

    // 3. Continuously observe drafted passengers once a pass ID is known
    LaunchedEffect(draftedShuttlePassState.data?.id) {
        draftedShuttlePassState.data?.id?.let { passId ->
            viewModel.loadDraftedPassenger(passId) 
        }
    }

    val title =
        if(draftedShuttlePassState.data != null) "Shuttle Pass"
        else "Create New Shuttle Pass"

    Scaffold(
        snackbarHost = { StyledSnackBarHost(hostState = snackBarHostState) },
        topBar = {
            TopBarComponent(
                title = title,
                navController = navController,
                action = {
                    if(title == "Shuttle Pass") {

                        var showDeleteConfirmation by remember { mutableStateOf(false) }

                        IconButton(

                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    userState.userData?.let {driver ->
                                        viewModel.getShuttlePassIdDraftByDriverId(driver.accountId) { result ->
                                            try {
                                                if(result != null && result != "") {
                                                    navController.navigate(Route.EditShuttlePassInfoScreen.route + "/${result}")
                                                } else {
                                                    Toast.makeText(context, "Failed to retrieve id", Toast.LENGTH_LONG).show()
                                                }
                                            } catch (ex: Exception) {
                                                Log.e("Error", "An error occurred while fetching draft ID: ${ex.message}")
                                                Toast.makeText(context, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }

                            }

                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                tint = White,
                                contentDescription = "edit icon"
                            )
                        }

                        IconButton(onClick = {
                            showDeleteConfirmation = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                tint = White,
                                contentDescription = "Delete"
                            )
                        }

                        //use the Dialog components
                        if(showDeleteConfirmation) {
                            ConfirmBasicDialog(
                                content = "Are you sure you want to delete? This can not be undone",
                                onDismiss = {
                                    showDeleteConfirmation = false
                                },

                                onConfirm = {

                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            draftedShuttlePassState.data?.let { viewModel.deleteShuttlePassById(it.id) }
                                            viewModel.resetShuttlePassInputState()
                                        }
                                        catch (ex: Exception) {
                                            Log.e("DeleteShuttlePass", ex.message.toString())
                                        }
                                    }
                                    Toast.makeText(context, "Clear draft successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            )
        },

        ) { innerPadding ->

        Box(modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .background(color = White)) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                //creating new shuttle pass
                if(draftedShuttlePassState.data == null) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp, end = 30.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No incomplete shuttle pass.",
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray)
                        Text(
                            text = "Complete the fields to create new shuttle pass",
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray)

                        Spacer(modifier = Modifier.height(4.dp))

                        LaunchedEffect(userState.userData) {
                            userState.userData?.let {
                                viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.DriverChanged(it.accountId))
                                it.providerId?.let { providerId ->
                                    viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.ShuttleProviderChanged(providerId))
                                }
                            }
                        }

                        ExpandedDropDown (
                            label = "Route",
                            categories = routesState.routes,
                            initialValue = shuttlePassInputState.route,
                            onValueChanged = {
                                viewModel.onShuttlePassInputEvent(
                                    ShuttlePassInputEvent
                                        .RouteChanged(it)
                                )
                                viewModel.resetRouteErrorMessage()
                            },
                            errorIndicator = shuttlePassInputState.routeErrorMessage != null,
                        )
                        Text(
                            text = shuttlePassInputState.routeErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error)

                        ExpandedDropDown(
                            label = "Trip Type",
                            categories = listOf("Incoming", "Outgoing"),
                            initialValue = shuttlePassInputState.tripType,
                            onValueChanged = {
                                viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent
                                    .TripTypeChanged(it))

                                viewModel.resetTripTypeErrorMessage()

                            },
                            errorIndicator = shuttlePassInputState.tripTypeErrorMessage != null,
                        )
                        Text(
                            text = shuttlePassInputState.tripTypeErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error)

                        ExpandedDropDown(
                            label = "Plate Number",
                            categories = shuttleState.plateNumbers,
                            initialValue = shuttlePassInputState.plateNumber,
                            onValueChanged = {
                                viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent
                                    .PlateNumberChanged(it))

                                viewModel.resetPlateNumberErrorMessage()
                            },
                            errorIndicator = shuttlePassInputState.plateNumberErrorMessage != null,
                        )
                        Text(
                            text = shuttlePassInputState.plateNumberErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error)

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {

                                if(viewModel.submitShuttlePass()) {

                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            Log.i("CreateShuttlePass", "Creating shuttle Pass. . . ")

                                            userState.userData?.let {
                                                val newDraftShuttlePass = shuttlePass.copy(
                                                    id = UUID.randomUUID().toString(),
                                                    routeId = viewModel.getRouteIdByName(
                                                        shuttlePassInputState.route
                                                    ),
                                                    plateNumber = viewModel.getShuttleIdByPlateNumber(
                                                        shuttlePassInputState.plateNumber
                                                    ),
                                                    tripType = shuttlePassInputState.tripType,
                                                    driver = shuttlePassInputState.driver,
                                                    provider = shuttlePassInputState.shuttleProvider
                                                )
                                                viewModel.insertShuttlePass(newDraftShuttlePass)
                                            }
                                            Log.i("CreateShuttlePass", "Shuttle Pass Created")
                                        } catch (ex: Exception) {
                                            Log.e(
                                                "Error",
                                                "An error occurred while creating new pass: ${ex.message}"
                                            )
                                            Toast.makeText(
                                                context,
                                                "An unexpected error occurred. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(NavyBlue),
                            elevation = ButtonDefaults.buttonElevation(8.dp))
                        {
                            Text("Create new shuttle pass", color = White)
                        }
                    }
                }

                //load drafted shuttle pass
                else {
                    ShuttlePassFormTest(
                        shuttlePass = draftedShuttlePassState.data!!,
                        draftPassengerList = draftPassengerListState.list,
                        viewModel = viewModel, 
                        navController = navController
                    )
                }
            }
        }

    }
}

@Composable
fun ShuttlePassFormTest(shuttlePass: ShuttlePass, draftPassengerList: List<PassengerQR>, viewModel: ShuttleViewModel = hiltViewModel(), navController: NavController) {

    var showAddPassenger by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    val nameState by viewModel.toNameState.collectAsState()
    val userState by viewModel.userState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shuttlePass) {
        viewModel.getRouteNameById(shuttlePass.routeId)
        viewModel.getPlateNumberByShuttleId(shuttlePass.plateNumber)
        viewModel.getProviderNameById(shuttlePass.provider)
        viewModel.getDriverNameById(shuttlePass.driver)
    }
    
    // Ensure form is reset if state leak occurs
    LaunchedEffect(userState.userData) {
        userState.userData?.let { userData ->
            viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.DriverChanged(userData.accountId))
            userData.providerId?.let { providerId ->
                viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.ShuttleProviderChanged(providerId))
            }
        }
    }

    Scaffold(snackbarHost = { StyledSnackBarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .background(color = White),

            ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ShuttleDetailsCard(
                        route = nameState.routeName ?: "",
                        departure = if(shuttlePass.departure != "") shuttlePass.departure else "- - - -",
                        arrival = if(shuttlePass.arrival != "") shuttlePass.arrival else "- - - -",
                        driver = nameState.driverName ?: "",
                        plateNumber = nameState.plateNumber ?: "",
                        shuttleProvider = nameState.providerName ?: "",
                        tripType = shuttlePass.tripType
                    )
                }

            }

            Row(
                modifier = Modifier
                    .padding(bottom = 12.dp, start = 12.dp)
                    .fillMaxWidth()
            ) {
                Text("Passenger List", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)

            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(LightGray) 
            )
            Column(
                modifier = Modifier
                    .weight(1f) 
                    .fillMaxWidth()
                    .background(White)
            ) {
                when {
                    draftPassengerList.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No passengers to display.", color = Color.DarkGray)
                            Text(text = "Click + to add", color = Color.DarkGray)
                        }

                    }
                    else -> {

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            itemsIndexed(draftPassengerList.reversed()) { index, passenger ->
                                PassengerItemTest(viewModel,index+1, passenger)
                            }
                        }
                    }
                }
            }

            if(shuttlePass.isDraft) {
                Column(
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(NavyBlue),
                        elevation = ButtonDefaults.buttonElevation(4.dp),
                        onClick = {
                            navController.navigate(Route.QrScannerScreen.route + "/${shuttlePass.id}")
                        })
                    {
                        Icon(
                            contentDescription = "add icon", tint = White,
                            imageVector = Icons.Rounded.AddCircle
                        )

                        Text("Add passenger", color = White)
                    }

                    if (shuttlePass.departure.isEmpty()) {
                        Button(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Green),
                            elevation = ButtonDefaults.buttonElevation(8.dp),
                            onClick = {

                                val updateTime = listOf(shuttlePass.copy(
                                    departure = displayTime()
                                ))
                                viewModel.updateShuttlePass(updateTime)
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "Departure Recorded",
                                        actionLabel = "Dismiss",
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                        ) {
                            Text("Start Departure", color = White)
                        }
                    } else {
                        Button(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Green),
                            elevation = ButtonDefaults.buttonElevation(8.dp),
                            enabled = draftPassengerList.isNotEmpty() && shuttlePass.departure != "",
                            onClick = {

                                showConfirmation = true

                            }

                        ) {

                            Text("End Shuttle Pass Record", color = White)

                        }
                    }
                }
            }

        }
    }

    if (showAddPassenger) {

        EditPassengerDialog("",viewModel,
            onDismiss = { showAddPassenger = false
            })

    }

    if (showConfirmation) {

        ConfirmDialogTest(

            onConfirm = {

                val finalDraftWithArrival = shuttlePass.copy(
                    arrival = displayTime(),
                    isLateShuttle = viewModel.isLateShuttle(shuttlePass.tripType)
                )
                finalDraftWithArrival.let {
                    viewModel.finalizeShuttleDraft(
                        driverId = it.driver,
                        shuttlePass = it,
                        passengers = draftPassengerList 
                    )
                }
                viewModel.resetShuttlePassInputState() }
            ,navController = navController,
            onDismiss = {
                showConfirmation = false
            }
        )
    }
}

@Composable
fun PassengerItemTest(viewModel: ShuttleViewModel  = hiltViewModel(), number: Int, item: PassengerQR) {

    var showEditDeleteDialog by remember { mutableStateOf(false) }
    var showEditPassengerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var id by remember { mutableStateOf<String?>("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(top = 12.dp, bottom = 12.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showEditDeleteDialog = true
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AutoResizeText(
            text = "$number.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            colors = Black,
            modifier = Modifier.padding(start = 12.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Outlined.AccountCircle,
            contentDescription = "Profile Picture",
            tint = NavyBlue,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            AutoResizeText(
                text = item.scannedQR,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                colors = Black
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        AutoResizeText(
            text = item.timeIn,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            colors = Black,
            modifier = Modifier.padding(end = 12.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LightGray)
    )

    if (showEditDeleteDialog && item.isDraft) {

        EditDeleteChoiceDialog(
            onEdit = {
                coroutineScope.launch {
                    viewModel.getPassengerId(item.scannedQR, item.shuttlePassId) { result ->
                        try {
                            if(result != null) {
                                id = result
                                showEditPassengerDialog = true
                            }
                            else {
                                Toast.makeText(context, "Failed to retrieve id", Toast.LENGTH_LONG).show()
                            }
                        }
                        catch (ex: Exception) {
                            Log.e("Error", "An error occurred while fetching draft ID: ${ex.message}")
                            Toast.makeText(context, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },

            onDelete = {
                viewModel.deletePassenger(item)
            },
            onDismiss = {
                showEditDeleteDialog = false
            }
        )
    }

    if(showEditPassengerDialog) {

        EditPassengerDialog(
            id = id ?: "",
            viewModel = viewModel,
            onDismiss = {
                showEditPassengerDialog = false
            }
        )
    }
}

@Composable
fun EditPassengerDialog(id: String, viewModel: ShuttleViewModel  = hiltViewModel(), onDismiss: ()-> Unit) {

    val passenger by viewModel.passenger.collectAsState()
    val passengerState by viewModel.qrInputState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(id) {
        viewModel.loadPassenger(id)
    }

    LaunchedEffect(passenger) {

        try {
            if(id != "") {
                viewModel.onPassengerInputEvent(QRInputEvent.ScannedQRChanged(passenger.scannedQR))
            }
            else {
                viewModel.onPassengerInputEvent(QRInputEvent.ScannedQRChanged(""))
            }

        }catch (ex: Exception) {
            Log.e("Error", "An error occurred getting values: ${ex.message}")
            ex.printStackTrace()
        }
    }

    AlertDialog(
        containerColor = White,
        onDismissRequest = {/*onDismiss*/},
        title = { if(id == "") Text(text = "Add Passenger")
        else Text(text = "Edit Passenger")},
        text = {

            Column {

                ModifiedTextField(
                    value = passengerState.qrCode,
                    label = "Passenger QR Code",
                    errorIndicator = passengerState.qrCodeErrorMessage != null,
                    onValueChanged = { viewModel.onPassengerInputEvent(QRInputEvent.ScannedQRChanged(it))}
                )
                if(passengerState.qrCodeErrorMessage != null) {
                    Text(
                        text = passengerState.qrCodeErrorMessage!!,
                        color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },

        confirmButton = {

            Button(modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(NavyBlue),
                onClick = {

                    if(viewModel.submitManualAddPassenger()) {
                        try {
                            val updatedPassenger = PassengerQR(
                                id = id,
                                shuttlePassId = passenger.shuttlePassId,
                                scannedQR = passengerState.qrCode,
                                timeIn = passenger.timeIn,
                                isDraft = passenger.isDraft
                            )
                            viewModel.updatePassenger(updatedPassenger)

                            Toast.makeText(context,"Saved successfully",Toast.LENGTH_SHORT).show()
                            viewModel.resetPassengerInputState()

                        } catch (ex: Exception) {

                            Log.e("Error", "An error occurred while submitting: ${ex.message}")
                            Toast.makeText(context, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    }
                }
            ) {
                Text("Save and Add")
            }
        },
        dismissButton = {
            Button(modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(PaleBlue),
                onClick = {
                    viewModel.resetPassengerInputState()
                    onDismiss()
                }
            ){
                Text("Cancel", color = Black)
            }
        }
    )
}

@Composable
fun ConfirmDialogTest(onConfirm: ()-> Unit, navController: NavController, onDismiss: () -> Unit) {

    val context = LocalContext.current
    AlertDialog(
        containerColor = White,
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirm Completion") },
        text = {
            Text("Are you sure you want to complete the pass?" +
                    " This cannot be undone")
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .clickable { onDismiss() }
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable {

                        onConfirm()
                        onDismiss()
                        navController.navigateUp()
                        Toast.makeText(context, "Shuttle pass complete!", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Text("Yes")
            }

        },
    )
}
