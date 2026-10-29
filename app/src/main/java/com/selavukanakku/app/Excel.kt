package com.selavukanakku.app

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Minimal .xlsx writer/reader (no external libraries) for the Transactions table. */
object Excel {

    private fun esc(s: String): String {
        val sb = StringBuilder(s.length + 8)
        for (ch in s) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                else -> if (ch.code >= 32 || ch == '\n' || ch == '\t') sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun colName(i: Int): String = ('A' + i).toString()

    private fun strCell(ref: String, text: String, style: Int = 0): String =
        "<c r=\"$ref\" t=\"inlineStr\"${if (style > 0) " s=\"$style\"" else ""}>" +
            "<is><t xml:space=\"preserve\">${esc(text)}</t></is></c>"

    private fun numCell(ref: String, v: Double): String {
        val text = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
        return "<c r=\"$ref\"><v>$text</v></c>"
    }

    private fun row(index: Int, cells: List<String>): String =
        "<row r=\"$index\">${cells.joinToString("")}</row>"

    private fun sheetXml(widths: List<Int>, rows: List<String>): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<cols>" + widths.mapIndexed { i, w ->
            "<col min=\"${i + 1}\" max=\"${i + 1}\" width=\"$w\" customWidth=\"1\"/>"
        }.joinToString("") + "</cols>" +
            "<sheetData>" + rows.joinToString("") + "</sheetData></worksheet>"

    fun write(txs: List<Transaction>, lang: Lang): ByteArray {
        // Sheet 1: Transactions
        val head = listOf("date", "type", "category", "amount", "col_note")
            .mapIndexed { i, k -> strCell("${colName(i)}1", S.t(k, lang), 1) }
        val rows = ArrayList<String>()
        rows.add(row(1, head))
        txs.sortedBy { it.dateMillis }.forEachIndexed { i, t ->
            val r = i + 2
            rows.add(
                row(
                    r, listOf(
                        strCell("A$r", dateIso(t.dateMillis)),
                        strCell("B$r", S.t(if (t.type == TxType.INCOME) "income" else "expense", lang)),
                        strCell("C$r", Categories.byId(t.categoryId).label(lang)),
                        numCell("D$r", t.amount),
                        strCell("E$r", t.note)
                    )
                )
            )
        }
        val sheet1 = sheetXml(listOf(14, 12, 18, 14, 40), rows)

        // Sheet 2: Summary
        val income = txs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
        val expense = txs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
        val sum = ArrayList<String>()
        sum.add(row(1, listOf(strCell("A1", S.t("item", lang), 1), strCell("B1", S.t("amount", lang), 1))))
        sum.add(row(2, listOf(strCell("A2", S.t("income", lang)), numCell("B2", income))))
        sum.add(row(3, listOf(strCell("A3", S.t("expense", lang)), numCell("B3", expense))))
        sum.add(row(4, listOf(strCell("A4", S.t("balance", lang)), numCell("B4", income - expense))))
        val sheet2 = sheetXml(listOf(22, 16), sum)

        val contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
            "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            "</Types>"

        val rootRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "</Relationships>"

        val workbook = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>" +
            "<sheet name=\"${esc(S.t("sheet_tx", lang))}\" sheetId=\"1\" r:id=\"rId1\"/>" +
            "<sheet name=\"${esc(S.t("sheet_summary", lang))}\" sheetId=\"2\" r:id=\"rId2\"/>" +
            "</sheets></workbook>"

        val workbookRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
            "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>" +
            "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
            "</Relationships>"

        val styles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "<font><b/><color rgb=\"FFFFFFFF\"/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>" +
            "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF4F46E5\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>" +
            "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/></cellXfs>" +
            "</styleSheet>"

        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun put(name: String, body: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("[Content_Types].xml", contentTypes)
            put("_rels/.rels", rootRels)
            put("xl/workbook.xml", workbook)
            put("xl/_rels/workbook.xml.rels", workbookRels)
            put("xl/styles.xml", styles)
            put("xl/worksheets/sheet1.xml", sheet1)
            put("xl/worksheets/sheet2.xml", sheet2)
        }
        return out.toByteArray()
    }

    // ---------------- Reading ----------------

    /** Returns the first worksheet as rows of cell text (empty string for blank cells). */
    fun readRows(bytes: ByteArray): List<List<String>> {
        var shared: ByteArray? = null
        var sheet: ByteArray? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var e = zip.nextEntry
            while (e != null) {
                when (e.name) {
                    "xl/sharedStrings.xml" -> shared = zip.readBytes()
                    "xl/worksheets/sheet1.xml" -> sheet = zip.readBytes()
                }
                e = zip.nextEntry
            }
        }
        val sheetBytes = sheet ?: return emptyList()
        val strings = shared?.let { parseShared(it) } ?: emptyList()
        return parseSheet(sheetBytes, strings)
    }

    private fun parseShared(data: ByteArray): List<String> {
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(data), "UTF-8")
        val list = ArrayList<String>()
        var sb: StringBuilder? = null
        var inT = false
        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> sb = StringBuilder()
                    "t" -> inT = true
                }
                XmlPullParser.TEXT -> if (inT) sb?.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "t" -> inT = false
                    "si" -> {
                        list.add(sb?.toString() ?: "")
                        sb = null
                    }
                }
            }
            ev = parser.next()
        }
        return list
    }

    private fun colIndex(ref: String): Int {
        var n = 0
        for (ch in ref) {
            if (ch in 'A'..'Z') n = n * 26 + (ch - 'A' + 1) else break
        }
        return n - 1
    }

    private fun parseSheet(data: ByteArray, strings: List<String>): List<List<String>> {
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(data), "UTF-8")
        val rows = ArrayList<List<String>>()
        var current: MutableList<String>? = null
        var cellType = ""
        var cellCol = 0
        var value = StringBuilder()
        var inValue = false
        var inText = false
        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> current = ArrayList()
                    "c" -> {
                        cellType = parser.getAttributeValue(null, "t") ?: ""
                        cellCol = colIndex(parser.getAttributeValue(null, "r") ?: "A1")
                        value = StringBuilder()
                    }
                    "v" -> inValue = true
                    "t" -> inText = true
                }
                XmlPullParser.TEXT -> if (inValue || inText) value.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "v" -> inValue = false
                    "t" -> inText = false
                    "c" -> {
                        val cells = current
                        if (cells != null) {
                            while (cells.size < cellCol) cells.add("")
                            val raw = value.toString()
                            val text = if (cellType == "s") {
                                strings.getOrNull(raw.trim().toIntOrNull() ?: -1) ?: ""
                            } else raw
                            if (cells.size == cellCol) cells.add(text) else cells[cellCol] = text
                        }
                    }
                    "row" -> {
                        current?.let { rows.add(it) }
                        current = null
                    }
                }
            }
            ev = parser.next()
        }
        return rows
    }
}
