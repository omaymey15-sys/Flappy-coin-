package com.example.flappycoin.models

// Import nécessaire pour companion object
import com.example.flappycoin.managers.GamePreferences

/**
 * Modèle DTO pour les statistiques du joueur
 */
data class PlayerStats(
    val username: String,
    val bestScore: Int,
    val bestCoins: Int,
    val totalCoins: Int,
    val gamesPlayed: Int,
    val totalDistance: Int,
    val totalTime: Long,
    val averageScore: Float,
    val coinPerGame: Float,
    val country: String = "",
    val currency: String = "USD"
) {
    companion object {
        fun fromPreferences(): PlayerStats {
            val bestScore = GamePreferences.getBestScore()
            val bestCoins = GamePreferences.getBestCoins()
            val totalCoins = GamePreferences.getTotalCoins()
            val gamesPlayed = GamePreferences.getGamesPlayed()
            val totalDistance = GamePreferences.getTotalDistance()
            val totalTime = GamePreferences.getTotalTime()
            val username = GamePreferences.getUsername() ?: "Guest"
            val country = GamePreferences.getCountry() ?: ""
            val currency = GamePreferences.getCurrency()

            val avgScore = if (gamesPlayed > 0) bestScore / gamesPlayed else 0
            val coinPerGame = if (gamesPlayed > 0) bestCoins / gamesPlayed.toFloat() else 0f

            return PlayerStats(
                username = username,
                bestScore = bestScore,
                bestCoins = bestCoins,
                totalCoins = totalCoins,
                gamesPlayed = gamesPlayed,
                totalDistance = totalDistance,
                totalTime = totalTime,
                averageScore = avgScore.toFloat(),
                coinPerGame = coinPerGame,
                country = country,
                currency = currency
            )
        }
    }
}