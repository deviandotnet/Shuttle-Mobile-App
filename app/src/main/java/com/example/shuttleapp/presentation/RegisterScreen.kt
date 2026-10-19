package com.example.shuttleapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shuttleapp.Route
import com.example.shuttleapp.domain.model.ShuttleProvider
import com.example.shuttleapp.presentation.components.AuthButton
import com.example.shuttleapp.presentation.components.HeaderBackground
import com.example.shuttleapp.presentation.components.TextDropDown
import com.example.shuttleapp.presentation.components.TextEntryModule
import com.example.shuttleapp.presentation.events.RegisterInputEvent
import com.example.shuttleapp.presentation.viewmodel.RegisterViewModel
import com.example.shuttleapp.presentation.components.InformationTextDialog
import com.example.shuttleapp.ui.theme.Black
import com.example.shuttleapp.ui.theme.Green
import com.example.shuttleapp.ui.theme.NavyBlue
import com.example.shuttleapp.ui.theme.White

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel,
    navController: NavController

) {
    var showSuccessRegDialog by remember { mutableStateOf(false) }
    val shuttleProviders by registerViewModel.shuttleProviderState.collectAsState()
    val registerState by registerViewModel.registerState.collectAsState()


    HeaderBackground(leftColor = NavyBlue, rightColor = Black, modifier = Modifier.fillMaxSize(), height = 3f)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth(0.9f)) {
                Text(
                    "Create account",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            RegisterContainer(
                firstNameValue = {registerState.firstName},
                onFirstNameChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.FirstnameChanged(it))
                    registerViewModel.resetFirstNameErrorMessage()
                },
                firstNameErrorMessage = {registerState.firstNameErrorMessage},

                middleNameValue = { registerState.middleName },
                onMiddleNameChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.MiddlenameChanged(it))
                    registerViewModel.resetMiddleNameErrorMessage()
                },
                middleNameErrorMessage = {registerState.middleNameErrorMessage},

                lastNameValue = { registerState.lastName },
                onLastNameChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.LastnameChanged(it))
                    registerViewModel.resetLastNameErrorMessage()
                },
                lastNameErrorMessage = {registerState.lastNameErrorMessage},

                providerValue = {registerState.provider},
                onProviderChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.ProviderChanged(it))
                    registerViewModel.resetProviderErrorMessage()
                },
                providerErrorMessage = {registerState.providerErrorMessage},

                empNumberValue = {registerState.employeeNumber},
                onEmpNumberChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.EmployeeNumberChanged(it))
                    registerViewModel.resetEmpNumErrorMessage()
                },
                empNumberErrorMessage = {registerState.employeeNumberErrorMessage},

                usernameValue = { registerState.userName },
                onUsernameChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.UsernameChanged(it))
                    registerViewModel.resetUserNameErrorMessage()
                },
                usernameErrorMessage = { registerState.userNameErrorMessage },

                passwordValue = {registerState.password},
                onPasswordChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.PasswordChanged(it))
                    registerViewModel.resetPasswordErrorMessage()
                },
                passwordErrorMessage = {registerState.passwordErrorMessage},

                rePasswordValue = {registerState.repeatPassword},
                onRePasswordChanged = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.RePasswordChanged(it))
                    registerViewModel.resetRePasswordErrorMessage()
                },
                rePasswordErrorMessage = {registerState.repeatPasswordErrorMessage},

                onRegisterButtonClicked = {
                    // The ViewModel validates the form and builds the registration from its own state.
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.Submit)
                },
                registerErrorMessage = {registerState.registerErrorMessage},
                isPasswordShown = {registerState.isPasswordShown},
                onTrailingPasswordIconClick = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.PasswordShown(
                        !registerState.isPasswordShown))
                },
                isRePasswordShown = {registerState.isRepeatPasswordShown},
                onTrailingRePasswordIconClick = {
                    registerViewModel.onRegisterInputEvent(RegisterInputEvent.RePasswordShown(
                        !registerState.isRepeatPasswordShown))
                },
                isLoading = {registerState.isLoading},

                list = {
                    shuttleProviders.list
                },
                modifier = Modifier
                    .padding() //adjust the X-position of the container
                    .fillMaxWidth(0.9f)
                    .shadow(5.dp, RoundedCornerShape(16.dp))
                    .background(
                        White, RoundedCornerShape(16.dp)
                    )
                    .padding(10.dp, 16.dp, 10.dp, 5.dp)

            )
        }

        item {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material.Text(
                    "Already had an account?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Login",
                    modifier = Modifier
                        .padding(5.dp)
                        .clickable {
                            registerViewModel.resetRegisterState()
                            navController.navigate(Route.LoginScreen.route) {
                                popUpTo(0)
                            }
                        },
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }

    }

    if(registerState.isSuccessfullyRegistered) {
        showSuccessRegDialog= true
    }

    if(showSuccessRegDialog) {
        InformationTextDialog(
            onDismissRequest = {
                showSuccessRegDialog = false

                navController.navigate(Route.LoginScreen.route) {
                    popUpTo(0)
                }
                registerViewModel.resetRegisterState()
            },
            dialogTitle = "Success!",
            dialogText = "Your account has been registered.",
            icon = Icons.Outlined.CheckCircleOutline,
            iconTint = Green
        )
    }

}

@Composable
fun RegisterContainer(
    firstNameValue: () -> String,
    onFirstNameChanged: (String) -> Unit,
    firstNameErrorMessage: () -> String?,

    middleNameValue: () -> String,
    onMiddleNameChanged: (String) -> Unit,
    middleNameErrorMessage: () -> String?,

    lastNameValue: () -> String,
    onLastNameChanged: (String) -> Unit,
    lastNameErrorMessage: () -> String?,

    providerValue: () -> String,
    onProviderChanged: (String) -> Unit,
    providerErrorMessage: () -> String?,

    empNumberValue: () -> String,
    onEmpNumberChanged: (String) -> Unit,
    empNumberErrorMessage: () -> String?,

    usernameValue: () -> String, //lambda that returns string
    onUsernameChanged: (String) -> Unit,
    usernameErrorMessage: () -> String?,

    passwordValue: () -> String,
    onPasswordChanged:(String) -> Unit,
    passwordErrorMessage: () -> String?,

    rePasswordValue: () -> String,
    onRePasswordChanged:(String) -> Unit,
    rePasswordErrorMessage: () -> String?,

    onRegisterButtonClicked: () -> Unit,
    registerErrorMessage: () -> String?,
    isPasswordShown:() ->Boolean,
    onTrailingPasswordIconClick: ()-> Unit,
    isRePasswordShown:() -> Boolean,
    onTrailingRePasswordIconClick: ()-> Unit,
    isLoading: ()-> Boolean,

    list: () -> List<ShuttleProvider>?,

    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextEntryModule(
            modifier = Modifier.fillMaxWidth(),
            description = "First Name",
            hint = "Enter first name",
            textValue = firstNameValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onFirstNameChanged,
            trailingIcon = null,
            onTrailingIconClicked = null,
            leadingIcon = Icons.Default.Abc,
            imeAction = ImeAction.Next
        )
        firstNameErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        TextEntryModule(
            modifier = Modifier.fillMaxWidth(),
            description = "Middle Name",
            hint = "Enter middle name",
            textValue = middleNameValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onMiddleNameChanged,
            trailingIcon = null,
            onTrailingIconClicked = null,
            leadingIcon = Icons.Default.Abc,
            imeAction = ImeAction.Next
        )
        middleNameErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        TextEntryModule(
            modifier = Modifier.fillMaxWidth(),
            description = "Last Name",
            hint = "Enter last name",
            textValue = lastNameValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onLastNameChanged,
            trailingIcon = null,
            onTrailingIconClicked = null,
            leadingIcon = Icons.Default.Abc,
            imeAction = ImeAction.Next
        )
        lastNameErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }

        TextDropDown(
            modifier = Modifier.fillMaxWidth(),
            description = "Shuttle Provider",
            hint = "Select shuttle provider",
            textValue = providerValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onProviderChanged,
            onTrailingIconClicked = null,
            leadingIcon = Icons.Default.DirectionsBus,
            listItems = list(),
            imeAction = ImeAction.Next
        )
        providerErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }

        TextEntryModule(
            modifier = Modifier.fillMaxWidth(),
            description = "Employee Number",
            hint = "Enter employee number",
            textValue = empNumberValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onEmpNumberChanged,
            trailingIcon = null,
            onTrailingIconClicked = null,
            leadingIcon = Icons.Default.Numbers,
            imeAction = ImeAction.Next
        )
        empNumberErrorMessage()?.let { Text(it,style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }

        TextEntryModule(
            modifier = Modifier.fillMaxWidth(),
            description = "Username",
            hint = "Enter username",
            textValue = usernameValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onUsernameChanged,
            trailingIcon = null,
            onTrailingIconClicked = null,
            leadingIcon = Icons.Default.Person,
            imeAction = ImeAction.Next

        )
        usernameErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }

        TextEntryModule(
            modifier = Modifier.fillMaxWidth(),
            description = "Password",
            hint = "Enter password",
            textValue = passwordValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onPasswordChanged,
            trailingIcon = if(isPasswordShown()) Icons.Rounded.RemoveRedEye else Icons.Rounded.VisibilityOff,
            onTrailingIconClicked = {
                onTrailingPasswordIconClick()
            },
            leadingIcon = Icons.Default.VpnKey,
            visualTransformation = if(isPasswordShown()) {
                VisualTransformation.None
            }else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
        passwordErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }

        TextEntryModule(
            modifier = Modifier.fillMaxWidth(),
            description = "Repeat Password",
            hint = "Re-enter password",
            textValue = rePasswordValue(),
            textColor = NavyBlue,
            cursorColor = NavyBlue,
            onValueChanged = onRePasswordChanged,
            trailingIcon = if(isRePasswordShown()) Icons.Rounded.RemoveRedEye else Icons.Rounded.VisibilityOff,
            onTrailingIconClicked = {
                onTrailingRePasswordIconClick()
            },
            leadingIcon = Icons.Default.VpnKey,
            visualTransformation = if(isRePasswordShown()) {
                VisualTransformation.None
            }else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
        rePasswordErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            AuthButton(
                text = "Register",
                backgroundColor = NavyBlue,
                contentColor = White,
                enabled = !isLoading(),
                onClick = onRegisterButtonClicked,
                isLoading = isLoading(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .shadow(5.dp, RoundedCornerShape(25.dp))
                    .padding(bottom = 6.dp)
            )

            registerErrorMessage()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }

        }
    }
}
