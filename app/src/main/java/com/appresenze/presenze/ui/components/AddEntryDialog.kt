package com.appresenze.presenze.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.window.Dialog
import com.appresenze.presenze.data.EventType
import com.appresenze.presenze.ui.theme.Accent
import com.appresenze.presenze.ui.theme.CardBorder
import com.appresenze.presenze.ui.theme.TextPrimary
import com.appresenze.presenze.ui.theme.TextSecondary40
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Popup for adding a clock-in/out entry after the fact (e.g. you forgot to
 * tap the button when you actually walked in). Lets you pick the type, the
 * day, and the time, then hands the result back to the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalTime, EventType) -> Unit,
) {
    var type by remember { mutableStateOf(EventType.IN) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = "Aggiungi timbratura",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .background(CardBorder)
                ) {
                    TypeOption("Ingresso", selected = type == EventType.IN, modifier = Modifier.weight(1f)) {
                        type = EventType.IN
                    }
                    TypeOption("Uscita", selected = type == EventType.OUT, modifier = Modifier.weight(1f)) {
                        type = EventType.OUT
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .background(CardBorder)
                ) {
                    TypeOption(
                        "Smart Working",
                        selected = type == EventType.SMART_WORKING,
                        modifier = Modifier.weight(1f),
                    ) {
                        type = EventType.SMART_WORKING
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Data: ${DATE_LABEL_FMT.format(date)}")
                }

                if (type != EventType.SMART_WORKING) {
                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(100.dp),
                    ) {
                        Text("Ora: ${TIME_LABEL_FMT.format(time)}")
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(100.dp),
                    ) {
                        Text("Annulla", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onConfirm(date, time, type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(100.dp),
                    ) {
                        Text("Salva", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                time = LocalTime.of(state.hour, state.minute)
                showTimePicker = false
            },
        ) {
            TimePicker(state = state)
        }
    }
}

@Composable
private fun TypeOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) Accent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextSecondary40,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}

/**
 * Material3 ships DatePickerDialog but, as of this compose-bom, no equivalent
 * for TimePicker -- this small wrapper follows the same shape by hand.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Annulla") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

private val DATE_LABEL_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALY)
private val TIME_LABEL_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALY)
