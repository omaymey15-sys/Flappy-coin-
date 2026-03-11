package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityHomeBinding
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.NetworkManager

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("HomeActivity", "onCreate started")
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // ================= PUBS =================
            // Banner en bas du menu
            AdManager.loadBanner(binding.adView)

            // Interstitial à l'ouverture du menu
            AdManager.showInterstitial(this)

            // Rewarded toutes les 5 minutes (check automatique)
            if (AdManager.canShowRewardedAd()) {
                AdManager.showRewardedAd(this) { reward ->
                    Toast.makeText(this, "Vous avez gagné $reward coins!", Toast.LENGTH_SHORT).show()
                    GamePreferences.addCoins(reward)
                    updateUI()
                }
            }

            updateUI()
            setupListeners()
            Log.d("HomeActivity", "✅ onCreate completed")
        } catch (e: Exception) {
            Log.e("HomeActivity", "onCreate failed", e)
            Toast.makeText(this, "Erreur HomeActivity: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnPlay.setOnClickListener {
            if (!NetworkManager.isInternetAvailable(this)) {
                Toast.makeText(this, "Connexion internet requise!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            SoundManager.playTap()
            startActivity(Intent(this, GameActivity::class.java))
        }

        binding.btnShop.setOnClickListener {
            SoundManager.playTap()
            startActivity(Intent(this, ShopActivity::class.java))
        }

        binding.btnStats.setOnClickListener {
            SoundManager.playTap()
            startActivity(Intent(this, StatsActivity::class.java))
        }

        binding.btnLeaderboard.setOnClickListener {
            SoundManager.playTap()
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        binding.btnHelp.setOnClickListener {
            SoundManager.playTap()
            startActivity(Intent(this, HelpActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            SoundManager.playTap()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnWithdraw.setOnClickListener {
            checkWithdrawal()
        }
    }

    private fun updateUI() {
        try {
            Log.d("HomeActivity", "updateUI started")

            val coins = GamePreferences.getTotalCoins()
            val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
            val username = GamePreferences.getUsername() ?: "Guest"
            val bestScore = GamePreferences.getBestScore()

            binding.tvUsername.text = username
            binding.tvBalance.text = "Solde: $localAmount"
            binding.tvCoinsCount.text = "🪙 $coins"
            binding.tvBestScore.text = "Best: $bestScore"

            Log.d("HomeActivity", "✅ updateUI completed")
        } catch (e: Exception) {
            Log.e("HomeActivity", "updateUI failed", e)
            Toast.makeText(this, "Erreur mise à jour UI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkWithdrawal() {
        try {
            val totalCoins = GamePreferences.getTotalCoins()
            if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS.toInt()) {
                val needed = Constants.MINIMUM_WITHDRAWAL_COINS.toInt() - totalCoins
                Toast.makeText(
                    this,
                    "Minimum: ${Constants.MINIMUM_WITHDRAWAL_DOLLARS.toInt()}\$\nManque: $needed coins",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Retrait simulé", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "checkWithdrawal failed", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            updateUI()
        } catch (e: Exception) {
            Log.e("HomeActivity", "onResume failed", e)
        }
    }
}