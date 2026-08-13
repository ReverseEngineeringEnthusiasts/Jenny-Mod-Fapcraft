package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.api.IKobold;
import com.trolmastercard.sexmod.entity.Action;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class KoboldNpcModel extends GirlModel<BaseGirlEntity> {
   static final float swingProgress = 1.2F;
   static final float legSwing = 1.0F;

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/kobold/kobold.geo.json"), new ResourceLocation("sexmod", "geo/kobold/armored.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var0) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/kobold.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/kobold/kobold.animation.json");
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations(var1, var2, var3);
      if (!(var1.world instanceof SexWorldClient)) {
         AnimationProcessor var4 = this.getAnimationProcessor();
         if (!var1.isLocallyRegistered() && var1 instanceof KoboldEntity) {
            var4.getBone("crown").setHidden(!(Boolean)var1.getDataManager().get(KoboldEntity.aZ));
            var4.getBone("egg").setHidden(!((KoboldEntity)var1).isRenderEgg);
         } else {
            var4.getBone("crown").setHidden(true);
            var4.getBone("egg").setHidden(true);
         }

         String[] var5 = AbstractNpcOnlyEntity.getModelCodeParts(var1);
         this.b(var4, var5[0]);
         this.e(var4, var5[1]);
         this.a(var4, var5[2], 0.75F, 1.35F, "boobL", "boobR", "armorBoobs");
         this.a(var4, var5[3], 1.0F, 1.2F, "eyeL", "eyeR");
         this.a(var4, var5[3], 1.0F, 1.2F);
         this.a(var4, var5[4]);
         this.d(var4, var5[5]);
         this.a(var1, var4, var5[6]);
         switch (var1.getCurrentAction()) {
            case STARTBLOWJOB:
            case SUCKBLOWJOB_BLINK:
            case THRUSTBLOWJOB:
            case CUMBLOWJOB:
               var4.getBone("tounge").setHidden(false);
               break;
            default:
               var4.getBone("tounge").setHidden(true);
         }

         this.b(var1, var4);
      }
   }

   void b(BaseGirlEntity var1, AnimationProcessor var2) {
      if (var1.actionController.getAnimationState() == AnimationState.Transitioning) {
         float var3 = (Float)var1.getDataManager().get(KoboldEntity.aE);
         var3 = 0.25F - var3;
         switch (var1.getCurrentAction()) {
            case SUCKBLOWJOB_BLINK:
            case THRUSTBLOWJOB:
            case CUMBLOWJOB:
               IBone var7 = var2.getBone("body");
               var7.setPositionZ(11.43F + var3 * -7.0F);
               return;
            case KOBOLD_ANAL_SLOW:
            case ANAL_FAST:
            case ANAL_CUM:
            case ANAL_START:
               IBone var6 = var2.getBone("body");
               var6.setPositionX(1.78F + var3 * -1.5F);
               var6.setPositionY(13.07F + var3 * -11.0F);
               var6.setPositionZ(2.05F + var3 * -8.0F);
               return;
            case MATING_PRESS_CUM:
            case MATING_PRESS_HARD:
            case MATING_PRESS_SOFT:
            case MATING_PRESS_START:
               IBone var4 = var2.getBone("body");
               var4.setPositionX(0.0F);
               var4.setPositionY(2.85F);
               var4.setPositionZ(-7.0F + var3 * 4.7F);
               return;
         }
      }
   }

   void a(BaseGirlEntity var1, AnimationProcessor var2, String var3) {
      int var4 = Integer.parseInt(var3);
      IBone var5 = var2.getBone("backpack");
      IBone var6 = var2.getBone("tailpack");
      switch (var4) {
         case 0:
            var5.setHidden(false);
            var6.setHidden(true);
            break;
         case 1:
            var5.setHidden(false);
            var6.setHidden(false);
            break;
         case 2:
            var5.setHidden(true);
            var6.setHidden(false);
            break;
         case 3:
            var5.setHidden(true);
            var6.setHidden(true);
      }

      if (var1.getCurrentAction() == Action.PAYMENT) {
         var5.setHidden(false);
      }
   }

   void d(AnimationProcessor var1, String var2) {
      int var3 = Integer.parseInt(var2);
      IBone var4 = var1.getBone("frecklesHR1");
      IBone var5 = var1.getBone("frecklesHR2");
      IBone var6 = var1.getBone("frecklesHL1");
      IBone var7 = var1.getBone("frecklesHL2");
      var6.setHidden(var3 != 1);
      var4.setHidden(var3 != 1);
      var7.setHidden(var3 != 2);
      var5.setHidden(var3 != 2);
   }

   void a(AnimationProcessor var1, String var2) {
      int var3 = Integer.parseInt(var2);
      IBone var4 = var1.getBone("frecklesAR1");
      IBone var5 = var1.getBone("frecklesAR2");
      IBone var6 = var1.getBone("frecklesAL1");
      IBone var7 = var1.getBone("frecklesAL2");
      var6.setHidden(var3 != 1);
      var4.setHidden(var3 != 1);
      var7.setHidden(var3 != 2);
      var5.setHidden(var3 != 2);
   }

   void a(AnimationProcessor var1, String var2, float var3, float var4) {
      if (!Minecraft.getMinecraft().isGamePaused()) {
         float var5 = Float.parseFloat(var2);
         var5 /= 100.0F;
         var5 = var3 + (var4 - var3) * var5 - 1.0F;
         IBone var6 = var1.getBone("eyeL");
         var6.setPositionX(var6.getPositionX() + var5);
         IBone var7 = var1.getBone("eyeR");
         var7.setPositionX(var7.getPositionX() - var5);
      }
   }

   void a(AnimationProcessor var1, String var2, float var3, float var4, String... var5) {
      float var6 = Float.parseFloat(var2);
      var6 /= 100.0F;
      var6 = var3 + (var4 - var3) * var6;

      for (String var10 : var5) {
         IBone var11 = var1.getBone(var10);
         if (var11 != null) {
            var11.setScaleX(var6);
            var11.setScaleY(var6);
            var11.setScaleZ(var6);
         }
      }
   }

   void e(AnimationProcessor var1, String var2) {
      List var3 = this.c(var1, "hornDL");
      List var4 = this.c(var1, "hornDR");
      this.hideAllBones(var3);
      this.hideAllBones(var4);
      int var5 = Integer.parseInt(var2);
      var1.getBone("hornDL" + var5).setHidden(false);
      var1.getBone("hornDR" + var5).setHidden(false);
   }

   void b(AnimationProcessor var1, String var2) {
      List var3 = this.c(var1, "hornUL");
      List var4 = this.c(var1, "hornUR");
      this.hideAllBones(var3);
      this.hideAllBones(var4);
      int var5 = Integer.parseInt(var2);
      var1.getBone("hornUL" + var5).setHidden(false);
      var1.getBone("hornUR" + var5).setHidden(false);
   }

   List<IBone> c(AnimationProcessor var1, String var2) {
      ArrayList var3 = new ArrayList();
      int var4 = 0;

      while (true) {
         IBone var5 = var1.getBone(var2 + var4);
         if (var5 == null) {
            return var3;
         }

         var3.add(var5);
         var4++;
      }
   }

   void hideAllBones(List<IBone> var1) {
      for (IBone var3 : var1) {
         var3.setHidden(true);
      }
   }

   @Override
   protected void a(BaseGirlEntity var1, AnimationProcessor var2, AnimationEvent var3) {
      if (!(var1.world instanceof SexWorldClient)) {
         switch (var1.getCurrentAction()) {
            case NULL:
               if (Math.abs(var1.prevPosX - var1.posX) + Math.abs(var1.prevPosZ - var1.posZ) < 0.0
                  || var1.onGround && Math.abs(Math.abs(var1.prevPosY) - Math.abs(var1.posY)) > 0.1F
                  || !((IKobold)var1).isBlockedByCeiling()) {
                  EntityModelData var4 = (EntityModelData) var3.getExtraDataOfType(EntityModelData.class).get(0);
                  IBone var5 = var2.getBone("head");
                  var5.setRotationY(var4.netHeadYaw * (float) (Math.PI / 180.0));
                  var5.setRotationX(var4.headPitch * (float) (Math.PI / 180.0));
                  IBone var6 = var2.getBone("body") == null ? var2.getBone("dd") : var2.getBone("body");
                  var6.setRotationY(0.0F);
                  return;
               }
         }
      }
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
      return new String[]{"boobsFlesh", "upperBodyL", "upperBodyR"};
   }

   @Override
   public String[] BottomArmor() {
      return new String[]{
         "armorBootyR",
         "armorBootyL",
         "armorPantsLowL",
         "armorPantsLowR",
         "armorPantsLowR",
         "armorPantsUpR",
         "armorPantsUpL",
         "armorHip",
         "armorKneeR",
         "armorKneeL"
      };
   }

   @Override
   public String[] Bottom() {
      return new String[]{"fleshL", "fleshR", "vagina", "fuckhole", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] ShoesArmor() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }

   @Override
   public String[] Shoes() {
      return new String[]{"toesR", "toesL"};
   }

}
