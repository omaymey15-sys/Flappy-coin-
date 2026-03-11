package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flappycoin.databinding.ActivityShopBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.models.ShopItem
import com.example.flappycoin.ui.ShopAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdListener

class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 🔹 Inflate layout
            binding = ActivityShopBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // 🔹 Mise à jour solde
            val coins = GamePreferences.getTotalCoins()
            val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
            binding.tvBalance.text = "Solde: $localAmount"

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
            val adapter = ShopAdapter(shopItems) { item ->
                purchaseItem(item)
            }
            binding.rvShop.layoutManager = LinearLayoutManager(this)
            binding.rvShop.adapter = adapter

            // 🔹 Bouton retour
            binding.btnBack.setOnClickListener { finish() }

            // 🔹 Initialisation AdMob
            MobileAds.initialize(this) { Log.d("ShopActivity", "AdMob initialized") }

            // 🔹 Charger la bannière
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)

            // 🔹 Listener pour debug
            binding.adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("ShopActivity", "Ad loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("ShopActivity", "Ad failed: ${adError.message}")
                }
            }

        } catch (e: Exception) {
            Log.e("ShopActivity", "Exception dans onCreate", e)
            Toast.makeText(
                this,
                "⚠️ ShopActivity crash\nType: ${e::class.simpleName}\nMessage: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
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
        recreate() // Recharge l'Activity pour mettre à jour le solde et l'état
    }
}