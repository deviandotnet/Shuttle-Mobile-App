package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.ui.theme.ShuttleTheme
//SelectableDropDown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedDropDown(label: String, categories: List<String>, initialValue: String, errorIndicator: Boolean, onValueChanged: (String) -> Unit) {
    val colors = ShuttleTheme.colors

    var isExpanded by remember { mutableStateOf(false) }

    var value by remember { mutableStateOf(initialValue) }

    LaunchedEffect(initialValue) {
        if (value != initialValue) {
            value = initialValue
        }
    }

    val filtered = if(value.isNotBlank()) {
        categories.filter { item ->
            item.contains(value, ignoreCase = true)
        }.sorted()
    } else {
        categories.sorted()
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
            //readOnly = true,
            value = value,
            onValueChange = {newValue ->
                value = newValue
                onValueChanged(newValue)
            },
            label = {
                Text( text = label) },
            textStyle = TextStyle(
                color = colors.textPrimary,
                fontSize = 16.sp
            ),
            trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next),
            isError = errorIndicator,
            singleLine = true
        )

        ExposedDropdownMenu(modifier = Modifier.exposedDropdownSize().fillMaxWidth().heightIn(max = 230.dp).background(color = colors.surface),
            scrollState = rememberScrollState(), expanded = isExpanded, onDismissRequest = {isExpanded = false} //<--
        ) {
            if(categories.isNotEmpty()) {
                if(filtered.isEmpty()) {
                    DropdownMenuItem(
                        onClick = {
                            value = "No items found"
                            onValueChanged("")
                            isExpanded = false
                        },
                        text = {
                            Text(
                                text = "No items found",
                                style = TextStyle(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 16.sp)
                            )
                        },
                        enabled = false
                    )
                }
                else{
                    filtered.forEach {item ->
                        DropdownMenuItem(
                            onClick = {
                                value = item
                                onValueChanged(item)
                                isExpanded = false
                            },
                            text = {
                                Text(
                                    text = item, fontSize = 16.sp
                                )
                            }
                        )
                    }
                }
            }
            else {
                Text("Empty. Having trouble while getting values", fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
