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

/**
 * Geckolib model for the kobold NPC: kobold geo (nude/armored variants) whose
 * per-frame pass applies the owner's model code — horn variants (up/down),
 * boob/eye scales, freckle variants, backpack/tailpack pose, crown/egg
 * visibility — plus action-specific body positioning during the
 * blowjob/anal/mating-press scenes (transition-time interpolation) and a
 * tongue bone shown only for blowjob actions.
 * <p>
 * <b>Pitfall:</b> the scene body offsets in {@link #handleSwingAnimation}
 * depend on the action controller being in {@code Transitioning} state and
 * the {@code aE} data value (0.25 - value); changing either breaks the scene
 * positioning.
 */
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
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/kobold.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "animations/kobold/kobold.animation.json");
   }

   /**
    * Per-frame pass: crown/egg visibility (real kobolds only), model-code
    * part application (horns, scales, freckles, packs, colors), tongue
    * visibility by action, then the scene swing animation. Skipped in the
    * {@link SexWorldClient} preload world.
    */
   @Override
   public void setLivingAnimations(BaseGirlEntity entity, Integer uniqueID, AnimationEvent event) {
      super.setLivingAnimations(entity, uniqueID, event);
      if (!(entity.world instanceof SexWorldClient)) {
         AnimationProcessor processor = this.getAnimationProcessor();
         if (!entity.isLocallyRegistered() && entity instanceof KoboldEntity) {
            processor.getBone("crown").setHidden(!(Boolean)entity.getDataManager().get(KoboldEntity.aZ));
            processor.getBone("egg").setHidden(!((KoboldEntity)entity).isRenderEgg);
         } else {
            processor.getBone("crown").setHidden(true);
            processor.getBone("egg").setHidden(true);
         }

         String[] modelCodeParts = AbstractNpcOnlyEntity.getModelCodeParts(entity);
         this.getHornsUp(processor, modelCodeParts[0]);
         this.getHornsDown(processor, modelCodeParts[1]);
         this.setBoneRotationMulti(processor, modelCodeParts[2], 0.75F, 1.35F, "boobL", "boobR", "armorBoobs");
         this.setBoneRotationMulti(processor, modelCodeParts[3], 1.0F, 1.2F, "eyeL", "eyeR");
         this.setBoneRotation(processor, modelCodeParts[3], 1.0F, 1.2F);
         this.getBoneData(processor, modelCodeParts[4]);
         this.parseBoneColor(processor, modelCodeParts[5]);
         this.updateBonePose(entity, processor, modelCodeParts[6]);
         switch (entity.getCurrentAction()) {
            case STARTBLOWJOB:
            case SUCKBLOWJOB_BLINK:
            case THRUSTBLOWJOB:
            case CUMBLOWJOB:
               processor.getBone("tounge").setHidden(false);
               break;
            default:
               processor.getBone("tounge").setHidden(true);
         }

         this.handleSwingAnimation(entity, processor);
      }
   }

   /**
    * Scene body positioning while the controller transitions into a scene
    * action: blowjob actions drive the body forward (positionZ), anal actions
    * lower+shift it, mating-press actions raise it — all interpolated from
    * the {@code aE} transition value. See class javadoc pitfall.
    */
   void handleSwingAnimation(BaseGirlEntity entity, AnimationProcessor processor) {
      if (entity.actionController.getAnimationState() == AnimationState.Transitioning) {
         float transitionValue = (Float)entity.getDataManager().get(KoboldEntity.aE);
         transitionValue = 0.25F - transitionValue;
         switch (entity.getCurrentAction()) {
            case SUCKBLOWJOB_BLINK:
            case THRUSTBLOWJOB:
            case CUMBLOWJOB:
               IBone blowjobBodyBone = processor.getBone("body");
               blowjobBodyBone.setPositionZ(11.43F + transitionValue * -7.0F);
               return;
            case KOBOLD_ANAL_SLOW:
            case ANAL_FAST:
            case ANAL_CUM:
            case ANAL_START:
               IBone analBodyBone = processor.getBone("body");
               analBodyBone.setPositionX(1.78F + transitionValue * -1.5F);
               analBodyBone.setPositionY(13.07F + transitionValue * -11.0F);
               analBodyBone.setPositionZ(2.05F + transitionValue * -8.0F);
               return;
            case MATING_PRESS_CUM:
            case MATING_PRESS_HARD:
            case MATING_PRESS_SOFT:
            case MATING_PRESS_START:
               IBone matingBodyBone = processor.getBone("body");
               matingBodyBone.setPositionX(0.0F);
               matingBodyBone.setPositionY(2.85F);
               matingBodyBone.setPositionZ(-7.0F + transitionValue * 4.7F);
               return;
         }
      }
   }

   /**
    * Backpack/tailpack pose from the model code (0..3 combinations of
    * visible/hidden), with the backpack forced visible during PAYMENT.
    */
   void updateBonePose(BaseGirlEntity entity, AnimationProcessor processor, String modelCode) {
      int poseValue = Integer.parseInt(modelCode);
      IBone backpackBone = processor.getBone("backpack");
      IBone tailpackBone = processor.getBone("tailpack");
      switch (poseValue) {
         case 0:
            backpackBone.setHidden(false);
            tailpackBone.setHidden(true);
            break;
         case 1:
            backpackBone.setHidden(false);
            tailpackBone.setHidden(false);
            break;
         case 2:
            backpackBone.setHidden(true);
            tailpackBone.setHidden(false);
            break;
         case 3:
            backpackBone.setHidden(true);
            tailpackBone.setHidden(true);
      }

      if (entity.getCurrentAction() == Action.PAYMENT) {
         backpackBone.setHidden(false);
      }
   }

   /**
    * Freckle-head variant selection from the model code (1 = variant 1 pair,
    * 2 = variant 2 pair, else hidden).
    */
   void parseBoneColor(AnimationProcessor processor, String modelCode) {
      int variant = Integer.parseInt(modelCode);
      IBone frecklesHR1Bone = processor.getBone("frecklesHR1");
      IBone frecklesHR2Bone = processor.getBone("frecklesHR2");
      IBone frecklesHL1Bone = processor.getBone("frecklesHL1");
      IBone frecklesHL2Bone = processor.getBone("frecklesHL2");
      frecklesHL1Bone.setHidden(variant != 1);
      frecklesHR1Bone.setHidden(variant != 1);
      frecklesHL2Bone.setHidden(variant != 2);
      frecklesHR2Bone.setHidden(variant != 2);
   }

   /**
    * Freckle-arm variant selection (same 1/2 semantics as the head variant).
    */
   void getBoneData(AnimationProcessor processor, String modelCode) {
      int variant = Integer.parseInt(modelCode);
      IBone frecklesAR1Bone = processor.getBone("frecklesAR1");
      IBone frecklesAR2Bone = processor.getBone("frecklesAR2");
      IBone frecklesAL1Bone = processor.getBone("frecklesAL1");
      IBone frecklesAL2Bone = processor.getBone("frecklesAL2");
      frecklesAL1Bone.setHidden(variant != 1);
      frecklesAR1Bone.setHidden(variant != 1);
      frecklesAL2Bone.setHidden(variant != 2);
      frecklesAR2Bone.setHidden(variant != 2);
   }

   /**
    * Eye-spacing from the model code: shifts both eyes apart/symmetrically by
    * the normalized value (0..1 lerp between the given bounds minus 1).
    */
   void setBoneRotation(AnimationProcessor processor, String modelCode, float min, float max) {
      if (!Minecraft.getMinecraft().isGamePaused()) {
         float eyeSpacing = Float.parseFloat(modelCode);
         eyeSpacing /= 100.0F;
         eyeSpacing = min + (max - min) * eyeSpacing - 1.0F;
         IBone eyeLBone = processor.getBone("eyeL");
         eyeLBone.setPositionX(eyeLBone.getPositionX() + eyeSpacing);
         IBone eyeRBone = processor.getBone("eyeR");
         eyeRBone.setPositionX(eyeRBone.getPositionX() - eyeSpacing);
      }
   }

   /**
    * Uniform scale for the named bones from the model code (0..1 lerp between
    * the given bounds) — used for boobs and eyes.
    */
   void setBoneRotationMulti(AnimationProcessor processor, String modelCode, float min, float max, String... boneNames) {
      float scale = Float.parseFloat(modelCode);
      scale /= 100.0F;
      scale = min + (max - min) * scale;

      for (String boneName : boneNames) {
         IBone bone = processor.getBone(boneName);
         if (bone != null) {
            bone.setScaleX(scale);
            bone.setScaleY(scale);
            bone.setScaleZ(scale);
         }
      }
   }

   /**
    * Horn part selection: hides every down-horn variant bone and shows the
    * one chosen by the model code.
    */
   void getHornsDown(AnimationProcessor processor, String modelCode) {
      List downLeftBones = this.getHornBones(processor, "hornDL");
      List downRightBones = this.getHornBones(processor, "hornDR");
      this.hideAllBones(downLeftBones);
      this.hideAllBones(downRightBones);
      int variant = Integer.parseInt(modelCode);
      processor.getBone("hornDL" + variant).setHidden(false);
      processor.getBone("hornDR" + variant).setHidden(false);
   }

   /**
    * Up-horn variant selection (mirror of {@link #getHornsDown}).
    */
   void getHornsUp(AnimationProcessor processor, String modelCode) {
      List upLeftBones = this.getHornBones(processor, "hornUL");
      List upRightBones = this.getHornBones(processor, "hornUR");
      this.hideAllBones(upLeftBones);
      this.hideAllBones(upRightBones);
      int variant = Integer.parseInt(modelCode);
      processor.getBone("hornUL" + variant).setHidden(false);
      processor.getBone("hornUR" + variant).setHidden(false);
   }

   /**
    * Collects all bones sharing the given prefix (numbered suffixes until a
    * null bone) — used for the horn variants.
    */
   List<IBone> getHornBones(AnimationProcessor processor, String prefix) {
      ArrayList bones = new ArrayList();
      int i = 0;

      while (true) {
         IBone bone = processor.getBone(prefix + i);
         if (bone == null) {
            return bones;
         }

         bones.add(bone);
         i++;
      }
   }

   void hideAllBones(List<IBone> bones) {
      for (IBone bone : bones) {
         bone.setHidden(true);
      }
   }

   /**
    * Head-look for the idle action, skipped while the kobold is blocked by a
    * ceiling or on an unstable surface (the scene poses take over).
    */
   @Override
   protected void handleAnimationEvent(BaseGirlEntity entity, AnimationProcessor processor, AnimationEvent event) {
      if (!(entity.world instanceof SexWorldClient)) {
         switch (entity.getCurrentAction()) {
            case NULL:
               if (Math.abs(entity.prevPosX - entity.posX) + Math.abs(entity.prevPosZ - entity.posZ) < 0.0
                  || entity.onGround && Math.abs(Math.abs(entity.prevPosY) - Math.abs(entity.posY)) > 0.1F
                  || !((IKobold)entity).isBlockedByCeiling()) {
                  EntityModelData modelData = (EntityModelData) event.getExtraDataOfType(EntityModelData.class).get(0);
                  IBone headBone = processor.getBone("head");
                  headBone.setRotationY(modelData.netHeadYaw * (float) (Math.PI / 180.0));
                  headBone.setRotationX(modelData.headPitch * (float) (Math.PI / 180.0));
                  IBone bodyBone = processor.getBone("body") == null ? processor.getBone("dd") : processor.getBone("body");
                  bodyBone.setRotationY(0.0F);
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
