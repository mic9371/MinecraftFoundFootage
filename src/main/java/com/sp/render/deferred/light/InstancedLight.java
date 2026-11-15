// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light;

import com.sp.render.deferred.light.renderer.InstancedLightRenderer;

import java.nio.ByteBuffer;

/**
 * A light that can be rendered with an implementation of {@link InstancedLightRenderer}.
 *
 * @author Ocelot
 */
public interface InstancedLight {

    /**
     * Stores the data of this light into the specified buffer.
     *
     * @param buffer The buffer to fill
     */
    void store(ByteBuffer buffer);
}
