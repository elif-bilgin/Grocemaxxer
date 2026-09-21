package com.grocemaxxer.shared

/** A known grocery item that can be suggested to the user, with its aisle. */
data class CatalogItem(val name: String, val section: StoreSection)

/**
 * Curated catalog of common grocery items, categorized by section. Seeds
 * the on-device Room database on first launch. It is deliberately static --
 * items the user types are not added to it, so a one-off typo never becomes
 * a recurring suggestion.
 */
object PresetCatalog {

    val items: List<CatalogItem> = buildList {
        fun s(section: StoreSection, vararg names: String) {
            names.forEach { add(CatalogItem(it, section)) }
        }

        s(
            StoreSection.PRODUCE,
            "Apples", "Bananas", "Strawberries", "Blueberries", "Grapes", "Oranges",
            "Lemons", "Limes", "Avocados", "Tomatoes", "Cherry Tomatoes", "Potatoes",
            "Sweet Potatoes", "Onions", "Red Onions", "Garlic", "Carrots", "Celery",
            "Spinach", "Kale", "Romaine Lettuce", "Spring Mix", "Broccoli",
            "Cauliflower", "Cucumbers", "Zucchini", "Bell Peppers", "Jalapenos",
            "Mushrooms", "Green Beans", "Asparagus", "Corn", "Cilantro", "Basil",
            "Ginger", "Watermelon", "Pineapple", "Mango", "Peaches",
        )
        s(
            StoreSection.BAKERY,
            "Sourdough Bread", "Whole Wheat Bread", "Bagels", "English Muffins",
            "Croissants", "Muffins", "Dinner Rolls", "Hamburger Buns", "Hot Dog Buns",
            "Tortillas", "Pita Bread", "Naan", "Baguette", "Chocolate Chip Cookies",
        )
        s(
            StoreSection.DELI,
            "Sliced Turkey", "Ham", "Salami", "Prosciutto", "Sliced Cheese", "Hummus",
            "Rotisserie Chicken", "Olives", "Potato Salad", "Guacamole",
        )
        s(
            StoreSection.MEAT_SEAFOOD,
            "Chicken Breast", "Chicken Thighs", "Ground Beef", "Ground Turkey",
            "Steak", "Pork Chops", "Bacon", "Sausages", "Salmon", "Shrimp",
            "Tilapia", "Cod",
        )
        s(
            StoreSection.DAIRY_EGGS,
            "Milk", "Whole Milk", "Oat Milk", "Almond Milk", "Eggs", "Butter",
            "Cheddar Cheese", "Mozzarella", "Parmesan", "Feta", "Goat Cheese",
            "String Cheese", "Greek Yogurt", "Yogurt", "Sour Cream", "Cream Cheese",
            "Heavy Cream", "Half And Half", "Cottage Cheese",
        )
        s(
            StoreSection.FROZEN,
            "Ice Cream", "Frozen Pizza", "Frozen Berries", "Frozen Vegetables",
            "Frozen Fries", "Frozen Waffles", "Frozen Dumplings", "Popsicles",
            "Frozen Shrimp",
        )
        s(
            StoreSection.PANTRY_CANNED,
            "Rice", "Pasta", "Spaghetti", "Quinoa", "Black Beans", "Chickpeas",
            "Lentils", "Canned Tomatoes", "Tomato Sauce", "Chicken Broth",
            "Canned Tuna", "Peanut Butter", "Jam", "Honey", "Olive Oil",
            "Vegetable Oil", "Balsamic Vinegar", "Breadcrumbs", "Canned Corn",
            "Chicken Noodle Soup",
        )
        s(
            StoreSection.BREAKFAST_CEREAL,
            "Cereal", "Oatmeal", "Granola", "Pancake Mix", "Maple Syrup",
            "Breakfast Bars", "Instant Oats",
        )
        s(
            StoreSection.BAKING,
            "Flour", "Sugar", "Brown Sugar", "Powdered Sugar", "Baking Soda",
            "Baking Powder", "Yeast", "Vanilla Extract", "Chocolate Chips",
            "Cocoa Powder", "Cake Mix", "Frosting", "Cornstarch", "Condensed Milk",
        )
        s(
            StoreSection.SNACKS,
            "Tortilla Chips", "Potato Chips", "Crackers", "Pretzels", "Popcorn",
            "Cookies", "Granola Bars", "Trail Mix", "Almonds", "Cashews", "Peanuts",
            "Beef Jerky", "Dark Chocolate", "Gummy Bears", "Fruit Snacks",
        )
        s(
            StoreSection.BEVERAGES,
            "Sparkling Water", "Bottled Water", "Orange Juice", "Apple Juice",
            "Ground Coffee", "Tea", "Green Tea", "Soda", "Lemonade", "Kombucha",
            "Sports Drink", "Beer", "Red Wine", "White Wine",
        )
        s(
            StoreSection.CONDIMENTS_SAUCES,
            "Ketchup", "Mustard", "Mayonnaise", "Ranch Dressing", "Salsa",
            "Hot Sauce", "Sriracha", "Soy Sauce", "BBQ Sauce", "Pasta Sauce",
            "Salad Dressing", "Pickles", "Relish",
        )
        s(
            StoreSection.INTERNATIONAL,
            "Curry Paste", "Coconut Milk", "Sushi Rice", "Kimchi", "Sesame Oil",
            "Rice Vinegar", "Miso Paste", "Tahini", "Taco Shells", "Enchilada Sauce",
            "Fish Sauce", "Hoisin Sauce",
        )
        s(
            StoreSection.FLORAL,
            "Flower Bouquet", "Roses", "Tulips", "Sunflowers", "Orchid", "Succulent",
        )
        s(
            StoreSection.PERSONAL_CARE,
            "Shampoo", "Conditioner", "Body Wash", "Bar Soap", "Toothpaste",
            "Toothbrush", "Mouthwash", "Deodorant", "Lotion", "Sunscreen", "Razors",
            "Floss", "Lip Balm", "Cotton Swabs",
        )
        s(
            StoreSection.PHARMACY,
            "Vitamins", "Vitamin D", "Ibuprofen", "Acetaminophen", "Band-Aids",
            "Cough Drops", "Allergy Medicine", "Antacids", "Melatonin",
            "Cold Medicine",
        )
        s(
            StoreSection.HOUSEHOLD,
            "Paper Towels", "Toilet Paper", "Tissues", "Napkins", "Dish Soap",
            "Dishwasher Pods", "Laundry Detergent", "Fabric Softener", "Trash Bags",
            "Aluminum Foil", "Plastic Wrap", "Parchment Paper", "Sandwich Bags",
            "Sponges", "All-Purpose Cleaner", "Glass Cleaner", "Disinfectant Wipes",
            "Batteries", "Light Bulbs", "Candles",
        )
        s(
            StoreSection.PET,
            "Dog Food", "Cat Food", "Dog Treats", "Cat Treats", "Cat Litter",
            "Pet Shampoo", "Bird Seed",
        )
    }
}
