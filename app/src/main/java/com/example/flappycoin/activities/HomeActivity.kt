package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.R
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.utils.NetworkManager

/**
 * MainActivity - Écran Splash
 * Vérification réseau + redirection
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vérifier connexion réseau
        if (!NetworkManager.isInternetAvailable(this)) {
            Toast.makeText(
                this,
                "Connexion internet requise pour jouer!",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        // Splash écran 2 secondes
        Handler(Looper.getMainLooper()).postDelayed({
            // Vérifier si utilisateur enregistré
            val username = GamePreferences.getUsername()
            val intent = if (username.isNullOrEmpty()) {
                Intent(this, SignUpActivity::class.java)
            } else {
                Intent(this, HomeActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 2000)
    }
}