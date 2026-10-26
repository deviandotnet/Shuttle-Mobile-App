package com.example.shuttleapp.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.presentation.components.ConfirmBasicDialog
import com.example.shuttleapp.presentation.components.CtaPlusIcon
import com.example.shuttleapp.presentation.components.ObserveAsEvents
import com.example.shuttleapp.presentation.components.PrimaryCtaButton
import com.example.shuttleapp.presentation.components.StyledSnackBarHost
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.components.pass.CreatePassForm
import com.example.shuttleapp.presentation.components.pass.CreatePassIntro
import com.example.shuttleapp.presentation.components.pass.CreatePassPreview
import com.example.shuttleapp.presentation.components.pass.EditPassengerDialog
import com.example.shuttleapp.presentation.components.pass.PassActionBar
import com.example.shuttleapp.presentation.components.pass.PassDetailsList
import com.example.shuttleapp.presentation.events.CurrentPassAction
import com.example.shuttleapp.presentation.events.CurrentPassEvent
import com.example.shuttleapp.presentation.state.CreatePassFormUi
import com.example.shuttleapp.presentation.state.CurrentPassMode
import com.example.shuttleapp.presentation.state.CurrentPassState
import com.example.shuttleapp.presentation.viewmodel.CurrentPassViewModel
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType
import kotlinx.coroutines.launch

/** Wires [CurrentPassViewModel] to the screen: collects state and turns one-time events into navigation and messages. */
@Composable
fun CurrentPassRoot(
    viewModel: CurrentPassViewModel,
    onBack: () -> Unit,
    onOpenScanner: (passId: String) -> Unit,
    onEditPass: (passId: String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CurrentPassEvent.NavigateBack -> {
                // A snackbar would leave with this screen, so confirmations that end it use a toast.
                event.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                onBack()
            }
            is CurrentPassEvent.NavigateToScanner -> onOpenScanner(event.passId)
            is CurrentPassEvent.NavigateToEditPass -> onEditPass(event.passId)
            is CurrentPassEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message, actionLabel = "Dismiss", duration = SnackbarDuration.Short)
            }
        }
    }

    CurrentPassScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
fun CurrentPassScreen(
    state: CurrentPassState,
    snackbarHostState: SnackbarHostState,
    onAction: (CurrentPassAction) -> Unit
) {
    val colors = ShuttleTheme.colors
    val mode = state.mode
    Scaffold(
        containerColor = colors.canvas,
        snackbarHost = { StyledSnackBarHost(snackbarHostState) },
        topBar = {
            TopBarComponent(
                title = if (mode is CurrentPassMode.Active) "Shuttle pass" else "New shuttle pass",
                onBack = { onAction(CurrentPassAction.Back) },
                action = { if (mode is CurrentPassMode.Active) ActivePassMenu(onAction) }
            )
        },
        bottomBar = {
            when (mode) {
                is CurrentPassMode.Active -> PassActionBar(
                    action = mode.action,
                    onAddPassenger = { onAction(CurrentPassAction.AddPassenger) },
                    onStartDeparture = { onAction(CurrentPassAction.StartDeparture) },
                    onComplete = { onAction(CurrentPassAction.CompletePass) }
                )
                CurrentPassMode.Create -> PrimaryCtaButton(
                    label = if (state.isSubmitting) "Creating…" else "Create shuttle pass",
                    onClick = { onAction(CurrentPassAction.CreatePass) },
                    leading = { CtaPlusIcon() },
                    modifier = Modifier.navigationBarsPadding().padding(Dimens.SpaceM)
                )
                CurrentPassMode.Loading -> Unit
            }
        }
    ) { innerPadding ->
        when (mode) {
            CurrentPassMode.Loading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            CurrentPassMode.Create -> CreatePassContent(state.form, onAction, innerPadding)
            is CurrentPassMode.Active -> PassDetailsList(
                card = mode.card,
                passengers = mode.passengers,
                onEditPassenger = { onAction(CurrentPassAction.EditPassenger(it)) },
                onDeletePassenger = { onAction(CurrentPassAction.DeletePassenger(it)) },
                contentPadding = innerPadding
            )
        }
    }

    state.editingPassenger?.let { editing ->
        EditPassengerDialog(
            editing = editing,
            onQrChanged = { onAction(CurrentPassAction.EditedQrChanged(it)) },
            onSave = { onAction(CurrentPassAction.SaveEditedPassenger) },
            onCancel = { onAction(CurrentPassAction.CancelEditPassenger) }
        )
    }
    if (state.showDeleteConfirm && mode is CurrentPassMode.Active) {
        val count = mode.passengers.size
        ConfirmBasicDialog(
            content = "Delete this draft" + (if (count > 0) " and its $count scanned passenger${if (count == 1) "" else "s"}" else "") +
                "? This can't be undone.",
            onDismiss = { onAction(CurrentPassAction.DismissDeletePass) },
            onConfirm = { onAction(CurrentPassAction.ConfirmDeletePass) }
        )
    }
}

@Composable
private fun ActivePassMenu(onAction: (CurrentPassAction) -> Unit) {
    val colors = ShuttleTheme.colors
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { onAction(CurrentPassAction.EditPass) }) {
        Icon(Icons.Filled.Edit, contentDescription = "Edit pass details", tint = colors.onBrandBand)
    }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = colors.onBrandBand)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = colors.surface) {
            DropdownMenuItem(
                text = { Text("Delete pass", style = ShuttleType.Body, color = colors.error) },
                onClick = {
                    expanded = false
                    onAction(CurrentPassAction.RequestDeletePass)
                }
            )
        }
    }
}

@Composable
private fun CreatePassContent(form: CreatePassFormUi, onAction: (CurrentPassAction) -> Unit, innerPadding: PaddingValues) {
    val colors = ShuttleTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(Modifier.fillMaxWidth().background(colors.brandBand)) {
            CreatePassIntro(Modifier.padding(top = Dimens.SpaceXs, bottom = Dimens.SpaceL))
        }
        Box {
            Box(Modifier.fillMaxWidth().height(40.dp).background(colors.brandBand))
            CreatePassForm(
                form = form,
                onRouteChanged = { onAction(CurrentPassAction.RouteChanged(it)) },
                onTripTypeSelected = { onAction(CurrentPassAction.TripTypeSelected(it)) },
                onPlateNumberChanged = { onAction(CurrentPassAction.PlateNumberChanged(it)) },
                modifier = Modifier.padding(horizontal = Dimens.SpaceM)
            )
        }
        CreatePassPreview(
            title = form.previewTitle,
            subtitle = form.previewSubtitle,
            modifier = Modifier.padding(Dimens.SpaceM)
        )
    }
}
