package com.appresenze.presenze.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.appresenze.presenze.data.HistoryDay
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds a minimal, dependency-free .xlsx (OOXML) workbook from the attendance
 * history and saves it into the device's Downloads collection via MediaStore
 * (no permission needed on API 29+). No third-party spreadsheet library is
 * used here on purpose: Apache POI is heavy and has known Android/AWT
 * compatibility gotchas, which isn't worth it for a 4-column export.
 */
object PresenzeExporter {

    private const val MIME_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    /** Saves the workbook to Downloads and returns its content Uri, or null if [rows] is empty. */
    fun export(context: Context, rows: List<HistoryDay>): Uri? {
        if (rows.isEmpty()) return null
        val bytes = buildWorkbook(rows)
        val fileName = "Presenze_${FILE_TS_FMT.format(LocalDateTime.now())}.xlsx"

        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { out -> out.write(bytes) } ?: return null
        return uri
    }

    /** Share/"open with" intent for the exported file, e.g. to send it via email or chat. */
    fun shareIntent(uri: Uri): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    private val FILE_TS_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    // ---- minimal OOXML (.xlsx) writer ----

    private const val DEFAULT_STYLE = 0
    private const val HEADER_STYLE = 1
    private const val GREEN_STYLE = 2
    private const val YELLOW_STYLE = 3

    private fun buildWorkbook(rows: List<HistoryDay>): ByteArray {
        val sheetXml = buildSheetXml(rows)
        val buffer = ByteArrayOutputStream()
        ZipOutputStream(buffer).use { zip ->
            writeEntry(zip, "[Content_Types].xml", CONTENT_TYPES_XML)
            writeEntry(zip, "_rels/.rels", RELS_XML)
            writeEntry(zip, "xl/workbook.xml", WORKBOOK_XML)
            writeEntry(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
            writeEntry(zip, "xl/styles.xml", STYLES_XML)
            writeEntry(zip, "xl/worksheets/sheet1.xml", sheetXml)
        }
        return buffer.toByteArray()
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildSheetXml(rows: List<HistoryDay>): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        append("<cols>")
        append("<col min=\"1\" max=\"1\" width=\"16\" customWidth=\"1\"/>")
        append("<col min=\"2\" max=\"3\" width=\"16\" customWidth=\"1\"/>")
        append("<col min=\"4\" max=\"4\" width=\"15\" customWidth=\"1\"/>")
        append("</cols>")
        append("<sheetData>")
        append("<row r=\"1\">")
        append(cell("A1", "Data", HEADER_STYLE))
        append(cell("B1", "Ora d'ingresso", HEADER_STYLE))
        append(cell("C1", "Ora d'uscita", HEADER_STYLE))
        append(cell("D1", "Ore in ufficio", HEADER_STYLE))
        append("</row>")
        rows.forEachIndexed { index, row ->
            val r = index + 2
            append("<row r=\"$r\">")
            append(cell("A$r", row.date, DEFAULT_STYLE))
            append(cell("B$r", row.entrata, GREEN_STYLE))
            append(cell("C$r", row.uscita, YELLOW_STYLE))
            append(cell("D$r", row.ore, DEFAULT_STYLE))
            append("</row>")
        }
        append("</sheetData>")
        append("</worksheet>")
    }

    private fun cell(ref: String, value: String, style: Int): String {
        val styleAttr = if (style != DEFAULT_STYLE) " s=\"$style\"" else ""
        return "<c r=\"$ref\" t=\"inlineStr\"$styleAttr><is><t xml:space=\"preserve\">${escape(value)}</t></is></c>"
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private val CONTENT_TYPES_XML = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
        <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
        <Default Extension="xml" ContentType="application/xml"/>
        <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
        <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        </Types>
    """.trimIndent()

    private val RELS_XML = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
    """.trimIndent()

    private val WORKBOOK_XML = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
        <sheets><sheet name="Presenze" sheetId="1" r:id="rId1"/></sheets>
        </workbook>
    """.trimIndent()

    private val WORKBOOK_RELS_XML = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
        <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        </Relationships>
    """.trimIndent()

    private val STYLES_XML = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        <fonts count="2">
        <font><sz val="11"/><name val="Calibri"/></font>
        <font><b/><sz val="11"/><name val="Calibri"/></font>
        </fonts>
        <fills count="4">
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFC6EFCE"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFFFEB9C"/><bgColor indexed="64"/></patternFill></fill>
        </fills>
        <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
        <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
        <cellXfs count="4">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
        <xf numFmtId="0" fontId="0" fillId="2" borderId="0" xfId="0" applyFill="1"/>
        <xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyFill="1"/>
        </cellXfs>
        <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
        </styleSheet>
    """.trimIndent()
}
