package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record InvokeBlackScreenPacket(int duration, boolean shouldPauseSounds, boolean noEscape) implements CustomPayload {
    public static final CustomPayload.Id<InvokeBlackScreenPacket> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "blk_screen"));
    public static final PacketCodec<RegistryByteBuf, InvokeBlackScreenPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, InvokeBlackScreenPacket::duration,
            PacketCodecs.BOOL, InvokeBlackScreenPacket::shouldPauseSounds,
            PacketCodecs.BOOL, InvokeBlackScreenPacket::noEscape,
            InvokeBlackScreenPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
