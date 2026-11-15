// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light.renderer;

import net.minecraft.client.render.BuiltBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderSystem;
import com.sp.render.deferred.light.DirectionalLight;
import com.sp.render.deferred.light.renderer.LightRenderer;
import com.sp.render.deferred.light.renderer.LightTypeRenderer;
import com.sp.render.deferred.DeferredShaders;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Set;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;

@ApiStatus.Internal
public class DirectionalLightRenderer implements LightTypeRenderer<DirectionalLight> {

    private final VertexBuffer vbo;
    private int visibleLights;

    public DirectionalLightRenderer() {
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

    @Override
    public void prepareLights(LightRenderer lightRenderer, List<DirectionalLight> lights, Set<DirectionalLight> removedLights, CullFrustum frustum) {
    }

    @Override
    public void renderLights(LightRenderer lightRenderer, List<DirectionalLight> lights) {
        this.visibleLights = lights.size();
        if (lights.isEmpty()) {
            return;
        }

        VeilRenderSystem.setShader(DeferredShaders.LIGHT_DIRECTIONAL);
        lightRenderer.applyShader();

        ShaderProgram shader = VeilRenderSystem.getShader();
        if (shader == null) {
            return;
        }

        this.vbo.bind();
        for (DirectionalLight light : lights) {
            Vector3fc lightColor = light.getColor();
            float brightness = light.getBrightness();
            shader.getUniformSafe("LightColor").setVector(lightColor.x() * brightness, lightColor.y() * brightness, lightColor.z() * brightness);
            shader.getUniformSafe("LightDirection").setVector(light.getDirection());
            this.vbo.draw();
        }

        VertexBuffer.unbind();
    }

    @Override
    public int getVisibleLights() {
        return this.visibleLights;
    }

    @Override
    public void free() {
        this.vbo.close();
    }
}
