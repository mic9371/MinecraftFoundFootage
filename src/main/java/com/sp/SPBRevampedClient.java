package com.sp;

import com.sp.networking.ClientPacketHandlers;
import com.sp.block.client.renderer.FluorescentLightBlockEntityRenderer;
import com.sp.block.client.renderer.ThinFluorescentLightBlockEntityRenderer;
import com.sp.block.client.renderer.TinyFluorescentLightBlockEntityRenderer;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.WorldEvents;
import com.sp.compat.modmenu.ConfigDefinitions;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.entity.client.model.SmilerModel;
import com.sp.entity.client.renderer.SkinWalkerRenderer;
import com.sp.entity.client.renderer.SmilerRenderer;
import com.sp.entity.client.renderer.WalkerRenderer;
import com.sp.init.*;
import com.sp.networking.InitializePackets;
import com.sp.networking.callbacks.ClientConnectionEvents;
import com.sp.render.*;
import com.sp.render.bird.BirdQuality;
import com.sp.render.bird.BirdRenderer;
import com.sp.render.bird.FlockManager;
import com.sp.render.camera.CameraShake;
import com.sp.render.camera.CutsceneManager;
import com.sp.render.grass.GrassRenderer;
import com.sp.render.gui.StaminaBar;
import com.sp.render.gui.TitleText;
import com.sp.render.pbr.BlockIdMap;
import com.sp.render.pbr.PbrRegistry;
import com.sp.util.MathStuff;
import com.sp.util.TickTimer;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.custom.InfiniteGrassBackroomsLevel;
import com.sp.world.levels.custom.Level2BackroomsLevel;
import com.sp.world.levels.custom.PoolroomsBackroomsLevel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import com.sp.render.deferred.DeferredRenderer;
import com.sp.render.deferred.light.renderer.LightRenderer;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.ShaderPreDefinitions;
import foundry.veil.api.client.render.shader.program.MutableUniformAccess;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilRenderLevelStageEvent.Stage;
import foundry.veil.platform.VeilEventPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import com.sp.networking.C2S.SeeActiveSkinwalkerSync;
import com.sp.networking.C2S.SyncServerComponent;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.*;

import java.util.List;
import java.util.Optional;
import java.util.Vector;


public class SPBRevampedClient implements ClientModInitializer {
    private GrassRenderer grassRenderer;
    private BirdRenderer birdRenderer;
    private static final CutsceneManager cutsceneManager = new CutsceneManager();
    private static final CameraShake cameraShake = new CameraShake();
    private final FlashlightRenderer flashlightRenderer = new FlashlightRenderer();

    private static final Identifier VHS_POST = Identifier.of(SPBRevamped.MOD_ID, "vhs");

    private static final Identifier SSAO = Identifier.of(SPBRevamped.MOD_ID, "vhs/ssao");
    private static final Identifier EVERYTHING_SHADER = Identifier.of(SPBRevamped.MOD_ID, "vhs/everything");
    private static final Identifier POST_VHS = Identifier.of(SPBRevamped.MOD_ID, "vhs/vhs_post");
    private static final Identifier MIXED_SHADER = Identifier.of(SPBRevamped.MOD_ID, "vhs/mixed");
    private static final Identifier GLITCH_SHADER = Identifier.of(SPBRevamped.MOD_ID, "vhs/glitch");

    static boolean inBackrooms = false;
    public static boolean isLightning = false;
    public static Camera camera;
    public static Vector3f cameraBobOffset;

    public static TickTimer tickTimer = new TickTimer();
    public static boolean blackScreen;
    public static boolean youCantEscape;

    private static boolean shouldBeUnmuted = false;

    private static final Random random = Random.create();
    private static final Random random2 = Random.create(34563264);

    public static boolean shouldRenderWarp = false;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(new TitleText());
        HudRenderCallback.EVENT.register(new StaminaBar());

        ClientPacketHandlers.register();

        ModKeyBinds.initializeKeyBinds();

        FlockManager.init();

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POOLROOMS_SKY_BLOCK, com.sp.compat.SodiumCompat.PRESENT ? RenderLayer.getSolid() : RenderLayers.getPoolroomsSky());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BOTTOM_TRIM, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_3, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_4, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_5, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_6, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_7, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_8, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_99, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_3, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_4, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_SMALL_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_SMALL_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_DRAWING_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_DRAWING_WINDOW, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RUG_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RUG_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RED_METAL_CASING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WINDOW, RenderLayer.getTranslucent());

        BlockEntityRendererFactories.register(ModBlockEntities.FLUORESCENT_LIGHT_BLOCK_ENTITY, FluorescentLightBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.THIN_FLUORESCENT_LIGHT_BLOCK_ENTITY, ThinFluorescentLightBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.TINY_FLUORESCENT_LIGHT_BLOCK_ENTITY, TinyFluorescentLightBlockEntityRenderer::new);

        EntityRendererRegistry.register(ModEntities.SKIN_WALKER_ENTITY, SkinWalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.WALKER_ENTITY, WalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.SMILER_ENTITY, SmilerRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.SMILER, SmilerModel::getTexturedModelData);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of(SPBRevamped.MOD_ID, "after_resources");
            }

            @Override
            public void reload(ResourceManager manager) {
                BlockIdMap.registerBlockID((blockIdMap) -> {
                    blockIdMap.put(ModBlocks.POOL_TILES, 18);
                    blockIdMap.put(ModBlocks.CEILINGLIGHT, 15);
                    blockIdMap.put(ModBlocks.EMERGENCY_LIGHT, 19);
                    blockIdMap.put(ModBlocks.POWER_POLE, 20);
                    blockIdMap.put(ModBlocks.PLASTIC, 22);
                    blockIdMap.put(ModBlocks.PILLAR, 22);
                    blockIdMap.put(ModBlocks.RED_METAL_CASING, 23);
                    blockIdMap.put(ModBlocks.POLE, 24);
                    blockIdMap.put(ModBlocks.WINDOW, 25);
                    blockIdMap.put(ModBlocks.FLOOR_TILING, 26);
                });

                PbrRegistry.registerPBR(ModBlocks.CARPET_BLOCK,      new PbrRegistry.PbrMaterial(false, 0.0f,1.25f, 1024));
                PbrRegistry.registerPBR(ModBlocks.CEILING_TILE,      new PbrRegistry.PbrMaterial(false, 0.0f,1.0f,  512));
                PbrRegistry.registerPBR(ModBlocks.GHOST_CEILING_TILE,new PbrRegistry.PbrMaterial(false, 0.0f,1.0f,  512));

                PbrRegistry.registerPBR(ModBlocks.CONCRETE_BLOCK_11, new PbrRegistry.PbrMaterial(true, 0.4f,8.0f,  1024));
                PbrRegistry.registerPBR(ModBlocks.BRICKS,            new PbrRegistry.PbrMaterial(true, 0.45f,5.0f,   2048));
                //PbrRegistry.registerPBR(ModBlocks.DIRT,             new PbrRegistry.PbrMaterial(true, 0.5f,3.0f,   128));
                PbrRegistry.registerPBR(ModBlocks.CHAINFENCE,        new PbrRegistry.PbrMaterial(true, 0.21f,2.8f,   1024));
                PbrRegistry.registerPBR(ModBlocks.WOODEN_CRATE,      new PbrRegistry.PbrMaterial(true, 0.9f, 1.0f,   1024));
                PbrRegistry.registerPBR(ModBlocks.ROAD,              new PbrRegistry.PbrMaterial(true, 0.37f,8.0f,   1024));
                PbrRegistry.registerPBR(ModBlocks.FLOOR_TILING,      new PbrRegistry.PbrMaterial(true, 0.37f,8.0f,   1024));
                PbrRegistry.registerPBR(ModBlocks.PAVEMENT,          new PbrRegistry.PbrMaterial(true, 0.37f,8.0f,   1024));

                BlockIdMap.init = false;

                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    SPBRevamped.LOGGER.error("This mod is not compatible with MacOS. Please use Windows or Linux (wayland).");
                    MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING, Text.of("Potential Incompatibility found"), Text.of("This mod is not compatible with MacOS. Please use Windows or Linux (wayland).")));
                }
            }
        });


        // The Veil renderer (and so the deferred renderer) is created inside the MinecraftClient constructor, after this runs
        VeilEventPlatform.INSTANCE.onVeilRendererAvailable(DeferredRenderer::init);

        VeilEventPlatform.INSTANCE.onVeilRenderLevelStage((stage, levelRenderer, bufferSource, matrixStack, frustumMatrix, projectionMatrix, renderTick, deltaTracker, camera, frustum) -> {
            float partialTicks = deltaTracker.getTickDelta(false);
            //*Setting for later use
            if(camera != null){
                SPBRevampedClient.camera = camera;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            World clientWorld = client.world;
            if (clientWorld != null) {
                //*Only render the shadow map when in the poolrooms
                if (clientWorld.getRegistryKey() == BackroomsLevels.POOLROOMS_WORLD_KEY) {
                    if (stage == Stage.AFTER_SKY) {
                        if (camera != null) {
                            ShadowMapRenderer.renderShadowMap(camera, partialTicks, clientWorld);
                        }
                    }
                }

                if (!cutsceneManager.fall) {
                    if (clientWorld.getRegistryKey() == BackroomsLevels.LEVEL0_WORLD_KEY) {
                        if (stage == Stage.AFTER_SKY) {
                            if (camera != null) {
                                ShadowMapRenderer.renderLevel0ShadowMap(camera, clientWorld);
                            }
                        }
                    }
                }

                if (clientWorld.getRegistryKey() == BackroomsLevels.INFINITE_FIELD_WORLD_KEY) {
                    if (stage == Stage.AFTER_SOLID_BLOCKS) {
                        if (this.grassRenderer == null) {
                            this.grassRenderer = new GrassRenderer();
                        }

                        if (this.birdRenderer == null) {
                            this.birdRenderer = new BirdRenderer();
                        }

                        this.grassRenderer.render();


                        if (ConfigStuff.birdQuality != BirdQuality.DISABLED) {
                            ShaderProgram shader = VeilRenderSystem.renderer().getShaderManager().getShader(BirdRenderer.computeShaderPath);
                            if (shader != null) {
                                List<Vector3f> vector3fcs = FlockManager.getFlockCenters().stream().map((vec3d -> new Vector3f((float) vec3d.x, (float) vec3d.y, (float) vec3d.z))).toList();
                                shader.getUniformSafe("FlockCenters").setVectors(vector3fcs.toArray(new Vector3fc[0]));
                                shader.getUniformSafe("FlockAmount").setInt(ConfigStuff.birdQuality.getFlockCount());
                                this.birdRenderer.render();
                            }
                        }
                    }
                } else if (clientWorld.getRegistryKey() == BackroomsLevels.LEVEL324_WORLD_KEY) {
                    if (stage == Stage.AFTER_SOLID_BLOCKS) {
                        if (this.grassRenderer == null) {
                            this.grassRenderer = new GrassRenderer();
                        }

                        this.grassRenderer.render();
                    }
                } else if (this.grassRenderer != null) {
                    this.grassRenderer.close();
                    this.grassRenderer = null;
                } else if (this.birdRenderer != null) {
                    this.birdRenderer.close();
                    this.birdRenderer = null;
                }
            }



            //*Enable the VHS shader
            PostProcessingManager postProcessingManager = VeilRenderSystem.renderer().getPostProcessingManager();
            if (stage == Stage.AFTER_LEVEL) {
                //*Flashlight
                flashlightRenderer.renderFlashlightForEveryPlayer(partialTicks);


                PostPipeline Pipeline = postProcessingManager.getPipeline(VHS_POST);
                if (Pipeline != null) {
                    if (shouldRenderCameraEffect()) {
                        if(!postProcessingManager.isActive(VHS_POST)) {
                            postProcessingManager.add(VHS_POST);
                        }
                    } else if (postProcessingManager.isActive(VHS_POST)) {
                        postProcessingManager.remove(VHS_POST);
                    }
                }


                if (client.player != null) {
                    if (clientWorld != null) {
                        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(client.player);
                        WorldEvents events = InitializeComponents.EVENTS.get(clientWorld);
                        Entity activeSkinwalker = events.getActiveSkinwalkerTarget();


                        if (activeSkinwalker != null) {
                            Box box = activeSkinwalker.getVisibilityBoundingBox().expand(0.1);
                            boolean inFrustum = frustum.isVisible(box);

                            if (inFrustum && client.player.canSee(activeSkinwalker)) {
                                if (!playerComponent.canSeeActiveSkinWalkerTarget()) {
                                    playerComponent.setCanSeeActiveSkinWalkerTarget(true);

                                    ClientPlayNetworking.send(new SeeActiveSkinwalkerSync(true));
                                }
                            } else {
                                if (playerComponent.canSeeActiveSkinWalkerTarget()) {
                                    playerComponent.setCanSeeActiveSkinWalkerTarget(false);

                                    ClientPlayNetworking.send(new SeeActiveSkinwalkerSync(false));
                                }
                            }
                        }
                    }

                    if (!client.player.isSpectator() && !client.player.isCreative()) {
                        if (client.getDebugHud().shouldShowDebugHud()) {
                            client.getDebugHud().toggleDebugHud();
                        }
                    }

                }
            }

        });

        VeilEventPlatform.INSTANCE.preVeilPostProcessing(((name, pipeline, context) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = MinecraftClient.getInstance().player;
            VeilRenderer renderer = VeilRenderSystem.renderer();
            ShaderPreDefinitions definitions = renderer.getShaderDefinitions();

            if (!shouldRenderCameraEffect()) {
                if(definitions.getDefinition("WARP") != null) {
                    definitions.remove("WARP");
                }
            }

            if (player != null && client.world != null) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);

                if (VHS_POST.equals(name)) {
                    ShaderProgram shaderProgram = context.getShader(POST_VHS);
                    if (shaderProgram != null) {
                        if(youCantEscape) {
                            shaderProgram.getUniformSafe("youCantEscape").setInt(1);
                        } else {
                            shaderProgram.getUniformSafe("youCantEscape").setInt(0);
                        }

                        if (playerComponent.isBeingCaptured()) {
                            SkinwalkerJumpscare.doJumpscare(shaderProgram, client, playerComponent);
                        } else {
                            shaderProgram.getUniformSafe("Jumpscare").setInt(0);
                            shaderProgram.getUniformSafe("CreepyFace1").setInt(0);
                            shaderProgram.getUniformSafe("CreepyFace2").setInt(0);
                            shaderProgram.getUniformSafe("Rand").setVector(0, 0);
                        }

                        if (PreviousUniforms.prevModelViewMat != null && PreviousUniforms.prevProjMat != null) {
                            shaderProgram.getUniformSafe("prevViewMat").setMatrix(PreviousUniforms.prevModelViewMat);
                            shaderProgram.getUniformSafe("prevProjMat").setMatrix(PreviousUniforms.prevProjMat);
                            shaderProgram.getUniformSafe("prevCameraPos").setVector(PreviousUniforms.prevCameraPos);
                        }

                        shaderProgram.getUniformSafe("MotionBlurStrength").setFloat(ConfigStuff.motionBlurStrength);
                        shaderProgram.getUniformSafe("DistortionStrength").setFloat(ConfigStuff.VHSDistortionMultiplier);
                    }

                    shaderProgram = context.getShader(SSAO);
                    if (shaderProgram != null) {
                        shaderProgram.getUniformSafe("samples").setVectors(SSAOSamples.getSSAOSamples());
                    }

                    shaderProgram = context.getShader(EVERYTHING_SHADER);
                    if (shaderProgram != null) {
                        if (client.world.getRegistryKey() == BackroomsLevels.LEVEL1_WORLD_KEY) {
                            shaderProgram.getUniformSafe("FogToggle").setInt(1);
                        } else {
                            shaderProgram.getUniformSafe("FogToggle").setInt(0);
                        }


                        if(blackScreen || (player.isInsideWall() && !getCutsceneManager().isPlaying) || playerComponent.isBeingReleased()) {
                            shaderProgram.getUniformSafe("blackScreen").setInt(1);
                        } else {
                            shaderProgram.getUniformSafe("blackScreen").setInt(0);
                        }

                        if (client.world.getRegistryKey() == BackroomsLevels.LEVEL1_WORLD_KEY) {
                            shaderProgram.getUniformSafe("TogglePuddles").setInt(1);
                        } else {
                            shaderProgram.getUniformSafe("TogglePuddles").setInt(0);
                        }


                        shaderProgram.getUniformSafe("shadowColor").setVector(PoolroomsDayCycle.getLightColor());
                    }

                    shaderProgram = context.getShader(MIXED_SHADER);
                    if (shaderProgram != null) {
                        shaderProgram.getUniformSafe("Rand").setVector(random.nextFloat() * 2.0f - 1.0f, random2.nextFloat() * 2.0f - 1.0f);

                        shaderProgram.getUniformSafe("shadowColor").setVector(PoolroomsDayCycle.getLightColor());

                        shaderProgram.getUniformSafe("isLightning").setInt(isLightning ? 1 : 0);

                    }

                    shaderProgram = context.getShader(GLITCH_SHADER);
                    if (shaderProgram != null) {
                        shaderProgram.getUniformSafe("glitchTime").setFloat(playerComponent.getGlitchTimer());
                    }

                }

                BackroomsLevels.getLevel(client.world).ifPresent((backroomsLevel -> {
                    if (backroomsLevel instanceof Level2BackroomsLevel level) {
                        if (level.isWarping() || !finishedWarp(client.world)) {
                            setDefinition(definitions, "WARP");
                        } else {
                            definitions.remove("WARP");
                        }
                    }
                }));

                ConfigDefinitions.definitions.forEach((s, aBoolean) -> {
                    if(aBoolean.get()) {
                        setDefinition(definitions, s);
                    } else {
                        definitions.remove(s);
                    }
                });

                BackroomsLevels.definitions.forEach((s, registryKey) -> {
                    if (client.world.getRegistryKey() == registryKey) {
                        setDefinition(definitions, s);
                    } else {
                        definitions.remove(s);
                    }
                });

                PreviousUniforms.update();
            }
        }));

        ClientPlayConnectionEvents.JOIN.register(((handler,sender, client) -> {
            DeferredRenderer.get().reset();

            if (client.world != null) {
                HelpfulHintManager.sendMessages(client.player);

                //*Just in case it become unsynced
                BackroomsLevels.getLevel(client.world).ifPresent((backroomsLevel -> {
                    if (backroomsLevel instanceof PoolroomsBackroomsLevel poolroomsBackroomsLevel) {
                        PoolroomsDayCycle.dayTime = poolroomsBackroomsLevel.getTimeOfDay();
                    }
                }));

            }

        }));

        //*For some reason veil lights aren't removed when you leave the game
        ClientConnectionEvents.DISCONNECT.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player != null) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                playerComponent.setFlashLightOn(false);
                flashlightRenderer.tryToRemoveFlashlight(player);
                flashlightRenderer.clearFlashlights();
                playerComponent.setDoingCutscene(false);
            }

            cutsceneManager.reset();

            if(this.grassRenderer != null) {
                this.grassRenderer.close();
                this.grassRenderer = null;
            }

            if (this.birdRenderer != null) {
                this.birdRenderer.close();
                this.birdRenderer = null;
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            cutsceneManager.reset();
            if (this.grassRenderer != null) {
                this.grassRenderer.close();
                this.grassRenderer = null;
            }

            if (this.birdRenderer != null) {
                this.birdRenderer.close();
                this.birdRenderer = null;
            }
        });


        ClientTickEvents.END_WORLD_TICK.register((client) ->{
            Vector<TickTimer> tickTimers = TickTimer.getAllInstances();
            if (!tickTimers.isEmpty()){
                for(TickTimer timer : tickTimers){
                    timer.addCurrentTick();
                }
            }

            //Fixes Minecraft spectating not loading chunks bug
            MinecraftClient client1 = MinecraftClient.getInstance();
            PlayerEntity player = client1.player;
            if (player != null) {
                if (player != client1.getCameraEntity() && client1.getCameraEntity() != null) {
                    Vec3d pos = client1.getCameraEntity().getPos();
                    player.setPosition(pos);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register((client) ->{
            if (cutsceneManager.isPlaying) {
                if (!ClientManager.getPlayerStateManager().isMuted()) {
                    shouldBeUnmuted = true;
                    ClientManager.getPlayerStateManager().setMuted(true);
                }
            } else if (shouldBeUnmuted) {
                ClientManager.getPlayerStateManager().setMuted(false);
                shouldBeUnmuted = false;
            }

            PlayerEntity playerClient = client.player;
            if (playerClient != null){
                //*Main Set in Backrooms
                setInBackrooms(BackroomsLevels.isInBackrooms(playerClient.getWorld().getRegistryKey()));

                if (client.world != null) {
                    LightRenderer lightRenderer = DeferredRenderer.get().getLightRenderer();

                    if (shouldRenderCameraEffect() && isInBackrooms()) {
                        HelpfulHintManager.disableSuffocateHint();

                        BackroomsLevel level = BackroomsLevels.getLevel(client.player.getWorld()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL);

                        if (!level.hasVanillaLighting()) {
                            lightRenderer.disableVanillaLight();
                        } else {
                            lightRenderer.enableVanillaLight();
                        }

                        lightRenderer.disableAmbientOcclusion();
                    } else {
                        lightRenderer.enableVanillaLight();
                        lightRenderer.enableAmbientOcclusion();
                    }

                    getCurrentBackroomsLevel().ifPresent((backroomsLevel -> {
                        if ((backroomsLevel instanceof InfiniteGrassBackroomsLevel/* || backroomsLevel instanceof Level324Backroomslevel*/) && ConfigStuff.birdQuality != BirdQuality.DISABLED) {
                            FlockManager.tick();
                        }
                    }));
                }
            }
        });

    }

    /**
     * Veil for 1.21.1 treats every set() of a flag definition as a change (it compares the stored "" with null),
     * which recompiles every dependent shader; only set definitions that are not already present.
     */
    private static void setDefinition(ShaderPreDefinitions definitions, String name) {
        if (definitions.getDefinition(name) == null) {
            definitions.set(name);
        }
    }

    public static void setShadowUniforms(foundry.veil.api.client.render.shader.program.ShaderProgram access, World world) {
        Matrix4f level0ViewMat = ShadowMapRenderer.createShadowModelView(camera.getPos().x, camera.getPos().y, camera.getPos().z, true).peek().getPositionMatrix();
        Matrix4f viewMat = ShadowMapRenderer.createShadowModelView(camera.getPos().x, camera.getPos().y, camera.getPos().z, world, true).peek().getPositionMatrix();

        access.getUniformSafe("level0ViewMatrix").setMatrix(level0ViewMat);
        access.getUniformSafe("viewMatrix").setMatrix(viewMat);
        access.getUniformSafe("IShadowViewMatrix").setMatrix(viewMat.invert());

        access.getUniformSafe("orthographMatrix").setMatrix(ShadowMapRenderer.createProjMat());
    }

    public static float getWarpTimer(World world) {
        if (!(BackroomsLevels.getLevel(world).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL) instanceof Level2BackroomsLevel level)) {
            return 0;
        }

        if (level.isWarping() || tickTimer.getCurrentTick() != 0) {
            tickTimer.setOnOrOff(true);
            float x = tickTimer.getCurrentTick();
            float w = 0.03141592f;
            float result = MathStuff.mod((x * w) * 0.002f, w);
            if (result == 0 || (!level.isWarping() && result == 0.03141592f/2)) {
                tickTimer.resetToZero();
            }
            return result;

        } else {
            tickTimer.setOnOrOff(false);
            return 0;
        }
    }

    public static boolean finishedWarp(World world) {
        float warp = getWarpTimer(world);
        return warp == 0 || warp == 0.03141592f/2;
    }

    public static void sendComponentSyncPacket(boolean writeBoolean, String component) {
        ClientPlayNetworking.send(new SyncServerComponent(writeBoolean, component));
    }

    public static boolean isInBackrooms() {
        return inBackrooms;
    }

    public static boolean shouldRenderCameraEffect() {
        // The VHS look needs the deferred renderer, which is off while Sodium is installed (see SodiumCompat)
        if (com.sp.compat.SodiumCompat.PRESENT) {
            return false;
        }

        return (!isInBackrooms() && ConfigStuff.enableVhsEffect) || (isInBackrooms() && ConfigStuff.enableVhsEffectInTheBackrooms);
    }

    public static void setInBackrooms(boolean inBackrooms) {
        SPBRevampedClient.inBackrooms = inBackrooms;
    }

    public static CutsceneManager getCutsceneManager() {
        return cutsceneManager;
    }

    public static CameraShake getCameraShake() {
        return cameraShake;
    }

    public static Optional<BackroomsLevel> getCurrentBackroomsLevel() {
        return BackroomsLevels.getLevel(MinecraftClient.getInstance().world);
    }

    public static boolean isInLevel(BackroomsLevel level) {
        if (MinecraftClient.getInstance().world == null) {
            return false;
        }

        return BackroomsLevels.isInBackroomsLevel(MinecraftClient.getInstance().world, level);
    }
}