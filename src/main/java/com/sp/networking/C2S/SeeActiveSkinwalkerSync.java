package com.sp.networking.C2S;

import com.sp.SPBRevamped;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public record SeeActiveSkinwalkerSync(boolean canSeeSkinwalker) implements CustomPayload {
    public static final CustomPayload.Id<SeeActiveSkinwalkerSync> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "see_skin"));
    public static final PacketCodec<RegistryByteBuf, SeeActiveSkinwalkerSync> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, SeeActiveSkinwalkerSync::canSeeSkinwalker,
            SeeActiveSkinwalkerSync::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(SeeActiveSkinwalkerSync payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
            playerComponent.setCanSeeActiveSkinWalkerTarget(payload.canSeeSkinwalker());
        });
    }
}
