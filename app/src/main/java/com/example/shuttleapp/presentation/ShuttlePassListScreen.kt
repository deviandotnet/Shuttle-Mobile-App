package com.example.shuttleapp.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.shuttleapp.presentation.components.ActionDropdownMenu
import com.example.shuttleapp.presentation.components.InformationTextDialog
import com.example.shuttleapp.presentation.components.LoadingDialog
import com.example.shuttleapp.presentation.components.StyledSnackBarHost
import com.example.shuttleapp.ui.theme.White
import com.example.shuttleapp.presentation.state.FilterType
import com.example.shuttleapp.presentation.viewmodel.FilterViewModel
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.state.UploadUiState
import com.example.shuttleapp.presentation.viewmodel.ShuttleViewModel
import com.example.shuttleapp.ui.theme.Green
import com.example.shuttleapp.ui.theme.Red
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShuttlePassListScreen(driverId: String,
                          shuttleViewModel: ShuttleViewModel = hiltViewModel(),
                          filterViewModel: FilterViewModel = hiltViewModel(),
                          navController: NavController) {

    val filterState by filterViewModel.filterState.collectAsState()
    val userState by shuttleViewModel.userState.collectAsState() // call the viewmodel to get the user state
    var uploadUiState by remember { mutableStateOf<UploadUiState>(UploadUiState.Idle) }
    var uploadMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }


    LaunchedEffect(driverId) { //with param id para controlled
        filterViewModel.loadShuttleWithPassenger(driverId)
        Log.i("loadShuttleWithPassenger", "load shuttle pass with passenger done")
    }

    Scaffold(
        snackbarHost = { StyledSnackBarHost(hostState = snackBarHostState) },
        topBar = {
            TopBarComponent(
                title = "Completed Shuttle Pass List",
                navController = navController,
                action = {
                    ActionDropdownMenu(
                        onUploadAll = { shuttleViewModel.syncUnsynced(driverId) },
                        onDeleteAll = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(
                                    message = "Delete not authorized",
                                    actionLabel = "Dismiss",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )

                }
            )
        }

    ) { innerPadding ->

        Box(modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .fillMaxSize()) {

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .background(White),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                FilterChoiceSegmentedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    selectedFilter = filterState.currentFilter,
                    onFilterSelected = {filter ->
                        when(filter) {
                            FilterType.All -> {
                                filterViewModel.loadShuttleWithPassenger(driverId)
                            }
                            FilterType.Late -> {
                                filterViewModel.loadShuttleWithPassengerFilterLate(driverId)
                            }
                            FilterType.NotSynced -> {
                                filterViewModel.loadShuttleWithPassengerFilterUnsynced(driverId)

                            }
                        }

                    },
                )

                HorizontalDivider(Modifier.fillMaxWidth())

                when {
                    filterState.list.isEmpty() -> {
                        Spacer(Modifier.weight(0.10f))
                        Text(text = "No items to display",
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.weight(0.90f))
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .background(color = White)
                                .padding(8.dp)
                                .fillMaxWidth())
                        {

                            //display all the shuttle pass items here
                            items(filterState.list.reversed())  {item ->
                                ShuttlePassListItem(item, navController)
                            }


                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                shuttleViewModel.postResult.collect{ postResult ->
                    when (postResult) {
                        is Resource.Error -> {
                            uploadUiState = UploadUiState.Error
                            postResult.message?.let {uploadMessage = it  }
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
                        iconTint = Green
                    )
                }
                is UploadUiState.Error -> {
                    InformationTextDialog(
                        onDismissRequest = { uploadUiState = UploadUiState.Idle },
                        dialogTitle = "Error!",
                        dialogText = uploadMessage,
                        icon = Icons.TwoTone.Cancel,
                        iconTint = Red
                    )
                }
                else -> {}
            }

        }
    }
}

@Composable
private fun FilterChoiceSegmentedButton(
    modifier: Modifier = Modifier,
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit
) {

    val options = listOf(
        "All" to FilterType.All,
        "Late" to FilterType.Late,
        "Not Synced" to FilterType.NotSynced
    )

    val selectedIndex = options.indexOfFirst { it.second == selectedFilter }

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, (label, filterType) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = {
                    onFilterSelected(filterType)
                },
                selected = index == selectedIndex,
                label = { Text(label) }
            )
        }
    }
}