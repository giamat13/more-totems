package com.more_totems;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TotemUtils {

    /**
     * Whether totems should be searched across the whole inventory.  Controlled by
     * the {@code totemsWorkInInventory} game rule; when off (the default) totems
     * only count while held in a hand.
     */
    private static boolean inventoryEnabled(ServerPlayer player) {
        return player.level().getGameRules().getBoolean(MoreTotems.TOTEMS_WORK_IN_INVENTORY);
    }

    /** Find the first totem of the given type the player can currently use. */
    public static boolean hasTotem(ServerPlayer player, Item item) {
        if (!inventoryEnabled(player)) {
            return player.getMainHandItem().is(item) || player.getOffhandItem().is(item);
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) return true;
        }
        for (EquipmentSlot slot : InventoryStorage.EQUIPMENT_SLOTS) {
            if (player.getItemBySlot(slot).is(item)) return true;
        }
        return false;
    }

    /**
     * Find the first totem of the given type, consume one charge (or break it if
     * this was the last charge), and return true.  Returns false if not found.
     */
    public static boolean findAndDamageTotem(ServerPlayer player, Item item) {
        if (!inventoryEnabled(player)) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.is(item)) {
                    damageOrBreakHand(stack, player, hand);
                    return true;
                }
            }
            return false;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(item)) {
                damageOrBreak(stack, player, false, i, null);
                return true;
            }
        }
        for (EquipmentSlot slot : InventoryStorage.EQUIPMENT_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.is(item)) {
                damageOrBreak(stack, player, true, -1, slot);
                return true;
            }
        }
        return false;
    }

    private static void damageOrBreakHand(ItemStack stack, ServerPlayer player, InteractionHand hand) {
        int newDamage = stack.getDamageValue() + 1;
        if (newDamage >= stack.getMaxDamage()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        } else {
            stack.setDamageValue(newDamage);
        }
    }

    private static void damageOrBreak(ItemStack stack, ServerPlayer player,
                                      boolean isEquip, int slotIdx, EquipmentSlot equipSlot) {
        int newDamage = stack.getDamageValue() + 1;
        if (newDamage >= stack.getMaxDamage()) {
            if (isEquip) player.setItemSlot(equipSlot, ItemStack.EMPTY);
            else player.getInventory().setItem(slotIdx, ItemStack.EMPTY);
        } else {
            stack.setDamageValue(newDamage);
        }
    }
}
