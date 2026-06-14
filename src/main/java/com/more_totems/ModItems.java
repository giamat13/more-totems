package com.more_totems;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

public class ModItems {

    // 10-use durability totems
    public static final Item TOTEM_OF_KEEP_INVENTORY        = register("totem_of_keep_inventory", 10);
    public static final Item TOTEM_OF_NO_FALL               = register("totem_of_no_fall", 10);
    public static final Item TOTEM_OF_ENCHANT               = register("totem_of_enchant", 10);

    // Single-use shockwave (no durability bar — consumed in one activation)
    public static final Item TOTEM_OF_SHOCKWAVE             = register("totem_of_shockwave", 0);

    // Cursed totem: kills its holder the instant it is held in hand.
    public static final Item TOTEM_OF_DYING                 = register("totem_of_dying", 0);

    // Iron-sword upgraded variants (activate on lethal hit, before death)
    public static final Item TOTEM_OF_SHOCKWAVE_IRON        = register("totem_of_shockwave_iron", 10);
    public static final Item TOTEM_OF_NO_FALL_IRON          = register("totem_of_no_fall_iron", 10);
    public static final Item TOTEM_OF_ENCHANT_IRON          = register("totem_of_enchant_iron", 10);

    // Gadgets
    public static final Item GRAPPLE_HOOK   = register("grapple_hook", GrappleHookItem::new, 64);
    public static final Item ORE_SHOOTER    = register("ore_shooter", OreShooterItem::new, 256);
    public static final Item THROWING_SPEAR = register("throwing_spear", ThrowingSpearItem::new, 0);

    private static Item register(String name, int durability) {
        return register(name, Item::new, durability);
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, int durability) {
        var id  = Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, name);
        var key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        Item.Properties props = new Item.Properties().setId(key).rarity(Rarity.UNCOMMON);
        props = durability > 0 ? props.durability(durability) : props.stacksTo(1);
        return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(props));
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
            output.insertAfter(Items.TOTEM_OF_UNDYING,
                    TOTEM_OF_KEEP_INVENTORY,
                    TOTEM_OF_SHOCKWAVE,
                    TOTEM_OF_NO_FALL,
                    TOTEM_OF_ENCHANT,
                    TOTEM_OF_DYING,
                    TOTEM_OF_SHOCKWAVE_IRON,
                    TOTEM_OF_NO_FALL_IRON,
                    TOTEM_OF_ENCHANT_IRON,
                    GRAPPLE_HOOK,
                    ORE_SHOOTER,
                    THROWING_SPEAR
            );
        });
    }
}
