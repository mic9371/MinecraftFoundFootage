package com.sp.networking;

import com.sp.networking.C2S.*;
import com.sp.networking.S2C.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class InitializePackets {

    /** Payload types must be registered on both sides, so this is called from the common initializer. */
    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(TargetEntitySync.ID, TargetEntitySync.CODEC);
        PayloadTypeRegistry.playC2S().register(SeeActiveSkinwalkerSync.ID, SeeActiveSkinwalkerSync.CODEC);
        PayloadTypeRegistry.playC2S().register(SyncServerComponent.ID, SyncServerComponent.CODEC);

        PayloadTypeRegistry.playS2C().register(InvokeScreenShakePacket.ID, InvokeScreenShakePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(InvokeBlackScreenPacket.ID, InvokeBlackScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ReloadLightsPacket.ID, ReloadLightsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SoundPacket.ID, SoundPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(LevelTransitionLightsOut.ID, LevelTransitionLightsOut.CODEC);
    }

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(TargetEntitySync.ID, TargetEntitySync::receive);
        ServerPlayNetworking.registerGlobalReceiver(SeeActiveSkinwalkerSync.ID, SeeActiveSkinwalkerSync::receive);
        ServerPlayNetworking.registerGlobalReceiver(SyncServerComponent.ID, SyncServerComponent::receive);
    }
}
