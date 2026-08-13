package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.ThreadNames;







import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class EllieNpcModel extends GirlModel<BaseGirlEntity> {
   HashMap<Integer, float[]> f = new HashMap<Integer, float[]>() {
      {
         this.put(0, new float[]{0.0F, -1.2F, 1.2F});
         this.put(-90, new float[]{2.0F, -71.56F, -68.0F});
         this.put(90, new float[]{-2.0F, 68.0F, 70.5F});
      }
   };

   public EllieNpcModel() {
      this.c = this.getModelLocations();
   }

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{new ResourceLocation("sexmod", "geo/ellie/nude.geo.json"), new ResourceLocation("sexmod", "geo/ellie/dressed.geo.json")};
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var0) {
      return new ResourceLocation("sexmod", "textures/entity/ellie/ellie.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/ellie/ellie.animation.json");
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations(var1, var2, var3);
      if (!(var1.field_70170_p instanceof SexWorldClient)) {
         if (!(var1 instanceof AbstractPlayerGirlEntity)) {
            if (var1.getCurrentAction() == fp.SITDOWNIDLE) {
               EntityPlayer var4 = var1.field_70170_p.func_72890_a(var1, 15.0);
               if (var4 != null) {
                  IBone var5 = this.getAnimationProcessor().getBone("head");
                  Vec3d var6 = var1.func_174791_d().func_178788_d(var4.func_174791_d());
                  int var7 = Math.round(var1.getYawRotation());
                  float var12;
                  if (var7 == 180) {
                     var12 = (float)Math.atan2(var6.field_72450_a, var6.field_72449_c) * 1.2F;
                     if (var12 > 0.0F) {
                        var12 = Math.max(1.5F, Math.min(3.14F, var12));
                     } else {
                        var12 = Math.max(-3.14F, Math.min(-1.5F, var12));
                     }

                     if (var12 != 1.5F && var12 != 3.14F && var12 != -3.14F && var12 != -1.5F) {
                        var12 += 3.0F;
                     } else {
                        var12 = 0.0F;
                     }
                  } else {
                     float var9 = this.f.get(var7)[1];
                     float var10 = this.f.get(var7)[2];
                     var12 = ((float)(Math.atan2(var6.field_72450_a, var6.field_72449_c) + this.f.get(var7)[0]) + var1.getYawRotation()) * 0.8F;
                     var12 = ThreadNames.b(var12, var9, var10);
                     if (var12 == var9 || var12 == var10) {
                        var12 = 0.0F;
                     }
                  }

                  float var14 = var12 == 0.0F ? 0.0F : ThreadNames.b((float)((var4.field_70163_u - var1.field_70163_u) * 0.5), -0.75F, 0.75F);
                  var5.setRotationY(var12);
                  var5.setRotationX(var14);
               }
            }
         }
      }
   }

   @Override
   public String[] c() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] g() {
      return new String[]{"headband"};
   }

   @Override
   public String[] f() {
      return new String[]{"armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs"};
   }

   @Override
   public String[] a() {
      return new String[]{"boobsFlesh", "upperBodyL", "upperBodyR"};
   }

   @Override
   public String[] h() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] e() {
      return new String[]{"fleshL", "fleshR", "vagina", "hotpants", "slip", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] b() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }

}
