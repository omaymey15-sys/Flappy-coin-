package com.example.flappycoin.utils

import android.content.Context
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
            "fr" -> frenchStrings
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

    fun getString(key: String): String {
        return getStrings()[key] ?: key
    }

    // ===================== 14 LANGUES =====================

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

    private val germanStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Spielen",
        "shop" to "Shop",
        "stats" to "Statistiken",
        "leaderboard" to "Rangliste",
        "help" to "Hilfe",
        "settings" to "Einstellungen",
        "sign_up" to "Registrieren",
        "username" to "Benutzername",
        "country" to "Land",
        "language" to "Sprache",
        "currency" to "Währung",
        "register" to "Registrieren",
        "game_over" to "GAME OVER",
        "score" to "Punktzahl",
        "coins" to "Münzen",
        "withdraw" to "Abheben",
        "balance" to "Guthaben",
        "watch_ad" to "Anzeige ansehen",
        "revive" to "Wiederbeleben",
        "minimum_withdrawal" to "Mindestabhebung: \$300",
        "distance" to "Entfernung",
        "time" to "Zeit",
        "best_score" to "Beste Punktzahl",
        "total_games" to "Gespeilte Spiele",
        "average_score" to "Durchschnittspunktzahl",
        "coin_per_game" to "Münzen/Spiel"
    )

    private val italianStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Gioca",
        "shop" to "Negozio",
        "stats" to "Statistiche",
        "leaderboard" to "Classifica",
        "help" to "Aiuto",
        "settings" to "Impostazioni",
        "sign_up" to "Registrati",
        "username" to "Nome utente",
        "country" to "Paese",
        "language" to "Lingua",
        "currency" to "Valuta",
        "register" to "Registrati",
        "game_over" to "GAME OVER",
        "score" to "Punteggio",
        "coins" to "Monete",
        "withdraw" to "Preleva",
        "balance" to "Saldo",
        "watch_ad" to "Guarda Annuncio",
        "revive" to "Rianima",
        "minimum_withdrawal" to "Prelievo minimo: \$300",
        "distance" to "Distanza",
        "time" to "Tempo",
        "best_score" to "Miglior punteggio",
        "total_games" to "Partite giocate",
        "average_score" to "Punteggio medio",
        "coin_per_game" to "Monete/Partita"
    )

    private val portugueseStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Jogar",
        "shop" to "Loja",
        "stats" to "Estatísticas",
        "leaderboard" to "Classificação",
        "help" to "Ajuda",
        "settings" to "Configurações",
        "sign_up" to "Inscrever-se",
        "username" to "Nome de usuário",
        "country" to "País",
        "language" to "Idioma",
        "currency" to "Moeda",
        "register" to "Inscrever-se",
        "game_over" to "GAME OVER",
        "score" to "Pontuação",
        "coins" to "Moedas",
        "withdraw" to "Retirar",
        "balance" to "Saldo",
        "watch_ad" to "Assistir Anúncio",
        "revive" to "Reviver",
        "minimum_withdrawal" to "Retirada mínima: \$300",
        "distance" to "Distância",
        "time" to "Tempo",
        "best_score" to "Melhor pontuação",
        "total_games" to "Jogos jogados",
        "average_score" to "Pontuação média",
        "coin_per_game" to "Moedas/Jogo"
    )

    private val russianStrings = mapOf(
        "app_name" to "FlappyCoin",
        "play" to "Играть",
        "shop" to "Магазин",
        "stats" to "Статистика",
        "leaderboard" to "Таблица лидеров",
        "help" to "Помощь",
        "settings" to "Настройки",
        "sign_up" to "Регистрация",
        "username" to "Имя пользователя",
        "country" to "Страна",
        "language" to "Язык",
        "currency" to "Валюта",
        "register" to "Регистрация",
        "game_over" to "GAME OVER",
        "score" to "Счёт",
        "coins" to "Монеты",
        "withdraw" to "Вывести",
        "balance" to "Баланс",
        "watch_ad" to "Смотреть рекламу",
        "revive" to "Возродиться",
        "minimum_withdrawal" to "Минимальная сумма: \$300",
        "distance" to "Расстояние",
        "time" to "Время",
        "best_score" to "Лучший счёт",
        "total_games" to "Сыграно игр",
        "average_score" to "Средний счёт",
        "coin_per_game" to "Монеты/Игра"
    )
    
    // Les 7 dernières langues (zh, ja, ko, ar, hi, tr, nl) peuvent être ajoutées de la même façon
// ----------------- 7 dernières langues -----------------

private val chineseStrings = mapOf(
    "app_name" to "FlappyCoin",
    "play" to "开始",
    "shop" to "商店",
    "stats" to "统计",
    "leaderboard" to "排行榜",
    "help" to "帮助",
    "settings" to "设置",
    "sign_up" to "注册",
    "username" to "用户名",
    "country" to "国家",
    "language" to "语言",
    "currency" to "货币",
    "register" to "注册",
    "game_over" to "游戏结束",
    "score" to "分数",
    "coins" to "金币",
    "withdraw" to "提现",
    "balance" to "余额",
    "watch_ad" to "观看广告",
    "revive" to "复活",
    "minimum_withdrawal" to "最低提现：\$300",
    "distance" to "距离",
    "time" to "时间",
    "best_score" to "最高分",
    "total_games" to "游戏次数",
    "average_score" to "平均分",
    "coin_per_game" to "每局金币"
)

private val japaneseStrings = mapOf(
    "app_name" to "FlappyCoin",
    "play" to "プレイ",
    "shop" to "ショップ",
    "stats" to "統計",
    "leaderboard" to "リーダーボード",
    "help" to "ヘルプ",
    "settings" to "設定",
    "sign_up" to "登録",
    "username" to "ユーザー名",
    "country" to "国",
    "language" to "言語",
    "currency" to "通貨",
    "register" to "登録",
    "game_over" to "ゲームオーバー",
    "score" to "スコア",
    "coins" to "コイン",
    "withdraw" to "出金",
    "balance" to "残高",
    "watch_ad" to "広告を見る",
    "revive" to "復活",
    "minimum_withdrawal" to "最低出金：\$300",
    "distance" to "距離",
    "time" to "時間",
    "best_score" to "最高スコア",
    "total_games" to "プレイ回数",
    "average_score" to "平均スコア",
    "coin_per_game" to "1ゲームあたりコイン"
)

private val koreanStrings = mapOf(
    "app_name" to "FlappyCoin",
    "play" to "플레이",
    "shop" to "상점",
    "stats" to "통계",
    "leaderboard" to "순위표",
    "help" to "도움말",
    "settings" to "설정",
    "sign_up" to "가입",
    "username" to "사용자 이름",
    "country" to "국가",
    "language" to "언어",
    "currency" to "통화",
    "register" to "가입",
    "game_over" to "게임 오버",
    "score" to "점수",
    "coins" to "코인",
    "withdraw" to "출금",
    "balance" to "잔액",
    "watch_ad" to "광고 보기",
    "revive" to "부활",
    "minimum_withdrawal" to "최소 출금: \$300",
    "distance" to "거리",
    "time" to "시간",
    "best_score" to "최고 점수",
    "total_games" to "게임 횟수",
    "average_score" to "평균 점수",
    "coin_per_game" to "게임당 코인"
)

private val arabicStrings = mapOf(
    "app_name" to "فلاپي كوين",
    "play" to "العب",
    "shop" to "المتجر",
    "stats" to "الإحصائيات",
    "leaderboard" to "لوحة المتصدرين",
    "help" to "مساعدة",
    "settings" to "الإعدادات",
    "sign_up" to "سجل",
    "username" to "اسم المستخدم",
    "country" to "الدولة",
    "language" to "اللغة",
    "currency" to "العملة",
    "register" to "سجل",
    "game_over" to "انتهت اللعبة",
    "score" to "النقاط",
    "coins" to "عملات",
    "withdraw" to "سحب",
    "balance" to "الرصيد",
    "watch_ad" to "مشاهدة إعلان",
    "revive" to "إحياء",
    "minimum_withdrawal" to "الحد الأدنى للسحب: \$300",
    "distance" to "المسافة",
    "time" to "الوقت",
    "best_score" to "أفضل نتيجة",
    "total_games" to "عدد الألعاب",
    "average_score" to "متوسط النقاط",
    "coin_per_game" to "العملات/اللعبة"
)

private val hindiStrings = mapOf(
    "app_name" to "FlappyCoin",
    "play" to "खेलें",
    "shop" to "दुकान",
    "stats" to "सांख्यिकी",
    "leaderboard" to "लीडरबोर्ड",
    "help" to "सहायता",
    "settings" to "सेटिंग्स",
    "sign_up" to "साइन अप",
    "username" to "उपयोगकर्ता नाम",
    "country" to "देश",
    "language" to "भाषा",
    "currency" to "मुद्रा",
    "register" to "साइन अप",
    "game_over" to "गेम ओवर",
    "score" to "स्कोर",
    "coins" to "सिक्के",
    "withdraw" to "निकासी",
    "balance" to "शेष राशि",
    "watch_ad" to "विज्ञापन देखें",
    "revive" to "पुनर्जीवित",
    "minimum_withdrawal" to "न्यूनतम निकासी: \$300",
    "distance" to "दूरी",
    "time" to "समय",
    "best_score" to "सर्वोत्तम स्कोर",
    "total_games" to "कुल खेल",
    "average_score" to "औसत स्कोर",
    "coin_per_game" to "प्रति खेल सिक्के"
)

private val turkishStrings = mapOf(
    "app_name" to "FlappyCoin",
    "play" to "Oyna",
    "shop" to "Mağaza",
    "stats" to "İstatistikler",
    "leaderboard" to "Liderler",
    "help" to "Yardım",
    "settings" to "Ayarlar",
    "sign_up" to "Kayıt Ol",
    "username" to "Kullanıcı Adı",
    "country" to "Ülke",
    "language" to "Dil",
    "currency" to "Para Birimi",
    "register" to "Kayıt Ol",
    "game_over" to "OYUN BİTTİ",
    "score" to "Skor",
    "coins" to "Madeni Para",
    "withdraw" to "Çek",
    "balance" to "Bakiye",
    "watch_ad" to "Reklam İzle",
    "revive" to "Dirilt",
    "minimum_withdrawal" to "Minimum çekim: \$300",
    "distance" to "Mesafe",
    "time" to "Zaman",
    "best_score" to "En İyi Skor",
    "total_games" to "Oynanan Oyunlar",
    "average_score" to "Ortalama Skor",
    "coin_per_game" to "Oyun Başına Madeni Para"
)

private val dutchStrings = mapOf(
    "app_name" to "FlappyCoin",
    "play" to "Spelen",
    "shop" to "Winkel",
    "stats" to "Statistieken",
    "leaderboard" to "Ranglijst",
    "help" to "Help",
    "settings" to "Instellingen",
    "sign_up" to "Registreren",
    "username" to "Gebruikersnaam",
    "country" to "Land",
    "language" to "Taal",
    "currency" to "Valuta",
    "register" to "Registreren",
    "game_over" to "GAME OVER",
    "score" to "Score",
    "coins" to "Munten",
    "withdraw" to "Opnemen",
    "balance" to "Saldo",
    "watch_ad" to "Bekijk Advertentie",
    "revive" to "Herleven",
    "minimum_withdrawal" to "Minimale opname: \$300",
    "distance" to "Afstand",
    "time" to "Tijd",
    "best_score" to "Beste Score",
    "total_games" to "Gespeelde Spellen",
    "average_score" to "Gemiddelde Score",
    "coin_per_game" to "Munten/Spel"
)
fun getString(key: String): String {
    return getStrings()[key] ?: key
}
} // ← fin de l'objet LanguageManager