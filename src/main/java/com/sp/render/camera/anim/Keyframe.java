// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil); removed from Veil for 1.21.1.
package com.sp.render.camera.anim;

import foundry.veil.api.client.util.Easing;
import net.minecraft.util.math.Vec3d;

public class Keyframe extends Frame {
    int duration;
    Easing easing;

    public Keyframe(Vec3d position, Vec3d rotation, Vec3d scale, int duration, Easing easing) {
        super(position, rotation, scale);
        this.duration = duration;
        this.easing = easing;
    }

    public Keyframe copy() {
        return new Keyframe(position, rotation, scale, duration, easing);
    }

    public int getDuration() {
        return duration;
    }

    public Easing getEasing() {
        return easing;
    }
}
