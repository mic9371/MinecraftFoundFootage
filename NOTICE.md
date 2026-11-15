# Third-party code

The following files contain code copied from Veil (https://github.com/FoundryMC/Veil), version 1.0.0.228, licensed under LGPL-3.0
(the same license as this mod). They were adapted to Minecraft 1.21.1 and to this mod's namespace, and carry a header comment saying so.

- `src/main/java/com/sp/render/deferred/**`: deferred renderer, light renderers and light types (Veil 1.20.1 `render/deferred`)
- `src/main/java/com/sp/render/camera/anim/**`: camera path and keyframe classes (removed from Veil for 1.21.1)
- `src/main/resources/assets/minecraft/pinwheel/shaders/deferred/**`, `assets/veil/pinwheel/framebuffers/**`,
  `assets/veil/pinwheel/shaders/include/{deferred_buffers,material}.glsl`,
  `assets/spb-revamped/pinwheel/shaders/program/deferred/**` and `assets/spb-revamped/pinwheel/post/deferred/**`:
  resources from Veil's 1.20.1 built-in `deferred` resource pack
