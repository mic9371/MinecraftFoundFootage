// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light;

import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A light in the renderer that has a position
 *
 * @param <T> The type of light to return in setters
 * @author Ocelot
 */
public interface PositionedLight<T extends PositionedLight<T>> {

    /**
     * @return The position of this light
     */
    Vector3d getPosition();

    /**
     * Sets the origin position of this light.
     *
     * @param position The position of the light
     */
    default T setPosition(Vector3dc position) {
        return this.setPosition(position.x(), position.y(), position.z());
    }

    /**
     * Sets the origin position of this light.
     *
     * @param x The x position of the light
     * @param y The y position of the light
     * @param z The z position of the light
     */
    T setPosition(double x, double y, double z);
}
