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

        try {
            setupCountrySpinner()
            setupLanguageSpinner()
            setupCurrencySpinner()
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors du chargement: ${e.message}", Toast.LENGTH_LONG).show()
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun setupCountrySpinner() {
        try {
            // ✅ Utilise directement CountryData.countries
            val countries = CountryData.countries.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
            binding.spinnerCountry.adapter = adapter

            binding.spinnerCountry.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    try {
                        if (position >= 0 && position < CountryData.countries.size) {
                            val selectedCountry = CountryData.countries[position]
                            val currencyAdapter = binding.spinnerCurrency.adapter as? ArrayAdapter<String>
                            if (currencyAdapter != null) {
                                val currencyPosition = currencyAdapter.getPosition(selectedCountry.currency)
                                if (currencyPosition >= 0) {
                                    binding.spinnerCurrency.setSelection(currencyPosition)
                                }
                            }
                            CurrencyManager.setExchangeRate(selectedCountry.exchangeRate)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur Spinner Pays: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguageSpinner() {
        try {
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
            binding.spinnerLanguage.setSelection(0)
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur Spinner Langue: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCurrencySpinner() {
        try {
            val currencies = CountryData.countries.map { it.currency }.distinct()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
            binding.spinnerCurrency.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur Spinner Devise: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser() {
        try {
            val username = binding.etUsername.text.toString().trim()

            // Validation
            if (username.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show()
                return
            }

            if (username.length < 3) {
                Toast.makeText(this, "Le nom doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show()
                return
            }

            // ✅ Vérification sécurisée
            val countryPosition = binding.spinnerCountry.selectedItemPosition
            if (countryPosition < 0 || countryPosition >= CountryData.countries.size) {
                Toast.makeText(this, "Veuillez sélectionner un pays valide", Toast.LENGTH_SHORT).show()
                return
            }

            val country = CountryData.countries[countryPosition]

            val languagePosition = binding.spinnerLanguage.selectedItemPosition
            val languages = listOf(
                LanguageCodes.FRENCH, LanguageCodes.ENGLISH, LanguageCodes.SPANISH, LanguageCodes.GERMAN,
                LanguageCodes.ITALIAN, LanguageCodes.PORTUGUESE, LanguageCodes.RUSSIAN, LanguageCodes.CHINESE,
                LanguageCodes.JAPANESE, LanguageCodes.KOREAN, LanguageCodes.ARABIC, LanguageCodes.HINDI,
                LanguageCodes.TURKISH, LanguageCodes.DUTCH
            )

            if (languagePosition < 0 || languagePosition >= languages.size) {
                Toast.makeText(this, "Veuillez sélectionner une langue valide", Toast.LENGTH_SHORT).show()
                return
            }

            val language = languages[languagePosition]
            val currency = binding.spinnerCurrency.selectedItem.toString()

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

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors de l'inscription: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
