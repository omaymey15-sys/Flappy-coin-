package com.example.flappycoin

import android.app.Application
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.utils.LanguageManager

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialisation ordre important
        GamePreferences.init(this)
        SoundManager.init(this)
        CurrencyManager.init(this)
        LanguageManager.init(this)
        AdManager.init(this)
    }
}