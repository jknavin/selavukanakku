# Selavu Kanakku (செலவு கணக்கு)

Offline expense tracker for Android. Native Kotlin + Jetpack Compose, glassmorphism UI with light and dark mode.

## Features
- Home: total balance, this-month income and expense, Add Income / Add Expense, recent transactions (tap to edit or delete)
- Charts: Daily (last 7 days), Monthly (day by day), Yearly (month by month) bar charts, plus a category donut
- Languages: English and Tamil (switch from the home screen or Settings)
- Theme: System, Light or Dark
- Export: Excel (.xlsx, two sheets) and PDF report
- Import: .xlsx or .csv with columns `Date, Type, Category, Amount, Note` (dates as yyyy-MM-dd or dd/MM/yyyy; Type is Income or Expense). Duplicates already in the app are skipped. Re-importing an export works in either language.
- Storage: local only (a JSON file in app storage). No internet permission, no account.

## Build with GitHub
1. Push this folder to a GitHub repository (branch `main`).
2. Open the **Actions** tab. The **Build APK** workflow runs on every push, or run it manually.
3. Download `SelavuKanakku-debug-apk` from the run's Artifacts. It is a debug-signed APK you can install directly.

## Build locally
Open the folder in Android Studio (Koala or newer, JDK 17) and run the `app` configuration, or `gradle assembleDebug`.

## Release signing
The workflow produces a debug APK. For Play Store or a release build, add a signing config and keystore as GitHub secrets.

## Project layout
`app/src/main/java/com/selavukanakku/app/`
- `MainActivity.kt` navigation and file pickers
- `Home.kt`, `Charts.kt`, `Settings.kt`, `EditorDialog.kt` screens
- `Theme.kt` glass palette and components
- `Excel.kt`, `Importer.kt`, `PdfReport.kt` import / export
- `Strings.kt` English and Tamil text
