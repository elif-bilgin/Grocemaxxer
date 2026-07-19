package com.grocemaxxer.shared

/**
 * Supported stores, each with its own "speed-run" section ordering.
 *
 * DEFAULT keeps the generic low-backtracking walk (entrance -> perimeter ->
 * center aisles). The named stores follow their real-world layouts and the
 * cart-packing rules of thumb: heavy dry goods first, produce on top of
 * them, proteins in the second half of the trip, frozen right before
 * checkout, delicate bakery items last.
 */
enum class Store(val displayName: String, val emoji: String) {
    DEFAULT("No Store Selected", "🍎"),
    SAFEWAY("Safeway", "🛒"),
    WHOLE_FOODS("Whole Foods", "🥑"),
    TARGET("Target", "🎯"),
    TRADER_JOES("Trader Joe's", "🌺"),
    ;

    companion object {
        fun fromName(name: String?): Store = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

private val centerAisles = listOf(
    StoreSection.PANTRY_CANNED,
    StoreSection.CONDIMENTS_SAUCES,
    StoreSection.BREAKFAST_CEREAL,
    StoreSection.BAKING,
    StoreSection.INTERNATIONAL,
    StoreSection.SNACKS,
    StoreSection.BEVERAGES,
)

private val careAndHome = listOf(
    StoreSection.PERSONAL_CARE,
    StoreSection.PHARMACY,
    StoreSection.HOUSEHOLD,
    StoreSection.PET,
)

private val storeOrders: Map<Store, List<StoreSection>> = mapOf(
    // Generic store: floral/produce at the entrance, around the perimeter,
    // then center aisles, ending near checkout.
    Store.DEFAULT to StoreSection.entries.sortedBy { it.order },

    // Safeway: heavy center aisles first, produce on top of them, proteins
    // in the second half, dairy, frozen last-ish, squishable bakery/deli at
    // the very end.
    Store.SAFEWAY to centerAisles + careAndHome + listOf(
        StoreSection.PRODUCE,
        StoreSection.FLORAL,
        StoreSection.MEAT_SEAFOOD,
        StoreSection.DAIRY_EGGS,
        StoreSection.FROZEN,
        StoreSection.BAKERY,
        StoreSection.DELI,
        StoreSection.OTHER,
    ),

    // Whole Foods: bypass the showpiece produce, center aisles/bulk first,
    // loop back for produce, then the perimeter counters, prepared foods
    // and bakery on the way to checkout.
    Store.WHOLE_FOODS to centerAisles + careAndHome + listOf(
        StoreSection.PRODUCE,
        StoreSection.FLORAL,
        StoreSection.MEAT_SEAFOOD,
        StoreSection.DAIRY_EGGS,
        StoreSection.FROZEN,
        StoreSection.DELI,
        StoreSection.BAKERY,
        StoreSection.OTHER,
    ),

    // Target: dry grocery center first, fresh section (produce/bakery/deli)
    // second, outer-wall meat and dairy, then weave back into the center
    // for frozen right before checkout.
    Store.TARGET to centerAisles + careAndHome + listOf(
        StoreSection.PRODUCE,
        StoreSection.BAKERY,
        StoreSection.FLORAL,
        StoreSection.DELI,
        StoreSection.MEAT_SEAFOOD,
        StoreSection.DAIRY_EGGS,
        StoreSection.FROZEN,
        StoreSection.OTHER,
    ),

    // Trader Joe's: small and crowded -- go with the flow. Produce/flowers
    // at the entrance first, dry center aisles, refrigerated wall sweep,
    // frozen bunkers feeding into the checkout line.
    Store.TRADER_JOES to listOf(StoreSection.PRODUCE, StoreSection.FLORAL) +
        centerAisles + careAndHome + listOf(
        StoreSection.BAKERY,
        StoreSection.MEAT_SEAFOOD,
        StoreSection.DELI,
        StoreSection.DAIRY_EGGS,
        StoreSection.FROZEN,
        StoreSection.OTHER,
    ),
)

/** Position of [section] in [store]'s speed-run; unknown sections go last. */
fun sectionRank(store: Store, section: StoreSection): Int {
    val order = storeOrders.getValue(store)
    val index = order.indexOf(section)
    return if (index >= 0) index else order.size
}
