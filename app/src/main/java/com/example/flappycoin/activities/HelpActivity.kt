package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityHelpBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.example.flappycoin.utils.AdHelper

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding
    private val TAG = "HelpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityHelpBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // 🔹 Texte d'aide
            binding.tvHelp.text = """
                🎮 COMMENT JOUER
                1️⃣ Appuyez pour faire voler l'oiseau
                2️⃣ Évitez les tuyaux verts
                3️⃣ Collectez les pièces 🪙
                4️⃣ Gagnez des points et de l'argent!
                
                💡 CONSEILS
                • Restez entre les tuyaux
                • Les combos multiplient vos gains
                • Regardez les pubs pour relancer
                • Collectez assez pour retirer (300$)
                
                🎯 OBJECTIFS
                • Atteindre 300$ (3000 coins)
                • Débloquer tous les skins
                • Atteindre le top 10
                
                📊 CONVERSION
                10 coins = 1$
                Minimum retrait: 300$
                
                ⚙️ PARAMÈTRES
                • Son: Activez dans les paramètres
                • Vibrations: Feedback haptique
                • Langue: Changez la langue de l'app
                
                👨‍💻 CRÉDITS
                FlappyCoin v1.0.0
                Inspiré de Flappy Bird
                Développé en Kotlin
            """.trimIndent()

            binding.btnBack.setOnClickListener { finish() }

            // 🔹 Initialisation AdMob banner
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
            Toast.makeText(this, "Erreur HelpActivity: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}