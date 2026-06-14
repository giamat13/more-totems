package com.more_totems.mixin;

import com.more_totems.MoreTotems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Issue #4 — Elytra + any Chestplate in an anvil → armoured elytra.
 *
 * <p>Hooks both {@code createResult} and {@code slotsChanged} (require = 0 each,
 * with a log) since the result-building method may be renamed on MC 26.1 — the
 * one that actually exists will fire and log, telling us the right name.
 * Uses only the public {@link AbstractContainerMenu#getSlot} API (no @Shadow).
 */
@Mixin(targets = "net.minecraft.world.inventory.AnvilMenu")
public abstract class AnvilArmoredElytraMixin extends AbstractContainerMenu {

    protected AnvilArmoredElytraMixin() {
        super(null, 0); // never invoked; required because we extend the menu
    }

    @Inject(method = "createResult", at = @At("TAIL"), require = 0)
    private void moreTotems$createResult(CallbackInfo ci) {
        moreTotems$tryArmouredElytra("createResult");
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"), require = 0)
    private void moreTotems$slotsChanged(Container container, CallbackInfo ci) {
        moreTotems$tryArmouredElytra("slotsChanged");
    }

    @Unique
    private void moreTotems$tryArmouredElytra(String via) {
        ItemStack a = this.getSlot(0).getItem();
        ItemStack b = this.getSlot(1).getItem();

        ItemStack elytra = null;
        ItemStack chestplate = null;
        if (a.is(Items.ELYTRA) && moreTotems$isChestplate(b)) {
            elytra = a;
            chestplate = b;
        } else if (b.is(Items.ELYTRA) && moreTotems$isChestplate(a)) {
            elytra = b;
            chestplate = a;
        }
        if (elytra == null) {
            return;
        }

        ItemStack result = elytra.copy();
        ItemAttributeModifiers armor = chestplate.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (armor != null) {
            result.set(DataComponents.ATTRIBUTE_MODIFIERS, armor);
        }
        this.getSlot(2).set(result);
        MoreTotems.LOGGER.info("[anvil] armoured elytra result set via {}", via);
    }

    @Unique
    private static boolean moreTotems$isChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().endsWith("_chestplate");
    }
}
