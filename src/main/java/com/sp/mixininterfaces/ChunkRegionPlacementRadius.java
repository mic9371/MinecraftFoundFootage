package com.sp.mixininterfaces;

/**
 * Lets {@link com.sp.world.generation.chunk_generator.BackroomsChunkGenerator} widen a ChunkRegion's block write radius.
 * In 1.20.1 this was a ChunkRegion constructor argument; since 1.20.5 it comes from the ChunkGenerationStep.
 */
public interface ChunkRegionPlacementRadius {
    void spb$setPlacementRadius(int radius);
}
