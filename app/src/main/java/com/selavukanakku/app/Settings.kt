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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    vm: AppViewModel,
    bottomPad: Dp,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    onImport: () -> Unit
) {
    val p = LocalPalette.current
    var confirmClear by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = bottomPad),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                tr("nav_settings"),
                color = p.text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(tr("language"), color = p.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pill("English", vm.lang == Lang.EN, Modifier.weight(1f)) { vm.changeLang(Lang.EN) }
                    Pill("தமிழ்", vm.lang == Lang.TA, Modifier.weight(1f)) { vm.changeLang(Lang.TA) }
                }
                Spacer(Modifier.height(18.dp))
                Text(tr("theme"), color = p.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(tr("theme_system"), vm.themeMode == ThemeMode.SYSTEM, Modifier.weight(1f)) {
                        vm.changeTheme(ThemeMode.SYSTEM)
                    }
                    Pill(tr("theme_light"), vm.themeMode == ThemeMode.LIGHT, Modifier.weight(1f)) {
                        vm.changeTheme(ThemeMode.LIGHT)
                    }
                    Pill(tr("theme_dark"), vm.themeMode == ThemeMode.DARK, Modifier.weight(1f)) {
                        vm.changeTheme(ThemeMode.DARK)
                    }
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), padding = 8.dp) {
                Text(
                    tr("data"),
                    color = p.textDim,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 4.dp)
                )
                ActionRow(Icons.Rounded.TableChart, p.income, tr("export_excel"), tr("export_excel_sub"), onExportExcel)
                ActionRow(Icons.Rounded.PictureAsPdf, p.expense, tr("export_pdf"), tr("export_pdf_sub"), onExportPdf)
                ActionRow(Icons.Rounded.FileUpload, p.accent, tr("import_data"), tr("import_sub"), onImport)
                ActionRow(
                    Icons.Rounded.DeleteSweep, p.expense, tr("clear_all"), tr("clear_sub"),
                    onClick = { confirmClear = true }
                )
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(tr("about"), color = p.textDim, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    tr("app_name") + "  v1.0.0",
                    color = p.text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(tr("about_text"), color = p.textDim, fontSize = 13.sp)
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = p.dialog,
            title = { Text(tr("clear_all"), color = p.text) },
            text = { Text(tr("clear_confirm"), color = p.textDim) },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAll()
                    confirmClear = false
                }) { Text(tr("delete"), color = p.expense) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(tr("cancel"), color = p.textDim) }
            }
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = p.textDim, fontSize = 12.sp)
        }
    }
}
