package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import net.minecraft.util.ResourceLocation;

public class CatNpcModel extends GirlModel<BaseGirlEntity> {
   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{new ResourceLocation("sexmod", "geo/cat/cat.geo.json"), new ResourceLocation("sexmod", "geo/cat/cat.geo.json")};
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var0) {
      return new ResourceLocation("sexmod", "textures/entity/cat/cat.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var0) { return new ResourceLocation("sexmod", "animations/cat/cat.animation.json");
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] TopArmor() {
      return new String[]{"armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs"};
   }

   @Override
   public String[] Top() {
      return new String[]{"boobsFlesh", "cloth"};
   }

   @Override
   public String[] BottomArmor() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] Bottom() {
      return new String[]{"fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR", "cloth"};
   }

   @Override
   public String[] ShoesArmor() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }
}
