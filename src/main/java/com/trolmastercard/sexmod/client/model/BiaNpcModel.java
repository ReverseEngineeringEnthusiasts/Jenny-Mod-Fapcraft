package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import net.minecraft.util.ResourceLocation;

public class BiaNpcModel extends GirlModel<BaseGirlEntity> {
   public BiaNpcModel() {
      this.c = this.a_clash33();
   }

   @Override
   protected ResourceLocation[] a_clash33() {
      return new ResourceLocation[]{new ResourceLocation("sexmod", "geo/bia/bianude.geo.json"), new ResourceLocation("sexmod", "geo/bia/biadressed.geo.json")};
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var0) {
      return new ResourceLocation("sexmod", "textures/entity/bia/bia.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var0) { return new ResourceLocation("sexmod", "animations/bia/bia.animation.json");
   }

   @Override
   public String[] c() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] g() {
      return new String[]{"leaf7", "leaf8"};
   }

   @Override
   public String[] f() {
      return new String[]{"armorChest", "armorBoobs", "armorShoulderR", "armorShoulderL"};
   }

   @Override
   public String[] a() {
      return new String[]{"bra", "upperBodyR", "upperBodyL"};
   }

   @Override
   public String[] h() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] e() {
      return new String[]{"slip", "fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] b() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }
}
