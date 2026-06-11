package com.more_totems.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Issue #4 — combine an Elytra with any Chestplate in an anvil to get an
 * "armoured elytra": the elytra keeps its gliding while gaining the chestplate's
 * armour. Costs 100 levels, and the anvil is destroyed by the single use.
 *
 * <p>Implemented against the standard Mojang names ({@code createResult},
 * {@code onTake}, {@code cost}, {@code access}). The injections use
 * {@code require = 0} so a mapping mismatch degrades gracefully; the two
 * {@code @Shadow} fields, however, must resolve for the class to load.
 */
@Mixin(targets = "net.minecraft.world.inventory.AnvilMenu")
public abstract class AnvilArmoredElytraMixin extends AbstractContainerMenu {

    @Shadow @Final public DataSlot cost;
    @Shadow @Final protected ContainerLevelAccess access;

    @Unique private boolean moreTotems$armoredElytra = false;

    protected AnvilArmoredElytraMixin() {
        super(null, 0); // never called; required because we extend the menu
    }

    @Inject(method = "createResult", at = @At("TAIL"), require = 0)
    private void moreTotems$createResult(CallbackInfo ci) {
        ItemStack a = this.getSlot(0).getItem();
        ItemStack b = this.getSlot(1).getItem();

        ItemStack elytra = null;
        ItemStack chestplate = null;
        if (a.is(Items.ELYTRA) && isChestplate(b)) {
            elytra = a;
            chestplate = b;
        } else if (b.is(Items.ELYTRA) && isChestplate(a)) {
            elytra = b;
            chestplate = a;
        }

        if (elytra == null) {
            this.moreTotems$armoredElytra = false;
            return;
        }

        ItemStack result = elytra.copy();
        ItemAttributeModifiers armor = chestplate.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (armor != null) {
            result.set(DataComponents.ATTRIBUTE_MODIFIERS, armor);
        }

        this.getSlot(2).set(result);
        this.cost.set(100);
        this.moreTotems$armoredElytra = true;
    }

    @Inject(method = "onTake", at = @At("TAIL"), require = 0)
    private void moreTotems$onTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!this.moreTotems$armoredElytra) return;
        this.moreTotems$armoredElytra = false;
        this.access.execute((level, pos) -> {
            level.destroyBlock(pos, false);
            level.levelEvent(1029, pos, 0); // anvil-destroyed effect
        });
    }

    @Unique
    private static boolean isChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().endsWith("_chestplate");
    }
}
