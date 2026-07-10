package com.grocemaxxer.shared

/**
 * A section (aisle/department) of a typical supermarket, ordered by [order]
 * to match a low-backtracking walking path through the store: produce and
 * bakery near the entrance, perimeter departments (deli, meat, dairy),
 * then frozen, then the center aisles, then health/household, ending at
 * checkout-adjacent items.
 */
enum class StoreSection(val displayName: String, val order: Int) {
    PRODUCE("Produce", 0),
    BAKERY("Bakery", 1),
    DELI("Deli", 2),
    MEAT_SEAFOOD("Meat & Seafood", 3),
    DAIRY_EGGS("Dairy & Eggs", 4),
    FROZEN("Frozen", 5),
    PANTRY_CANNED("Pantry & Canned Goods", 6),
    BREAKFAST_CEREAL("Breakfast & Cereal", 7),
    BAKING("Baking Supplies", 8),
    SNACKS("Snacks & Candy", 9),
    BEVERAGES("Beverages", 10),
    CONDIMENTS_SAUCES("Condiments & Sauces", 11),
    INTERNATIONAL("International Foods", 12),
    HEALTH_BEAUTY("Health & Beauty", 13),
    HOUSEHOLD("Household & Cleaning", 14),
    PET("Pet Supplies", 15),
    OTHER("Other", 16),
}
