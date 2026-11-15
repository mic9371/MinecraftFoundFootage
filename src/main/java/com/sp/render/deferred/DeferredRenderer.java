// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sp.SPBRevamped;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.ShaderSourceSet;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import foundry.veil.api.client.render.VeilRenderer;
import com.sp.render.deferred.light.renderer.LightRenderer;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.ShaderPreDefinitions;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;

/**
 * <p>Handles mixing the regular deferred pipeline and the forward-rendered transparency pipeline.</p>
 * <p>The rendering pipeline goes in this order:</p>
 * <ul>
 *     <li>Opaque Shaders</li>
 *     <li>Opaque post-processing ({@link DeferredRenderer#OPAQUE_POST})</li>
 *     <li>Light Shaders via {@link LightRenderer}</li>
 *     <li>Light post-processing ({@link DeferredRenderer#LIGHT_POST})</li>
 *     <li>Transparency Shaders</li>
 *     <li>Transparency post-processing ({@link DeferredRenderer#TRANSPARENT_POST})</li>
 *     <li>Light Shaders via {@link LightRenderer}</li>
 *     <li>Light post-processing ({@link DeferredRenderer#LIGHT_POST})</li>
 *     <li>Final image compositing</li>
 *     <li>Final post-processing via {@link PostProcessingManager}</li>
 * </ul>
 *
 * @author Ocelot
 */
public class DeferredRenderer implements ResourceReloader, NativeResource {

    public static final String DISABLE_VANILLA_ENTITY_LIGHT_KEY = "DISABLE_VANILLA_ENTITY_LIGHT";

    /** Shaders in {@code assets/<namespace>/pinwheel/shaders/deferred/} replace the vanilla shader with the same id while deferred rendering is active. */
    public static final ShaderSourceSet DEFERRED_SET = new ShaderSourceSet("pinwheel/shaders/deferred");

    // Framebuffers keep the ids Veil 1.20.1 used, since all of this mod's shaders sample them by these names.
    public static final Identifier OPAQUE = Identifier.of("veil", "opaque");
    public static final Identifier OPAQUE_FINAL = Identifier.of("veil", "opaque_final");
    public static final Identifier TRANSPARENT = Identifier.of("veil", "transparent");
    public static final Identifier TRANSPARENT_FINAL = Identifier.of("veil", "transparent_final");
    public static final Identifier OPAQUE_LIGHT = Identifier.of("veil", "opaque_light");
    public static final Identifier TRANSPARENT_LIGHT = Identifier.of("veil", "transparent_light");
    /** Composite target (Veil 1.20.1's "veil:post" definition: RGBA16F with depth); Veil 1.21.1's own veil:post has no depth. */
    public static final Identifier DEFERRED_POST = path("deferred_post");

    public static final Identifier OPAQUE_POST = path("deferred/opaque");
    public static final Identifier LIGHT_POST = path("deferred/light");
    public static final Identifier OPAQUE_MIX = path("deferred/mix_opaque");
    public static final Identifier TRANSPARENT_MIX = path("deferred/mix_transparent");
    public static final Identifier TRANSPARENT_POST = path("deferred/transparent");
    public static final Identifier SCREEN_POST = path("deferred/screen");

    private static final Logger LOGGER = LoggerFactory.getLogger("SPBRevamped/DeferredRenderer");
    private static DeferredRenderer instance;

    private final ShaderManager deferredShaderManager;
    private final ShaderPreDefinitions shaderPreDefinitions;
    private final FramebufferManager framebufferManager;
    private final PostProcessingManager postProcessingManager;
    private final LightRenderer lightRenderer;

    private boolean enabled;
    private RendererState state;

    private DeferredRenderer(ShaderManager deferredShaderManager, ShaderPreDefinitions shaderPreDefinitions, FramebufferManager framebufferManager, PostProcessingManager postProcessingManager) {
        this.deferredShaderManager = deferredShaderManager;
        this.shaderPreDefinitions = shaderPreDefinitions;
        this.framebufferManager = framebufferManager;
        this.postProcessingManager = postProcessingManager;
        this.lightRenderer = new LightRenderer();

        this.enabled = false;
        this.state = RendererState.INACTIVE;
    }

    private static Identifier path(String path) {
        return Identifier.of(SPBRevamped.MOD_ID, path);
    }

    /**
     * Creates the deferred renderer. Called once Veil's renderer exists, which is before the first resource reload.
     */
    @ApiStatus.Internal
    public static void init(VeilRenderer renderer) {
        if (instance != null) {
            return;
        }

        ShaderManager deferredShaderManager = new ShaderManager(DEFERRED_SET, renderer.getShaderDefinitions(), renderer.getDynamicBufferManger());
        instance = new DeferredRenderer(deferredShaderManager, renderer.getShaderDefinitions(), renderer.getFramebufferManager(), renderer.getPostProcessingManager());
        if (MinecraftClient.getInstance().getResourceManager() instanceof ReloadableResourceManagerImpl resourceManager) {
            resourceManager.registerReloader(instance);
        } else {
            LOGGER.error("Unable to register the deferred renderer for resource reloads");
        }
    }

    /**
     * @return The deferred renderer. Only valid on the client after Veil has initialized.
     */
    public static DeferredRenderer get() {
        return instance;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer preparationBarrier, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.<CompletableFuture<Void>>supplyAsync(() -> {
            // The deferred shaders ship with this mod, so the renderer is always selected (Veil 1.20.1 required its "veil:deferred" pack).
            boolean active = isSupported();
            if (this.enabled != active) {
                this.enabled = active;
                if (active) {
                    LOGGER.info("Deferred Renderer Enabled");
                } else {
                    LOGGER.info("Deferred Renderer Disabled");
                    return preparationBarrier.whenPrepared(null).thenRunAsync(this::free, gameExecutor);
                }
            }

            if (this.enabled) {
                return this.deferredShaderManager.reload(preparationBarrier, resourceManager, prepareProfiler, applyProfiler, backgroundExecutor, gameExecutor);
            }

            return preparationBarrier.whenPrepared(null);
        }, gameExecutor).thenCompose(future -> future);
    }

    @Override
    public void free() {
        this.enabled = false;
        this.state = RendererState.INACTIVE;
        this.deferredShaderManager.close();
        this.lightRenderer.free();
    }

    @ApiStatus.Internal
    public void reset() {
        this.lightRenderer.free();
    }

    @ApiStatus.Internal
    public void setup() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        switch (this.state) {
            case OPAQUE -> {
                AdvancedFbo deferredFramebuffer = this.framebufferManager.getFramebuffer(OPAQUE);
                if (deferredFramebuffer == null) {
                    LOGGER.error("Missing deferred opaque buffer");
                    this.free();
                    return;
                }

                deferredFramebuffer.bind(true);
            }
            case TRANSLUCENT -> {
                AdvancedFbo transparent = this.framebufferManager.getFramebuffer(TRANSPARENT);
                if (transparent == null) {
                    LOGGER.error("Missing deferred transparent buffer");
                    this.free();
                    return;
                }

                transparent.bind(true);
            }
        }

        // Temporary hack until we blit
//        deferredFramebuffer.bindDraw(false);
//        glDrawBuffers(GL_COLOR_ATTACHMENT0);
//        AdvancedFbo.getMainFramebuffer().resolveToAdvancedFbo(deferredFramebuffer);
//        deferredFramebuffer.bind(true);
//        glDrawBuffers(deferredFramebuffer.getDrawBuffers());
    }

    @ApiStatus.Internal
    public void clear() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        switch (this.state) {
            case OPAQUE, TRANSLUCENT -> AdvancedFbo.unbind();
        }
    }

    @ApiStatus.Internal
    public void beginOpaque() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        this.state = RendererState.OPAQUE;
        AdvancedFbo deferred = this.framebufferManager.getFramebuffer(OPAQUE);
        if (deferred == null) {
            LOGGER.error("Missing deferred opaque buffer");
            this.free();
        }
    }

    @ApiStatus.Internal
    public void beginTranslucent() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        this.state = RendererState.TRANSLUCENT;
        AdvancedFbo transparent = this.framebufferManager.getFramebuffer(TRANSPARENT);
        if (transparent == null) {
            LOGGER.error("Missing deferred transparent buffer");
            this.free();
            return;
        }

        // Copy opaque depth to transparency, so it doesn't draw on top
        AdvancedFbo deferredFramebuffer = this.framebufferManager.getFramebuffer(OPAQUE);
        if (deferredFramebuffer != null) {
            deferredFramebuffer.resolveToAdvancedFbo(transparent, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
        }
    }

    private void run(Profiler profiler, AdvancedFbo deferred, AdvancedFbo light, Identifier post, Identifier mix) {
        PostPipeline postPipeline = this.postProcessingManager.getPipeline(post);
        if (postPipeline != null) {
            profiler.push("post");
            this.postProcessingManager.runPipeline(postPipeline, false);
            profiler.pop();
        }

        profiler.push("draw_lights");
        light.bind(true);
        this.lightRenderer.render(deferred);
        profiler.pop();

        // Applies effects to the final light image
        PostPipeline lightPipeline = this.postProcessingManager.getPipeline(LIGHT_POST);
        if (lightPipeline != null) {
            profiler.push("light_post");
            this.postProcessingManager.runPipeline(lightPipeline, false);
            profiler.pop();
        }

        // Applies light to the image
        PostPipeline mixPipeline = this.postProcessingManager.getPipeline(mix);
        if (mixPipeline != null) {
            profiler.push("mix");
            this.postProcessingManager.runPipeline(mixPipeline, false);
            profiler.pop();
        }
    }

    @ApiStatus.Internal
    public void blit() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("veil_deferred");

        this.end();

        AdvancedFbo deferred = this.framebufferManager.getFramebuffer(OPAQUE);
        AdvancedFbo transparent = this.framebufferManager.getFramebuffer(TRANSPARENT);
        AdvancedFbo deferredLight = this.framebufferManager.getFramebuffer(OPAQUE_LIGHT);
        AdvancedFbo transparentLight = this.framebufferManager.getFramebuffer(TRANSPARENT_LIGHT);
        AdvancedFbo post = this.framebufferManager.getFramebuffer(DEFERRED_POST);
        if (deferred == null || transparent == null || deferredLight == null || transparentLight == null || post == null) {
            LOGGER.error("Missing deferred light buffers");
            this.free();
            return;
        }

        profiler.push("setup_lights");
        this.lightRenderer.setup(VeilRenderSystem.getCullingFrustum());
        profiler.swap("opaque_light");
        this.run(profiler, deferred, deferredLight, OPAQUE_POST, OPAQUE_MIX);
        profiler.swap("transparent_light");
        this.run(profiler, transparent, transparentLight, TRANSPARENT_POST, TRANSPARENT_MIX);
        profiler.pop();
        this.lightRenderer.clear();

        profiler.push("screen_post");

        // Draws the final opaque image and transparent onto the background
        PostPipeline screenPipeline = this.postProcessingManager.getPipeline(SCREEN_POST);
        if (screenPipeline != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.postProcessingManager.runPipeline(screenPipeline, false);
            RenderSystem.disableBlend();
        }

        profiler.swap("resolve");
        post.resolveToRenderTarget(MinecraftClient.getInstance().getFramebuffer(), GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST);
        profiler.pop();

        profiler.pop();
    }

    @ApiStatus.Internal
    public void end() {
        if (!this.isEnabled() || this.state == RendererState.DISABLED) {
            return;
        }

        this.state = RendererState.INACTIVE;
    }

    @ApiStatus.Internal
    public void addDebugInfo(Consumer<String> consumer) {
        boolean ambientOcclusion = this.lightRenderer.isAmbientOcclusionEnabled();
        boolean vanillaLights = this.lightRenderer.isVanillaLightEnabled();
        boolean vanillaEntityLights = this.shaderPreDefinitions.getDefinition(DISABLE_VANILLA_ENTITY_LIGHT_KEY) == null;
        consumer.accept("Ambient Occlusion: " + (ambientOcclusion ? Formatting.GREEN + "On" : Formatting.RED + "Off"));
        consumer.accept("Vanilla Light: " + (vanillaLights ? Formatting.GREEN + "On" : Formatting.RED + "Off"));
        consumer.accept("Vanilla Entity Light: " + (vanillaEntityLights ? Formatting.GREEN + "On" : Formatting.RED + "Off"));
        this.lightRenderer.addDebugInfo(consumer);
    }

    /**
     * Allows the renderer to run normally.
     */
    public void enable() {
        this.state = RendererState.INACTIVE;
    }

    /**
     * Forces the renderer off.
     */
    public void disable() {
        this.state = RendererState.DISABLED;
    }

    /**
     * @return Whether the deferred renderer is initialized and ready to use
     */
    public boolean isEnabled() {
        return this.enabled && isSupported();
    }

    /**
     * @return Whether the deferred rendering pipeline is supported
     */
    public static boolean isSupported() {
        return !MinecraftClient.isFabulousGraphicsOrBetter() && !com.sp.compat.SodiumCompat.PRESENT;
    }

    /**
     * @return Whether the deferred renderer is currently actively being used
     */
    public boolean isActive() {
        return this.isEnabled() && this.state.isActive();
    }

    public LightRenderer getLightRenderer() {
        return this.lightRenderer;
    }

    public RendererState getRendererState() {
        return this.state;
    }

    public ShaderManager getDeferredShaderManager() {
        return this.deferredShaderManager;
    }

    public enum RendererState {
        DISABLED, INACTIVE, OPAQUE, TRANSLUCENT;

        public boolean isActive() {
            return this == OPAQUE || this == TRANSLUCENT;
        }
    }
}
