package com.selavukanakku.app

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun ChartsScreen(vm: AppViewModel, bottomPad: Dp) {
    val p = LocalPalette.current
    val lang = vm.lang
    val txs = vm.transactions

    var periodOrd by rememberSaveable { mutableIntStateOf(1) }
    var ref by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var donutOrd by rememberSaveable { mutableIntStateOf(1) } // 0 = income, 1 = expense

    val period = Period.values()[periodOrd]
    val donutType = if (donutOrd == 0) TxType.INCOME else TxType.EXPENSE
    var selected by remember(periodOrd, ref) { mutableStateOf<Int?>(null) }

    val (start, end) = windowRange(period, ref)
    val buckets = remember(txs, period, ref, lang) { buildBuckets(txs, period, ref, lang) }
    val windowTx = remember(txs, start, end) {
        txs.filter { it.dateMillis >= start && it.dateMillis < end }
    }
    val income = windowTx.filter { it.type == TxType.INCOME }.sumOf { it.amount }
    val expense = windowTx.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    val cats = remember(txs, donutType, start, end) { categoryTotals(txs, donutType, start, end) }
    val catTotal = cats.sumOf { it.second }
    val canNext = windowRange(period, shiftRef(period, ref, 1)).first <= System.currentTimeMillis()

    val chartTitle = when (period) {
        Period.DAILY -> tr("last7")
        Period.MONTHLY -> tr("by_day")
        Period.YEARLY -> tr("by_month")
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = bottomPad),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                tr("charts_title"),
                color = p.text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("daily", "monthly", "yearly").forEachIndexed { i, key ->
                    Pill(tr(key), selected = periodOrd == i, modifier = Modifier.weight(1f)) {
                        periodOrd = i
                        ref = System.currentTimeMillis()
                    }
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), padding = 8.dp, corner = 22.dp) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ArrowButton(left = true, enabled = true) { ref = shiftRef(period, ref, -1) }
                    Text(
                        periodLabel(period, ref, lang),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = p.text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    ArrowButton(left = false, enabled = canNext) { ref = shiftRef(period, ref, 1) }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryTile(tr("income"), money(income), p.income, Modifier.weight(1f))
                SummaryTile(tr("expense"), money(expense), p.expense, Modifier.weight(1f))
                SummaryTile(
                    tr("balance"),
                    signedMoney(income - expense),
                    if (income - expense < 0) p.expense else p.accent,
                    Modifier.weight(1f)
                )
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), padding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        chartTitle,
                        color = p.text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    LegendDot(p.income, tr("income"))
                    Spacer(Modifier.width(10.dp))
                    LegendDot(p.expense, tr("expense"))
                }
                Spacer(Modifier.height(8.dp))
                val sel = selected?.let { buckets.getOrNull(it) }
                Text(
                    if (sel != null) {
                        "${sel.label}:  +${money(sel.income)}   -${money(sel.expense)}"
                    } else "",
                    color = p.textDim,
                    fontSize = 12.sp,
                    modifier = Modifier.height(18.dp)
                )
                BarChart(
                    buckets = buckets,
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), padding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tr("by_category"),
                        color = p.text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Pill(tr("expense"), selected = donutOrd == 1) { donutOrd = 1 }
                    Spacer(Modifier.width(8.dp))
                    Pill(tr("income"), selected = donutOrd == 0) { donutOrd = 0 }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DonutChart(
                        data = cats,
                        centerLabel = tr("total"),
                        centerValue = "₹" + compact(catTotal),
                        modifier = Modifier.size(190.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                if (cats.isEmpty()) {
                    Text(
                        tr("no_data"),
                        color = p.textDim,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    cats.forEach { (cat, value) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(cat.color))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                cat.label(lang),
                                color = p.text,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(money(value), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                String.format("%.0f%%", value / catTotal * 100),
                                color = p.textDim,
                                fontSize = 12.sp,
                                modifier = Modifier.width(38.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArrowButton(left: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (left) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = if (enabled) p.text else p.textDim.copy(alpha = 0.3f),
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: String, color: Color, modifier: Modifier) {
    val p = LocalPalette.current
    GlassCard(modifier, corner = 20.dp, padding = 12.dp) {
        Text(label, color = p.textDim, fontSize = 12.sp, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val p = LocalPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, color = p.textDim, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
fun BarChart(
    buckets: List<Bucket>,
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val density = LocalDensity.current
    val leftPad = with(density) { 42.dp.toPx() }
    val bottomPad = with(density) { 24.dp.toPx() }
    val topPad = with(density) { 8.dp.toPx() }
    val n = buckets.size
    val rawMax = max(4.0, buckets.maxOfOrNull { max(it.income, it.expense) } ?: 0.0)
    val niceMax = niceCeil(rawMax)
    val labelStep = if (n > 12) 5 else 1

    Canvas(
        modifier.pointerInput(n, selected) {
            detectTapGestures { off ->
                if (n > 0 && off.x >= leftPad) {
                    val groupW = (size.width - leftPad) / n
                    val i = ((off.x - leftPad) / groupW).toInt().coerceIn(0, n - 1)
                    onSelect(if (selected == i) null else i)
                }
            }
        }
    ) {
        if (n == 0) return@Canvas
        val chartW = size.width - leftPad
        val chartH = size.height - bottomPad - topPad
        val groupW = chartW / n

        val axisPaint = Paint().apply {
            isAntiAlias = true
            color = p.textDim.toArgb()
            textSize = 10.sp.toPx()
            textAlign = Paint.Align.RIGHT
        }
        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = p.textDim.toArgb()
            textSize = 10.sp.toPx()
            textAlign = Paint.Align.CENTER
        }

        // Grid lines and y-axis labels
        for (i in 0..4) {
            val y = topPad + chartH * (1f - i / 4f)
            drawLine(
                color = p.outline.copy(alpha = 0.45f),
                start = Offset(leftPad, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                compact(niceMax * i / 4.0),
                leftPad - 6.dp.toPx(),
                y + 3.5.dp.toPx(),
                axisPaint
            )
        }

        val barW = groupW * (if (n > 12) 0.36f else 0.30f)
        val minH = 3.dp.toPx()
        val radius = CornerRadius(3.dp.toPx())

        buckets.forEachIndexed { i, b ->
            val cx = leftPad + groupW * i + groupW / 2f
            if (selected == i) {
                drawRoundRect(
                    color = p.glassStrong,
                    topLeft = Offset(leftPad + groupW * i, topPad),
                    size = Size(groupW, chartH),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }
            if (b.income > 0) {
                val h = max((b.income / niceMax * chartH).toFloat(), minH)
                drawRoundRect(
                    color = p.income,
                    topLeft = Offset(cx - barW, topPad + chartH - h),
                    size = Size(barW - 1f, h),
                    cornerRadius = radius
                )
            }
            if (b.expense > 0) {
                val h = max((b.expense / niceMax * chartH).toFloat(), minH)
                drawRoundRect(
                    color = p.expense,
                    topLeft = Offset(cx + 1f, topPad + chartH - h),
                    size = Size(barW - 1f, h),
                    cornerRadius = radius
                )
            }
            if (i % labelStep == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    b.label,
                    cx,
                    size.height - 6.dp.toPx(),
                    labelPaint
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<Pair<Category, Double>>,
    centerLabel: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val total = data.sumOf { it.second }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 26.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            if (total <= 0.0) {
                drawArc(
                    color = p.outline.copy(alpha = 0.5f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
            } else {
                var startAngle = -90f
                val gap = if (data.size > 1) 2f else 0f
                data.forEach { (cat, v) ->
                    val sweep = (v / total * 360.0).toFloat()
                    drawArc(
                        color = Color(cat.color),
                        startAngle = startAngle,
                        sweepAngle = max(sweep - gap, 0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, color = p.textDim, fontSize = 12.sp)
            Text(centerValue, color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
