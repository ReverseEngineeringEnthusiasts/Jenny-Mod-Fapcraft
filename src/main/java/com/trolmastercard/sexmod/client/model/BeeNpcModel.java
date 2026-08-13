package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;







import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class BeeNpcModel extends GirlModel<BaseGirlEntity> {
   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{new ResourceLocation("sexmod", "geo/bee/bee.geo.json"), new ResourceLocation("sexmod", "geo/bee/armored.geo.json")};
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var0) {
      return new ResourceLocation("sexmod", "textures/entity/bee/bee.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/bee/bee.animation.json");
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations(var1, var2, var3);
      if (!(var1.world instanceof SexWorldClient)) {
         AnimationProcessor var4 = this.getAnimationProcessor();
         IBone var5 = var4.getBone("chest");
         if (var5 != null) {
            var5.setHidden(var1.movementController.getCurrentAnimation() == null || !var1.movementController.getCurrentAnimation().animationName.contains("chest"));
         }
      }
   }

   @Override
   protected void a(BaseGirlEntity var1, AnimationProcessor var2, AnimationEvent var3) {
      if (!(var1.world instanceof SexWorldClient) && (var1.getCurrentAction() == Action.NULL || var1.getCurrentAction() == Action.ATTACK || var1.getCurrentAction() == Action.BOW)) {
         EntityModelData var4 = (EntityModelData) var3.getExtraDataOfType(EntityModelData.class).get(0);
         IBone var5 = var2.getBone("neck");
         var5.setRotationY(var4.netHeadYaw * 0.5F * (float) (Math.PI / 180.0));
         IBone var6 = var2.getBone("head");
         var6.setRotationY(var4.netHeadYaw * (float) (Math.PI / 180.0));
         var6.setRotationX(1.0F + var4.headPitch * (float) (Math.PI / 180.0));
         IBone var7 = var2.getBone("body") == null ? var2.getBone("dd") : var2.getBone("body");
         var7.setRotationY(0.0F);
      }
   }

   @Override
   public String[] c() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] g() {
      return new String[]{"band", "feeler", "feeler2", "brow", "brow2", "brow3", "brow4"};
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
      return new String[]{"sideL", "sideR", "fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] b() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }

}
