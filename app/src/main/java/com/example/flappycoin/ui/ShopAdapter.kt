package com.example.flappycoin.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flappycoin.databinding.ItemShopBinding
import com.example.flappycoin.models.ShopItem

class ShopAdapter(
    private val items: List<ShopItem>,
    private val onItemClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemShopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShopItem) {
            binding.tvName.text = item.name
            binding.tvDescription.text = item.description

            if (item.owned) {
                binding.tvPrice.text = "✓ Possédé"
                binding.btnBuy.isEnabled = false
                binding.btnBuy.text = "Possédé"
            } else {
                binding.tvPrice.text = "🪙 ${item.price}"
                binding.btnBuy.isEnabled = true
                binding.btnBuy.text = "Acheter"
            }

            binding.btnBuy.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}