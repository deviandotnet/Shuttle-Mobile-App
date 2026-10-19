@file:Suppress("UNUSED_EXPRESSION")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.shuttleapp.presentation.components

//AppComponents.kt
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shuttleapp.ui.theme.ShuttleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarComponent(
    openDrawer: () -> Unit = {},
    navController: NavController? = null,
    title: String,
    action: @Composable (RowScope.() -> Unit) ? = {},
    onBack: () -> Unit = { navController?.navigateUp() }
) {
    val colors = ShuttleTheme.colors

    //region we are putting these variables to be put in the top bar or bottom bar components
    val navigationIcon : (@Composable () -> Unit) = {
        //if we are NOT on a wishlist screen, then display the back button
        if(!title.contains("Good day, ")) {
            IconButton(
                onClick = onBack)
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = colors.onBrandBand,
                    contentDescription = "Arrow back",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        else {
            IconButton(
                onClick = {
                    openDrawer()
                })
            {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    tint = colors.onBrandBand,
                    contentDescription = "Account",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    val titleString : (@Composable () -> Unit) = {

        if(title.isNotEmpty()) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBrandBand,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .heightIn(24.dp),
                textAlign = TextAlign.Center
            )
        }
        else {
            Text(
                text = "Shuttle Mobile App",
                color = colors.onBrandBand,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .heightIn(24.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    TopAppBar(
        title = titleString,
        navigationIcon = navigationIcon,
        actions = action ?: {},
        colors = TopAppBarColors(
            containerColor = colors.brandBand,
            scrolledContainerColor = colors.brandBand,
            navigationIconContentColor = colors.onBrandBand,
            titleContentColor = colors.onBrandBand,
            actionIconContentColor = colors.onBrandBand,
            //subtitleContentColor = White //added
        ),

        )

}


@Preview(showBackground = true)
@Composable
fun TopBarComponentPreview() {
    val colors = ShuttleTheme.colors
    val navController = rememberNavController()

    TopBarComponent(
        openDrawer = { /* No-op for preview */ },
        navController = navController,
        title = "Preview Title",
        action = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.onBrandBand
                )
            }
        }
    )
}