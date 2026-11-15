package com.sp.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public final class TeleportUtil {
    private TeleportUtil() {
    }

    /**
     * Same-dimension teleport with the semantics of 1.20.1's {@code Entity.teleport(double, double, double)}:
     * it only acts on the logical server and is a no-op on the client.
     */
    public static void teleport(Entity entity, double x, double y, double z) {
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            entity.teleport(serverWorld, x, y, z, Set.of(), entity.getYaw(), entity.getPitch());
        }
    }
}
