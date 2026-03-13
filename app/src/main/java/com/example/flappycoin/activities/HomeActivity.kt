package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.example.flappycoin.utils.LanguageManager
import com.example.flappycoin.utils.NetworkManager

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Initialisation langue
        LanguageManager.init(this)

        // ✅ Initialisation AdMob
        binding.adView.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())

        // ✅ Animations
        binding.btnReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out1))
        binding.btnDailyReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out2))

        setupListeners()
        updateUI()
        updateTexts()
    }

    private fun updateTexts() {
        val L = LanguageManager::getString
        binding.tvUsername.text = GamePreferences.getUsername() ?: "Guest"
        binding.btnPlay.text = L("play")
        binding.btnShop.text = L("shop")
        binding.btnStats.text = L("stats")
        binding.btnLeaderboard.text = L("leaderboard")
        binding.btnHelp.text = L("help")
        binding.btnSettings.text = L("settings")
        binding.btnWithdraw.text = L("withdraw")
        binding.btnReward.text = L("watch_ad")
        binding.btnDailyReward.text = L("revive")
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

        binding.btnShop.setOnClickListener { SoundManager.playTap(); startActivity(Intent(this, ShopActivity::class.java)) }
        binding.btnStats.setOnClickListener { SoundManager.playTap(); startActivity(Intent(this, StatsActivity::class.java)) }
        binding.btnLeaderboard.setOnClickListener { SoundManager.playTap(); startActivity(Intent(this, LeaderboardActivity::class.java)) }
        binding.btnHelp.setOnClickListener { SoundManager.playTap(); startActivity(Intent(this, HelpActivity::class.java)) }
        binding.btnSettings.setOnClickListener { SoundManager.playTap(); startActivity(Intent(this, SettingsActivity::class.java)) }

        binding.btnWithdraw.setOnClickListener { checkWithdrawal() }
        binding.btnReward.setOnClickListener { showGiftPopup() }
        binding.btnDailyReward.setOnClickListener { showDailyPopup() }
    }

    private fun updateUI() {
        val coins = GamePreferences.getTotalCoins()
        val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
        binding.tvCoinsCount.text = "🪙 $coins"
        binding.tvBalance.text = "${LanguageManager.getString("balance")}: $localAmount"
        binding.tvBestScore.text = "${LanguageManager.getString("best_score")}: ${GamePreferences.getBestScore()}"
    }

    private fun checkWithdrawal() {
        val totalCoins = GamePreferences.getTotalCoins()
        if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS.toInt()) {
            val needed = Constants.MINIMUM_WITHDRAWAL_COINS.toInt() - totalCoins
            Toast.makeText(this, "${LanguageManager.getString("minimum_withdrawal")}\nManque: $needed coins", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Retrait simulé", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Popup cadeau (Share / Invite / Watch) ---
    private fun showGiftPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        // ✅ Partage (10 coins après 5s)
        popupView.findViewById<Button>(R.id.btnShare).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(this, "Traitement...", Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                GamePreferences.addCoins(Constants.SHARE_REWARD)
                Toast.makeText(this, "+${Constants.SHARE_REWARD} coins (partagé)!", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 5000)
        }

        // ✅ Invitation (10 coins après 5s)
        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(this, "Traitement...", Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                GamePreferences.addCoins(Constants.INVITE_REWARD)
                Toast.makeText(this, "+${Constants.INVITE_REWARD} coins (invité)!", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 5000)
        }

        // ✅ Regarder pub (bonus immédiat)
        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener {
            GamePreferences.addCoins(10)
            Toast.makeText(this, "+10 coins (pub)!", Toast.LENGTH_SHORT).show()
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