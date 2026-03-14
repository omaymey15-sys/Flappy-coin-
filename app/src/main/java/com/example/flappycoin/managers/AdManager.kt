package com.example.flappycoin.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.flappycoin.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false

    fun init(context: Context) {
        try {
            MobileAds.initialize(context)
            loadRewardedAd(context)
        } catch (e: Exception) {
            Log.e("AdManager", "Erreur init AdMob", e)
        }
    }

    fun loadRewardedAd(context: Context) {
        if (isLoadingRewarded || rewardedAd != null) return

        isLoadingRewarded = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            Constants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdManager", "Pub échouée: ${adError.message}")
                    isLoadingRewarded = false
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.d("AdManager", "Pub chargée")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, callback: () -> Unit) {
        if (rewardedAd == null) {
            Log.d("AdManager", "Pub non disponible")
            return
        }

        rewardedAd?.show(activity) { rewardItem ->
            callback()
            GamePreferences.setLastRewardedAdTime(System.currentTimeMillis())
        }
        
        rewardedAd = null
        loadRewardedAd(activity)
    }

    fun isRewardedAdLoaded(): Boolean = rewardedAd != null
}