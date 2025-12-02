package org.alter.plugins.content.items.consumables.food


enum class Food(
    val item: String,
    val heal: Int = 0,
    val overheal: Boolean = false,
    val replacement: Int = -1,
    val tickDelay: Int = 3,
    val comboFood: Boolean = false,
) {
    /**
     * Sea food.
     */
    SHRIMP(item = "item.shrimps", heal = 3),
    SARDINE(item = "item.sardine", heal = 4),
    HERRING(item = "item.herring", heal = 5),
    MACKEREL(item = "item.mackerel", heal = 6),
    TROUT(item = "item.trout", heal = 7),
    COD(item = "item.cod", heal = 7),
    PIKE(item = "item.pike", heal = 8),
    SALMON(item = "item.salmon", heal = 9),
    TUNA(item = "item.tuna", heal = 10),
    RAINBOW(item = "item.rainbow_fish", heal = 11),
    CAVEEEL(item = "item.cave_eel", heal = 9),
    LOBSTER(item = "item.lobster", heal = 12),
    BASS(item = "item.bass", heal = 13),
    SWORDFISH(item = "item.swordfish", heal = 14),
    MONKFISH(item = "item.monkfish", heal = 16),
    KARAMBWAN(item = "item.cooked_karambwan", heal = 18, comboFood = true),
    SHARK(item = "item.shark", heal = 20),
    SEATURTLE(item = "item.sea_turtle", heal = 21),
    MANTA_RAY(item = "item.manta_ray", heal = 21),
    DARK_CRAB(item = "item.dark_crab", heal = 22),
    ANGLERFISH(item = "item.anglerfish", overheal = true),

    /**
     * Meat.
     */
    CHICKEN(item = "item.cooked_chicken", heal = 4),
    MEAT(item = "item.cooked_meat", heal = 4),
    ROASTBEASTMEAT(item = "item.roast_beast_meat", heal = 8),
    KEBAB(item = "item.ugthanki_kebab", heal = 19),

    /**
     * Pastries & Baked Goods.
     */
    BREAD(item = "item.bread", heal = 5),
    CAKE(item = "item.cake", heal = 4),
    CHOCOLATE_CAKE(item = "item.chocolate_cake", heal = 5),
    MEAT_PIE(item = "item.meat_pie", heal = 6),
    APPLE_PIE(item = "item.apple_pie", heal = 7),
    PIZZA(item = "item.plain_pizza", heal = 7),
    MEAT_PIZZA(item = "item.meat_pizza", heal = 8),
    ANCHOVY_PIZZA(item = "item.anchovy_pizza", heal = 9),
    PINEAPPLE_PIZZA(item = "item.pineapple_pizza", heal = 11),
    
    /**
     * Vegetables & Fruits.
     */
    POTATO(item = "item.potato", heal = 1),
    BAKED_POTATO(item = "item.baked_potato", heal = 4),
    POTATO_WITH_BUTTER(item = "item.potato_with_butter", heal = 14),
    POTATO_WITH_CHEESE(item = "item.potato_with_cheese", heal = 16),
    CABBAGE(item = "item.cabbage", heal = 1),
    ONION(item = "item.onion", heal = 1),
    BANANA(item = "item.banana", heal = 2),
    STRAWBERRY(item = "item.strawberry", heal = 6),
    WATERMELON(item = "item.watermelon_slice", heal = 5),
    PINEAPPLE_CHUNKS(item = "item.pineapple_chunks", heal = 2),
    PINEAPPLE_RING(item = "item.pineapple_ring", heal = 2),
    
    /**
     * Stews & Soups.
     */
    STEW(item = "item.stew", heal = 11),
    CURRY(item = "item.curry", heal = 19),
    
    /**
     * Other Foods.
     */
    EGG(item = "item.egg", heal = 3),
    CHEESE(item = "item.cheese", heal = 2),
    TOMATO(item = "item.tomato", heal = 2),
    SWEETCORN(item = "item.sweetcorn", heal = 10),
    ;

    companion object {
        val values = enumValues<Food>()
    }
}
