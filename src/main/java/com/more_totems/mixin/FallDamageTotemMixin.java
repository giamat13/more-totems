package com.more_totems.mixin;

import com.more_totems.InventoryStorage;
import com.more_totems.ModItems;
import com.more_totems.TotemNoFallPayload;
import com.more_totems.TotemUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class FallDamageTotemMixin {

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void onCauseFallDamage(float fallDistance, float multiplier, DamageSource source,
                                   CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) return;
        if (Math.ceil((fallDistance - 3.0f) * multiplier) <= 0) return;

        if (TotemUtils.findAndDamageTotem(player, ModItems.TOTEM_OF_NO_FALL)) {
            ServerPlayNetworking.send(player, TotemNoFallPayload.INSTANCE);
            cir.setReturnValue(false);
        }
    }
}
