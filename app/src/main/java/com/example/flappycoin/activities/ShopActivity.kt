package com.example.flappycoin.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flappycoin.R
import com.example.flappycoin.databinding.ActivityShopBinding

// Data class pour les items de la boutique
data class ShopItem(
    val id: Int,
    val name: String,
    val price: Int,
    var isPurchased: Boolean = false
)

// Adapter simple pour RecyclerView
class ShopAdapter(
    private val items: List<ShopItem>,
    private val onItemClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    inner class ShopViewHolder(val binding: ShopItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ShopItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvItemName.text = item.name
        holder.binding.tvItemPrice.text = "${item.price} Coins"
        holder.binding.btnBuy.text = if (item.isPurchased) "✔ acheté" else "Acheter"
        holder.binding.btnBuy.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}

// Activity Shop
class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    private val shopItems = mutableListOf<ShopItem>()
    private var userCoins = 1000 // Exemple de coins utilisateur

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation des items de la boutique
        shopItems.add(ShopItem(1, "Skin Rouge", 200))
        shopItems.add(ShopItem(2, "Skin Bleu", 300))
        shopItems.add(ShopItem(3, "Skin Vert", 500))

        // Setup RecyclerView
        val adapter = ShopAdapter(shopItems) { item ->
            onShopItemClick(item)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Bouton retour
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    // Gestion achat
    private fun onShopItemClick(item: ShopItem) {
        if (item.isPurchased) {
            Toast.makeText(this, "${item.name} est déjà acheté !", Toast.LENGTH_SHORT).show()
            return
        }

        if (userCoins >= item.price) {
            userCoins -= item.price
            item.isPurchased = true
            binding.recyclerView.adapter?.notifyDataSetChanged()
            Toast.makeText(this, "Vous avez acheté ${item.name} !", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Pas assez de coins !", Toast.LENGTH_SHORT).show()
        }
    }
}