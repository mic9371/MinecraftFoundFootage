package com.sp.mixin;

import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SmilerComponent;
import com.sp.entity.custom.SmilerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    private LivingEntity entity;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void getEntity(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
        this.entity = livingEntity;
    }

    // 1.20.1 replaced the four float RGBA constants passed to EntityModel#render; since 1.21 that is one ARGB int.
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"),
            index = 4)
    private int setOpacity(int color){
        if(entity instanceof SmilerEntity) {
            SmilerComponent component = InitializeComponents.SMILER.get(entity);
            float opacity = component.getOpacity();
            return ColorHelper.Argb.fromFloats(opacity, opacity, opacity, opacity);
        }

        return color;
    }

}
