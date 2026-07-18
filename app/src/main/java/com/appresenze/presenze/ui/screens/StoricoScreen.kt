package com.appresenze.presenze.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import com.appresenze.presenze.data.AttendanceViewModel
import com.appresenze.presenze.data.DayKind
import com.appresenze.presenze.ui.theme.Accent
import com.appresenze.presenze.ui.theme.AccentSoftBg
import com.appresenze.presenze.ui.theme.CardBorder
import com.appresenze.presenze.ui.theme.ClockedInGreen
import com.appresenze.presenze.ui.theme.NotifBadgeRed
import com.appresenze.presenze.ui.theme.SmartWorkingAccent
import com.appresenze.presenze.ui.theme.SmartWorkingSoftBg
import com.appresenze.presenze.ui.theme.SuccessSoftBg
import com.appresenze.presenze.ui.theme.TextPrimary
import com.appresenze.presenze.ui.theme.TextSecondary50
import com.appresenze.presenze.ui.theme.VacationAccent
import com.appresenze.presenze.ui.theme.VacationSoftBg
import java.time.LocalDate

@Composable
fun StoricoScreen(vm: AttendanceViewModel, modifier: Modifier = Modifier) {
    var pendingDeleteDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingDeleteLabel by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Storico presenze",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            vm.history.forEach { day ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = day.date,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )

                        val pillColor = when (day.kind) {
                            DayKind.SMART_WORKING -> SmartWorkingAccent
                            DayKind.VACATION -> VacationAccent
                            DayKind.NORMAL -> if (day.dailyTargetMet) ClockedInGreen else Accent
                        }
                        val pillBg = when (day.kind) {
                            DayKind.SMART_WORKING -> SmartWorkingSoftBg
                            DayKind.VACATION -> VacationSoftBg
                            DayKind.NORMAL -> if (day.dailyTargetMet) SuccessSoftBg else AccentSoftBg
                        }
                        Text(
                            text = day.ore,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = pillColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(pillBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )

                        Spacer(Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(NotifBadgeRed)
                                .clickable {
                                    pendingDeleteDate = day.rawDate
                                    pendingDeleteLabel = day.date
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Elimina timbrature del giorno",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    when (day.kind) {
                        DayKind.SMART_WORKING -> {
                            Row(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = null,
                                    tint = SmartWorkingAccent,
                                    modifier = Modifier.padding(end = 6.dp),
                                )
                                Text(
                                    text = "Smart working",
                                    fontSize = 14.sp,
                                    color = SmartWorkingAccent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        DayKind.VACATION -> {
                            Row(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BeachAccess,
                                    contentDescription = null,
                                    tint = VacationAccent,
                                    modifier = Modifier.padding(end = 6.dp),
                                )
                                Text(
                                    text = "Ferie",
                                    fontSize = 14.sp,
                                    color = VacationAccent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        DayKind.NORMAL -> {
                            Column {
                                Row(
                                    modifier = Modifier.padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                                ) {
                                    Column {
                                        Text(
                                            text = "ENTRATA",
                                            fontSize = 11.sp,
                                            color = TextSecondary50,
                                        )
                                        Text(
                                            text = day.entrata,
                                            fontSize = 15.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "USCITA",
                                            fontSize = 11.sp,
                                            color = TextSecondary50,
                                        )
                                        Text(
                                            text = day.uscita,
                                            fontSize = 15.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                }
                                val surplus = day.surplusLabel
                                if (surplus != null) {
                                    Text(
                                        text = "$surplus rispetto alle 8h",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ClockedInGreen,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val deleteDate = pendingDeleteDate
    if (deleteDate != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteDate = null },
            title = { Text("Eliminare questa giornata?") },
            text = {
                Text("Verranno eliminate tutte le timbrature di $pendingDeleteLabel. L'operazione non è reversibile.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteDay(deleteDate)
                    pendingDeleteDate = null
                }) {
                    Text("Elimina", color = NotifBadgeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteDate = null }) {
                    Text("Annulla")
                }
            },
        )
    }
}
