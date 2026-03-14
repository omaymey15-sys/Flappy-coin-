package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityStatsBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class StatsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStatsBinding
    private val TAG = "StatsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialiser le binding
            binding = ActivityStatsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Initialiser AdMob
            MobileAds.initialize(this) {}
            
            // Charger les statistiques
            loadStatistics()
            
            // Configurer la bannière publicitaire
            setupAdBanner()
            
            // Bouton retour
            binding.btnBack.setOnClickListener {
                finish()
            }
            
            Log.d(TAG, "StatsActivity créée avec succès")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur d'initialisation: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadStatistics() {
        try {
            // Récupérer les statistiques depuis GamePreferences
            val gamesPlayed = GamePreferences.getGamesPlayed()
            val bestScore = GamePreferences.getBestScore()
            val bestCoins = GamePreferences.getBestCoins()
            val totalDistance = GamePreferences.getTotalDistance()
            val totalTime = GamePreferences.getTotalTime()

            // Conversion en monnaie locale
            val localAmount = try {
                CurrencyManager.coinsToLocalCurrency(bestCoins)
            } catch (e: Exception) {
                "${bestCoins} FCFA"
            }

            // Calcul du score moyen (approximatif basé sur le meilleur score)
            val avgScore = if (gamesPlayed > 0) {
                (bestScore * 0.6).toInt()
            } else {
                0
            }

            // Formatage du temps
            val timeSeconds = totalTime / 1000
            val hours = timeSeconds / 3600
            val minutes = (timeSeconds % 3600) / 60
            val seconds = timeSeconds % 60
            
            val timeFormatted = when {
                hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
                minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
                else -> String.format("%ds", seconds)
            }

            // Mise à jour des TextViews (UNIQUEMENT ceux qui existent dans votre layout)
            binding.apply {
                tvTotalGames.text = gamesPlayed.toString()
                tvBestScore.text = bestScore.toString()
                tvBestCoins.text = "$bestCoins ($localAmount)"
                tvTotalDistance.text = "${totalDistance / 10}m"
                tvTotalTime.text = timeFormatted
                tvAverageScore.text = avgScore.toString()
            }

            // Log pour déboguer
            Log.d(TAG, "=== STATISTIQUES CHARGÉES ===")
            Log.d(TAG, "📊 Parties jouées: $gamesPlayed")
            Log.d(TAG, "🏆 Meilleur score: $bestScore")
            Log.d(TAG, "🪙 Meilleure collecte: $bestCoins ($localAmount)")
            Log.d(TAG, "📏 Distance totale: ${totalDistance / 10}m")
            Log.d(TAG, "⏱️ Temps total: $timeFormatted")
            Log.d(TAG, "📈 Score moyen: $avgScore")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur chargement stats: ${e.message}")
            e.printStackTrace()
            showError()
        }
    }

    private fun setupAdBanner() {
        try {
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            Log.d(TAG, "Bannière publicitaire chargée")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur chargement bannière: ${e.message}")
            binding.adView.visibility = android.view.View.GONE
        }
    }

    private fun showError() {
        binding.apply {
            tvTotalGames.text = "0"
            tvBestScore.text = "0"
            tvBestCoins.text = "0"
            tvTotalDistance.text = "0m"
            tvTotalTime.text = "0s"
            tvAverageScore.text = "0"
        }
        
        Toast.makeText(this, "Erreur de chargement des statistiques", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        try {
            binding.adView.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur onPause: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            binding.adView.resume()
            // Recharger les stats au cas où
            loadStatistics()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur onResume: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            binding.adView.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur onDestroy: ${e.message}")
        }
    }
}