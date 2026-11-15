// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light.renderer;

import net.minecraft.client.render.BuiltBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderSystem;
import com.sp.render.deferred.light.AreaLight;
import com.sp.render.deferred.light.renderer.InstancedLightRenderer;
import com.sp.render.deferred.light.renderer.LightRenderer;
import com.sp.render.deferred.light.renderer.LightTypeRenderer;
import com.sp.render.deferred.DeferredShaders;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.List;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

@ApiStatus.Internal
public class AreaLightRenderer extends InstancedLightRenderer<AreaLight> {

    public AreaLightRenderer() {
        // SPB: 23 floats, matching AreaLight#store (was AreaLightRendererMixin)
        super(Float.BYTES * 23);
    }

    @Override
    protected BuiltBuffer createMesh() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION);
        LightTypeRenderer.createInvertedCube(bufferBuilder);
        return bufferBuilder.end();
    }

    @Override
    protected void setupBufferState() {
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(5);
        glEnableVertexAttribArray(6);
        glEnableVertexAttribArray(7);
        glEnableVertexAttribArray(8);

        glVertexAttribPointer(1, 4, GL_FLOAT, false, this.lightSize, 0);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, this.lightSize, Float.BYTES * 4);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, this.lightSize, Float.BYTES * 8);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, this.lightSize, Float.BYTES * 12); // matrix !

        glVertexAttribPointer(5, 3, GL_FLOAT, false, this.lightSize, Float.BYTES * 16); // color
        glVertexAttribPointer(6, 2, GL_FLOAT, false, this.lightSize, Float.BYTES * 19); // size
        // SPB: angle is a float and distance follows it (was AreaLightRendererMixin)
        glVertexAttribPointer(7, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 21); // angle
        glVertexAttribPointer(8, 1, GL_FLOAT, false, this.lightSize, Float.BYTES * 22); // distance

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);
        glVertexAttribDivisor(5, 1);
        glVertexAttribDivisor(6, 1);
        glVertexAttribDivisor(7, 1);
        glVertexAttribDivisor(8, 1);
    }

    @Override
    protected void setupRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<AreaLight> lights) {
        VeilRenderSystem.setShader(DeferredShaders.LIGHT_AREA);
    }

    @Override
    protected void clearRenderState(@NotNull LightRenderer lightRenderer, @NotNull List<AreaLight> lights) {
    }

    // the bounding box here isn't particularly tight, but it should always encapsulate the light's area.
    @Override
    protected boolean isVisible(AreaLight light, CullFrustum frustum) {
        Vector2f size = light.getSize();
        float distance = light.getDistance();
        Vector3d position = light.getPosition();
        float radius = Math.max(size.x, size.y) + distance;
        double minX = position.x - radius;
        double minY = position.y - radius;
        double minZ = position.z - radius;
        double maxX = position.x + radius;
        double maxY = position.y + radius;
        double maxZ = position.z + radius;
        return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
