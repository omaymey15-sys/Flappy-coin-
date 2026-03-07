package com.example.flappycoin.managers

import android.content.Context
import android.content.SharedPreferences
import com.example.flappycoin.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Gestion persistance données via SharedPreferences
 * Sauvegarde locale de toutes les stats et préférences
 */
object GamePreferences {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("FlappyCoin", Context.MODE_PRIVATE)
    }

    // ============= UTILISATEUR =============

    fun getUsername(): String? = prefs.getString(Constants.PrefsKeys.USERNAME, null)
    fun setUsername(name: String) {
        prefs.edit().putString(Constants.PrefsKeys.USERNAME, name).apply()
    }

    fun getCountry(): String? = prefs.getString(Constants.PrefsKeys.COUNTRY, null)
    fun setCountry(country: String) {
        prefs.edit().putString(Constants.PrefsKeys.COUNTRY, country).apply()
    }

    fun getLanguage(): String = 
        prefs.getString(Constants.PrefsKeys.LANGUAGE, "fr") ?: "fr"
    fun setLanguage(lang: String) {
        prefs.edit().putString(Constants.PrefsKeys.LANGUAGE, lang).apply()
    }

    fun getCurrency(): String = 
        prefs.getString(Constants.PrefsKeys.CURRENCY, "USD") ?: "USD"
    fun setCurrency(curr: String) {
        prefs.edit().putString(Constants.PrefsKeys.CURRENCY, curr).apply()
    }

    fun getExchangeRate(): Float = 
        prefs.getFloat(Constants.PrefsKeys.EXCHANGE_RATE, 1.0f)
    fun setExchangeRate(rate: Float) {
        prefs.edit().putFloat(Constants.PrefsKeys.EXCHANGE_RATE, rate).apply()
    }

    // ============= STATS JEUX =============

    fun getBestScore(): Int = prefs.getInt(Constants.PrefsKeys.BEST_SCORE, 0)
    fun setBestScore(score: Int) {
        if (score > getBestScore()) {
            prefs.edit().putInt(Constants.PrefsKeys.BEST_SCORE, score).apply()
        }
    }

    fun getBestCoins(): Int = prefs.getInt(Constants.PrefsKeys.BEST_COINS, 0)
    fun setBestCoins(coins: Int) {
        if (coins > getBestCoins()) {
            prefs.edit().putInt(Constants.PrefsKeys.BEST_COINS, coins).apply()
        }
    }

    fun getTotalCoins(): Int = prefs.getInt(Constants.PrefsKeys.TOTAL_COINS, 0)
    fun addCoins(amount: Int) {
        val total = getTotalCoins() + amount
        prefs.edit().putInt(Constants.PrefsKeys.TOTAL_COINS, total).apply()
    }

    fun removeCoins(amount: Int) {
        val total = (getTotalCoins() - amount).coerceAtLeast(0)
        prefs.edit().putInt(Constants.PrefsKeys.TOTAL_COINS, total).apply()
    }

    fun getGamesPlayed(): Int = prefs.getInt(Constants.PrefsKeys.GAMES_PLAYED, 0)
    fun incrementGames() {
        prefs.edit().putInt(Constants.PrefsKeys.GAMES_PLAYED, getGamesPlayed() + 1).apply()
    }

    fun getTotalDistance(): Int = prefs.getInt(Constants.PrefsKeys.TOTAL_DISTANCE, 0)
    fun addDistance(distance: Int) {
        prefs.edit().putInt(Constants.PrefsKeys.TOTAL_DISTANCE, getTotalDistance() + distance).apply()
    }

    fun getTotalTime(): Long = prefs.getLong(Constants.PrefsKeys.TOTAL_TIME, 0)
    fun addTime(time: Long) {
        prefs.edit().putLong(Constants.PrefsKeys.TOTAL_TIME, getTotalTime() + time).apply()
    }

    // ============= PRÉFÉRENCES =============

    fun isAudioEnabled(): Boolean = 
        prefs.getBoolean(Constants.PrefsKeys.AUDIO_ENABLED, true)
    fun setAudioEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.PrefsKeys.AUDIO_ENABLED, enabled).apply()
    }

    fun isVibrationEnabled(): Boolean = 
        prefs.getBoolean(Constants.PrefsKeys.VIBRATION_ENABLED, true)
    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.PrefsKeys.VIBRATION_ENABLED, enabled).apply()
    }

    fun getLastRewardedAdTime(): Long = 
        prefs.getLong(Constants.PrefsKeys.LAST_REWARDED_AD_TIME, 0)
    fun setLastRewardedAdTime(time: Long) {
        prefs.edit().putLong(Constants.PrefsKeys.LAST_REWARDED_AD_TIME, time).apply()
    }

    // ============= ITEMS ACHETÉS =============

    fun getPurchasedItems(): List<String> {
        val json = prefs.getString(Constants.PrefsKeys.PURCHASED_ITEMS, "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }

    fun addPurchasedItem(itemId: String) {
        val items = getPurchasedItems().toMutableList()
        if (!items.contains(itemId)) {
            items.add(itemId)
            prefs.edit()
                .putString(Constants.PrefsKeys.PURCHASED_ITEMS, gson.toJson(items))
                .apply()
        }
    }

    fun getSelectedBird(): String = 
        prefs.getString(Constants.PrefsKeys.SELECTED_BIRD, "red") ?: "red"
    fun setSelectedBird(birdId: String) {
        prefs.edit().putString(Constants.PrefsKeys.SELECTED_BIRD, birdId).apply()
    }

    // ============= RÉINITIALISER =============

    fun resetStats() {
        prefs.edit().clear().apply()
    }
}