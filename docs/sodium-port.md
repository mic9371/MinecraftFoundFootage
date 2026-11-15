# Running the mod's visuals under Sodium

Status: with Sodium 0.6.13 the mod **loads and runs**, with its own visuals off (`SodiumCompat`, `SPBMixinPlugin`).
This document is the plan for getting the deferred lighting, PBR and VHS look working with Sodium as well.

## Why it does not work yet
The visuals are a deferred renderer: chunk shaders write a G-buffer (albedo, normal, material id, light coords,
lightmap) and later passes light it. Vanilla chunk rendering is hooked in three places, all of which Sodium replaces:

| Need | Vanilla hook (current) | Sodium |
|---|---|---|
| G-buffer output from chunk shaders | `ShaderStateMixin` swaps `rendertype_*` for deferred twins | Sodium draws chunks with its own program (`assets/sodium/shaders/blocks/block_layer_opaque.{vsh,fsh}`), single color output |
| Per-vertex block material id | `BufferBuilderMixin` + custom `VertexFormats.BLOCKS` | Compact packed vertex format, no extra fields, no normals |
| PBR layer (extra per-vertex zoom/resolution/height) | custom chunk `RenderLayer` + `VertexFormats.PBR` | Only knows solid/cutout/translucent passes; a custom layer crashes meshing |
| Terrain uniforms / layer hooks | `WorldRenderer#renderLayer` injections | `renderLayer` is replaced |

## Plan
1. **G-buffer chunk shader.** Replace Sodium's chunk shader sources (`ShaderLoader` / `ChunkShaderInterface`
   mixins) with versions that output the G-buffer when the deferred renderer is active.
   - Normal: not in the vertex data; derive from `dFdx/dFdy` of the view position (flat shading, which matches block faces).
   - Lightmap: Sodium applies it in the vertex shader (`v_Color`); output the raw lightmap sample separately.
2. **Material id.** `PbrRegistry`/`BlockIdMap` are per-block constants, so no per-vertex data is needed if the shader
   can tell the block. Options: (a) spare bits of Sodium's material/`_material_params` byte set from the block in
   the quad writer, (b) a per-sprite lookup keyed by texture atlas UV (block id table baked at resource reload).
   Prefer (b): no vertex format change.
3. **PBR layer.** Look up zoom/resolution/height/depth multiplier by material id (uniform array or UBO built from
   `PbrRegistry`) and run the PBR shader logic inside the chunk fragment shader, instead of a separate layer with a geometry shader.
4. **Shadow map.** `ShadowMapRenderer` calls vanilla `setupTerrain`/`renderLayer` through `WorldRendererAccessor`; needs a
   Sodium path (render the Sodium chunk lists with a depth-only program from the light's point of view).
5. **Terrain uniforms / hooks.** Move the `renderLayer` hooks (`NormalMat`, pool sky, PBR) onto Sodium's
   `ChunkRenderer` / `DefaultChunkRenderer`.
6. Remove the `SodiumCompat.PRESENT` gates one by one and re-run the five-level walkthrough.

Not planned: Iris (it replaces the whole pipeline; run the mod's own shaders or Iris, not both).

## Testing
`./gradlew runClient -PwithSodium` starts the dev client with Sodium 0.6.13 (compile dependency is always present).
Veil 1.0.0.228 is built for Sodium 0.6.x; 0.7+ fails in Veil's own `RenderSectionManagerMixin`.
