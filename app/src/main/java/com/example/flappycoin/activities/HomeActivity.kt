package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityHomeBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.utils.NetworkManager
import com.example.flappycoin.utils.Constants

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateUI()

        binding.btnPlay.setOnClickListener {
            if (!NetworkManager.isInternetAvailable(this)) {
                Toast.makeText(this, "Connexion internet requise!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            SoundManager.playWing()
            startActivity(Intent(this, GameActivity::class.java))
        }

        binding.btnShop.setOnClickListener {
            SoundManager.playWing()
            startActivity(Intent(this, ShopActivity::class.java))
        }

        binding.btnStats.setOnClickListener {
            SoundManager.playWing()
            startActivity(Intent(this, StatsActivity::class.java))
        }

        binding.btnLeaderboard.setOnClickListener {
            SoundManager.playWing()
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        binding.btnHelp.setOnClickListener {
            SoundManager.playWing()
            startActivity(Intent(this, HelpActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            SoundManager.playWing()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnWithdraw.setOnClickListener {
            checkWithdrawal()
        }
    }

    private fun updateUI() {
        val coins = GamePreferences.getTotalCoins()
        val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
        
        binding.tvUsername.text = GamePreferences.getUsername() ?: "Guest"
        binding.tvBalance.text = "Solde: $localAmount"
        binding.tvCoinsCount.text = "🪙 $coins"
        binding.tvBestScore.text = "Best: ${GamePreferences.getBestScore()}"
    }

    private fun checkWithdrawal() {
        val totalCoins = GamePreferences.getTotalCoins()
        if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS) {
            val needed = Constants.MINIMUM_WITHDRAWAL_COINS - totalCoins
            Toast.makeText(
                this,
                "Vous devez avoir ${Constants.MINIMUM_WITHDRAWAL_DOLLARS.toInt()} USD pour retirer\n" +
                "Il vous manque ${needed / 10} coins",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, "Fonctionnalité de retrait simulée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}