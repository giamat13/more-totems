package com.more_totems;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TotemNoFallPayload() implements CustomPacketPayload {

    public static final TotemNoFallPayload INSTANCE = new TotemNoFallPayload();

    public static final Type<TotemNoFallPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, "totem_no_fall"));

    public static final StreamCodec<ByteBuf, TotemNoFallPayload> CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<TotemNoFallPayload> type() {
        return TYPE;
    }
}
