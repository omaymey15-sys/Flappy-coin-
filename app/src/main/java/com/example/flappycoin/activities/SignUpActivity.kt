package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivitySignupBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.utils.CountryData
import com.example.flappycoin.utils.LanguageCodes

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySignupBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Log.d(TAG, "onCreate: Loading spinners...")
            setupCountrySpinner()
            setupLanguageSpinner()
            setupCurrencySpinner()

            binding.btnRegister.setOnClickListener {
                registerUser()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}", e)
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCountrySpinner() {
        try {
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
                                    binding.spinnerCurrency.setSelection(currencyPosition, false)
                                }
                            }
                            CurrencyManager.setExchangeRate(selectedCountry.exchangeRate)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Country selection error: ${e.message}")
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "setupCountrySpinner error: ${e.message}")
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
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
            binding.spinnerLanguage.adapter = adapter
            binding.spinnerLanguage.setSelection(0, false)
        } catch (e: Exception) {
            Log.e(TAG, "setupLanguageSpinner error: ${e.message}")
            Toast.makeText(this, "Erreur Spinner Langue: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCurrencySpinner() {
        try {
            val currencies = CountryData.countries.map { it.currency }.distinct()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
            binding.spinnerCurrency.adapter = adapter
        } catch (e: Exception) {
            Log.e(TAG, "setupCurrencySpinner error: ${e.message}")
            Toast.makeText(this, "Erreur Spinner Devise: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser() {
        try {
            Log.d(TAG, "Starting registration...")

            val username = binding.etUsername.text?.toString()?.trim() ?: ""

            if (username.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show()
                return
            }

            if (username.length < 3) {
                Toast.makeText(this, "Le nom doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show()
                return
            }

            val countryPosition = binding.spinnerCountry.selectedItemPosition
            if (countryPosition < 0 || countryPosition >= CountryData.countries.size) {
                Toast.makeText(this, "Sélectionnez un pays", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Sélectionnez une langue", Toast.LENGTH_SHORT).show()
                return
            }

            val language = languages[languagePosition]

            val currencyItem = binding.spinnerCurrency.selectedItem
            if (currencyItem == null) {
                Toast.makeText(this, "Sélectionnez une devise", Toast.LENGTH_SHORT).show()
                return
            }

            val currency = currencyItem.toString()

            // ✅ Sauvegarde SIMPLE sans appels dangereux
            GamePreferences.apply {
                setUsername(username)
                setCountry(country.code)
                setLanguage(language)
                setCurrency(currency)
                setExchangeRate(country.exchangeRate)
            }

            CurrencyManager.setExchangeRate(country.exchangeRate)

            Log.d(TAG, "Registration successful!")
            
            // Redirection
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}