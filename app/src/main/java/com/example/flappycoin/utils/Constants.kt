package com.example.flappycoin.utils

/**

Configuration globale de l'application FlappyCoin

Toutes les constantes métier et techniques
*/


object Constants {
// ============= CONVERSION MONÉTAIRE =============
const val COINS_PER_DOLLAR = 10           // 10 coins = 1 USD
const val REWARDED_AD_BONUS = 15          // Bonus par pub récompensée
const val MINIMUM_WITHDRAWAL_DOLLARS = 300.0
const val MINIMUM_WITHDRAWAL_COINS = MINIMUM_WITHDRAWAL_DOLLARS * COINS_PER_DOLLAR

// ============= PUBLICITÉS =============  
const val REWARDED_AD_INTERVAL_MS = 5 * 60 * 1000  // 5 minutes minimum  
const val INTERSTITIAL_SHOW_RATE = 0.35f           // 35% chance  

// IDs AdMob TEST - À remplacer en production!  
const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"  
const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"  
const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"  
const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"  

// ============= JEU =============  
const val TARGET_FPS = 60  
const val FRAME_TIME_MS = 16L  
const val PIPE_GAP_MULTIPLIER = 3.5f  
const val DIFFICULTY_INCREASE_INTERVAL = 5  // Tous les 5 gaps  
const val COMBO_DURATION_MS = 5000L  // 5 secondes  

// ============= LANGUES =============  
object LanguageCodes {  
    const val FRENCH = "fr"  
    const val ENGLISH = "en"  
    const val SPANISH = "es"  
    const val GERMAN = "de"  
    const val ITALIAN = "it"  
    const val PORTUGUESE = "pt"  
    const val RUSSIAN = "ru"  
    const val CHINESE = "zh"  
    const val JAPANESE = "ja"  
    const val KOREAN = "ko"  
    const val ARABIC = "ar"  
    const val HINDI = "hi"  
    const val TURKISH = "tr"  
    const val DUTCH = "nl"  
}  

// ============= CLÉS PREFERENCES =============  
object PrefsKeys {  
    const val USERNAME = "username"  
    const val COUNTRY = "country"  
    const val LANGUAGE = "language"  
    const val CURRENCY = "currency"  
    const val EXCHANGE_RATE = "exchange_rate"  
    const val BEST_SCORE = "best_score"  
    const val BEST_COINS = "best_coins"  
    const val TOTAL_COINS = "total_coins"  
    const val GAMES_PLAYED = "games_played"  
    const val TOTAL_DISTANCE = "total_distance"  
    const val TOTAL_TIME = "total_time"  
    const val AUDIO_ENABLED = "audio_enabled"  
    const val VIBRATION_ENABLED = "vibration_enabled"  
    const val LAST_REWARDED_AD_TIME = "last_rewarded_ad_time"  
    const val PURCHASED_ITEMS = "purchased_items"  
    const val SELECTED_BIRD = "selected_bird"  
}

}