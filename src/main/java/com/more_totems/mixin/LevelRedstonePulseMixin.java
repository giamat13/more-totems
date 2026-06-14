package com.more_totems.mixin;

import com.more_totems.RedstonePulses;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reports a full redstone signal (15) for any block position that the flint
 * &amp; steel pulse system ({@link RedstonePulses}) has temporarily activated.
 *
 * <p>{@code require = 0}: if the {@code getSignal} mapping differs on this
 * Minecraft version the injection simply no-ops instead of crashing the game —
 * the pulse feature is then inert but everything else keeps working.
 */
@Mixin(Level.class)
public class LevelRedstonePulseMixin {

    @Inject(method = "getSignal", at = @At("HEAD"), cancellable = true, require = 0)
    private void moreTotems$pulseSignal(BlockPos pos, Direction direction,
                                        CallbackInfoReturnable<Integer> cir) {
        Level self = (Level) (Object) this;
        if (RedstonePulses.isPowered(self, pos)) {
            cir.setReturnValue(15);
        }
    }

    @Inject(method = "getDirectSignal", at = @At("HEAD"), cancellable = true, require = 0)
    private void moreTotems$pulseDirectSignal(BlockPos pos, Direction direction,
                                              CallbackInfoReturnable<Integer> cir) {
        Level self = (Level) (Object) this;
        if (RedstonePulses.isPowered(self, pos)) {
            cir.setReturnValue(15);
        }
    }
}
