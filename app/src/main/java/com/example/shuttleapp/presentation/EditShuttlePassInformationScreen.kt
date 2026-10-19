package com.example.shuttleapp.presentation

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.presentation.components.ExpandedDropDown
import com.example.shuttleapp.ui.theme.Black
import com.example.shuttleapp.ui.theme.Gray
import com.example.shuttleapp.ui.theme.NavyBlue
import com.example.shuttleapp.ui.theme.White
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.events.ShuttlePassInputEvent
import com.example.shuttleapp.presentation.events.ShuttlePassUiEvent
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.util.formatTimeHHmm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditShuttlePassInformationScreen(id: String, viewModel: ShuttleViewModel, navController: NavController) {

    val context = LocalContext.current
    val routesState by viewModel.routeState.collectAsState()
    val editedShuttlePass by viewModel.editedShuttlePassState.collectAsState()
    val shuttlePassInputState by viewModel.shuttlePassInputState.collectAsState()
    val shuttleState by viewModel.shuttleState.collectAsState()
    val nameState by viewModel.toNameState.collectAsState()

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var editingTimeType by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // 1. Initial Load: Fetch the entity and populate state
    LaunchedEffect(id) {
        if(id.isNotEmpty()) {
            viewModel.loadEditedShuttlePass(id)
        }
    }

    // 2. Listen for UI Events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when(event) {
                is ShuttlePassUiEvent.NavigateUp -> navController.navigateUp()
                is ShuttlePassUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .padding(WindowInsets.ime.asPaddingValues())
            .consumeWindowInsets(WindowInsets.ime),
        topBar = {
            TopBarComponent(
                title = "Edit Shuttle Pass",
                navController = navController
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = White)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Section 1: Schedule Information
            SectionHeader(title = "Schedule Information", icon = Icons.Default.Schedule)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoRow(
                        icon = Icons.Default.DateRange,
                        label = "Date",
                        value = shuttlePassInputState.date,
                        enabled = false,
                        onClick = { /* showDateDialog = true */ }
                    )
                    HorizontalDivider(color = Gray.copy(alpha = 0.3f))
                    InfoRow(
                        icon = Icons.Default.AccessTime,
                        label = "Departure",
                        value = if(shuttlePassInputState.departure.isEmpty()) "Not Started" else formatTimeHHmm(shuttlePassInputState.departure),
                        enabled = shuttlePassInputState.departure.isNotEmpty(),
                        onClick = { 
                            editingTimeType = "Departure"
                            showTimePickerDialog = true 
                        }
                    )
                    HorizontalDivider(color = Gray.copy(alpha = 0.3f))
                    InfoRow(
                        icon = Icons.Default.AccessTime,
                        label = "Arrival",
                        value = if(shuttlePassInputState.arrival.isEmpty()) "In Progress" else formatTimeHHmm(shuttlePassInputState.arrival),
                        enabled = shuttlePassInputState.arrival.isNotEmpty(),
                        onClick = { 
                            editingTimeType = "Arrival"
                            showTimePickerDialog = true 
                        }
                    )
                }
            }

            // Section 2: Shuttle Details
            SectionHeader(title = "Shuttle Configuration", icon = Icons.Default.Info)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExpandedDropDown(
                    label = "Plate Number",
                    categories = shuttleState.plateNumbers,
                    initialValue = shuttlePassInputState.plateNumber,
                    errorIndicator = shuttlePassInputState.plateNumberErrorMessage != null,
                    onValueChanged = {
                        viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.PlateNumberChanged(it))
                    },
                )

                ExpandedDropDown(
                    label = "Route",
                    categories = routesState.routes,
                    initialValue = shuttlePassInputState.route,
                    errorIndicator = shuttlePassInputState.routeErrorMessage != null,
                    onValueChanged = {
                        viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.RouteChanged(it))
                    },
                )

                ExpandedDropDown(
                    label = "Trip Type",
                    categories = listOf("Incoming", "Outgoing"),
                    initialValue = shuttlePassInputState.tripType,
                    errorIndicator = shuttlePassInputState.tripTypeErrorMessage != null,
                    onValueChanged = {
                        viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.TripTypeChanged(it))
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(NavyBlue),
                onClick = {
                    viewModel.onUpdateShuttlePass(editedShuttlePass)
                }) {
                Text("Update Shuttle Pass", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showTimePickerDialog) {
            InputTimeDialog(
                title = "Select $editingTimeType Time",
                onConfirm = { time -> 
                    if (editingTimeType == "Departure") {
                        viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.DepartureChanged(time))
                    } else if (editingTimeType == "Arrival") {
                        viewModel.onShuttlePassInputEvent(ShuttlePassInputEvent.ArrivalChanged(time))
                    }
                    showTimePickerDialog = false 
                }, 
                onDismiss = { showTimePickerDialog = false }
            )
        }

        if (showDateDialog) {
            InputDateDialog(onDateSelected = {}, onDismiss = { showDateDialog = false })
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NavyBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = NavyBlue,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentAlpha = if (enabled) 1f else 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .alpha(contentAlpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            tint = NavyBlue,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, color = NavyBlue, fontSize = 12.sp)
            Text(text = value, color = Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTimeDialog(
    title: String = "Select Departure Time",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    AlertDialog(
        containerColor = White,
        onDismissRequest = onDismiss,
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                TimeInput(state = timePickerState)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = NavyBlue) }
        },
        confirmButton = {
            TextButton(onClick = {
                val hour = timePickerState.hour
                val minutes = timePickerState.minute
                val timeToString = String.format("%02d:%02d", hour, minutes)
                onConfirm(timeToString)
                onDismiss()
            }) {
                Text("Confirm", color = NavyBlue)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputDateDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit) {

    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK", color = NavyBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NavyBlue)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
