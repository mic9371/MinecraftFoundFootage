package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReloadLightsPacket() implements CustomPayload {
    public static final ReloadLightsPacket INSTANCE = new ReloadLightsPacket();
    public static final CustomPayload.Id<ReloadLightsPacket> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "rl_lights"));
    public static final PacketCodec<RegistryByteBuf, ReloadLightsPacket> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
