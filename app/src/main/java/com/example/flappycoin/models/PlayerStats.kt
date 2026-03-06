package com.example.flappycoin.models

data class PlayerStats(
    val bestScore: Int,
    val bestCoins: Int,
    val totalCoins: Int,
    val gamesPlayed: Int,
    val totalDistance: Int,
    val totalTime: Long,
    val averageScore: Float,
    val coinPerGame: Float
)