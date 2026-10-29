package com.selavukanakku.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Local-only storage: a JSON file for entries and SharedPreferences for settings. */
class Storage(private val ctx: Context) {

    private val file: File get() = File(ctx.filesDir, "transactions.json")
    private val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load(): List<Transaction> {
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Transaction(
                    id = o.getString("id"),
                    type = TxType.valueOf(o.getString("type")),
                    amount = o.getDouble("amount"),
                    categoryId = o.getString("category"),
                    note = o.optString("note", ""),
                    dateMillis = o.getLong("date")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(list: List<Transaction>) {
        val arr = JSONArray()
        for (t in list) {
            arr.put(
                JSONObject()
                    .put("id", t.id)
                    .put("type", t.type.name)
                    .put("amount", t.amount)
                    .put("category", t.categoryId)
                    .put("note", t.note)
                    .put("date", t.dateMillis)
            )
        }
        // Write to a temp file first so a crash can never leave a half-written file.
        val tmp = File(ctx.filesDir, "transactions.json.tmp")
        tmp.writeText(arr.toString())
        if (!tmp.renameTo(file)) {
            file.writeText(arr.toString())
            tmp.delete()
        }
    }

    var language: Lang
        get() = if (prefs.getString("lang", "en") == "ta") Lang.TA else Lang.EN
        set(v) {
            prefs.edit().putString("lang", if (v == Lang.TA) "ta" else "en").apply()
        }

    var themeMode: ThemeMode
        get() = try {
            ThemeMode.valueOf(prefs.getString("theme", ThemeMode.SYSTEM.name)!!)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        set(v) {
            prefs.edit().putString("theme", v.name).apply()
        }
}
