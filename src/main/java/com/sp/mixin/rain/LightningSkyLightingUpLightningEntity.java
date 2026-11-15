package com.sp.mixin.rain;

import com.sp.clientWrapper.ClientWrapper;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.init.BackroomsLevels;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightningEntity.class)
public class LightningSkyLightingUpLightningEntity {
    @WrapMethod(method = "tick")
    public void spbrevamped$LightingUpSkyDuringLightningTick(Operation<Void> original) {
        original.call();

        LightningEntity thiz = ((LightningEntity) (Object) this);
        if (!thiz.getWorld().isClient()) {
            return;
        }


        // Client-only logic lives in ClientWrapper so this common mixin does not reference client classes
        // (which made dedicated servers fail to load LightningEntity).
        ClientWrapper.tickLightningSky(thiz);
    }

    @WrapMethod(method = "spawnFire")
    public void spbrevamped$LightingUpSkyDuringLightningSpawnFire(int spreadAttempts, Operation<Void> original) {
        LightningEntity thiz = ((LightningEntity) (Object) this);

        if (BackroomsLevels.isInBackroomsLevel(thiz.getWorld(), BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return;
        }

        original.call(spreadAttempts);
    }
}
