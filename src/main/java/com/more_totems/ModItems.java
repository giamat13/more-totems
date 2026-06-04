package com.more_totems;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

public class ModItems {

    public static final Item TOTEM_OF_KEEP_INVENTORY = register("totem_of_keep_inventory", 10);
    public static final Item TOTEM_OF_SHOCKWAVE = register("totem_of_shockwave", 0);
    public static final Item TOTEM_OF_NO_FALL = register("totem_of_no_fall", 10);

    private static Item register(String name, int durability) {
        var id = Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, name);
        var key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        Item.Properties props = new Item.Properties().setId(key).rarity(Rarity.UNCOMMON);
        props = durability > 0 ? props.durability(durability) : props.stacksTo(1);
        return Registry.register(BuiltInRegistries.ITEM, id, new Item(props));
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.addAfter(Items.TOTEM_OF_UNDYING, TOTEM_OF_KEEP_INVENTORY);
            entries.addAfter(TOTEM_OF_KEEP_INVENTORY, TOTEM_OF_SHOCKWAVE);
            entries.addAfter(TOTEM_OF_SHOCKWAVE, TOTEM_OF_NO_FALL);
        });
    }
}
