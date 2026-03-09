package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.databinding.ActivitySettingsBinding
import com.example.flappycoin.managers.GamePreferences

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    companion object {
        private const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "🔧 onCreate démarré")
            
            // ✅ Inflation du layout avec gestion d'erreur
            try {
                binding = ActivitySettingsBinding.inflate(layoutInflater)
                Log.d(TAG, "✅ Layout inflé avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur inflation layout", e)
                Toast.makeText(
                    this,
                    "❌ Erreur layout: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }
            
            setContentView(binding.root)

            // ✅ Configuration des switches
            try {
                val audioEnabled = GamePreferences.isAudioEnabled()
                binding.swSound.isChecked = audioEnabled
                binding.swSound.setOnCheckedChangeListener { _, isChecked ->
                    try {
                        GamePreferences.setAudioEnabled(isChecked)
                        Log.d(TAG, "🔊 Audio: $isChecked")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erreur audio switch", e)
                    }
                }
                Log.d(TAG, "✅ Switch son configuré")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur configuration son", e)
            }

            try {
                val vibrationEnabled = GamePreferences.isVibrationEnabled()
                binding.swVibration.isChecked = vibrationEnabled
                binding.swVibration.setOnCheckedChangeListener { _, isChecked ->
                    try {
                        GamePreferences.setVibrationEnabled(isChecked)
                        Log.d(TAG, "📳 Vibration: $isChecked")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erreur vibration switch", e)
                    }
                }
                Log.d(TAG, "✅ Switch vibration configuré")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur configuration vibration", e)
            }

            // ✅ Affichage des infos utilisateur
            try {
                val username = GamePreferences.getUsername() ?: "Inconnu"
                val country = GamePreferences.getCountry() ?: "Non défini"
                val language = GamePreferences.getLanguage() ?: "Non défini"
                val currency = GamePreferences.getCurrency() ?: "USD"
                
                binding.tvUsername.text = "👤 Utilisateur: $username"
                binding.tvCountry.text = "🌍 Pays: $country"
                binding.tvLanguage.text = "🗣️ Langue: $language"
                binding.tvCurrency.text = "💰 Devise: $currency"
                
                Log.d(TAG, "✅ Infos: $username / $country / $language / $currency")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur affichage infos", e)
                Toast.makeText(
                    this,
                    "⚠️ Erreur chargement infos",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // ✅ Bouton retour
            try {
                binding.btnBack.setOnClickListener {
                    Log.d(TAG, "⬅️ Retour cliqué")
                    finish()
                }
                Log.d(TAG, "✅ Bouton retour configuré")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur bouton retour", e)
            }
            
            Log.d(TAG, "✅✅✅ onCreate terminé avec succès!")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERREUR GÉNÉRALE dans onCreate", e)
            Log.e(TAG, "Stack trace complet: ${e.stackTraceToString()}")
            
            Toast.makeText(
                this,
                "⚠️ SettingsActivity crash\n" +
                "Type: ${e::class.simpleName}\n" +
                "Message: ${e.message}\n" +
                "Cause: ${e.cause}",
                Toast.LENGTH_LONG
            ).show()
            
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📍 onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "📍 onPause")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "📍 onDestroy")
    }
}