package com.more_totems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ore Shooter ("Roulette") — fires an ore from your inventory as a hitscan shot.
 * The richer the ore you feed it, the harder it hits.  It always spends the
 * strongest ore you are carrying.
 */
public class OreShooterItem extends Item {

    private static final double RANGE = 48.0;

    /** Ore ammunition, ordered strongest first; the shooter spends the best one available. */
    private static final Map<Item, Float> AMMO = new LinkedHashMap<>();
    static {
        AMMO.put(Items.NETHERITE_INGOT, 16.0f);
        AMMO.put(Items.DIAMOND,         11.0f);
        AMMO.put(Items.EMERALD,          9.0f);
        AMMO.put(Items.RAW_GOLD,         6.0f);
        AMMO.put(Items.GOLD_INGOT,       6.0f);
        AMMO.put(Items.AMETHYST_SHARD,   5.5f);
        AMMO.put(Items.RAW_IRON,         5.0f);
        AMMO.put(Items.IRON_INGOT,       5.0f);
        AMMO.put(Items.LAPIS_LAZULI,     4.0f);
        AMMO.put(Items.RAW_COPPER,       4.0f);
        AMMO.put(Items.COPPER_INGOT,     4.0f);
        AMMO.put(Items.QUARTZ,           4.0f);
        AMMO.put(Items.REDSTONE,         3.0f);
        AMMO.put(Items.COAL,             2.0f);
    }

    public OreShooterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Item ammo = strongestAmmo(player);
        if (ammo == null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            consumeOne(player, ammo);
            float damage = AMMO.get(ammo);

            LivingEntity target = RangedUtils.targetedLiving(player, RANGE);
            if (target != null) {
                RangedUtils.dealDamage(serverLevel, player, target, damage);
            }

            RangedUtils.damageItem(player.getItemInHand(hand), player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT.value(), SoundSource.PLAYERS, 0.9f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    private static Item strongestAmmo(Player player) {
        if (player.getAbilities().instabuild) {
            // Creative: just use the best ore the player happens to hold, else iron-tier.
            Item held = firstAmmoInInventory(player);
            return held != null ? held : Items.IRON_INGOT;
        }
        Inventory inv = player.getInventory();
        Item best = null;
        float bestDamage = -1f;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            Item item = inv.getItem(i).getItem();
            Float damage = AMMO.get(item);
            if (damage != null && damage > bestDamage) {
                best = item;
                bestDamage = damage;
            }
        }
        return best;
    }

    private static Item firstAmmoInInventory(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            Item item = inv.getItem(i).getItem();
            if (AMMO.containsKey(item)) return item;
        }
        return null;
    }

    private static void consumeOne(Player player, Item ammo) {
        if (player.getAbilities().instabuild) return;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ammo)) {
                stack.shrink(1);
                return;
            }
        }
    }
}
