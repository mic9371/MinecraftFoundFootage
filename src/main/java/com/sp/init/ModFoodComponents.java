package com.sp.init;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.component.type.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent CANNED_FOOD = new FoodComponent.Builder().nutrition(5).saturationModifier(0.8f).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 2), 0.05f).build();
    public static final FoodComponent BACKSHROOM = new FoodComponent.Builder().nutrition(3).saturationModifier(0.4f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 2), 0.1f).build();
}
