apackage com.example.flappycoin.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdHelper {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private const val TAG = "AdHelper"
    private val handler = Handler(Looper.getMainLooper())
    private var rewardRunnable: Runnable? = null

    // ---------------- Interstitial ----------------
    fun loadInterstitial(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            "ca-app-pub-1299408509965704/5246862599", // test ad unit
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial failed: ${adError.message}")
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed")
                interstitialAd = null
                loadInterstitial(activity) // Reload pour la prochaine fois
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                interstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial shown")
            }
        }

        interstitialAd?.show(activity) ?: Log.d(TAG, "Interstitial not ready")
    }

    // ---------------- Rewarded ----------------
    fun loadRewardedAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            "ca-app-pub-1299408509965704/5901468710", // test ad unit
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "RewardedAd loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "RewardedAd failed: ${adError.message}")
                    rewardedAd = null
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onReward: (RewardItem) -> Unit) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "RewardedAd dismissed")
                rewardedAd = null
                loadRewardedAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "RewardedAd failed to show: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "RewardedAd shown")
            }
        }

        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onReward(rewardItem)
            }
        } else {
            Log.d(TAG, "RewardedAd not ready")
        }
    }

    fun scheduleRewardedEvery5Minutes(activity: Activity, onReward: (RewardItem) -> Unit) {
        rewardRunnable = object : Runnable {
            override fun run() {
                showRewardedAd(activity, onReward)
                handler.postDelayed(this, 5 * 60 * 1000) // toutes les 5 minutes
            }
        }
        handler.postDelayed(rewardRunnable!!, 5 * 60 * 1000)
    }

    fun cancelScheduledRewarded() {
        rewardRunnable?.let { handler.removeCallbacks(it) }
        rewardRunnable = null
    }
}