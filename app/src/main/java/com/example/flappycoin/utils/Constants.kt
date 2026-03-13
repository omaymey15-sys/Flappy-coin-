package com.example.flappycoin.utils

/**
 * Configuration globale de l'application FlappyCoin
 * Toutes les constantes métier et techniques
 */

object Constants {

    // ============= CONVERSION MONÉTAIRE =============

    const val COINS_PER_DOLLAR = 10
    const val REWARDED_AD_BONUS = 15
    const val MINIMUM_WITHDRAWAL_DOLLARS = 300.0
    const val MINIMUM_WITHDRAWAL_COINS = MINIMUM_WITHDRAWAL_DOLLARS * COINS_PER_DOLLAR

    // ============= PUBLICITÉS =============

    const val REWARDED_AD_INTERVAL_MS = 5 * 60 * 1000
    const val INTERSTITIAL_SHOW_RATE = 0.35f

    // IDs AdMob TEST
    const val ADMOB_APP_ID = "ca-app-pub-1299408509965704-5438288491"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-1299408509965704/5960673880"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1299408509965704/5246862599"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-1299408509965704/5901468710"

    // ============= GAMEPLAY =============

    const val TARGET_FPS = 60
    const val FRAME_TIME_MS = 16L
    const val PIPE_GAP_MULTIPLIER = 3.5f
    const val DIFFICULTY_INCREASE_INTERVAL = 5
    const val COMBO_DURATION_MS = 5000L

    // ============= RÉCOMPENSES =============

    const val DAILY_REWARD_WEEKDAY = 10
    const val DAILY_REWARD_WEEKEND = 20

    const val SHARE_REWARD = 10
    const val INVITE_REWARD = 20

    const val MAX_COINS = 1_000_000

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

        // nouvelles clés

        const val LAST_DAILY_REWARD = "last_daily_reward"

        const val LAST_SHARE_DATE = "last_share_date"
        const val SHARE_COUNT = "share_count"

        const val LAST_INVITE_DATE = "last_invite_date"
        const val INVITE_COUNT = "invite_count"
    }
}