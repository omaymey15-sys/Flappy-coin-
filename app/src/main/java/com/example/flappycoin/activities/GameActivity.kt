package com.example.flappycoin.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.R
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.ui.GameView
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.NetworkManager

class GameActivity : AppCompatActivity() {

private lateinit var gameView: GameView  
private var isWaitingForAd = false  

override fun onCreate(savedInstanceState: Bundle?) {  
    super.onCreate(savedInstanceState)  

    AdManager.init(this)  

    gameView = GameView(  
        this,  
        { score, coins, distance, time ->  
            onGameOver(score, coins, distance, time)  
        },  
        {  
            onWatchAdClicked()  
        },  
        {  
            returnToMenu()  
        }  
    )  

    setContentView(gameView)  
}  

private fun onWatchAdClicked() {  
    if (isWaitingForAd) return  

    if (!NetworkManager.isInternetAvailable(this)) {  
        showNoInternetDialog()  
        return  
    }  

    if (!AdManager.isRewardedAdLoaded()) {  
        showAdNotReadyDialog()  
        AdManager.loadRewardedAd(this)  
        return  
    }  

    isWaitingForAd = true  

    AdManager.showRewardedAd(this) {  
        isWaitingForAd = false  
        Toast.makeText(this, "🎮 Bonne chance !", Toast.LENGTH_SHORT).show()  
        gameView.revive()  
    }  
}  

private fun showNoInternetDialog() {  
    AlertDialog.Builder(this)  
        .setTitle("📶 Pas de connexion")  
        .setMessage("Une connexion internet est requise pour regarder une publicité.")  
        .setPositiveButton("OK") { _, _ -> }  
        .show()  
}  

private fun showAdNotReadyDialog() {  
    AlertDialog.Builder(this)  
        .setTitle("📺 Publicité en chargement")  
        .setMessage("La publicité n'est pas encore prête. Veuillez réessayer dans quelques instants.")  
        .setPositiveButton("OK") { _, _ -> }  
        .show()  
}  

private fun onGameOver(score: Int, coins: Int, distance: Int, time: Long) {  
    GamePreferences.apply {  
        setBestScore(score)  
        setBestCoins(coins)  
        addCoins(coins)  
        addDistance(distance)  
        addTime(time)  
        incrementGames()  
    }  

    if (score > GamePreferences.getBestScore()) {  
        Toast.makeText(this, "🏆 Nouveau record !", Toast.LENGTH_SHORT).show()  
    }  

    if (!AdManager.isRewardedAdLoaded()) {  
        AdManager.loadRewardedAd(this)  
    }  
}  

private fun returnToMenu() {  
    val intent = Intent(this, HomeActivity::class.java)  
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP  
    startActivity(intent)  
    finish()  
}  

override fun onPause() {  
    super.onPause()  
    gameView.pause()  
}  

override fun onResume() {  
    super.onResume()  
    gameView.resume()  
    isWaitingForAd = false  
}

}