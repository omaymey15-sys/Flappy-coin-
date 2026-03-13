package com.example.flappycoin.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flappycoin.databinding.ActivityShopBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.models.ShopItem
import com.example.flappycoin.ui.ShopAdapter
import com.example.flappycoin.utils.AdHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    private val TAG = "ShopActivity"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var rewardRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityShopBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // 🔹 Mise à jour solde initial
            updateBalance()

            // 🔹 Liste des items du shop
            val shopItems = listOf(
                ShopItem("Red Bird", "L'oiseau original", 0, true),
                ShopItem("Blue Bird", "Oiseau bleu mystérieux", 500, false),
                ShopItem("Golden Bird", "Oiseau en or massif", 1500, false),
                ShopItem("2x Multiplier", "Double les coins", 2000, false),
                ShopItem("Shield", "Protection 1 collision", 1000, false),
                ShopItem("Coins Pack 100", "100 coins bonus", 50, false)
            )

            // 🔹 Adapter RecyclerView
            val adapter = ShopAdapter(shopItems) { item -> purchaseItem(item) }
            binding.rvShop.layoutManager = LinearLayoutManager(this)
            binding.rvShop.adapter = adapter

            // 🔹 Bouton retour
            binding.btnBack.setOnClickListener { finish() }

            // 🔹 Initialisation AdMob banner
            MobileAds.initialize(this) { Log.d(TAG, "AdMob initialized") }
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.adListener = object : AdListener() {
                override fun onAdLoaded() { Log.d(TAG, "Banner loaded") }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Banner failed: ${adError.message}")
                }
            }

            // 🔹 Rewarded Ad à l’ouverture
            AdHelper.loadRewardedAd(this)
            binding.root.postDelayed({
                AdHelper.showRewardedAd(this) { reward ->
                    // ✅ Ajout de la récompense au solde
                    GamePreferences.addCoins(reward.amount)
                    updateBalance()
                    Toast.makeText(
                        this,
                        "Vous avez gagné ${reward.amount} ${reward.type}!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, 500)

            // 🔹 Rewarded Ad toutes les 5 minutes
            rewardRunnable = object : Runnable {
                override fun run() {
                    AdHelper.showRewardedAd(this@ShopActivity) { reward ->
                        // ✅ Ajout de la récompense au solde
                        GamePreferences.addCoins(reward.amount)
                        updateBalance()
                        Toast.makeText(
                            this@ShopActivity,
                            "Vous avez gagné ${reward.amount} ${reward.type}!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    handler.postDelayed(this, 5 * 60 * 1000) // toutes les 5 minutes
                }
            }
            handler.postDelayed(rewardRunnable, 5 * 60 * 1000)

        } catch (e: Exception) {
            Log.e(TAG, "Exception dans onCreate", e)
            Toast.makeText(
                this,
                "⚠️ ShopActivity crash\nType: ${e::class.simpleName}\nMessage: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateBalance() {
        val coins = GamePreferences.getTotalCoins()
        val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
        binding.tvBalance.text = "Solde: $localAmount"
        binding.tvCoinsCount.text = "🪙 $coins"
    }

    private fun purchaseItem(item: ShopItem) {
        val coins = GamePreferences.getTotalCoins()
        if (coins < item.price) {
            Toast.makeText(this, "Pas assez de pièces!", Toast.LENGTH_SHORT).show()
            return
        }

        GamePreferences.apply {
            removeCoins(item.price)
            addPurchasedItem(item.name)
        }

        Toast.makeText(this, "${item.name} acheté!", Toast.LENGTH_SHORT).show()
        updateBalance()
        binding.rvShop.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rewardRunnable)
    }
}