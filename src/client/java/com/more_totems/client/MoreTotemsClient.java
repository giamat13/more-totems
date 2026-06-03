package com.more_totems.client;

import com.more_totems.ModItems;
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
	}
}
