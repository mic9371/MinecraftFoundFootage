package com.sp.entity.client.model;

import net.minecraft.client.util.SkinTextures;
import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.entity.ik.model.GeckoLib.GeoModelAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class SkinWalkerModel extends GeoModel<SkinWalkerEntity> {
	private final Identifier SLIM_MODEL = Identifier.of(SPBRevamped.MOD_ID, "geo/entity/skin_walker_slim.geo.json");
	private final Identifier DEFAULT_MODEL = Identifier.of(SPBRevamped.MOD_ID, "geo/entity/skin_walker_default.geo.json");
	private final Identifier FINAL_MODEL = Identifier.of(SPBRevamped.MOD_ID, "geo/entity/skin_walker_final_default.geo.json");

	private final Identifier PLACEHOLDER_TEXTURE = Identifier.of(SPBRevamped.MOD_ID, "textures/entity/skinwalker/placeholder.png");
	private final Identifier STEVE_TEXTURE = Identifier.of("textures/entity/player/wide/steve.png");

	private final Identifier ANIMATION = Identifier.of(SPBRevamped.MOD_ID, "animations/entity/skinwalker.animation.json");

	@Override
	public void setCustomAnimations(SkinWalkerEntity animatable, long instanceId, AnimationState<SkinWalkerEntity> animationState) {
		super.setCustomAnimations(animatable, instanceId, animationState);
		SkinWalkerComponent component = InitializeComponents.SKIN_WALKER.get(animatable);

		if(component.isInTrueForm()) {
			animatable.tickComponentsClient(animatable, new GeoModelAccessor(this));
		}
	}

	@Override
	public Identifier getModelResource(SkinWalkerEntity animatable) {
		SkinWalkerComponent component = InitializeComponents.SKIN_WALKER.get(animatable);

		if(!component.isInTrueForm()) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world != null) {
				AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(component.getTargetPlayerUUID());
				if (player != null) {
					if (player.getSkinTextures().model() == SkinTextures.Model.SLIM) {
						return SLIM_MODEL;
					}
				}
			}

			return DEFAULT_MODEL;
		}

		return FINAL_MODEL;
	}

	@Override
	public Identifier getTextureResource(SkinWalkerEntity animatable) {
		MinecraftClient client = MinecraftClient.getInstance();
		SkinWalkerComponent component = InitializeComponents.SKIN_WALKER.get(animatable);
		if(client.world != null){
			AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(component.getTargetPlayerUUID());
			if(player != null) {
				return player.getSkinTextures().texture();
			}
		}

		return PLACEHOLDER_TEXTURE;
	}

	@Override
	public Identifier getAnimationResource(SkinWalkerEntity animatable) {
		return ANIMATION;
	}

}