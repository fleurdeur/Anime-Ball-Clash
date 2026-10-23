package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.SportsKabaddi
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppScreen
import com.example.ui.theme.AppTheme

private data class NavTab(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val hubTabs = listOf(
    NavTab(AppScreen.HOME_HUB, "Arena", Icons.Filled.SportsKabaddi, Icons.Outlined.SportsKabaddi, "nav_play"),
    NavTab(AppScreen.CHARACTER_SELECT, "Fighters", Icons.Filled.Groups, Icons.Outlined.Groups, "nav_fighters"),
    NavTab(AppScreen.SHOP, "Recruit", Icons.Filled.Storefront, Icons.Outlined.Storefront, "nav_shop"),
    NavTab(AppScreen.QUESTS, "Missions", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, "nav_missions"),
    NavTab(AppScreen.LEADERBOARD, "Ladder", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_ranks")
)

@Composable
fun HubNavigationBar(
    currentScreen: AppScreen,
    claimableQuestCount: Int = 0,
    onNavigate: (AppScreen) -> Unit
) {
    val palette = AppTheme.palette

    Column {
        HorizontalDivider(thickness = 1.dp, color = palette.border)
        NavigationBar(
            containerColor = palette.cardSurface,
            tonalElevation = 0.dp
        ) {
            hubTabs.forEach { tab ->
                val selected = currentScreen == tab.screen
                NavigationBarItem(
                    selected = selected,
                    onClick = { if (!selected) onNavigate(tab.screen) },
                    modifier = Modifier.testTag(tab.tag),
                    icon = {
                        if (tab.screen == AppScreen.QUESTS && claimableQuestCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = palette.primaryAccent,
                                        contentColor = Color.White
                                    ) {
                                        Text("$claimableQuestCount")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.primaryAccent,
                        selectedTextColor = palette.textPrimary,
                        indicatorColor = palette.elevatedSurface,
                        unselectedIconColor = palette.textMuted,
                        unselectedTextColor = palette.textMuted
                    )
                )
            }
        }
    }
}
