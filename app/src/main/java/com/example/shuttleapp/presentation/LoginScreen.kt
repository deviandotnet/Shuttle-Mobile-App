package com.example.shuttleapp.presentation

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.shuttleapp.R
import com.example.shuttleapp.Route
import com.example.shuttleapp.data.network.request.LoginRequestBodyDto
import com.example.shuttleapp.presentation.components.AuthButton
import com.example.shuttleapp.presentation.components.HeaderBackground
import com.example.shuttleapp.presentation.components.TextEntryModule
import com.example.shuttleapp.presentation.events.LoginInputEvent
import com.example.shuttleapp.presentation.viewmodel.LoginViewModel
import com.example.shuttleapp.ui.theme.*

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {

    val loginState by loginViewModel.loginState.collectAsState()

    HeaderBackground(leftColor = NavyBlue, rightColor = Black, modifier = Modifier.fillMaxSize())
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally)
    {

        Spacer(Modifier.height(32.dp))
        Image(
            painter = painterResource(R.drawable.shuttle_vector),
            contentDescription = "Test",
            modifier = Modifier
                .size(height = 100.dp, width = 200.dp)
                .padding(end = 20.dp),
            colorFilter = ColorFilter.tint(White),
            contentScale = ContentScale.FillWidth,
        )
        Text("SHUTTLE MOBILE APP", style = TextStyle(color = White, fontWeight = FontWeight.Bold, fontSize = 26.sp))
        Spacer(Modifier.weight(0.30f))
        Text("Login your account", color = NavyBlue, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        LoginContainer(
            modifier = Modifier
                .padding() //adjust the X-position of the container
                .fillMaxWidth(0.9f)
                .shadow(5.dp, RoundedCornerShape(16.dp))
                .background(
                    White, RoundedCornerShape(16.dp)
                )
                .padding(10.dp, 16.dp, 10.dp, 5.dp)
                .align(Alignment.CenterHorizontally),

            usernameValue = {
                loginState.username
            },
            onUsernameChanged = {
                loginViewModel.onLoginInputEvent(LoginInputEvent.UsernameChanged(it))
                loginViewModel.resetLoginValidationMessage()
            },
            usernameErrorMessage = { loginState.usernameErrorMessage },
            passwordValue = {
                loginState.password
            },
            onPasswordChanged = {
                loginViewModel.onLoginInputEvent(LoginInputEvent.PasswordChanged(it))
                loginViewModel.resetLoginValidationMessage()
            },
            passwordErrorMessage = { loginState.passwordErrorMessage },
            onLoginButtonClicked = {

                if (loginViewModel.submitLogin()) {
                    try {
                        val postLoginCredentials = LoginRequestBodyDto(
                            username = loginState.username,
                            password = loginState.password
                        )
                        loginViewModel.postLogin(postLoginCredentials)

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Log.e("postLogin", ex.message.toString())
                    }
                }


            },
            loginErrorMessage = { loginState.loginErrorMessage },
            isPasswordShown = {
                loginState.isPasswordShown
            },
            onTrailingPasswordIconClick = {
                loginViewModel.onLoginInputEvent(
                    LoginInputEvent.PasswordShown(
                        !loginState.isPasswordShown
                    )
                )
            },
            isLoading = { loginState.isLoading },
            onForgotPasswordClicked = {
                //TODO forgot password link
            }
        )
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("No account yet?",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Register",
                modifier = Modifier
                    .padding(5.dp)
                    .clickable {
                        navController.navigate(Route.RegisterScreen.route) {
                            popUpTo(0)
                        }
                    },
                color = NavyBlue,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodyMedium
            )

        }
        Spacer(Modifier.weight(0.70f))

        LaunchedEffect(loginState) {

            if (loginState.userData != null && !loginState.isLoading) {
                // The session was already saved by the repository; it persists until logout.
                navController.navigate(Route.HomeScreen.route + "/${loginState.userData!!.accountId}") {
                    popUpTo(Route.LoginScreen.route) { inclusive = true } // so that no back button can function once success (no backstack)
                    launchSingleTop = true
                }
            }

            if(loginState.isSuccessfullyLoggedIn) {
                loginViewModel.resetLoginState()

                //dito ka mag popost para sa login account info
            }
        }

    }

}

@Composable
fun LoginContainer(
    usernameValue: () -> String, //lambda that returns string
    onUsernameChanged: (String) -> Unit,
    usernameErrorMessage: () -> String?,
    passwordValue: () -> String,
    onPasswordChanged:(String) -> Unit,
    passwordErrorMessage: () -> String?,
    onLoginButtonClicked: () -> Unit,
    loginErrorMessage: () -> String?,
    isPasswordShown:() ->Boolean,
    onTrailingPasswordIconClick: ()-> Unit,
    isLoading: ()-> Boolean,
    onForgotPasswordClicked: ()-> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
        //Text(usernameErrorMessage() ?: "", color = MaterialTheme.colors.error)

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
        //Text(passwordErrorMessage() ?: "", color = MaterialTheme.colors.error)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            loginErrorMessage()?.let {
                Text(
                    text = it,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                    )
                )
            }

            AuthButton(
                text = "Log in",
                backgroundColor = NavyBlue,
                contentColor = White,
                enabled = !isLoading(),
                onClick = onLoginButtonClicked,
                isLoading = isLoading(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .shadow(5.dp, RoundedCornerShape(25.dp))
                    .padding(bottom = 6.dp)
            )

            Text(
                text = "Forgot password?",
                style = TextStyle(
                    color = NavyBlue,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {

                        onForgotPasswordClicked()
                    }
            )
        }
    }
}

@Composable()
fun Test() {
    HeaderBackground(leftColor = NavyBlue, rightColor = Black, modifier = Modifier.fillMaxSize())
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.height(32.dp))
        Image(
            painter = painterResource(R.drawable.shuttle_vector),
            contentDescription = "Test",
            modifier = Modifier
                .size(height = 100.dp, width = 200.dp)
                .padding(end = 20.dp),
            colorFilter = ColorFilter.tint(White),
            contentScale = ContentScale.FillWidth,
        )
        Text("SHUTTLE MOBILE APP", style = TextStyle(color = White, fontWeight = FontWeight.Bold, fontSize = 26.sp))
        Spacer(Modifier.weight(.20f))
        Text("Login your account")
        LoginContainer(
            usernameValue = { "Test" },
            onUsernameChanged = { },
            usernameErrorMessage = { null },
            passwordValue = { "TODO()" },
            onPasswordChanged = { },
            passwordErrorMessage = { null },
            onLoginButtonClicked = { },
            loginErrorMessage = { null },
            isPasswordShown = { false },
            onTrailingPasswordIconClick = { },
            isLoading = { false },
            modifier = Modifier,
            onForgotPasswordClicked = {}
        )
        Spacer(Modifier.weight(.80f))


    }


}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun Preview2() {

    Test()
}