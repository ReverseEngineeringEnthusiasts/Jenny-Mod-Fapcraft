package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.item.DragonStaffItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DragonStaffModel extends AnimatedGeoModel<DragonStaffItem> {
   public ResourceLocation getModelLocation(DragonStaffItem var0) { return new ResourceLocation("sexmod", "geo/kobold/staff.geo.json"); }

   public ResourceLocation getTextureLocation(DragonStaffItem var0) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/staff.png");
   }

   public ResourceLocation getAnimationFileLocation(DragonStaffItem var0) {
      return new ResourceLocation("sexmod", "animations/kobold/staff.animation.json");
   }
}
