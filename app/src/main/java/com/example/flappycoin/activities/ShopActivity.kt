package com.example.flappycoin.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flappycoin.databinding.ActivityShopBinding
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.models.ShopItem
import com.example.flappycoin.ui.ShopAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    private lateinit var shopItems: MutableList<ShopItem>
    private lateinit var adapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Initialisation AdMob
        MobileAds.initialize(this) { Log.d("ShopActivity", "AdMob initialized") }
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() { Log.d("ShopActivity", "Ad loaded") }
            override fun onAdFailedToLoad(error: LoadAdError) { Log.e("ShopActivity", "Ad failed: $error") }
        }

        // 🔹 Mise à jour solde
        updateBalance()

        // 🔹 Liste des items
        shopItems = mutableListOf(
            ShopItem("Red Bird", "L'oiseau original", 0, true),
            ShopItem("Blue Bird", "Oiseau bleu mystérieux", 500, GamePreferences.isItemPurchased("Blue Bird")),
            ShopItem("Golden Bird", "Oiseau en or massif", 1500, GamePreferences.isItemPurchased("Golden Bird")),
            ShopItem("2x Multiplier", "Double les coins", 2000, GamePreferences.isItemPurchased("2x Multiplier")),
            ShopItem("Shield", "Protection 1 collision", 1000, GamePreferences.isItemPurchased("Shield")),
            ShopItem("Coins Pack 100", "100 coins bonus", 50, GamePreferences.isItemPurchased("Coins Pack 100"))
        )

        // 🔹 Adapter RecyclerView avec callback dynamique
        adapter = ShopAdapter(shopItems) { item, position ->
            purchaseItem(item, position)
        }

        binding.rvShop.layoutManager = LinearLayoutManager(this)
        binding.rvShop.adapter = adapter

        // 🔹 Bouton retour
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun updateBalance() {
        val coins = GamePreferences.getTotalCoins()
        val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
        binding.tvBalance.text = "Solde: $localAmount"
    }

    private fun purchaseItem(item: ShopItem, position: Int) {
        if (item.isPurchased) {
            Toast.makeText(this, "${item.name} déjà acheté!", Toast.LENGTH_SHORT).show()
            return
        }

        val coins = GamePreferences.getTotalCoins()
        if (coins < item.price) {
            Toast.makeText(this, "Pas assez de pièces!", Toast.LENGTH_SHORT).show()
            return
        }

        GamePreferences.apply {
            removeCoins(item.price)
            addPurchasedItem(item.name)
        }

        item.isPurchased = true
        adapter.notifyItemChanged(position)
        updateBalance()

        Toast.makeText(this, "${item.name} acheté!", Toast.LENGTH_SHORT).show()
    }
}