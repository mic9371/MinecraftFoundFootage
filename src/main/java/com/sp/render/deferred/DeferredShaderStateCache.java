// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.render.shader.program.ShaderProgramImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Looks up the deferred variant of a shader (by id, in {@link DeferredRenderer#DEFERRED_SET}) while deferred rendering is active.
 */
@ApiStatus.Internal
public class DeferredShaderStateCache {

    private static final Logger LOGGER = LoggerFactory.getLogger("SPBRevamped/DeferredShaderStateCache");

    private Object veil$oldShader;
    private ShaderProgram veil$deferredShader;
    private boolean printedError;

    /**
     * Sets up the render state for the specified vanilla shader instance.
     *
     * @param shaderInstance The shader to set the render state for
     * @return Whether the state was handled and set
     */
    public boolean setupRenderState(@Nullable net.minecraft.client.gl.ShaderProgram shaderInstance) {
        net.minecraft.client.gl.ShaderProgram shader = this.getShader(shaderInstance);
        if (shader != shaderInstance) {
            if (shader instanceof ShaderProgramImpl.Wrapper wrapper) {
                VeilRenderSystem.setShader(wrapper.program());
            } else {
                RenderSystem.setShader(() -> shader);
            }
            return true;
        }
        return false;
    }

    /**
     * Sets up the render state for the specified Veil shader program.
     *
     * @param shaderInstance The shader to set the render state for
     * @return Whether the state was handled and set
     */
    public boolean setupRenderState(@Nullable ShaderProgram shaderInstance) {
        ShaderProgram shader = this.getShader(shaderInstance);
        if (shader != shaderInstance) {
            VeilRenderSystem.setShader(shader);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the shader that should be used if using deferred rendering
     *
     * @param shaderInstance The shader to get the render state for
     * @return The shader to use
     */
    @Contract("null -> null")
    public net.minecraft.client.gl.ShaderProgram getShader(@Nullable net.minecraft.client.gl.ShaderProgram shaderInstance) {
        DeferredRenderer deferredRenderer = DeferredRenderer.get();
        if (shaderInstance == null || deferredRenderer == null || !deferredRenderer.isActive()) {
            this.veil$oldShader = null;
            this.veil$deferredShader = null;
            return shaderInstance;
        }

        if (!Objects.equals(this.veil$oldShader, shaderInstance)) {
            this.veil$oldShader = shaderInstance;
            Identifier id = Identifier.tryParse(shaderInstance.getName());
            this.veil$deferredShader = id != null ? deferredRenderer.getDeferredShaderManager().getShader(id) : null;

            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                if (this.veil$deferredShader != null) {
                    this.printedError = false;
                } else {
                    if (!this.printedError) {
                        LOGGER.warn("Failed to find deferred shader for vanilla shader: {}", shaderInstance.getName());
                        this.printedError = true;
                    }
                }
            }
        }

        return this.veil$deferredShader != null ? VeilRenderBridge.toShaderInstance(this.veil$deferredShader) : shaderInstance;
    }

    /**
     * Retrieves the shader that should be used if using deferred rendering
     *
     * @param shaderProgram The shader to get the render state for
     * @return The shader to use
     */
    @Contract("null -> null")
    public ShaderProgram getShader(@Nullable ShaderProgram shaderProgram) {
        DeferredRenderer deferredRenderer = DeferredRenderer.get();
        if (shaderProgram == null || deferredRenderer == null || !deferredRenderer.isActive()) {
            this.veil$oldShader = null;
            this.veil$deferredShader = null;
            return shaderProgram;
        }

        if (!Objects.equals(this.veil$oldShader, shaderProgram)) {
            this.veil$oldShader = shaderProgram;
            this.veil$deferredShader = deferredRenderer.getDeferredShaderManager().getShader(shaderProgram.getName());

            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                if (this.veil$deferredShader != null) {
                    this.printedError = false;
                } else {
                    if (!this.printedError) {
                        LOGGER.warn("Failed to find deferred shader for veil shader: {}", shaderProgram.getName());
                        this.printedError = true;
                    }
                }
            }
        }

        return Objects.requireNonNullElse(this.veil$deferredShader, shaderProgram);
    }
}
