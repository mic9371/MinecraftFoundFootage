// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil); removed from Veil for 1.21.1.
package com.sp.render.camera.anim;

import foundry.veil.api.client.util.Easing;
import net.minecraft.util.math.Vec3d;

public class Frame {
    Vec3d position;
    Vec3d rotation;
    Vec3d scale;

    public Frame(Vec3d position, Vec3d rotation, Vec3d scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Vec3d getPosition() {
        return position;
    }

    public Vec3d getRotation() {
        return rotation;
    }

    public Vec3d getScale() {
        return scale;
    }

    public Frame copy() {
        return new Frame(position, rotation, scale);
    }

    public Frame interpolate(Frame frame, float progress, Easing easing) {
        Vec3d position = this.position.add(frame.position.subtract(this.position).multiply(easing.ease(progress)));
        Vec3d rotation = this.rotation.add(frame.rotation.subtract(this.rotation).multiply(easing.ease(progress)));
        Vec3d scale = this.scale.add(frame.scale.subtract(this.scale).multiply(easing.ease(progress)));
        return new Frame(position, rotation, scale);
    }

    public Frame bezierInterpolate(Frame frame, float progress, Easing easing) {
        // interpolate from this to frame using a bezier curve
        // https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B%C3%A9zier_curves
        Vec3d p0 = this.position;
        Vec3d p1 = this.position.add(this.rotation);
        Vec3d p2 = frame.position.add(frame.rotation);
        Vec3d p3 = frame.position;
        Vec3d position = p0.multiply((float) Math.pow(1 - progress, 3))
                .add(p1.multiply(3 * progress * (float) Math.pow(1 - progress, 2)))
                .add(p2.multiply(3 * (float) Math.pow(progress, 2) * (1 - progress)))
                .add(p3.multiply((float) Math.pow(progress, 3)));
        Vec3d r0 = this.rotation;
        Vec3d r1 = this.rotation.add(this.scale);
        Vec3d r2 = frame.rotation.add(frame.scale);
        Vec3d r3 = frame.rotation;
        Vec3d rotation = r0.multiply((float) Math.pow(1 - progress, 3))
                .add(r1.multiply(3 * progress * (float) Math.pow(1 - progress, 2)))
                .add(r2.multiply(3 * (float) Math.pow(progress, 2) * (1 - progress)))
                .add(r3.multiply((float) Math.pow(progress, 3)));
        Vec3d s0 = this.scale;
        Vec3d s1 = this.scale.add(this.position);
        Vec3d s2 = frame.scale.add(frame.position);
        Vec3d s3 = frame.scale;
        Vec3d scale = s0.multiply((float) Math.pow(1 - progress, 3))
                .add(s1.multiply(3 * progress * (float) Math.pow(1 - progress, 2)))
                .add(s2.multiply(3 * (float) Math.pow(progress, 2) * (1 - progress)))
                .add(s3.multiply((float) Math.pow(progress, 3)));
        return new Frame(position, rotation, scale);
    }
}
