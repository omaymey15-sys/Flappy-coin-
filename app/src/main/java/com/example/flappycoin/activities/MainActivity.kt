package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.utils.NetworkManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "🎬 MainActivity onCreate")

            // Vérifier connexion Internet
            if (!NetworkManager.isInternetAvailable(this)) {
                Log.e(TAG, "❌ Pas de connexion internet")
                Toast.makeText(
                    this,
                    "Connexion internet requise pour jouer!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }

            Log.d(TAG, "✅ Connexion internet OK")

            // 🔹 Initialisation AdManager
            AdManager.init(applicationContext)

            // Charger App Open Ad
            AdManager.loadAppOpenAd(applicationContext)

            // Afficher App Open Ad si chargée
            if (AdManager.isAppOpenAdLoaded()) {
                AdManager.showAppOpenAd(this) {
                    // Callback après fermeture de l'Ad → redirection
                    redirectUser()
                }
            } else {
                // Si l'ad n'est pas encore prête, rediriger directement
                redirectUser()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERREUR MainActivity onCreate", e)
            e.printStackTrace()
            finish()
        }
    }

    private fun redirectUser() {
        try {
            val username = GamePreferences.getUsername()
            Log.d(TAG, "Username: $username")

            val intent = if (username.isNullOrEmpty()) {
                Log.d(TAG, "→ Redirection SignUpActivity")
                Intent(this, SignUpActivity::class.java)
            } else {
                Log.d(TAG, "→ Redirection HomeActivity")
                Intent(this, HomeActivity::class.java)
            }

            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur redirection", e)
            finish()
        }
    }
} 