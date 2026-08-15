package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.entity.KoboldEggEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the kobold egg entity (thrown/spawned; geo/texture/
 * animation locations only).
 */
public class KoboldEggModel extends AnimatedGeoModel<KoboldEggEntity> {
   public ResourceLocation getModelLocation(KoboldEggEntity var0) { return new ResourceLocation("sexmod", "geo/kobold/koboldegg.geo.json"); }

   public ResourceLocation getTextureLocation(KoboldEggEntity var0) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/koboldegg.png");
   }

   public ResourceLocation getAnimationFileLocation(KoboldEggEntity var0) {
      return new ResourceLocation("sexmod", "animations/kobold/egg.animation.json");
   }
}
