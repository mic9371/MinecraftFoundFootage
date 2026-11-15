package com.sp.networking;

import com.sp.SPBRevampedClient;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.ModSounds;
import com.sp.networking.S2C.*;
import com.sp.render.deferred.DeferredRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Client-side handlers for server-to-client packets. Kept separate from the payload records so the dedicated server
 * never loads client classes when the payload types are registered.
 */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(InvokeScreenShakePacket.ID, ClientPacketHandlers::receiveInvokeScreenShake);
        ClientPlayNetworking.registerGlobalReceiver(InvokeBlackScreenPacket.ID, ClientPacketHandlers::receiveInvokeBlackScreen);
        ClientPlayNetworking.registerGlobalReceiver(ReloadLightsPacket.ID, ClientPacketHandlers::receiveReloadLights);
        ClientPlayNetworking.registerGlobalReceiver(SoundPacket.ID, ClientPacketHandlers::receiveSound);
        ClientPlayNetworking.registerGlobalReceiver(LevelTransitionLightsOut.ID, ClientPacketHandlers::receiveLevelTransitionLightsOut);
    }

    static void receiveInvokeBlackScreen(InvokeBlackScreenPacket payload, ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(payload.duration(), payload.shouldPauseSounds(), payload.noEscape());
        });
    }

    static void receiveInvokeScreenShake(InvokeScreenShakePacket payload, ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            SPBRevampedClient.getCameraShake().noiseSpeed = payload.speed();
            SPBRevampedClient.getCameraShake().trauma = payload.trauma();
        });
    }

    static void receiveLevelTransitionLightsOut(LevelTransitionLightsOut payload, ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        int time = payload.time();

        client.execute(() -> {
            if (client.player != null) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(client.player);
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

                //Turn off the lights
                playerComponent.player.playSoundToPlayer(ModSounds.LIGHTS_OUT, SoundCategory.AMBIENT, 1, 1);
                SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(time, false, false);

                //PlaySound after black screen is over
                executorService.schedule(() -> {
                    playerComponent.player.playSoundToPlayer(ModSounds.LIGHTS_ON, SoundCategory.AMBIENT, 1, 1);
                    SPBRevampedClient.sendComponentSyncPacket(false, "teleporting");
                    executorService.shutdown();
                }, (time * 100L) / 2, TimeUnit.MILLISECONDS);
            }
        });
    }

    static void receiveReloadLights(ReloadLightsPacket payload, ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            VeilRenderer renderer = VeilRenderSystem.renderer();
            DeferredRenderer.get().getLightRenderer().free();

            if (SPBRevampedClient.getCutsceneManager().started) {
                SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(60, true, false);
            }
        });
    }

    static void receiveSound(SoundPacket payload, ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            client.getSoundManager().play(PositionedSoundInstance.master(payload.sound().value(), payload.pitch(), payload.volume()));
        });
    }
}
