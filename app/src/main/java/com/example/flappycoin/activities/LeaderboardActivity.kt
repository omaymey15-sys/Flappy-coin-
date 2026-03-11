package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityLeaderboardBinding
import com.example.flappycoin.managers.GamePreferences
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.example.flappycoin.utils.AdHelper

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private val TAG = "LeaderboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityLeaderboardBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val bestScore = GamePreferences.getBestScore()
            val bestCoins = GamePreferences.getBestCoins()
            val username = GamePreferences.getUsername() ?: "Guest"

            binding.tvLeaderboard.text = """
                🏆 TOP SCORES (LOCAL)
                
                👤 $username
                Score: $bestScore
                Pièces: $bestCoins
                
                (Sauvegarde locale)
                
                💡 Conseil: Partagez votre score!
            """.trimIndent()

            binding.btnBack.setOnClickListener { finish() }

            // 🔹 Banner
            MobileAds.initialize(this) { Log.d(TAG, "AdMob initialized") }
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.adListener = object : AdListener() {
                override fun onAdLoaded() { Log.d(TAG, "Banner loaded") }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Banner failed: ${adError.message}")
                }
            }

            // 🔹 Interstitial
            AdHelper.loadInterstitial(this)
            binding.root.postDelayed({ AdHelper.showInterstitial(this) }, 500)

        } catch (e: Exception) {
            Log.e(TAG, "Error onCreate", e)
            Toast.makeText(this, "Erreur Leaderboard: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}