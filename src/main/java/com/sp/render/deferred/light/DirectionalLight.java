// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light;

import com.sp.render.deferred.light.LightTypeRegistry;
import net.minecraft.client.render.Camera;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Represents a light where all rays come from the same direction everywhere. (The sun)
 */
public class DirectionalLight extends Light {

    protected final Vector3f direction;

    public DirectionalLight() {
        this.direction = new Vector3f(0.0F, -1.0F, 0.0F);
    }

    /**
     * @return The direction this light is facing
     */
    public Vector3fc getDirection() {
        return this.direction;
    }

    @Override
    public DirectionalLight setColor(float red, float green, float blue) {
        return (DirectionalLight) super.setColor(red, green, blue);
    }

    @Override
    public DirectionalLight setColor(Vector3fc color) {
        return (DirectionalLight) super.setColor(color);
    }

    @Override
    public DirectionalLight setColor(int color) {
        return (DirectionalLight) super.setColor(color);
    }

    /**
     * Sets the direction of this light.
     *
     * @param direction The new direction
     */
    public DirectionalLight setDirection(Vector3fc direction) {
        return this.setDirection(direction.x(), direction.y(), direction.z());
    }

    /**
     * Sets the direction of this light.
     *
     * @param x The new x direction
     * @param y The new y direction
     * @param z The new z direction
     */
    public DirectionalLight setDirection(float x, float y, float z) {
        this.direction.set(x, y, z).normalize();
        this.markDirty();
        return this;
    }

    @Override
    public DirectionalLight setTo(Camera camera) {
        Vector3f look = camera.getHorizontalPlane();
        return this.setDirection(look.x, look.y, look.z);
    }

    @Override
    public LightTypeRegistry.LightType<?> getType() {
        return LightTypeRegistry.DIRECTIONAL.get();
    }

    @Override
    public DirectionalLight clone() {
        return new DirectionalLight()
                .setColor(this.color)
                .setDirection(this.direction);
    }

}
