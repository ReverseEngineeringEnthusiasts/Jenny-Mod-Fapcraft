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
   public String[] c() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] f() {
      return new String[]{"armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs"};
   }

   @Override
   public String[] a() {
      return new String[]{"boobsFlesh", "cloth"};
   }

   @Override
   public String[] h() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] e() {
      return new String[]{"fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR", "cloth"};
   }

   @Override
   public String[] b() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }
}
