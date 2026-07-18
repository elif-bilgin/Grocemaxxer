package com.grocemaxxer.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class ItemCategorizerTest {

    @Test
    fun categorizesCommonItemsIntoExpectedSections() {
        val cases = mapOf(
            "apple" to StoreSection.PRODUCE,
            "Apples" to StoreSection.PRODUCE,
            "spinach" to StoreSection.PRODUCE,
            "sourdough bread" to StoreSection.BAKERY,
            "bagels" to StoreSection.BAKERY,
            "rotisserie chicken" to StoreSection.DELI,
            "chicken breast" to StoreSection.MEAT_SEAFOOD,
            "ground beef" to StoreSection.MEAT_SEAFOOD,
            "salmon" to StoreSection.MEAT_SEAFOOD,
            "milk" to StoreSection.DAIRY_EGGS,
            "eggs" to StoreSection.DAIRY_EGGS,
            "cheddar cheese" to StoreSection.DAIRY_EGGS,
            "frozen pizza" to StoreSection.FROZEN,
            "ice cream" to StoreSection.FROZEN,
            "black beans" to StoreSection.PANTRY_CANNED,
            "olive oil" to StoreSection.PANTRY_CANNED,
            "cereal" to StoreSection.BREAKFAST_CEREAL,
            "flour" to StoreSection.BAKING,
            "tortilla chips" to StoreSection.SNACKS,
            "sparkling water" to StoreSection.BEVERAGES,
            "ketchup" to StoreSection.CONDIMENTS_SAUCES,
            "curry paste" to StoreSection.INTERNATIONAL,
            "shampoo" to StoreSection.PERSONAL_CARE,
            "mascara" to StoreSection.PERSONAL_CARE,
            "vitamins" to StoreSection.PHARMACY,
            "ibuprofen" to StoreSection.PHARMACY,
            "paper towels" to StoreSection.HOUSEHOLD,
            "garbage bags" to StoreSection.HOUSEHOLD,
            "dog food" to StoreSection.PET,
            "cat litter" to StoreSection.PET,
            "flowers" to StoreSection.FLORAL,
            "tulips" to StoreSection.FLORAL,
        )

        for ((input, expected) in cases) {
            assertEquals(expected, ItemCategorizer.categorize(input), "mismatched category for '$input'")
        }
    }

    @Test
    fun avoidsFalsePositiveSubstringMatches() {
        // "eggplant" must not match the "egg" keyword just because it contains that substring.
        assertEquals(StoreSection.PRODUCE, ItemCategorizer.categorize("eggplant"))
    }

    @Test
    fun handlesPluralsViaSingularization() {
        assertEquals(StoreSection.PRODUCE, ItemCategorizer.categorize("tomatoes"))
        assertEquals(StoreSection.PRODUCE, ItemCategorizer.categorize("carrots"))
        assertEquals(StoreSection.DAIRY_EGGS, ItemCategorizer.categorize("eggs"))
        assertEquals(StoreSection.SNACKS, ItemCategorizer.categorize("chips"))
    }

    @Test
    fun prefersMoreSpecificMultiWordMatch() {
        // "sweet potato" is Produce, not just "potato" -- both map to Produce here,
        // but this guards against a more specific phrase losing to a shorter one.
        assertEquals(StoreSection.PRODUCE, ItemCategorizer.categorize("sweet potato"))
        assertEquals(StoreSection.PANTRY_CANNED, ItemCategorizer.categorize("peanut butter"))
    }

    @Test
    fun unknownItemFallsBackToOther() {
        assertEquals(StoreSection.OTHER, ItemCategorizer.categorize("quantum flux capacitor"))
    }

    @Test
    fun blankInputFallsBackToOther() {
        assertEquals(StoreSection.OTHER, ItemCategorizer.categorize("   "))
    }
}
