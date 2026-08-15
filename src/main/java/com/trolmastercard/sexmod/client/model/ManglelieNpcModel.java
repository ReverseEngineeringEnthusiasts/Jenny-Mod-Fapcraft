package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.renderer.ManglelieRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.Vector2f;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.TrigMath;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

/**
 * Geckolib model for Manglelie: the manglelie geo (with a combined Galath pose
 * variant for outfit index 2) whose per-frame pass runs the corruption arm/
 * head animation, the ride-mommy pose blend, the threesome pose (body follows
 * Galath's published rotation/scale) and the skirt/cheek visibility for the
 * look pose.
 * <p>
 * <b>Corruption animation.</b> While corrupting, the arms swing toward/away
 * from Galath with a frame-rate independent blend cycle
 * ({@code VELOCITY_0}/{@code aj} on the entity) between the corrupt pose and
 * the ride pose; the head tracks Galath with a speed-limited chase
 * ({@code TICK_0}/{@code ai}). The entity fields are written by this class —
 * do not move this logic.
 * <p>
 * CLIENT-side only; skipped in the {@link SexWorldClient} preload world and
 * during preloading.
 */
public class ManglelieNpcModel extends GirlModel<BaseGirlEntity> {
   public static final float HEAD_ROTATION_SPEED = 7.0F;
   public static final float headRotSpeed = 0.75F;
   static final float LOWER_ARM_BASE_ANGLE = TrigMath.wrapDegrees(140.0F);
   static final float UPPER_ARM_BASE_ANGLE = TrigMath.wrapDegrees(35.0F);
   static final float armSwing = 90.0F;
   static final float ARM_SWING_ANGLE = TrigMath.wrapDegrees(45.0F);
   static final float CORRUPTION_POSE_OFFSET = TrigMath.wrapDegrees(-45.0F);
   public static final ResourceLocation MANGLELIE_TEXTURE = new ResourceLocation("sexmod", "textures/entity/manglelie/manglelie.png");

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/manglelie/manglelie.geo.json"),
         new ResourceLocation("sexmod", "geo/manglelie/manglelie.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath_con_mang.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/manglelie/manglelie.png");
   }

   /**
    * Whether the girl is in a threesome action (slow/fast/cum) — disables the
    * corruption and look poses.
    */
   public static boolean isInThreesome(BaseGirlEntity girl) {
      return Action.isAnyAction(girl, Action.THREESOME_SLOW, Action.THREESOME_FAST, Action.THREESOME_CUM);
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "animations/manglelie/manglelie.animation.json");
   }

   /**
    * Per-frame pass: shared animation (skirt/cheek/cock-stage bones), then
    * the corruption arm/head poses, threesome body coupling and the
    * corruption leg/arm offset.
    */
   @Override
   public void setLivingAnimations(BaseGirlEntity entity, Integer uniqueID, AnimationEvent event) {
      super.setLivingAnimations(entity, uniqueID, event);
      animateModel(entity, this.getAnimationProcessor(), event.getPartialTick());
      this.updatePoseBones(entity);
      this.updateCorruptBones(entity);
      this.updateThreesomePose(entity);
      this.updateCorruptPose(entity);
   }

   /**
    * Static corruption offset on the leg/forearms while Galath runs her
    * corruption actions (not in threesome).
    */
   void updateCorruptPose(BaseGirlEntity entity) {
      if (!this.mc.isGamePaused()) {
         if (!isInThreesome(entity)) {
            GalathEntity galath = ManglelieEntity.getGalathPartnerOf(entity, false);
            if (galath != null) {
               if (Action.isAny(galath.getCurrentAction(), Action.CORRUPT_CUM, Action.CARRY_FAST, Action.CORRUPT_INTRO, Action.CORRUPT_SLOW)) {
                  AnimationProcessor processor = this.getAnimationProcessor();
                  IBone legRBone = processor.getBone("legR");
                  legRBone.setRotationY(legRBone.getRotationY() + CORRUPTION_POSE_OFFSET);
                  IBone lowerArmRBone = processor.getBone("lowerArmR");
                  IBone lowerArmLBone = processor.getBone("lowerArmL");
                  lowerArmRBone.setRotationX(lowerArmRBone.getRotationX() + CORRUPTION_POSE_OFFSET);
                  lowerArmLBone.setRotationX(lowerArmLBone.getRotationX() + CORRUPTION_POSE_OFFSET);
               }
            }
         }
      }
   }

   /**
    * Threesome coupling: the body yaw follows Galath's published rotation
    * ({@code bw}) and its scale follows {@code bm} — Manglelie mirrors
    * Galath's pose in the threesome scenes.
    */
   void updateThreesomePose(BaseGirlEntity entity) {
      if (entity instanceof ManglelieEntity) {
         if (!isInThreesome(entity)) {
            ManglelieEntity manglelie = (ManglelieEntity)entity;
            GalathEntity galath = manglelie.getGalathPartner(false);
            if (galath != null) {
               IBone bodyBone = this.getAnimationProcessor().getBone("body");
               bodyBone.setRotationY(galath.bw + (this.mc.isGamePaused() ? 0.0F : bodyBone.getRotationY()));
               bodyBone.setScaleX(galath.bm);
               bodyBone.setScaleY(galath.bm);
               bodyBone.setScaleZ(galath.bm);
            }
         }
      }
   }

   Vec3d getLookVector(@Nonnull Entity entity) {
      return EntityLookVectorHelper.getEntityLookVector(entity, this.mc.getRenderPartialTicks()).add(0.0, entity.getEyeHeight(), 0.0);
   }

   /**
    * Corruption arm animation: blends between the corrupt pose and the ride
    * pose on a frame-rate independent cycle (entity fields {@code VELOCITY_0}
    * + {@code aj}) and applies the resulting rotations/scales/elbows to all
    * four arm bones. The look target is the corrupt entity when present, else
    * Galath's look vector.
    */
   void updateCorruptBones(BaseGirlEntity entity) {
      if (!ClientProxy.IS_PRELOADING) {
         if (!isInThreesome(entity)) {
            if (!this.mc.isGamePaused()) {
               ManglelieEntity manglelie = (ManglelieEntity)entity;
               if (manglelie.isCorrupting()) {
                  GalathEntity galath = manglelie.getGalathPartner(false);
                  if (galath != null) {
                     AnimationProcessor processor = this.getAnimationProcessor();
                     IBone armLBone = processor.getBone("armL");
                     IBone armRBone = processor.getBone("armR");
                     IBone lowerArmLBone = processor.getBone("lowerArmL");
                     IBone lowerArmRBone = processor.getBone("lowerArmR");
                     IBone elbowRBone = processor.getBone("elbowR");
                     IBone elbowLBone = processor.getBone("elbowL");
                     Entity corruptEntity = manglelie.getCorruptEntity();
                     boolean hasNoCorruptEntity = corruptEntity == null;
                     if (hasNoCorruptEntity) {
                        float fps2 = Minecraft.getDebugFPS();
                        if (fps2 == 0.0F) {
                           fps2 = 1.0F;
                        }

                        if (manglelie.aj == hasNoCorruptEntity) {
                           manglelie.VELOCITY_0 = 0.0F;
                        } else {
                           manglelie.VELOCITY_0 += 1.5F / fps2;
                        }

                        if (manglelie.VELOCITY_0 >= 1.0F) {
                           manglelie.VELOCITY_0 = 0.0F;
                           manglelie.aj = hasNoCorruptEntity;
                        }

                        ManglelieNpcModel.RotationData rotationData2;
                        if (manglelie.VELOCITY_0 == 0.0F) {
                           rotationData2 = this.updateCorruptPose(galath, armRBone, armLBone, lowerArmLBone, lowerArmRBone);
                        } else {
                           rotationData2 = ManglelieNpcModel.RotationData.lerpRotationData(
                              this.updateCorruptPose(galath, armRBone, armLBone, lowerArmLBone, lowerArmRBone),
                              this.updateRidePose(manglelie, galath, lowerArmRBone, lowerArmLBone, processor),
                              (float)(manglelie.aj ? RotationHelper.smoothStep(manglelie.VELOCITY_0) : 1.0 - RotationHelper.smoothStep(manglelie.VELOCITY_0))
                           );
                        }

                        armRBone.setRotationX(rotationData2.armRRotation.x);
                        armRBone.setRotationY(rotationData2.armRRotation.y);
                        armRBone.setRotationZ(rotationData2.armRRotation.z);
                        armLBone.setRotationX(rotationData2.armLRotation.x);
                        armLBone.setRotationY(rotationData2.armLRotation.y);
                        armLBone.setRotationZ(rotationData2.armLRotation.z);
                        lowerArmLBone.setRotationX(rotationData2.lowerArmLRotation.x);
                        lowerArmLBone.setRotationY(rotationData2.lowerArmLRotation.y);
                        lowerArmLBone.setRotationZ(rotationData2.lowerArmLRotation.z);
                        lowerArmRBone.setRotationX(rotationData2.lowerArmRRotation.x);
                        lowerArmRBone.setRotationY(rotationData2.lowerArmRRotation.y);
                        lowerArmRBone.setRotationZ(rotationData2.lowerArmRRotation.z);
                        armLBone.setScaleY(rotationData2.armLScale);
                        armRBone.setScaleY(rotationData2.armRScale);
                        elbowRBone.setRotationY(rotationData2.elbowRRotationY);
                        elbowLBone.setRotationY(rotationData2.elbowLRotationY);
                     } else {
                        manglelie.ZERO_VECTOR = this.getLookVector(corruptEntity);
                        float fps = Minecraft.getDebugFPS();
                        if (fps == 0.0F) {
                           fps = 1.0F;
                        }

                        if (manglelie.aj == hasNoCorruptEntity) {
                           manglelie.VELOCITY_0 = 0.0F;
                        } else {
                           manglelie.VELOCITY_0 += 1.5F / fps;
                        }

                        if (manglelie.VELOCITY_0 >= 1.0F) {
                           manglelie.VELOCITY_0 = 0.0F;
                           manglelie.aj = hasNoCorruptEntity;
                        }

                        ManglelieNpcModel.RotationData rotationData;
                        if (manglelie.VELOCITY_0 == 0.0F) {
                           rotationData = this.updateRidePose(manglelie, galath, lowerArmRBone, lowerArmLBone, processor);
                        } else {
                           rotationData = ManglelieNpcModel.RotationData.lerpRotationData(
                              this.updateCorruptPose(galath, armRBone, armLBone, lowerArmLBone, lowerArmRBone),
                              this.updateRidePose(manglelie, galath, lowerArmRBone, lowerArmLBone, processor),
                              (float)(manglelie.aj ? RotationHelper.smoothStep(manglelie.VELOCITY_0) : 1.0 - RotationHelper.smoothStep(manglelie.VELOCITY_0))
                           );
                        }

                        armRBone.setRotationX(rotationData.armRRotation.x);
                        armRBone.setRotationY(rotationData.armRRotation.y);
                        armRBone.setRotationZ(rotationData.armRRotation.z);
                        armLBone.setRotationX(rotationData.armLRotation.x);
                        armLBone.setRotationY(rotationData.armLRotation.y);
                        armLBone.setRotationZ(rotationData.armLRotation.z);
                        lowerArmLBone.setRotationX(rotationData.lowerArmLRotation.x);
                        lowerArmLBone.setRotationY(rotationData.lowerArmLRotation.y);
                        lowerArmLBone.setRotationZ(rotationData.lowerArmLRotation.z);
                        lowerArmRBone.setRotationX(rotationData.lowerArmRRotation.x);
                        lowerArmRBone.setRotationY(rotationData.lowerArmRRotation.y);
                        lowerArmRBone.setRotationZ(rotationData.lowerArmRRotation.z);
                        armLBone.setScaleY(rotationData.armLScale);
                        armRBone.setScaleY(rotationData.armRScale);
                        elbowRBone.setRotationY(rotationData.elbowRRotationY);
                        elbowLBone.setRotationY(rotationData.elbowLRotationY);
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Ride-pose arm data: arms reach toward Galath's look point (aim angles
    * from the bone offsets), with scale/elbow corrections and a staged arm
    * swing during the corruption progress phases.
    *
    * @return the computed arm rotations for this frame
    */
   ManglelieNpcModel.RotationData updateRidePose(@Nonnull ManglelieEntity manglelie, @Nonnull GalathEntity galath, IBone lowerArmLBone, IBone lowerArmRBone, AnimationProcessor processor) {
      ManglelieNpcModel.RotationData rotationData = new ManglelieNpcModel.RotationData();
      rotationData.lowerArmLRotation = new Vector3fSexmodSpecial(UPPER_ARM_BASE_ANGLE, 0.0F, lowerArmLBone.getRotationZ());
      rotationData.lowerArmRRotation = new Vector3fSexmodSpecial(LOWER_ARM_BASE_ANGLE, 0.0F, lowerArmRBone.getRotationZ());
      float upperBodyRotation = galath.aE + processor.getBone("upperBody").getRotationX();
      float partialTicks = this.mc.getRenderPartialTicks();
      Vec3d galathLookVec = ManglelieRenderer.getEntityLookVector(galath, partialTicks);
      Vec3d armRPoint = manglelie.getCachedBoneOffset("armR").add(galathLookVec);
      Vec3d armLPoint = manglelie.getCachedBoneOffset("armL").add(galathLookVec);
      Vector2f armRAngles = ThreadNames.getLookAngles(armRPoint, manglelie.ZERO_VECTOR);
      Vector2f armLAngles = ThreadNames.getLookAngles(armLPoint, manglelie.ZERO_VECTOR);
      Float aimYaw = GalathEntity.getAimYaw(galath, partialTicks);
      float headYaw = aimYaw == null ? RotationHelper.lerpFloat(galath.prevRotationYawHead, galath.rotationYawHead, partialTicks) : aimYaw;
      float wrappedHeadYaw = TrigMath.wrapDegrees(headYaw);
      float corruptProgress = manglelie.getCorruptProgress(partialTicks);
      float easedProgress = (float)RotationHelper.easeInOutQuad(Math.min(1.0F, corruptProgress));
      float swingProgress;
      if (easedProgress != 1.0F) {
         swingProgress = 0.0F;
      } else {
         swingProgress = (corruptProgress * 28.0F - 28.0F) / 32.0F;
         swingProgress = Math.max(0.0F, swingProgress - 0.5F) * 2.0F;
      }

      float easedSwingProgress = (float)RotationHelper.easeInOutQuad(swingProgress);
      float armLift = TrigMath.wrapDegrees(RotationHelper.lerp(0.0F, 90.0F, easedProgress));
      boolean isLookingAtGalath = manglelie.isLookingAtGalathPoint(manglelie.ZERO_VECTOR, partialTicks);
      if (isLookingAtGalath) {
         rotationData.armRRotation = new Vector3fSexmodSpecial(-upperBodyRotation + armRAngles.y + TrigMath.wrapDegrees(90.0F), armRAngles.x, 0.0F);
         rotationData.armLRotation = new Vector3fSexmodSpecial(
            -upperBodyRotation + armLAngles.y + TrigMath.wrapDegrees(90.0F),
            (float)(armLAngles.x + TrigMath.wrapDegrees(-20.0F) * Math.cos(armRAngles.x + wrappedHeadYaw * 1.0F) + RotationHelper.lerp(armLift / 2.0F, 0.0F, easedSwingProgress)),
            0.0F
         );
         rotationData.armLScale = 1.0F + Math.abs(Math.abs(armRAngles.x) - Math.abs(wrappedHeadYaw)) * 0.1909F;
         rotationData.elbowLRotationY = TrigMath.wrapDegrees(90.0F);
         rotationData.lowerArmLRotation.z = RotationHelper.lerp(armLift, 0.0F, easedSwingProgress);
         if (swingProgress > 0.5) {
            rotationData.lowerArmLRotation.x = UPPER_ARM_BASE_ANGLE + (float)RotationHelper.lerpDouble(ARM_SWING_ANGLE, 0.0, RotationHelper.easeInOutQuad((swingProgress - 0.5F) * 2.0F));
         } else if (swingProgress != 0.0F && swingProgress < 0.5) {
            rotationData.lowerArmLRotation.x = UPPER_ARM_BASE_ANGLE + (float)RotationHelper.lerpDouble(0.0, ARM_SWING_ANGLE, RotationHelper.easeInOutQuad(swingProgress * 2.0F));
         }
      } else {
         rotationData.armLRotation = new Vector3fSexmodSpecial(-upperBodyRotation + armLAngles.y + TrigMath.wrapDegrees(90.0F), armLAngles.x, 0.0F);
         rotationData.armRRotation = new Vector3fSexmodSpecial(
            -upperBodyRotation + armRAngles.y + TrigMath.wrapDegrees(90.0F),
            (float)(armRAngles.x + TrigMath.wrapDegrees(20.0F) * Math.cos(armLAngles.x + wrappedHeadYaw * 1.0F)) - RotationHelper.lerp(armLift / 2.0F, 0.0F, easedSwingProgress),
            0.0F
         );
         rotationData.armRScale = 1.0F + Math.abs(Math.abs(armLAngles.x) - Math.abs(wrappedHeadYaw)) * 0.1909F;
         rotationData.elbowRRotationY = TrigMath.wrapDegrees(90.0F);
         rotationData.lowerArmRRotation.z = -RotationHelper.lerp(armLift, 0.0F, easedSwingProgress);
         if (swingProgress > 0.5) {
            rotationData.lowerArmRRotation.x = LOWER_ARM_BASE_ANGLE + (float)RotationHelper.lerpDouble(ARM_SWING_ANGLE, 0.0, RotationHelper.easeInOutQuad((swingProgress - 0.5F) * 2.0F));
         } else if (swingProgress != 0.0F && swingProgress < 0.5) {
            rotationData.lowerArmRRotation.x = LOWER_ARM_BASE_ANGLE + (float)RotationHelper.lerpDouble(0.0, ARM_SWING_ANGLE, RotationHelper.easeInOutQuad(swingProgress * 2.0F));
         }
      }

      rotationData.armRRotation.y += wrappedHeadYaw;
      rotationData.armLRotation.y += wrappedHeadYaw;
      return rotationData;
   }

   /**
    * Corrupt-pose arm data from Galath's head rotation: positive head pitch
    * bends both arms up with mirrored yaw/z offsets; negative pitch drops the
    * forearms (scaled factors) and spreads the arms slightly.
    */
   ManglelieNpcModel.RotationData updateCorruptPose(GalathEntity galath, IBone armRBone, IBone armLBone, IBone lowerArmLBone, IBone lowerArmRBone) {
      float headRotation = galath.aE;
      ManglelieNpcModel.RotationData rotationData = new ManglelieNpcModel.RotationData();
      if (headRotation > 0.0F) {
         rotationData.armRRotation = new Vector3fSexmodSpecial(armRBone.getRotationX() - headRotation, armRBone.getRotationY() - headRotation * -25.0F / 45.0F, armRBone.getRotationZ() + headRotation * 12.5F / 45.0F);
         rotationData.armLRotation = new Vector3fSexmodSpecial(armLBone.getRotationX() - headRotation, armLBone.getRotationY() + headRotation * 15.0F / 45.0F, armLBone.getRotationZ());
         rotationData.lowerArmLRotation = new Vector3fSexmodSpecial(lowerArmLBone.getRotationX(), lowerArmLBone.getRotationY(), lowerArmLBone.getRotationZ());
         rotationData.lowerArmRRotation = new Vector3fSexmodSpecial(lowerArmRBone.getRotationX(), lowerArmRBone.getRotationY(), lowerArmRBone.getRotationZ());
         return rotationData;
      } else {
         rotationData.lowerArmRRotation = new Vector3fSexmodSpecial(lowerArmRBone.getRotationX() + 2.0F * headRotation, lowerArmRBone.getRotationY(), lowerArmRBone.getRotationZ());
         rotationData.lowerArmLRotation = new Vector3fSexmodSpecial(lowerArmLBone.getRotationX() + 2.2222223F * headRotation, lowerArmLBone.getRotationY(), lowerArmLBone.getRotationZ());
         rotationData.armRRotation = new Vector3fSexmodSpecial(armRBone.getRotationX() - headRotation, armRBone.getRotationY(), armRBone.getRotationZ() + headRotation * 5.0F / 45.0F);
         rotationData.armLRotation = new Vector3fSexmodSpecial(armLBone.getRotationX() - headRotation, armLBone.getRotationY(), armLBone.getRotationZ() - headRotation * 5.0F / 45.0F);
         return rotationData;
      }
   }

   /**
    * Corruption head/body tracking: the rotation tool follows Galath's head
    * rotation, the upper body/head/boobs bend by it, and the head chases
    * Galath with a speed-limited (7 deg/frame at 60fps) wrap-around lerp —
    * state persists in the entity's {@code TICK_0}/{@code ai} fields.
    */
   void updatePoseBones(BaseGirlEntity entity) {
      if (!ClientProxy.IS_PRELOADING) {
         if (!this.mc.isGamePaused()) {
            ManglelieEntity manglelie = (ManglelieEntity)entity;
            if (ManglelieRenderer.isCorrupting(manglelie)) {
               GalathEntity galath = manglelie.getGalathPartner(false);
               if (galath != null) {
                  AnimationProcessor processor = this.getAnimationProcessor();
                  float headRotation = galath.aE;
                  processor.getBone("rotationTool").setRotationX(headRotation);
                  IBone headBone = processor.getBone("head");
                  IBone upperBodyBone = processor.getBone("upperBody");
                  IBone boobsBone = processor.getBone("boobs");
                  if (headRotation > 0.0F) {
                     upperBodyBone.setRotationX(-1.1111112F * headRotation);
                     headBone.setRotationX(0.1333F * headRotation);
                     boobsBone.setRotationX(headRotation * 22.5F / 45.0F);
                  } else {
                     upperBodyBone.setRotationX(-1.6666666F * headRotation);
                     headBone.setRotationX(headRotation * 0.666F);
                  }

                  float yawError = ThreadNames.wrapAngle(manglelie.TICK_0, manglelie.af);
                  float pitchError = ThreadNames.wrapAngle(manglelie.ai, manglelie.rotationLerp);
                  float fps = Minecraft.getDebugFPS();
                  if (fps == 0.0F) {
                     fps = 1.0F;
                  }

                  float yawStep = 7.0F * (Math.abs(yawError) < 7.0F ? yawError : (yawError > 0.0F ? 7.0F : -7.0F)) * (1.0F / fps);
                  float pitchStep = 7.0F * (Math.abs(pitchError) < 7.0F ? pitchError : (pitchError > 0.0F ? 7.0F : -7.0F)) * (1.0F / fps);
                  float newYaw = manglelie.TICK_0 + yawStep;
                  float newPitch = manglelie.ai + pitchStep;
                  headBone.setRotationY(headBone.getRotationY() + newYaw);
                  headBone.setRotationX(headBone.getRotationX() + newPitch);
                  manglelie.TICK_0 = newYaw;
                  manglelie.ai = newPitch;
               }
            }
         }
      }
   }

   /**
    * Shared pose pass (also called from {@code GalathNpcModel} while Galath
    * hugs Manglelie): toggles the skirt/cheek bones by the look state and
    * stages the cock bones by the corruption stage ({@code an}).
    */
   public static void animateModel(BaseGirlEntity girl, AnimationProcessor processor, float partialTicks) {
      if (!ClientProxy.IS_PRELOADING) {
         boolean isGalathLooking = ManglelieRenderer.isGalathLooking(girl);
         setCheekHidden(processor, isGalathLooking);
         setSkirtHidden(processor, isGalathLooking);
         animatePose(girl, processor, partialTicks);
      }
   }

   /**
    * Cock-stage bone visibility from the corruption stage counter.
    */
   static void animatePose(BaseGirlEntity girl, AnimationProcessor processor, float partialTicks) {
      if (girl instanceof ManglelieEntity) {
         for (int i = 0; i < 3; i++) {
            IBone cockBone = processor.getBone("cockStage" + i);
            if (cockBone != null) {
               cockBone.setHidden(i > ((ManglelieEntity)girl).an);
            }
         }
      }
   }

   /**
    * Hides the skirt bone while the look pose is active (the custom ribbon
    * mesh replaces it).
    */
   static void setSkirtHidden(AnimationProcessor processor, boolean hidden) {
      processor.getBone("skirt").setHidden(!hidden);
   }

   /**
    * Swaps the below-skirt cheek/side bones against their skirted variants
    * depending on the look pose.
    */
   static void setCheekHidden(AnimationProcessor processor, boolean hidden) {
      processor.getBone("cheekRBelowSkirt").setHidden(hidden);
      processor.getBone("cheekLBelowSkirt").setHidden(hidden);
      processor.getBone("sideRNoSkirt").setHidden(hidden);
      IBone sideRSkirtBone = processor.getBone("sideRSkirt");
      IBone boneToToggle;
      boolean hiddenState;
      if (!hidden) {
         boneToToggle = sideRSkirtBone;
         hiddenState = true;
      } else {
         boneToToggle = sideRSkirtBone;
         hiddenState = false;
      }

      boneToToggle.setHidden(hiddenState);
      processor.getBone("sideLNoSkirt").setHidden(hidden);
      IBone sideLSkirtBone = processor.getBone("sideLSkirt");
      if (!hidden) {
         boneToToggle = sideLSkirtBone;
         hiddenState = true;
      } else {
         boneToToggle = sideLSkirtBone;
         hiddenState = false;
      }

      boneToToggle.setHidden(hiddenState);
   }

   /**
    * Per-frame arm pose bundle (rotations, scales, elbow yaws) lerped between
    * the corrupt and ride poses during the corruption cycle.
    */
   private static class RotationData {
      private Vector3fSexmodSpecial armRRotation;
      private Vector3fSexmodSpecial armLRotation;
      private Vector3fSexmodSpecial lowerArmRRotation;
      private Vector3fSexmodSpecial lowerArmLRotation;
      private float armRScale = 1.0F;
      private float armLScale = 1.0F;
      private float elbowLRotationY = 0.0F;
      private float elbowRRotationY = 0.0F;

      private RotationData() {
      }

      /**
    * Component-wise lerp of two pose bundles.
    */
   static ManglelieNpcModel.RotationData lerpRotationData(ManglelieNpcModel.RotationData from, ManglelieNpcModel.RotationData to, float progress) {
         ManglelieNpcModel.RotationData result = new ManglelieNpcModel.RotationData();
         result.armRRotation = RotationHelper.lerpVector3f(from.armRRotation, to.armRRotation, progress);
         result.armLRotation = RotationHelper.lerpVector3f(from.armLRotation, to.armLRotation, progress);
         result.lowerArmRRotation = RotationHelper.lerpVector3f(from.lowerArmRRotation, to.lowerArmRRotation, progress);
         result.lowerArmLRotation = RotationHelper.lerpVector3f(from.lowerArmLRotation, to.lowerArmLRotation, progress);
         result.armRScale = RotationHelper.lerp(from.armRScale, to.armRScale, progress);
         result.armLScale = RotationHelper.lerp(from.armLScale, to.armLScale, progress);
         result.elbowLRotationY = RotationHelper.lerp(from.elbowLRotationY, to.elbowLRotationY, progress);
         result.elbowRRotationY = RotationHelper.lerp(from.elbowRRotationY, to.elbowRRotationY, progress);
         return result;
      }
   }
}
