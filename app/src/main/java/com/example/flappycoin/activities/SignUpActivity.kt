package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivitySignupBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.utils.CountryData
import com.example.flappycoin.utils.LanguageManager
import com.example.flappycoin.utils.LanguageCodes

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCountrySpinner()
        setupLanguageSpinner()
        setupCurrencySpinner()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun setupCountrySpinner() {
        val countries = CountryData.countries.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
        binding.spinnerCountry.adapter = adapter

        binding.spinnerCountry.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCountry = CountryData.countries[position]
                binding.spinnerCurrency.setSelection(
                    (binding.spinnerCurrency.adapter as ArrayAdapter<String>).getPosition(selectedCountry.currency)
                )
                val exchangeRate = selectedCountry.exchangeRate
                CurrencyManager.setExchangeRate(exchangeRate)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupLanguageSpinner() {
        val languages = listOf(
            "Français", "English", "Español", "Deutsch", "Italiano",
            "Português", "Русский", "中文", "日本語", "한국어",
            "العربية", "हिन्दी", "Türkçe", "Nederlands"
        )
        val codes = listOf(
            LanguageCodes.FRENCH, LanguageCodes.ENGLISH, LanguageCodes.SPANISH, LanguageCodes.GERMAN,
            LanguageCodes.ITALIAN, LanguageCodes.PORTUGUESE, LanguageCodes.RUSSIAN, LanguageCodes.CHINESE,
            LanguageCodes.JAPANESE, LanguageCodes.KOREAN, LanguageCodes.ARABIC, LanguageCodes.HINDI,
            LanguageCodes.TURKISH, LanguageCodes.DUTCH
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        binding.spinnerLanguage.adapter = adapter
        binding.spinnerLanguage.setSelection(0) // Français par défaut
    }

    private fun setupCurrencySpinner() {
        val currencies = CountryData.countries.map { it.currency }.distinct()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun registerUser() {
        val username = binding.etUsername.text.toString().trim()
        val country = CountryData.countries[binding.spinnerCountry.selectedItemPosition]
        val language = listOf(
            LanguageCodes.FRENCH, LanguageCodes.ENGLISH, LanguageCodes.SPANISH, LanguageCodes.GERMAN,
            LanguageCodes.ITALIAN, LanguageCodes.PORTUGUESE, LanguageCodes.RUSSIAN, LanguageCodes.CHINESE,
            LanguageCodes.JAPANESE, LanguageCodes.KOREAN, LanguageCodes.ARABIC, LanguageCodes.HINDI,
            LanguageCodes.TURKISH, LanguageCodes.DUTCH
        )[binding.spinnerLanguage.selectedItemPosition]
        val currency = binding.spinnerCurrency.selectedItem.toString()

        // Validation
        if (username.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.length < 3) {
            Toast.makeText(this, "Le nom doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show()
            return
        }

        // Sauvegarde
        GamePreferences.apply {
            setUsername(username)
            setCountry(country.code)
            setLanguage(language)
            setCurrency(currency)
            setExchangeRate(country.exchangeRate)
        }

        // Appliquer la langue
        LanguageManager.setLanguage(language)
        CurrencyManager.setCurrency(currency)
        CurrencyManager.setExchangeRate(country.exchangeRate)

        // Redirection
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}