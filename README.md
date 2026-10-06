# قرآن كريم - Quran Android App

Application Android pour la lecture du Saint Coran (Hafs et Warsh).

## Configuration requise
- Android 5.0+ (API 21 minimum)
- Android 12/13/14/15 (API 31-35) pleinement supporté

## Fonctionnalités
- Lecture du Coran page par page (604 pages)
- Deux riwayat : Hafs et Warsh
- Lecture audio des sourates
- Index par sourate, page, juz et ayat
- Signets (3 emplacements)
- Tafsir (explication)

## Structure du projet
```
QuranHW/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/quran/hb/com/quran/   # Code source
│   │   ├── assets/                     # Base de données SQLite
│   │   └── res/                        # Ressources (layouts, images)
│   └── build.gradle
├── gradle/wrapper/
├── build.gradle
├── settings.gradle
└── .github/workflows/build.yml         # GitHub Actions CI/CD
```

## Construire le APK

### Via GitHub Actions (recommandé)
1. Pousser le code sur GitHub
2. Aller dans l''onglet **Actions**
3. Le workflow se lance automatiquement
4. Télécharger le APK depuis les **Artifacts**

### En local (nécessite Android Studio ou SDK)
```bash
./gradlew assembleDebug
```
Le APK sera dans : `app/build/outputs/apk/debug/`

## Stockage des fichiers
Les images et fichiers audio sont stockés dans le répertoire privé de l''application :
```
/sdcard/Android/data/quran.hb.com.quran/files/QuranHW/
├── H/img/   # Images Hafs
├── H/aud/   # Audio Hafs (.xls + .wav)
├── W/img/   # Images Warsh
└── W/aud/   # Audio Warsh (.xls + .wav)
```
Aucune permission de stockage n''est requise sur Android 10+.

## Dépendances principales
- AndroidX AppCompat 1.7.0
- Material Components 1.12.0
- AndroidX ViewPager 1.0.0
- Apache POI 5.2.5 (lecture fichiers .xls audio index)
