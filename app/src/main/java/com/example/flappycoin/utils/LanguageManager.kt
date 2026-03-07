package com.example.flappycoin.utils

import android.content.Context
import android.content.res.Resources
import android.util.Log
import java.util.Locale

object LanguageManager {
    private lateinit var context: Context
    private var currentLanguage = "fr"
    private val TAG = "LanguageManager"

    fun init(ctx: Context) {
        context = ctx
        currentLanguage = getSelectedLanguage()
        Log.d(TAG, "Initialized with language: $currentLanguage")
    }

    fun getSelectedLanguage(): String {
        return try {
            val prefs = context.getSharedPreferences("FlappyCoin", Context.MODE_PRIVATE)
            prefs.getString("language", "fr") ?: "fr"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting language: ${e.message}")
            "fr"
        }
    }

    fun setLanguage(languageCode: String) {
        try {
            currentLanguage = languageCode
            val prefs = context.getSharedPreferences("FlappyCoin", Context.MODE_PRIVATE)
            prefs.edit().putString("language", languageCode).apply()
            Log.d(TAG, "Language set to: $languageCode")
            
            // ✅ CORRECTION : Ne pas appeler applyLanguage() ici
            // car cela peut causer un crash lors du changement de config
            // La langue sera appliquée au prochain lancement de l'activity
        } catch (e: Exception) {
            Log.e(TAG, "Error setting language: ${e.message}", e)
        }
    }

    fun applyLanguageToContext(ctx: Context): Context {
        return try {
            val locale = Locale(currentLanguage)
            Locale.setDefault(locale)
            val resources = ctx.resources
            val config = resources.configuration
            config.setLocale(locale)
            ctx.createConfigurationContext(config)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying language: ${e.message}", e)
            ctx  // Retourner le contexte original en cas d'erreur
        }
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
        "settings" to "Paramètres"
    )

    private val englishStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Play",
        "shop" to "Shop",
        "stats" to "Statistics",
        "leaderboard" to "Leaderboard",
        "help" to "Help",
        "settings" to "Settings"
    )

    private val spanishStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Jugar",
        "shop" to "Tienda",
        "stats" to "Estadísticas",
        "leaderboard" to "Tabla de clasificación",
        "help" to "Ayuda",
        "settings" to "Configuración"
    )

    private val germanStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Spielen",
        "shop" to "Shop",
        "stats" to "Statistiken",
        "leaderboard" to "Bestenliste",
        "help" to "Hilfe",
        "settings" to "Einstellungen"
    )

    private val italianStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Gioca",
        "shop" to "Negozio",
        "stats" to "Statistiche",
        "leaderboard" to "Classifica",
        "help" to "Aiuto",
        "settings" to "Impostazioni"
    )

    private val portugueseStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Jogar",
        "shop" to "Loja",
        "stats" to "Estatísticas",
        "leaderboard" to "Ranking",
        "help" to "Ajuda",
        "settings" to "Configurações"
    )

    private val russianStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Играть",
        "shop" to "Магазин",
        "stats" to "Статистика",
        "leaderboard" to "Лидеры",
        "help" to "Справка",
        "settings" to "Настройки"
    )

    private val chineseStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "玩耍",
        "shop" to "商店",
        "stats" to "统计数据",
        "leaderboard" to "排行榜",
        "help" to "帮助",
        "settings" to "设置"
    )

    private val japaneseStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "プレイ",
        "shop" to "ショップ",
        "stats" to "統計",
        "leaderboard" to "リーダーボード",
        "help" to "ヘルプ",
        "settings" to "設定"
    )

    private val koreanStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "재생",
        "shop" to "상점",
        "stats" to "통계",
        "leaderboard" to "리더보드",
        "help" to "도움말",
        "settings" to "설정"
    )

    private val arabicStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "يلعب",
        "shop" to "متجر",
        "stats" to "الإحصائيات",
        "leaderboard" to "لوحة المتصدرين",
        "help" to "مساعدة",
        "settings" to "الإعدادات"
    )

    private val hindiStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "खेलो",
        "shop" to "दुकान",
        "stats" to "आंकड़े",
        "leaderboard" to "लीडरबोर्ड",
        "help" to "मदद",
        "settings" to "सेटिंग्स"
    )

    private val turkishStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Oyna",
        "shop" to "Dükkan",
        "stats" to "İstatistikler",
        "leaderboard" to "Lider Tablosu",
        "help" to "Yardım",
        "settings" to "Ayarlar"
    )

    private val dutchStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Spelen",
        "shop" to "Winkel",
        "stats" to "Statistieken",
        "leaderboard" to "Ranglijst",
        "help" to "Help",
        "settings" to "Instellingen"
    )
}