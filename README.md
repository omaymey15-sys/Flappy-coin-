# 🎮 FlappyCoin - Jeu Mobile Android

> Un jeu Flappy Bird amélioré avec système de monétisation, boutique et multi-langues!

## 📋 Table des matières

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Installation](#installation)
- [Configuration AdMob](#configuration-admob)
- [Gestion des devises](#gestion-des-devises)
- [Gameplay](#gameplay)
- [Assets Requis](#assets-requis)
- [Roadmap](#roadmap)

---

## ✨ Fonctionnalités

### 🎮 Gameplay
- ✅ Oiseau avec physique réaliste
- ✅ Obstacles variés (tuyaux, pics, mouvants)
- ✅ Système de collecte de pièces 🪙
- ✅ Difficulté progressive
- ✅ Combo multiplicateur de points
- ✅ Particules et effets visuels
- ✅ Stats en temps réel (score, distance, temps)

### 💰 Monétisation
- ✅ **Conversion**: 10 coins = 1$
- ✅ **Publicités**:
  - Bannière (bas des écrans)
  - Interstitielle (lancement/achats)
  - Récompensée (relancer après Game Over)
- ✅ **Retrait simulé**: Minimum 300$ (3000 coins)
- ✅ **Boutique**: Skins, powerups, packs pièces

### 🌍 Multi-Langues
- 🇫🇷 Français
- 🇬🇧 English
- 🇪🇸 Español
- 🇩🇪 Deutsch
- 🇮🇹 Italiano
- 🇵🇹 Português
- 🇷🇺 Русский
- 🇨🇳 中文
- 🇯🇵 日本語
- 🇰🇷 한국어
- 🇸🇦 العربية
- 🇮🇳 हिन्दी
- 🇹🇷 Türkçe
- 🇳🇱 Nederlands

### 🌐 Support Devises
Support de 45+ pays avec taux de change:
- EUR, USD, GBP, JPY, CNY, INR, BRL, RUB, etc.

### 👤 Gestion Utilisateur
- Inscription avec pseudo, pays, langue, devise
- Sauvegarde locale (SharedPreferences)
- Profil persistant
- Statistiques détaillées

---

## 🏗️ Architecture

```
FlappyCoin/
├── app/src/main/
│   ├── AndroidManifest.xml
│   │
│   ├── java/com/example/flappycoin/
│   │   ├── MyApplication.kt          (Initialisation)
│   │   │
│   │   ├── activities/
│   │   │   ├── MainActivity.kt       (Splash + vérif réseau)
│   │   │   ├── SignUpActivity.kt     (Inscription)
│   │   │   ├── HomeActivity.kt       (Menu principal)
│   │   │   ├── GameActivity.kt       (Container jeu)
│   │   │   ├── ShopActivity.kt       (Boutique)
│   │   │   ├── StatsActivity.kt      (Statistiques)
│   │   │   ├── LeaderboardActivity.kt(Classement)
│   │   │   ├── SettingsActivity.kt   (Paramètres)
│   │   │   └── HelpActivity.kt       (Aide)
│   │   │
│   │   ├── ui/
│   │   │   ├── GameView.kt           (🎮 Moteur jeu)
│   │   │   └── ShopAdapter.kt        (Adapter RecyclerView)
│   │   │
│   │   ├── managers/
│   │   │   ├── AdManager.kt          (AdMob)
│   │   │   ├── GamePreferences.kt     (Persistance)
│   │   │   ├── SoundManager.kt        (Audio)
│   │   │   └── CurrencyManager.kt     (Devises)
│   │   │
│   │   ├── models/
│   │   │   ├── PlayerStats.kt
│   │   │   └── ShopItem.kt
│   │   │
│   │   └── utils/
│   │       ├── Constants.kt           (Config globale)
│   │       ├── CountryData.kt         (Pays + devises)
│   │       ├── LanguageManager.kt     (Multi-langues)
│   │       └── NetworkManager.kt      (Vérif réseau)
│   │
│   └── res/
│       ├── layout/                    (8 fichiers XML)
│       ├── drawable/                  (Formes + images)
│       ├── mipmap/                    (Icons)
│       ├── raw/                       (Sons)
│       └── values/                    (Couleurs, thèmes, strings)
│
├── build.gradle                       (Dépendances)
├── AndroidManifest.xml
├── proguard-rules.pro
└── gradle/wrapper/                    (Gradle wrapper)
```

### Architecture logique

```
┌─────────────────────────────────────┐
│        MainActivity (Splash)         │
│     + Vérification Réseau            │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        │             │
    SignUp        Home (Menu)
        │        /  |  \  \  \
        │       /   |   \  \  \
       Game   Shop Stats Lead Help
        │
    GameView
    (SurfaceView)
```

---

## 🚀 Installation

### Prérequis
- Android Studio 2023.1+
- JDK 11+
- SDK Android 34
- Émulateur/Appareil Android 7.0+

### Étapes

#### 1. Cloner le repo
```bash
git clone https://github.com/omaymey15-sys/FlappyCoin.git
cd FlappyCoin
```

#### 2. Ouvrir dans Android Studio
```bash
open -a "Android Studio" .
```
Ou via l'interface: File > Open

#### 3. Synchroniser Gradle
L'IDE va auto-synchroniser. Si pas:
```
File > Sync Now
```

#### 4. Ajouter les assets

##### Images (res/drawable/)
```bash
mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/raw

# Images à placer:
cp background.png app/src/main/res/drawable/
cp base.png app/src/main/res/drawable/
cp pipe_green.png app/src/main/res/drawable/
cp redbird_upflap.png app/src/main/res/drawable/
cp redbird_midflap.png app/src/main/res/drawable/
cp redbird_downflap.png app/src/main/res/drawable/
cp spike.png app/src/main/res/drawable/

# Sons à placer:
cp wing.ogg app/src/main/res/raw/
cp point.ogg app/src/main/res/raw/
cp hit.ogg app/src/main/res/raw/
cp die.ogg app/src/main/res/raw/
```

**Spécifications images:**
- `background.png`: 1080x1920 px (ciel + nuages)
- `base.png`: 1080x240 px (sol)
- `pipe_green.png`: 194x1920 px (tuyau)
- `redbird_*.png`: 120x120 px (3 frames animation)
- `spike.png`: 60x60 px (obstacle pic)

**Spécifications sons (OGG):**
- `wing.ogg`: 500-1000ms
- `point.ogg`: 100-200ms
- `hit.ogg`: 200-300ms
- `die.ogg`: 500-1000ms

#### 5. Compiler et lancer

```bash
# Via Android Studio:
Run > Run 'app'

# Ou CLI:
./gradlew installDebug
```

---

## 📱 Configuration AdMob

### 1. Créer compte AdMob

1. Aller sur [Google AdMob](https://admob.google.com)
2. Se connecter avec compte Google
3. Créer app
4. Créer unités publicitaires (Banner, Interstitial, Rewarded)
5. Récupérer les IDs

### 2. Remplacer test IDs

**En DEV (test - ne pas soumettre au Play Store):**

```kotlin
// Constants.kt
const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
```

**En PROD (avant Play Store):**

```kotlin
const val ADMOB_APP_ID = "ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy"
const val BANNER_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz"
const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/aaaaaaaaaa"
const val REWARDED_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/bbbbbbbbbb"
```

Et dans `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
```

### 3. Politique AdMob

⚠️ **Important:**
- ✅ Utiliser test IDs en DEV
- ✅ Laisser au moins 5s entre pubs récompensées
- ✅ Ne pas forcer pubs pendant le jeu
- ✅ Respecter politique "no artificial traffic"
- ❌ Ne pas cliquer sur propres pubs
- ❌ Ne pas soumettre au Play Store avec test IDs

---

## 💰 Gestion des Devises

### Conversion

```
10 coins = 1 USD (configurable)
```

Exemple:
- 100 coins = $10.00 USD = €9.20 EUR
- 3000 coins = $300.00 (minimum retrait)

### Ajouter un pays

1. Éditer `CountryData.kt`
2. Ajouter entrée:

```kotlin
Country("FR", "France", "EUR", 0.92f)
```

Où:
- Code pays ISO 2 caractères
- Nom complet
- Code devise ISO
- Taux de change (1 USD = X devises)

### Ajouter une langue

1. Éditer `LanguageManager.kt`
2. Ajouter dictionnaire:

```kotlin
private val frenchStrings = mapOf(
    "app_name" to "FlappyCoin",
    "play" to "Jouer",
    // ...
)
```

---

## 🎮 Gameplay Détaillé

### Écran START
- Affiche titre "FLAPPY COIN"
- Message "TAP TO START"
- Oiseau flotte en haut

### En JEU
**HUD (Haut gauche):**
- Score (gros chiffres)
- Distance (mètres)
- Temps (mm:ss)

**HUD (Haut droit):**
- 🪙 Coins collectés

**Actions:**
- TAP = Sauter
- Éviter tuyaux
- Collecter pièces 🪙
- Maintenir combo

### GAME OVER
Affiche:
```
GAME OVER
Score: 42 | 🪙 120 | 150m | 45s

[Regarder pub pour relancer] [Retour au menu]
```

### Après Game Over
Retour Home:
- Mise à jour best score
- Sauvegarde coins
- Stats mises à jour

---

## 📦 Assets Requis

### Images PNG
| Fichier | Dimensions | Description |
|---------|-----------|------------|
| background.png | 1080x1920 | Ciel parallaxe |
| base.png | 1080x240 | Sol |
| pipe_green.png | 194x1920 | Tuyau obstacle |
| redbird_upflap.png | 120x120 | Oiseau aile haut |
| redbird_midflap.png | 120x120 | Oiseau aile milieu |
| redbird_downflap.png | 120x120 | Oiseau aile bas |
| spike.png | 60x60 | Obstacle pic |

### Sons OGG
| Fichier | Durée | Utilité |
|---------|-------|---------|
| wing.ogg | 500-1000ms | Saut |
| point.ogg | 100-200ms | Pièce |
| hit.ogg | 200-300ms | Collision |
| die.ogg | 500-1000ms | Mort |

### Outils création assets

**Images:**
- Photoshop, GIMP, Aseprite
- Compresser avec TinyPNG

**Sons:**
- Audacity, Beepbox, SFXR
- Format: OGG Vorbis

---

## 🔮 Roadmap

### v1.1 (Court terme)
- [ ] Boutique fonctionnelle
- [ ] Savecloud (Firebase)
- [ ] Achievements
- [ ] Leaderboard online

### v1.5 (Moyen terme)
- [ ] Powerups (shield, slow-mo)
- [ ] Maps thématiques
- [ ] Skins cosmétiques
- [ ] Musique de fond

### v2.0 (Long terme)
- [ ] Mode story
- [ ] Multijoueur
- [ ] Mode offline
- [ ] Réalité augmentée (AR)

---

## 🐛 Troubleshooting

### "Bitmaps non trouvés"
```
❌ Vérifier: app/src/main/res/drawable/
✅ Fichiers doivent s'appeler exactement:
   - background.png
   - base.png
   - pipe_green.png
   - redbird_upflap.png
   - redbird_midflap.png
   - redbird_downflap.png
```

### "Pas de son"
```
❌ Vérifier app/src/main/res/raw/
✅ Fichiers OGG doivent exister
❌ Vérifier GamePreferences.isAudioEnabled()
✅ Vérifier Settings > Audio ON
```

### "Lags/Ralentissements"
```
✅ Profiler: Run > Profile 'app'
✅ Vérifier CPU < 30%
✅ Vérifier Memory < 100MB
❌ Réduire nombre particules si besoin
❌ Tester sur appareil réel
```

### "Pubs non affichées"
```
❌ Vérifier CONNECTION INTERNET
❌ Vérifier AdMob IDs corrects
❌ Attendre quelques heures après création Ad Unit
✅ Utiliser test IDs en DEV
❌ Ne pas cliquer propres pubs
```

---

## 📊 Performance

- **FPS**: 60 constant
- **Memory**: < 100MB
- **Battery**: Optimisé (pause en background)
- **Réseau**: Vérification avant jeu

---

## 👨‍💻 Développement

### Tests unitaires

```bash
# Exécuter tests
./gradlew test

# Tests coverage
./gradlew testDebugUnitTestCoverage
```

### Profiling

```
Android Studio > Run > Profile 'app'

Monitorer:
- CPU usage
- Memory allocation
- Frame rate
- Battery drain
```

### APK Release

```bash
# Build release
./gradlew assembleRelease

# Signer APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore keystore.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  key_alias

# Zipalign
zipalign -v 4 app-release-unsigned.apk FlappyCoin.apk
```

---

## 📄 Licence

MIT License - Libre d'utilisation

```
Copyright (c) 2024 FlappyCoin
```

---

## 🙏 Crédits

- Inspiré de **Flappy Bird** par Dong Nguyen
- Sons: Freesound.org, SFXR
- Assets: OpenGameArt
- Framework: Android Team (Google)

---

## 📞 Support

- **GitHub**: [omaymey15-sys/FlappyCoin](https://github.com/omaymey15-sys/FlappyCoin)
- **Issues**: Rapporter bugs
- **Email**: contact@flappycoin.dev

---

## 🎉 Jouez Maintenant!

```bash
git clone https://github.com/omaymey15-sys/FlappyCoin.git
cd FlappyCoin
# Ajouter assets...
./gradlew installDebug
# Profitez! 🎮
```

**Version**: 1.0.0  
**Dernière mise à jour**: Mars 2024  
**Développé avec ❤️ en Kotlin**