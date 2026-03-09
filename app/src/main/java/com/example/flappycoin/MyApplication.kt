package com.example.flappycoin

import android.app.Application
import android.util.Log
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.utils.CrashHandler
import com.example.flappycoin.utils.LanguageManager

class MyApplication : Application() {
    
    companion object {
        private const val TAG = "MyApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            Log.d(TAG, "🚀 Application démarrage...")
            
            // ✅ Active le crash handler IMMÉDIATEMENT (avant tout le reste!)
            CrashHandler.setupGlobalCrashHandler(this)
            Log.d(TAG, "✅ CrashHandler initialisé")
            
            // Initialise les managers
            SoundManager.init(this)
            Log.d(TAG, "✅ SoundManager initialisé")
            
            CurrencyManager.init()
            Log.d(TAG, "✅ CurrencyManager initialisé")
            
            LanguageManager.init(this)
            Log.d(TAG, "✅ LanguageManager initialisé")
            
            Log.d(TAG, "✅✅✅ Application prête!")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERREUR initialisation MyApplication", e)
            e.printStackTrace()
        }
    }
}