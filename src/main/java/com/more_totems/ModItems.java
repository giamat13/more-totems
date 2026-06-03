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

    public static final Item TOTEM_OF_KEEP_INVENTORY = register("totem_of_keep_inventory");

    private static Item register(String name) {
        var id = Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, name);
        var key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        var item = new Item(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.UNCOMMON));
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries ->
                entries.addAfter(Items.TOTEM_OF_UNDYING, TOTEM_OF_KEEP_INVENTORY)
        );
    }
}
