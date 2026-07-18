package com.appresenze.presenze.export

import android.content.Context
import android.net.Uri
import com.appresenze.presenze.data.AttendanceRecord
import com.appresenze.presenze.data.EventType
import com.appresenze.presenze.data.SMART_WORKING_LABEL
import com.appresenze.presenze.data.VACATION_LABEL
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

/**
 * Reads back a workbook produced by [PresenzeExporter] and rebuilds the list
 * of attendance records from it. Only understands that exact layout (Data /
 * Ora d'ingresso / Ora d'uscita / Ore in ufficio, one row per day, inline
 * strings, "Data" formatted as "Wd d Mon yyyy") -- this is a round-trip
 * companion to our own export, not a general-purpose spreadsheet reader.
 */
object PresenzeImporter {

    private val MONTH_LABELS = arrayOf(
        "gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "set", "ott", "nov", "dic"
    )

    private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val ROW_REGEX = Regex("<row[^>]*r=\"(\\d+)\"[^>]*>(.*?)</row>", RegexOption.DOT_MATCHES_ALL)
    private val TEXT_REGEX = Regex("<t[^>]*>(.*?)</t>", RegexOption.DOT_MATCHES_ALL)

    /** Returns the parsed records, or null if the file couldn't be read or didn't match the layout. */
    fun import(context: Context, uri: Uri): List<AttendanceRecord>? {
        val sheetXml = runCatching {
            context.contentResolver.openInputStream(uri)?.use { readSheetXml(it) }
        }.getOrNull() ?: return null

        return runCatching { parseSheetXml(sheetXml) }.getOrNull()
    }

    private fun readSheetXml(input: InputStream): String? {
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "xl/worksheets/sheet1.xml") {
                    return zip.readBytes().toString(Charsets.UTF_8)
                }
                entry = zip.nextEntry
            }
        }
        return null
    }

    private fun parseSheetXml(xml: String): List<AttendanceRecord> {
        val records = mutableListOf<AttendanceRecord>()
        for (rowMatch in ROW_REGEX.findAll(xml)) {
            val rowNum = rowMatch.groupValues[1].toIntOrNull() ?: continue
            if (rowNum == 1) continue // header row

            val values = TEXT_REGEX.findAll(rowMatch.groupValues[2])
                .map { unescape(it.groupValues[1]) }
                .toList()
            if (values.size < 3) continue

            val date = parseDataLabel(values[0]) ?: continue

            if (values[1].trim().equals(SMART_WORKING_LABEL, ignoreCase = true)) {
                records += AttendanceRecord(LocalDateTime.of(date, LocalTime.MIDNIGHT), EventType.SMART_WORKING)
                continue
            }
            if (values[1].trim().equals(VACATION_LABEL, ignoreCase = true)) {
                records += AttendanceRecord(LocalDateTime.of(date, LocalTime.MIDNIGHT), EventType.VACATION)
                continue
            }

            parseTime(values[1])?.let { records += AttendanceRecord(LocalDateTime.of(date, it), EventType.IN) }
            parseTime(values[2])?.let { records += AttendanceRecord(LocalDateTime.of(date, it), EventType.OUT) }
        }
        return records.sortedBy { it.dateTime }
    }

    private fun parseDataLabel(label: String): LocalDate? {
        val parts = label.trim().split(Regex("\\s+"))
        if (parts.size != 4) return null
        val day = parts[1].toIntOrNull() ?: return null
        val monthIdx = MONTH_LABELS.indexOfFirst { it.equals(parts[2], ignoreCase = true) }
        if (monthIdx < 0) return null
        val year = parts[3].toIntOrNull() ?: return null
        return runCatching { LocalDate.of(year, monthIdx + 1, day) }.getOrNull()
    }

    private fun parseTime(value: String): LocalTime? {
        val trimmed = value.trim()
        if (trimmed.isEmpty() || trimmed == "–" || trimmed == "-") return null
        return runCatching { LocalTime.parse(trimmed, TIME_FMT) }.getOrNull()
    }

    private fun unescape(value: String): String = value
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&amp;", "&")
}
