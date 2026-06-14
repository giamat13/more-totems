package com.more_totems;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Grapple Hook — right-click while aiming at a block within range to yank
 * yourself toward it.  A hitscan pull (no projectile entity), so it is snappy
 * and predictable.  Cancels fall damage for the launch.
 */
public class GrappleHookItem extends Item {

    private static final double RANGE = 32.0;

    public GrappleHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Vec3 start = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0f);
        Vec3 end = start.add(dir.scale(RANGE));

        BlockHitResult hit = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            Vec3 pull = hit.getLocation().subtract(player.position());
            double distance = pull.length();
            double speed = Math.min(2.4, 0.7 + distance * 0.12);
            Vec3 velocity = pull.normalize().scale(speed);

            // A little extra lift so you arc onto the ledge instead of into the wall.
            player.setDeltaMovement(velocity.x, velocity.y + 0.3, velocity.z);
            player.hurtMarked = true;        // sync the new velocity to the client
            player.fallDistance = 0;         // the launch shouldn't hurt

            RangedUtils.damageItem(player.getItemInHand(hand), player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.7f, 1.4f);
        }
        return InteractionResult.SUCCESS;
    }
}
