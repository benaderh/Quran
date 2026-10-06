# قرآن كريم - Quran Android App (Hafs & Warsh)

[![Build APK](https://github.com/benaderh/Quran/actions/workflows/build.yml/badge.svg)](https://github.com/benaderh/Quran/actions/workflows/build.yml)

Application Android pour la lecture du Saint Coran avec les deux riwayat **Hafs** et **Warsh**, lecture audio et Tafsir.

## ⬇️ Télécharger le APK

1. Aller dans l''onglet **[Actions](https://github.com/benaderh/Quran/actions)**
2. Cliquer sur le dernier workflow réussi
3. Télécharger `quran-debug-apk` dans les **Artifacts**

---

## 📁 Fichiers à copier manuellement après installation

> Les images et fichiers audio ne sont pas inclus dans l''APK (trop volumineux).
> Après installation de l''APK, copiez les dossiers suivants sur votre téléphone :

### Chemin de destination sur le téléphone :
```
/sdcard/Android/data/quran.hb.com.quran/files/QuranHW/
```

### Structure à créer :
```
QuranHW/
├── H/
│   ├── img/     ← Contenu du dossier HI/ (images Hafs : p1.jpg ... p608.jpg)
│   └── aud/     ← Contenu du dossier HA/ (audio Hafs : s1.wav + s1.xls ...)
└── W/
    ├── img/     ← Contenu du dossier WI/ (images Warsh : p1.png ... p608.png)
    └── aud/     ← Contenu du dossier WA/ (audio Warsh : s1.wav + s1.xls ...)
```

### Comment copier (Windows) :
1. Connecter le téléphone en USB (mode Transfert de fichiers)
2. Ouvrir l''Explorateur Windows
3. Naviguer vers `Ce PC > [Nom du téléphone] > Stockage interne > Android > data > quran.hb.com.quran > files`
4. Créer le dossier `QuranHW` et copier les sous-dossiers

**Note :** Le dossier `Android/data/` est accessible sur Android 15 via un gestionnaire de fichiers ou en USB.

---

## 📱 Configuration requise
- Android 5.0+ (API 21)
- Android 12/13/14/15 pleinement supporté ✅
- Espace requis : ~650 MB (images + audio)

## 🔧 Fonctionnalités
- 604 pages du Coran en images haute qualité
- Deux riwayat : Hafs (حفص) et Warsh (ورش)  
- Lecture audio par verset avec saut au bon endroit
- Index par Sourate, Page, Juz, Ayat
- 3 emplacements de signets
- Tafsir Muyassar (تفسير ميسر)

## 🏗️ Construire localement
```bash
git clone https://github.com/benaderh/Quran.git
cd Quran
./gradlew assembleDebug
```
APK dans : `app/build/outputs/apk/debug/`
