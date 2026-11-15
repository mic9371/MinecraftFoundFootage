package com.sp.mixin;

import com.sp.SPBRevampedClient;
import com.sp.render.PoolroomsDayCycle;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.render.shader.program.ShaderProgramImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.Window;
import net.minecraft.screen.PlayerScreenHandler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds this mod's extra per-draw uniforms to every Veil shader program.
 * In 1.20.1 this overwrote Veil's MutableUniformAccess#applyRenderSystem; since Veil for 1.21.1 sets default uniforms
 * through vanilla's ShaderProgram#initializeUniforms on its program wrapper, the extras are added there.
 */
@Mixin(net.minecraft.client.gl.ShaderProgram.class)
public class ShaderProgramUniformsMixin {

    @Inject(method = "initializeUniforms", at = @At("TAIL"))
    private void spb$applyExtraUniforms(VertexFormat.DrawMode drawMode, Matrix4f viewMatrix, Matrix4f projectionMatrix, Window window, CallbackInfo ci) {
        if (!((Object) this instanceof ShaderProgramImpl.Wrapper wrapper)) {
            return;
        }
        ShaderProgram program = wrapper.program();

        if (SPBRevampedClient.cameraBobOffset != null) {
            program.getUniformSafe("cameraBobOffset").setVector(SPBRevampedClient.cameraBobOffset);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        program.getUniformSafe("ScreenSize").setVector(window.getWidth(), window.getHeight());

        SpriteAtlasTexture texture = client.getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        if (texture != null) {
            program.getUniformSafe("atlasAspectRatio").setFloat((float) texture.getHeight() / texture.getWidth());
        }

        if (client.world != null && SPBRevampedClient.camera != null) {
            program.getUniformSafe("sunsetTimer").setFloat(PoolroomsDayCycle.getDayTime(client.world));
            SPBRevampedClient.setShadowUniforms(program, client.world);

            program.getUniformSafe("warpAngle").setFloat(SPBRevampedClient.getWarpTimer(client.world));
        }
    }
}
