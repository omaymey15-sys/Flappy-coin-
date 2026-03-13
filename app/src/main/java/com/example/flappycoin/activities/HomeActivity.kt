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
import android.animation.*
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
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val handler = Handler(Looper.getMainLooper())

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

        // 🔹 Animations boutons cadeau et quotidien
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
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleY.repeatMode = ValueAnimator.REVERSE

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

        // Calculer le jour actuel pour le calendrier quotidien
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
        binding.btnDailyReward.setOnClickListener { showDailyPopup(todayIndex) }
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
            Toast.makeText(
                this,
                "Minimum: ${Constants.MINIMUM_WITHDRAWAL_DOLLARS.toInt()}$\nManque: $needed coins",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, "Retrait simulé", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Popup cadeau (partage/invitation/pub) ---
    private fun showGiftPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        // Partage (max 2 fois/jour)
        popupView.findViewById<Button>(R.id.btnShare).setOnClickListener {
            if (GamePreferences.canShareApp()) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Viens jouer à FlappyCoin ! https://fake.link/share")
                startActivity(Intent.createChooser(shareIntent, "Partager avec..."))

                it.isEnabled = false
                handler.postDelayed({
                    GamePreferences.addCoins(10)
                    GamePreferences.recordShare()
                    Toast.makeText(this, "+10 coins après partage !", Toast.LENGTH_SHORT).show()
                    updateUI()
                }, 5000)
            } else Toast.makeText(this, "Limite partage atteinte aujourd'hui", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        // Invitation (max 1 fois/jour)
        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener {
            if (GamePreferences.canInviteFriend()) {
                val inviteIntent = Intent(Intent.ACTION_SEND)
                inviteIntent.type = "text/plain"
                inviteIntent.putExtra(Intent.EXTRA_TEXT, "Invitez un ami à FlappyCoin ! https://fake.link/invite")
                startActivity(Intent.createChooser(inviteIntent, "Inviter un ami"))

                it.isEnabled = false
                handler.postDelayed({
                    GamePreferences.addCoins(10) // 10 coins pour l'invitation
                    GamePreferences.recordInvite()
                    Toast.makeText(this, "+10 coins après invitation !", Toast.LENGTH_SHORT).show()
                    updateUI()
                }, 5000)
            } else Toast.makeText(this, "Invitation déjà utilisée aujourd'hui", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        // Pub récompensée
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
    private fun showDailyPopup(today: Int) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_daily, null)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
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

        days.forEachIndexed { index, button ->
            button.isEnabled = index == today
            if (index == today) {
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