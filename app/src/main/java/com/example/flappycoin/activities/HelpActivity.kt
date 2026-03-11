package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityHelpBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 🔹 Inflate layout
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

            // 🔹 Bouton retour
            binding.btnBack.setOnClickListener { finish() }

            // 🔹 Initialisation AdMob
            MobileAds.initialize(this) { Log.d("HelpActivity", "AdMob initialized") }

            // 🔹 Charger la bannière
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)

        } catch (e: Exception) {
            // 🔹 Log complet
            Log.e("HelpActivity", "Exception dans onCreate", e)

            // 🔹 Toast pour voir l'erreur sur l'appareil
            Toast.makeText(
                this,
                "⚠️ HelpActivity crash\nType: ${e::class.simpleName}\nMessage: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}