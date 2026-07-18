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
    StoreSection.FLORAL to listOf(
        "flower", "bouquet", "rose", "tulip", "lily", "orchid", "daisy", "sunflower",
        "carnation", "peony", "succulent", "houseplant", "potted plant", "vase",
    ),
    StoreSection.PRODUCE to listOf(
        // fruits
        "apple", "banana", "orange", "grape", "strawberry", "blueberry", "raspberry",
        "blackberry", "cranberry", "currant", "gooseberry", "melon", "watermelon",
        "cantaloupe", "honeydew", "pineapple", "mango", "peach", "nectarine", "apricot",
        "pear", "plum", "pluot", "cherry", "lemon", "lime", "grapefruit", "clementine",
        "mandarin", "tangerine", "blood orange", "avocado", "kiwi", "fig", "date",
        "pomegranate", "papaya", "guava", "dragon fruit", "lychee", "passion fruit",
        "persimmon", "starfruit", "plantain", "rhubarb", "coconut",
        // leafy greens & salad
        "lettuce", "romaine", "iceberg", "butter lettuce", "spring mix", "spinach",
        "kale", "arugula", "watercress", "endive", "radicchio", "chard", "swiss chard",
        "collard greens", "mustard greens", "bok choy", "napa cabbage", "cabbage",
        "microgreens", "sprouts", "bean sprout", "alfalfa",
        // vegetables
        "carrot", "baby carrot", "celery", "celery root", "cucumber", "tomato",
        "cherry tomato", "grape tomato", "roma tomato", "heirloom tomato", "potato",
        "russet potato", "red potato", "fingerling potato", "sweet potato", "yam",
        "onion", "red onion", "shallot", "garlic", "ginger", "turmeric root",
        "bell pepper", "sweet pepper", "banana pepper", "chili pepper", "jalapeno",
        "serrano", "habanero", "poblano", "anaheim", "broccoli", "broccolini",
        "cauliflower", "zucchini", "squash", "butternut squash", "acorn squash",
        "spaghetti squash", "delicata squash", "pumpkin", "mushroom", "portobello",
        "shiitake", "cremini", "corn", "green bean", "snap pea", "snow pea", "edamame",
        "asparagus", "scallion", "green onion", "radish", "daikon", "beet",
        "brussels sprout", "artichoke", "leek", "fennel", "okra", "turnip", "parsnip",
        "rutabaga", "kohlrabi", "jicama", "eggplant", "horseradish",
        // fresh herbs
        "cilantro", "parsley", "basil", "mint", "rosemary", "thyme", "dill", "sage",
        "oregano", "tarragon", "chive", "lemongrass", "fresh herbs",
        // generic
        "produce", "fruit", "vegetable", "salad mix", "coleslaw mix",
    ),
    StoreSection.BAKERY to listOf(
        "bread", "white bread", "wheat bread", "whole wheat bread", "multigrain bread",
        "rye bread", "pumpernickel", "sourdough", "baguette", "ciabatta", "focaccia",
        "brioche", "challah", "loaf", "bagel", "english muffin", "biscuit", "scone",
        "croissant", "muffin", "danish", "strudel", "donut", "doughnut", "cinnamon roll",
        "dinner roll", "kaiser roll", "sub roll", "hoagie roll", "bun", "hamburger bun",
        "hot dog bun", "tortilla", "flour tortilla", "corn tortilla", "pita", "naan",
        "flatbread", "lavash", "cornbread", "banana bread", "pretzel bread",
        "cake", "birthday cake", "sheet cake", "pound cake", "angel food cake",
        "cheesecake", "pie", "tart", "cupcake", "brownie", "eclair", "macaron",
        "macaroon", "pastry",
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
        // milk & eggs
        "milk", "whole milk", "skim milk", "lactose-free milk", "buttermilk",
        "almond milk", "oat milk", "soy milk", "coconut milk beverage", "egg",
        "egg white", "creamer",
        // cheeses
        "cheese", "cheddar", "mozzarella", "parmesan", "feta", "brie", "camembert",
        "gouda", "swiss cheese", "provolone", "havarti", "gruyere", "manchego",
        "asiago", "pecorino", "romano", "gorgonzola", "blue cheese", "burrata",
        "mascarpone", "queso fresco", "cotija", "oaxaca cheese", "halloumi", "paneer",
        "goat cheese", "chevre", "string cheese", "cheese stick", "babybel", "colby",
        "colby jack", "monterey jack", "pepper jack", "muenster", "american cheese",
        "cream cheese", "cottage cheese", "ricotta", "shredded cheese",
        // cultured & cream
        "butter", "margarine", "ghee", "yogurt", "yoghurt", "greek yogurt", "kefir",
        "skyr", "cream", "sour cream", "heavy cream", "whipping cream",
        "half and half", "whipped cream",
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
    StoreSection.PERSONAL_CARE to listOf(
        "shampoo", "conditioner", "soap", "body wash", "toothpaste", "toothbrush",
        "mouthwash", "deodorant", "lotion", "moisturizer", "sunscreen", "razor",
        "shaving cream", "cotton swab", "floss", "makeup", "mascara", "lipstick",
        "eyeliner", "foundation", "nail polish", "face wash", "skincare", "lip balm",
        "chapstick", "hair gel", "hairspray", "tampon", "cosmetics",
    ),
    StoreSection.PHARMACY to listOf(
        "vitamin", "medicine", "pain reliever", "ibuprofen", "acetaminophen", "tylenol",
        "advil", "aspirin", "band-aid", "bandage", "cough drop", "cough syrup",
        "cold medicine", "allergy medicine", "antacid", "supplement", "melatonin",
        "probiotic", "thermometer", "first aid",
    ),
    StoreSection.HOUSEHOLD to listOf(
        "paper towel", "toilet paper", "tissue", "napkin", "dish soap", "dishwasher pod",
        "laundry detergent", "fabric softener", "trash bag", "garbage bag",
        "aluminum foil", "plastic wrap", "parchment paper", "sandwich bag", "ziploc",
        "sponge", "cleaning spray", "all-purpose cleaner", "glass cleaner", "bleach",
        "disinfectant wipe", "mop", "broom", "dryer sheet", "batteries", "light bulb",
        "candle", "air freshener", "cleaning supplies",
    ),
    StoreSection.PET to listOf(
        "dog food", "cat food", "pet food", "pet treat", "cat litter", "litter",
        "kibble", "dog treat", "pet toy", "cat toy", "dog bone", "bird seed",
        "fish food", "pet shampoo",
    ),
)
