// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light.renderer;

import net.minecraft.client.render.BuiltBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import foundry.veil.api.client.render.VeilRenderSystem;
import com.sp.render.deferred.light.renderer.LightRenderer;
import com.sp.render.deferred.light.renderer.LightTypeRenderer;
import com.sp.render.deferred.DeferredShaders;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.util.math.Direction;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

@ApiStatus.Internal
public class VanillaLightRenderer implements NativeResource {

    private final VertexBuffer vbo;

    public VanillaLightRenderer() {
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.vbo.bind();
        this.vbo.upload(createMesh());
        VertexBuffer.unbind();
    }

    private static BuiltBuffer createMesh() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION);
        LightTypeRenderer.createQuad(bufferBuilder);
        return bufferBuilder.end();
    }

    public void render(LightRenderer lightRenderer, ClientWorld level) {
        VeilRenderSystem.setShader(DeferredShaders.LIGHT_VANILLA_LIGHTMAP);
        lightRenderer.applyShader();

        ShaderProgram shader = VeilRenderSystem.getShader();
        if (shader == null) {
            return;
        }

        for (Direction direction : Direction.values()) {
            shader.getUniformSafe("LightShading" + direction.ordinal()).setFloat(level.getBrightness(direction, true));
        }

        this.vbo.bind();
        this.vbo.draw();
        VertexBuffer.unbind();
    }

    @Override
    public void free() {
        this.vbo.close();
    }
}
