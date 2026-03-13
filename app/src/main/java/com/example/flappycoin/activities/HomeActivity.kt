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
import com.example.flappycoin.utils.LanguageManager
import com.example.flappycoin.utils.NetworkManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var rewardedAd: RewardedAd? = null

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

        // 🔹 Animations des boutons cadeaux
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

    // --- Vérification retrait ---
    private fun checkWithdrawal() {
        val totalCoins = GamePreferences.getTotalCoins()
        if (totalCoins < Constants.MINIMUM_WITHDRAWAL_COINS.toInt()) {
            val needed = Constants.MINIMUM_WITHDRAWAL_COINS.toInt() - totalCoins
            Toast.makeText(this, "Minimum: ${Constants.MINIMUM_WITHDRAWAL_DOLLARS.toInt()}$\nManque: $needed coins", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Retrait simulé", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Popup cadeau (Partager / Inviter / Regarder pub) ---
    private fun showGiftPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_gift, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        // 🔹 Partager (2 fois/jour)
        popupView.findViewById<Button>(R.id.btnShare).setOnClickListener { handleShare() }

        // 🔹 Inviter un ami (1 fois/jour)
        popupView.findViewById<Button>(R.id.btnInvite).setOnClickListener { handleInvite() }

        // 🔹 Regarder pub récompensée réelle
        popupView.findViewById<Button>(R.id.btnWatch).setOnClickListener { showRewardedAd() }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    // --- Popup calendrier quotidien ---
    private fun showDailyPopup() {
        val prefs = getSharedPreferences("FlappyCoin", MODE_PRIVATE)
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_daily, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastClaim = prefs.getString(Constants.PrefsKeys.LAST_DAILY_REWARD, "") ?: ""
        if (today == lastClaim) {
            Toast.makeText(this, "Vous avez déjà récupéré la récompense du jour !", Toast.LENGTH_SHORT).show()
            return
        }

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
                    Toast.makeText(this, "+${coinsWeek[index]} coins !", Toast.LENGTH_SHORT).show()
                    button.isEnabled = false
                    popupWindow.dismiss()
                    prefs.edit().putString(Constants.PrefsKeys.LAST_DAILY_REWARD, today).apply()
                    updateUI()
                }
            }
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    // --- Gérer partage ---
    private fun handleShare() {
        val prefs = getSharedPreferences("FlappyCoin", MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        var shareCount = prefs.getInt(Constants.PrefsKeys.SHARE_COUNT + today, 0)

        if (shareCount >= 2) {
            Toast.makeText(this, "Vous avez déjà partagé 2 fois aujourd'hui !", Toast.LENGTH_SHORT).show()
            return
        }

        val shareMessage = "Regardez ce jeu incroyable FlappyCoin ! https://faux.lien/partage"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, "Partager avec"))

        binding.root.postDelayed({
            GamePreferences.addCoins(Constants.SHARE_REWARD)
            Toast.makeText(this, "+${Constants.SHARE_REWARD} coins pour le partage !", Toast.LENGTH_SHORT).show()
            shareCount++
            prefs.edit().putInt(Constants.PrefsKeys.SHARE_COUNT + today, shareCount).apply()
            updateUI()
        }, 5000)
    }

    // --- Gérer invitation ---
    private fun handleInvite() {
        val prefs = getSharedPreferences("FlappyCoin", MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val inviteCount = prefs.getInt(Constants.PrefsKeys.INVITE_COUNT + today, 0)

        if (inviteCount >= 1) {
            Toast.makeText(this, "Vous avez déjà invité un ami aujourd'hui !", Toast.LENGTH_SHORT).show()
            return
        }

        val inviteMessage = "Rejoignez-moi sur FlappyCoin ! https://faux.lien/invite"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, inviteMessage)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, "Inviter un ami"))

        binding.root.postDelayed({
            GamePreferences.addCoins(Constants.INVITE_REWARD)
            Toast.makeText(this, "+${Constants.INVITE_REWARD} coins pour l'invitation !", Toast.LENGTH_SHORT).show()
            prefs.edit().putInt(Constants.PrefsKeys.INVITE_COUNT + today, inviteCount + 1).apply()
            updateUI()
        }, 5000)
    }

    // --- Pub récompensée réelle ---
    private fun showRewardedAd() {
        val prefs = getSharedPreferences("FlappyCoin", MODE_PRIVATE)
        val lastTime = prefs.getLong(Constants.PrefsKeys.LAST_REWARDED_AD_TIME, 0)
        val now = System.currentTimeMillis()

        if (now - lastTime < Constants.REWARDED_AD_INTERVAL_MS) {
            val remaining = (Constants.REWARDED_AD_INTERVAL_MS - (now - lastTime)) / 1000
            Toast.makeText(this, "Veuillez attendre $remaining secondes avant la prochaine pub.", Toast.LENGTH_SHORT).show()
            return
        }

        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        rewardedAd = RewardedAd(this, Constants.REWARDED_AD_UNIT_ID)
        rewardedAd?.loadAd(adRequest, object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
            override fun onAdLoaded() {
                rewardedAd?.show(this@HomeActivity) { rewardItem ->
                    GamePreferences.addCoins(Constants.REWARDED_AD_BONUS)
                    Toast.makeText(this@HomeActivity, "+${Constants.REWARDED_AD_BONUS} coins !", Toast.LENGTH_SHORT).show()
                    prefs.edit().putLong(Constants.PrefsKeys.LAST_REWARDED_AD_TIME, now).apply()
                    updateUI()
                }
            }

            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                Toast.makeText(this@HomeActivity, "Échec du chargement de la pub", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}