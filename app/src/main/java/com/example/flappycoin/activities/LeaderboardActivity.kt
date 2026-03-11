package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityLeaderboardBinding
import com.example.flappycoin.managers.GamePreferences
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 🔹 Inflate layout
            binding = ActivityLeaderboardBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // 🔹 Récupérer données locales
            val bestScore = GamePreferences.getBestScore()
            val bestCoins = GamePreferences.getBestCoins()
            val username = GamePreferences.getUsername()

            // 🔹 Afficher leaderboard
            binding.tvLeaderboard.text = """
                🏆 TOP SCORES (LOCAL)
                
                👤 $username
                Score: $bestScore
                Pièces: $bestCoins
                
                (Sauvegarde locale)
                
                💡 Conseil: Partagez votre score!
            """.trimIndent()

            // 🔹 Bouton retour
            binding.btnBack.setOnClickListener { finish() }

            // 🔹 Initialisation AdMob
            MobileAds.initialize(this) { Log.d("LeaderboardActivity", "AdMob initialized") }

            // 🔹 Charger la bannière
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)

            // 🔹 Listener pour debug
            binding.adView.adListener = object : AdListener() {
                override fun onAdLoaded() { Log.d("LeaderboardActivity", "Ad loaded") }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("LeaderboardActivity", "Ad failed: ${adError.message}")
                }
            }

        } catch (e: Exception) {
            Log.e("LeaderboardActivity", "Exception dans onCreate", e)
            Toast.makeText(
                this,
                "⚠️ LeaderboardActivity crash\nType: ${e::class.simpleName}\nMessage: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}