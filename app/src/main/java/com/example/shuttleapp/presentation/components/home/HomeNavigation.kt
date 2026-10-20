package com.example.shuttleapp.presentation.components.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** The three top-level destinations. Each is a real screen and shows this bar (or the rail). */
enum class HomeNavItem(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Passes("Passes", Icons.AutoMirrored.Filled.Article),
    Sync("Sync", Icons.Filled.Sync)
}

/** Phone navigation: white bar; the selected item sits in a Mist pill. Sync shows how many passes wait to upload. */
@Composable
fun HomeBottomBar(
    selected: HomeNavItem,
    pendingSyncCount: Int,
    onSelect: (HomeNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    NavigationBar(modifier = modifier, containerColor = colors.surface, tonalElevation = 0.dp) {
        HomeNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = item == selected,
                onClick = { onSelect(item) },
                icon = { NavIcon(item, pendingSyncCount) },
                label = { Text(item.label, style = ShuttleType.NavLabel, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.textPrimary,
                    selectedTextColor = colors.textPrimary,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                    indicatorColor = colors.surfaceQuiet
                )
            )
        }
    }
}

/** Tablet / landscape navigation: the same items as a rail. */
@Composable
fun HomeNavRail(
    selected: HomeNavItem,
    pendingSyncCount: Int,
    onSelect: (HomeNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    NavigationRail(modifier = modifier.fillMaxHeight(), containerColor = colors.surface) {
        Spacer(Modifier.height(Dimens.SpaceL))
        HomeNavItem.entries.forEach { item ->
            NavigationRailItem(
                selected = item == selected,
                onClick = { onSelect(item) },
                icon = { NavIcon(item, pendingSyncCount) },
                label = { Text(item.label, style = ShuttleType.NavLabel, maxLines = 1) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.textPrimary,
                    selectedTextColor = colors.textPrimary,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                    indicatorColor = colors.surfaceQuiet
                )
            )
        }
    }
}

/** App bar for the Passes and Sync tabs: Midnight, no back arrow (the bar is the navigation). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTopBar(title: String, actions: @Composable RowScope.() -> Unit = {}) {
    val colors = ShuttleTheme.colors
    TopAppBar(
        title = { Text(title, style = ShuttleType.AppBar, modifier = Modifier.semantics { heading() }) },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.brandBand,
            titleContentColor = colors.onBrandBand,
            actionIconContentColor = colors.onBrandBand
        )
    )
}

@Composable
private fun NavIcon(item: HomeNavItem, pendingSyncCount: Int) {
    val colors = ShuttleTheme.colors
    val count = if (item == HomeNavItem.Sync) pendingSyncCount else 0
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge(containerColor = colors.warning, contentColor = colors.onSuccess) {
                    val waiting = if (count == 1) "1 pass waiting" else "$count passes waiting"
                    Text(
                        if (count > 99) "99+" else "$count",
                        modifier = Modifier.semantics { contentDescription = waiting }
                    )
                }
            }
        }
    ) {
        Icon(item.icon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

@Preview(widthDp = 360)
@Preview(widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeBottomBarPreview() = ShuttleAppTheme {
    HomeBottomBar(HomeNavItem.Home, pendingSyncCount = 3, onSelect = {})
}

@Preview(widthDp = 360)
@Composable
private fun HomeBottomBarSyncSelectedPreview() = ShuttleAppTheme(darkTheme = false) {
    HomeBottomBar(HomeNavItem.Sync, pendingSyncCount = 0, onSelect = {})
}

@Preview(heightDp = 400)
@Preview(heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeNavRailPreview() = ShuttleAppTheme {
    HomeNavRail(HomeNavItem.Home, pendingSyncCount = 12, onSelect = {})
}
