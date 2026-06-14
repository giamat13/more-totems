package com.more_totems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Throwing Spear (issue #7) — a dedicated item crafted from a regular spear + an
 * arrow. Right-click to hurl it like a trident: tier-independent hitscan damage,
 * and the spear lands where it strikes so you can pick it back up.
 */
public class ThrowingSpearItem extends Item {

    private static final double RANGE = 32.0;
    private static final float DAMAGE = 9.0f;

    public ThrowingSpearItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemStack stack = player.getItemInHand(hand);

            HitResult hit = RangedUtils.raycast(player, RANGE);
            Vec3 landing = hit.getLocation();
            if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity target) {
                RangedUtils.dealDamage(serverLevel, player, target, DAMAGE);
                landing = target.position();
            }

            if (!player.getAbilities().instabuild) {
                ItemStack thrown = stack.copyWithCount(1);
                stack.shrink(1);
                ItemEntity drop = new ItemEntity(level, landing.x, landing.y + 0.25, landing.z, thrown);
                drop.setPickUpDelay(10);
                level.addFreshEntity(drop);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }
}
