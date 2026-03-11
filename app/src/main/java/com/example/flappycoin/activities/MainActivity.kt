package com.example.flappycoin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.R
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.utils.NetworkManager

/**

MainActivity - Écran Splash

Vérification réseau + redirection
*/
class MainActivity : AppCompatActivity() {

companion object {
private const val TAG = "MainActivity"
}

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

try {  
     Log.d(TAG, "🎬 MainActivity onCreate")  
     setContentView(R.layout.activity_main)  

     // Vérifier connexion réseau  
     if (!NetworkManager.isInternetAvailable(this)) {  
         Log.e(TAG, "❌ Pas de connexion internet")  
         Toast.makeText(  
             this,  
             "Connexion internet requise pour jouer!",  
             Toast.LENGTH_LONG  
         ).show()  
         finish()  
         return  
     }  

     Log.d(TAG, "✅ Connexion internet OK")  

     // Splash écran 2 secondes  
     Handler(Looper.getMainLooper()).postDelayed({  
         try {  
             // Vérifier si utilisateur enregistré  
             val username = GamePreferences.getUsername()  
             Log.d(TAG, "Username: $username")  

             val intent = if (username.isNullOrEmpty()) {  
                 Log.d(TAG, "→ Redirection SignUpActivity")  
                 Intent(this, SignUpActivity::class.java)  
             } else {  
                 Log.d(TAG, "→ Redirection HomeActivity")  
                 Intent(this, HomeActivity::class.java)  
             }  

             startActivity(intent)  
             finish()  
         } catch (e: Exception) {  
             Log.e(TAG, "❌ Erreur redirection", e)  
             finish()  
         }  
     }, 2000)  

 } catch (e: Exception) {  
     Log.e(TAG, "❌ ERREUR MainActivity onCreate", e)  
     e.printStackTrace()  
     finish()  
 }

}
}