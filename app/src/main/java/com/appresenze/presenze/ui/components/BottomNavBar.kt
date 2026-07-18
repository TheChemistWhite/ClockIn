package com.appresenze.presenze.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appresenze.presenze.data.Tab
import com.appresenze.presenze.ui.theme.Accent
import com.appresenze.presenze.ui.theme.AccentPillBg
import com.appresenze.presenze.ui.theme.NavBarBg
import com.appresenze.presenze.ui.theme.NotifBadgeRed
import com.appresenze.presenze.ui.theme.TextSecondary40

@Composable
fun BottomNavBar(
    activeTab: Tab,
    showBadge: Boolean,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(NavBarBg)
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        NavItem(
            label = "Home",
            icon = Icons.Filled.Home,
            active = activeTab == Tab.HOME,
            onClick = { onTabSelected(Tab.HOME) },
            modifier = Modifier.weight(1f),
        )
        NavItem(
            label = "Storico",
            icon = Icons.Filled.DateRange,
            active = activeTab == Tab.STORICO,
            onClick = { onTabSelected(Tab.STORICO) },
            modifier = Modifier.weight(1f),
        )
        NavItem(
            label = "Ore",
            icon = Icons.Filled.BarChart,
            active = activeTab == Tab.RIEPILOGO,
            onClick = { onTabSelected(Tab.RIEPILOGO) },
            modifier = Modifier.weight(1f),
        )
        NavItem(
            label = "Notifiche",
            icon = Icons.Filled.Notifications,
            active = activeTab == Tab.NOTIFICHE,
            onClick = { onTabSelected(Tab.NOTIFICHE) },
            modifier = Modifier.weight(1f),
            showBadge = showBadge,
        )
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
) {
    val color = if (active) Accent else TextSecondary40
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) AccentPillBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-2).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NotifBadgeRed)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }
        if (active) {
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.5.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
