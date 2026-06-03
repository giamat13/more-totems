package com.more_totems.mixin;

import com.more_totems.InventoryStorage;
import com.more_totems.ModItems;
import com.more_totems.TotemActivatedPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public class PlayerKeepInventoryMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onDie(DamageSource source, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (findAndRemoveTotem(player)) {
            InventoryStorage.SAVED.put(player.getUUID(), saveInventory(player));
            clearInventory(player);
            ServerPlayNetworking.send(player, TotemActivatedPayload.INSTANCE);
        }
    }

    private boolean findAndRemoveTotem(ServerPlayer player) {
        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i).is(ModItems.TOTEM_OF_KEEP_INVENTORY)) {
                inv.setItem(i, ItemStack.EMPTY);
                return true;
            }
        }
        for (EquipmentSlot slot : InventoryStorage.EQUIPMENT_SLOTS) {
            if (player.getItemBySlot(slot).is(ModItems.TOTEM_OF_KEEP_INVENTORY)) {
                player.setItemSlot(slot, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    private List<ItemStack> saveInventory(ServerPlayer player) {
        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        List<ItemStack> saved = new ArrayList<>(size + InventoryStorage.EQUIPMENT_SLOTS.length);
        for (int i = 0; i < size; i++) {
            saved.add(inv.getItem(i).copy());
        }
        for (EquipmentSlot slot : InventoryStorage.EQUIPMENT_SLOTS) {
            saved.add(player.getItemBySlot(slot).copy());
        }
        return saved;
    }

    private void clearInventory(ServerPlayer player) {
        Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            inv.setItem(i, ItemStack.EMPTY);
        }
        for (EquipmentSlot slot : InventoryStorage.EQUIPMENT_SLOTS) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
