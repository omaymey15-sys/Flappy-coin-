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
            Log.d(TAG, "=== onCreate START ===")
            
            binding = ActivitySignupBinding.inflate(layoutInflater)
            Log.d(TAG, "✅ Binding inflated")
            setContentView(binding.root)
            Log.d(TAG, "✅ Content view set")

            setupCountrySpinner()
            Log.d(TAG, "✅ Country spinner setup")
            
            setupLanguageSpinner()
            Log.d(TAG, "✅ Language spinner setup")
            
            setupCurrencySpinner()
            Log.d(TAG, "✅ Currency spinner setup")

            binding.btnRegister.setOnClickListener {
                Log.d(TAG, "Register button clicked")
                registerUser()
            }
            
            Log.d(TAG, "=== onCreate END ===")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 onCreate EXCEPTION", e)
            val errorMsg = "${e.javaClass.simpleName}: ${e.message}\n${e.stackTrace.take(3).joinToString("\n")}"
            Toast.makeText(this, "ERREUR:\n$errorMsg", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCountrySpinner() {
        try {
            Log.d(TAG, "Setting up country spinner...")
            
            val countries = CountryData.countries.map { it.name }
            Log.d(TAG, "Countries loaded: ${countries.size} items")
            
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
            binding.spinnerCountry.adapter = adapter
            Log.d(TAG, "Country adapter set")

            binding.spinnerCountry.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    try {
                        Log.d(TAG, "Country selected: position=$position")
                        
                        if (position >= 0 && position < CountryData.countries.size) {
                            val selectedCountry = CountryData.countries[position]
                            Log.d(TAG, "Selected country: ${selectedCountry.code}")
                            
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
                        Log.e(TAG, "💥 onItemSelected error", e)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
            
            Log.d(TAG, "Country spinner setup complete")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 setupCountrySpinner error", e)
            Toast.makeText(this, "Erreur Spinner Pays: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguageSpinner() {
        try {
            Log.d(TAG, "Setting up language spinner...")
            
            val languages = listOf(
                "Français", "English", "Español", "Deutsch", "Italiano",
                "Português", "Русский", "中文", "日本語", "한국어",
                "العربية", "हिन्दी", "Türkçe", "Nederlands"
            )
            Log.d(TAG, "Languages list created: ${languages.size} items")
            
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
            binding.spinnerLanguage.adapter = adapter
            binding.spinnerLanguage.setSelection(0, false)
            
            Log.d(TAG, "Language spinner setup complete")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 setupLanguageSpinner error", e)
            Toast.makeText(this, "Erreur Spinner Langue: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCurrencySpinner() {
        try {
            Log.d(TAG, "Setting up currency spinner...")
            
            val currencies = CountryData.countries.map { it.currency }.distinct()
            Log.d(TAG, "Currencies loaded: ${currencies.size} items")
            
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
            binding.spinnerCurrency.adapter = adapter
            
            Log.d(TAG, "Currency spinner setup complete")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 setupCurrencySpinner error", e)
            Toast.makeText(this, "Erreur Spinner Devise: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser() {
        try {
            Log.d(TAG, "=== registerUser START ===")
            
            val username = binding.etUsername.text?.toString()?.trim() ?: ""
            Log.d(TAG, "Username: '$username'")

            if (username.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show()
                return
            }

            if (username.length < 3) {
                Toast.makeText(this, "Le nom doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show()
                return
            }

            val countryPosition = binding.spinnerCountry.selectedItemPosition
            Log.d(TAG, "Country position: $countryPosition")
            
            if (countryPosition < 0 || countryPosition >= CountryData.countries.size) {
                Toast.makeText(this, "Sélectionnez un pays", Toast.LENGTH_SHORT).show()
                return
            }

            val country = CountryData.countries[countryPosition]
            Log.d(TAG, "Country: ${country.code}")

            val languagePosition = binding.spinnerLanguage.selectedItemPosition
            Log.d(TAG, "Language position: $languagePosition")
            
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
            Log.d(TAG, "Language: $language")

            val currencyItem = binding.spinnerCurrency.selectedItem
            if (currencyItem == null) {
                Log.e(TAG, "💥 Currency item is NULL!")
                Toast.makeText(this, "Sélectionnez une devise", Toast.LENGTH_SHORT).show()
                return
            }

            val currency = currencyItem.toString()
            Log.d(TAG, "Currency: $currency")

            Log.d(TAG, "Saving to GamePreferences...")
            GamePreferences.apply {
                setUsername(username)
                setCountry(country.code)
                setLanguage(language)
                setCurrency(currency)
                setExchangeRate(country.exchangeRate)
            }
            Log.d(TAG, "✅ GamePreferences saved")

            Log.d(TAG, "Setting currency manager...")
            CurrencyManager.setExchangeRate(country.exchangeRate)
            Log.d(TAG, "✅ CurrencyManager updated")

            Log.d(TAG, "Starting HomeActivity...")
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            
            Log.d(TAG, "=== registerUser END ===")

        } catch (e: Exception) {
            Log.e(TAG, "💥 registerUser EXCEPTION", e)
            e.printStackTrace()
            val errorMsg = "${e.javaClass.simpleName}: ${e.message}"
            val stack = e.stackTrace.take(5).joinToString("\n") { "  at ${it.methodName}(${it.fileName}:${it.lineNumber})" }
            Toast.makeText(this, "ERREUR:\n$errorMsg\n\n$stack", Toast.LENGTH_LONG).show()
        }
    }
}