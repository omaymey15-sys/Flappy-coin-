package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.animation.*
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.flappycoin.R
import com.example.flappycoin.databinding.ActivityHomeBinding
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.SoundManager
import com.example.flappycoin.utils.AdHelper
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.LanguageManager
import com.example.flappycoin.utils.NetworkManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

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
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("HomeActivity", adError.message)
            }
        }

        // 🔹 Animations boutons
        animateButtonBounceGlow(binding.btnReward)
        animateButtonBounceGlow(binding.btnDailyReward)

        // 🔹 Définir tous les textes via LanguageManager
        setDynamicTexts()

        setupListeners()
        updateUI()

        // Charger pub interstitielle et récompensée
        AdHelper.loadInterstitial(this)
        AdHelper.loadRewardedAd(this)
    }

    private fun setDynamicTexts() {
        binding.btnPlay.text = LanguageManager.getString("play")
        binding.btnShop.text = LanguageManager.getString("shop")
        binding.btnStats.text = LanguageManager.getString("stats")
        binding.btnLeaderboard.text = LanguageManager.getString("leaderboard")
        binding.btnHelp.text = LanguageManager.getString("help")
        binding.btnSettings.text = LanguageManager.getString("settings")
        binding.btnWithdraw.text = LanguageManager.getString("withdraw")
        binding.btnReward.text = LanguageManager.getString("reward")
        binding.btnDailyReward.text = LanguageManager.getString("daily_reward")
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
                Toast.makeText(this, LanguageManager.getString("internet_required"), Toast.LENGTH_SHORT).show()
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
        val username = GamePreferences.getUsername() ?: LanguageManager.getString("guest")
        val bestScore = GamePreferences.getBestScore()

        binding.tvUsername.text = username
        binding.tvBalance.text = "${LanguageManager.getString("withdraw")}: $localAmount"
        binding.tvCoinsCount.text = "🪙 $coins"
        binding.tvBestScore.text = "${LanguageManager.getString("best_score")}: $bestScore"
    }

    private fun checkWithdrawal() {
        val totalCoins = GamePreferences.getTotalCoins()
        if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS.toInt()) {
            val needed = Constants.MINIMUM_WITHDRAWAL_COINS.toInt() - totalCoins
            Toast.makeText(
                this,
                "${LanguageManager.getString("minimum_withdrawal")}\n${LanguageManager.getString("missing")}: $needed ${LanguageManager.getString("coins")}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, LanguageManager.getString("withdraw_simulated"), Toast.LENGTH_SHORT).show()
        }
    }

    // --- Popup cadeau ---
    private fun showGiftPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        popupView.findViewById<Button>(R.id.btnShare).text = LanguageManager.getString("share")
        popupView.findViewById<Button>(R.id.btnInvite).text = LanguageManager.getString("invite")
        popupView.findViewById<Button>(R.id.btnWatch).text = LanguageManager.getString("watch_ad")

        popupView.findViewById<Button>(R.id.btnShare).setOnClickListener {
            if (shareCountToday >= 2) {
                Toast.makeText(this, LanguageManager.getString("share_limit"), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, LanguageManager.getString("preparing_share"), Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                GamePreferences.addCoins(10)
                shareCountToday++
                Toast.makeText(this, "+10 ${LanguageManager.getString("coins")}!", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 5000)
            popupWindow.dismiss()
        }

        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener {
            if (inviteCountToday >= 1) {
                Toast.makeText(this, LanguageManager.getString("invite_limit"), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, LanguageManager.getString("sending_invite"), Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                GamePreferences.addCoins(10)
                inviteCountToday++
                Toast.makeText(this, "+10 ${LanguageManager.getString("coins")}!", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 5000)
            popupWindow.dismiss()
        }

        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener {
            AdHelper.showRewardedAd(this) { reward ->
                GamePreferences.addCoins(reward.amount)
                Toast.makeText(this, "+${reward.amount} ${LanguageManager.getString("coins")}!", Toast.LENGTH_SHORT).show()
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
        val coinsWeek = listOf(10, 10, 10, 10, 10, 20, 20)
        val todayIndex = (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7

        days.forEachIndexed { index, button ->
            button.isEnabled = index == todayIndex
            if (index == todayIndex) {
                button.setOnClickListener {
                    GamePreferences.addCoins(coinsWeek[index])
                    Toast.makeText(this, "+${coinsWeek[index]} ${LanguageManager.getString("coins")}!", Toast.LENGTH_SHORT).show()
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