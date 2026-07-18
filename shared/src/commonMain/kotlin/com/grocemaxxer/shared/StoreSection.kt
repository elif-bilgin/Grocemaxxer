package com.grocemaxxer.shared

/**
 * A section (aisle/department) of a typical supermarket, ordered by [order]
 * to match a low-backtracking walking path through the store: floral and
 * produce near the entrance, bakery, perimeter departments (deli, meat,
 * dairy), then frozen, then the center aisles, then personal care /
 * pharmacy / household, ending at checkout-adjacent items.
 */
enum class StoreSection(val displayName: String, val order: Int) {
    FLORAL("Flowers & Plants", 0),
    PRODUCE("Produce", 1),
    BAKERY("Bakery", 2),
    DELI("Deli", 3),
    MEAT_SEAFOOD("Meat & Seafood", 4),
    DAIRY_EGGS("Dairy & Eggs", 5),
    FROZEN("Frozen", 6),
    PANTRY_CANNED("Pantry & Canned Goods", 7),
    BREAKFAST_CEREAL("Breakfast & Cereal", 8),
    BAKING("Baking Supplies", 9),
    SNACKS("Snacks & Candy", 10),
    BEVERAGES("Beverages", 11),
    CONDIMENTS_SAUCES("Condiments & Sauces", 12),
    INTERNATIONAL("International Foods", 13),
    PERSONAL_CARE("Personal Care & Beauty", 14),
    PHARMACY("Pharmacy & Wellness", 15),
    HOUSEHOLD("Household & Cleaning", 16),
    PET("Pet Supplies", 17),
    OTHER("Other", 18),
}
