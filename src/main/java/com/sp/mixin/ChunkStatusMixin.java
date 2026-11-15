package com.sp.mixin;

import com.sp.mixininterfaces.ChunkRegionPlacementRadius;
import com.sp.world.generation.chunk_generator.BackroomsChunkGenerator;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerating;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * This mixin calls the {@link  BackroomsChunkGenerator#generate(StructureWorldAccess, Chunk)} method during the NOISE step.
 * It's what allows minecraft to generate the backrooms mazes.
 * (In 1.20.1 this targeted the NOISE lambda in ChunkStatus; since 1.20.5 the step tasks live in ChunkGenerating.)
 */
@Mixin(ChunkGenerating.class)
public abstract class ChunkStatusMixin {
    @Inject(method = "populateNoise", at = @At("HEAD"))
    private static void runGenerationTask(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk, CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
        if (context.generator() instanceof BackroomsChunkGenerator backroomsChunkGenerator) {
            ChunkRegion chunkRegion = new ChunkRegion(context.world(), chunks, step, chunk);
            ((ChunkRegionPlacementRadius) chunkRegion).spb$setPlacementRadius(backroomsChunkGenerator.getPlacementRadius());
            backroomsChunkGenerator.generate(chunkRegion, chunk);
        }
    }
}
