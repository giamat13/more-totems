package com.more_totems.mixin;

import com.more_totems.InventoryStorage;
import com.more_totems.ModItems;
import com.more_totems.TotemNoFallPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class FallDamageTotemMixin {

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void onCauseFallDamage(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) return;
        // Only consume a charge for falls that would actually deal damage
        if (Math.ceil((fallDistance - 3.0f) * multiplier) <= 0) return;

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModItems.TOTEM_OF_NO_FALL)) {
                useNoFallCharge(stack, player, false, i, null);
                ServerPlayNetworking.send(player, TotemNoFallPayload.INSTANCE);
                cir.setReturnValue(false);
                return;
            }
        }
        for (EquipmentSlot slot : InventoryStorage.EQUIPMENT_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.is(ModItems.TOTEM_OF_NO_FALL)) {
                useNoFallCharge(stack, player, true, -1, slot);
                ServerPlayNetworking.send(player, TotemNoFallPayload.INSTANCE);
                cir.setReturnValue(false);
                return;
            }
        }
    }

    private void useNoFallCharge(ItemStack stack, ServerPlayer player, boolean isEquipSlot, int slotIndex, EquipmentSlot equipSlot) {
        int newDamage = stack.getDamageValue() + 1;
        if (newDamage >= stack.getMaxDamage()) {
            if (isEquipSlot) {
                player.setItemSlot(equipSlot, ItemStack.EMPTY);
            } else {
                player.getInventory().setItem(slotIndex, ItemStack.EMPTY);
            }
        } else {
            stack.setDamageValue(newDamage);
        }
    }
}
