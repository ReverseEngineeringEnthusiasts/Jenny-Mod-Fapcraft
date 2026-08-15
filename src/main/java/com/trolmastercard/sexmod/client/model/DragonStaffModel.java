package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.item.DragonStaffItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the dragon staff item (geo/texture/animation locations
 * only).
 */
public class DragonStaffModel extends AnimatedGeoModel<DragonStaffItem> {
   public ResourceLocation getModelLocation(DragonStaffItem item) { return new ResourceLocation("sexmod", "geo/kobold/staff.geo.json"); }

   public ResourceLocation getTextureLocation(DragonStaffItem item) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/staff.png");
   }

   public ResourceLocation getAnimationFileLocation(DragonStaffItem item) {
      return new ResourceLocation("sexmod", "animations/kobold/staff.animation.json");
   }
}
