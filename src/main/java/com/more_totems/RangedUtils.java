package com.more_totems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Small shared helpers for the hitscan-style ranged items (Ore Shooter, thrown
 * spears).  Centralised so the few version-sensitive calls (entity damage in
 * particular) live in one place.
 */
public final class RangedUtils {

    private RangedUtils() {}

    /**
     * Trace the player's line of sight up to {@code range} blocks and return the
     * first thing hit — a living entity if one is in the way, otherwise the block
     * (or a MISS result at the end of the ray).
     */
    public static HitResult raycast(Player player, double range) {
        Level level = player.level();
        Vec3 start = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0f);
        Vec3 end = start.add(dir.scale(range));

        BlockHitResult blockHit = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : end;

        AABB box = player.getBoundingBox().expandTowards(dir.scale(range)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, player, start, limit, box,
                e -> e instanceof LivingEntity && e != player && e.isAlive() && e.isPickable());

        return entityHit != null ? entityHit : blockHit;
    }

    /** Apply damage from {@code shooter} to {@code target}. */
    public static void dealDamage(ServerLevel level, Player shooter, LivingEntity target, float amount) {
        target.hurtServer(level, shooter.damageSources().playerAttack(shooter), amount);
    }

    /** True for the player's currently-aimed living target, if any, via {@link #raycast}. */
    public static LivingEntity targetedLiving(Player player, double range) {
        HitResult hit = raycast(player, range);
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    /** Damage a damageable item by one point, shrinking the stack if it breaks. */
    public static void damageItem(net.minecraft.world.item.ItemStack stack, Player player) {
        if (!stack.isDamageableItem()) return;
        int next = stack.getDamageValue() + 1;
        if (next >= stack.getMaxDamage()) {
            stack.shrink(1);
        } else {
            stack.setDamageValue(next);
        }
    }
}
