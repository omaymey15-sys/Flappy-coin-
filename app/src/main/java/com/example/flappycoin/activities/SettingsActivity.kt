package com.example.flappycoin.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivitySettingsBinding
import com.example.flappycoin.managers.GamePreferences
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    companion object {
        private const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 🔹 Inflate layout
            binding = ActivitySettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "✅ Layout inflé avec succès")

            // 🔹 Switch Son
            val audioEnabled = GamePreferences.isAudioEnabled()
            binding.swSound.isChecked = audioEnabled
            binding.swSound.setOnCheckedChangeListener { _, isChecked ->
                GamePreferences.setAudioEnabled(isChecked)
                Log.d(TAG, "🔊 Audio: $isChecked")
            }

            // 🔹 Switch Vibration
            val vibrationEnabled = GamePreferences.isVibrationEnabled()
            binding.swVibration.isChecked = vibrationEnabled
            binding.swVibration.setOnCheckedChangeListener { _, isChecked ->
                GamePreferences.setVibrationEnabled(isChecked)
                Log.d(TAG, "📳 Vibration: $isChecked")
            }

            // 🔹 Affichage infos utilisateur
            val username = GamePreferences.getUsername() ?: "Inconnu"
            val country = GamePreferences.getCountry() ?: "Non défini"
            val language = GamePreferences.getLanguage() ?: "Non défini"
            val currency = GamePreferences.getCurrency() ?: "USD"

            binding.tvUsername.text = "👤 Utilisateur: $username"
            binding.tvCountry.text = "🌍 Pays: $country"
            binding.tvLanguage.text = "🗣️ Langue: $language"
            binding.tvCurrency.text = "💰 Devise: $currency"

            Log.d(TAG, "✅ Infos profil chargées")

            // 🔹 Boutons Privacy / Rate / Share
            binding.btnPrivacyPolicy.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://ornannnzembe-ops.github.io/privacy-policy/") // Remplace par ton
                startActivity(intent)
                Log.d(TAG, "Privacy Policy cliqué")
            }

            binding.btnRate.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                startActivity(intent)
                Log.d(TAG, "⭐Rate App cliqué")
            }

            binding.btnShare.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Télécharge mon jeu Flappy Coin ! https://play.google.com/store/apps/details?id=$packageName"
                )
                startActivity(Intent.createChooser(intent, "Share via"))
                Log.d(TAG, "🔗Share App cliqué")
            }

            // 🔹 Bouton retour
            binding.btnBack.setOnClickListener {
                Log.d(TAG, "Retour cliqué")
                finish()
            }

            // 🔹 Initialisation AdMob
            MobileAds.initialize(this) { Log.d(TAG, "AdMob initialized") }
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.adListener = object : AdListener() {
                override fun onAdLoaded() { Log.d(TAG, "Ad loaded") }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad failed: ${adError.message}")
                }
            }

            Log.d(TAG, "✅ SettingsActivity prêt")

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERREUR dans onCreate", e)
            Toast.makeText(
                this,
                "⚠️ SettingsActivity crash\nType: ${e::class.simpleName}\nMessage: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📍 onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "📍 onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "📍 onDestroy")
    }
}