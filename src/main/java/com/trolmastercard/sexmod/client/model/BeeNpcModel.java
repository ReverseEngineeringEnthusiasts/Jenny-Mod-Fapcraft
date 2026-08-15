package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

/**
 * Geckolib model for the bee NPC: bee geo (nude/armored variants) with a
 * chest-bone visibility toggle driven by the current animation name, plus the
 * standard head-look and armor/nude bone groups.
 */
public class BeeNpcModel extends GirlModel<BaseGirlEntity> {
   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{new ResourceLocation("sexmod", "geo/bee/bee.geo.json"), new ResourceLocation("sexmod", "geo/bee/armored.geo.json")};
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/bee/bee.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "animations/bee/bee.animation.json");
   }

   /**
    * Hides the {@code chest} bone unless the movement controller's current
    * animation is a chest animation (e.g. the chest-opening scene).
    */
   @Override
   public void setLivingAnimations(BaseGirlEntity entity, Integer uniqueID, AnimationEvent event) {
      super.setLivingAnimations(entity, uniqueID, event);
      if (!(entity.world instanceof SexWorldClient)) {
         AnimationProcessor processor = this.getAnimationProcessor();
         IBone chestBone = processor.getBone("chest");
         if (chestBone != null) {
            chestBone.setHidden(entity.movementController.getCurrentAnimation() == null || !entity.movementController.getCurrentAnimation().animationName.contains("chest"));
         }
      }
   }

   @Override
   protected void handleAnimationEvent(BaseGirlEntity entity, AnimationProcessor processor, AnimationEvent event) {
      if (!(entity.world instanceof SexWorldClient) && (entity.getCurrentAction() == Action.NULL || entity.getCurrentAction() == Action.ATTACK || entity.getCurrentAction() == Action.BOW)) {
         EntityModelData modelData = (EntityModelData) event.getExtraDataOfType(EntityModelData.class).get(0);
         IBone neckBone = processor.getBone("neck");
         neckBone.setRotationY(modelData.netHeadYaw * 0.5F * (float) (Math.PI / 180.0));
         IBone headBone = processor.getBone("head");
         headBone.setRotationY(modelData.netHeadYaw * (float) (Math.PI / 180.0));
         headBone.setRotationX(1.0F + modelData.headPitch * (float) (Math.PI / 180.0));
         IBone bodyBone = processor.getBone("body") == null ? processor.getBone("dd") : processor.getBone("body");
         bodyBone.setRotationY(0.0F);
      }
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] Attachments() {
      return new String[]{"band", "feeler", "feeler2", "brow", "brow2", "brow3", "brow4"};
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
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] Bottom() {
      return new String[]{"sideL", "sideR", "fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] ShoesArmor() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }

}
