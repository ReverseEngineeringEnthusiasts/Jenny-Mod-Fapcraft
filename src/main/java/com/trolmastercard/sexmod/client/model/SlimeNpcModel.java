package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.SlimePlayerEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.an;







import java.util.Arrays;
import javax.vecmath.Vector3f;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

public class SlimeNpcModel extends GirlModel<BaseGirlEntity> {
   fp[] f = new fp[]{fp.STARTDOGGY, fp.DOGGYCUM, fp.DOGGYSLOW, fp.DOGGYFAST, fp.DOGGYCUM, fp.DOGGYSTART, fp.WAITDOGGY};

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/slime/nude.geo.json"),
         new ResourceLocation("sexmod", "geo/slime/armored.geo.json"),
         new ResourceLocation("sexmod", "geo/slime/dressed.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/slime/slime.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/slime/slime.animation.json");
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations(var1, var2, var3);
      AnimationProcessor var4 = this.getAnimationProcessor();
      if (!(var1.world instanceof SexWorldClient) && var4.getBone("bedSlime") != null && var4.getBone("bedSlimeLayer") != null) {
         var4.getBone("bedSlime").setHidden(!Arrays.asList(this.f).contains(var1.getCurrentAction()));
         var4.getBone("bedSlimeLayer").setHidden(!Arrays.asList(this.f).contains(var1.getCurrentAction()));
      }

      if (!(var1 instanceof AbstractPlayerGirlEntity)) {
         this.a(new String[]{"head"}, "hat");
      }
   }

   void a(String[] var1, String var2) {
      AnimationProcessor var3 = this.getAnimationProcessor();
      IBone var4 = var3.getBone(var2);
      IBone[] var5 = new IBone[var1.length];

      for (int var6 = 0; var6 < var5.length; var6++) {
         var5[var6] = var3.getBone(var1[var6]);
      }

      Vector3f var12 = new Vector3f(0.0F, 0.0F, 0.0F);
      Vector3f var7 = new Vector3f(0.0F, 0.0F, 0.0F);

      for (IBone var11 : var5) {
         var12.add(new Vector3f(var11.getRotationX(), var11.getRotationY(), var11.getRotationZ()));
         var7.add(new Vector3f(var11.getPositionX(), var11.getPositionY(), var11.getPositionZ()));
      }

      var4.setRotationX(var12.x);
      var4.setRotationY(var12.y);
      var4.setRotationZ(var12.z);
      var4.setPositionX(var7.x);
      var4.setPositionY(var7.y);
      var4.setPositionZ(var7.z);
      var4.setPositionZ(var7.z);
   }

   @Override
   public String[] c() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] g() {
      return new String[]{"bigblob"};
   }

   @Override
   public String[] f() {
      return new String[]{"armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs"};
   }

   @Override
   public String[] a() {
      return new String[]{"boobsFlesh", "upperBodyL", "upperBodyR", "cloth"};
   }

   @Override
   public String[] h() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] e() {
      return new String[]{"fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] b() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }

}
