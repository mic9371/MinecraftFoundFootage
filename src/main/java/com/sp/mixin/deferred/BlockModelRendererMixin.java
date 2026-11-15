// Ported from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil): drives the vendored deferred renderer.
package com.sp.mixin.deferred;

import com.sp.render.deferred.DeferredRenderer;
import com.sp.render.deferred.DeferredVertexConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes vanilla's directional face shading from block models while deferred rendering, since lighting is applied later.
 */
@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {

    @Unique
    private static final ThreadLocal<Boolean> spb$DEFERRED = ThreadLocal.withInitial(() -> false);

    @Unique
    private static final String RENDER = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V";
    @Unique
    private static final String RENDER_QUAD = "Lnet/minecraft/client/render/block/BlockModelRenderer;renderQuad(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFIIIII)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void spb$captureState(CallbackInfo ci) {
        DeferredRenderer deferredRenderer = DeferredRenderer.get();
        spb$DEFERRED.set(deferredRenderer.isEnabled() && deferredRenderer.getRendererState() != DeferredRenderer.RendererState.DISABLED);
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void spb$clearState(CallbackInfo ci) {
        spb$DEFERRED.set(false);
    }

    @ModifyArg(method = "renderQuadsFlat", at = @At(value = "INVOKE", target = RENDER_QUAD), index = 6)
    private float spb$modifyShade0(float value) {
        return spb$DEFERRED.get() ? 1.0F : value;
    }

    @ModifyArg(method = "renderQuadsFlat", at = @At(value = "INVOKE", target = RENDER_QUAD), index = 7)
    private float spb$modifyShade1(float value) {
        return spb$DEFERRED.get() ? 1.0F : value;
    }

    @ModifyArg(method = "renderQuadsFlat", at = @At(value = "INVOKE", target = RENDER_QUAD), index = 8)
    private float spb$modifyShade2(float value) {
        return spb$DEFERRED.get() ? 1.0F : value;
    }

    @ModifyArg(method = "renderQuadsFlat", at = @At(value = "INVOKE", target = RENDER_QUAD), index = 9)
    private float spb$modifyShade3(float value) {
        return spb$DEFERRED.get() ? 1.0F : value;
    }

    @ModifyVariable(method = RENDER, at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private VertexConsumer spb$modifyConsumer(VertexConsumer value) {
        return spb$DEFERRED.get() ? new DeferredVertexConsumer(value) : value;
    }
}
