package com.more_totems;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryStorage {

    public static final EquipmentSlot[] EQUIPMENT_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.OFFHAND
    };

    public static final Map<UUID, List<ItemStack>> SAVED = new HashMap<>();

    public static void restore(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        List<ItemStack> saved = SAVED.remove(oldPlayer.getUUID());
        if (saved == null) return;
        Inventory inv = newPlayer.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            inv.setItem(i, saved.get(i));
        }
        for (int i = 0; i < EQUIPMENT_SLOTS.length; i++) {
            newPlayer.setItemSlot(EQUIPMENT_SLOTS[i], saved.get(size + i));
        }
    }
}
