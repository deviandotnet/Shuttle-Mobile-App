package com.example.shuttleapp.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.R
import com.example.shuttleapp.ui.theme.ShuttleTheme

//SnackBar

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun StyledSnackBarHost(hostState: SnackbarHostState) {
    val colors = ShuttleTheme.colors
    SnackbarHost(hostState = hostState) { snackBarData ->
        Snackbar(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.brandBand),
            contentColor = colors.onBrandBand,
            containerColor = colors.brandBand,
            action = {
                snackBarData.visuals.actionLabel?.let { label ->
                    TextButton(onClick = {
                        snackBarData.dismiss()
                        //snackBarData.performAction()
                    }
                    ) {
                        Text(text = label, style = TextStyle(color = colors.onBrandBand, textDecoration = TextDecoration.Underline))
                    }
                }
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {

                Icon(painter = painterResource(R.drawable.shuttle_vector),
                    contentDescription = null,
                    tint = colors.onBrandBand,
                    modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = snackBarData.visuals.message,
                    style = TextStyle(fontWeight = FontWeight.Medium)
                )
            }
        }

    }
}

// for action-packed snackbar
//                                val result = snackbarHostState.showSnackbar(
//                                    message = "Something happened",
//                                    actionLabel = "Dismiss",
//                                    duration = SnackbarDuration.Indefinite
//                                )
//
//                                when (result) {
//                                    SnackbarResult.ActionPerformed -> {
//                                        // User clicked "Dismiss"
//                                    }
//                                    SnackbarResult.Dismissed -> {
//                                        // Snackbar timed out or was swiped away
//                                    }
//                                }

@Preview()
@Composable
private fun SnackPreview() {
    val hostState = remember { SnackbarHostState() }
    StyledSnackBarHost(hostState)
}