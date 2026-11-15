package com.sp.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Sodium replaces vanilla's chunk meshing and chunk shaders. This mod's visuals (deferred lighting, PBR, VHS look)
 * need a G-buffer written by the chunk shaders and per-vertex block material ids, neither of which Sodium's chunk
 * pipeline provides yet. Until that is ported, the mod runs next to Sodium with those visuals switched off.
 */
public final class SodiumCompat {
    public static final boolean PRESENT = FabricLoader.getInstance().isModLoaded("sodium");

    private SodiumCompat() {
    }
}
