# قرآن كريم — النسخة الحديثة (Kotlin + Jetpack Compose)

## ملفات الهاتف
ضع المجلدات في: `/storage/emulated/0/QuranHW/`
```
QuranHW/
  HI/  p0.jpg ... p608.jpg
  HA/  s1.wav s1.xls ... s114.wav s114.xls
  WI/  p0.png ... p608.png
  WA/  s1.wav s1.xls ... s114.wav s114.xls
  db_quran/db_quran.sqlite   (أو db_quran.sqlite مباشرة، أو داخل assets)
```
عند أول تشغيل يطلب التطبيق إذن "الوصول إلى كل الملفات". بدون الإذن يبحث أيضًا في
`Android/data/quran.hb.com.quran/files/QuranHW/` (لا يحتاج إذنًا).

## داخل المشروع
- `app/src/main/assets/font/` : خطوط التفسير (UthmanicHafs1 Ver09.otf و P001.otf ... P604.otf)
- بناء الـ APK : GitHub Actions (`.github/workflows/build.yml`) ← Artifacts ← `quran-debug-apk`
- محليًا : `gradle assembleDebug` (Gradle 8.9 + JDK 17)

## الأقسام
| القديم | الجديد |
|---|---|
| QMain + QImage + Utils | ui/ReaderScreen.kt |
| IndexS / IndexP / IndexJ | ui/IndexScreens.kt |
| IndexF | ui/SearchScreen.kt |
| LandMark | ui/LandmarkScreen.kt |
| Tefcir | ui/TafsirScreen.kt |
| About | ui/AboutScreen.kt |
| DbHelper | data/QuranDb.kt |
