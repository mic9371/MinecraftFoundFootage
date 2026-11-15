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

public record SyncServerComponent(boolean value, String component) implements CustomPayload {
    public static final CustomPayload.Id<SyncServerComponent> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "comp_sync"));
    public static final PacketCodec<RegistryByteBuf, SyncServerComponent> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, SyncServerComponent::value,
            PacketCodecs.STRING, SyncServerComponent::component,
            SyncServerComponent::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(SyncServerComponent payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        boolean readBoolean = payload.value();
        context.server().execute(() -> {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);

            switch (payload.component()) {
                case "beingCaptured": playerComponent.setBeingCaptured(readBoolean); break;
                case "cutscene": playerComponent.setDoingCutscene(readBoolean); break;
                case "flashlight": playerComponent.setFlashLightOn(readBoolean); break;
                case "glitch": playerComponent.setShouldInflictGlitchDamage(readBoolean); break;
                case "teleporting": playerComponent.setTeleporting(readBoolean); break;
            }

            playerComponent.sync();
        });
    }
}
