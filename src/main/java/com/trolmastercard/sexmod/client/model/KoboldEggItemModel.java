package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.item.KoboldEggItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the kobold egg item (held stack; geo/texture/animation
 * locations only).
 */
public class KoboldEggItemModel extends AnimatedGeoModel<KoboldEggItem> {
   public ResourceLocation getModelLocation(KoboldEggItem item) { return new ResourceLocation("sexmod", "geo/kobold/koboldegg.geo.json"); }

   public ResourceLocation getTextureLocation(KoboldEggItem item) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/koboldegg.png");
   }

   public ResourceLocation getAnimationFileLocation(KoboldEggItem item) {
      return new ResourceLocation("sexmod", "animations/kobold/egg.animation.json");
   }
}
