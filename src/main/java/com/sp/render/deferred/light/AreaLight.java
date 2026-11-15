// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light;

import com.sp.render.deferred.light.LightTypeRegistry;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;

public class AreaLight extends Light implements InstancedLight, PositionedLight<AreaLight> {

    private static final float MAX_ANGLE_SIZE = (float) (65535 / 2 / Math.PI);

    protected final Vector3d position;
    protected final Quaternionf orientation;
    private final Matrix4d matrix;

    protected final Vector2f size;

    protected float angle;
    protected float distance;

    public AreaLight() {
        this.matrix = new Matrix4d();
        this.position = new Vector3d();
        this.orientation = new Quaternionf();

        this.size = new Vector2f(1.0F, 1.0F);

        this.angle = (float) Math.toRadians(45);
        this.distance = 1.0F;
    }

    @Override
    public void store(ByteBuffer buffer) {
        this.matrix.getFloats(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 16);

        buffer.putFloat(this.color.x() * this.brightness);
        buffer.putFloat(this.color.y() * this.brightness);
        buffer.putFloat(this.color.z() * this.brightness);

        this.size.get(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 2);

        // SPB: stored as a float instead of a short so the area light shader attribute layout lines up (was AreaLightMixin)
        buffer.putFloat((float) MathHelper.clamp((int) (this.angle * MAX_ANGLE_SIZE), 0, 65535));
        buffer.putFloat(this.distance);
    }

    @Override
    public LightTypeRegistry.LightType<?> getType() {
        return LightTypeRegistry.AREA.get();
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    /**
     * @return The current orientation of the light.
     */
    public Quaternionf getOrientation() {
        return this.orientation;
    }

    /**
     * @return The size of the light's surface
     */
    public Vector2f getSize() {
        return this.size;
    }

    /**
     * @return The maximum angle of the light from the plane's surface.
     */
    public float getAngle() {
        return this.angle;
    }

    /**
     * @return The maximum distance the light can travel
     */
    public float getDistance() {
        return this.distance;
    }

    @Override
    public AreaLight setColor(float red, float green, float blue) {
        return (AreaLight) super.setColor(red, green, blue);
    }

    @Override
    public AreaLight setColor(Vector3fc color) {
        return (AreaLight) super.setColor(color);
    }

    @Override
    public AreaLight setBrightness(float brightness) {
        return (AreaLight) super.setBrightness(brightness);
    }

    @Override
    public AreaLight setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
        this.updateMatrix();
        return this;
    }

    /**
     * Sets the orientation of the light's surface
     *
     * @param orientation The orientation of the light's surface.
     */
    public AreaLight setOrientation(Quaternionfc orientation) {
        this.orientation.set(orientation).normalize();
        this.updateMatrix();
        return this;
    }

    /**
     * Sets the size of the light's surface
     *
     * @param x The length, in blocks, of the light's surface.
     * @param y The width, in blocks, of the light's surface.
     */
    public AreaLight setSize(double x, double y) {
        this.size.set(x, y);
        this.markDirty();
        return this;
    }

    /**
     * Sets the maximum angle the light can influence.
     *
     * @param angle The maximum angle of the light's influence in radians
     */
    public AreaLight setAngle(float angle) {
        this.angle = angle;
        this.markDirty();
        return this;
    }

    /**
     * Sets the maximum distance the light can influence.
     *
     * @param distance The maximum area of influence for the light
     */
    public AreaLight setDistance(float distance) {
        this.distance = distance;
        this.markDirty();
        return this;
    }

    @Override
    public Light setTo(Camera camera) {
        Vec3d pos = camera.getPos();
        return this.setPosition(pos.x, pos.y, pos.z).setOrientation(new Quaternionf().lookAlong(camera.getHorizontalPlane().mul(-1), camera.getVerticalPlane()));
    }

    protected void updateMatrix() {
        Vector3d position = this.getPosition();
        Quaternionf orientation = this.getOrientation();
        this.matrix.rotation(orientation).translate(position);
        this.markDirty();
    }

    @Override
    public AreaLight clone() {
        AreaLight light = new AreaLight();
        light.matrix.set(this.matrix);
        light.size.set(this.size);
        light.angle = this.angle;
        light.distance = this.distance;
        light.markDirty();
        return light;
    }

}
