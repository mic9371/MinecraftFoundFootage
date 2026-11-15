// Ported from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil): drives the vendored deferred renderer.
package com.sp.mixin.deferred;

import com.sp.render.deferred.DeferredShaderStateCache;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Swaps vanilla shaders used by render layers for their deferred variants while deferred rendering is active.
 */
@Mixin(RenderPhase.ShaderProgram.class)
public class ShaderStateMixin extends RenderPhase {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow
    @Final
    private Optional<Supplier<net.minecraft.client.gl.ShaderProgram>> supplier;

    @Unique
    private final DeferredShaderStateCache spb$cache = new DeferredShaderStateCache();

    public ShaderStateMixin(String name, Runnable beginAction, Runnable endAction) {
        super(name, beginAction, endAction);
    }

    @Override
    public void startDrawing() {
        if (!this.spb$cache.setupRenderState(this.supplier.orElse(() -> null).get())) {
            super.startDrawing();
        }
    }
}
