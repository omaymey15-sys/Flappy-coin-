package com.example.flappycoin.managers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.example.flappycoin.utils.Constants

object AdManager {

    private const val TAG = "AdManager"

    // ───────────── BANNER ─────────────
    private var bannerAdView: AdView? = null

    fun createBanner(context: Context): AdView {
        if (bannerAdView == null) {
            bannerAdView = AdView(context).apply {
                adSize = AdSize.BANNER
                adUnitId = Constants.BANNER_AD_UNIT_ID
            }
            bannerAdView?.loadAd(AdRequest.Builder().build())
            Log.d(TAG, "Banner créée")
        }
        return bannerAdView!!
    }

    fun showBanner(container: FrameLayout) {
        bannerAdView?.let { banner ->
            if (banner.parent == null) container.addView(banner)
            banner.visibility = View.VISIBLE
            Log.d(TAG, "Banner affichée")
        }
    }

    fun hideBanner() {
        bannerAdView?.visibility = View.GONE
    }

    // ───────────── REWARDED ─────────────
    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false
    private var lastRewardedTime = 0L

    fun loadRewarded(context: Context) {
        if (isLoadingRewarded || rewardedAd != null) return
        isLoadingRewarded = true

        RewardedAd.load(
            context,
            Constants.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.d(TAG, "Rewarded chargée")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Rewarded échouée: ${adError.message}")
                    isLoadingRewarded = false
                }
            }
        )
    }

    fun canShowRewarded(): Boolean {
        return System.currentTimeMillis() - lastRewardedTime >= Constants.REWARDED_AD_INTERVAL_MS
    }

    fun showRewarded(activity: Activity, onReward: () -> Unit) {
        if (rewardedAd == null || !canShowRewarded()) {
            Log.d(TAG, "Rewarded non disponible ou intervalle non atteint")
            return
        }

        rewardedAd?.show(activity) { _ ->
            onReward()
            lastRewardedTime = System.currentTimeMillis()
        }

        rewardedAd = null
        loadRewarded(activity)
    }

    // ───────────── INTERSTITIAL ─────────────
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var lastInterstitialTime = 0L

    fun loadInterstitial(context: Context) {
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true

        InterstitialAd.load(
            context,
            Constants.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    Log.d(TAG, "Interstitial chargée")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial échouée: ${adError.message}")
                    isLoadingInterstitial = false
                }
            }
        )
    }

    fun canShowInterstitial(): Boolean {
        return System.currentTimeMillis() - lastInterstitialTime >= Constants.INTERSTITIAL_AD_INTERVAL_MS
    }

    fun showInterstitial(activity: Activity) {
        if (interstitialAd != null && canShowInterstitial()) {
            interstitialAd?.show(activity)
            lastInterstitialTime = System.currentTimeMillis()
            interstitialAd = null
            loadInterstitial(activity)
        } else {
            Log.d(TAG, "Interstitial non disponible ou intervalle non atteint")
        }
    }

    // ───────────── APP OPEN ─────────────
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false

    fun loadAppOpen(application: Application) {
        if (isAppOpenLoading || appOpenAd != null) return
        isAppOpenLoading = true

        AppOpenAd.load(
            application,
            Constants.APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    Log.d(TAG, "AppOpen chargée")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "AppOpen échouée: ${loadAdError.message}")
                    isAppOpenLoading = false
                }
            }
        )
    }

    fun showAppOpen(activity: Activity) {
        appOpenAd?.show(activity)
        appOpenAd = null
        Log.d(TAG, "AppOpen affichée")
    }

    // ───────────── INITIALISATION GLOBALE ─────────────
    fun init(application: Application) {
        MobileAds.initialize(application)
        loadRewarded(application)
        loadInterstitial(application)
        loadAppOpen(application)
        Log.d(TAG, "AdManager initialisé")
    }
}