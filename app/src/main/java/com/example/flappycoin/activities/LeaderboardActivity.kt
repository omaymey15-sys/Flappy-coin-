package com.example.flappycoin.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityLeaderboardBinding
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.GamePreferences

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Initialisation AdManager avec applicationContext
        AdManager.init(applicationContext)

        // 🔹 Banner
        AdManager.loadBanner(binding.adView)

        // 🔹 Interstitial
        AdManager.showInterstitial(this)

        // ================= STATS =================
        val bestScore = GamePreferences.getBestScore()
        val bestCoins = GamePreferences.getBestCoins()
        val username = GamePreferences.getUsername() ?: "Guest"

        binding.tvLeaderboard.text = """
            🏆 TOP SCORES (Local)
            
            👤 $username
            Score: $bestScore
            Pièces: $bestCoins
            
            (Sauvegarde locale)
            
            💡 Conseil: Partagez votre score!
        """.trimIndent()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}