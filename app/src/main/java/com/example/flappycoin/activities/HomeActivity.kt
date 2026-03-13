package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.example.flappycoin.utils.LanguageManager
import com.example.flappycoin.utils.NetworkManager
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val handler = Handler()
    private var updateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LanguageManager.init(this)
        binding.adView.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())

        binding.btnReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out1))
        binding.btnDailyReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out2))

        setupListeners()
        updateUI()
        updateTexts()

        // Charger toutes les pubs
        AdHelper.loadInterstitial(this)
        AdHelper.loadRewardedAd(this)
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
        binding.btnReward.text = L("🎁")
        binding.btnDailyReward.text = L("📆")
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

    // --- Popup cadeaux (Share / Invite / Watch) ---
    private fun showGiftPopup() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val btnShare = popupView.findViewById<Button>(R.id.btnShare)
        val btnInvite = popupView.findViewById<Button>(R.id.btnInvite)
        val btnWatch = popupView.findViewById<Button>(R.id.btnWatch)

        btnShare.isEnabled = GamePreferences.canShareApp(today)
        btnInvite.isEnabled = GamePreferences.canInviteFriend(today)
        btnWatch.isEnabled = GamePreferences.canWatchRewardedAd()
        btnShare.alpha = if (btnShare.isEnabled) 1f else 0.5f
        btnInvite.alpha = if (btnInvite.isEnabled) 1f else 0.5f
        btnWatch.alpha = if (btnWatch.isEnabled) 1f else 0.5f

        updateRunnable = object : Runnable {
            override fun run() {
                btnWatch.isEnabled = GamePreferences.canWatchRewardedAd()
                btnWatch.alpha = if (btnWatch.isEnabled) 1f else 0.5f
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable!!)

        btnShare.setOnClickListener {
            if (!btnShare.isEnabled) return@setOnClickListener
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Découvrez FlappyCoin ! https://play.google.com/store/apps/details?id=${packageName}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'application via"))

            btnShare.isEnabled = false
            btnShare.alpha = 0.5f
            GamePreferences.recordShare(today)

            handler.postDelayed({
                GamePreferences.addCoins(Constants.SHARE_REWARD)
                Toast.makeText(this, "+${Constants.SHARE_REWARD} coins (partage)!", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 10_000)

            popupWindow.dismiss()
            handler.removeCallbacks(updateRunnable!!)
        }

        btnInvite.setOnClickListener {
            if (!btnInvite.isEnabled) return@setOnClickListener
            val inviteIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Rejoignez FlappyCoin avec moi : https://play.google.com/store/apps/details?id=${packageName}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(inviteIntent, "Inviter un ami via"))

            btnInvite.isEnabled = false
            btnInvite.alpha = 0.5f
            GamePreferences.recordInvite(today)

            handler.postDelayed({
                GamePreferences.addCoins(Constants.INVITE_REWARD)
                Toast.makeText(this, "+${Constants.INVITE_REWARD} coins (invité)!", Toast.LENGTH_SHORT).show()
                updateUI()
            }, 10_000)

            popupWindow.dismiss()
            handler.removeCallbacks(updateRunnable!!)
        }

        btnWatch.setOnClickListener {
            if (!btnWatch.isEnabled) return@setOnClickListener

            AdHelper.showRewardedAd(this) { rewardItem ->
                GamePreferences.addCoins(rewardItem.amount)
                GamePreferences.setLastRewardedAdTime(System.currentTimeMillis())
                Toast.makeText(this, "+${rewardItem.amount} coins (pub)!", Toast.LENGTH_SHORT).show()
                updateUI()
            }

            popupWindow.dismiss()
            handler.removeCallbacks(updateRunnable!!)
        }

        popupWindow.setOnDismissListener { handler.removeCallbacks(updateRunnable!!) }
        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    // --- Popup calendrier quotidien ---
    private fun showDailyPopup() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        if (!GamePreferences.canClaimDailyReward(today)) {
            Toast.makeText(this, "Récompense quotidienne déjà réclamée", Toast.LENGTH_SHORT).show()
            return
        }

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
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

        days.forEachIndexed { index, button ->
            button.isEnabled = index == todayIndex
            button.alpha = if (button.isEnabled) 1f else 0.5f
            if (button.isEnabled) {
                button.setOnClickListener {
                    GamePreferences.addCoins(coinsWeek[index])
                    GamePreferences.setLastDailyReward(today)
                    Toast.makeText(this, "+${coinsWeek[index]} coins pour le jour ${index + 1} !", Toast.LENGTH_SHORT).show()
                    button.isEnabled = false
                    button.alpha = 0.5f
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
        // Programmer pub toutes les 5 minutes
        AdHelper.scheduleRewardedEvery5Minutes(this) { rewardItem ->
            GamePreferences.addCoins(rewardItem.amount)
            GamePreferences.setLastRewardedAdTime(System.currentTimeMillis())
            Toast.makeText(this, "+${rewardItem.amount} coins (pub 5min)!", Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }

    override fun onPause() {
        super.onPause()
        AdHelper.cancelScheduledRewarded()
    }
}