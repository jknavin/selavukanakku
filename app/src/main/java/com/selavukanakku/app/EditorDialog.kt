package com.selavukanakku.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.UUID

private val AmountPattern = Regex("^\\d{0,10}(\\.\\d{0,2})?$")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorDialog(
    initialType: TxType,
    existing: Transaction?,
    lang: Lang,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((String) -> Unit)?
) {
    val p = LocalPalette.current

    var type by remember { mutableStateOf(existing?.type ?: initialType) }
    var amountText by remember {
        mutableStateOf(
            existing?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                ?: ""
        )
    }
    var catId by remember {
        mutableStateOf(existing?.categoryId ?: Categories.forType(initialType).first().id)
    }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var dateMillis by remember { mutableLongStateOf(existing?.dateMillis ?: System.currentTimeMillis()) }
    var showPicker by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val isIncome = type == TxType.INCOME
    val shape = RoundedCornerShape(30.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(shape)
                .background(p.dialog)
                .border(1.dp, p.outline.copy(alpha = 0.6f), shape)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (existing == null) {
                            if (isIncome) tr("add_income") else tr("add_expense")
                        } else tr("edit_entry"),
                        color = p.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (existing != null && onDelete != null) {
                        Icon(
                            Icons.Rounded.DeleteForever,
                            contentDescription = tr("delete"),
                            tint = p.expense,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { showConfirm = true }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(TxType.INCOME, TxType.EXPENSE).forEach { t ->
                        Pill(
                            text = if (t == TxType.INCOME) tr("income") else tr("expense"),
                            selected = type == t,
                            modifier = Modifier.weight(1f)
                        ) {
                            type = t
                            if (Categories.byId(catId).type != t) {
                                catId = Categories.forType(t).first().id
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v ->
                        if (AmountPattern.matches(v)) {
                            amountText = v
                            amountError = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr("amount")) },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text(tr("enter_amount"), color = p.expense) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors()
                )

                Spacer(Modifier.height(14.dp))

                Text(tr("category"), color = p.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Categories.forType(type).forEach { c ->
                        val selected = c.id == catId
                        val col = Color(c.color)
                        val chipShape = RoundedCornerShape(50)
                        Row(
                            modifier = Modifier
                                .clip(chipShape)
                                .background(if (selected) col.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (selected) col else p.outline.copy(alpha = 0.6f),
                                    chipShape
                                )
                                .clickable { catId = c.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                categoryIcon(c.id),
                                contentDescription = null,
                                tint = col,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                c.label(lang),
                                color = p.text,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 80) note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr("note")) },
                    maxLines = 2,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors()
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, p.outline, RoundedCornerShape(16.dp))
                        .clickable { showPicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = p.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(tr("date"), color = p.textDim, fontSize = 12.sp)
                        Text(
                            dateFull(dateMillis, lang),
                            color = p.text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(tr("cancel"), color = p.textDim)
                    }
                    GradientButton(
                        text = tr("save"),
                        icon = Icons.Rounded.Check,
                        colors = if (isIncome) {
                            listOf(Color(0xFF10B981), Color(0xFF059669))
                        } else {
                            listOf(Color(0xFFFB7185), Color(0xFFE11D48))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        val amt = amountText.toDoubleOrNull()
                        if (amt == null || amt <= 0.0) {
                            amountError = true
                        } else {
                            onSave(
                                Transaction(
                                    id = existing?.id ?: UUID.randomUUID().toString(),
                                    type = type,
                                    amount = amt,
                                    categoryId = catId,
                                    note = note.trim(),
                                    dateMillis = dateMillis
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = localToPickerUtc(dateMillis))
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dateMillis = pickerUtcToLocal(it, dateMillis) }
                    showPicker = false
                }) { Text(tr("ok")) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(tr("cancel")) }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showConfirm && existing != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = p.dialog,
            title = { Text(tr("delete_q"), color = p.text) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onDelete(existing.id)
                }) { Text(tr("delete"), color = p.expense) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(tr("cancel"), color = p.textDim)
                }
            }
        )
    }
}
