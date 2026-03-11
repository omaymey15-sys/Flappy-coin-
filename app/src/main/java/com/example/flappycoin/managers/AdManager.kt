package com.example.flappycoin.managers

import android.app.Application
import android.util.Log
import com.example.flappycoin.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenManager(private val application: Application) {

    private var appOpenAd: AppOpenAd? = null
    private val TAG = "AppOpenManager"

    fun loadAd() {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            Constants.REWARDED_AD_UNIT_ID, // TODO: mettre ton vrai APP_OPEN_AD_UNIT_ID
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "✅ AppOpenAd loaded")
                    appOpenAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "❌ AppOpenAd failed to load: ${loadAdError.message}")
                    appOpenAd = null
                }
            }
        )
    }

    fun showAdIfAvailable() {
        appOpenAd?.show(application as? androidx.appcompat.app.AppCompatActivity ?: return)
        appOpenAd = null
    }
}