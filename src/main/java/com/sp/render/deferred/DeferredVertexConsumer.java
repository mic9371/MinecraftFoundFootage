// Vendored from Veil 1.0.0.228 for 1.20.1 (LGPL-3.0, https://github.com/FoundryMC/Veil) and ported to 1.21.1.
// Veil for 1.21.1 replaced its deferred renderer with "dynamic buffers", which this mod's render pipeline cannot use.
package com.sp.render.deferred;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DeferredVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private boolean verticalNormal;

    public DeferredVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer vertex(float pX, float pY, float pZ) {
        this.delegate.vertex(pX, pY, pZ);
        return this;
    }

    @Override
    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        this.delegate.color(pRed, pGreen, pBlue, pAlpha);
        return this;
    }

    @Override
    public VertexConsumer texture(float pU, float pV) {
        this.delegate.texture(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer overlay(int pU, int pV) {
        this.delegate.overlay(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer light(int pU, int pV) {
        this.delegate.light(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer normal(float pX, float pY, float pZ) {
        if (this.verticalNormal) {
            this.delegate.normal(0, 1, 0);
        } else {
            this.delegate.normal(pX, pY, pZ);
        }
        return this;
    }

    @Override
    public void quad(MatrixStack.Entry pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, float pAlpha, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor) {
        this.verticalNormal = !pQuad.hasShade();
        VertexConsumer.super.quad(pPoseEntry, pQuad, pColorMuls, pRed, pGreen, pBlue, pAlpha, pCombinedLights, pCombinedOverlay, pMulColor);
        this.verticalNormal = false;
    }
}