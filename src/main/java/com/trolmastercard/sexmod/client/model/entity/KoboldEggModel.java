package com.trolmastercard.sexmod.client.model.entity;

import com.trolmastercard.sexmod.common.entity.kobold.KoboldEggEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the kobold egg entity (thrown/spawned; geo/texture/
 * animation locations only).
 */
public class KoboldEggModel extends AnimatedGeoModel<KoboldEggEntity> {
   public ResourceLocation getModelLocation(KoboldEggEntity egg) { return new ResourceLocation("sexmod", "geo/kobold/koboldegg.geo.json"); }

   public ResourceLocation getTextureLocation(KoboldEggEntity egg) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/koboldegg.png");
   }

   public ResourceLocation getAnimationFileLocation(KoboldEggEntity egg) {
      return new ResourceLocation("sexmod", "animations/kobold/egg.animation.json");
   }
}
