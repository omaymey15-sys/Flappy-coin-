package com.example.flappycoin.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappycoin.R
import com.example.flappycoin.managers.AdManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.ui.GameView
import com.example.flappycoin.utils.Constants
import com.example.flappycoin.utils.NetworkManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private var adView: AdView? = null
    
    // Variable renommée pour éviter tout conflit
    private var adLoadingInProgress = false
    private var bannerLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser AdMob
        AdManager.init(this)

        // Créer le layout principal
        val mainLayout = FrameLayout(this)

        // Créer la vue du jeu
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

        // Paramètres de layout pour la vue du jeu
        val gameLayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mainLayout.addView(gameView, gameLayoutParams)

        // Charger la bannière si la connexion est disponible
        if (NetworkManager.isInternetAvailable(this)) {
            loadBannerAd(mainLayout)
        }

        setContentView(mainLayout)
    }

    private fun loadBannerAd(mainLayout: FrameLayout) {
        // Créer la bannière publicitaire
        adView = AdView(this)
        adView?.adUnitId = Constants.BANNER_AD_UNIT_ID
        adView?.adSize = AdSize.BANNER
        
        // Cachée par défaut
        adView?.visibility = View.GONE

        // Ajouter au layout (en bas)
        val adLayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        adLayoutParams.gravity = android.view.Gravity.BOTTOM
        mainLayout.addView(adView, adLayoutParams)

        // Listener pour savoir quand la bannière est chargée
        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Bannière chargée avec succès → on l'affiche
                bannerLoaded = true
                adView?.visibility = View.VISIBLE
                println("✅ Bannière chargée et affichée")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Échec du chargement → on la cache
                bannerLoaded = false
                adView?.visibility = View.GONE
                println("❌ Bannière non disponible: ${adError.message}")
                
                // Réessayer plus tard
                adView?.postDelayed({
                    if (!bannerLoaded && NetworkManager.isInternetAvailable(this@GameActivity)) {
                        loadBannerAd(mainLayout)
                    }
                }, 60000)
            }
        }

        // Charger l'annonce
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)
    }

    private fun onWatchAdClicked() {
        // Utilisation de la nouvelle variable
        if (adLoadingInProgress) return

        if (!NetworkManager.isInternetAvailable(this)) {
            showNoInternetDialog()
            return
        }

        if (!AdManager.isRewardedAdLoaded()) {
            showAdNotReadyDialog()
            AdManager.loadRewardedAd(this)
            return
        }

        // ← Ligne 74 : Modification de adLoadingInProgress
        adLoadingInProgress = true

        AdManager.showRewardedAd(this) {
            adLoadingInProgress = false
            Toast.makeText(this@GameActivity, "🎮 Bonne chance !", Toast.LENGTH_SHORT).show()
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
        adView?.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
        adView?.resume()
        adLoadingInProgress = false
        
        if (!bannerLoaded && NetworkManager.isInternetAvailable(this)) {
            adView?.loadAd(AdRequest.Builder().build())
        }
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }
}
