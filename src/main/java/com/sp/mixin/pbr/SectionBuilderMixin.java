package com.sp.mixin.pbr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.sp.mixininterfaces.BlockMaterial;
import com.sp.render.RenderLayers;
import com.sp.render.VertexFormats;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Builds chunk sections with this mod's vertex formats (material ID per vertex, or PBR parameters for the PBR layer),
 * and tells the buffer which block is being meshed. Replaces 1.20.1's ChunkBuilderMixin and RebuildTaskMixin.
 */
@Mixin(SectionBuilder.class)
public class SectionBuilderMixin {

    private static final String RENDER_BLOCK = "Lnet/minecraft/client/render/block/BlockRenderManager;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;)V";

    @WrapOperation(method = "beginBufferBuilding", at = @At(value = "NEW", target = "(Lnet/minecraft/client/util/BufferAllocator;Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;)Lnet/minecraft/client/render/BufferBuilder;"))
    private BufferBuilder spb$beginWithCustomFormat(BufferAllocator allocator, VertexFormat.DrawMode drawMode, VertexFormat format, Operation<BufferBuilder> original, @Local(argsOnly = true) RenderLayer renderLayer) {
        if (renderLayer == RenderLayers.getPbrLayer()) {
            return original.call(allocator, drawMode, VertexFormats.PBR);
        }
        if (format == net.minecraft.client.render.VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL) {
            return original.call(allocator, drawMode, VertexFormats.BLOCKS);
        }
        return original.call(allocator, drawMode, format);
    }

    @Inject(method = "build", at = @At(value = "INVOKE", target = RENDER_BLOCK))
    private void spb$setCurrentBlock(CallbackInfoReturnable<SectionBuilder.RenderData> cir, @Local BufferBuilder bufferBuilder, @Local(ordinal = 0) BlockState blockState) {
        if (bufferBuilder instanceof BlockMaterial blockMaterial) {
            blockMaterial.setCurrentBlock(blockState.getBlock());
        }
    }

    @Inject(method = "build", at = @At(value = "INVOKE", target = RENDER_BLOCK, shift = At.Shift.AFTER))
    private void spb$clearCurrentBlock(CallbackInfoReturnable<SectionBuilder.RenderData> cir, @Local BufferBuilder bufferBuilder) {
        if (bufferBuilder instanceof BlockMaterial blockMaterial) {
            blockMaterial.setCurrentBlock(null);
        }
    }
}
