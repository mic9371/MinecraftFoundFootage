package com.sp.render;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

/**
 * Chunk vertex formats with extra per-vertex data, written by {@link com.sp.mixin.pbr.BufferBuilderMixin}.
 *
 * <p>Attribute locations follow element order: in {@link #BLOCKS} "Material" is location 5 (1.20.1 had a padding
 * element at 5, so it used to be 6); in {@link #PBR} the four extra attributes are locations 5-8.</p>
 */
public class VertexFormats {
    //For some reason setting the Component Type to INT breaks everything
    public static final VertexFormatElement MATERIAL = registerFloat();
    public static final VertexFormatElement ZOOM = registerFloat();
    public static final VertexFormatElement RESOLUTION = registerFloat();
    public static final VertexFormatElement ENABLE_HEIGHT = registerFloat();
    public static final VertexFormatElement DEPTH_MULTIPLIER = registerFloat();

    public static final VertexFormat BLOCKS = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV_0)
            .add("UV2", VertexFormatElement.UV_2)
            .add("Normal", VertexFormatElement.NORMAL)
            .skip(1)
            .add("Material", MATERIAL)
            .build();

    public static final VertexFormat PBR = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV_0)
            .add("UV2", VertexFormatElement.UV_2)
            .add("Normal", VertexFormatElement.NORMAL)
            .add("Zoom", ZOOM)
            .add("Resolution", RESOLUTION)
            .add("EnableHeight", ENABLE_HEIGHT)
            .add("DepthMultiplier", DEPTH_MULTIPLIER)
            .build();

    /**
     * Since 1.20.5 every vertex element needs a unique global id (max 32), so take the next free one.
     */
    private static VertexFormatElement registerFloat() {
        for (int id = 0; id < 32; id++) { // 32 = VertexFormatElement.ELEMENTS.length
            if (VertexFormatElement.get(id) == null) {
                return VertexFormatElement.register(id, 0, VertexFormatElement.ComponentType.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
            }
        }
        throw new IllegalStateException("No free vertex format element ids left");
    }
}
