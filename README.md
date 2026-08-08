# پرسنل‌یار (StaffMate)

اپلیکیشن Android، تک‌کاربره و کاملاً Offline برای ثبت سوابق و ارزیابی پرسنل تولید.

## تکنولوژی
- Kotlin + Jetpack Compose + Material3
- Room (SQLite) برای دیتابیس محلی
- Navigation Compose
- بدون هیچ وابستگی اینترنتی یا سرویس ابری

## روش Build
```
gradle assembleDebug
```
(یا از طریق GitHub Actions در `.github/workflows/build.yml` که به‌صورت خودکار روی هر push به main اجرا می‌شود و APK را در بخش Artifacts قرار می‌دهد.)

## محل APK
`app/build/outputs/apk/debug/app-debug.apk`

## روش Backup
تنظیمات → Backup / Restore → «تهیه Backup» → انتخاب محل ذخیره فایل.

## روش Restore
تنظیمات → Backup / Restore → «بازیابی از Backup» → انتخاب فایل → تأیید.

## روش تغییر Version
در `app/build.gradle.kts`، مقادیر `versionCode` و `versionName` را افزایش دهید.
