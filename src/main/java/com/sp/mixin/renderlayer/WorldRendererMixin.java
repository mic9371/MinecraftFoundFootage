package com.sp.mixin.renderlayer;

import net.minecraft.client.render.RenderTickCounter;
import com.sp.render.RenderLayers;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow protected abstract void renderLayer(RenderLayer renderLayer, double x, double y, double z, Matrix4f viewMatrix, Matrix4f positionMatrix);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", ordinal = 0))
    private void renderRenderLayer(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f viewMatrix, Matrix4f projectionMatrix, CallbackInfo ci){
        Vec3d cameraPos = camera.getPos();
        this.renderLayer(RenderLayers.getPoolroomsSky(), cameraPos.getX(), cameraPos.getY(), cameraPos.getZ(), viewMatrix, projectionMatrix);
        this.renderLayer(RenderLayers.getPbrLayer(), cameraPos.getX(), cameraPos.getY(), cameraPos.getZ(), viewMatrix, projectionMatrix);
    }

}
