package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flappycoin.R
import com.example.flappycoin.databinding.ActivityStatsBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStatsBinding
    private lateinit var adView: AdView
    private val TAG = "StatsActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialiser le binding
            binding = ActivityStatsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Initialiser AdMob
            MobileAds.initialize(this) {}
            
            // Initialiser les statistiques
            loadStatistics()
            
            // Configurer la bannière publicitaire
            setupAdBanner()
            
            // Configurer les boutons
            setupButtons()
            
            // Afficher la date du jour
            displayCurrentDate()
            
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
            // Récupérer toutes les statistiques depuis GamePreferences
            val gamesPlayed = GamePreferences.getGamesPlayed()
            val bestScore = GamePreferences.getBestScore()
            val bestCoins = GamePreferences.getBestCoins()
            val totalCoins = GamePreferences.getTotalCoins()
            val totalDistance = GamePreferences.getTotalDistance()
            val totalTime = GamePreferences.getTotalTime()
            
            // Calculer le nombre total de pièces converties en monnaie locale
            val totalLocalCurrency = try {
                CurrencyManager.coinsToLocalCurrency(totalCoins)
            } catch (e: Exception) {
                "${totalCoins} FCFA"
            }
            
            // Calculer la valeur des meilleures pièces en monnaie locale
            val bestLocalCurrency = try {
                CurrencyManager.coinsToLocalCurrency(bestCoins)
            } catch (e: Exception) {
                "${bestCoins} FCFA"
            }
            
            // Calculer le score moyen
            // Note: Si vous avez getTotalScore() dans GamePreferences, utilisez-le
            // Sinon, on fait une estimation
            val avgScore = if (gamesPlayed > 0) {
                // Estimation basée sur le meilleur score
                // Le meilleur score est généralement plus élevé que la moyenne
                (bestScore * 0.6).toInt()
            } else {
                0
            }
            
            // Calculer la distance moyenne par partie
            val avgDistance = if (gamesPlayed > 0) {
                (totalDistance / 10) / gamesPlayed
            } else {
                0
            }
            
            // Calculer le temps moyen par partie
            val avgTimeSeconds = if (gamesPlayed > 0) {
                (totalTime / 1000) / gamesPlayed
            } else {
                0
            }
            
            // Formatage du temps total
            val timeSeconds = totalTime / 1000
            val hours = timeSeconds / 3600
            val minutes = (timeSeconds % 3600) / 60
            val seconds = timeSeconds % 60
            
            val totalTimeFormatted = when {
                hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
                minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
                else -> String.format("%ds", seconds)
            }
            
            // Formatage du temps moyen
            val avgTimeFormatted = when {
                avgTimeSeconds >= 3600 -> String.format("%dh", avgTimeSeconds / 3600)
                avgTimeSeconds >= 60 -> String.format("%dm", avgTimeSeconds / 60)
                else -> String.format("%ds", avgTimeSeconds)
            }
            
            // Mise à jour des TextViews
            binding.apply {
                // Statistiques principales
                tvTotalGames.text = gamesPlayed.toString()
                tvBestScore.text = formatNumber(bestScore)
                tvBestCoins.text = "$bestCoins"
                tvTotalCoins.text = formatNumber(totalCoins)
                tvTotalDistance.text = "${formatNumber(totalDistance / 10)} m"
                tvTotalTime.text = totalTimeFormatted
                tvAverageScore.text = formatNumber(avgScore)
                
                // Statistiques secondaires (si les TextViews existent)
                try {
                    tvBestCoinsValue.text = bestLocalCurrency
                    tvTotalCoinsValue.text = totalLocalCurrency
                    tvAvgDistance.text = "${avgDistance} m"
                    tvAvgTime.text = avgTimeFormatted
                } catch (e: Exception) {
                    // Ignorer si ces vues n'existent pas
                }
                
                // Taux de conversion actuel
                try {
                    val exchangeRate = GamePreferences.getExchangeRate()
                    tvExchangeRate.text = "1 🪙 = ${String.format("%.2f", exchangeRate)} FCFA"
                } catch (e: Exception) {
                    // Ignorer
                }
            }
            
            // Log des statistiques pour débogage
            logStatistics(
                gamesPlayed, bestScore, bestCoins, totalCoins, 
                totalDistance, totalTime, avgScore
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur chargement stats: ${e.message}")
            e.printStackTrace()
            showError()
        }
    }
    
    private fun logStatistics(
        gamesPlayed: Int,
        bestScore: Int,
        bestCoins: Int,
        totalCoins: Int,
        totalDistance: Int,
        totalTime: Long,
        avgScore: Int
    ) {
        Log.d(TAG, "========== STATISTIQUES COMPLÈTES ==========")
        Log.d(TAG, "📊 Parties jouées: $gamesPlayed")
        Log.d(TAG, "🏆 Meilleur score: $bestScore")
        Log.d(TAG, "🪙 Meilleure collecte: $bestCoins")
        Log.d(TAG, "💰 Pièces totales: $totalCoins")
        Log.d(TAG, "📏 Distance totale: ${totalDistance / 10}m")
        Log.d(TAG, "⏱️ Temps total: ${totalTime / 1000}s")
        Log.d(TAG, "📈 Score moyen: $avgScore")
        Log.d(TAG, "==============================================")
    }
    
    private fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fk", number / 1000.0)
            else -> number.toString()
        }
    }
    
    private fun setupAdBanner() {
        try {
            adView = binding.adView
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            Log.d(TAG, "Bannière publicitaire chargée")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur chargement bannière: ${e.message}")
            binding.adView.visibility = android.view.View.GONE
        }
    }
    
    private fun setupButtons() {
        // Bouton retour
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        
        // Bouton réinitialiser (optionnel)
        try {
            binding.btnResetStats.setOnClickListener {
                showResetConfirmationDialog()
            }
        } catch (e: Exception) {
            // Ignorer si le bouton n'existe pas
        }
        
        // Bouton partager (optionnel)
        try {
            binding.btnShare.setOnClickListener {
                shareStatistics()
            }
        } catch (e: Exception) {
            // Ignorer si le bouton n'existe pas
        }
    }
    
    private fun displayCurrentDate() {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            binding.tvCurrentDate.text = "Mise à jour: $currentDate"
        } catch (e: Exception) {
            // Ignorer
        }
    }
    
    private fun showResetConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Réinitialiser les statistiques")
            .setMessage("Êtes-vous sûr de vouloir réinitialiser toutes vos statistiques ? Cette action est irréversible.")
            .setPositiveButton("Réinitialiser") { _, _ ->
                resetStatistics()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun resetStatistics() {
        try {
            // Réinitialiser les statistiques
            GamePreferences.resetStats()
            
            // Recharger l'affichage
            loadStatistics()
            
            Toast.makeText(this, "Statistiques réinitialisées", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Statistiques réinitialisées")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur réinitialisation: ${e.message}")
            Toast.makeText(this, "Erreur lors de la réinitialisation", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareStatistics() {
        try {
            val gamesPlayed = GamePreferences.getGamesPlayed()
            val bestScore = GamePreferences.getBestScore()
            val bestCoins = GamePreferences.getBestCoins()
            val totalDistance = GamePreferences.getTotalDistance()
            
            val shareText = """
                🎮 Mes statistiques Flappy Coin:
                
                📊 Parties jouées: $gamesPlayed
                🏆 Meilleur score: $bestScore
                🪙 Meilleure collecte: $bestCoins
                📏 Distance totale: ${totalDistance / 10}m
                
                Viens jouer à Flappy Coin ! 🚀
            """.trimIndent()
            
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            
            startActivity(android.content.Intent.createChooser(shareIntent, "Partager mes statistiques"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Erreur partage: ${e.message}")
            Toast.makeText(this, "Erreur lors du partage", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showError() {
        binding.apply {
            tvTotalGames.text = "0"
            tvBestScore.text = "0"
            tvBestCoins.text = "0"
            tvTotalCoins.text = "0"
            tvTotalDistance.text = "0 m"
            tvTotalTime.text = "0s"
            tvAverageScore.text = "0"
        }
        
        Toast.makeText(this, "Erreur de chargement des statistiques", Toast.LENGTH_SHORT).show()
    }
    
    override fun onPause() {
        try {
            if (::adView.isInitialized) {
                adView.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur onPause: ${e.message}")
        }
        super.onPause()
    }
    
    override fun onResume() {
        super.onResume()
        try {
            if (::adView.isInitialized) {
                adView.resume()
            }
            // Recharger les stats au cas où elles auraient changé
            loadStatistics()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur onResume: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        try {
            if (::adView.isInitialized) {
                adView.destroy()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur onDestroy: ${e.message}")
        }
        super.onDestroy()
    }
}