package com.appresenze.presenze.data

import java.time.LocalDate
import java.time.LocalDateTime

enum class Tab { HOME, STORICO, RIEPILOGO, NOTIFICHE }

enum class EventType { IN, OUT, SMART_WORKING, VACATION }

/** What kind of day a Storico/export row represents. */
enum class DayKind { NORMAL, SMART_WORKING, VACATION }

/** How a week compares against the contract hours target, for the Riepilogo highlight. */
enum class WeekStatus { MET, VACATION, BELOW }

/** Fixed daily credit the company recognizes for a smart-working day. */
const val SMART_WORKING_HOURS = 7.5
const val SMART_WORKING_DURATION_LABEL = "7h 30m"

/** Marker text written into the Ora d'ingresso/uscita export cells for a smart-working day. */
const val SMART_WORKING_LABEL = "Smart working"

/** Marker text written into the Ora d'ingresso/uscita export cells for a vacation day. */
const val VACATION_LABEL = "Ferie"

/** Daily hours target: a normal day's Storico card turns green at or above this. */
const val DAILY_TARGET_HOURS = 8.0

/** A single clock-in/out event, or a smart-working/vacation day marker (recorded or manually added). */
data class AttendanceRecord(
    val dateTime: LocalDateTime,
    val type: EventType,
)

data class AttendanceEvent(
    val time: String,
    val type: EventType,
    /** The exact record timestamp, used to target this specific event for deletion. */
    val dateTime: LocalDateTime,
)

data class HistoryDay(
    val date: String,
    val entrata: String,
    val uscita: String,
    val ore: String,
    val kind: DayKind = DayKind.NORMAL,
    /** The actual calendar date behind [date]'s formatted label, used to target deletion. */
    val rawDate: LocalDate,
    /** True for a NORMAL day whose worked hours reached [DAILY_TARGET_HOURS] -> green card in Storico. */
    val dailyTargetMet: Boolean = false,
    /** Formatted surplus ("+1h 15m") when hours exceed [DAILY_TARGET_HOURS]; null otherwise. */
    val surplusLabel: String? = null,
)

data class WeekBar(
    val label: String,
    val hours: Double,
    val barHeightDp: Int,
    val hoursLabel: String,
)

data class AppNotification(
    val title: String,
    val body: String,
    val time: String,
)

/** One row in the "Settimane precedenti" history on the Riepilogo screen. */
data class WeekSummary(
    val rangeLabel: String,
    val totalLabel: String,
    val avgLabel: String,
    val status: WeekStatus,
)
