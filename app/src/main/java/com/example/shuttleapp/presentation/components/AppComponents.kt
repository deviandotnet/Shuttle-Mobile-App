@file:Suppress("UNUSED_EXPRESSION")
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.shuttleapp.presentation.components

//AppComponents.kt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shuttleapp.ui.theme.NavyBlue
import com.example.shuttleapp.ui.theme.White
import com.example.shuttleapp.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarComponent(
    openDrawer: () -> Unit = {},
    navController: NavController,
    title: String,
    action: @Composable (RowScope.() -> Unit) ? = {}
) {

    //region we are putting these variables to be put in the top bar or bottom bar components
    val navigationIcon : (@Composable () -> Unit) = {
        //if we are NOT on a wishlist screen, then display the back button
        if(!title.contains("Good day, ")) {
            IconButton(
                onClick = {
                    navController.navigateUp()
                })
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = White,
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
                    tint = White,
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
                color = White,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .heightIn(24.dp),
                textAlign = TextAlign.Center
            )
        }
        else {
            Text(
                text = "Shuttle Mobile App",
                color = White,
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
            containerColor = NavyBlue,
            scrolledContainerColor = NavyBlue,
            navigationIconContentColor = White,
            titleContentColor = White,
            actionIconContentColor = White,
            //subtitleContentColor = White //added
        ),

        )

}


@Composable
fun BottomBarComponent(accountId: String? = "", navController: NavController) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = NavyBlue,
        contentColor = White,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavigationItem(
                icon = Icons.Default.Home,
                label = "Home",
                isActive = true,
                onClick = { /* TODO */ }
            )
            NavigationItem(
                icon = Icons.Default.AddBox,
                label = "New Pass",
                isActive = false,
                onClick = {
                    navController.navigate(Route.AVCurrentPassScreen.route + "/$accountId")
                }
            )
            NavigationItem(
                icon = Icons.Default.History,
                label = "Pass List",
                isActive = false,
                onClick = {
                    navController.navigate(Route.ShuttlePassListScreen.route + "/$accountId")
                }
            )
        }
    }
}

//TODO use this all throughout
@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) White else White.copy(alpha = 0.55f),
            modifier = Modifier.size(if (isActive) 26.dp else 24.dp)
        )
        Text(
            text = label,
            color = if (isActive) White else White.copy(alpha = 0.55f),
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

//@Preview()
//@Composable
//fun Prev() {
//    NavigationItem(icon = Icons.Default.Home, label = "Test", onClick = {})
//}

@Preview(showBackground = true)
@Composable
fun TopBarComponentPreview() {
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
                    tint = White
                )
            }
        }
    )
}