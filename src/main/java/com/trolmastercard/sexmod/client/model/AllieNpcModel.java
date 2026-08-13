package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AlliePlayerEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







import net.minecraft.util.ResourceLocation;

public class AllieNpcModel extends GirlModel<BaseGirlEntity> {
   @Override
   protected ResourceLocation[] a_clash33() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/allie/allie.geo.json"),
         new ResourceLocation("sexmod", "geo/allie/armored.geo.json"),
         new ResourceLocation("sexmod", "geo/allie/allie.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/allie/allie.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/allie/allie.animation.json");
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
      return new String[]{"boobsFlesh", "clothes", "clothesR", "clothesL"};
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
