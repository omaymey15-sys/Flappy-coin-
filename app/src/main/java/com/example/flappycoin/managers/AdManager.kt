package com.example.flappycoin.managers

import android.app.Application
import android.util.Log
import com.example.flappycoin.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"

    private var rewardedAd: RewardedAd? = null
    private var lastRewardedAdTime: Long = 0

    // ================= Banner =================
    fun loadBanner(application: Application, adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        Log.d(TAG, "✅ Banner loaded")
    }

    // ================= Rewarded Ad =================
    fun loadRewardedAd(application: Application) {
        // Vérifie si on peut charger selon l’intervalle
        val now = System.currentTimeMillis()
        if (now - lastRewardedAdTime < Constants.REWARDED_AD_INTERVAL_MS) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            application,
            Constants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "❌ RewardedAd failed to load: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "✅ RewardedAd loaded")
                    rewardedAd = ad
                    lastRewardedAdTime = System.currentTimeMillis()
                }
            }
        )
    }

    fun isRewardedAdLoaded(): Boolean = rewardedAd != null

    fun showRewardedAd(application: Application, onReward: (RewardItem) -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(application as? androidx.appcompat.app.AppCompatActivity ?: return) { reward ->
                onReward(reward)
            }
            rewardedAd = null // Reset après affichage
        } ?: run {
            Log.d(TAG, "⚠️ RewardedAd not loaded")
        }
    }
}