package com.appresenze.presenze.data

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appresenze.presenze.export.PresenzeExporter
import com.appresenze.presenze.export.PresenzeImporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Single source of truth for all clock-in/out data: live timbrature, manual
 * additions from [AddEntryDialog], and everything derived from them (today's
 * status, the Storico list, the weekly bar chart, and the .xlsx export).
 *
 * `now` is kept as observable state and re-read by every "live" computation
 * below (instead of calling LocalDateTime.now() inline) so Compose actually
 * recomposes on the 30s ticker rather than freezing at first composition.
 */
class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val store = RecordStore(File(application.filesDir, RecordStore.FILE_NAME))
    private val recordsStore = mutableStateListOf<AttendanceRecord>()

    var activeTab by mutableStateOf(Tab.HOME)
        private set

    var pendingType by mutableStateOf<EventType?>(null)
        private set

    var addSheetVisible by mutableStateOf(false)
        private set

    /** Set when a manual entry is rejected (duplicate ingresso/uscita for that day); null otherwise. */
    var manualEntryError by mutableStateOf<String?>(null)
        private set

    private var now by mutableStateOf(System.currentTimeMillis())

    /** Equivalent to the design-tool's `requireConfirm` prop (default false). */
    val requireConfirm: Boolean = false

    /** Weekly hours required by contract; drives the green/red highlight in Riepilogo. */
    val contractWeeklyHours: Double = 36.0

    /** No notifications are generated yet; kept as a real (empty) list so NotificheScreen has data to render. */
    val notifications: List<AppNotification> = emptyList()

    init {
        recordsStore.addAll(store.load())

        viewModelScope.launch {
            while (true) {
                delay(30_000)
                now = System.currentTimeMillis()
            }
        }
    }

    fun setTab(tab: Tab) {
        activeTab = tab
    }

    fun openAddSheet() {
        addSheetVisible = true
    }

    fun closeAddSheet() {
        addSheetVisible = false
    }

    fun addManualRecord(date: LocalDate, time: LocalTime, type: EventType) {
        if ((type == EventType.IN || type == EventType.OUT) &&
            recordsStore.any { it.dateTime.toLocalDate() == date && it.type == type }
        ) {
            manualEntryError = if (type == EventType.IN) {
                "Esiste già un ingresso registrato per questo giorno. Elimina quello esistente dallo Storico per registrarne uno nuovo."
            } else {
                "Esiste già un'uscita registrata per questo giorno. Elimina quella esistente dallo Storico per registrarne una nuova."
            }
            return
        }
        addRecord(LocalDateTime.of(date, time), type)
        addSheetVisible = false
    }

    fun dismissManualEntryError() {
        manualEntryError = null
    }

    /** Removes every record (ingresso/uscita/smart-working/ferie) on [date]. */
    fun deleteDay(date: LocalDate) {
        recordsStore.removeAll { it.dateTime.toLocalDate() == date }
        persist()
    }

    /** Removes a single ingresso/uscita record, identified by its exact timestamp and type. */
    fun deleteRecord(dateTime: LocalDateTime, type: EventType) {
        recordsStore.removeAll { it.dateTime == dateTime && it.type == type }
        persist()
    }

    fun markTodaySmartWorking() = markSmartWorkingDay(nowDateTime.toLocalDate())

    fun unmarkTodaySmartWorking() = unmarkSmartWorkingDay(nowDateTime.toLocalDate())

    /** Credits [date] with the fixed smart-working hours instead of real clock-in/out times. */
    fun markSmartWorkingDay(date: LocalDate) {
        addSheetVisible = false
        if (recordsStore.any { it.dateTime.toLocalDate() == date && it.type == EventType.SMART_WORKING }) return
        recordsStore.add(AttendanceRecord(LocalDateTime.of(date, LocalTime.MIDNIGHT), EventType.SMART_WORKING))
        recordsStore.sortBy { it.dateTime }
        persist()
    }

    fun unmarkSmartWorkingDay(date: LocalDate) {
        recordsStore.removeAll { it.dateTime.toLocalDate() == date && it.type == EventType.SMART_WORKING }
        persist()
    }

    /** Marks every day from [start] to [end] (inclusive) as a vacation day. */
    fun markVacationRange(start: LocalDate, end: LocalDate) {
        val first = if (start.isAfter(end)) end else start
        val last = if (start.isAfter(end)) start else end
        var date = first
        while (!date.isAfter(last)) {
            if (recordsStore.none { it.dateTime.toLocalDate() == date && it.type == EventType.VACATION }) {
                recordsStore.add(AttendanceRecord(LocalDateTime.of(date, LocalTime.MIDNIGHT), EventType.VACATION))
            }
            date = date.plusDays(1)
        }
        recordsStore.sortBy { it.dateTime }
        persist()
    }

    fun mainButtonClick() {
        if (requireConfirm) {
            pendingType = if (clockedIn) EventType.OUT else EventType.IN
        } else {
            doClock()
        }
    }

    fun confirmYes() {
        doClock()
        pendingType = null
    }

    fun confirmNo() {
        pendingType = null
    }

    /** Builds the .xlsx workbook and saves it to Downloads; null if there's nothing to export. */
    fun exportToXlsx(context: Context): Uri? {
        val rows = buildHistory(descending = false, includeYear = true)
        if (rows.isEmpty()) return null
        return PresenzeExporter.export(context, rows)
    }

    /**
     * Replaces the entire attendance history with what's in [uri] (an .xlsx
     * produced by [exportToXlsx]). Returns the number of records imported, or
     * null if the file couldn't be read/parsed.
     */
    fun importFromXlsx(context: Context, uri: Uri): Int? {
        val imported = PresenzeImporter.import(context, uri) ?: return null
        recordsStore.clear()
        recordsStore.addAll(imported.sortedBy { it.dateTime })
        persist()
        return recordsStore.size
    }

    private fun doClock() {
        val date = nowDateTime.toLocalDate()
        val type = if (clockedIn) EventType.OUT else EventType.IN
        if (recordsStore.any { it.dateTime.toLocalDate() == date && it.type == type }) {
            manualEntryError = if (type == EventType.IN) {
                "Hai già registrato un ingresso oggi. Elimina quello esistente dallo Storico per timbrarne uno nuovo."
            } else {
                "Hai già registrato un'uscita oggi. Elimina quella esistente dallo Storico per timbrarne una nuova."
            }
            return
        }
        addRecord(LocalDateTime.now(), type)
    }

    private fun addRecord(dateTime: LocalDateTime, type: EventType) {
        recordsStore.add(AttendanceRecord(dateTime, type))
        recordsStore.sortBy { it.dateTime }
        persist()
    }

    private fun persist() = store.save(recordsStore)

    // ---- "live now", derived from the tracked ticker so recomposition keeps working ----

    private val nowDateTime: LocalDateTime
        get() = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime()

    // ---- derived / display values ----

    private val todayRecords: List<AttendanceRecord>
        get() {
            val today = nowDateTime.toLocalDate()
            return recordsStore.filter { it.dateTime.toLocalDate() == today }.sortedBy { it.dateTime }
        }

    val clockedIn: Boolean
        get() = todayRecords.lastOrNull()?.type == EventType.IN

    val smartWorkingToday: Boolean
        get() = todayRecords.any { it.type == EventType.SMART_WORKING }

    val vacationToday: Boolean
        get() = todayRecords.any { it.type == EventType.VACATION }

    val elapsedLabel: String
        get() {
            val lastIn = todayRecords.lastOrNull()?.takeIf { it.type == EventType.IN }
            val elapsedMs = if (clockedIn && lastIn != null) {
                now - lastIn.dateTime.toEpochMs()
            } else {
                0L
            }
            return formatElapsed(elapsedMs)
        }

    val statusLabel: String
        get() = when {
            smartWorkingToday -> "Oggi sei in Smart Working ($SMART_WORKING_DURATION_LABEL)"
            vacationToday -> "Oggi sei in ferie"
            clockedIn -> "Sei in ufficio da $elapsedLabel"
            todayRecords.isNotEmpty() -> "Giornata conclusa"
            else -> "Non hai ancora timbrato oggi"
        }

    val todayEventsDisplay: List<AttendanceEvent>
        get() = todayRecords
            .filter { it.type != EventType.SMART_WORKING && it.type != EventType.VACATION }
            .asReversed()
            .map { AttendanceEvent(time = TIME_FMT.format(it.dateTime), type = it.type, dateTime = it.dateTime) }

    val history: List<HistoryDay>
        get() = buildHistory(descending = true)

    val weekTotal: Double
        get() = weekHours().sum()

    val weekTotalLabel: String
        get() = "${trimTrailingZero(weekTotal)}h"

    val weekAvgLabel: String
        get() {
            val hours = weekHours()
            val workDays = hours.count { it > 0.0 }.let { if (it == 0) 1 else it }
            val avg = weekTotal / workDays
            return "%.1f".format(Locale.US, avg) + "h/giorno"
        }

    val weekBars: List<WeekBar>
        get() {
            val hours = weekHours()
            val maxHour = (hours.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
            return WEEKDAY_LABELS.indices.map { i ->
                val h = hours[i]
                val height = ((h / maxHour) * 120).roundToInt().coerceAtLeast(if (h > 0) 6 else 2)
                val label = if (h > 0) "${trimTrailingZero(h)}h" else "–"
                WeekBar(WEEKDAY_LABELS[i], h, height, label)
            }
        }

    val todayDateLabel: String
        get() {
            val today = nowDateTime.toLocalDate()
            return "${WEEKDAY_FULL[today.dayOfWeek.value - 1]} ${today.dayOfMonth} ${MONTH_FULL[today.monthValue - 1]}"
        }

    /** Past weeks (most recent first), skipping the current week and any week with no hours or vacation. */
    val weekHistory: List<WeekSummary>
        get() = buildWeekHistory()

    /** How the current week compares to the 36h contract target (green/orange/red in Riepilogo). */
    val weekStatus: WeekStatus
        get() = weekStatusFor(weekHours(), currentMonday)

    private val currentMonday: LocalDate
        get() {
            val today = nowDateTime.toLocalDate()
            return today.minusDays((today.dayOfWeek.value - 1).toLong())
        }

    private fun weekDates(monday: LocalDate): List<LocalDate> = (0..6).map { monday.plusDays(it.toLong()) }

    private fun weekHasVacation(monday: LocalDate): Boolean =
        weekDates(monday).any { date -> recordsStore.any { it.dateTime.toLocalDate() == date && it.type == EventType.VACATION } }

    private fun weekStatusFor(hours: List<Double>, monday: LocalDate): WeekStatus {
        val total = hours.sum()
        return when {
            total >= contractWeeklyHours -> WeekStatus.MET
            weekHasVacation(monday) -> WeekStatus.VACATION
            else -> WeekStatus.BELOW
        }
    }

    private fun buildWeekHistory(): List<WeekSummary> {
        if (recordsStore.isEmpty()) return emptyList()
        val earliestDate = recordsStore.minOf { it.dateTime.toLocalDate() }
        val earliestMonday = earliestDate.minusDays((earliestDate.dayOfWeek.value - 1).toLong())

        val summaries = mutableListOf<WeekSummary>()
        var monday = currentMonday.minusWeeks(1)
        while (!monday.isBefore(earliestMonday)) {
            val hours = weekDates(monday).map { dayHours(it) }
            val total = hours.sum()
            if (total > 0.0 || weekHasVacation(monday)) {
                val sunday = monday.plusDays(6)
                val workDays = hours.count { it > 0.0 }.let { if (it == 0) 1 else it }
                val avg = total / workDays
                summaries += WeekSummary(
                    rangeLabel = "${monday.dayOfMonth} ${MONTH_LABELS[monday.monthValue - 1]}" +
                        " - ${sunday.dayOfMonth} ${MONTH_LABELS[sunday.monthValue - 1]}",
                    totalLabel = "${trimTrailingZero(total)}h",
                    avgLabel = "%.1f".format(Locale.US, avg) + "h/giorno",
                    status = weekStatusFor(hours, monday),
                )
            }
            monday = monday.minusWeeks(1)
        }
        return summaries
    }

    private fun weekHours(): List<Double> = weekDates(currentMonday).map { dayHours(it) }

    private fun dayHours(date: LocalDate): Double {
        val dayRecords = recordsStore.filter { it.dateTime.toLocalDate() == date }.sortedBy { it.dateTime }
        if (dayRecords.any { it.type == EventType.SMART_WORKING }) return SMART_WORKING_HOURS
        if (dayRecords.any { it.type == EventType.VACATION }) return 0.0
        val firstIn = dayRecords.firstOrNull { it.type == EventType.IN }
        val lastOut = dayRecords.lastOrNull { it.type == EventType.OUT }
        val minutes = when {
            firstIn != null && lastOut != null && lastOut.dateTime.isAfter(firstIn.dateTime) ->
                Duration.between(firstIn.dateTime, lastOut.dateTime).toMinutes()
            firstIn != null && date == nowDateTime.toLocalDate() ->
                Duration.between(firstIn.dateTime, nowDateTime).toMinutes()
            else -> 0L
        }
        return minutes / 60.0
    }

    private fun buildHistory(descending: Boolean, includeYear: Boolean = false): List<HistoryDay> {
        val today = nowDateTime.toLocalDate()
        val byDate = recordsStore.groupBy { it.dateTime.toLocalDate() }
        val dates = byDate.keys.sortedWith(if (descending) compareByDescending { it } else compareBy { it })
        return dates.map { date ->
            val dayRecords = byDate.getValue(date).sortedBy { it.dateTime }
            val dateLabel = formatDayLabel(date, includeYear)

            if (dayRecords.any { it.type == EventType.SMART_WORKING }) {
                return@map HistoryDay(
                    date = dateLabel,
                    entrata = SMART_WORKING_LABEL,
                    uscita = SMART_WORKING_LABEL,
                    ore = SMART_WORKING_DURATION_LABEL,
                    kind = DayKind.SMART_WORKING,
                    rawDate = date,
                )
            }
            if (dayRecords.any { it.type == EventType.VACATION }) {
                return@map HistoryDay(
                    date = dateLabel,
                    entrata = VACATION_LABEL,
                    uscita = VACATION_LABEL,
                    ore = "–",
                    kind = DayKind.VACATION,
                    rawDate = date,
                )
            }

            val firstIn = dayRecords.firstOrNull { it.type == EventType.IN }
            val lastOut = dayRecords.lastOrNull { it.type == EventType.OUT }
            val entrata = firstIn?.let { TIME_FMT.format(it.dateTime) } ?: "–"
            val uscita = lastOut?.let { TIME_FMT.format(it.dateTime) } ?: "–"
            val ore = when {
                firstIn != null && lastOut != null && lastOut.dateTime.isAfter(firstIn.dateTime) ->
                    formatDayDuration(Duration.between(firstIn.dateTime, lastOut.dateTime))
                firstIn != null && date == today ->
                    formatDayDuration(Duration.between(firstIn.dateTime, nowDateTime))
                else -> "–"
            }
            HistoryDay(date = dateLabel, entrata = entrata, uscita = uscita, ore = ore, rawDate = date)
        }
    }

    /**
     * [includeYear] is turned on for the export sheet only, so importing it back
     * can resolve an unambiguous date; the on-screen Storico list doesn't need it.
     */
    private fun formatDayLabel(date: LocalDate, includeYear: Boolean = false): String {
        val base = "${WEEKDAY_LABELS[date.dayOfWeek.value - 1]} ${date.dayOfMonth} ${MONTH_LABELS[date.monthValue - 1]}"
        return if (includeYear) "$base ${date.year}" else base
    }

    companion object {
        private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private val WEEKDAY_LABELS = arrayOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
        private val WEEKDAY_FULL = arrayOf(
            "lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato", "domenica"
        )
        private val MONTH_LABELS = arrayOf(
            "Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"
        )
        private val MONTH_FULL = arrayOf(
            "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
            "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre"
        )

        private fun LocalDateTime.toEpochMs(): Long =
            this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        private fun formatElapsed(ms: Long): String {
            val totalMin = ms / 60_000
            val h = totalMin / 60
            val m = totalMin % 60
            return if (h > 0) "${h}h ${m.toString().padStart(2, '0')}min" else "${m}min"
        }

        private fun formatDayDuration(duration: Duration): String {
            val totalMin = duration.toMinutes().coerceAtLeast(0)
            val h = totalMin / 60
            val m = totalMin % 60
            return "${h}h ${m.toString().padStart(2, '0')}m"
        }

        private fun trimTrailingZero(value: Double): String {
            val s = "%.1f".format(Locale.US, value)
            return if (s.endsWith(".0")) s.dropLast(2) else s
        }
    }
}
