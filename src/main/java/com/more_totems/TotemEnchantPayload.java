package com.more_totems;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TotemEnchantPayload() implements CustomPacketPayload {

    public static final TotemEnchantPayload INSTANCE = new TotemEnchantPayload();

    public static final Type<TotemEnchantPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, "totem_enchant"));

    public static final StreamCodec<ByteBuf, TotemEnchantPayload> CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<TotemEnchantPayload> type() {
        return TYPE;
    }
}
