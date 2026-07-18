package com.appresenze.presenze.ui.components

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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.appresenze.presenze.ui.theme.TextPrimary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Popup for "Programma ferie": pick a start date and an end date (or the
 * same day twice for a single vacation day) and hand the range back to the
 * ViewModel, which marks every day in between as VACATION.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacationDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
) {
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = "Programma ferie",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Dal: ${DATE_LABEL_FMT.format(startDate)}")
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Al: ${DATE_LABEL_FMT.format(endDate)}")
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
                        onClick = { onConfirm(startDate, endDate) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(100.dp),
                    ) {
                        Text("Conferma", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        val initialMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        if (endDate.isBefore(startDate)) endDate = startDate
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Annulla") }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showEndPicker) {
        val initialMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        endDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        if (startDate.isAfter(endDate)) startDate = endDate
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Annulla") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

private val DATE_LABEL_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALY)
