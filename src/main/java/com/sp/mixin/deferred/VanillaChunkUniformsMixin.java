package com.sp.mixin.deferred;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feeds the normal matrix to vanilla chunk shaders. Split out of WorldRendererMixin because it targets vanilla's
 * renderLayer, which Sodium replaces (see SPBMixinPlugin).
 */
@Mixin(WorldRenderer.class)
public class VanillaChunkUniformsMixin {
    @Inject(method = "renderLayer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getShader()Lnet/minecraft/client/gl/ShaderProgram;", shift = At.Shift.AFTER))
    private void spb$updateUniforms(RenderLayer renderLayer, double x, double y, double z, Matrix4f viewMatrix, Matrix4f positionMatrix, CallbackInfo ci) {
        ShaderProgram shader = RenderSystem.getShader();
        if (shader != null) {
            var normalMat = shader.getUniform("NormalMat");
            if (normalMat != null) {
                normalMat.set(viewMatrix.normal(new Matrix3f()));
            }
        }
    }
}
