package com.more_totems;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Flint &amp; steel redstone pulse (issue #6).  Right-clicking a block with flint
 * and steel makes that block emit a redstone signal for two ticks (in addition
 * to its normal fire-lighting behaviour).
 *
 * <p>The actual signal is supplied by {@code LevelRedstonePulseMixin}, which
 * reports power 15 for any position currently registered here.
 */
public final class RedstonePulses {

    private static final int PULSE_TICKS = 2;

    // dimension -> (powered position -> game-time at which the pulse ends)
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> ACTIVE = new HashMap<>();

    private RedstonePulses() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.FLINT_AND_STEEL)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                BlockPos pos = hitResult.getBlockPos();
                ACTIVE.computeIfAbsent(level.dimension(), k -> new HashMap<>())
                      .put(pos.immutable(), level.getGameTime() + PULSE_TICKS);
                level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
                // also poke the 6 direct neighbours so adjacent redstone recalculates
                for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                    BlockPos n = pos.relative(dir);
                    level.updateNeighborsAt(n, level.getBlockState(n).getBlock());
                }
                MoreTotems.LOGGER.info("[redstone] flint&steel pulse registered at {}", pos);
            }
            return InteractionResult.PASS; // don't swallow the vanilla fire-lighting
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                expire(level);
            }
        });
    }

    /** Called from the mixin: is this position currently emitting a pulse? */
    public static boolean isPowered(Level level, BlockPos pos) {
        Map<BlockPos, Long> map = ACTIVE.get(level.dimension());
        if (map == null) return false;
        Long until = map.get(pos);
        boolean powered = until != null && level.getGameTime() < until;
        if (powered) {
            MoreTotems.LOGGER.info("[redstone] getSignal hook supplying power at {}", pos);
        }
        return powered;
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
                // depower neighbours now that the pulse is over
                level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
            }
        }
    }
}
