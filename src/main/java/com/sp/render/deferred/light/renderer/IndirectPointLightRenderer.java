// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light.renderer;

import net.minecraft.client.render.BuiltBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import com.sp.render.deferred.light.PointLight;
import com.sp.render.deferred.light.renderer.IndirectLightRenderer;
import com.sp.render.deferred.light.renderer.LightRenderer;
import com.sp.render.deferred.light.renderer.LightTypeRenderer;
import com.sp.render.deferred.DeferredShaders;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

@ApiStatus.Internal
public class IndirectPointLightRenderer extends IndirectLightRenderer<PointLight> {

    public IndirectPointLightRenderer() {
        super(Float.BYTES * 7, 4, 0, 6);
    }

    @Override
    protected BuiltBuffer createMesh() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION);

        // High-res mesh
        LightTypeRenderer.createInvertedCube(bufferBuilder);

        // Low-res mesh
        float sqrt2 = (float) Math.sqrt(2.0);
        bufferBuilder.vertex(-sqrt2, -sqrt2, 0);
        bufferBuilder.vertex(sqrt2, -sqrt2, 0);
        bufferBuilder.vertex(-sqrt2, sqrt2, 0);
        bufferBuilder.vertex(sqrt2, sqrt2, 0);

        return bufferBuilder.end();
    }

    @Override
    protected void setupBufferState() {
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4); // SPB (was PointLightRendererMixin)

        glVertexAttribPointer(1, 3, GL_FLOAT, false, this.lightSize, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, this.lightSize, Float.BYTES * 3);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 6);
        glVertexAttribPointer(4, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 7); // SPB

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1); // SPB
    }

    @Override
    protected void setupRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
        VeilRenderSystem.setShader(DeferredShaders.LIGHT_POINT);
    }

    @Override
    protected void clearRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<PointLight> lights) {
    }
}
