package com.more_totems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MoreTotems implements ModInitializer {
    public static final String MOD_ID = "more-totems";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.initialize();

        PayloadTypeRegistry.playS2C().register(TotemActivatedPayload.TYPE, TotemActivatedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShockwavePayload.TYPE, ShockwavePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TotemNoFallPayload.TYPE, TotemNoFallPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TotemEnchantPayload.TYPE, TotemEnchantPayload.CODEC);

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) InventoryStorage.restore(oldPlayer, newPlayer);
        });

        // ── BEFORE DEATH ─────────────────────────────────────────────────────────
        // Iron-variant totems activate when incoming damage would be lethal
        // (raw-damage check; generous for safety — may fire on heavily-armoured hits)
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;
            if (player.getHealth() - amount > 0) return true; // not lethal

            // Keep Inventory Iron: prevent death, inventory stays intact naturally
            if (TotemUtils.findAndDamageTotem(player, ModItems.TOTEM_OF_KEEP_INVENTORY_IRON)) {
                player.setHealth(1.0f);
                applyTotemEffects(player);
                ServerPlayNetworking.send(player, TotemActivatedPayload.INSTANCE);
                return false;
            }

            // Enchant Iron: prevent death + enchant all items
            if (TotemUtils.findAndDamageTotem(player, ModItems.TOTEM_OF_ENCHANT_IRON)) {
                player.setHealth(1.0f);
                applyTotemEffects(player);
                enchantInventory(player);
                ServerPlayNetworking.send(player, TotemEnchantPayload.INSTANCE);
                return false;
            }

            // No Fall Iron: last-resort against lethal falls only
            if (isFallDamage(source) && TotemUtils.findAndDamageTotem(player, ModItems.TOTEM_OF_NO_FALL_IRON)) {
                player.setHealth(1.0f);
                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));
                ServerPlayNetworking.send(player, TotemNoFallPayload.INSTANCE);
                return false;
            }

            // Shockwave Iron: knock enemies NOW (before the death hit lands),
            // then let the hit through so ALLOW_DEATH saves the player.
            if (TotemUtils.hasTotem(player, ModItems.TOTEM_OF_SHOCKWAVE_IRON)) {
                knockNearbyEntities(player);
                ServerPlayNetworking.send(player, ShockwavePayload.INSTANCE);
                // intentionally return true — damage is allowed, ALLOW_DEATH takes over
            }

            return true;
        });

        // ── AT DEATH ─────────────────────────────────────────────────────────────
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;

            // Shockwave (base or iron): save player + knock enemies
            boolean hasShockwave =
                    TotemUtils.findAndDamageTotem(player, ModItems.TOTEM_OF_SHOCKWAVE_IRON) ||
                    TotemUtils.findAndDamageTotem(player, ModItems.TOTEM_OF_SHOCKWAVE);
            if (hasShockwave) {
                player.setHealth(1.0f);
                applyTotemEffects(player);
                knockNearbyEntities(player);
                ServerPlayNetworking.send(player, ShockwavePayload.INSTANCE);
                return false;
            }

            // Enchant (base): save player + enchant all items
            if (TotemUtils.findAndDamageTotem(player, ModItems.TOTEM_OF_ENCHANT)) {
                player.setHealth(1.0f);
                applyTotemEffects(player);
                enchantInventory(player);
                ServerPlayNetworking.send(player, TotemEnchantPayload.INSTANCE);
                return false;
            }

            return true;
        });

        LOGGER.info("More Totems initialized!");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private static void applyTotemEffects(ServerPlayer player) {
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 3));
    }

    static void knockNearbyEntities(ServerPlayer player) {
        double radius = 8.0;
        ServerLevel level = (ServerLevel) player.level();
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player);
        for (LivingEntity target : nearby) {
            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0 && dist <= radius) {
                double force = (1.0 - dist / radius) * 1.5;
                target.knockback(force, dx, dz);
            }
        }
    }

    private static boolean isFallDamage(DamageSource source) {
        return "fall".equals(source.type().msgId());
    }

    static void enchantInventory(ServerPlayer player) {
        net.minecraft.core.Registry<Enchantment> enchReg =
                player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> unbreaking = enchReg.getHolderOrThrow(Enchantments.UNBREAKING);

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            applyUnbreaking(inv.getItem(i), unbreaking);
        }
        for (EquipmentSlot slot : InventoryStorage.EQUIPMENT_SLOTS) {
            applyUnbreaking(player.getItemBySlot(slot), unbreaking);
        }
    }

    private static void applyUnbreaking(ItemStack stack, Holder<Enchantment> unbreaking) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return;
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (current.getLevel(unbreaking) >= 3) return;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        mutable.set(unbreaking, 3);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }
}
