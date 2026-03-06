package com.example.flappycoin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivitySettingsBinding
import com.example.flappycoin.managers.GamePreferences

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swSound.isChecked = GamePreferences.isAudioEnabled()
        binding.swSound.setOnCheckedChangeListener { _, isChecked ->
            GamePreferences.setAudioEnabled(isChecked)
        }

        binding.swVibration.isChecked = GamePreferences.isVibrationEnabled()
        binding.swVibration.setOnCheckedChangeListener { _, isChecked ->
            GamePreferences.setVibrationEnabled(isChecked)
        }

        binding.tvUsername.text = "Utilisateur: ${GamePreferences.getUsername()}"
        binding.tvCountry.text = "Pays: ${GamePreferences.getCountry()}"
        binding.tvLanguage.text = "Langue: ${GamePreferences.getLanguage()}"
        binding.tvCurrency.text = "Devise: ${GamePreferences.getCurrency()}"

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}