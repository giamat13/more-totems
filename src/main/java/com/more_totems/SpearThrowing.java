package com.more_totems;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * Throwable spears (issue #7).  Any vanilla {@code <tier>_spear} can be hurled
 * like a trident: spend one arrow to throw, deal tier-scaled damage to whatever
 * you hit, and the spear lands where it strikes so you can pick it back up.
 *
 * <p>This is a hitscan throw (instant), so it skips the water-only trident
 * behaviour (Riptide / Channeling) entirely, as requested.
 */
public final class SpearThrowing {

    private static final double RANGE = 24.0;

    /** Base throw damage per spear tier, keyed by the item-id suffix. */
    private static final Map<String, Float> TIER_DAMAGE = Map.of(
            "wooden_spear",    4.0f,
            "stone_spear",     5.0f,
            "golden_spear",    4.0f,
            "iron_spear",      6.0f,
            "diamond_spear",   8.0f,
            "netherite_spear", 9.0f);

    private SpearThrowing() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            Float damage = spearDamage(stack);
            if (damage == null) {
                return InteractionResult.PASS;
            }
            boolean creative = player.getAbilities().instabuild;
            boolean hasArrow = hasArrow(player);
            MoreTotems.LOGGER.info("[spear] use {} hand={} creative={} hasArrow={}",
                    BuiltInRegistries.ITEM.getKey(stack.getItem()), hand, creative, hasArrow);
            if (!creative && !hasArrow) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Need an arrow to throw the spear!"),
                            true);
                }
                return InteractionResult.PASS; // need an arrow to throw
            }

            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                if (!creative) consumeArrow(player);

                HitResult hit = RangedUtils.raycast(player, RANGE);
                Vec3 landing = hit.getLocation();
                if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity target) {
                    RangedUtils.dealDamage(serverLevel, player, target, damage);
                    landing = target.position();
                }

                // In survival the thrown spear leaves the hand and drops where it lands,
                // so you can run over and pick it back up (trident-style).
                if (!creative) {
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
        });
    }

    private static Float spearDamage(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return null;
        String path = id.getPath();
        if (!path.contains("spear")) return null;
        // Exact tier match first, otherwise a sensible default for any *spear* item.
        Float exact = TIER_DAMAGE.get(path);
        return exact != null ? exact : 6.0f;
    }

    private static boolean hasArrow(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(Items.ARROW)) return true;
        }
        return false;
    }

    private static void consumeArrow(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(Items.ARROW)) {
                stack.shrink(1);
                return;
            }
        }
    }
}
