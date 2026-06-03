package com.more_totems;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client packet telling the client to play the keep-inventory totem
 * activation animation (the on-screen totem + sparkle particles) using our
 * custom totem item, instead of the hard-coded vanilla totem-of-undying.
 */
public record TotemActivatedPayload() implements CustomPacketPayload {

    public static final TotemActivatedPayload INSTANCE = new TotemActivatedPayload();

    public static final Type<TotemActivatedPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MoreTotems.MOD_ID, "totem_activated"));

    public static final StreamCodec<ByteBuf, TotemActivatedPayload> CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<TotemActivatedPayload> type() {
        return TYPE;
    }
}
