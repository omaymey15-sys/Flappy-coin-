package com.example.flappycoin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityStatsBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.AdManager

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ================= STATS =================
        val bestScore = GamePreferences.getBestScore()
        val bestCoins = GamePreferences.getBestCoins()
        val totalCoins = GamePreferences.getTotalCoins()
        val gamesPlayed = GamePreferences.getGamesPlayed()
        val totalDistance = GamePreferences.getTotalDistance()
        val totalTime = GamePreferences.getTotalTime()

        val localAmount = CurrencyManager.coinsToLocalCurrency(bestCoins)
        val avgScore = if (gamesPlayed > 0) bestScore / gamesPlayed else 0
        val timeSeconds = totalTime / 1000

        binding.tvTotalGames.text = "Parties jouées: $gamesPlayed"
        binding.tvBestScore.text = "Meilleur score: $bestScore"
        binding.tvBestCoins.text = "Meilleur collecte: $bestCoins ($localAmount)"
        binding.tvTotalDistance.text = "Distance totale: ${totalDistance / 10}m"
        binding.tvTotalTime.text = "Temps total: ${timeSeconds}s"
        binding.tvAverageScore.text = "Score moyen: $avgScore"

        // ================= BOUTON RETOUR =================
        binding.btnBack.setOnClickListener { finish() }

        // ================= PUBS =================
        // Banner en bas de la page Stats
        AdManager.loadBanner(binding.adView)

        // Interstitial à l’ouverture de StatsActivity
        AdManager.showInterstitial(this)
    }
}