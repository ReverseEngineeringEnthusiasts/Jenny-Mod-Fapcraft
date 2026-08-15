package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.command.CommandFuta;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.api.IGalath;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.Vector4d;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.TrigMath;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

/**
 * Geckolib model for Galath: three geo variants (normal, normal, and the
 * combined Manglelie pose {@code galath_con_mang}). The per-frame pass
 * orchestrates every flight/dash/hurt/scene pose: flight-bone lerping,
 * sword-dash body motion, knock-out-flight body rotation, rape-charge pose,
 * pussy-licking head tracking with lip sounds, wing visibility, futa bones
 * (from {@code CommandFuta.ENABLED}), and the HUG_MANG body2 placement.
 * <p>
 * <b>Couplings.</b> Publishes {@code body} rotation/scale into the entity
 * ({@code bw}/{@code bm}) and the head rotation into {@code aE} — read by the
 * renderer/geometry code; do not reorder the pose chain or drop these writes.
 * While hugging Manglelie, {@link ManglelieNpcModel#animateModel} takes over.
 */
public class GalathNpcModel extends GirlModel<BaseGirlEntity> {
   public static ResourceLocation GALATH_TEXTURE = new ResourceLocation("sexmod", "textures/entity/galath/galath.png");
   float lastPussyLickingWave = 0.0F;
   long swordDashStartTime = -1L;
   long swordDashEndTime = -1L;

   public GalathNpcModel() {
      this.modelLocations = this.getModelLocations();
   }

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/galath/galath.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath_con_mang.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/galath/galath.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "animations/galath/galath.animation.json");
   }

   /**
    * Skips Galath's render entirely when she has a target (she flies/attacks
    * invisibly until anchored) unless she has a master.
    */
   @Override
   protected boolean shouldRender(BaseGirlEntity girl) {
      if (!(girl instanceof GalathEntity)) {
         return true;
      }

      GalathEntity galath = (GalathEntity)girl;
      return galath.hasMaster() ? true : galath.getTargetEntity() == null;
   }

   /**
    * The full per-frame pose chain (see class javadoc for the order and the
    * published values).
    */
   @Override
   public void setLivingAnimations(BaseGirlEntity entity, Integer uniqueID, AnimationEvent event) {
      this.updateIdlePose(entity);
      super.setLivingAnimations(entity, uniqueID, event);
      this.handleActionPose(entity);
      this.updateHurtPose(entity);
      this.handleFlightPose(entity);
      this.handleWingState(entity);
      this.updateSwordBones(entity);
      this.hideWings(entity);
      this.updatePlayerPose(entity);
      this.hideFutaBone();
      this.updateModelState(entity);
      this.updatePussyPose(entity);
      this.handleDashAnimation(entity);
      if (entity instanceof GalathEntity) {
         GalathEntity galath = (GalathEntity)entity;
         galath.aE = this.getAnimationProcessor().getBone("head").getRotationX();
         if (galath.isHuggingManglelie()) {
            ManglelieNpcModel.animateModel(galath, this.getAnimationProcessor(), event.getPartialTick());
         }
      }
   }

   /**
    * Pussy-licking head tracking: the head follows the sword-swing offset
    * (lerped by the sword attack progress) and the lip sound fires on each
    * lick wave (sine zero-crossing).
    */
   void updatePussyPose(BaseGirlEntity entity) {
      if (Action.isAnyAction(entity, Action.PUSSY_LICKING)) {
         if (entity instanceof GalathEntity) {
            if (!this.mc.isGamePaused()) {
               AnimationProcessor processor = this.getAnimationProcessor();
               IBone headBone = processor.getBone("head");
               float waveTime = this.mc.getRenderPartialTicks() + this.mc.player.ticksExisted;
               Vector3fSexmodSpecial swordOffset = this.getSwordPos((GalathEntity)entity, waveTime);
               headBone.setRotationX(headBone.getRotationX() + swordOffset.x);
               headBone.setRotationY(headBone.getRotationY() + swordOffset.y);
               headBone.setRotationZ(headBone.getRotationZ() + swordOffset.z);
               if (entity.getCurrentAction() == Action.PUSSY_LICKING && !((GalathEntity)entity).a5) {
                  float wave = (float)(Math.sin(waveTime * 0.3F) * 10.0);
                  if (wave > 0.0F && this.lastPussyLickingWave < 0.0F || wave < 0.0F && this.lastPussyLickingWave > 0.0F) {
                     entity.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
                  }

                  this.lastPussyLickingWave = wave;
               }
            }
         }
      }
   }

   Vector3fSexmodSpecial getSwordPos(GalathEntity galath, float partialTicks) {
      return RotationHelper.lerpVector3f(this.getSwordSwingOffset(partialTicks), Vector3fSexmodSpecial.ZERO, galath.getSwordAttackProgress(this.mc.getRenderPartialTicks()));
   }

   Vector3fSexmodSpecial getSwordSwingOffset(float time) {
      return new Vector3fSexmodSpecial(
         (float)Math.sin(time * 0.3F) * TrigMath.wrapDegrees(10.0F),
         (float)Math.sin(time * 0.15F) * TrigMath.wrapDegrees(7.0F),
         (float)Math.sin(time * -0.15) * TrigMath.wrapDegrees(7.0F)
      );
   }

   /**
    * Publishes the body bone's rotation/scale to the entity fields
    * ({@code bw} = rotationY, {@code bm} = scaleY) — consumed by the wing/
    * geometry renderers.
    */
   void updateModelState(BaseGirlEntity entity) {
      if (entity instanceof GalathEntity) {
         GalathEntity galath = (GalathEntity)entity;
         AnimationProcessor processor = this.getAnimationProcessor();
         IBone bodyBone = processor.getBone("body");
         galath.bw = bodyBone.getRotationY();
         galath.bm = bodyBone.getScaleY();
      }
   }

   /**
    * HUG_MANG transition: positions the {@code body2} bone (Manglelie's
    * attachment) while the controller transitions into the hug.
    */
   void handleDashAnimation(BaseGirlEntity entity) {
      if (entity.actionController.getAnimationState() == AnimationState.Transitioning) {
         AnimationProcessor processor = this.getAnimationProcessor();
         Action action = entity.getCurrentAction();
         if (action == Action.HUG_MANG) {
            IBone body2Bone = processor.getBone("body2");
            if (body2Bone == null) {
               return;
            }

            body2Bone.setPositionX(0.0F);
            body2Bone.setPositionY(-0.53F);
            body2Bone.setPositionZ(-40.05F);
         }
      }
   }

   /**
    * Masturbate pose: drives the molang variables (pitch, armpitch, armyaw,
    * yaw) from the look vector to the master player so the animation reacts
    * to his position.
    */
   void updateIdlePose(BaseGirlEntity entity) {
      if (!ClientProxy.IS_PRELOADING) {
         if (entity.getCurrentAction() == Action.MASTERBATE) {
            Object masterPlayer = entity.getMasterPlayer();
            if (masterPlayer == null) {
               masterPlayer = this.mc.player;
            }

            MolangParser parser = GeckoLibCache.getInstance().parser;
            Vec3d lookVec = EntityLookVectorHelper.getLookVectorTo(entity, (EntityPlayer)masterPlayer, this.mc.getRenderPartialTicks()).add(entity.getCachedBoneOffset("head"));
            float yaw = (float)TrigMath.sinDegrees(Math.atan2(lookVec.z, lookVec.x)) - entity.getYawRotation();
            float pitch = (float)TrigMath.sinDegrees(
               Math.atan2(lookVec.y, Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z))
            );
            double magnitude = Math.abs(lookVec.x) + Math.abs(lookVec.y) + Math.abs(lookVec.z);
            double pitchMolang = magnitude * 7.0 + -20.0;
            double armpitchMolang = magnitude * 5.0 + -20.0;
            parser.setValue("pitch", pitchMolang + pitch - 80.0);
            parser.setValue("armpitch", armpitchMolang + pitch + -110.0);
            parser.setValue("armyaw", yaw + 80.0F);
            parser.setValue("yaw", yaw + 90.0F);
         }
      }
   }

   /**
    * Hides the futa bones unless the futa command is enabled.
    */
   void hideFutaBone() {
      if (!ClientProxy.IS_PRELOADING) {
         this.getAnimationProcessor().getBone("futaCock").setHidden(!CommandFuta.ENABLED);
         this.getAnimationProcessor().getBone("futaBallLL").setHidden(!CommandFuta.ENABLED);
         this.getAnimationProcessor().getBone("futaBallLR").setHidden(!CommandFuta.ENABLED);
      }
   }

   /**
    * Hides the coin bone for player-girls (only NPC Galath gives coins).
    */
   void updatePlayerPose(BaseGirlEntity entity) {
      if (entity instanceof AbstractPlayerGirlEntity) {
         this.getAnimationProcessor().getBone("coin").setHidden(true);
      }
   }

   void hideWings(BaseGirlEntity entity) {
      this.getAnimationProcessor().getBone("wings").setHidden(!((IGalath)entity).areWingsAnimated());
   }

   /**
    * Nipple/bra/slip visibility by wing state: while the wings are hidden
    * (idle/corrupted) the nipples hide and the bra/slip show; wing-animated
    * states expose the nipples.
    */
   void updateSwordBones(BaseGirlEntity entity) {
      AnimationProcessor processor = this.getAnimationProcessor();
      IBone nippleRBone = processor.getBone("nippleR");
      IBone nippleLBone = processor.getBone("nippleL");
      IBone braBoobLBone = processor.getBone("braBoobL");
      IBone braBoobRBone = processor.getBone("braBoobR");
      IBone slipBone = processor.getBone("slip");
      boolean wingsAnimated = ((IGalath)entity).isWingsAnimated();
      if (wingsAnimated) {
         Action.isAnyAction(entity, Action.PUSSY_LICKING, Action.MASTERBATE_SITTING, Action.MASTERBATE_SITTING_CUM);
         if (nippleRBone != null) {
            if (braBoobLBone != null) {
               IBone nippleRBone3 = nippleRBone;
               nippleRBone3.setHidden(false);
               IBone nippleLBone3 = nippleLBone;
               nippleLBone3.setHidden(false);
               braBoobLBone.setHidden(wingsAnimated);
               braBoobRBone.setHidden(wingsAnimated);
               IBone slipBone3 = slipBone;
               slipBone3.setHidden(true);
            }
         }
      } else {
         boolean isPussyAction = Action.isAnyAction(entity, Action.PUSSY_LICKING, Action.MASTERBATE_SITTING, Action.MASTERBATE_SITTING_CUM);
         if (nippleRBone != null) {
            if (braBoobLBone != null) {
               IBone nippleRBone2 = nippleRBone;
               nippleRBone2.setHidden(true);
               IBone nippleLBone2 = nippleLBone;
               nippleLBone2.setHidden(true);
               braBoobLBone.setHidden(wingsAnimated);
               braBoobRBone.setHidden(wingsAnimated);
               IBone slipBone2 = slipBone;
               slipBone2.setHidden(isPussyAction);
            }
         }
      }
   }

   /**
    * Knock-out-flight pose: while flying with the KNOCK_OUT_FLY action the
    * body pitches -90 (hovering) or follows the interpolated flight vector.
    */
   void handleWingState(BaseGirlEntity entity) {
      if (entity instanceof GalathEntity) {
         if ((Boolean)entity.getDataManager().get(GalathEntity.bP)) {
            if (entity.getCurrentAction() == Action.KNOCK_OUT_FLY) {
               IBone bodyBone = this.getAnimationProcessor().getBone("body");
               Vec3d lastPos = new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
               Vec3d motion = entity.getPositionVector().subtract(lastPos);
               boolean isHovering = Math.abs(motion.x) + Math.abs(motion.z) < 0.01F;
               if (isHovering) {
                  bodyBone.setRotationX(TrigMath.wrapDegrees(-90.0F));
                  bodyBone.setPositionY(0.0F);
                  bodyBone.setPositionZ(0.0F);
               } else {
                  Vec3d interpolated = getInterpolatedPosition(entity);
                  bodyBone.setRotationX(-((float)interpolated.x));
                  bodyBone.setPositionY((float)interpolated.y);
                  bodyBone.setPositionZ((float)interpolated.z);
               }
            }
         }
      }
   }

   /**
    * Rape-charge pose: the rotation tool/body follow the interpolated charge
    * vector, with the body yaw from the charge progress value ({@code bO}).
    */
   void updateHurtPose(BaseGirlEntity entity) {
      if (entity instanceof GalathEntity) {
         if (entity.getCurrentAction() == Action.RAPE_CHARGE) {
            Vec3d interpolated = getInterpolatedPosition(entity);
            IBone bodyBone = this.getAnimationProcessor().getBone("body");
            IBone rotationToolBone = this.getAnimationProcessor().getBone("rotationTool");
            rotationToolBone.setRotationX((float)interpolated.x);
            bodyBone.setPositionY((float)interpolated.y);
            bodyBone.setPositionZ((float)interpolated.z);
            float chargeProgress = (Float)entity.getDataManager().get(GalathEntity.bO);
            bodyBone.setRotationY(TrigMath.wrapDegrees(chargeProgress * 180.0F));
         }
      }
   }

   /**
    * Sword-dash body motion: while the dash progress is in the 24..32 window
    * the body lerps from the dash-start offset back to zero (8-tick dash).
    */
   void handleFlightPose(BaseGirlEntity entity) {
      if (entity instanceof GalathEntity) {
         GalathEntity galath = (GalathEntity)entity;
         if (galath.getCurrentAction() != Action.ATTACK_SWORD) {
            this.swordDashStartTime = -1L;
            this.swordDashEndTime = -1L;
         } else {
            int dashTick = galath.az();
            if (dashTick == 24 && this.swordDashStartTime == -1L) {
               this.swordDashStartTime = this.mc.world.getTotalWorldTime();
               this.swordDashEndTime = this.swordDashStartTime + 8L;
            }

            if (ThreadNames.isBetween(dashTick, 24.0, 32.0)) {
               IBone bodyBone = this.getAnimationProcessor().getBone("body");
               Vec3d dashOffset = GirlModel.getBoneOffsetWorld(galath, galath.B_clash642());
               float dashProgress = ((float)Minecraft.getMinecraft().world.getTotalWorldTime() + this.mc.getRenderPartialTicks() - (float)this.swordDashStartTime) / (float)(this.swordDashEndTime - this.swordDashStartTime);
               dashOffset = RotationHelper.lerpVec3dDouble(dashOffset, Vec3d.ZERO, dashProgress);
               bodyBone.setRotationX((float)dashOffset.x);
               bodyBone.setPositionY((float)dashOffset.y);
               bodyBone.setPositionZ((float)dashOffset.z);
            }
         }
      }
   }

   /**
    * Flight/boost pose: lerps the rotation tool between the flight data
    * vectors (with an extra 45-degree pitch during the boost window).
    */
   void handleActionPose(BaseGirlEntity entity) {
      float boostPitch = 0.0F;
      switch (entity.getCurrentAction()) {
         case BOOST:
            if (Action.BOOST.ticksPlaying[1] > 13 && Action.BOOST.ticksPlaying[1] < 40) {
               boostPitch = 45.0F;
            }
         case FLY:
         case CONTROLLED_FLIGHT:
            float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
            IBone rotationToolBone = this.getAnimationProcessor().getBone("rotationTool");
            Vector4d flightData = ((IGalath)entity).getFlightData();
            rotationToolBone.setRotationX((float)RotationHelper.lerpDouble(flightData.z + boostPitch, flightData.x + boostPitch, partialTicks));
            rotationToolBone.setRotationZ((float)RotationHelper.lerpDouble(flightData.w, flightData.y, partialTicks));
            return;
      }
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

}
