package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivitySignupBinding
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.CountryData
import com.example.flappycoin.utils.LanguageManager

/**
 * SignUpActivity - Inscription utilisateur
 * Collecte: pseudo, pays, langue, devise
 */
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
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            countries
        )
        binding.spinnerCountry.adapter = adapter

        binding.spinnerCountry.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedCountry = CountryData.countries[position]
                    val currencyAdapter = binding.spinnerCurrency.adapter as ArrayAdapter<String>
                    val currencyPosition = currencyAdapter.getPosition(selectedCountry.currency)
                    binding.spinnerCurrency.setSelection(currencyPosition)
                    CurrencyManager.setExchangeRate(selectedCountry.exchangeRate)
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        )
    }

    private fun setupLanguageSpinner() {
        val languages = listOf(
            "Français", "English", "Español", "Deutsch", "Italiano",
            "Português", "Русский", "中文", "日本語", "한국어",
            "العربية", "हिन्दी", "Türkçe", "Nederlands"
        )
        val codes = listOf(
            Constants.LanguageCodes.FRENCH,
            Constants.LanguageCodes.ENGLISH,
            Constants.LanguageCodes.SPANISH,
            Constants.LanguageCodes.GERMAN,
            Constants.LanguageCodes.ITALIAN,
            Constants.LanguageCodes.PORTUGUESE,
            Constants.LanguageCodes.RUSSIAN,
            Constants.LanguageCodes.CHINESE,
            Constants.LanguageCodes.JAPANESE,
            Constants.LanguageCodes.KOREAN,
            Constants.LanguageCodes.ARABIC,
            Constants.LanguageCodes.HINDI,
            Constants.LanguageCodes.TURKISH,
            Constants.LanguageCodes.DUTCH
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        )
        binding.spinnerLanguage.adapter = adapter
        binding.spinnerLanguage.setSelection(0)  // Français par défaut
    }

    private fun setupCurrencySpinner() {
        val currencies = CountryData.countries
            .map { it.currency }
            .distinct()
            .sorted()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            currencies
        )
        binding.spinnerCurrency.adapter = adapter
    }

    private fun registerUser() {
        val username = binding.etUsername.text.toString().trim()

        // Validation
        if (username.isEmpty()) {
            Toast.makeText(
                this,
                "Veuillez entrer un nom d'utilisateur",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (username.length < 3) {
            Toast.makeText(
                this,
                "Minimum 3 caractères requis",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Récupérer sélections
        val country = CountryData.countries[binding.spinnerCountry.selectedItemPosition]
        val languageCodes = listOf(
            Constants.LanguageCodes.FRENCH,
            Constants.LanguageCodes.ENGLISH,
            Constants.LanguageCodes.SPANISH,
            Constants.LanguageCodes.GERMAN,
            Constants.LanguageCodes.ITALIAN,
            Constants.LanguageCodes.PORTUGUESE,
            Constants.LanguageCodes.RUSSIAN,
            Constants.LanguageCodes.CHINESE,
            Constants.LanguageCodes.JAPANESE,
            Constants.LanguageCodes.KOREAN,
            Constants.LanguageCodes.ARABIC,
            Constants.LanguageCodes.HINDI,
            Constants.LanguageCodes.TURKISH,
            Constants.LanguageCodes.DUTCH
        )
        val language = languageCodes[binding.spinnerLanguage.selectedItemPosition]
        val currency = binding.spinnerCurrency.selectedItem.toString()

        // Sauvegarder
        GamePreferences.apply {
            setUsername(username)
            setCountry(country.code)
            setLanguage(language)
            setCurrency(currency)
            setExchangeRate(country.exchangeRate)
        }

        // Appliquer langue
        LanguageManager.setLanguage(language)
        CurrencyManager.setCurrency(currency)
        CurrencyManager.setExchangeRate(country.exchangeRate)

        // Redirection
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}