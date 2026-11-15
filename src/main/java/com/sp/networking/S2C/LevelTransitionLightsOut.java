package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record LevelTransitionLightsOut(int time) implements CustomPayload {
    public static final CustomPayload.Id<LevelTransitionLightsOut> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "ltos"));
    public static final PacketCodec<RegistryByteBuf, LevelTransitionLightsOut> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, LevelTransitionLightsOut::time,
            LevelTransitionLightsOut::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
