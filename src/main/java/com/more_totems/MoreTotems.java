package com.more_totems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (!alive) InventoryStorage.restore(oldPlayer, newPlayer);
		});

		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;

			ItemStack mainHand = player.getMainHandItem();
			ItemStack offHand = player.getOffhandItem();

			ItemStack totemStack = null;
			if (mainHand.is(ModItems.TOTEM_OF_SHOCKWAVE)) {
				totemStack = mainHand;
			} else if (offHand.is(ModItems.TOTEM_OF_SHOCKWAVE)) {
				totemStack = offHand;
			}
			if (totemStack == null) return true;

			totemStack.shrink(1);
			player.setHealth(1.0f);

			// Vanilla Totem of Undying effects
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 3));

			// Shockwave: knock back all nearby living entities
			double radius = 8.0;
			ServerLevel serverLevel = (ServerLevel) player.level();
			AABB box = player.getBoundingBox().inflate(radius);
			List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
					LivingEntity.class, box, e -> e != player);
			for (LivingEntity target : nearbyEntities) {
				double dx = target.getX() - player.getX();
				double dz = target.getZ() - player.getZ();
				double dist = Math.sqrt(dx * dx + dz * dz);
				if (dist > 0 && dist <= radius) {
					double force = (1.0 - dist / radius) * 1.5;
					target.knockback(force, dx, dz);
				}
			}

			ServerPlayNetworking.send(player, ShockwavePayload.INSTANCE);
			return false;
		});

		LOGGER.info("More Totems initialized!");
	}
}
