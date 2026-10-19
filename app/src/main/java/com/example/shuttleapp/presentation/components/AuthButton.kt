package com.example.shuttleapp.presentation.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.ShuttleTheme

@Composable
fun AuthButton(text: String,
               backgroundColor: Color,
               contentColor: Color,
               enabled: Boolean = true,
               onClick: () -> Unit,
               isLoading: Boolean,
               modifier: Modifier = Modifier
) {
    Button(modifier = modifier,
        onClick = {
            onClick()
        },
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(

            disabledContentColor = backgroundColor,
            contentColor = contentColor,
            containerColor = backgroundColor
        ),
        enabled = enabled

    ) {
        if(isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        else {
            Text(text = text,
                style = MaterialTheme.typography.bodyMedium)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val colors = ShuttleTheme.colors
    AuthButton(
        text = "Login",
        backgroundColor = colors.primary,
        contentColor = colors.onPrimary,
        enabled = true,
        onClick = {},
        isLoading = false,
        modifier = Modifier.fillMaxWidth()
    )
}