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

override fun onCreate(savedInstanceState: Bundle?) {  
    super.onCreate(savedInstanceState)  
    binding = ActivityShopBinding.inflate(layoutInflater)  
    setContentView(binding.root)  

    // Mise à jour solde  
    val coins = GamePreferences.getTotalCoins()  
    val localAmount = CurrencyManager.coinsToLocalCurrency(coins)  
    binding.tvBalance.text = "Solde: $localAmount"  

    val shopItems = listOf(  
        ShopItem("Red Bird", "L'oiseau original", 0, true),  
        ShopItem("Blue Bird", "Oiseau bleu mystérieux", 500, false),  
        ShopItem("Golden Bird", "Oiseau en or massif", 1500, false),  
        ShopItem("2x Multiplier", "Double les coins", 2000, false),  
        ShopItem("Shield", "Protection 1 collision", 1000, false),  
        ShopItem("Coins Pack 100", "100 coins bonus", 50, false)  
    )  

    val adapter = ShopAdapter(shopItems) { item ->  
        purchaseItem(item)  
    }  

    binding.rvShop.layoutManager = LinearLayoutManager(this)  
    binding.rvShop.adapter = adapter  

    binding.btnBack.setOnClickListener {  
        finish()  
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
    recreate()  
}

}