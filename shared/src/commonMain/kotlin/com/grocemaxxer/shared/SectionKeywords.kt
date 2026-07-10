package com.grocemaxxer.shared

/**
 * Heuristic keyword map used to guess which [StoreSection] a freeform item
 * name belongs to. This stands in for "likelihood of being colocated in the
 * store": items that share a keyword bucket are assumed to live in the same
 * aisle/department. Keys are singular, lowercase words or phrases; single
 * words are matched against normalized (singularized) tokens of the item
 * name, phrases are matched as whole substrings with word boundaries.
 *
 * This is intentionally a static, editable table rather than real
 * point-of-sale layout data -- extend it with more keywords/sections as
 * needed for a given store.
 */
internal val sectionKeywords: Map<StoreSection, List<String>> = mapOf(
    StoreSection.PRODUCE to listOf(
        "apple", "banana", "orange", "grape", "strawberry", "blueberry", "raspberry",
        "blackberry", "melon", "watermelon", "cantaloupe", "pineapple", "mango", "peach",
        "pear", "plum", "cherry", "lemon", "lime", "avocado", "kiwi", "fig", "date",
        "lettuce", "spinach", "kale", "arugula", "cabbage", "carrot", "celery", "cucumber",
        "tomato", "potato", "sweet potato", "yam", "onion", "shallot", "garlic", "ginger",
        "bell pepper", "jalapeno", "broccoli", "cauliflower", "zucchini", "squash",
        "pumpkin", "mushroom", "corn", "green bean", "asparagus", "cilantro", "parsley",
        "basil", "mint", "rosemary", "thyme", "scallion", "green onion", "radish", "beet",
        "brussels sprout", "artichoke", "leek", "okra", "turnip", "eggplant", "produce",
        "fruit", "vegetable", "salad mix", "coleslaw mix", "fresh herbs",
    ),
    StoreSection.BAKERY to listOf(
        "bread", "baguette", "bagel", "croissant", "muffin", "danish", "donut", "doughnut",
        "dinner roll", "bun", "hamburger bun", "hot dog bun", "tortilla", "pita", "naan",
        "cake", "pie", "cupcake", "sourdough", "rye bread", "brioche", "loaf",
    ),
    StoreSection.DELI to listOf(
        "deli meat", "ham", "turkey slices", "sliced turkey", "salami", "prosciutto",
        "pepperoni", "sliced cheese", "hummus", "rotisserie chicken", "olives",
        "pre-made sandwich", "potato salad", "macaroni salad", "deli",
    ),
    StoreSection.MEAT_SEAFOOD to listOf(
        "chicken", "chicken breast", "chicken thigh", "chicken wing", "ground beef",
        "beef", "steak", "ribeye", "brisket", "pork", "pork chop", "bacon", "sausage",
        "bratwurst", "lamb", "ground turkey", "meat", "fish", "salmon", "tuna steak",
        "shrimp", "prawn", "crab", "lobster", "tilapia", "cod", "halibut", "scallop",
        "mussel", "oyster", "seafood",
    ),
    StoreSection.DAIRY_EGGS to listOf(
        "milk", "egg", "cheese", "cheddar", "mozzarella", "parmesan", "feta", "brie",
        "butter", "margarine", "yogurt", "yoghurt", "cream", "sour cream", "cream cheese",
        "cottage cheese", "ricotta", "half and half", "whipped cream", "almond milk",
        "oat milk", "soy milk", "creamer",
    ),
    StoreSection.FROZEN to listOf(
        "frozen", "ice cream", "popsicle", "frozen pizza", "frozen vegetable",
        "frozen fruit", "frozen fries", "french fries", "waffle", "frozen waffle",
        "frozen dinner", "frozen meal", "gelato", "sorbet", "frozen berries",
        "ice cube", "hash brown", "dumpling", "frozen shrimp",
    ),
    StoreSection.PANTRY_CANNED to listOf(
        "rice", "pasta", "noodle", "spaghetti", "macaroni", "canned bean", "bean",
        "lentil", "chickpea", "canned tomato", "tomato sauce", "tomato paste", "broth",
        "stock", "soup", "canned soup", "canned corn", "canned tuna", "canned fruit",
        "peanut butter", "almond butter", "jelly", "jam", "honey", "cooking oil",
        "olive oil", "vegetable oil", "vinegar", "quinoa", "couscous", "breadcrumb",
        "canned vegetable", "applesauce",
    ),
    StoreSection.BREAKFAST_CEREAL to listOf(
        "cereal", "oatmeal", "granola", "pancake mix", "waffle mix", "syrup",
        "maple syrup", "pop tart", "breakfast bar", "instant oats",
    ),
    StoreSection.BAKING to listOf(
        "flour", "sugar", "brown sugar", "powdered sugar", "baking soda", "baking powder",
        "yeast", "vanilla extract", "chocolate chip", "cocoa powder", "cake mix",
        "frosting", "cornstarch", "food coloring", "sprinkles",
    ),
    StoreSection.SNACKS to listOf(
        "chip", "cracker", "pretzel", "popcorn", "cookie", "candy", "chocolate bar",
        "chocolate", "granola bar", "nuts", "almonds", "cashew", "peanuts", "trail mix",
        "jerky", "pudding", "fruit snack", "gum", "tortilla chip",
    ),
    StoreSection.BEVERAGES to listOf(
        "water", "sparkling water", "soda", "pop", "juice", "orange juice", "apple juice",
        "coffee", "tea", "beer", "wine", "sports drink", "energy drink", "lemonade",
        "seltzer", "kombucha", "cola",
    ),
    StoreSection.CONDIMENTS_SAUCES to listOf(
        "ketchup", "mustard", "mayonnaise", "mayo", "salsa", "soy sauce", "hot sauce",
        "bbq sauce", "barbecue sauce", "salad dressing", "ranch", "pickle", "relish",
        "sriracha", "marinara", "pasta sauce", "gravy", "worcestershire",
    ),
    StoreSection.INTERNATIONAL to listOf(
        "curry", "taco shell", "coconut milk", "sushi rice", "kimchi",
        "sesame oil", "rice vinegar", "hoisin", "miso", "tahini", "wasabi", "adobo",
        "enchilada sauce", "fish sauce",
    ),
    StoreSection.HEALTH_BEAUTY to listOf(
        "shampoo", "conditioner", "soap", "body wash", "toothpaste", "toothbrush",
        "deodorant", "lotion", "sunscreen", "vitamin", "medicine", "pain reliever",
        "band-aid", "razor", "cotton swab", "floss",
    ),
    StoreSection.HOUSEHOLD to listOf(
        "paper towel", "toilet paper", "tissue", "napkin", "dish soap", "dishwasher pod",
        "laundry detergent", "fabric softener", "trash bag", "aluminum foil",
        "plastic wrap", "sponge", "cleaning spray", "disinfectant wipe", "batteries",
        "light bulb", "candle", "air freshener",
    ),
    StoreSection.PET to listOf(
        "dog food", "cat food", "pet treat", "cat litter", "dog treat", "pet toy",
        "cat toy", "dog bone",
    ),
)
