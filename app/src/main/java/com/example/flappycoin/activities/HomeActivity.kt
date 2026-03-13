package com.example.flappycoin.activities

import android.content.Intent
import android.net.Uri
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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var rewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Initialisation AdMob
        MobileAds.initialize(this) {}
        loadRewardedAd()

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // 🔹 Animation des boutons 🎁📆
        binding.btnReward.text = "🎁"
        binding.btnDailyReward.text = "📆"
        binding.btnReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out1))
        binding.btnDailyReward.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_out2))

        setupListeners()
        updateUI()
    }

    private fun loadRewardedAd() {
        RewardedAd.load(
            this,
            Constants.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d("HomeActivity", "Rewarded Ad loaded")
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    Log.e("HomeActivity", "Rewarded Ad failed: ${error.message}")
                    rewardedAd = null
                }
            }
        )
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

    // --- 🎁 Popup cadeau ---
    private fun showGiftPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        // Partage réel (max 2x/jour)
        popupView.findViewById<Button>(R.id.btnShare).setOnClickListener {
            val today = Calendar.getInstance().timeInMillis
            val lastShare = GamePreferences.getLastShareTime()
            val shareCount = GamePreferences.getShareCount()
            if (!isSameDay(today, lastShare)) GamePreferences.resetShareCount()

            if (shareCount >= 2) {
                Toast.makeText(this, "Vous avez déjà partagé 2 fois aujourd'hui", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Viens jouer à FlappyCoin ! Lien: https://fake.link/share")
            startActivity(Intent.createChooser(shareIntent, "Partager via"))

            GamePreferences.incrementShareCount()
            GamePreferences.setLastShareTime(today)
            GamePreferences.addCoins(Constants.SHARE_REWARD)
            Toast.makeText(this, "+${Constants.SHARE_REWARD} coins !", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
            updateUI()
        }

        // Invitation réel (max 1x/jour)
        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener {
            val today = Calendar.getInstance().timeInMillis
            val lastInvite = GamePreferences.getLastInviteTime()
            if (isSameDay(today, lastInvite)) {
                Toast.makeText(this, "Vous avez déjà invité un ami aujourd'hui", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inviteIntent = Intent(Intent.ACTION_SEND)
            inviteIntent.type = "text/plain"
            inviteIntent.putExtra(Intent.EXTRA_TEXT, "Rejoins-moi sur FlappyCoin ! Lien: https://fake.link/invite")
            startActivity(Intent.createChooser(inviteIntent, "Inviter un ami via"))

            GamePreferences.setLastInviteTime(today)
            GamePreferences.addCoins(10) // 10 coins pour inviter
            Toast.makeText(this, "+10 coins !", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
            updateUI()
        }

        // Pub récompensée réelle
        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener {
            val now = System.currentTimeMillis()
            val lastAd = GamePreferences.getLastRewardedAdTime()
            if (now - lastAd < Constants.REWARDED_AD_INTERVAL_MS) {
                Toast.makeText(this, "Attendez ${ (Constants.REWARDED_AD_INTERVAL_MS - (now-lastAd))/1000 } secondes avant de regarder une autre pub", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            rewardedAd?.let { ad ->
                ad.show(this, object : RewardedAdCallback() {
                    override fun onUserEarnedReward(reward: RewardItem) {
                        GamePreferences.addCoins(reward.amount)
                        Toast.makeText(this@HomeActivity, "+${reward.amount} coins !", Toast.LENGTH_SHORT).show()
                        updateUI()
                    }

                    override fun onAdClosed() {
                        loadRewardedAd()
                    }
                })
                GamePreferences.setLastRewardedAdTime(now)
            } ?: run {
                Toast.makeText(this, "Pub non chargée", Toast.LENGTH_SHORT).show()
            }
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    // --- 📆 Popup calendrier quotidien ---
    private fun showDailyPopup() {
        val today = Calendar.getInstance().timeInMillis
        val lastDaily = GamePreferences.getLastDailyReward()
        if (isSameDay(today, lastDaily)) {
            Toast.makeText(this, "Vous avez déjà récupéré la récompense d'aujourd'hui", Toast.LENGTH_SHORT).show()
            return
        }

        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_daily, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        // Récompense quotidienne
        val coinsToday = if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) in listOf(Calendar.SATURDAY, Calendar.SUNDAY)) Constants.DAILY_REWARD_WEEKEND else Constants.DAILY_REWARD_WEEKDAY
        GamePreferences.addCoins(coinsToday)
        GamePreferences.setLastDailyReward(today)
        Toast.makeText(this, "+$coinsToday coins pour la récompense quotidienne !", Toast.LENGTH_SHORT).show()
        updateUI()
        popupWindow.dismiss()
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}