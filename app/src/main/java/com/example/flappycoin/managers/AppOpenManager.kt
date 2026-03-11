package com.example.flappycoin.managers

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.example.flappycoin.utils.Constants  // ← Important !

class AppOpenManager(private val application: Application) {

    companion object {
        private const val TAG = "AppOpenManager"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false

    fun loadAd() {
        if (isLoading || appOpenAd != null) return
        isLoading = true

        AppOpenAd.load(
            application,
            Constants.ADMOB_APP_ID, // ou Constants.APP_OPEN_AD_UNIT_ID si tu le rajoutes
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoading = false
                    Log.d(TAG, "AppOpen chargée")
                }

                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                    Log.e(TAG, "Échec AppOpen: ${loadAdError.message}")
                    isLoading = false
                }
            }
        )
    }

    fun showAd(activity: Activity) {
        appOpenAd?.show(activity)
        appOpenAd = null
        loadAd()
    }
}