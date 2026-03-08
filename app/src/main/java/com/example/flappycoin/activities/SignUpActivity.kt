package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivitySignupBinding
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.CountryData
import com.example.flappycoin.utils.LanguageManager

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySignupBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupCountrySpinner()
            setupLanguageSpinner()
            setupCurrencySpinner()

            binding.btnRegister.setOnClickListener {
                registerUser()
            }
        } catch (e: Exception) {
            Log.e("SignUpActivity", "onCreate failed", e)
            Toast.makeText(this, "Erreur d'initialisation: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupCountrySpinner() {
        try {
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
                        try {
                            val selectedCountry = CountryData.countries[position]
                            val currencyAdapter = binding.spinnerCurrency.adapter as? ArrayAdapter<String>
                            if (currencyAdapter != null) {
                                val currencyPosition = currencyAdapter.getPosition(selectedCountry.currency)
                                binding.spinnerCurrency.setSelection(currencyPosition)
                                CurrencyManager.setExchangeRate(selectedCountry.exchangeRate)
                            }
                        } catch (e: Exception) {
                            Log.e("SignUpActivity", "onItemSelected failed", e)
                        }
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
            )
        } catch (e: Exception) {
            Log.e("SignUpActivity", "setupCountrySpinner failed", e)
            Toast.makeText(this, "Erreur spinner pays", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguageSpinner() {
        try {
            val languages = listOf(
                "Français", "English", "Español", "Deutsch", "Italiano",
                "Português", "Рус��кий", "中文", "日本語", "한국어",
                "العربية", "हिन्दी", "Türkçe", "Nederlands"
            )
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                languages
            )
            binding.spinnerLanguage.adapter = adapter
            binding.spinnerLanguage.setSelection(0)
        } catch (e: Exception) {
            Log.e("SignUpActivity", "setupLanguageSpinner failed", e)
        }
    }

    private fun setupCurrencySpinner() {
        try {
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
        } catch (e: Exception) {
            Log.e("SignUpActivity", "setupCurrencySpinner failed", e)
        }
    }

    private fun registerUser() {
        try {
            val username = binding.etUsername.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show()
                return
            }

            if (username.length < 3) {
                Toast.makeText(this, "Minimum 3 caractères requis", Toast.LENGTH_SHORT).show()
                return
            }

            // ✅ Vérifications de sécurité
            val countryPosition = binding.spinnerCountry.selectedItemPosition
            if (countryPosition < 0 || countryPosition >= CountryData.countries.size) {
                Toast.makeText(this, "Sélectionnez un pays valide", Toast.LENGTH_SHORT).show()
                return
            }

            val languagePosition = binding.spinnerLanguage.selectedItemPosition
            if (languagePosition < 0) {
                Toast.makeText(this, "Sélectionnez une langue valide", Toast.LENGTH_SHORT).show()
                return
            }

            val country = CountryData.countries[countryPosition]
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
            val language = languageCodes[languagePosition]
            val currency = binding.spinnerCurrency.selectedItem?.toString() ?: "USD"

            Log.d("SignUpActivity", "Registering: $username, Country: ${country.code}, Lang: $language, Currency: $currency")

            // ✅ Sauvegarde sécurisée
            try {
                GamePreferences.apply {
                    setUsername(username)
                    setCountry(country.code)
                    setLanguage(language)
                    setCurrency(currency)
                    setExchangeRate(country.exchangeRate)
                }
                Log.d("SignUpActivity", "✅ GamePreferences saved")
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to save GamePreferences", e)
                Toast.makeText(this, "Erreur sauvegarde préférences", Toast.LENGTH_SHORT).show()
                return
            }

            // ✅ Initialisation des managers
            try {
                LanguageManager.setLanguage(language)
                Log.d("SignUpActivity", "✅ Language set")
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to set language", e)
            }

            try {
                CurrencyManager.setCurrency(currency)
                CurrencyManager.setExchangeRate(country.exchangeRate)
                Log.d("SignUpActivity", "✅ Currency set")
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to set currency", e)
            }

            // ✅ Transition sûre vers HomeActivity
            Log.d("SignUpActivity", "Navigating to HomeActivity")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()

        } catch (e: Exception) {
            Log.e("SignUpActivity", "registerUser failed", e)
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}