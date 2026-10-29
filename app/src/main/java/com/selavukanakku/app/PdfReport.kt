package com.selavukanakku.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

/** Draws an A4 expense report with PdfDocument (system fonts, so Tamil renders correctly). */
object PdfReport {

    private const val W = 595
    private const val H = 842
    private const val M = 36f
    private const val ROW_H = 22f

    private val accent = Color.parseColor("#4F46E5")
    private val green = Color.parseColor("#059669")
    private val red = Color.parseColor("#E11D48")
    private val grey = Color.parseColor("#64748B")
    private val ink = Color.parseColor("#0F172A")

    private fun paint(size: Float, color: Int, bold: Boolean = false, right: Boolean = false) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textAlign = if (right) Paint.Align.RIGHT else Paint.Align.LEFT
        }

    private fun fit(text: String, p: Paint, maxW: Float): String {
        if (p.measureText(text) <= maxW) return text
        val n = p.breakText(text, true, maxW - p.measureText("…"), null)
        return text.take(n) + "…"
    }

    fun build(txs: List<Transaction>, lang: Lang): ByteArray {
        val doc = PdfDocument()
        val sorted = txs.sortedByDescending { it.dateMillis }
        val income = txs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
        val expense = txs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
        var pageNo = 0
        var page: PdfDocument.Page? = null
        var c: Canvas? = null
        var y = 0f

        fun startPage() {
            page?.let { doc.finishPage(it) }
            pageNo++
            val pg = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            page = pg
            c = pg.canvas
            // footer
            c!!.drawText(
                "${S.t("app_name", lang)}   |   ${S.t("page", lang)} $pageNo",
                W / 2f, H - 20f,
                paint(9f, grey).apply { textAlign = Paint.Align.CENTER }
            )
            y = M + 10f
        }

        fun tableHeader() {
            val cv = c!!
            cv.drawRect(M, y, W - M, y + ROW_H, Paint().apply { color = accent })
            val hp = paint(10.5f, Color.WHITE, bold = true)
            val hr = paint(10.5f, Color.WHITE, bold = true, right = true)
            cv.drawText(S.t("date", lang), M + 8, y + 15, hp)
            cv.drawText(S.t("type", lang), M + 100, y + 15, hp)
            cv.drawText(S.t("category", lang), M + 170, y + 15, hp)
            cv.drawText(S.t("col_note", lang), M + 275, y + 15, hp)
            cv.drawText(S.t("amount", lang), W - M - 8, y + 15, hr)
            y += ROW_H
        }

        startPage()
        val cv0 = c!!

        // Title band
        cv0.drawRect(M, y, W - M, y + 64f, Paint().apply { color = accent })
        cv0.drawText(S.t("app_name", lang), M + 16, y + 28, paint(20f, Color.WHITE, bold = true))
        cv0.drawText(
            "${S.t("report_title", lang)}  •  ${S.t("generated", lang)} ${dateFull(System.currentTimeMillis(), lang)}",
            M + 16, y + 49, paint(11f, Color.parseColor("#E0E7FF"))
        )
        y += 84f

        // Summary boxes
        val boxW = (W - 2 * M - 20f) / 3f
        val items = listOf(
            Triple(S.t("income", lang), money(income), green),
            Triple(S.t("expense", lang), money(expense), red),
            Triple(S.t("balance", lang), signedMoney(income - expense), accent)
        )
        items.forEachIndexed { i, (label, value, color) ->
            val x = M + i * (boxW + 10f)
            cv0.drawRect(x, y, x + boxW, y + 54f, Paint().apply {
                this.color = Color.parseColor("#F1F5F9")
            })
            cv0.drawRect(x, y, x + 4f, y + 54f, Paint().apply { this.color = color })
            cv0.drawText(label, x + 14, y + 20, paint(10.5f, grey))
            cv0.drawText(value, x + 14, y + 41, paint(15f, color, bold = true))
        }
        y += 76f

        // Top expense categories
        val top = txs.filter { it.type == TxType.EXPENSE }
            .groupBy { it.categoryId }
            .map { Categories.byId(it.key) to it.value.sumOf { t -> t.amount } }
            .sortedByDescending { it.second }
            .take(5)
        if (top.isNotEmpty()) {
            cv0.drawText(S.t("top_expense", lang), M, y, paint(13f, ink, bold = true))
            y += 14f
            val barMax = W - 2 * M - 250f
            top.forEach { (cat, v) ->
                cv0.drawText(cat.label(lang), M, y + 12, paint(10.5f, ink))
                val bw = (v / top.first().second * barMax).toFloat().coerceAtLeast(3f)
                cv0.drawRect(M + 110, y + 3, M + 110 + bw, y + 14, Paint().apply {
                    color = cat.color.toInt()
                })
                cv0.drawText(money(v), W - M, y + 12, paint(10.5f, ink, right = true))
                y += 20f
            }
            y += 12f
        }

        // Transactions table
        cv0.drawText(S.t("sheet_tx", lang), M, y + 4, paint(13f, ink, bold = true))
        y += 14f
        tableHeader()

        val tp = paint(10f, ink)
        val ap = paint(10f, ink, bold = true, right = true)
        sorted.forEachIndexed { i, t ->
            if (y + ROW_H > H - 40f) {
                startPage()
                tableHeader()
            }
            val cv = c!!
            if (i % 2 == 1) {
                cv.drawRect(M, y, W - M, y + ROW_H, Paint().apply {
                    color = Color.parseColor("#F8FAFC")
                })
            }
            val isInc = t.type == TxType.INCOME
            cv.drawText(dateIso(t.dateMillis), M + 8, y + 15, tp)
            cv.drawText(
                S.t(if (isInc) "income" else "expense", lang), M + 100, y + 15,
                paint(10f, if (isInc) green else red)
            )
            cv.drawText(fit(Categories.byId(t.categoryId).label(lang), tp, 100f), M + 170, y + 15, tp)
            cv.drawText(fit(t.note, tp, 170f), M + 275, y + 15, tp)
            ap.color = if (isInc) green else red
            cv.drawText((if (isInc) "+" else "-") + money(t.amount), W - M - 8, y + 15, ap)
            y += ROW_H
        }

        page?.let { doc.finishPage(it) }
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }
}
