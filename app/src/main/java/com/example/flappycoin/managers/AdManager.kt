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
    private var rewardCallback: ((Int) -> Unit)? = null

    fun init(context: Context) {
        MobileAds.initialize(context)
        loadRewardedAd(context)
    }

    // ================= PUBLICITÉ RÉCOMPENSÉE =================
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
                    Log.d("AdManager", "Failed to load rewarded ad: ${adError.message}")
                    isLoadingRewarded = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.d("AdManager", "Rewarded ad loaded")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, callback: (Int) -> Unit) {
        if (rewardedAd == null) {
            Log.d("AdManager", "No rewarded ad loaded")
            return
        }

        if (System.currentTimeMillis() - GamePreferences.getLastRewardedAdTime() < Constants.REWARDED_AD_INTERVAL_MS) {
            Log.d("AdManager", "Rewarded ad interval not met")
            return
        }

        rewardCallback = callback
        rewardedAd?.show(activity) { rewardItem ->
            val reward = Constants.REWARDED_AD_BONUS
            callback(reward)
            GamePreferences.setLastRewardedAdTime(System.currentTimeMillis())
        }
        rewardedAd = null
        loadRewardedAd(activity)
    }

    fun canShowRewardedAd(): Boolean {
        val lastTime = GamePreferences.getLastRewardedAdTime()
        return System.currentTimeMillis() - lastTime >= Constants.REWARDED_AD_INTERVAL_MS
    }

    fun isRewardedAdLoaded(): Boolean = rewardedAd != null
}