package com.example.flappycoin.models

/**
 * Modèle pour items de la boutique
 * Skins, powerups, packs pièces
 */
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,        // Prix en coins
    val owned: Boolean,
    val category: ItemCategory = ItemCategory.COSMETIC
) {
    enum class ItemCategory {
        COSMETIC,      // Skins d'oiseau
        POWERUP,       // Boosters
        CURRENCY       // Packs pièces
    }
}