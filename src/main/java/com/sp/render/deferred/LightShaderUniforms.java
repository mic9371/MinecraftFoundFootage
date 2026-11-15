package com.sp.render.deferred;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sp.SPBRevampedClient;
import com.sp.init.BackroomsLevels;
import com.sp.render.ShadowMapRenderer;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Per-frame uniforms for this mod's light shaders (shadows in Level 0). Was LightRendererMixin.
 */
public final class LightShaderUniforms {
    private LightShaderUniforms() {
    }

    public static void apply(ShaderProgram shader) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player != null && client.world != null) {
            RegistryKey<World> registryKey = player.getWorld().getRegistryKey();
            if (registryKey == BackroomsLevels.LEVEL0_WORLD_KEY && !SPBRevampedClient.getCutsceneManager().isPlaying) {
                setShadowUniforms(shader);
                shader.getUniformSafe("InOverWorld").setInt(registryKey == World.OVERWORLD ? 1 : 0);
                shader.getUniformSafe("ShouldRender").setInt(1);
            } else {
                shader.getUniformSafe("InOverWorld").setInt(registryKey == World.OVERWORLD ? 1 : 0);
                shader.getUniformSafe("ShouldRender").setInt(0);
            }
        }
        shader.getUniformSafe("gameTime").setFloat(RenderSystem.getShaderGameTime());
    }

    private static void setShadowUniforms(ShaderProgram shaderProgram) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        shaderProgram.getUniformSafe("viewMatrix").setMatrix(ShadowMapRenderer.createShadowModelView(camera.getPos().x, camera.getPos().y, camera.getPos().z, true).peek().getPositionMatrix());
        shaderProgram.getUniformSafe("orthographMatrix").setMatrix(ShadowMapRenderer.createProjMat());
    }
}
