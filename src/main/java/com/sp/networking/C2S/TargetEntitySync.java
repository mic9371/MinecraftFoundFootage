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

/** {@code targetId} is -1 when the player is not looking at an entity. */
public record TargetEntitySync(int targetId) implements CustomPayload {
    public static final CustomPayload.Id<TargetEntitySync> ID = new CustomPayload.Id<>(Identifier.of(SPBRevamped.MOD_ID, "targ_ent"));
    public static final PacketCodec<RegistryByteBuf, TargetEntitySync> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, TargetEntitySync::targetId,
            TargetEntitySync::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(TargetEntitySync payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        int targetID = payload.targetId();
        context.server().execute(() -> {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);

            if (targetID == -1) {
                playerComponent.setTargetEntity(null);
            } else {
                playerComponent.setTargetEntity(player.getWorld().getEntityById(targetID));
            }
        });
    }
}
