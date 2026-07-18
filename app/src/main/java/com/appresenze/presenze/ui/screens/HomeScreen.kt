package com.appresenze.presenze.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appresenze.presenze.data.AttendanceEvent
import com.appresenze.presenze.data.AttendanceViewModel
import com.appresenze.presenze.data.EventType
import com.appresenze.presenze.export.PresenzeExporter
import com.appresenze.presenze.ui.components.AddEntryDialog
import com.appresenze.presenze.ui.components.VacationDialog
import com.appresenze.presenze.ui.components.dashedBorder
import com.appresenze.presenze.ui.theme.Accent
import com.appresenze.presenze.ui.theme.AccentSoftBg
import com.appresenze.presenze.ui.theme.CardBorder
import com.appresenze.presenze.ui.theme.ClockedInGreen
import com.appresenze.presenze.ui.theme.DashedBorder
import com.appresenze.presenze.ui.theme.NotifBadgeRed
import com.appresenze.presenze.ui.theme.SecondaryDotGray
import com.appresenze.presenze.ui.theme.SmartWorkingAccent
import com.appresenze.presenze.ui.theme.TextPrimary
import com.appresenze.presenze.ui.theme.TextSecondary50
import com.appresenze.presenze.ui.theme.TextSecondary55
import com.appresenze.presenze.ui.theme.VacationAccent

@Composable
fun HomeScreen(vm: AttendanceViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var showExportMenu by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showVacationDialog by remember { mutableStateOf(false) }
    var pendingDeleteEvent by remember { mutableStateOf<AttendanceEvent?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingImportUri = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header: date + greeting, quick actions, avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = vm.todayDateLabel,
                    fontSize = 13.sp,
                    color = TextSecondary55,
                )
                Text(
                    text = "Ciao, Valerio",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.openAddSheet() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Aggiungi timbratura", tint = TextPrimary)
                }
                Box {
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Importa/Esporta presenze", tint = TextPrimary)
                    }
                    DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Importa da Excel") },
                            leadingIcon = { Icon(Icons.Filled.FileUpload, contentDescription = null) },
                            onClick = {
                                showExportMenu = false
                                importLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/octet-stream",
                                    )
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Esporta in Excel") },
                            leadingIcon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                            onClick = {
                                showExportMenu = false
                                val uri = vm.exportToXlsx(context)
                                if (uri != null) {
                                    Toast.makeText(context, "File esportato in Download", Toast.LENGTH_LONG).show()
                                    context.startActivity(
                                        Intent.createChooser(PresenzeExporter.shareIntent(uri), "Condividi presenze")
                                    )
                                } else {
                                    Toast.makeText(context, "Nessun dato da esportare", Toast.LENGTH_LONG).show()
                                }
                            },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentSoftBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("V", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        // Big clock in/out button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val bgColor = if (vm.clockedIn) ClockedInGreen else Accent
            val rotation by animateFloatAsState(if (vm.clockedIn) 180f else 0f, label = "clockIconRotation")

            Box(
                modifier = Modifier
                    .size(176.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = Accent.copy(alpha = 0.25f),
                        spotColor = Accent.copy(alpha = 0.25f),
                    )
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { vm.mainButtonClick() },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(34.dp)
                            .graphicsLayer { rotationZ = rotation },
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (vm.clockedIn) "Timbra uscita" else "Timbra entrata",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = vm.statusLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(14.dp))
            if (vm.smartWorkingToday) {
                TextButton(onClick = { vm.unmarkTodaySmartWorking() }) {
                    Text("Annulla Smart Working", color = TextSecondary50, fontSize = 13.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { vm.markTodaySmartWorking() },
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        tint = SmartWorkingAccent,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Segna oggi come Smart Working", color = SmartWorkingAccent, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showVacationDialog = true },
                shape = RoundedCornerShape(100.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.BeachAccess,
                    contentDescription = null,
                    tint = VacationAccent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Programma ferie", color = VacationAccent, fontSize = 13.sp)
            }
        }

        // "Oggi" section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 88.dp)
        ) {
            Text(
                text = "OGGI",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary50,
                modifier = Modifier.padding(bottom = 10.dp),
            )

            val events = vm.todayEventsDisplay
            if (events.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    events.forEachIndexed { index, ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (ev.type == EventType.IN) Accent else SecondaryDotGray)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = if (ev.type == EventType.IN) "Ingresso" else "Uscita",
                                fontSize = 14.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = ev.time,
                                fontSize = 14.sp,
                                color = TextSecondary55,
                            )
                            Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(NotifBadgeRed)
                                    .clickable { pendingDeleteEvent = ev },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Elimina timbratura",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }
                        if (index < events.lastIndex) {
                            HorizontalDivider(color = CardBorder, thickness = 1.dp)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dashedBorder(DashedBorder, cornerRadius = 16.dp)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nessuna timbratura ancora oggi",
                        fontSize = 13.sp,
                        color = TextSecondary50,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    if (vm.addSheetVisible) {
        AddEntryDialog(
            onDismiss = { vm.closeAddSheet() },
            onConfirm = { date, time, type ->
                if (type == EventType.SMART_WORKING) {
                    vm.markSmartWorkingDay(date)
                } else {
                    vm.addManualRecord(date, time, type)
                }
            },
        )
    }

    if (showVacationDialog) {
        VacationDialog(
            onDismiss = { showVacationDialog = false },
            onConfirm = { start, end ->
                vm.markVacationRange(start, end)
                showVacationDialog = false
            },
        )
    }

    val manualEntryError = vm.manualEntryError
    if (manualEntryError != null) {
        AlertDialog(
            onDismissRequest = { vm.dismissManualEntryError() },
            title = { Text("Non è possibile timbrare") },
            text = { Text(manualEntryError) },
            confirmButton = {
                TextButton(onClick = { vm.dismissManualEntryError() }) {
                    Text("OK")
                }
            },
        )
    }

    val deleteEvent = pendingDeleteEvent
    if (deleteEvent != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteEvent = null },
            title = { Text("Eliminare questa timbratura?") },
            text = {
                val typeLabel = if (deleteEvent.type == EventType.IN) "di ingresso" else "di uscita"
                Text("Verrà eliminata la timbratura $typeLabel delle ${deleteEvent.time}. L'operazione non è reversibile.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteRecord(deleteEvent.dateTime, deleteEvent.type)
                    pendingDeleteEvent = null
                }) {
                    Text("Elimina", color = NotifBadgeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEvent = null }) {
                    Text("Annulla")
                }
            },
        )
    }

    val importUri = pendingImportUri
    if (importUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Importare da Excel?") },
            text = {
                Text("Questo sostituirà tutte le presenze attualmente salvate nell'app con quelle del file selezionato.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val count = vm.importFromXlsx(context, importUri)
                    pendingImportUri = null
                    if (count != null) {
                        Toast.makeText(context, "Importate $count timbrature", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Impossibile leggere il file selezionato", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Importa")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("Annulla")
                }
            },
        )
    }
}
