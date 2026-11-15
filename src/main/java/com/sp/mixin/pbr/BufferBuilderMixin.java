package com.sp.mixin.pbr;

import com.sp.mixininterfaces.BlockMaterial;
import com.sp.render.VertexFormats;
import com.sp.render.pbr.BlockIdMap;
import com.sp.render.pbr.PbrRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This Mixin adds additional vertex data to blocks when rendering:<br>
 * MaterialID for every block<br>
 * Zoom and Resolution for PBR blocks
 *
 * <p>Since 1.21 a vertex is finished implicitly when the next one starts (or the buffer ends), and every element of
 * the format must have been written by then, so the extra elements are filled in right before the vertex ends.</p>
 */
@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements BlockMaterial {
    @Shadow @Final private VertexFormat format;
    @Shadow private int vertexCount;
    @Shadow private int currentMask;

    @Shadow protected abstract long beginElement(VertexFormatElement element);

    @Unique private Block currentBlock;

    @Override
    public void setCurrentBlock(Block block) {
        this.currentBlock = block;
    }

    @Inject(method = "endVertex", at = @At("HEAD"))
    private void putBlockID(CallbackInfo ci) {
        if (this.vertexCount == 0) {
            return;
        }

        //Normal Block
        if (this.format == VertexFormats.BLOCKS) {
            this.putIntElement(VertexFormats.MATERIAL, BlockIdMap.getBlockID(this.currentBlock));
        }
        //PBR block
        else if (this.format == VertexFormats.PBR) {
            PbrRegistry.PbrMaterial material = PbrRegistry.getMaterial(this.currentBlock);
            if (material == null) {
                // Matches 1.20.1, where these attributes were simply left unwritten
                this.putFloatElement(VertexFormats.ZOOM, 0.0F);
                this.putIntElement(VertexFormats.RESOLUTION, 0);
                this.putIntElement(VertexFormats.ENABLE_HEIGHT, 0);
                this.putFloatElement(VertexFormats.DEPTH_MULTIPLIER, 0.0F);
                return;
            }
            this.putFloatElement(VertexFormats.ZOOM, material.zoom());
            this.putIntElement(VertexFormats.RESOLUTION, material.textureResolution());
            this.putIntElement(VertexFormats.ENABLE_HEIGHT, material.enableHeight() ? 1 : 0);
            this.putFloatElement(VertexFormats.DEPTH_MULTIPLIER, material.depthMultiplier());
        }
    }

    // Integers are written as raw bits into FLOAT elements, exactly like 1.20.1 did (the shaders read them as int attributes).
    @Unique
    private void putIntElement(VertexFormatElement element, int value) {
        if ((this.currentMask & element.getBit()) != 0) {
            long pointer = this.beginElement(element);
            if (pointer != -1L) {
                MemoryUtil.memPutInt(pointer, value);
            }
        }
    }

    @Unique
    private void putFloatElement(VertexFormatElement element, float value) {
        if ((this.currentMask & element.getBit()) != 0) {
            long pointer = this.beginElement(element);
            if (pointer != -1L) {
                MemoryUtil.memPutFloat(pointer, value);
            }
        }
    }
}
