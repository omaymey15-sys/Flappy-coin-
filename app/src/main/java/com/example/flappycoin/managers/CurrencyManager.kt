package com.example.flappycoin.managers

import com.example.flappycoin.utils.Constants

/**
 * Gestion des conversions monétaires
 * Convertit coins ↔ devises locales
 */
object CurrencyManager {
    private var exchangeRate = 1.0f
    private var currency = "USD"

    fun init(gamePrefs: GamePreferences) {
        currency = gamePrefs.getCurrency()
        exchangeRate = gamePrefs.getExchangeRate()
    }

    fun setExchangeRate(rate: Float) {
        exchangeRate = rate
    }

    fun setCurrency(curr: String) {
        currency = curr
    }

    /**
     * Convertit coins en devise locale formatée
     * 10 coins = 1 USD
     */
    fun coinsToLocalCurrency(coins: Int): String {
        val dollars = coins / Constants.COINS_PER_DOLLAR.toFloat()
        val localAmount = dollars * exchangeRate
        return formatCurrency(localAmount)
    }

    /**
     * Convertit montant devise locale en coins
     */
    fun currencyToCoins(amount: Float): Int {
        val dollars = amount / exchangeRate
        return (dollars * Constants.COINS_PER_DOLLAR).toInt()
    }

    /**
     * Formate montant selon devise sélectionnée
     */
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
            "KRW" -> "₩%.0f".format(amount)
            "THB" -> "฿%.2f".format(amount)
            "MXN" -> "Mex$%.2f".format(amount)
            "CHF" -> "CHF %.2f".format(amount)
            "SEK" -> "kr %.2f".format(amount)
            "AUD" -> "A$%.2f".format(amount)
            "CAD" -> "C$%.2f".format(amount)
            "AED" -> "د.إ%.2f".format(amount)
            else -> "$%.2f".format(amount)
        }
    }

    fun getCurrency(): String = currency
    fun getExchangeRate(): Float = exchangeRate
}