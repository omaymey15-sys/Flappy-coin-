package com.example.flappycoin

import android.app.Application
import android.util.Log
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.utils.CrashHandler
import com.example.flappycoin.utils.LanguageManager
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {

    companion object {
        private const val TAG = "MyApplication"
    }

    // 🔹 AppOpenManager pour gérer les App Open Ads
    lateinit var appOpenManager: AppOpenManager

    override fun onCreate() {
        super.onCreate()

        try {
            Log.d(TAG, "🚀 Application démarrage...")

            // 🔹 Initialisation de tes managers
            GamePreferences.init(this)
            Log.d(TAG, "✅ GamePreferences initialisé")

            CrashHandler.setupGlobalCrashHandler(this)
            Log.d(TAG, "✅ CrashHandler initialisé")

            SoundManager.init(this)
            Log.d(TAG, "✅ SoundManager initialisé")

            CurrencyManager.init(GamePreferences)
            Log.d(TAG, "✅ CurrencyManager initialisé")

            LanguageManager.init(this)
            Log.d(TAG, "✅ LanguageManager initialisé")

            // 🔹 Initialisation des Mobile Ads
            MobileAds.initialize(this) { initializationStatus ->
                Log.d(TAG, "✅ MobileAds initialisé: $initializationStatus")
            }

            // 🔹 Initialisation AppOpenManager
            appOpenManager = AppOpenManager(this)
            appOpenManager.loadAd() // Charger la pub dès le lancement
            Log.d(TAG, "✅ AppOpenManager initialisé et pub chargée")

            Log.d(TAG, "✅✅✅ Application prête!")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERREUR initialisation MyApplication", e)
            e.printStackTrace()
        }
    }
}