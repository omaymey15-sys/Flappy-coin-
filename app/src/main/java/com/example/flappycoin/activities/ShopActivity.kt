package com.example.flappycoin.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flappycoin.databinding.ActivityShopBinding
import com.example.flappycoin.managers.GamePreferences
import com.example.flappycoin.managers.CurrencyManager
import com.example.flappycoin.models.ShopItem
import com.example.flappycoin.ui.ShopAdapter

class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    private lateinit var shopItems: MutableList<ShopItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solde
        val coins = GamePreferences.getTotalCoins()
        val localAmount = CurrencyManager.coinsToLocalCurrency(coins)
        binding.tvBalance.text = "Solde: $localAmount"

        // Items boutique
        shopItems = mutableListOf(
            ShopItem("Red Bird", "L'oiseau original", 0, true),
            ShopItem("Blue Bird", "Oiseau bleu mystérieux", 500, GamePreferences.isItemPurchased("Blue Bird")),
            ShopItem("Golden Bird", "Oiseau en or massif", 1500, GamePreferences.isItemPurchased("Golden Bird")),
            ShopItem("2x Multiplier", "Double les coins", 2000, GamePreferences.isItemPurchased("2x Multiplier")),
            ShopItem("Shield", "Protection 1 collision", 1000, GamePreferences.isItemPurchased("Shield")),
            ShopItem("Coins Pack 100", "100 coins bonus", 50, GamePreferences.isItemPurchased("Coins Pack 100"))
        )

        // Adapter
        val adapter = ShopAdapter(shopItems) { item ->
            purchaseItem(item)
        }

        binding.rvShop.layoutManager = LinearLayoutManager(this)
        binding.rvShop.adapter = adapter

        // Retour
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun purchaseItem(item: ShopItem) {
        val coins = GamePreferences.getTotalCoins()

        if (item.isPurchased) {
            Toast.makeText(this, "${item.name} est déjà acheté!", Toast.LENGTH_SHORT).show()
            return
        }

        if (coins < item.price) {
            Toast.makeText(this, "Pas assez de pièces!", Toast.LENGTH_SHORT).show()
            return
        }

        GamePreferences.removeCoins(item.price)
        GamePreferences.addPurchasedItem(item.name)
        item.isPurchased = true

        Toast.makeText(this, "${item.name} acheté!", Toast.LENGTH_SHORT).show()
        binding.rvShop.adapter?.notifyDataSetChanged()
    }
}