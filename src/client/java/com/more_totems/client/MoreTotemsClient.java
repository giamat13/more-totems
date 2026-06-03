package com.more_totems.client;

import com.more_totems.ModItems;
import com.more_totems.ShockwavePayload;
import com.more_totems.TotemActivatedPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class MoreTotemsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(TotemActivatedPayload.TYPE, (payload, context) -> {
			Minecraft client = context.client();
			LocalPlayer player = client.player;
			if (player == null) {
				return;
			}

			// Big on-screen totem animation, using our custom totem texture.
			client.gameRenderer.displayItemActivation(new ItemStack(ModItems.TOTEM_OF_KEEP_INVENTORY));

			// Sparkle particles around the player, like the vanilla totem pop.
			RandomSource random = player.getRandom();
			for (int i = 0; i < 30; i++) {
				player.level().addParticle(
						ParticleTypes.TOTEM_OF_UNDYING,
						player.getX() + random.nextDouble() - 0.5,
						player.getY() + 1.0 + random.nextDouble() * 2.0,
						player.getZ() + random.nextDouble() - 0.5,
						random.nextGaussian() * 0.05,
						random.nextDouble() * 0.2,
						random.nextGaussian() * 0.05
				);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(ShockwavePayload.TYPE, (payload, context) -> {
			Minecraft client = context.client();
			LocalPlayer player = client.player;
			if (player == null) {
				return;
			}

			// On-screen totem animation with our shockwave totem texture.
			client.gameRenderer.displayItemActivation(new ItemStack(ModItems.TOTEM_OF_SHOCKWAVE));

			// Totem sparkle particles near the player.
			RandomSource random = player.getRandom();
			for (int i = 0; i < 20; i++) {
				player.level().addParticle(
						ParticleTypes.TOTEM_OF_UNDYING,
						player.getX() + random.nextDouble() - 0.5,
						player.getY() + 1.0 + random.nextDouble() * 2.0,
						player.getZ() + random.nextDouble() - 0.5,
						random.nextGaussian() * 0.05,
						random.nextDouble() * 0.2,
						random.nextGaussian() * 0.05
				);
			}

			// Expanding shockwave rings of explosion particles.
			for (int ring = 1; ring <= 5; ring++) {
				double radius = ring * 1.6;
				int count = Math.max(8, (int) (radius * 5));
				for (int i = 0; i < count; i++) {
					double angle = (2 * Math.PI * i) / count;
					double x = player.getX() + radius * Math.cos(angle);
					double z = player.getZ() + radius * Math.sin(angle);
					player.level().addParticle(
							ParticleTypes.EXPLOSION,
							x, player.getY() + 0.3, z,
							0, 0.02, 0
					);
				}
			}
		});
	}
}
