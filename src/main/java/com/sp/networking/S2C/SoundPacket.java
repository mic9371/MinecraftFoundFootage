package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;

public record SoundPacket(RegistryEntry<SoundEvent> sound, float volume, float pitch) implements CustomPayload {
    public static final CustomPayload.Id<SoundPacket> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "snd"));
    public static final PacketCodec<RegistryByteBuf, SoundPacket> CODEC = PacketCodec.tuple(
            SoundEvent.ENTRY_PACKET_CODEC, SoundPacket::sound,
            PacketCodecs.FLOAT, SoundPacket::volume,
            PacketCodecs.FLOAT, SoundPacket::pitch,
            SoundPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
