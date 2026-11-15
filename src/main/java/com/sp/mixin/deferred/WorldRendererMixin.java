// Ported from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil): drives the vendored deferred renderer.
package com.sp.mixin.deferred;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sp.render.deferred.DeferredRenderer;
import com.sp.render.deferred.DeferredShaderStateCache;
import foundry.veil.impl.client.render.shader.VeilVanillaShaders;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Unique
    private final DeferredShaderStateCache spb$weatherCache = new DeferredShaderStateCache();
    @Unique
    private final DeferredShaderStateCache spb$worldborderCache = new DeferredShaderStateCache();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", shift = At.Shift.BEFORE))
    private void spb$setupOpaque(CallbackInfo ci) {
        DeferredRenderer.get().beginOpaque();
        // In 1.20.1 the first RenderTarget#beginWrite came before the chunk layers; in 1.21.1 it comes after them,
        // so the G-buffer has to be bound here for the terrain to be drawn into it.
        DeferredRenderer.get().setup();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V", shift = At.Shift.AFTER))
    private void spb$bindWrite(CallbackInfo ci) {
        DeferredRenderer.get().setup();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", shift = At.Shift.BEFORE))
    private void spb$preRain(CallbackInfo ci) {
        DeferredRenderer.get().setup();
        // This sets the blend function for rain correctly
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V", shift = At.Shift.AFTER))
    private void spb$postRain(CallbackInfo ci) {
        DeferredRenderer.get().clear();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V", shift = At.Shift.BEFORE))
    private void spb$preClouds(CallbackInfo ci) {
        DeferredRenderer.get().setup();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V", shift = At.Shift.AFTER))
    private void spb$postClouds(CallbackInfo ci) {
        DeferredRenderer.get().clear();
    }

    // Add custom weather shader
    @ModifyArg(method = "renderWeather", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"))
    private Supplier<ShaderProgram> spb$setWeatherShader(Supplier<ShaderProgram> supplier) {
        return () -> this.spb$weatherCache.getShader(supplier.get());
    }

    // Add custom world border shader
    @ModifyArg(method = "renderWorldBorder", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"))
    private Supplier<ShaderProgram> spb$setWorldBorderShader(Supplier<ShaderProgram> supplier) {
        return () -> {
            ShaderProgram worldborder = VeilVanillaShaders.getWorldborder();
            return this.spb$worldborderCache.getShader(worldborder != null ? worldborder : supplier.get());
        };
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V", shift = At.Shift.BEFORE))
    private void spb$endOpaque(CallbackInfo ci) {
        DeferredRenderer.get().end();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;long2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;", shift = At.Shift.BEFORE, remap = false))
    private void spb$beginTranslucent(CallbackInfo ci) {
        DeferredRenderer.get().beginTranslucent();
    }

    // This makes sure the breaking texture is drawn into the opaque buffer
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V", ordinal = 0, shift = At.Shift.BEFORE))
    private void spb$preDrawCrumblingOpaque(CallbackInfo ci) {
        DeferredRenderer.get().beginOpaque();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V", ordinal = 0, shift = At.Shift.AFTER))
    private void spb$postDrawCrumblingOpaque(CallbackInfo ci) {
        DeferredRenderer.get().beginTranslucent();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void spb$blit(CallbackInfo ci) {
        DeferredRenderer.get().blit();
    }

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
