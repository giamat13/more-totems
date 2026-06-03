package com.more_totems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreTotems implements ModInitializer {
	public static final String MOD_ID = "more-totems";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.initialize();

		PayloadTypeRegistry.playS2C().register(TotemActivatedPayload.TYPE, TotemActivatedPayload.CODEC);

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (!alive) InventoryStorage.restore(oldPlayer, newPlayer);
		});

		LOGGER.info("More Totems initialized!");
	}
}
