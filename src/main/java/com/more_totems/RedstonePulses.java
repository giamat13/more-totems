package com.more_totems;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Flint &amp; steel redstone pulse (issue #6).  Right-clicking a block face with
 * flint and steel briefly places a redstone block in the air against that face,
 * which powers everything around it for two ticks, then removes it.
 *
 * <p>This needs no mixin into the (renamed-on-26.1) redstone signal methods — it
 * uses a real, vanilla power source, so it just works.
 */
public final class RedstonePulses {

    private static final int PULSE_TICKS = 2;

    // dimension -> (temporary-redstone-block position -> game-time to remove it)
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> ACTIVE = new HashMap<>();

    private RedstonePulses() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.FLINT_AND_STEEL)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                Direction face = hitResult.getDirection();
                BlockPos placePos = hitResult.getBlockPos().relative(face);
                if (level.getBlockState(placePos).isAir()) {
                    level.setBlockAndUpdate(placePos, Blocks.REDSTONE_BLOCK.defaultBlockState());
                    ACTIVE.computeIfAbsent(level.dimension(), k -> new HashMap<>())
                          .put(placePos.immutable(), level.getGameTime() + PULSE_TICKS);
                    MoreTotems.LOGGER.info("[redstone] pulse block placed at {}", placePos);
                }
            }
            return InteractionResult.PASS; // let flint&steel still light fire too
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                expire(level);
            }
        });
    }

    private static void expire(ServerLevel level) {
        Map<BlockPos, Long> map = ACTIVE.get(level.dimension());
        if (map == null || map.isEmpty()) return;
        long now = level.getGameTime();
        Iterator<Map.Entry<BlockPos, Long>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Long> entry = it.next();
            if (now >= entry.getValue()) {
                BlockPos pos = entry.getKey();
                it.remove();
                if (level.getBlockState(pos).is(Blocks.REDSTONE_BLOCK)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}
