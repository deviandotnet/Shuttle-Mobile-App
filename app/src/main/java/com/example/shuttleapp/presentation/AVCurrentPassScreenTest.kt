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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.presentation.components.EditDeleteChoiceDialog
import com.example.shuttleapp.presentation.components.ModifiedTextField
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.events.QRInputEvent
import com.example.shuttleapp.presentation.state.IdNameState
import com.example.shuttleapp.presentation.state.QRInputState
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
    viewModel: ShuttleViewModel
) {
    val colors = ShuttleTheme.colors

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
            // Keep the pass form's driver/provider in sync with the signed-in driver.
            viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.DriverChanged(userData.accountId))
            userData.providerId?.let { providerId ->
                viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.ShuttleProviderChanged(providerId))
            }
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
                                tint = colors.onBrandBand,
                                contentDescription = "edit icon"
                            )
                        }

                        IconButton(onClick = {
                            showDeleteConfirmation = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                tint = colors.onBrandBand,
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
            .background(color = colors.canvas)) {

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
                            color = colors.textSecondary)
                        Text(
                            text = "Complete the fields to create new shuttle pass",
                            fontStyle = FontStyle.Italic,
                            color = colors.textSecondary)

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
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                            elevation = ButtonDefaults.buttonElevation(8.dp))
                        {
                            Text("Create new shuttle pass", color = colors.onPrimary)
                        }
                    }
                }

                //load drafted shuttle pass
                else {
                    val draftPass = draftedShuttlePassState.data!!
                    val nameState by viewModel.toNameState.collectAsState()
                    val qrInputState by viewModel.qrInputState.collectAsState()

                    LaunchedEffect(draftPass) {
                        viewModel.getRouteNameById(draftPass.routeId)
                        viewModel.getPlateNumberByShuttleId(draftPass.plateNumber)
                        viewModel.getProviderNameById(draftPass.provider)
                        viewModel.getDriverNameById(draftPass.driver)
                    }

                    ShuttlePassFormTest(
                        shuttlePass = draftPass,
                        draftPassengerList = draftPassengerListState.list,
                        nameState = nameState,
                        qrInputState = qrInputState,
                        onAddPassenger = { navController.navigate(Route.QrScannerScreen.route + "/${draftPass.id}") },
                        onRecordDeparture = { viewModel.recordDeparture(draftPass) },
                        onComplete = {
                            viewModel.completeShuttlePass(draftPass, draftPassengerListState.list)
                            navController.navigateUp()
                        },
                        onDeletePassenger = viewModel::deletePassenger,
                        onStartEditPassenger = viewModel::startEditingPassenger,
                        onEditQrChanged = { viewModel.onPassengerInputEvent(QRInputEvent.ScannedQRChanged(it)) },
                        onSaveEditedPassenger = viewModel::saveEditedPassenger,
                        onCancelEditPassenger = viewModel::resetPassengerInputState
                    )
                }
            }
        }

    }
}

@Composable
fun ShuttlePassFormTest(
    shuttlePass: ShuttlePass,
    draftPassengerList: List<PassengerQR>,
    nameState: IdNameState,
    qrInputState: QRInputState,
    onAddPassenger: () -> Unit,
    onRecordDeparture: () -> Unit,
    onComplete: () -> Unit,
    onDeletePassenger: (PassengerQR) -> Unit,
    onStartEditPassenger: (PassengerQR) -> Unit,
    onEditQrChanged: (String) -> Unit,
    onSaveEditedPassenger: (PassengerQR) -> Boolean,
    onCancelEditPassenger: () -> Unit
) {
    val colors = ShuttleTheme.colors

    var showConfirmation by remember { mutableStateOf(false) }
    var editingPassenger by remember { mutableStateOf<PassengerQR?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(snackbarHost = { StyledSnackBarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .background(color = colors.canvas),

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
                    .background(colors.divider)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(colors.canvas)
            ) {
                PassengerListContent(
                    passengers = draftPassengerList,
                    onEdit = { passenger ->
                        onStartEditPassenger(passenger)
                        editingPassenger = passenger
                    },
                    onDelete = onDeletePassenger
                )
            }

            if(shuttlePass.isDraft) {
                PassActionButtons(
                    hasDeparted = shuttlePass.departure.isNotEmpty(),
                    canComplete = draftPassengerList.isNotEmpty() && shuttlePass.departure != "",
                    onAddPassenger = onAddPassenger,
                    onRecordDeparture = {
                        onRecordDeparture()
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar(
                                message = "Departure Recorded",
                                actionLabel = "Dismiss",
                                withDismissAction = true,
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    onEnd = { showConfirmation = true }
                )
            }

        }
    }

    editingPassenger?.let { passenger ->
        EditPassengerDialog(
            qrInputState = qrInputState,
            onQrChanged = onEditQrChanged,
            onSave = {
                if (onSaveEditedPassenger(passenger)) {
                    editingPassenger = null
                    Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
                }
            },
            onCancel = {
                onCancelEditPassenger()
                editingPassenger = null
            }
        )
    }

    if (showConfirmation) {
        ConfirmDialogTest(
            onConfirm = onComplete,
            onDismiss = {
                showConfirmation = false
            }
        )
    }
}

@Composable
private fun PassengerListContent(
    passengers: List<PassengerQR>,
    onEdit: (PassengerQR) -> Unit,
    onDelete: (PassengerQR) -> Unit
) {
    val colors = ShuttleTheme.colors
    if (passengers.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.canvas),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "No passengers to display.", color = colors.textSecondary)
            Text(text = "Click + to add", color = colors.textSecondary)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(passengers.reversed()) { index, passenger ->
                PassengerItemTest(
                    number = index + 1,
                    item = passenger,
                    onEdit = { onEdit(passenger) },
                    onDelete = { onDelete(passenger) }
                )
            }
        }
    }
}

@Composable
private fun PassActionButtons(
    hasDeparted: Boolean,
    canComplete: Boolean,
    onAddPassenger: () -> Unit,
    onRecordDeparture: () -> Unit,
    onEnd: () -> Unit
) {
    val colors = ShuttleTheme.colors
    Column(
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            elevation = ButtonDefaults.buttonElevation(4.dp),
            onClick = onAddPassenger
        ) {
            Icon(
                contentDescription = "add icon", tint = colors.onPrimary,
                imageVector = Icons.Rounded.AddCircle
            )

            Text("Add passenger", color = colors.onPrimary)
        }

        if (!hasDeparted) {
            Button(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.success, contentColor = colors.onSuccess),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                onClick = onRecordDeparture
            ) {
                Text("Start Departure", color = colors.onSuccess)
            }
        } else {
            Button(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.success, contentColor = colors.onSuccess),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                enabled = canComplete,
                onClick = onEnd
            ) {
                Text("End Shuttle Pass Record", color = colors.onSuccess)
            }
        }
    }
}

@Composable
fun PassengerItemTest(number: Int, item: PassengerQR, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = ShuttleTheme.colors

    var showEditDeleteDialog by remember { mutableStateOf(false) }

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
            colors = colors.textPrimary,
            modifier = Modifier.padding(start = 12.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Outlined.AccountCircle,
            contentDescription = "Profile Picture",
            tint = colors.primary,
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
                colors = colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        AutoResizeText(
            text = item.timeIn,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            colors = colors.textPrimary,
            modifier = Modifier.padding(end = 12.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.divider)
    )

    if (showEditDeleteDialog && item.isDraft) {
        EditDeleteChoiceDialog(
            onEdit = onEdit,
            onDelete = onDelete,
            onDismiss = {
                showEditDeleteDialog = false
            }
        )
    }
}

@Composable
fun EditPassengerDialog(
    qrInputState: QRInputState,
    onQrChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = ShuttleTheme.colors
    AlertDialog(
        containerColor = colors.surface,
        onDismissRequest = {},
        title = { Text(text = "Edit Passenger") },
        text = {
            Column {
                ModifiedTextField(
                    value = qrInputState.qrCode,
                    label = "Passenger QR Code",
                    errorIndicator = qrInputState.qrCodeErrorMessage != null,
                    onValueChanged = onQrChanged
                )
                qrInputState.qrCodeErrorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                onClick = onSave
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceQuiet, contentColor = colors.textPrimary),
                onClick = onCancel
            ) {
                Text("Cancel", color = colors.textPrimary)
            }
        }
    )
}

@Composable
fun ConfirmDialogTest(onConfirm: ()-> Unit, onDismiss: () -> Unit) {
    val colors = ShuttleTheme.colors

    val context = LocalContext.current
    AlertDialog(
        containerColor = colors.surface,
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
                        onDismiss()
                        onConfirm()
                        Toast.makeText(context, "Shuttle pass complete!", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Text("Yes")
            }

        },
    )
}
