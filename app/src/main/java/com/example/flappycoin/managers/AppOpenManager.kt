package com.example.flappycoin.managers

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenManager(private val application: Application) {

    companion object {
        private const val TAG = "AppOpenManager"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false

    // Charger l'AppOpenAd
    fun loadAd() {
        if (isLoading || appOpenAd != null) return
        isLoading = true

        AppOpenAd.load(
            application,
            Constants.APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoading = false
                    Log.d(TAG, "AppOpenAd chargée")
                }

                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                    Log.e(TAG, "Échec chargement AppOpenAd: ${loadAdError.message}")
                    isLoading = false
                }
            }
        )
    }

    // Afficher la pub si disponible
    fun showAd(activity: Activity) {
        appOpenAd?.show(activity)
        appOpenAd = null
        loadAd() // Recharge pour la prochaine ouverture
        Log.d(TAG, "AppOpenAd affichée")
    }
}