package com.selavukanakku.app

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val dark = when (vm.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SelavuTheme(dark = dark) {
                CompositionLocalProvider(LocalLang provides vm.lang) {
                    AppRoot(vm)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(vm: AppViewModel) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = vm.lang

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editorOpen by remember { mutableStateOf(false) }
    var editorType by remember { mutableStateOf(TxType.EXPENSE) }
    var editing by remember { mutableStateOf<Transaction?>(null) }

    fun toast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()

    suspend fun writeTo(uri: Uri?, bytes: ByteArray) {
        if (uri == null) return
        val ok = withContext(Dispatchers.IO) {
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } != null
            } catch (e: Exception) {
                false
            }
        }
        toast(S.t(if (ok) "saved" else "save_fail", lang))
    }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        scope.launch {
            val bytes = withContext(Dispatchers.Default) { Excel.write(vm.transactions, lang) }
            writeTo(uri, bytes)
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        scope.launch {
            val bytes = withContext(Dispatchers.Default) { PdfReport.build(vm.transactions, lang) }
            writeTo(uri, bytes)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val parsed = withContext(Dispatchers.IO) {
                    try {
                        var name = ""
                        ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
                            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (c.moveToFirst() && idx >= 0) name = c.getString(idx) ?: ""
                        }
                        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        if (bytes == null) null else Importer.parse(bytes, name)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (parsed.isNullOrEmpty()) {
                    toast(S.t("import_fail", lang))
                } else {
                    val added = vm.importMerge(parsed)
                    toast(S.t("imported", lang).format(added))
                }
            }
        }
    }

    GlassBackground {
        val barSpace = 96.dp
        Box(Modifier.statusBarsPadding()) {
            when (tab) {
                0 -> HomeScreen(
                    vm = vm,
                    bottomPad = barSpace + 24.dp,
                    onAdd = { type ->
                        editing = null
                        editorType = type
                        editorOpen = true
                    },
                    onEdit = { tx ->
                        editing = tx
                        editorOpen = true
                    }
                )
                1 -> ChartsScreen(vm, bottomPad = barSpace + 24.dp)
                else -> SettingsScreen(
                    vm = vm,
                    bottomPad = barSpace + 24.dp,
                    onExportExcel = { excelLauncher.launch("SelavuKanakku-${dateIso(System.currentTimeMillis())}.xlsx") },
                    onExportPdf = { pdfLauncher.launch("SelavuKanakku-${dateIso(System.currentTimeMillis())}.pdf") },
                    onImport = {
                        importLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.ms-excel",
                                "application/octet-stream"
                            )
                        )
                    }
                )
            }
        }

        GlassBottomBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
        )
    }

    if (editorOpen) {
        EditorDialog(
            initialType = editorType,
            existing = editing,
            lang = lang,
            onDismiss = { editorOpen = false },
            onSave = {
                vm.upsert(it)
                editorOpen = false
            },
            onDelete = if (editing != null) {
                { id ->
                    vm.delete(id)
                    editorOpen = false
                }
            } else null
        )
    }
}
