package com.example.shuttleapp.presentation//package com.example.shuttle.view.screen


import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.shuttleapp.presentation.components.InformationTextDialog
import com.example.shuttleapp.presentation.components.LoadingDialog
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.presentation.state.UploadUiState
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.formatMonthYear
import com.example.shuttleapp.presentation.events.QRInputEvent
import com.example.shuttleapp.Route
import com.example.shuttleapp.ui.theme.ShuttleTheme

import kotlinx.coroutines.launch
import java.sql.Time


@Composable
fun ShuttlePassItemScreen(id: String, viewModel: ShuttleViewModel, navController: NavController) {
    val colors = ShuttleTheme.colors

    val shuttlePassWithPassengers by viewModel.shuttlePassWithPassengers.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val forUpdateShuttlePass by viewModel.forUpdateShuttlePass.collectAsState()

    var uploadUiState by remember { mutableStateOf<UploadUiState>(UploadUiState.Idle) }
    var uploadMessage by remember { mutableStateOf("") }

    LaunchedEffect(id) {
        viewModel.getShuttlePassWithPassengersById(id)
        viewModel.loadShuttlePassById(id)
    }

    shuttlePassWithPassengers.data?.let { shuttlePassenger ->
        Scaffold(
            topBar = {
                TopBarComponent(
                    navController = navController,
                    title = "SPT-${formatMonthYear(shuttlePassenger.shuttlePass.dateCreated)}-${shuttlePassenger.shuttlePass.id.takeLast(12).uppercase()}",
                    action = {


                        IconButton(
                            onClick = { viewModel.syncPass(shuttlePassenger.shuttlePass.id) },
                            enabled = !shuttlePassenger.shuttlePass.isSynced
                        ) {
                            Icon(
                                imageVector = if(!shuttlePassenger.shuttlePass.isSynced) Icons.Outlined.Upload else Icons.TwoTone.CheckCircle,
                                tint = if(!shuttlePassenger.shuttlePass.isSynced) colors.onBrandBand else colors.success,
                                contentDescription = "Upload button")
                        }

                        LaunchedEffect(Unit) {
                            //collect the response
                            viewModel.postResult.collect { postResult ->
                                when (postResult) {
                                    is Resource.Error -> {
                                        uploadUiState = UploadUiState.Error
                                        postResult.message?.let { uploadMessage = it }
                                    }
                                    is Resource.Loading -> {
                                        if (postResult.isLoading) {
                                            uploadUiState = UploadUiState.Loading
                                        }
                                    }
                                    is Resource.Success -> {
                                        uploadUiState = UploadUiState.Success
                                        postResult.data?.message?.let { uploadMessage = it }
                                    }

                                }
                            }

                        }

                        when (uploadUiState) {
                            is UploadUiState.Loading -> LoadingDialog()
                            is UploadUiState.Success -> {
                                InformationTextDialog(
                                    onDismissRequest = { uploadUiState = UploadUiState.Idle },
                                    dialogTitle = "Success!",
                                    dialogText = uploadMessage,
                                    icon = Icons.TwoTone.CheckCircle,
                                    iconTint = colors.success
                                )
                            }
                            is UploadUiState.Error -> {
                                InformationTextDialog(
                                    onDismissRequest = { uploadUiState = UploadUiState.Idle },
                                    dialogTitle = "Error!",
                                    dialogText = uploadMessage,
                                    icon = Icons.TwoTone.Cancel,
                                    iconTint = colors.error
                                )
                            }
                            else -> {}
                        }

                    }
                )
            })
        { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                Column {
                    val pass = shuttlePassenger.shuttlePass
                    val nameState by viewModel.toNameState.collectAsState()
                    val qrInputState by viewModel.qrInputState.collectAsState()

                    LaunchedEffect(pass) {
                        viewModel.getRouteNameById(pass.routeId)
                        viewModel.getPlateNumberByShuttleId(pass.plateNumber)
                        viewModel.getProviderNameById(pass.provider)
                        viewModel.getDriverNameById(pass.driver)
                    }

                    // A finalized pass is read-only: the form hides its draft-only actions.
                    ShuttlePassFormTest(
                        shuttlePass = pass,
                        draftPassengerList = shuttlePassenger.passengersQR,
                        nameState = nameState,
                        qrInputState = qrInputState,
                        onAddPassenger = { navController.navigate(Route.QrScannerScreen.route + "/${pass.id}") },
                        onRecordDeparture = { viewModel.recordDeparture(pass) },
                        onComplete = {
                            viewModel.completeShuttlePass(pass, shuttlePassenger.passengersQR)
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
@Preview(showBackground = true)
fun Prev(){
    val colors = ShuttleTheme.colors
    InformationTextDialog(
        onDismissRequest = {},
        dialogTitle = "Success",
        dialogText = "The shuttle pass has been uploaded to the server.",
        icon = Icons.Outlined.CheckCircleOutline,
        iconTint = colors.success
    )
}



//@Composable
//fun ExpandedDropDown(label: String, itemsList: List<String>,value: String, errorIndicator: Boolean, onValueChanged: (String) -> Unit) {
//
//    var isExpanded by remember { mutableStateOf(false) }
//
//    ExposedDropdownMenuBox(
//        expanded = isExpanded,
//        onExpandedChange = { isExpanded = !isExpanded }
//    ) {
//        OutlinedTextField(
//            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
//            readOnly = true,
//            value = value,
//            onValueChange = {newValue ->
//                onValueChanged(newValue)
//            },
//            label = { Text(label, color = Black) },
//            enabled = errorIndicator,
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)},
//            colors = OutlinedTextFieldDefaults.colors(),
//            keyboardOptions = KeyboardOptions(
//                keyboardType = KeyboardType.Text,
//                imeAction = ImeAction.Next),
//        )
//
//        ExposedDropdownMenu(modifier = Modifier.exposedDropdownSize().fillMaxWidth().heightIn(max = 230.dp).background(color = White),
//            scrollState = rememberScrollState(), expanded = isExpanded, onDismissRequest = {isExpanded = false} //<--
//        ) {
//            if(itemsList.isNotEmpty()) {
//                itemsList.forEach { item ->
//                    DropdownMenuItem(
//                        text = { Text(item, fontSize = 16.sp, color = Black) },
//                        onClick = {
//                            onValueChanged(item)
//                            isExpanded = false
//                        }
//                    )
//                }
//            } else {
//                Text("Empty. Having trouble while getting values", fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
//
//            }
//
//        }
//    }
//
//
//    AnimatedVisibility(visible = isExpanded) { // <-- only show when text field is clicked
//        Card(
//            modifier = Modifier
//                .padding(horizontal = 5.dp)
//                .width(textFieldSize.width.dp),
//            elevation = CardDefaults.cardElevation(10.dp),
//            colors = CardDefaults.cardColors(containerColor = White, contentColor = Black)
//        ) {
//            LazyColumn(
//                modifier = Modifier.heightIn(max = 150.dp)
//            ) {
//                if (value.isNotEmpty()) {
//                    items(
//                        categories.filter {
//                            it.lowercase()
//                                .contains(value.lowercase())
//                        }.sorted()
//                    ) { item ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(12.dp)
//                                .clickable {
//                                    value = item
//                                    isExpanded = false
//                                    onValueChanged(item)
//                                }
//                        ) {
//                            Text(text = item, fontSize = 16.sp)
//                        }
//                    }
//                } else {
//                    items(categories.sorted()) { item ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(12.dp)
//                                .clickable {
//                                    value = item
//                                    isExpanded = false
//                                    onValueChanged(item)
//                                }
//                        ) {
//                            Text(text = item, fontSize = 16.sp)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//}