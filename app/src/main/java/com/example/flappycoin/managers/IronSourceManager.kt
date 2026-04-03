package com.example.flappycoin.managers

import android.app.Activity
import android.util.Log
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.InterstitialListener
import com.ironsource.mediationsdk.sdk.RewardedVideoListener
import com.ironsource.mediationsdk.sdk.placement.Placement
import com.ironsource.mediationsdk.IronSourceBannerLayout

object IronSourceManager {

    private const val TAG = "IronSourceManager"

    // ----------------------------------
    // BANNIÈRES
    // ----------------------------------
    fun loadBanner(activity: Activity, banner: IronSourceBannerLayout) {
        IronSource.loadBanner(banner)
        Log.d(TAG, "Banner requested")
    }

    // ----------------------------------
    // INTERSTITIELS
    // ----------------------------------
    fun initInterstitial(activity: Activity) {
        IronSource.loadInterstitial()
        IronSource.setInterstitialListener(object : InterstitialListener {
            override fun onInterstitialAdReady() { Log.d(TAG, "Interstitial ready") }
            override fun onInterstitialAdLoadFailed(error: IronSourceError) {
                Log.e(TAG, "Interstitial failed: ${error.errorMessage}")
            }
            override fun onInterstitialAdOpened() { Log.d(TAG, "Interstitial opened") }
            override fun onInterstitialAdClosed() { Log.d(TAG, "Interstitial closed") }
            override fun onInterstitialAdShowSucceeded() { Log.d(TAG, "Interstitial shown") }
            override fun onInterstitialAdShowFailed(error: IronSourceError) {
                Log.e(TAG, "Interstitial show failed: ${error.errorMessage}")
            }
        })
    }

    fun showInterstitial() {
        if (IronSource.isInterstitialReady()) {
            IronSource.showInterstitial()
        } else {
            Log.d(TAG, "Interstitial not ready yet")
        }
    }

    // ----------------------------------
    // REWARDED VIDEOS
    // ----------------------------------
    fun initRewardedVideo(activity: Activity, rewardCallback: (success: Boolean) -> Unit) {
        IronSource.setRewardedVideoListener(object : RewardedVideoListener {
            override fun onRewardedVideoAdOpened() { Log.d(TAG, "Rewarded opened") }
            override fun onRewardedVideoAdClosed() { Log.d(TAG, "Rewarded closed") }
            override fun onRewardedVideoAvailabilityChanged(available: Boolean) {
                Log.d(TAG, "Rewarded available: $available")
            }
            override fun onRewardedVideoAdStarted() { Log.d(TAG, "Rewarded started") }
            override fun onRewardedVideoAdEnded() { Log.d(TAG, "Rewarded ended") }
            override fun onRewardedVideoAdShowFailed(error: IronSourceError) {
                Log.e(TAG, "Rewarded show failed: ${error.errorMessage}")
                rewardCallback(false)
            }
            override fun onRewardedVideoAdClicked(placement: Placement) { Log.d(TAG, "Rewarded clicked") }
            override fun onRewardedVideoAdRewarded(placement: Placement) {
                Log.d(TAG, "Reward granted: ${placement.rewardAmount} ${placement.rewardName}")
                rewardCallback(true)
            }
        })

        IronSource.loadRewardedVideo()
    }

    fun showRewardedVideo() {
        if (IronSource.isRewardedVideoAvailable()) {
            IronSource.showRewardedVideo()
        } else {
            Log.d(TAG, "Rewarded video not ready")
        }
    }
}