package com.more_totems.mixin;

import com.more_totems.MoreTotems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
 * Issue #4 — combine an Elytra with any Chestplate in an anvil to get an
 * "armoured elytra" (the elytra keeps gliding but gains the chestplate's armour).
 *
 * <p>This version avoids {@code @Shadow} fields entirely (their MC 26 names crashed
 * the client) and uses only the public {@link AbstractContainerMenu#getSlot} API.
 * The inject uses {@code require = 0}, so if the {@code createResult} name differs
 * it simply no-ops instead of crashing.
 *
 * <p>TODO (needs MC 26 field names): set the XP cost to 100 and destroy the anvil
 * on take — both require {@code AnvilMenu.cost} / {@code ItemCombinerMenu.access}.
 */
@Mixin(targets = "net.minecraft.world.inventory.AnvilMenu")
public abstract class AnvilArmoredElytraMixin extends AbstractContainerMenu {

    protected AnvilArmoredElytraMixin() {
        super(null, 0); // never invoked; required because we extend the menu
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
            return;
        }

        ItemStack result = elytra.copy();
        ItemAttributeModifiers armor = chestplate.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (armor != null) {
            result.set(DataComponents.ATTRIBUTE_MODIFIERS, armor);
        }
        this.getSlot(2).set(result);
        MoreTotems.LOGGER.info("[anvil] armoured elytra result set");
    }

    @Unique
    private static boolean isChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().endsWith("_chestplate");
    }
}
