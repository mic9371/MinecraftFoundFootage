package com.sp.mixin.veilfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.opengl.ARBClearTexture;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.FloatBuffer;

/**
 * Veil 1.0.0.228 (1.21.1) clears every color attachment with float data, which OpenGL rejects for integer textures
 * (GL_INVALID_OPERATION every frame), leaving the deferred renderer's material buffers (R8UI/RG8UI) uncleared.
 * Integer attachments are cleared with integer data instead.
 */
@Mixin(targets = "foundry.veil.impl.client.render.framebuffer.DSAAdvancedFboImpl", remap = false)
public class AdvancedFboIntegerClearMixin {

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/ARBClearTexture;glClearTexImage(IIIILjava/nio/FloatBuffer;)V"))
    private void spb$clearIntegerTextures(int texture, int level, int format, int type, FloatBuffer data, Operation<Void> original) {
        Integer integerFormat = spb$integerFormat(GL45C.glGetTextureLevelParameteri(texture, level, GL30C.GL_TEXTURE_INTERNAL_FORMAT));
        if (integerFormat == null) {
            original.call(texture, level, format, type, data);
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ARBClearTexture.glClearTexImage(texture, level, integerFormat, GL30C.GL_UNSIGNED_INT, stack.callocInt(4));
        }
    }

    @Unique
    private static Integer spb$integerFormat(int internalFormat) {
        return switch (internalFormat) {
            case GL30C.GL_R8UI, GL30C.GL_R16UI, GL30C.GL_R32UI, GL30C.GL_R8I, GL30C.GL_R16I, GL30C.GL_R32I -> GL30C.GL_RED_INTEGER;
            case GL30C.GL_RG8UI, GL30C.GL_RG16UI, GL30C.GL_RG32UI, GL30C.GL_RG8I, GL30C.GL_RG16I, GL30C.GL_RG32I -> GL30C.GL_RG_INTEGER;
            case GL30C.GL_RGB8UI, GL30C.GL_RGB16UI, GL30C.GL_RGB32UI, GL30C.GL_RGB8I, GL30C.GL_RGB16I, GL30C.GL_RGB32I -> GL30C.GL_RGB_INTEGER;
            case GL30C.GL_RGBA8UI, GL30C.GL_RGBA16UI, GL30C.GL_RGBA32UI, GL30C.GL_RGBA8I, GL30C.GL_RGBA16I, GL30C.GL_RGBA32I -> GL30C.GL_RGBA_INTEGER;
            default -> null;
        };
    }
}
