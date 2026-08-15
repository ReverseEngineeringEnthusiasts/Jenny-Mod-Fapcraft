package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import net.minecraft.util.ResourceLocation;

/**
 * Geckolib model for the Bia NPC: nude/dressed geo variants, shared texture,
 * armor/nude bone groups with leaf attachments.
 */
public class BiaNpcModel extends GirlModel<BaseGirlEntity> {
   public BiaNpcModel() {
      this.modelLocations = this.getModelLocations();
   }

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{new ResourceLocation("sexmod", "geo/bia/bianude.geo.json"), new ResourceLocation("sexmod", "geo/bia/biadressed.geo.json")};
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/bia/bia.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) { return new ResourceLocation("sexmod", "animations/bia/bia.animation.json");
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] Attachments() {
      return new String[]{"leaf7", "leaf8"};
   }

   @Override
   public String[] TopArmor() {
      return new String[]{"armorChest", "armorBoobs", "armorShoulderR", "armorShoulderL"};
   }

   @Override
   public String[] Top() {
      return new String[]{"bra", "upperBodyR", "upperBodyL"};
   }

   @Override
   public String[] BottomArmor() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] Bottom() {
      return new String[]{"slip", "fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] ShoesArmor() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }
}
