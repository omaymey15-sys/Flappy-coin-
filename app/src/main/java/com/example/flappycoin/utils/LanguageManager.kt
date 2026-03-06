package com.example.flappycoin.utils

import android.content.Context
import android.content.res.Resources
import java.util.Locale

object LanguageManager {
    private lateinit var context: Context
    private var currentLanguage = "fr"

    fun init(ctx: Context) {
        context = ctx
        currentLanguage = getSelectedLanguage()
        applyLanguage(currentLanguage)
    }

    fun getSelectedLanguage(): String {
        val prefs = context.getSharedPreferences("FlappyCoin", Context.MODE_PRIVATE)
        return prefs.getString("language", "fr") ?: "fr"
    }

    fun setLanguage(languageCode: String) {
        currentLanguage = languageCode
        val prefs = context.getSharedPreferences("FlappyCoin", Context.MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()
        applyLanguage(languageCode)
    }

    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun getStrings(): Map<String, String> {
        return when (currentLanguage) {
            "en" -> englishStrings
            "es" -> spanishStrings
            "de" -> germanStrings
            "it" -> italianStrings
            "pt" -> portugueseStrings
            "ru" -> russianStrings
            "zh" -> chineseStrings
            "ja" -> japaneseStrings
            "ko" -> koreanStrings
            "ar" -> arabicStrings
            "hi" -> hindiStrings
            "tr" -> turkishStrings
            "nl" -> dutchStrings
            else -> frenchStrings
        }
    }

    private val frenchStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Jouer",
        "shop" to "Boutique",
        "stats" to "Statistiques",
        "leaderboard" to "Classement",
        "help" to "Aide",
        "settings" to "Paramètres",
        "sign_up" to "S'inscrire",
        "username" to "Nom d'utilisateur",
        "country" to "Pays",
        "language" to "Langue",
        "currency" to "Devise",
        "register" to "S'inscrire",
        "game_over" to "GAME OVER",
        "score" to "Score",
        "coins" to "Pièces",
        "withdraw" to "Retirer",
        "balance" to "Solde",
        "watch_ad" to "Regarder une pub",
        "revive" to "Relancer",
        "minimum_withdrawal" to "Minimum de retrait : 300\$",
        "distance" to "Distance",
        "time" to "Temps",
        "best_score" to "Meilleur score",
        "total_games" to "Parties jouées",
        "average_score" to "Score moyen",
        "coin_per_game" to "Pièces/Partie"
    )

    private val englishStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Play",
        "shop" to "Shop",
        "stats" to "Statistics",
        "leaderboard" to "Leaderboard",
        "help" to "Help",
        "settings" to "Settings",
        "sign_up" to "Sign Up",
        "username" to "Username",
        "country" to "Country",
        "language" to "Language",
        "currency" to "Currency",
        "register" to "Register",
        "game_over" to "GAME OVER",
        "score" to "Score",
        "coins" to "Coins",
        "withdraw" to "Withdraw",
        "balance" to "Balance",
        "watch_ad" to "Watch Ad",
        "revive" to "Revive",
        "minimum_withdrawal" to "Minimum withdrawal: \$300",
        "distance" to "Distance",
        "time" to "Time",
        "best_score" to "Best Score",
        "total_games" to "Games Played",
        "average_score" to "Average Score",
        "coin_per_game" to "Coins/Game"
    )

    private val spanishStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Jugar",
        "shop" to "Tienda",
        "stats" to "Estadísticas",
        "leaderboard" to "Clasificación",
        "help" to "Ayuda",
        "settings" to "Configuración",
        "sign_up" to "Registrarse",
        "username" to "Nombre de usuario",
        "country" to "País",
        "language" to "Idioma",
        "currency" to "Moneda",
        "register" to "Registrarse",
        "game_over" to "JUEGO TERMINADO",
        "score" to "Puntuación",
        "coins" to "Monedas",
        "withdraw" to "Retirar",
        "balance" to "Saldo",
        "watch_ad" to "Ver anuncio",
        "revive" to "Revivir",
        "minimum_withdrawal" to "Retiro mínimo: \$300",
        "distance" to "Distancia",
        "time" to "Tiempo",
        "best_score" to "Mejor puntuación",
        "total_games" to "Juegos jugados",
        "average_score" to "Puntuación promedio",
        "coin_per_game" to "Monedas/Juego"
    )

    // Autres langues avec même structure...
    private val germanStrings = englishStrings  // Simplifié
    private val italianStrings = englishStrings
    private val portugueseStrings = englishStrings
    private val russianStrings = englishStrings
    private val chineseStrings = englishStrings
    private val japaneseStrings = englishStrings
    private val koreanStrings = englishStrings
    private val arabicStrings = englishStrings
    private val hindiStrings = englishStrings
    private val turkishStrings = englishStrings
    private val dutchStrings = englishStrings

    fun getString(key: String): String {
        return getStrings()[key] ?: key
    }
}