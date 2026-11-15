package com.sp.mixin.respawnsystem;

import com.sp.init.BackroomsLevels;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * When a player dies inside a Backrooms level they respawn in that same level at their last death position
 * (or the level's origin if they died below y=0), keeping their rotation, and their spawn point is moved there.
 */
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow @Final private MinecraftServer server;

    @Unique ServerPlayerEntity targetPlayer;

    @Inject(method = "respawnPlayer", at = @At("HEAD"))
    private void setTargetPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        this.targetPlayer = player;
    }

    @Unique
    private @Nullable BlockPos getBackroomsRespawnPos() {
        if (!BackroomsLevels.isInBackrooms(targetPlayer.getWorld().getRegistryKey())) {
            return null;
        }

        Optional<GlobalPos> lastDeathPos = targetPlayer.getLastDeathPos();
        if (lastDeathPos.isEmpty()) {
            return null;
        }

        BlockPos pos = lastDeathPos.get().pos();
        if (pos.getY() < 0) {
            pos = BlockPos.ofFloored(BackroomsLevels.getCurrentLevelsOrigin(targetPlayer.getWorld().getRegistryKey()));
        }
        return pos;
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"))
    private TeleportTarget getBackroomsRespawnTarget(ServerPlayerEntity instance, boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition) {
        BlockPos respawnPos = getBackroomsRespawnPos();
        ServerWorld currentWorld = this.server.getWorld(targetPlayer.getWorld().getRegistryKey());

        if (respawnPos != null && currentWorld != null) {
            Vec3d pos = respawnPos.toCenterPos();
            return new TeleportTarget(currentWorld, pos, Vec3d.ZERO, targetPlayer.getYaw(), targetPlayer.getPitch(), postDimensionTransition);
        }

        return instance.getRespawnTarget(alive, postDimensionTransition);
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPointFrom(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void setBackroomsSpawnPoint(ServerPlayerEntity newPlayer, ServerPlayerEntity oldPlayer) {
        newPlayer.setSpawnPointFrom(oldPlayer);

        BlockPos respawnPos = getBackroomsRespawnPos();
        ServerWorld currentWorld = this.server.getWorld(targetPlayer.getWorld().getRegistryKey());

        if (respawnPos != null && currentWorld != null) {
            newPlayer.setSpawnPoint(currentWorld.getRegistryKey(), respawnPos, oldPlayer.getSpawnAngle(), oldPlayer.isSpawnForced(), false);
        }
    }
}
