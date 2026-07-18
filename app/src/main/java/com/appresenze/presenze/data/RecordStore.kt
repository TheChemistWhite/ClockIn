package com.appresenze.presenze.data

import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Minimal on-device persistence for attendance records: one line per event,
 * tab-separated "yyyy-MM-dd\tHH:mm\tIN|OUT". This keeps the app dependency-free
 * (no Room/DataStore) while still surviving process death and app restarts.
 */
class RecordStore(private val file: File) {

    fun load(): List<AttendanceRecord> {
        if (!file.exists()) return emptyList()
        return file.readLines().mapNotNull { line -> parseLine(line) }
    }

    fun save(records: List<AttendanceRecord>) {
        file.writeText(
            records.joinToString(separator = "\n") { r ->
                "${r.dateTime.toLocalDate()}\t${TIME_FMT.format(r.dateTime.toLocalTime())}\t${r.type.name}"
            }
        )
    }

    private fun parseLine(line: String): AttendanceRecord? {
        val parts = line.split('\t')
        if (parts.size != 3) return null
        return runCatching {
            val date = LocalDate.parse(parts[0])
            val time = LocalTime.parse(parts[1], TIME_FMT)
            val type = EventType.valueOf(parts[2])
            AttendanceRecord(LocalDateTime.of(date, time), type)
        }.getOrNull()
    }

    companion object {
        private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        const val FILE_NAME = "presenze_records.tsv"
    }
}
