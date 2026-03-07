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

/**
 * Gestion complète AdMob
 * Banner, Interstitiel, Récompensée
 */
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

    // ============= PUBLICITÉ RÉCOMPENSÉE =============

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
                    Log.d("AdManager", "Pub récompensée échouée: ${adError.message}")
                    isLoadingRewarded = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.d("AdManager", "Pub récompensée chargée")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, callback: (Int) -> Unit) {
        if (rewardedAd == null) {
            Log.d("AdManager", "Pub récompensée non disponible")
            return
        }

        if (!canShowRewardedAd()) {
            Log.d("AdManager", "Intervalle minimum non atteint")
            return
        }

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