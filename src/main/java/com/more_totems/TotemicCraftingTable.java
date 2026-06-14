package com.more_totems;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Totemic Crafting Table (issue #3): a crafting-table-styled block that opens a
 * crafting menu when right-clicked.  Block registration is wrapped in try/catch
 * so a wrong MC 26 API can't take the whole mod down — it just logs and the
 * block is absent.
 *
 * <p>This is the first pass: it gives a working block + crafting GUI. The fancier
 * parts of the issue (recipe-book totem tab, "any totem + item = totemic item",
 * removing totem recipes from the normal grid) are follow-ups.
 */
public final class TotemicCraftingTable {

    public static Block BLOCK;
    public static Item ITEM;

    private TotemicCraftingTable() {}

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, "totemic_crafting_table");
        try {
            ResourceKey<Block> blockKey = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
            BlockBehaviour.Properties props =
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(blockKey);
            BLOCK = Registry.register(BuiltInRegistries.BLOCK, id, new Block(props));

            ResourceKey<Item> itemKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
            ITEM = Registry.register(BuiltInRegistries.ITEM, id,
                    new BlockItem(BLOCK, new Item.Properties().setId(itemKey)));

            MoreTotems.LOGGER.info("[table] registered totemic crafting table");
        } catch (Throwable t) {
            MoreTotems.LOGGER.error("[table] failed to register totemic crafting table", t);
        }

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            if (ITEM != null) output.accept(ITEM);
        });

        // Right-click the block to open a crafting menu (reuses the vanilla crafting screen).
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (BLOCK == null) return InteractionResult.PASS;
            if (!level.getBlockState(hit.getBlockPos()).is(BLOCK)) return InteractionResult.PASS;
            MoreTotems.LOGGER.info("[table] clicked totemic table, client={} shift={}",
                    level.isClientSide(), player.isShiftKeyDown());
            if (player.isShiftKeyDown()) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                MenuProvider provider = new SimpleMenuProvider(
                        (syncId, inv, p) -> new CraftingMenu(syncId, inv,
                                ContainerLevelAccess.create(level, hit.getBlockPos())),
                        Component.literal("Totemic Crafting Table"));
                player.openMenu(provider);
                MoreTotems.LOGGER.info("[table] openMenu called");
            }
            return InteractionResult.SUCCESS;
        });
    }
}
