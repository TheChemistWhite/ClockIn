package com.appresenze.presenze.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appresenze.presenze.data.AttendanceViewModel
import com.appresenze.presenze.data.WeekStatus
import com.appresenze.presenze.ui.theme.Accent
import com.appresenze.presenze.ui.theme.CardBorder
import com.appresenze.presenze.ui.theme.ClockedInGreen
import com.appresenze.presenze.ui.theme.DangerSoftBg
import com.appresenze.presenze.ui.theme.NotifBadgeRed
import com.appresenze.presenze.ui.theme.SuccessSoftBg
import com.appresenze.presenze.ui.theme.TextPrimary
import com.appresenze.presenze.ui.theme.TextSecondary50
import com.appresenze.presenze.ui.theme.TextSecondary55
import com.appresenze.presenze.ui.theme.VacationAccent
import com.appresenze.presenze.ui.theme.VacationSoftBg

@Composable
fun RiepilogoScreen(vm: AttendanceViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Riepilogo settimanale",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp),
        )

        val (currentBg, currentBorder) = weekStatusColors(vm.weekStatus)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                label = "Totale ore",
                value = vm.weekTotalLabel,
                modifier = Modifier.weight(1f),
                cardBg = currentBg,
                cardBorder = currentBorder,
            )
            StatCard("Media giornaliera", vm.weekAvgLabel, Modifier.weight(1f))
        }

        Text(
            text = vm.weekDeltaLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = currentBorder,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp),
        )

        val weekHistory = vm.weekHistory

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = if (weekHistory.isNotEmpty()) 12.dp else 88.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                vm.weekBars.forEach { bar ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
                    ) {
                        Text(bar.hoursLabel, fontSize = 11.sp, color = TextSecondary50)
                        Spacer(
                            modifier = Modifier
                                .width(20.dp)
                                .height(bar.barHeightDp.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Accent)
                        )
                        Text(
                            text = bar.label,
                            fontSize = 12.sp,
                            color = TextSecondary55,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        if (weekHistory.isNotEmpty()) {
            Text(
                text = "SETTIMANE PRECEDENTI",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary50,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 10.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                weekHistory.forEach { week ->
                    val (cardBg, cardBorder) = weekStatusColors(week.status)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(week.rangeLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = week.totalLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = cardBorder,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                        Text(
                            text = week.avgLabel,
                            fontSize = 12.sp,
                            color = TextSecondary50,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Maps a week's contract-hours status to (background, border/accent) colors for its card. */
private fun weekStatusColors(status: WeekStatus): Pair<Color, Color> = when (status) {
    WeekStatus.MET -> SuccessSoftBg to ClockedInGreen
    WeekStatus.VACATION -> VacationSoftBg to VacationAccent
    WeekStatus.BELOW -> DangerSoftBg to NotifBadgeRed
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    cardBg: Color = Color.White,
    cardBorder: Color = CardBorder,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary50)
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
