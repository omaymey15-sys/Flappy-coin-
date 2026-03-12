package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.R
import com.example.flappycoin.databinding.ActivityHomeBinding
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.NetworkManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Initialisation AdMob
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {}
            override fun onAdFailedToLoad(adError: LoadAdError) { Log.e("HomeActivity", adError.message) }
        }

        // 🔹 Animations 🎁 et 📆
        binding.btnReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out1))
        binding.btnDailyReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out2))

        setupListeners()
        updateUI()
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

        binding.btnWithdraw.setOnClickListener { checkWithdrawal() }
        binding.btnReward.setOnClickListener { showGiftPopup() }
        binding.btnDailyReward.setOnClickListener { showDailyPopup() }
    }

    private fun updateUI() {
        val coins = GamePreferences.getTotalCoins()
        val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
        val username = GamePreferences.getUsername() ?: "Guest"
        val bestScore = GamePreferences.getBestScore()

        binding.tvUsername.text = username
        binding.tvBalance.text = "Solde: $localAmount"
        binding.tvCoinsCount.text = "🪙 $coins"
        binding.tvBestScore.text = "Best: $bestScore"
    }

    private fun checkWithdrawal() {
        val totalCoins = GamePreferences.getTotalCoins()
        if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS.toInt()) {
            val needed = Constants.MINIMUM_WITHDRAWAL_COINS.toInt() - totalCoins
            Toast.makeText(this, "Minimum: ${Constants.MINIMUM_WITHDRAWAL_DOLLARS.toInt()}$\nManque: $needed coins", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Retrait simulé", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Popup cadeau ---
    private fun showGiftPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        popupView.findViewById<Button>(R.id.btnShare).setOnClickListener {
            GamePreferences.addCoins(10)
            Toast.makeText(this, "+10 coins (partagé) !", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
            updateUI()
        }

        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener {
            GamePreferences.addCoins(10)
            Toast.makeText(this, "+10 coins (invité) !", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
            updateUI()
        }

        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener {
            GamePreferences.addCoins(10)
            Toast.makeText(this, "+10 coins (pub) !", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
            updateUI()
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    // --- Popup calendrier quotidien ---
    private fun showDailyPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_daily, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val days = listOf(
            popupView.findViewById<Button>(R.id.day1),
            popupView.findViewById<Button>(R.id.day2),
            popupView.findViewById<Button>(R.id.day3),
            popupView.findViewById<Button>(R.id.day4),
            popupView.findViewById<Button>(R.id.day5),
            popupView.findViewById<Button>(R.id.day6),
            popupView.findViewById<Button>(R.id.day7)
        )
        val coinsWeek = listOf(10,10,10,10,10,20,20)
        val todayIndex = (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7

        days.forEachIndexed { index, button ->
            button.isEnabled = index == todayIndex
            if (index == todayIndex) {
                button.setOnClickListener {
                    GamePreferences.addCoins(coinsWeek[index])
                    Toast.makeText(this, "+${coinsWeek[index]} coins !", Toast.LENGTH_SHORT).show()
                    button.isEnabled = false
                    popupWindow.dismiss()
                    updateUI()
                }
            }
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}