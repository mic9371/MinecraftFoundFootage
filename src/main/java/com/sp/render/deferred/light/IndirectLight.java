// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light;

import com.sp.render.deferred.light.renderer.InstancedLightRenderer;

/**
 * A light that can be rendered with an implementation of {@link InstancedLightRenderer}.
 *
 * @author Ocelot
 */
public interface IndirectLight<T extends PositionedLight<T>> extends PositionedLight<T>, InstancedLight {

    /**
     * @return The maximum distance the light can travel
     */
    float getRadius();

    /**
     * Sets the maximum radius the light can influence.
     *
     * @param radius The maximum area of influence for the light
     */
    T setRadius(float radius);
}
