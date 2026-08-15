package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.SlimePlayerEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.TrailSegment;
import java.util.Arrays;
import javax.vecmath.Vector3f;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

/**
 * Geckolib model for the slime NPC: three outfit geo variants (nude, armored,
 * dressed) plus the bed-scene slime body — the {@code bedSlime}/
 * {@code bedSlimeLayer} bones are only visible during the doggy bed actions —
 * and a hat bone that mirrors the head's pose for plain NPCs.
 */
public class SlimeNpcModel extends GirlModel<BaseGirlEntity> {
   Action[] bedSlimeActions = new Action[]{Action.STARTDOGGY, Action.DOGGYCUM, Action.DOGGYSLOW, Action.DOGGYFAST, Action.DOGGYCUM, Action.DOGGYSTART, Action.WAITDOGGY};

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/slime/nude.geo.json"),
         new ResourceLocation("sexmod", "geo/slime/armored.geo.json"),
         new ResourceLocation("sexmod", "geo/slime/dressed.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/slime/slime.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "animations/slime/slime.animation.json");
   }

   /**
    * Toggles the bed-slime bones by action (visible only for the doggy bed
    * actions) and copies the head pose onto the hat bone for plain NPCs.
    */
   @Override
   public void setLivingAnimations(BaseGirlEntity entity, Integer uniqueID, AnimationEvent event) {
      super.setLivingAnimations(entity, uniqueID, event);
      AnimationProcessor processor = this.getAnimationProcessor();
      if (!(entity.world instanceof SexWorldClient) && processor.getBone("bedSlime") != null && processor.getBone("bedSlimeLayer") != null) {
         processor.getBone("bedSlime").setHidden(!Arrays.asList(this.bedSlimeActions).contains(entity.getCurrentAction()));
         processor.getBone("bedSlimeLayer").setHidden(!Arrays.asList(this.bedSlimeActions).contains(entity.getCurrentAction()));
      }

      if (!(entity instanceof AbstractPlayerGirlEntity)) {
         this.applyBoneName(new String[]{"head"}, "hat");
      }
   }

   /**
    * Sums the named bones' rotations/positions into the target bone — used to
    * bind the hat to the head's current pose.
    */
   void applyBoneName(String[] boneNames, String targetBoneName) {
      AnimationProcessor processor = this.getAnimationProcessor();
      IBone targetBone = processor.getBone(targetBoneName);
      IBone[] sourceBones = new IBone[boneNames.length];

      for (int i = 0; i < sourceBones.length; i++) {
         sourceBones[i] = processor.getBone(boneNames[i]);
      }

      Vector3f rotationSum = new Vector3f(0.0F, 0.0F, 0.0F);
      Vector3f positionSum = new Vector3f(0.0F, 0.0F, 0.0F);

      for (IBone bone : sourceBones) {
         rotationSum.add(new Vector3f(bone.getRotationX(), bone.getRotationY(), bone.getRotationZ()));
         positionSum.add(new Vector3f(bone.getPositionX(), bone.getPositionY(), bone.getPositionZ()));
      }

      targetBone.setRotationX(rotationSum.x);
      targetBone.setRotationY(rotationSum.y);
      targetBone.setRotationZ(rotationSum.z);
      targetBone.setPositionX(positionSum.x);
      targetBone.setPositionY(positionSum.y);
      targetBone.setPositionZ(positionSum.z);
      targetBone.setPositionZ(positionSum.z);
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] Attachments() {
      return new String[]{"bigblob"};
   }

   @Override
   public String[] TopArmor() {
      return new String[]{"armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs"};
   }

   @Override
   public String[] Top() {
      return new String[]{"boobsFlesh", "upperBodyL", "upperBodyR", "cloth"};
   }

   @Override
   public String[] BottomArmor() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] Bottom() {
      return new String[]{"fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] ShoesArmor() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }

}
