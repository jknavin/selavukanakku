package com.selavukanakku.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.floor

/** Turns an .xlsx or .csv file into transactions. Expected columns: Date, Type, Category, Amount, Note. */
object Importer {

    fun parse(bytes: ByteArray, fileName: String): List<Transaction> {
        val isZip = bytes.size > 3 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()
        val rows = if (isZip && !fileName.endsWith(".csv", true)) {
            Excel.readRows(bytes)
        } else {
            parseCsv(String(bytes, Charsets.UTF_8).removePrefix("﻿"))
        }
        return rows.mapNotNull { toTransaction(it) }
    }

    private fun toTransaction(cells: List<String>): Transaction? {
        if (cells.size < 4) return null
        val date = parseDate(cells[0].trim()) ?: return null
        val rawAmount = cleanAmount(cells[3]) ?: return null
        if (rawAmount == 0.0) return null

        val typeText = cells[1].trim().lowercase(Locale.ROOT)
        val type = when {
            typeText.startsWith("inc") || typeText.startsWith("வரு") -> TxType.INCOME
            typeText.startsWith("exp") || typeText.startsWith("செலவு") -> TxType.EXPENSE
            else -> return null // also skips the header row
        }
        val cat = Categories.match(cells[2], type)
        return Transaction(
            type = type,
            amount = abs(rawAmount),
            categoryId = cat.id,
            note = cells.getOrNull(4)?.trim().orEmpty().take(80),
            dateMillis = date
        )
    }

    private fun cleanAmount(s: String): Double? =
        s.replace("₹", "").replace(",", "").replace(" ", "").trim().toDoubleOrNull()

    private fun noonOf(year: Int, month: Int, day: Int): Long {
        val c = Calendar.getInstance()
        c.clear()
        c.set(year, month, day, 12, 0, 0)
        return c.timeInMillis
    }

    private val formats = listOf("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "d MMM yyyy", "dd.MM.yyyy")

    private fun parseDate(text: String): Long? {
        if (text.isEmpty()) return null

        // Excel stores real dates as day numbers (days since 1899-12-30).
        val serial = text.toDoubleOrNull()
        if (serial != null) {
            if (serial < 1 || serial > 80000) return null
            val utcMillis = (floor(serial) - 25569.0).toLong() * 86_400_000L
            val u = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            u.timeInMillis = utcMillis
            return noonOf(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH))
        }

        for (f in formats) {
            val fmt = SimpleDateFormat(f, Locale.ENGLISH)
            fmt.isLenient = false
            val d = try {
                fmt.parse(text)
            } catch (e: Exception) {
                null
            }
            if (d != null) {
                val c = Calendar.getInstance()
                c.time = d
                return noonOf(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            }
        }
        return null
    }

    private fun parseCsv(text: String): List<List<String>> {
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') {
                        cell.append('"')
                        i++
                    } else quoted = false
                } else cell.append(ch)
            } else when (ch) {
                '"' -> quoted = true
                ',' -> {
                    row.add(cell.toString())
                    cell.setLength(0)
                }
                '\n' -> {
                    row.add(cell.toString())
                    cell.setLength(0)
                    rows.add(row)
                    row = ArrayList()
                }
                '\r' -> {}
                else -> cell.append(ch)
            }
            i++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString())
            rows.add(row)
        }
        return rows
    }
}
