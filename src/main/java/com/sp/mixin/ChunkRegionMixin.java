package com.sp.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sp.mixininterfaces.ChunkRegionPlacementRadius;
import net.minecraft.world.ChunkRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkRegion.class)
public abstract class ChunkRegionMixin implements ChunkRegionPlacementRadius {
    @Unique
    private int spb$placementRadius = -1;

    @Override
    public void spb$setPlacementRadius(int radius) {
        this.spb$placementRadius = radius;
    }

    @ModifyExpressionValue(method = "isValidForSetBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkGenerationStep;blockStateWriteRadius()I"))
    private int spb$overridePlacementRadius(int original) {
        return this.spb$placementRadius >= 0 ? this.spb$placementRadius : original;
    }
}
