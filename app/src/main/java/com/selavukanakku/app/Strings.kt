package com.selavukanakku.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLang = staticCompositionLocalOf { Lang.EN }

@Composable
fun tr(key: String): String = S.t(key, LocalLang.current)

object S {
    fun t(key: String, lang: Lang): String {
        val pair = map[key] ?: return key
        return if (lang == Lang.TA) pair.second else pair.first
    }

    private val map: Map<String, Pair<String, String>> = mapOf(
        "app_name" to ("Selavu Kanakku" to "செலவு கணக்கு"),
        "welcome" to ("Welcome back" to "மீண்டும் வருக"),
        "total_balance" to ("Total Balance" to "மொத்த இருப்பு"),
        "this_month" to ("This Month" to "இந்த மாதம்"),
        "income" to ("Income" to "வருமானம்"),
        "expense" to ("Expense" to "செலவு"),
        "balance" to ("Balance" to "இருப்பு"),
        "add_income" to ("Add Income" to "வருமானம் சேர்"),
        "add_expense" to ("Add Expense" to "செலவு சேர்"),
        "edit_entry" to ("Edit Entry" to "பதிவைத் திருத்து"),
        "recent" to ("Recent Transactions" to "சமீபத்திய பரிவர்த்தனைகள்"),
        "no_tx" to (
            "No transactions yet. Tap Add Income or Add Expense to begin." to
                "இன்னும் பரிவர்த்தனைகள் இல்லை. தொடங்க வருமானம் அல்லது செலவு சேர்க்கவும்."
            ),
        "nav_home" to ("Home" to "முகப்பு"),
        "nav_charts" to ("Charts" to "வரைபடம்"),
        "nav_settings" to ("Settings" to "அமைப்புகள்"),
        "amount" to ("Amount" to "தொகை"),
        "category" to ("Category" to "வகை"),
        "note" to ("Note (optional)" to "குறிப்பு (விருப்பம்)"),
        "col_note" to ("Note" to "குறிப்பு"),
        "date" to ("Date" to "தேதி"),
        "type" to ("Type" to "இனம்"),
        "save" to ("Save" to "சேமி"),
        "cancel" to ("Cancel" to "ரத்து செய்"),
        "ok" to ("OK" to "சரி"),
        "delete" to ("Delete" to "நீக்கு"),
        "delete_q" to ("Delete this entry?" to "இந்த பதிவை நீக்கவா?"),
        "enter_amount" to ("Enter a valid amount" to "சரியான தொகையை உள்ளிடவும்"),
        "daily" to ("Daily" to "தினசரி"),
        "monthly" to ("Monthly" to "மாதம்"),
        "yearly" to ("Yearly" to "ஆண்டு"),
        "charts_title" to ("Expense Charts" to "செலவு வரைபடங்கள்"),
        "last7" to ("Last 7 days" to "கடந்த 7 நாட்கள்"),
        "by_day" to ("Day by day" to "நாள் வாரியாக"),
        "by_month" to ("Month by month" to "மாதம் வாரியாக"),
        "by_category" to ("By category" to "வகை வாரியாக"),
        "total" to ("Total" to "மொத்தம்"),
        "no_data" to ("No data for this period" to "இந்த காலத்திற்கு தரவு இல்லை"),
        "language" to ("Language" to "மொழி"),
        "theme" to ("Appearance" to "தோற்றம்"),
        "theme_system" to ("System" to "சாதனம்"),
        "theme_light" to ("Light" to "ஒளி"),
        "theme_dark" to ("Dark" to "இருள்"),
        "data" to ("Data" to "தரவு"),
        "export_excel" to ("Export to Excel" to "எக்செல்லுக்கு ஏற்றுமதி"),
        "export_excel_sub" to ("Save all entries as .xlsx" to "அனைத்து பதிவுகளையும் .xlsx ஆக சேமி"),
        "export_pdf" to ("Export to PDF" to "PDF-க்கு ஏற்றுமதி"),
        "export_pdf_sub" to ("Save a printable report" to "அச்சிடக்கூடிய அறிக்கையை சேமி"),
        "import_data" to ("Import from Excel / CSV" to "எக்செல் / CSV-இலிருந்து இறக்குமதி"),
        "import_sub" to ("Add entries from a file" to "கோப்பிலிருந்து பதிவுகளைச் சேர்"),
        "imported" to ("Imported %d new entries" to "%d புதிய பதிவுகள் இறக்குமதி செய்யப்பட்டன"),
        "import_fail" to (
            "Could not read entries from this file" to
                "இந்த கோப்பிலிருந்து பதிவுகளைப் படிக்க முடியவில்லை"
            ),
        "saved" to ("File saved" to "கோப்பு சேமிக்கப்பட்டது"),
        "save_fail" to ("Could not save the file" to "கோப்பைச் சேமிக்க முடியவில்லை"),
        "clear_all" to ("Delete all data" to "அனைத்து தரவையும் நீக்கு"),
        "clear_sub" to (
            "Remove every entry from this device" to
                "இந்த சாதனத்திலிருந்து அனைத்து பதிவுகளையும் நீக்கு"
            ),
        "clear_confirm" to (
            "All entries will be deleted permanently. Export a backup first if you need one." to
                "அனைத்து பதிவுகளும் நிரந்தரமாக நீக்கப்படும். தேவைப்பட்டால் முதலில் காப்புப்பிரதி எடுக்கவும்."
            ),
        "about" to ("About" to "பற்றி"),
        "about_text" to (
            "All data stays on this device. No account and no internet needed." to
                "அனைத்து தரவும் இந்த சாதனத்திலேயே இருக்கும். கணக்கு, இணையம் தேவையில்லை."
            ),
        "report_title" to ("Expense Report" to "செலவு அறிக்கை"),
        "generated" to ("Generated on" to "உருவாக்கிய தேதி"),
        "top_expense" to ("Top expense categories" to "முக்கிய செலவு வகைகள்"),
        "page" to ("Page" to "பக்கம்"),
        "sheet_tx" to ("Transactions" to "பரிவர்த்தனைகள்"),
        "sheet_summary" to ("Summary" to "சுருக்கம்"),
        "item" to ("Item" to "விவரம்")
    )
}
