package com.example.shuttleapp.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

enum class HomeNavItem(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Passes("Passes", Icons.AutoMirrored.Filled.Article),
    Profile("Profile", Icons.Filled.Person)
}

/** Phone navigation: white bar of filled icons; the selected item is Midnight with an underline. */
@Composable
fun HomeBottomBar(selected: HomeNavItem, onSelect: (HomeNavItem) -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    NavigationBar(modifier = modifier, containerColor = colors.surface, tonalElevation = 0.dp) {
        HomeNavItem.entries.forEach { item ->
            val isSelected = item == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(item) },
                icon = { NavIcon(item) },
                label = { NavLabel(item, isSelected, underline = true) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

/** Tablet / landscape navigation: the same items as a rail. */
@Composable
fun HomeNavRail(selected: HomeNavItem, onSelect: (HomeNavItem) -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    NavigationRail(modifier = modifier.fillMaxHeight(), containerColor = colors.surface) {
        Spacer(Modifier.height(Dimens.SpaceL))
        HomeNavItem.entries.forEach { item ->
            val isSelected = item == selected
            NavigationRailItem(
                selected = isSelected,
                onClick = { onSelect(item) },
                icon = { NavIcon(item) },
                label = { NavLabel(item, isSelected, underline = false) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun NavIcon(item: HomeNavItem) {
    Icon(item.icon, contentDescription = null, modifier = Modifier.size(26.dp))
}

@Composable
private fun NavLabel(item: HomeNavItem, selected: Boolean, underline: Boolean) {
    val colors = ShuttleTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(item.label, style = ShuttleType.NavLabel, maxLines = 1)
        if (underline) {
            Box(
                Modifier
                    .padding(top = 4.dp)
                    .width(28.dp)
                    .height(3.dp)
                    .background(if (selected) colors.primary else Color.Transparent, RoundedCornerShape(2.dp))
            )
        }
    }
}


@Preview(widthDp = 360)
@Composable
private fun HomeBottomBarPreview() {
    HomeBottomBar(HomeNavItem.Home, onSelect = {})
}

@Preview(heightDp = 400)
@Composable
private fun HomeNavRailPreview() {
    HomeNavRail(HomeNavItem.Home, onSelect = {})
}
