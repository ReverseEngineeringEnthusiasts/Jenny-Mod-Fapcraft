package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.item.WinchesterItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the Winchester summoning item (geo/texture/animation
 * locations only).
 */
public class SummonItemModel extends AnimatedGeoModel<WinchesterItem> {
   public ResourceLocation getModelLocation(WinchesterItem var0) { return new ResourceLocation("sexmod", "geo/west/winchester.geo.json"); }

   public ResourceLocation getTextureLocation(WinchesterItem var0) {
      return new ResourceLocation("sexmod", "textures/items/winchester/winchester.png");
   }

   public ResourceLocation getAnimationFileLocation(WinchesterItem var0) {
      return new ResourceLocation("sexmod", "animations/west/winchester.animation.json");
   }
}
