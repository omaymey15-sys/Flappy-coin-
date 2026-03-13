package com.example.flappycoin.activities

import android.animation.*
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
import com.example.flappycoin.utils.*
import com.google.android.gms.ads.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val handler = Handler(Looper.getMainLooper())

    private var shareCountToday = 0
    private var inviteCountToday = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LanguageManager.init(this)

        initAds()
        setupListeners()
        startAnimations()

        updateUI()

        AdHelper.loadInterstitial(this)
        AdHelper.loadRewardedAd(this)
    }

    // ---------------- ADS ----------------

    private fun initAds() {

        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()

        binding.adView.loadAd(adRequest)

        binding.adView.adListener = object : AdListener() {

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("HomeActivity", adError.message)
            }
        }
    }

    // ---------------- ANIMATIONS ----------------

    private fun startAnimations() {

        animateButton(binding.btnReward)
        animateButton(binding.btnDailyReward)

        val logoAnim = ObjectAnimator.ofFloat(binding.imgLogo, "rotation", -2f, 2f)
        logoAnim.duration = 1500
        logoAnim.repeatCount = ObjectAnimator.INFINITE
        logoAnim.repeatMode = ObjectAnimator.REVERSE
        logoAnim.start()
    }

    private fun animateButton(button: Button) {

        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.15f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.15f, 1f)

        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY)

        set.duration = 900
        set.interpolator = AccelerateDecelerateInterpolator()
        set.repeatCount = ObjectAnimator.INFINITE

        set.start()
    }

    // ---------------- LISTENERS ----------------

    private fun setupListeners() {

        binding.btnPlay.setOnClickListener {

            if (!NetworkManager.isInternetAvailable(this)) {

                Toast.makeText(
                    this,
                    LanguageManager.getString("internet_required"),
                    Toast.LENGTH_SHORT
                ).show()

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

    // ---------------- UPDATE UI ----------------

    private fun updateUI() {

        val coins = GamePreferences.getTotalCoins()

        val localAmount = CurrencyManager.coinsToLocalCurrency(coins)

        val username =
            GamePreferences.getUsername() ?: LanguageManager.getString("guest")

        val bestScore = GamePreferences.getBestScore()

        binding.tvUsername.text = username

        binding.tvBalance.text =
            "${LanguageManager.getString("balance")}: $localAmount"

        binding.tvCoinsCount.text = "🪙 $coins"

        binding.tvBestScore.text =
            "${LanguageManager.getString("best_score")}: $bestScore"
    }

    // ---------------- WITHDRAW ----------------

    private fun checkWithdrawal() {

        val totalCoins = GamePreferences.getTotalCoins()

        if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS.toInt()) {

            val needed =
                Constants.MINIMUM_WITHDRAWAL_COINS.toInt() - totalCoins

            Toast.makeText(
                this,
                "${LanguageManager.getString("minimum_withdrawal")}\n$needed coins",
                Toast.LENGTH_LONG
            ).show()

        } else {

            Toast.makeText(
                this,
                LanguageManager.getString("withdraw"),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---------------- POPUP REWARD ----------------

    private fun showGiftPopup() {

        val popupView =
            LayoutInflater.from(this).inflate(R.layout.popup_gift, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener {

            AdHelper.showRewardedAd(this) { reward ->

                GamePreferences.addCoins(reward.amount)

                Toast.makeText(
                    this,
                    "+${reward.amount} coins",
                    Toast.LENGTH_SHORT
                ).show()

                updateUI()
            }

            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    // ---------------- DAILY REWARD ----------------

    private fun showDailyPopup() {

        val popupView =
            LayoutInflater.from(this).inflate(R.layout.popup_daily, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val coinsWeek = listOf(10, 10, 10, 10, 10, 20, 20)

        val today =
            (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7

        val days = listOf(
            popupView.findViewById<Button>(R.id.day1),
            popupView.findViewById<Button>(R.id.day2),
            popupView.findViewById<Button>(R.id.day3),
            popupView.findViewById<Button>(R.id.day4),
            popupView.findViewById<Button>(R.id.day5),
            popupView.findViewById<Button>(R.id.day6),
            popupView.findViewById<Button>(R.id.day7)
        )

        days.forEachIndexed { index, button ->

            button.isEnabled = index == today

            if (index == today) {

                button.setOnClickListener {

                    GamePreferences.addCoins(coinsWeek[index])

                    Toast.makeText(
                        this,
                        "+${coinsWeek[index]} coins",
                        Toast.LENGTH_SHORT
                    ).show()

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