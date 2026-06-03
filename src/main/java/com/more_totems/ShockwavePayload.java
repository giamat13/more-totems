package com.more_totems;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShockwavePayload() implements CustomPacketPayload {

    public static final ShockwavePayload INSTANCE = new ShockwavePayload();

    public static final Type<ShockwavePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, "shockwave_activated"));

    public static final StreamCodec<ByteBuf, ShockwavePayload> CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<ShockwavePayload> type() {
        return TYPE;
    }
}
