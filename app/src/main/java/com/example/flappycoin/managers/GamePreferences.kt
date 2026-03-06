package com.example.flappycoin.managers

import android.content.Context
import android.content.SharedPreferences
import com.example.flappycoin.utils.PrefsKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GamePreferences {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("FlappyCoin", Context.MODE_PRIVATE)
    }

    // ================= UTILISATEUR =================
    fun getUsername(): String? = prefs.getString(PrefsKeys.USERNAME, null)
    fun setUsername(name: String) {
        prefs.edit().putString(PrefsKeys.USERNAME, name).apply()
    }

    fun getCountry(): String? = prefs.getString(PrefsKeys.COUNTRY, null)
    fun setCountry(country: String) {
        prefs.edit().putString(PrefsKeys.COUNTRY, country).apply()
    }

    fun getLanguage(): String = prefs.getString(PrefsKeys.LANGUAGE, "fr") ?: "fr"
    fun setLanguage(lang: String) {
        prefs.edit().putString(PrefsKeys.LANGUAGE, lang).apply()
    }

    fun getCurrency(): String = prefs.getString(PrefsKeys.CURRENCY, "USD") ?: "USD"
    fun setCurrency(curr: String) {
        prefs.edit().putString(PrefsKeys.CURRENCY, curr).apply()
    }

    fun getExchangeRate(): Float = prefs.getFloat(PrefsKeys.EXCHANGE_RATE, 1.0f)
    fun setExchangeRate(rate: Float) {
        prefs.edit().putFloat(PrefsKeys.EXCHANGE_RATE, rate).apply()
    }

    // ================= STATS JEUX =================
    fun getBestScore(): Int = prefs.getInt(PrefsKeys.BEST_SCORE, 0)
    fun setBestScore(score: Int) {
        if (score > getBestScore()) {
            prefs.edit().putInt(PrefsKeys.BEST_SCORE, score).apply()
        }
    }

    fun getBestCoins(): Int = prefs.getInt(PrefsKeys.BEST_COINS, 0)
    fun setBestCoins(coins: Int) {
        if (coins > getBestCoins()) {
            prefs.edit().putInt(PrefsKeys.BEST_COINS, coins).apply()
        }
    }

    fun getTotalCoins(): Int = prefs.getInt(PrefsKeys.TOTAL_COINS, 0)
    fun addCoins(amount: Int) {
        val total = getTotalCoins() + amount
        prefs.edit().putInt(PrefsKeys.TOTAL_COINS, total).apply()
    }

    fun removeCoins(amount: Int) {
        val total = (getTotalCoins() - amount).coerceAtLeast(0)
        prefs.edit().putInt(PrefsKeys.TOTAL_COINS, total).apply()
    }

    fun getGamesPlayed(): Int = prefs.getInt(PrefsKeys.GAMES_PLAYED, 0)
    fun incrementGames() {
        prefs.edit().putInt(PrefsKeys.GAMES_PLAYED, getGamesPlayed() + 1).apply()
    }

    fun getTotalDistance(): Int = prefs.getInt(PrefsKeys.TOTAL_DISTANCE, 0)
    fun addDistance(distance: Int) {
        prefs.edit().putInt(PrefsKeys.TOTAL_DISTANCE, getTotalDistance() + distance).apply()
    }

    fun getTotalTime(): Long = prefs.getLong(PrefsKeys.TOTAL_TIME, 0)
    fun addTime(time: Long) {
        prefs.edit().putLong(PrefsKeys.TOTAL_TIME, getTotalTime() + time).apply()
    }

    // ================= PRÉFÉRENCES =================
    fun isAudioEnabled(): Boolean = prefs.getBoolean(PrefsKeys.AUDIO_ENABLED, true)
    fun setAudioEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PrefsKeys.AUDIO_ENABLED, enabled).apply()
    }

    fun isVibrationEnabled(): Boolean = prefs.getBoolean(PrefsKeys.VIBRATION_ENABLED, true)
    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PrefsKeys.VIBRATION_ENABLED, enabled).apply()
    }

    fun getLastRewardedAdTime(): Long = prefs.getLong(PrefsKeys.LAST_REWARDED_AD_TIME, 0)
    fun setLastRewardedAdTime(time: Long) {
        prefs.edit().putLong(PrefsKeys.LAST_REWARDED_AD_TIME, time).apply()
    }

    // ================= ITEMS ACHETÉS =================
    fun getPurchasedItems(): List<String> {
        val json = prefs.getString(PrefsKeys.PURCHASED_ITEMS, "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }

    fun addPurchasedItem(itemId: String) {
        val items = getPurchasedItems().toMutableList()
        if (!items.contains(itemId)) {
            items.add(itemId)
            prefs.edit().putString(PrefsKeys.PURCHASED_ITEMS, gson.toJson(items)).apply()
        }
    }

    fun getSelectedBird(): String = prefs.getString(PrefsKeys.SELECTED_BIRD, "red") ?: "red"
    fun setSelectedBird(birdId: String) {
        prefs.edit().putString(PrefsKeys.SELECTED_BIRD, birdId).apply()
    }

    // ================= RÉINITIALISER =================
    fun resetStats() {
        prefs.edit().clear().apply()
    }
}