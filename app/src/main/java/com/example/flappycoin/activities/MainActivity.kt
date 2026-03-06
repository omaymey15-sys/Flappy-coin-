package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivityMainBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.utils.NetworkManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Vérifier connexion réseau
        if (!NetworkManager.isInternetAvailable(this)) {
            Toast.makeText(this, "Connexion internet requise!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Vérifier si utilisateur enregistré
        val username = GamePreferences.getUsername()
        val intent = if (username.isNullOrEmpty()) {
            Intent(this, SignUpActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}