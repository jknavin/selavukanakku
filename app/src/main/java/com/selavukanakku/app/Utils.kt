package com.selavukanakku.app

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

fun localeFor(lang: Lang): Locale =
    if (lang == Lang.TA) Locale("ta", "IN") else Locale("en", "IN")

// ---------- Money ----------

fun money(v: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.maximumFractionDigits = 2
    nf.minimumFractionDigits = if (v % 1.0 == 0.0) 0 else 2
    return "₹" + nf.format(v)
}

fun signedMoney(v: Double): String = if (v < 0) "-" + money(abs(v)) else money(v)

private fun trimNum(d: Double): String =
    if (d % 1.0 == 0.0) d.toLong().toString() else String.format(Locale.US, "%.1f", d)

/** Compact label for chart axes: 1.2K, 3L, 1.5Cr. */
fun compact(v: Double): String = when {
    v >= 1e7 -> trimNum(v / 1e7) + "Cr"
    v >= 1e5 -> trimNum(v / 1e5) + "L"
    v >= 1e3 -> trimNum(v / 1e3) + "K"
    else -> trimNum(v)
}

/** Rounds up to a value that divides cleanly into four grid steps. */
fun niceCeil(v: Double): Double {
    val exp = floor(log10(v))
    val base = 10.0.pow(exp)
    val f = v / base
    val nf = when {
        f <= 1.0 -> 1.0
        f <= 2.0 -> 2.0
        f <= 4.0 -> 4.0
        f <= 8.0 -> 8.0
        else -> 10.0
    }
    return nf * base
}

// ---------- Dates ----------

fun cal(millis: Long): Calendar = Calendar.getInstance().apply { timeInMillis = millis }

fun startOfDay(millis: Long): Long = cal(millis).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun dateFull(millis: Long, lang: Lang): String =
    SimpleDateFormat("d MMM yyyy", localeFor(lang)).format(Date(millis))

fun dateIso(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

/** [start, end) of the period that contains [ref]. */
fun windowRange(period: Period, ref: Long): Pair<Long, Long> {
    val c = cal(startOfDay(ref))
    var start = 0L
    var end = 0L
    when (period) {
        Period.DAILY -> {
            start = c.timeInMillis
            c.add(Calendar.DAY_OF_YEAR, 1)
            end = c.timeInMillis
        }
        Period.MONTHLY -> {
            c.set(Calendar.DAY_OF_MONTH, 1)
            start = c.timeInMillis
            c.add(Calendar.MONTH, 1)
            end = c.timeInMillis
        }
        Period.YEARLY -> {
            c.set(Calendar.DAY_OF_YEAR, 1)
            start = c.timeInMillis
            c.add(Calendar.YEAR, 1)
            end = c.timeInMillis
        }
    }
    return start to end
}

fun shiftRef(period: Period, ref: Long, dir: Int): Long {
    val c = cal(ref)
    when (period) {
        Period.DAILY -> c.add(Calendar.DAY_OF_YEAR, dir)
        Period.MONTHLY -> c.add(Calendar.MONTH, dir)
        Period.YEARLY -> c.add(Calendar.YEAR, dir)
    }
    return c.timeInMillis
}

fun periodLabel(period: Period, ref: Long, lang: Lang): String {
    val pattern = when (period) {
        Period.DAILY -> "d MMM yyyy"
        Period.MONTHLY -> "MMMM yyyy"
        Period.YEARLY -> "yyyy"
    }
    return SimpleDateFormat(pattern, localeFor(lang)).format(Date(ref))
}

// The Material date picker works in UTC midnight; transactions use local time.
fun localToPickerUtc(millis: Long): Long {
    val l = cal(millis)
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    u.clear()
    u.set(l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH))
    return u.timeInMillis
}

fun pickerUtcToLocal(utcMillis: Long, timeSource: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    u.timeInMillis = utcMillis
    val t = cal(timeSource)
    val c = Calendar.getInstance()
    c.clear()
    c.set(
        u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH),
        t.get(Calendar.HOUR_OF_DAY), t.get(Calendar.MINUTE), t.get(Calendar.SECOND)
    )
    return c.timeInMillis
}

// ---------- Chart data ----------

data class Bucket(val label: String, val income: Double, val expense: Double)

private fun bucketOf(txs: List<Transaction>, start: Long, end: Long, label: String): Bucket {
    var inc = 0.0
    var exp = 0.0
    for (t in txs) {
        if (t.dateMillis >= start && t.dateMillis < end) {
            if (t.type == TxType.INCOME) inc += t.amount else exp += t.amount
        }
    }
    return Bucket(label, inc, exp)
}

fun buildBuckets(txs: List<Transaction>, period: Period, ref: Long, lang: Lang): List<Bucket> {
    val loc = localeFor(lang)
    val base = cal(startOfDay(ref))
    return when (period) {
        Period.DAILY -> {
            val fmt = SimpleDateFormat("d/M", loc)
            (6 downTo 0).map { i ->
                val c = base.clone() as Calendar
                c.add(Calendar.DAY_OF_YEAR, -i)
                val s = c.timeInMillis
                c.add(Calendar.DAY_OF_YEAR, 1)
                bucketOf(txs, s, c.timeInMillis, fmt.format(Date(s)))
            }
        }
        Period.MONTHLY -> {
            base.set(Calendar.DAY_OF_MONTH, 1)
            val days = base.getActualMaximum(Calendar.DAY_OF_MONTH)
            (0 until days).map { i ->
                val c = base.clone() as Calendar
                c.add(Calendar.DAY_OF_MONTH, i)
                val s = c.timeInMillis
                c.add(Calendar.DAY_OF_MONTH, 1)
                bucketOf(txs, s, c.timeInMillis, (i + 1).toString())
            }
        }
        Period.YEARLY -> {
            base.set(Calendar.DAY_OF_YEAR, 1)
            val fmt = SimpleDateFormat("MMM", loc)
            (0 until 12).map { i ->
                val c = base.clone() as Calendar
                c.add(Calendar.MONTH, i)
                val s = c.timeInMillis
                c.add(Calendar.MONTH, 1)
                bucketOf(txs, s, c.timeInMillis, fmt.format(Date(s)))
            }
        }
    }
}

fun categoryTotals(
    txs: List<Transaction>,
    type: TxType,
    start: Long,
    end: Long
): List<Pair<Category, Double>> =
    txs.filter { it.type == type && it.dateMillis >= start && it.dateMillis < end }
        .groupBy { it.categoryId }
        .map { (id, list) -> Categories.byId(id) to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
