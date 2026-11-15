// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred.light;

import com.google.common.base.Suppliers;
import com.sp.render.deferred.light.renderer.AreaLightRenderer;
import com.sp.render.deferred.light.renderer.DirectionalLightRenderer;
import com.sp.render.deferred.light.renderer.IndirectLightRenderer;
import com.sp.render.deferred.light.renderer.IndirectPointLightRenderer;
import com.sp.render.deferred.light.renderer.InstancedPointLightRenderer;
import com.sp.render.deferred.light.renderer.LightTypeRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * All light types the deferred renderer can draw.
 * Veil's version was a vanilla registry; nothing else registers light types here, so plain constants are enough.
 */
public final class LightTypeRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("SPBRevamped/LightTypeRegistry");

    public static final Supplier<LightType<DirectionalLight>> DIRECTIONAL = register("directional", DirectionalLightRenderer::new);
    public static final Supplier<LightType<PointLight>> POINT = register("point", () -> {
        boolean supported = IndirectLightRenderer.isSupported();
        if (supported) {
            LOGGER.info("Using Indirect Point Light Renderer");
            return new IndirectPointLightRenderer();
        } else {
            LOGGER.info("Using Instanced Point Light Renderer");
            return new InstancedPointLightRenderer();
        }
    });
    public static final Supplier<LightType<AreaLight>> AREA = register("area", AreaLightRenderer::new);

    private LightTypeRegistry() {
    }

    private static <T extends Light> Supplier<LightType<T>> register(String name, RendererFactory<T> factory) {
        return Suppliers.memoize(() -> new LightType<>(name, factory));
    }

    public record LightType<T extends Light>(String name, RendererFactory<T> rendererFactory) {
    }

    /**
     * Creates the renderer for lights when requested.
     *
     * @param <T> The type of light the renderer needs to draw
     */
    @FunctionalInterface
    public interface RendererFactory<T extends Light> {

        /**
         * @return A new renderer for lights
         */
        LightTypeRenderer<T> createRenderer();
    }
}
