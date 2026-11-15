// Ported from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil): drives the vendored deferred renderer.
package com.sp.mixin.deferred;

import com.sp.render.deferred.DeferredRenderer;
import com.sp.render.deferred.DeferredShaderStateCache;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Unique
    private final DeferredShaderStateCache spb$cache = new DeferredShaderStateCache();

    @Inject(method = "renderParticles", at = @At("HEAD"))
    private void spb$setupRenderState(CallbackInfo ci) {
        DeferredRenderer.get().setup();
    }

    @Inject(method = "renderParticles", at = @At("RETURN"))
    private void spb$clearRenderState(CallbackInfo ci) {
        DeferredRenderer.get().clear();
    }

    @ModifyArg(method = "renderParticles", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"), index = 0)
    private Supplier<ShaderProgram> spb$setShader(Supplier<ShaderProgram> value) {
        return () -> this.spb$cache.getShader(value.get());
    }
}
