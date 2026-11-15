package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record InvokeScreenShakePacket(double speed, double trauma) implements CustomPayload {
    public static final CustomPayload.Id<InvokeScreenShakePacket> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "scr_shake"));
    public static final PacketCodec<RegistryByteBuf, InvokeScreenShakePacket> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, InvokeScreenShakePacket::speed,
            PacketCodecs.DOUBLE, InvokeScreenShakePacket::trauma,
            InvokeScreenShakePacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
