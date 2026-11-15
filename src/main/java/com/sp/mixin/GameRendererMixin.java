package com.sp.mixin;

import org.spongepowered.asm.mixin.Unique;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.ClientPlayerEntity;
import com.sp.SPBRevamped;
import com.sp.SPBRevampedClient;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.render.ShadowMapRenderer;
import com.sp.render.camera.CameraRoll;
import com.sp.render.camera.CutsceneManager;
import com.sp.util.MathStuff;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique
    Entity newCamera;
    @Unique
    private static final Identifier shadowSolid = Identifier.of(SPBRevamped.MOD_ID, "shadowmap/rendertype_solid");

    @Unique
    private static final Identifier shadowEntity = Identifier.of(SPBRevamped.MOD_ID, "shadowmap/rendertype_entity");

    @Unique
    private static final Identifier warpEntity = Identifier.of("spbrevamped", "warp_player");

    @Unique
    private float smoothPitch = 0.0f;

    @Unique
    private float smoothYaw = 0.0f;

    @Shadow @Final MinecraftClient client;

    @Shadow @Final private Camera camera;


    @Shadow public abstract void setBlockOutlineEnabled(boolean blockOutlineEnabled);
    @Shadow public abstract void tick();
    @Shadow protected abstract void renderHand(Camera camera, float tickDelta, Matrix4f matrix4f);

    @Shadow private static @Nullable ShaderProgram renderTypeEntityTranslucentProgram;


    /**
     * Since 1.20.5 the world view rotation is conj(camera rotation) = Rx(pitch) * Ry(yaw + 180), the same matrix 1.20.1
     * built on the view MatrixStack. This applies the camera roll / cutscene rotation (pre-multiplied, as 1.20.1 did
     * before the pitch/yaw rotations) and the smoothed pitch/yaw that 1.20.1 applied via @ModifyArg on rotationDegrees.
     */
    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;conjugate(Lorg/joml/Quaternionf;)Lorg/joml/Quaternionf;"))
    private Quaternionf modifyViewRotation(Quaternionf original) {
        if (!SPBRevampedClient.shouldRenderCameraEffect()) {
            return original;
        }

        PlayerEntity player = this.client.player;
        this.setBlockOutlineEnabled(true);

        float tickDelta = this.camera.getLastTickDelta();
        float pitch = this.smoothPitch(this.camera.getPitch());
        float yaw = this.smoothYaw(this.camera.getYaw() + 180.0F);

        Quaternionf prefix = null;
        if (player != null) {
            CutsceneManager cutsceneManager = SPBRevampedClient.getCutsceneManager();

            if (ConfigStuff.enableRealCamera && !cutsceneManager.isPlaying && client.getCameraEntity() == player) {
                prefix = RotationAxis.POSITIVE_Z.rotationDegrees(CameraRoll.doCameraRoll(player, tickDelta));
            } else if (cutsceneManager.isPlaying) {
                prefix = RotationAxis.POSITIVE_Y.rotationDegrees(cutsceneManager.cameraRotZ);
            }
        }

        boolean smoothed = pitch != this.camera.getPitch() || yaw != this.camera.getYaw() + 180.0F;
        Quaternionf view = smoothed
                ? RotationAxis.POSITIVE_X.rotationDegrees(pitch).mul(RotationAxis.POSITIVE_Y.rotationDegrees(yaw))
                : original;
        return prefix != null ? prefix.mul(view) : view;
    }

    @Unique
    private float smoothPitch(float deg) {
        PlayerEntity player = this.client.player;

        if(player != null && ConfigStuff.enableSmoothCamera && client.options.getPerspective() == Perspective.FIRST_PERSON){
            this.smoothYaw = MathStuff.Lerp(this.smoothYaw, deg, ConfigStuff.cameraSmoothing, client.getRenderTickCounter().getLastFrameDuration());
            return this.smoothYaw;
        } else {
            this.smoothYaw = deg;
        }

        return deg;
    }

    @Unique
    private float smoothYaw(float deg) {
        PlayerEntity player = this.client.player;

        if(player != null && ConfigStuff.enableSmoothCamera && client.options.getPerspective() == Perspective.FIRST_PERSON){
            this.smoothPitch = MathStuff.Lerp(this.smoothPitch, deg, ConfigStuff.cameraSmoothing, client.getRenderTickCounter().getLastFrameDuration());
            return this.smoothPitch;
        } else {
            this.smoothPitch = deg;
        }

        return deg;
    }

    /// Why are we doing this. Why are we overwriting this method? Space please tell me? best of wishes -Chaos

    /// Dearest Chaos, Becasue I can -SpacePotato

    /**
     * Replaces vanilla view bobbing. Was an @Overwrite; Veil for 1.21.1 also injects into bobView, which an overwrite
     * would break, so this cancels at HEAD instead.
     */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(MatrixStack matrices, float tickDelta, CallbackInfo ci){
        ci.cancel();
        if (this.client.getCameraEntity() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)this.client.getCameraEntity();
            float f = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float g = -(playerEntity.horizontalSpeed + f * tickDelta);
            float h = MathHelper.lerp(tickDelta, playerEntity.prevStrideDistance, playerEntity.strideDistance);

            Vector3f cameraBob = new Vector3f(MathHelper.sin(g * (float) Math.PI) * h * 0.5F, -Math.abs(MathHelper.cos(g * (float) Math.PI) * h), 0.0F);
            matrices.translate(cameraBob.x, cameraBob.y, cameraBob.z);
            SPBRevampedClient.cameraBobOffset = new Vector3f(cameraBob);

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(g * (float) Math.PI) * h * 3.0F));
            float multiplier = 5.0f;
            if (ConfigStuff.enableRealCamera) {
                multiplier = 10.0f;
            }
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(g * (float) Math.PI - 0.2F) * h) * multiplier));
        }
    }


    @Inject(method = {
            "getRenderTypeSolidProgram",
            "getRenderTypeCutoutProgram",
            "getRenderTypeCutoutMippedProgram"
    }, at = @At("HEAD"), cancellable = true)
    private static void setSolidShader(CallbackInfoReturnable<ShaderProgram> cir) {
        if(ShadowMapRenderer.isRenderingShadowMap()) {
            foundry.veil.api.client.render.shader.program.ShaderProgram shader = VeilRenderSystem.renderer().getShaderManager().getShader(shadowSolid);
            if (shader == null) {
                return;
            }
            cir.setReturnValue(shader.toShaderInstance());
        }
    }

    @Inject(method = {
            "getRenderTypeEntityTranslucentProgram",
            "getRenderTypeEntitySolidProgram",
            "getRenderTypeEntityCutoutProgram",
            "getRenderTypeEntityCutoutNoNullProgram",
            "getRenderTypeEntityTranslucentCullProgram"
    }, at = @At("TAIL"), cancellable = true)
    private static void setPlayerShader(CallbackInfoReturnable<ShaderProgram> cir) {
        if(ShadowMapRenderer.isRenderingShadowMap()) {
            foundry.veil.api.client.render.shader.program.ShaderProgram shader = VeilRenderSystem.renderer().getShaderManager().getShader(shadowEntity);
            if (shader == null) {
                return;
            }
            cir.setReturnValue(shader.toShaderInstance());
        }
    }

    @Inject(method = {
            "getRenderTypeEntityTranslucentProgram"
    }, at = @At("TAIL"), cancellable = true)
    private static void setPlayerWarpShader(CallbackInfoReturnable<ShaderProgram> cir) {
        foundry.veil.api.client.render.shader.program.ShaderProgram shader = VeilRenderSystem.renderer().getShaderManager().getShader(warpEntity);
        if (shader == null || !SPBRevampedClient.shouldRenderWarp) {
            cir.setReturnValue(renderTypeEntityTranslucentProgram);
            return;
        }
        cir.setReturnValue(shader.toShaderInstance());
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V"))
    private void redirect(GameRenderer instance, Camera camera, float tickDelta, Matrix4f matrix4f) {
        if(!SPBRevampedClient.getCutsceneManager().isPlaying){
            this.renderHand(camera, tickDelta, matrix4f);
        }
    }

    // Reach is an attribute since 1.20.5; 1.20.1 returned 6 for block reach and 36 (= 6^2) for squared entity reach
    @Redirect(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getBlockInteractionRange()D"))
    private double increaseReach(ClientPlayerEntity instance){
        return 6;
    }

    @Redirect(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEntityInteractionRange()D"))
    private double increaseEntityReach(ClientPlayerEntity instance){
        return 6;
    }
}
