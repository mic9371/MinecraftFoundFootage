package com.sp.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Skips the mixins that patch vanilla's chunk meshing and chunk rendering when Sodium is installed, because Sodium
 * replaces those code paths (see {@link com.sp.compat.SodiumCompat}).
 */
public class SPBMixinPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "com.sp.mixin.";

    /** Mixins that only make sense with vanilla's chunk renderer. */
    private static final Set<String> VANILLA_CHUNK_RENDERER_ONLY = Set.of(
            "pbr.SectionBuilderMixin",
            "pbr.BufferBuilderMixin",
            "deferred.VanillaChunkUniformsMixin",
            "deferred.BlockModelRendererMixin",
            "AOFixMixin",
            "renderlayer.RenderLayerMixin",
            "renderlayer.WorldRendererMixin"
    );

    private boolean sodium;

    @Override
    public void onLoad(String mixinPackage) {
        this.sodium = FabricLoader.getInstance().isModLoaded("sodium");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (this.sodium && mixinClassName.startsWith(PACKAGE)) {
            return !VANILLA_CHUNK_RENDERER_ONLY.contains(mixinClassName.substring(PACKAGE.length()));
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
