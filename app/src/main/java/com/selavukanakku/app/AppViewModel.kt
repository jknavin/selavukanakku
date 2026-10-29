package com.selavukanakku.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = Storage(app)
    private val lock = Mutex()
    private val order = compareByDescending<Transaction> { it.dateMillis }

    var transactions by mutableStateOf(storage.load().sortedWith(order))
        private set

    var lang by mutableStateOf(storage.language)
        private set

    var themeMode by mutableStateOf(storage.themeMode)
        private set

    fun changeLang(value: Lang) {
        lang = value
        storage.language = value
    }

    fun changeTheme(value: ThemeMode) {
        themeMode = value
        storage.themeMode = value
    }

    fun upsert(tx: Transaction) {
        transactions = (transactions.filter { it.id != tx.id } + tx).sortedWith(order)
        persist()
    }

    fun delete(id: String) {
        transactions = transactions.filter { it.id != id }
        persist()
    }

    fun clearAll() {
        transactions = emptyList()
        persist()
    }

    /**
     * Adds imported entries, skipping ones that already exist (same type, amount,
     * category, day and note). Genuine repeats inside the file are kept.
     * Returns how many entries were added.
     */
    fun importMerge(items: List<Transaction>): Int {
        val existing = HashMap<String, Int>()
        for (t in transactions) {
            val k = key(t)
            existing[k] = (existing[k] ?: 0) + 1
        }

        val fresh = ArrayList<Transaction>()
        for (t in items) {
            val k = key(t)
            val left = existing[k] ?: 0
            if (left > 0) existing[k] = left - 1 else fresh.add(t)
        }
        if (fresh.isNotEmpty()) {
            transactions = (transactions + fresh).sortedWith(order)
            persist()
        }
        return fresh.size
    }

    private fun key(t: Transaction): String =
        "${t.type}|${t.amount}|${t.categoryId}|${startOfDay(t.dateMillis)}|${t.note.trim()}"

    private fun persist() {
        viewModelScope.launch(Dispatchers.IO) {
            lock.withLock { storage.save(transactions) }
        }
    }
}
