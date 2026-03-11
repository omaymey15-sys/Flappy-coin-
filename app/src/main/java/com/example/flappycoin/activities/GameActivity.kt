package com.example.flappycoin.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.ui.GameView
import com.example.flappycoin.utils.NetworkManager

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private var lastGameOverTime = 0L
    private var lastInterstitialTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 FrameLayout pour gameView + banner
        val rootLayout = FrameLayout(this)

        gameView = GameView(this) { score, coins, distance, time ->
            showGameOver(score, coins, distance, time)
        }
        rootLayout.addView(gameView)

        // 🔹 Ajouter Banner AdView
        val bannerView = AdManager.createBanner(applicationContext)
        val bannerParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        bannerParams.topMargin = 20
        rootLayout.addView(bannerView, bannerParams)

        setContentView(rootLayout)

        // 🔹 Charger Rewarded + Interstitial
        AdManager.loadRewardedAd(applicationContext)
        AdManager.loadInterstitial(applicationContext)
    }

    private fun showGameOver(score: Int, coins: Int, distance: Int, time: Long) {
        lastGameOverTime = System.currentTimeMillis()

        val timeSeconds = time / 1000
        val distanceM = distance
        val localCurrency = CurrencyManager.coinsToLocalCurrency(coins)

        val dialog = AlertDialog.Builder(this)
            .setTitle("GAME OVER")
            .setMessage(
                "Score: $score\n" +
                        "Pièces: $coins ($localCurrency)\n" +
                        "Distance: ${distanceM}m\n" +
                        "Temps: ${timeSeconds}s"
            )
            .setPositiveButton("Regarder pub pour relancer") { _, _ ->
                if (!NetworkManager.isInternetAvailable(this)) {
                    Toast.makeText(this, "Connexion internet requise", Toast.LENGTH_SHORT).show()
                    finish()
                    return@setPositiveButton
                }

                if (AdManager.isRewardedAdLoaded()) {
                    AdManager.showRewardedAd(this) { reward ->
                        GamePreferences.addCoins(reward)
                        gameView.revive()
                    }
                } else {
                    Toast.makeText(this, "Pub non disponible", Toast.LENGTH_SHORT).show()
                    gameView.revive()
                }
            }
            .setNegativeButton("Retour au menu") { _, _ ->
                GamePreferences.apply {
                    setBestScore(score)
                    setBestCoins(coins)
                    addCoins(coins)
                    addDistance(distance)
                    addTime(time)
                    incrementGames()
                }

                // 🔹 Montrer Interstitial si >5 min depuis dernier
                if (System.currentTimeMillis() - lastInterstitialTime >= 5 * 60 * 1000) {
                    AdManager.showInterstitial(this)
                    lastInterstitialTime = System.currentTimeMillis()
                }

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()

        // 🔹 Vérifier interstitiel automatique toutes les 5 minutes
        if (System.currentTimeMillis() - lastInterstitialTime >= 5 * 60 * 1000) {
            AdManager.showInterstitial(this)
            lastInterstitialTime = System.currentTimeMillis()
        }
    }
}