package com.trolmastercard.sexmod.client.model.item;

import com.trolmastercard.sexmod.common.item.WinchesterItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the Winchester summoning item (geo/texture/animation
 * locations only).
 */
public class WinchesterModel extends AnimatedGeoModel<WinchesterItem> {
   public ResourceLocation getModelLocation(WinchesterItem item) { return new ResourceLocation("sexmod", "geo/west/winchester.geo.json"); }

   public ResourceLocation getTextureLocation(WinchesterItem item) {
      return new ResourceLocation("sexmod", "textures/items/winchester/winchester.png");
   }

   public ResourceLocation getAnimationFileLocation(WinchesterItem item) {
      return new ResourceLocation("sexmod", "animations/west/winchester.animation.json");
   }
}
