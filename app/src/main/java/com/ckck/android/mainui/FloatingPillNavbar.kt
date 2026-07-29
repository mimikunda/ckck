package com.ckck.android.mainui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ckck.android.models.HomeTab

@Composable
fun FloatingPillNavbar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(16.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            // Sliding Highlight
            val highlightOffset by animateDpAsState(
                targetValue = when (selectedTab) {
                    HomeTab.Home -> 0.dp
                    HomeTab.Map -> 56.dp // Item size (48) + Spacing (8)
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "highlightOffset"
            )

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = highlightOffset.roundToPx(), y = 0) }
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavbarItem(
                    icon = Icons.Default.Home,
                    selected = selectedTab == HomeTab.Home,
                    onClick = { onTabSelected(HomeTab.Home) }
                )
                NavbarItem(
                    icon = Icons.Default.Map,
                    selected = selectedTab == HomeTab.Map,
                    onClick = { onTabSelected(HomeTab.Map) }
                )
            }
        }
    }
}

@Composable
private fun NavbarItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
