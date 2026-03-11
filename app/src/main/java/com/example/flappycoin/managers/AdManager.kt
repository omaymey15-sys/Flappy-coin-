package com.example.flappycoin.managers

import android.app.Activity
import com.example.flappycoin.utils.Constants
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.LoadAdError

object AdManager {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    // ================= BANNER =================
    fun loadBanner(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    // ================= INTERSTITIAL =================
    fun loadInterstitial(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            Constants.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity) {
        interstitialAd?.show(activity)
    }

    // ================= REWARDED =================
    fun loadRewardedAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            Constants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun isRewardedAdLoaded(): Boolean {
        return rewardedAd != null
    }

    fun showRewardedAd(activity: Activity, onReward: (RewardItem) -> Unit) {
        rewardedAd?.show(activity) { rewardItem ->
            onReward(rewardItem)
            // Recharger la pub pour la prochaine fois
            loadRewardedAd(activity)
        }
    }
}