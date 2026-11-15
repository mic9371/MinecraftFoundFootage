package com.sp.mixin.veilfix;

import foundry.veil.api.client.render.shader.processor.ShaderImportProcessor;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.GlslRootNode;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.api.node.function.GlslFunctionNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslNewFieldNode;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Works around Veil 1.0.0.228 (1.21.1) emitting {@code #include}s in the wrong order. Veil inserts every include at
 * the very top of the shader and drops definitions an include shares with code that is already present, so with
 * several includes that share a dependency (for example veil:deferred_utils) the only remaining copy of that
 * dependency ended up below the code that uses it, and the shader failed to compile.
 *
 * <p>This keeps the order from the file instead: includes are emitted top to bottom, and a definition that an
 * earlier include (or the shader itself) already provides is dropped from later includes.</p>
 */
@Mixin(value = ShaderImportProcessor.class, remap = false)
public class ShaderImportProcessorOrderMixin {

    @Unique
    private static final String INCLUDE_KEY = "#include ";

    @Inject(method = "modify", at = @At("HEAD"), cancellable = true)
    private void spb$includeInOrder(ShaderPreProcessor.Context ctx, GlslTree tree, CallbackInfo ci) throws IOException, GlslSyntaxException, LexerException {
        List<String> directives = tree.getDirectives();
        List<String> imports = new ArrayList<>();
        for (String directive : directives) {
            if (directive.startsWith(INCLUDE_KEY)) {
                imports.add(directive);
            }
        }
        directives.removeAll(imports);

        List<GlslNode> merged = new ArrayList<>();
        for (String directive : imports) {
            String importId = directive.substring(INCLUDE_KEY.length()).trim();
            GlslTree loadedImport;
            try {
                loadedImport = ctx.shaderImporter().loadImport(ctx, Identifier.of(importId), false);
            } catch (InvalidIdentifierException e) {
                throw new IOException("Invalid import: " + importId, e);
            }

            directives.addAll(loadedImport.getDirectives());
            for (GlslNode node : loadedImport.getBody()) {
                if (!spb$isAlreadyDefined(node, tree.getBody()) && !spb$isAlreadyDefined(node, merged)) {
                    merged.add(node);
                }
            }
        }

        tree.getBody().addAll(0, merged);
        ci.cancel();
    }

    /**
     * Same duplicate rules as Veil's SOURCE include strategy: fields by name, functions with a body by name and header.
     */
    @Unique
    private static boolean spb$isAlreadyDefined(GlslNode node, List<GlslNode> existing) {
        if (!(node instanceof GlslRootNode root)) {
            return false;
        }

        if (root.isField()) {
            String name = root.asField().getName();
            if (name == null) {
                return false;
            }
            for (GlslNode other : existing) {
                if (other instanceof GlslRootNode otherRoot && otherRoot.isField() && name.equals(otherRoot.asField().getName())) {
                    return true;
                }
            }
        } else if (root.isFunction()) {
            GlslFunctionNode function = root.asFunction();
            if (function.getBody() == null) {
                return false;
            }
            for (GlslNode other : existing) {
                if (other instanceof GlslRootNode otherRoot && otherRoot.isFunction()) {
                    GlslFunctionNode otherFunction = otherRoot.asFunction();
                    if (otherFunction.getBody() != null && Objects.equals(function.getName(), otherFunction.getName()) && function.getHeader().equals(otherFunction.getHeader())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
