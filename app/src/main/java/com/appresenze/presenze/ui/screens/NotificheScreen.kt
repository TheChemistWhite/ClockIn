package com.appresenze.presenze.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.appresenze.presenze.data.AttendanceViewModel
import com.appresenze.presenze.ui.components.dashedBorder
import com.appresenze.presenze.ui.theme.Accent
import com.appresenze.presenze.ui.theme.AccentSoftBg
import com.appresenze.presenze.ui.theme.CardBorder
import com.appresenze.presenze.ui.theme.DashedBorder
import com.appresenze.presenze.ui.theme.TextPrimary
import com.appresenze.presenze.ui.theme.TextSecondary45
import com.appresenze.presenze.ui.theme.TextSecondary50
import com.appresenze.presenze.ui.theme.TextSecondary60

@Composable
fun NotificheScreen(vm: AttendanceViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Notifiche",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
        )

        if (vm.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 88.dp)
                    .dashedBorder(DashedBorder, cornerRadius = 16.dp)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nessuna notifica al momento",
                    fontSize = 13.sp,
                    color = TextSecondary50,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            vm.notifications.forEach { n ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentSoftBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = n.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Text(
                                text = n.time,
                                fontSize = 12.sp,
                                color = TextSecondary45,
                            )
                        }
                        Text(
                            text = n.body,
                            fontSize = 13.sp,
                            color = TextSecondary60,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
            }
        }
        }
    }
}
