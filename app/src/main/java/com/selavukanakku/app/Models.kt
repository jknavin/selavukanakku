package com.selavukanakku.app

import java.util.UUID

enum class TxType { INCOME, EXPENSE }

enum class Period { DAILY, MONTHLY, YEARLY }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class Lang { EN, TA }

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TxType,
    val amount: Double,
    val categoryId: String,
    val note: String = "",
    val dateMillis: Long
)

data class Category(
    val id: String,
    val en: String,
    val ta: String,
    val type: TxType,
    val color: Long
) {
    fun label(lang: Lang): String = if (lang == Lang.TA) ta else en
}

object Categories {
    // "Other" is always the last entry of each list (used as import fallback).
    val expense = listOf(
        Category("food", "Food", "உணவு", TxType.EXPENSE, 0xFFF97316),
        Category("groceries", "Groceries", "மளிகை", TxType.EXPENSE, 0xFF84CC16),
        Category("transport", "Transport", "போக்குவரத்து", TxType.EXPENSE, 0xFF3B82F6),
        Category("shopping", "Shopping", "ஷாப்பிங்", TxType.EXPENSE, 0xFFEC4899),
        Category("bills", "Bills", "பில்கள்", TxType.EXPENSE, 0xFFEAB308),
        Category("rent", "Rent", "வாடகை", TxType.EXPENSE, 0xFF8B5CF6),
        Category("health", "Health", "மருத்துவம்", TxType.EXPENSE, 0xFFEF4444),
        Category("education", "Education", "கல்வி", TxType.EXPENSE, 0xFF06B6D4),
        Category("entertainment", "Entertainment", "பொழுதுபோக்கு", TxType.EXPENSE, 0xFFA855F7),
        Category("other_exp", "Other", "மற்றவை", TxType.EXPENSE, 0xFF94A3B8)
    )

    val income = listOf(
        Category("salary", "Salary", "சம்பளம்", TxType.INCOME, 0xFF10B981),
        Category("business", "Business", "வியாபாரம்", TxType.INCOME, 0xFF14B8A6),
        Category("gift", "Gift", "பரிசு", TxType.INCOME, 0xFFF472B6),
        Category("interest", "Interest", "வட்டி", TxType.INCOME, 0xFF22D3EE),
        Category("other_inc", "Other", "மற்றவை", TxType.INCOME, 0xFF94A3B8)
    )

    val all: List<Category> = expense + income

    fun forType(type: TxType): List<Category> =
        if (type == TxType.INCOME) income else expense

    fun byId(id: String): Category =
        all.firstOrNull { it.id == id } ?: expense.last()

    /** Match free text (English or Tamil label) to a category of the given type. */
    fun match(text: String, type: TxType): Category {
        val t = text.trim()
        return forType(type).firstOrNull { it.en.equals(t, ignoreCase = true) || it.ta == t }
            ?: forType(type).last()
    }
}
