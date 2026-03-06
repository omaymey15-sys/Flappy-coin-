package com.example.flappycoin.managers

import android.content.Context
import com.example.flappycoin.utils.CountryData
import com.example.flappycoin.utils.PrefsKeys

object CurrencyManager {
    private var exchangeRate = 1.0f
    private var currency = "USD"

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("FlappyCoin", Context.MODE_PRIVATE)
        currency = prefs.getString(PrefsKeys.CURRENCY, "USD") ?: "USD"
        exchangeRate = prefs.getFloat(PrefsKeys.EXCHANGE_RATE, 1.0f)
    }

    fun setExchangeRate(rate: Float) {
        exchangeRate = rate
    }

    fun setCurrency(curr: String) {
        currency = curr
    }

    // Convertir coins en devise locale
    fun coinsToLocalCurrency(coins: Int): String {
        val dollars = coins / 10f  // 10 coins = 1$
        val localAmount = dollars * exchangeRate
        return formatCurrency(localAmount)
    }

    // Convertir devise locale en coins
    fun currencyToCoins(amount: Float): Int {
        val dollars = amount / exchangeRate
        return (dollars * 10).toInt()  // 10 coins = 1$
    }

    fun formatCurrency(amount: Float): String {
        return when (currency) {
            "USD" -> "$%.2f".format(amount)
            "EUR" -> "€%.2f".format(amount)
            "GBP" -> "£%.2f".format(amount)
            "JPY" -> "¥%.0f".format(amount)
            "CNY" -> "¥%.2f".format(amount)
            "INR" -> "₹%.2f".format(amount)
            "BRL" -> "R$%.2f".format(amount)
            "RUB" -> "₽%.2f".format(amount)
            else -> "$%.2f".format(amount)
        }
    }

    fun getCurrency(): String = currency

    fun getExchangeRate(): Float = exchangeRate
}