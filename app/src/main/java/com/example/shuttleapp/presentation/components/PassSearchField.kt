package com.example.shuttleapp.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** White search box that sits on the brand band; shows a clear button once something is typed. */
@Composable
fun PassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search pass ID or route"
) {
    val colors = ShuttleTheme.colors
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(Dimens.RadiusButton)
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouch)
            .shadow(elevation = 2.dp, shape = shape),
        singleLine = true,
        textStyle = ShuttleType.BodyLarge,
        placeholder = { Text(placeholder, style = ShuttleType.BodyLarge, color = colors.textSecondary) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colors.textSecondary) },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = colors.textSecondary)
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.primary,
            focusedIndicatorColor = colors.surface,
            unfocusedIndicatorColor = colors.surface
        )
    )
}

@Preview(widthDp = 360)
@Preview(widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PassSearchFieldPreview() = ShuttleAppTheme {
    Box(Modifier.background(ShuttleTheme.colors.brandBand).padding(Dimens.SpaceM)) {
        PassSearchField(value = "", onValueChange = {})
    }
}

@Preview(widthDp = 360)
@Composable
private fun PassSearchFieldFilledPreview() = ShuttleAppTheme(darkTheme = false) {
    Box(Modifier.background(ShuttleTheme.colors.brandBand).padding(Dimens.SpaceM)) {
        PassSearchField(value = "Mamatid", onValueChange = {})
    }
}
