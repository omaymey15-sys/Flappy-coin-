package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
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
import com.example.flappycoin.utils.AdHelper
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.NetworkManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.time.LocalDate
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAds()
        initAnimations()
        setupListeners()
        updateUI()
    }

    // ================= ADS =================

    private fun initAds() {

        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        AdHelper.loadRewardedAd(this)
    }

    // ================= ANIMATIONS =================

    private fun initAnimations() {

        val anim1 = AnimationUtils.loadAnimation(this, R.anim.zoom_in_out1)
        val anim2 = AnimationUtils.loadAnimation(this, R.anim.zoom_in_out2)

        binding.btnReward.startAnimation(anim1)
        binding.btnDailyReward.startAnimation(anim2)
    }

    // ================= LISTENERS =================

    private fun setupListeners() {

        binding.btnPlay.setOnClickListener {

            if (!NetworkManager.isInternetAvailable(this)) {

                Toast.makeText(this, "Connexion internet requise", Toast.LENGTH_SHORT).show()
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

    // ================= UI =================

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

    // ================= WITHDRAW =================

    private fun checkWithdrawal() {

        val totalCoins = GamePreferences.getTotalCoins()

        if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS.toInt()) {

            val needed = Constants.MINIMUM_WITHDRAWAL_COINS.toInt() - totalCoins

            Toast.makeText(
                this,
                "Minimum: ${Constants.MINIMUM_WITHDRAWAL_DOLLARS}$\nManque: $needed coins",
                Toast.LENGTH_LONG
            ).show()

        } else {

            Toast.makeText(this, "Retrait simulé", Toast.LENGTH_SHORT).show()
        }
    }

    // ================= POPUP CADEAU =================

    private fun showGiftPopup() {

        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 10f

        val today = LocalDate.now().toString()

        // partager
        popupView.findViewById<Button>(R.id.btnShare).setOnClickListener {

            if (GamePreferences.canShareApp(today)) {

                val intent = Intent(Intent.ACTION_SEND)

                intent.type = "text/plain"

                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Télécharge FlappyCoin et gagne des coins !"
                )

                startActivity(Intent.createChooser(intent, "Partager"))

                GamePreferences.recordShare(today)

                GamePreferences.addCoins(Constants.SHARE_REWARD)

                Toast.makeText(this, "+10 coins", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(this, "Limite partage atteinte", Toast.LENGTH_SHORT).show()
            }

            popupWindow.dismiss()

            updateUI()
        }

        // inviter
        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener {

            if (GamePreferences.canInviteFriend(today)) {

                val intent = Intent(Intent.ACTION_SEND)

                intent.type = "text/plain"

                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Viens jouer à FlappyCoin avec moi !"
                )

                startActivity(Intent.createChooser(intent, "Inviter"))

                GamePreferences.recordInvite(today)

                GamePreferences.addCoins(Constants.INVITE_REWARD)

                Toast.makeText(this, "+20 coins", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(this, "Invitation déjà utilisée aujourd'hui", Toast.LENGTH_SHORT).show()
            }

            popupWindow.dismiss()

            updateUI()
        }

        // pub
        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener {

            if (GamePreferences.canWatchRewardedAd()) {

                AdHelper.showRewardedAd(this) {

                    GamePreferences.setLastRewardedAdTime(System.currentTimeMillis())

                    GamePreferences.addCoins(Constants.REWARDED_AD_BONUS)

                    Toast.makeText(this, "+15 coins", Toast.LENGTH_SHORT).show()

                    updateUI()
                }

            } else {

                Toast.makeText(this, "Attends 5 minutes", Toast.LENGTH_SHORT).show()
            }

            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    // ================= CALENDRIER =================

    private fun showDailyPopup() {

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

        val coinsWeek = listOf(10,10,10,10,10,20,20)

        val todayIndex =
            (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

        val today = LocalDate.now().toString()

        days.forEachIndexed { index, button ->

            button.isEnabled = index == todayIndex

            if (index == todayIndex) {

                button.setOnClickListener {

                    if (GamePreferences.canClaimDailyReward(today)) {

                        GamePreferences.addCoins(coinsWeek[index])

                        GamePreferences.setLastDailyReward(today)

                        Toast.makeText(
                            this,
                            "+${coinsWeek[index]} coins",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            this,
                            "Récompense déjà récupérée aujourd'hui",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

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