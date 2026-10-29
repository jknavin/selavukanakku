package com.selavukanakku.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun categoryIcon(id: String): ImageVector = when (id) {
    "food" -> Icons.Rounded.Restaurant
    "groceries" -> Icons.Rounded.ShoppingCart
    "transport" -> Icons.Rounded.DirectionsBus
    "shopping" -> Icons.Rounded.ShoppingBag
    "bills" -> Icons.Rounded.ReceiptLong
    "rent" -> Icons.Rounded.Home
    "health" -> Icons.Rounded.LocalHospital
    "education" -> Icons.Rounded.School
    "entertainment" -> Icons.Rounded.Movie
    "salary" -> Icons.Rounded.Payments
    "business" -> Icons.Rounded.Storefront
    "gift" -> Icons.Rounded.CardGiftcard
    "interest" -> Icons.Rounded.Savings
    else -> Icons.Rounded.MoreHoriz
}

@Composable
fun HomeScreen(
    vm: AppViewModel,
    bottomPad: Dp,
    onAdd: (TxType) -> Unit,
    onEdit: (Transaction) -> Unit
) {
    val p = LocalPalette.current
    val lang = vm.lang
    val txs = vm.transactions

    val totals = remember(txs) {
        val (ms, me) = windowRange(Period.MONTHLY, System.currentTimeMillis())
        var income = 0.0
        var expense = 0.0
        var mIncome = 0.0
        var mExpense = 0.0
        for (t in txs) {
            val inMonth = t.dateMillis >= ms && t.dateMillis < me
            if (t.type == TxType.INCOME) {
                income += t.amount
                if (inMonth) mIncome += t.amount
            } else {
                expense += t.amount
                if (inMonth) mExpense += t.amount
            }
        }
        doubleArrayOf(income, expense, mIncome, mExpense)
    }
    val balance = totals[0] - totals[1]

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = bottomPad),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("welcome"), color = p.textDim, fontSize = 13.sp)
                    Text(
                        tr("app_name"),
                        color = p.text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                GlassCircleButton(onClick = {
                    vm.changeLang(if (lang == Lang.EN) Lang.TA else Lang.EN)
                }) {
                    Text(
                        if (lang == Lang.EN) "த" else "EN",
                        color = p.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                GlassCircleButton(onClick = {
                    vm.changeTheme(if (p.dark) ThemeMode.LIGHT else ThemeMode.DARK)
                }) {
                    Icon(
                        if (p.dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = null,
                        tint = p.text,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), strong = true, padding = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = p.textDim,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(tr("total_balance"), color = p.textDim, fontSize = 14.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    signedMoney(balance),
                    color = if (balance < 0) p.expense else p.text,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(16.dp))
                Text(tr("this_month"), color = p.textDim, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        label = tr("income"),
                        value = money(totals[2]),
                        color = p.income,
                        icon = Icons.Rounded.ArrowDownward,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = tr("expense"),
                        value = money(totals[3]),
                        color = p.expense,
                        icon = Icons.Rounded.ArrowUpward,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GradientButton(
                    text = tr("add_income"),
                    icon = Icons.Rounded.Add,
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                    modifier = Modifier.weight(1f)
                ) { onAdd(TxType.INCOME) }
                GradientButton(
                    text = tr("add_expense"),
                    icon = Icons.Rounded.Add,
                    colors = listOf(Color(0xFFFB7185), Color(0xFFE11D48)),
                    modifier = Modifier.weight(1f)
                ) { onAdd(TxType.EXPENSE) }
            }
        }

        item { SectionTitle(tr("recent")) }

        if (txs.isEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth(), padding = 24.dp) {
                    Text(
                        tr("no_tx"),
                        color = p.textDim,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(txs.take(40), key = { it.id }) { tx ->
                TxRow(tx, lang) { onEdit(tx) }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = p.textDim, fontSize = 12.sp)
            Text(
                value,
                color = p.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TxRow(tx: Transaction, lang: Lang, onClick: () -> Unit) {
    val p = LocalPalette.current
    val cat = Categories.byId(tx.categoryId)
    val col = Color(cat.color)
    val isIncome = tx.type == TxType.INCOME
    val subtitle = dateFull(tx.dateMillis, lang) +
        if (tx.note.isNotBlank()) "  •  " + tx.note else ""

    GlassCard(Modifier.fillMaxWidth(), corner = 20.dp, padding = 12.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(col.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon(cat.id),
                    contentDescription = null,
                    tint = col,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    cat.label(lang),
                    color = p.text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    color = p.textDim,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                (if (isIncome) "+" else "-") + money(tx.amount),
                color = if (isIncome) p.income else p.expense,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun GlassBottomBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val tabs = listOf(
        Triple("nav_home", Icons.Rounded.Home, 0),
        Triple("nav_charts", Icons.Rounded.BarChart, 1),
        Triple("nav_settings", Icons.Rounded.Settings, 2)
    )
    GlassCard(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        corner = 30.dp,
        padding = 8.dp,
        fill = p.bar
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            tabs.forEach { (key, icon, index) ->
                val active = selected == index
                val shape = RoundedCornerShape(24.dp)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .clip(shape)
                        .then(
                            if (active) Modifier.background(
                                Brush.horizontalGradient(listOf(p.accent, p.accent2))
                            ) else Modifier
                        )
                        .clickable { onSelect(index) }
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (active) Color.White else p.textDim,
                        modifier = Modifier.size(21.dp)
                    )
                    if (active) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            tr(key),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
