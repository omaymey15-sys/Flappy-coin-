package com.example.flappycoin.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.LoadAdError
import kotlin.system.measureTimeMillis

object AdManager {

    private const val TAG = "AdManager"

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null
    private var mAppOpenAd: AppOpenAd? = null
    private var lastRewardedTime = 0L

    fun init(activity: Activity) {
        MobileAds.initialize(activity) { initializationStatus ->
            Log.d(TAG, "MobileAds initialized: $initializationStatus")
        }
    }

    // ================= BANNER =================
    fun createBanner(activity: Activity): AdView {
        val adView = AdView(activity)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" // test ID
        return adView
    }

    fun loadBanner(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    // ================= INTERSTITIAL =================
    fun loadInterstitial(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            "ca-app-pub-3940256099942544/1033173712", // test ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial loaded")
                    mInterstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial failed: $adError")
                    mInterstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity) {
        mInterstitialAd?.let { ad ->
            ad.show(activity)
            mInterstitialAd = null
            loadInterstitial(activity)
        } ?: Log.d(TAG, "Interstitial not loaded yet")
    }

    // ================= REWARDED =================
    fun loadRewardedAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            "ca-app-pub-3940256099942544/5224354917", // test ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "RewardedAd loaded")
                    mRewardedAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "RewardedAd failed: $adError")
                    mRewardedAd = null
                }
            }
        )
    }

    fun isRewardedAdLoaded(): Boolean = mRewardedAd != null

    fun showRewardedAd(activity: Activity, callback: (Int) -> Unit) {
        mRewardedAd?.let { ad ->
            ad.show(activity) { rewardItem: RewardItem ->
                callback(rewardItem.amount)
                mRewardedAd = null
                loadRewardedAd(activity)
            }
        } ?: Log.d(TAG, "RewardedAd not loaded")
    }

    fun canShowRewardedAd(): Boolean {
        val now = System.currentTimeMillis()
        return mRewardedAd != null && now - lastRewardedTime >= 5 * 60 * 1000
    }

    // ================= APP OPEN =================
    fun loadAppOpenAd(activity: Activity) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            activity,
            "ca-app-pub-3940256099942544/3419835294", // test ID
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "AppOpenAd loaded")
                    mAppOpenAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "AppOpenAd failed: $loadAdError")
                    mAppOpenAd = null
                }
            }
        )
    }

    fun isAppOpenAdLoaded(): Boolean = mAppOpenAd != null

    fun showAppOpenAd(activity: Activity, callback: () -> Unit) {
        mAppOpenAd?.let { ad ->
            ad.show(activity, object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AppOpenAd dismissed")
                    mAppOpenAd = null
                    callback()
                    loadAppOpenAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "AppOpenAd failed to show: $adError")
                    mAppOpenAd = null
                    callback()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "AppOpenAd showed")
                }
            })
        } ?: callback()
    }
}