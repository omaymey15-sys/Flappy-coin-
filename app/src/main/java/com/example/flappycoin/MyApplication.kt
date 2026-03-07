package com.example.flappycoin

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.utils.LanguageManager
import com.example.flappycoin.utils.CrashHandler

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
        private const val TAG = "MyApplication"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.d(TAG, "╔════════════════════════════════════╗")
        Log.d(TAG, "║   APPLICATION STARTING...          ║")
        Log.d(TAG, "╚════════════════════════════════════╝")
        
        try {
            // 🔴 INITIALISER LE CRASH HANDLER D'ABORD
            CrashHandler.init(this)
            Log.d(TAG, "✅ CrashHandler initialized")

            // ✅ Initialiser les managers critiques
            GamePreferences.init(this)
            Log.d(TAG, "✅ GamePreferences initialized")
            
            CurrencyManager.init(this)
            Log.d(TAG, "✅ CurrencyManager initialized")

            // ✅ Initialiser les autres managers en arrière-plan
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    Log.d(TAG, "Starting background initialization...")
                    
                    SoundManager.init(this)
                    Log.d(TAG, "✅ SoundManager initialized")
                    
                    LanguageManager.init(this)
                    Log.d(TAG, "✅ LanguageManager initialized")
                    
                    AdManager.init(this)
                    Log.d(TAG, "✅ AdManager initialized")
                    
                    Log.d(TAG, "✅ All managers initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "💥 Error in background initialization", e)
                }
            }, 500)
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 CRITICAL ERROR in onCreate", e)
            e.printStackTrace()
        }
    }
}