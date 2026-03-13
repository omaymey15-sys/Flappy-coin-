package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
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
import com.example.flappycoin.utils.AdHelper
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.NetworkManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import android.animation.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val handler = Handler(Looper.getMainLooper())
    private var shareCountToday = 0
    private var inviteCountToday = 0

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

        // 🔹 Animations bouton avec bounce + glow
        animateButtonBounceGlow(binding.btnReward)
        animateButtonBounceGlow(binding.btnDailyReward)

        setupListeners()
        updateUI()
        // Charger pub interstitielle et récompensée
        AdHelper.loadInterstitial(this)
        AdHelper.loadRewardedAd(this)
    }

    private fun animateButtonBounceGlow(button: Button) {
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            button.elevation = 10f
            val glowAnimator = ValueAnimator.ofArgb(0x00000000, 0xFFFFFF00.toInt(), 0x00000000)
            glowAnimator.addUpdateListener { animator ->
                button.setShadowLayer(15f, 0f, 0f, animator.animatedValue as Int)
            }
            glowAnimator.duration = 1200
            glowAnimator.repeatCount = ValueAnimator.INFINITE
            glowAnimator.start()
        }

        val bounceSet = AnimatorSet()
        bounceSet.playTogether(scaleX, scaleY)
        bounceSet.duration = 800
        bounceSet.interpolator = AccelerateDecelerateInterpolator()
        bounceSet.repeatCount = ObjectAnimator.INFINITE
        bounceSet.start()
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
            if (shareCountToday >= 2) {
                Toast.makeText(this, "Limite partage atteinte (2/jour)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Préparation du partage...", Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                GamePreferences.addCoins(10)
                shareCountToday++
                Toast.makeText(this, "+10 coins (partagé) !", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 5000)
            popupWindow.dismiss()
        }

        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener {
            if (inviteCountToday >= 1) {
                Toast.makeText(this, "Limite invitation atteinte (1/jour)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Invitation en cours...", Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                GamePreferences.addCoins(10)
                inviteCountToday++
                Toast.makeText(this, "+10 coins (invité) !", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 5000)
            popupWindow.dismiss()
        }

        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener {
            AdHelper.showRewardedAd(this) { reward ->
                GamePreferences.addCoins(reward.amount)
                Toast.makeText(this, "+${reward.amount} coins (pub) !", Toast.LENGTH_SHORT).show()
                updateUI()
            }
            popupWindow.dismiss()
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