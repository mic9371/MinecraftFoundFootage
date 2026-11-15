package com.sp.render.deferred;

import com.sp.SPBRevamped;
import net.minecraft.util.Identifier;

/**
 * Shader programs used by the deferred light renderers.
 * Point and area lights use this mod's shadow-casting light shaders; in 1.20.1 this was done by overriding Veil's VeilShaders constants with a mixin.
 */
public final class DeferredShaders {
    public static final Identifier LIGHT_POINT = Identifier.of("spbrevamped", "point");
    public static final Identifier LIGHT_AREA = Identifier.of("spbrevamped", "area");
    public static final Identifier LIGHT_DIRECTIONAL = Identifier.of(SPBRevamped.MOD_ID, "deferred/light/directional");
    public static final Identifier LIGHT_VANILLA_LIGHTMAP = Identifier.of(SPBRevamped.MOD_ID, "deferred/light/vanilla_lightmap");
    public static final Identifier LIGHT_INDIRECT_SPHERE = Identifier.of(SPBRevamped.MOD_ID, "deferred/light/indirect_sphere");

    private DeferredShaders() {
    }
}
