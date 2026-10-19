package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.domain.model.ShuttleProvider
import com.example.shuttleapp.ui.theme.ShuttleTheme

@Composable
fun TextEntryModule(
    description: String,
    hint: String,
    leadingIcon: ImageVector,
    textValue: String,
    keyboardType: KeyboardType = KeyboardType.Ascii,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textColor: Color,
    cursorColor: Color,
    onValueChanged: (String) -> Unit,
    trailingIcon: ImageVector? = null,
    onTrailingIconClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    readOnly: Boolean = false
) {
    val colors = ShuttleTheme.colors
    Column(
        modifier = modifier,
    ) {
        Text(
            text = description,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
        TextField(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).border(0.5.dp, textColor, RoundedCornerShape(25.dp)).height(50.dp).shadow(4.dp, RoundedCornerShape(25.dp)),
            value = textValue,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = colors.surface,
                textColor = textColor,
                placeholderColor = colors.textSecondary,
                cursorColor = cursorColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedLabelColor = Color.Transparent
            ),
            onValueChange = onValueChanged,
            shape = RoundedCornerShape(25.dp),
            singleLine = true,
            readOnly = readOnly,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = cursorColor
                )
            },
            trailingIcon = {
                if(trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = cursorColor,
                        modifier = Modifier.clickable {
                            if(onTrailingIconClicked != null) onTrailingIconClicked()
                        }
                    )
                }
            },
            placeholder = {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType, imeAction = imeAction),
            visualTransformation = visualTransformation,

            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDropDown(description: String,
                 hint: String,
                 leadingIcon: ImageVector,
                 textValue: String,
                 listItems: List<ShuttleProvider>?,
                 keyboardType: KeyboardType = KeyboardType.Ascii,
                 trailingIcon: ImageVector? = null,
                 visualTransformation: VisualTransformation = VisualTransformation.None,
                 textColor: Color,
                 cursorColor: Color,
                 onValueChanged: (String) -> Unit,
                 onTrailingIconClicked: (() -> Unit)?,
                 modifier: Modifier = Modifier,
                 imeAction: ImeAction = ImeAction.Next,
                 readOnly: Boolean = false
) {
    val colors = ShuttleTheme.colors

    var isExpanded by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var selectedItemName by remember { mutableStateOf<String?>(null) }
    var value by remember { mutableStateOf(textValue) }


    Column(
        modifier = modifier,
    ) {
        Text(
            text = description,
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {

            TextField(
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                    .padding(top = 4.dp).border(0.5.dp, textColor, RoundedCornerShape(25.dp))
                    .height(50.dp)
                    .shadow(4.dp, RoundedCornerShape(25.dp)),
                value = value,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colors.surface,
                    textColor = textColor,
                    placeholderColor = colors.textSecondary,
                    cursorColor = cursorColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedLabelColor = Color.Transparent
                ),
                onValueChange = { newValue ->
                    value = newValue
                    // onValueChanged(newValue) //No longer needed for filtering
                },
                shape = RoundedCornerShape(25.dp),
                singleLine = true,
                readOnly = readOnly,
                leadingIcon = {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = cursorColor
                    )
                },
                trailingIcon = {
                    if(trailingIcon != null) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = cursorColor,
                            modifier = Modifier.clickable {
                                if(onTrailingIconClicked != null) onTrailingIconClicked()
                            }
                        )
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                placeholder = {
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType, imeAction = imeAction),
                visualTransformation = visualTransformation,
            )

            ExposedDropdownMenu(modifier = Modifier.exposedDropdownSize().fillMaxWidth().heightIn(max = 230.dp).background(color = colors.surface),
                scrollState = rememberScrollState(), expanded = isExpanded, onDismissRequest = {isExpanded = false} //<--
            ) {
                listItems?.let {
                    if(it.isNotEmpty()) {
                        listItems.forEach {item ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedItemId = item.shuttleProviderId
                                    selectedItemName = item.providerName
                                    value = item.providerName
                                    onValueChanged(selectedItemId ?: "")
                                    isExpanded = false
                                },
                                text = {
                                    Text(text = item.providerName, fontSize = 13.sp)
                                }
                            )
                        }
                    } else {
                        Text(
                            "Empty. Having trouble while getting values",
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewText() {
    val colors = ShuttleTheme.colors

    TextDropDown(
        description = "Shuttle Provider",
        hint = "Select shuttle provider",
        leadingIcon = Icons.Default.DirectionsBus,
        textValue = "", //"123123"
        textColor = colors.textPrimary,
        cursorColor = colors.primary,
        onValueChanged = {},
        onTrailingIconClicked = {},
        visualTransformation = PasswordVisualTransformation(),
        listItems = null
    )
}