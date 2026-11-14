package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.EscapeMinigameHud;
import com.trolmastercard.sexmod.client.gui.GalathFlightHud;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.command.CommandFuta;
import com.trolmastercard.sexmod.networking.GalathRapePouncePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.networking.UpdateVelocityPacket;
import com.trolmastercard.sexmod.util.DynamicTrailRenderer;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

/**
 * <b>Role.</b> Galath's GeckoLib animation glue, split out of
 * {@link GalathEntity}: the per-tick predicate for the action/eyes/movement
 * controllers, the scene overrides the predicate cannot express (mounting,
 * bed variants, morning blowjob), the sword-progress curve the renderer reads,
 * and the sound-keyframe listener that drives the scenes.
 * <p>
 * <b>Flow.</b> The controllers are still created in
 * {@link GalathEntity#registerControllers(software.bernie.geckolib3.core.manager.AnimationData)};
 * every keyframe goes through
 * {@link #handleSoundKeyframe(GalathEntity, SoundKeyframeEvent)}.
 * <p>
 * <b>Pitfalls.</b> The keyframe switch relies on two deliberate fall-throughs
 * ({@code creampie} into {@code creampieGalath}, {@code blackScreenTamed} into
 * {@code blackScreen}) and on {@code corrupt_hard_end} forcing CORRUPT_SLOW, so
 * the cases must not be reordered. Action transitions from the predicate only
 * run where the controller is not locally registered.
 */
final class GalathAnimations {
   private GalathAnimations() {
   }

   static boolean handleActionAnimationOverrides(GalathEntity galath, Action action, String animName, boolean started, AnimationEvent event) {
      if (action == Action.MASTERBATE_SITTING && galath.masterbateSittingPending) {
         galath.masterbateSittingPending = false;
         galath.createAnimation("animation.galath.masterbating_sitting", true, event, true);
         return true;
      }

      if (action == Action.MORNING_BLOWJOB_FAST && galath.morningBlowjobStarted) {
         galath.setCurrentAction(Action.MORNING_BLOWJOB_CUM);
         return true;
      }

      if (action == Action.MORNING_BLOWJOB_FAST && galath.bedSoftAnimation) {
         galath.createAnimation("animation.shared.bed_fast", true, event, true);
         galath.bedSoftAnimation = false;
         return true;
      }

      if (action == Action.MORNING_BLOWJOB_CUM) {
         galath.setCurrentAction((Action)null);
         return true;
      }

      if (action == Action.PUSSY_LICKING && galath.pussyLickingForward) {
         galath.pussyLickingForward = false;
         galath.createAnimation("animation.galath.pussy_licking", true, event, true);
         return true;
      }

      if (action != Action.MORNING_BLOWJOB_SLOW || !galath.morningBlowjobStarted && !HandlePlayerMovement.isJumping) {
         if (action == Action.MORNING_BLOWJOB_SLOW && galath.bedBackAnimation) {
            galath.bedBackAnimation = false;
            galath.createAnimation("animation.shared.bed_slow", true, event, true);
            return true;
         } else if (action == Action.MORNING_BLOWJOB_FAST && !HandlePlayerMovement.isJumping) {
            galath.setCurrentAction(Action.MORNING_BLOWJOB_SLOW);
            galath.bedBackAnimation = true;
            galath.createAnimation("animation.shared.bed_back", true, event, true);
            return true;
         } else {
            return false;
         }
      } else {
         galath.bedSoftAnimation = true;
         galath.setCurrentAction(Action.MORNING_BLOWJOB_FAST);
         galath.createAnimation("animation.shared.bed_soft", true, event, true);
         return true;
      }
   }

   static float getSwordAttackProgress(GalathEntity galath, float partialTicks) {
      Action action = galath.getCurrentAction();
      if (action == Action.PUSSY_LICKING && !galath.pussyLickingForward) {
         return 0.0F;
      }

      if (action == Action.MASTERBATE_SITTING && !galath.masterbateSittingPending) {
         return 1.0F;
      }

      float scale = Action.getActionTimeScale(galath, partialTicks);
      return action == Action.MASTERBATE_SITTING ? scale : 1.0F - scale;
   }

   static <E extends IAnimatable> PlayState animationPredicate(GalathEntity galath, AnimationEvent<E> event) {
      if (galath.isLocallyRegistered()) {
         galath.createAnimation("animation.galath.idle", true, event);
         return PlayState.CONTINUE;
      }

      Action action = galath.getCurrentAction();
      AnimationController controller = event.getController();
      controller.setAnimationSpeed(1.0);
      if (controller.equals(galath.eyesController)) {
         if (action.autoBlink && action != Action.GALATH_DE_SUMMON) {
            galath.createAnimation("animation.galath.blink", true, event);
            return PlayState.CONTINUE;
         } else {
            return PlayState.STOP;
         }
      } else if (controller.equals(galath.movementController)) {
         if (action != Action.NULL) {
            return PlayState.STOP;
         } else if (!galath.onGround) {
            galath.createAnimation("animation.galath.controlled_flight", true, event);
            return PlayState.CONTINUE;
         } else {
            Vec3d movement = galath.getPositionVector().subtract(new Vec3d(galath.lastTickPosX, galath.lastTickPosY, galath.lastTickPosZ));
            if (movement.equals(Vec3d.ZERO)) {
               galath.createAnimation("animation.galath.idle", true, event);
               return PlayState.CONTINUE;
            } else {
               galath.rotationYaw = galath.rotationYawHead;
               galath.createAnimation("animation.galath." + (galath.entityDataManager.get(GalathEntity.SPRINTING) ? "run" : "walk"), true, event);
               return PlayState.CONTINUE;
            }
         }
      } else {
         if ((action == Action.RAPE_INTRO || action == Action.RAPE_ON_GOING)
               && galath.isControlledByLocalPlayer() && !galath.hasMaster()) {
            EscapeMinigameHud.ensureVisible(galath);
         }

         switch (galath.getCurrentAction()) {
            case HUG_MANG:
               galath.createAnimation("animation.galath.hug_mang", true, event);
               break;
            case MORNING_BLOWJOB_SLOW:
               galath.createAnimation(galath.bedBackAnimation ? "animation.shared.bed_back" : "animation.shared.bed_slow", true, event);
               break;
            case MORNING_BLOWJOB_FAST:
               if (galath.bedSoftAnimation) {
                  galath.createAnimation("animation.shared.bed_soft", true, event);
               } else {
                  galath.playRandomizedAnimation("animation.shared.bed_fast", 4, 0.75F, event);
               }
               break;
            case MORNING_BLOWJOB_CUM:
               galath.createAnimation("animation.shared.bed_cum", true, event);
               break;
            case RAPE_INTRO:
               galath.createAnimation("animation.galath.rape_intro", true, event);
               break;
            case RAPE_ON_GOING:
               galath.createAnimation("animation.galath.rape" + galath.rapeVariant, true, event);
               break;
            case RAPE_CUM:
               galath.createAnimation("animation.galath.rape_cum", true, event);
               break;
            case RAPE_CHARGE:
               galath.createAnimation("animation.galath.rape_charge", true, event);
               break;
            case RAPE_CUM_IDLE:
               galath.createAnimation("animation.galath.rape_cum_idle", true, event);
               break;
            case CORRUPT_SLOW:
               galath.createAnimation("animation.galath.corrupt_slow", true, event);
               break;
            case CORRUPT_FAST:
               galath.createAnimation("animation.galath.corrupt_" + (galath.corruptHard ? "hard" : "soft"), true, event);
               break;
            case CORRUPT_CUM:
               galath.createAnimation("animation.galath.corrupt_cum", true, event);
               break;
            case MASTERBATE:
               galath.createAnimation("animation.galath.masterbate", true, event);
               break;
            case RAPE_PREPARE:
               galath.createAnimation("animation.galath.rape_prepare", true, event);
               break;
            case CORRUPT_INTRO:
               galath.createAnimation("animation.galath.corrupt_intro", true, event);
            case COWGIRLCUM:
            default:
               break;
            case NULL:
               return PlayState.STOP;
            case FLY:
               galath.createAnimation("animation.galath.idle_flying", true, event);
               break;
            case SUMMON_SKELETON:
               galath.createAnimation("animation.galath.summon_skeleton" + (galath.entityDataManager.get(GalathEntity.MIRROR_SIDE) ? "Mirrored" : ""), true, event);
               break;
            case ATTACK_SWORD:
               galath.createAnimation("animation.galath.attack", true, event);
               break;
            case KNOCK_OUT_FLY:
               controller.setAnimationSpeed(1.5);
               galath.createAnimation("animation.galath.knockout_air", true, event);
               break;
            case KNOCK_OUT_GROUND:
               galath.createAnimation("animation.galath.knocked_out", true, event);
               break;
            case KNOCK_OUT_STAND_UP:
               galath.createAnimation("animation.galath.knocked_out_stand_up", true, event);
               break;
            case CONTROLLED_FLIGHT:
               galath.createAnimation("animation.galath.controlled_flight", true, event);
               break;
            case BOOST:
               galath.createAnimation("animation.galath.boost", true, event);
               break;
            case GALATH_SUMMON:
               galath.createAnimation("animation.galath.summon", false, event);
               break;
            case GALATH_DE_SUMMON:
               galath.createAnimation("animation.galath.desummon" + (galath.onGround ? "_standing" : ""), true, event);
               break;
            case GIVE_COIN:
               galath.createAnimation("animation.galath.give_coin", true, event);
               break;
            case RUN:
               controller.setAnimationSpeed(0.7);
               galath.createAnimation("animation.galath.running", true, event);
               break;
            case PUSSY_LICKING:
               galath.createAnimation(galath.pussyLickingForward ? "animation.galath.pussy_licking_forward" : "animation.galath.pussy_licking", true, event);
               break;
            case MASTERBATE_SITTING:
               galath.createAnimation(galath.masterbateSittingPending ? "animation.galath.pussy_licking_back" : "animation.galath.masterbating_sitting", true, event);
               break;
            case MASTERBATE_SITTING_CUM:
               galath.createAnimation("animation.galath.masterbating_sitting_cum", true, event);
         }

         return PlayState.CONTINUE;
      }
   }

   /**
    * CLIENT: the sound-keyframe listener driving the wild fight (charge
    * sounds, sword render toggles, stars) and the tamed scenes (rape
    * switch/UI, corrupt switch, coin cam, morning blowjob, creampie trails).
    * {@code blackScreenTamed} only black-screens wild girls;
    * {@code flapControlled} sends the flight input as
    * {@link UpdateVelocityPacket}.
    */
   @SideOnly(Side.CLIENT)
   static void handleSoundKeyframe(GalathEntity galath, SoundKeyframeEvent<?> sound) {
      switch (sound.sound) {
         case "goodTiming":
            galath.playSound(SoundHandler.GIRLS_GALATH_DIALOG[4]);
            galath.sendChatMessage("Good timing boy~");
            break;
         case "huh":
            galath.playRandomSound(SoundHandler.GIRLS_GALATH_HUH);
            break;
         case "giggle":
            Vec3d vec = galath.getVectorTowardPlayer();
            galath.world
               .playSound(
                  vec.x,
                  vec.y,
                  vec.z,
                  SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_GIGGLE),
                  SoundCategory.HOSTILE,
                  1.0F,
                  1.0F,
                  false
               );
            break;
         case "dialog1":
            galath.playSound(SoundHandler.GIRLS_GALATH_DIALOG[1]);
            break;
         case "moan":
            galath.playRandomSound(SoundHandler.GIRLS_GALATH_MOAN);
            break;
         case "breath":
            galath.playRandomSound(SoundHandler.GIRLS_GALATH_BREATHING);
            break;
         case "dialog5":
            galath.playSound(SoundHandler.GIRLS_GALATH_DIALOG[5]);
            break;
         case "switchmoan":
            if (galath.alternateMoan) {
               galath.playRandomSound(SoundHandler.GIRLS_GALATH_BREATHING);
            } else {
               galath.playRandomSound(galath.getRNG().nextBoolean() ? SoundHandler.GIRLS_GALATH_MOAN : SoundHandler.GIRLS_GALATH_AHH);
            }

            galath.alternateMoan = !galath.alternateMoan;
            break;
         case "lightcharge":
Vec3d aimVec = galath.getVectorTowardPlayer();
            galath.world
               .playSound(
                  aimVec.x,
                  aimVec.y,
                  aimVec.z,
                  SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_LIGHTCHARGE),
                  SoundCategory.HOSTILE,
                  1.0F,
                  1.0F,
                  false
               );
            break;
         case "strongcharge":
            galath.playRandomSound(SoundHandler.GIRLS_GALATH_STRONGCHARGE);
            break;
         case "hmph":
            galath.playRandomSound(SoundHandler.GIRLS_GALATH_HMPH);
            break;
         case "cum":
            galath.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 2.0F);
            break;
         case "giggle0":
            galath.playSound(SoundHandler.GIRLS_GALATH_GIGGLE[0]);
            break;
         case "orgasm":
            galath.playRandomSound(SoundHandler.GIRLS_GALATH_ORGASM);
            break;
         case "pound":
            galath.playRandomSound(SoundHandler.MISC_POUNDING);
            break;
         case "flap":
Vec3d flapVec = galath.getVectorTowardPlayer();
            galath.world
               .playSound(
                  flapVec.x,
                  flapVec.y,
                  flapVec.z,
                  SoundHandler.randomSound(SoundHandler.MISC_FLAP),
                  SoundCategory.HOSTILE,
                  1.0F,
                  1.0F,
                  false
               );
            break;
         case "startRenderSword":
            galath.renderSword = true;
            galath.spawnSwordParticles = true;
            break;
         case "stopFadeInParticles":
            galath.spawnSwordParticles = false;
            break;
         case "stopRenderSword":
            galath.renderSword = false;
            galath.spawnSwordParticles = false;
            break;
         case "dontDrawStars":
            galath.drawStars = false;
            break;
         case "setNude":
            galath.wingsAnimated = true;
            Vec3d pos = galath.getPositionVector();
            Vec3d slipR = galath.getCachedBoneOffset("slipR").add(pos);
            Vec3d slipL = galath.getCachedBoneOffset("slipL").add(pos);
            Vec3d turnable = galath.getCachedBoneOffset("turnable").add(pos);
            galath.world
               .spawnParticle(EnumParticleTypes.DRAGON_BREATH, slipR.x, slipR.y, slipR.z, 0.0, 0.0, 0.0, new int[0]);
            galath.world
               .spawnParticle(EnumParticleTypes.DRAGON_BREATH, slipL.x, slipL.y, slipL.z, 0.0, 0.0, 0.0, new int[0]);
            galath.world
               .spawnParticle(EnumParticleTypes.DRAGON_BREATH, turnable.x, turnable.y, turnable.z, 0.0, 0.0, 0.0, new int[0]);
            break;
         case "rapeIntroDone":
            if (galath.isControlledByLocalPlayer()) {
               galath.setCurrentAction(Action.RAPE_ON_GOING);
            }
            break;
         case "rape_switch":
            Random random = galath.getRNG();
            int oldState = galath.rapeVariant;

            do {
               galath.rapeVariant = random.nextInt(3);
            } while (galath.rapeVariant == oldState);

            if (!galath.hasMaster() && galath.isControlledByLocalPlayer()) {
               EntityPlayerSP player = Minecraft.getMinecraft().player;
               if (0.0F >= player.getHealth() - 1.0F) {
                  galath.setCurrentAction(Action.RAPE_CUM);
               }
            }
            break;
         case "poundRape":
            galath.playRandomSound(SoundHandler.MISC_POUNDING);
            if (galath.isControlledByLocalPlayer()) {
               if (galath.hasMaster()) {
                  HornyMeterHud.addToHornyMeter(0.03F);
               } else {
                  PacketHandler.networkWrapper.sendToServer(new GalathRapePouncePacket(true));
               }
            }
            break;
         case "rapeHurt":
            if (!galath.hasMaster() && galath.isControlledByLocalPlayer()) {
               PacketHandler.networkWrapper.sendToServer(new GalathRapePouncePacket(false));
            }
            break;
         case "enableRapeUI":
            if (galath.isControlledByLocalPlayer()) {
               if (galath.hasMaster()) {
                  HornyMeterHud.setHornyMeterVisible(false);
               } else {
                  EscapeMinigameHud.showMinigame(galath);
               }
            }
            break;
         case "removeUI":
            if (galath.isControlledByLocalPlayer() && !galath.hasMaster()) {
               EscapeMinigameHud.failMinigame();
            }
            break;
         case "reloadRenderer":
            if (!galath.isControlledByLocalPlayer()) {
               return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            if (mc.gameSettings.thirdPersonView != 0) {
               mc.renderGlobal.loadRenderers();
            }
            break;
         case "corruptSwitch":
            if (galath.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
               galath.setCurrentAction(Action.CORRUPT_FAST);
            }
            break;
         case "corrupt_hard":
            if (galath.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
               galath.corruptHard = true;
               galath.resetAnimationControllerOffset();
            }
            break;
         case "corrupt_hard_end":
            galath.setCurrentAction(Action.CORRUPT_SLOW);
            galath.corruptHard = false;
            break;
         case "addCum":
            HornyMeterHud.addToHornyMeter(0.03);
            break;
         case "clearcum":
            CummyEntity.spawnCummyTrails(galath);
            break;
         case "setCamCorrupt":
            if (!galath.isControlledByLocalPlayer()) {
               return;
            }

            galath.corruptIntroActive = true;
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float yaw = galath.getYawRotation() + 220.0F;
            Vec3d corruptPos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - player.getEyeHeight(), 0.4F), galath.getYawRotation()).add(galath.getTargetPosition());
            PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), corruptPos, yaw, 15.0F));
            HornyMeterHud.showHornyMeter();
            break;
         case "enableBoyCam":
            if (galath.isControlledByLocalPlayer()) {
               galath.corruptIntroActive = false;
            }
            break;
         case "masterbateCumming":
            if (CommandFuta.ENABLED) {
               CummyEntity.registerTrail(new DynamicTrailRenderer(90, girl -> {
                  Vec3d cockTipPos = girl.getBoneWorldPosition("futaCockTip");
                  Vec3d tipDir = girl.getBoneWorldPosition("futaCockTipDirHelp");
                  return cockTipPos.subtract(tipDir).normalize();
               }, girl -> girl.getCachedBoneOffset("futaCockTip").add(girl.getTargetPosition()), galath, 0.3F, 0.3F));
            }
            break;
         case "creampie":
            CummyEntity.registerTrail(
               new DynamicTrailRenderer(
                  100,
                  girl -> VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.6F), galath.getYawRotation()),
                  girl -> girl.getCachedBoneOffset("creampiePos").add(girl.getTargetPosition()),
                  galath,
                  0.6F,
                  0.5F
               )
            );
         case "creampieGalath":
            if (CommandFuta.ENABLED) {
               CummyEntity.registerTrail(new DynamicTrailRenderer(130, girl -> {
                  Vec3d cockTipPos = girl.getBoneWorldPosition("futaCockTip");
                  Vec3d tipDir = girl.getBoneWorldPosition("futaCockTipDirHelp");
                  return cockTipPos.subtract(tipDir).normalize();
               }, girl -> girl.getCachedBoneOffset("futaCockTip").add(girl.getTargetPosition()), galath, 0.3F, 0.3F));
            }

            galath.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 3.0F);
            break;
         case "blackScreenTamed":
            if (galath.hasMaster()) {
               return;
            }
         case "blackScreen":
            if (galath.isControlledByLocalPlayer()) {
               BeeScreen.enableInteraction();
            }
            break;
         case "blackScreenMaster":
            if (Minecraft.getMinecraft().player.getPersistentID().equals(galath.getMasterUUID())) {
               BeeScreen.enableInteraction();
               HandlePlayerMovement.setMovementLock(false);
            }
            break;
         case "flapControlled":
            if (galath.isControlledByLocalPlayer()) {
               GalathFlightHud.showHud();
               galath.playRandomSound(SoundHandler.MISC_FLAP);
               Minecraft flapMc = Minecraft.getMinecraft();
               EntityPlayerSP flapPlayer = flapMc.player;
               MovementInput input = flapPlayer.movementInput;
               Vec2f moveVec = input.getMoveVector();
               if (moveVec.x != 0.0F || moveVec.y != 0.0F) {
                  Vec3d vel = VectorMath.rotateByYawPitch(
                     new Vec3d(-moveVec.x, 0.0, moveVec.y),
                     RotationHelper.lerp(flapPlayer.prevRotationPitch, flapPlayer.rotationPitch, flapMc.getRenderPartialTicks()),
                     RotationHelper.lerp(flapPlayer.prevRotationYawHead, flapPlayer.rotationYawHead, flapMc.getRenderPartialTicks())
                  );
                  PacketHandler.networkWrapper.sendToServer(new UpdateVelocityPacket(vel, galath.getGirlId()));
               }
            }
            break;
         case "clap":
            galath.playRandomSound(SoundHandler.MISC_CLAP);
            break;
         case "energysound":
            galath.playSound(SoundHandler.MISC_BEEW[1]);
            break;
         case "energy2":
            galath.playSound(SoundHandler.MISC_BEEW[2]);
            break;
         case "tpSound":
            galath.playSound(SoundHandler.MISC_WEOWEO[2]);
            break;
         case "lick":
            galath.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
            break;
         case "setCoinLook":
            if (galath.isControlledByLocalPlayer()) {
               EntityPlayerSP coinPlayer = Minecraft.getMinecraft().player;
               float coinYaw = galath.getYawRotation() + 180.0F;
               coinPlayer.rotationYaw = coinYaw;
               coinPlayer.prevRotationYaw = coinYaw;
               coinPlayer.rotationPitch = 0.0F;
               coinPlayer.prevRotationPitch = 0.0F;
            }
            break;
         case "sexui":
            if (galath.isControlledByLocalPlayer()) {
               HornyMeterHud.showHornyMeter();
            }
            break;
         case "boostSound":
            Minecraft.getMinecraft().player.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_LIGHTCHARGE), 1.0F, 1.0F);
            Minecraft.getMinecraft().player.playSound(SoundHandler.randomSound(SoundHandler.MISC_FLAP), 1.0F, 1.0F);
      }
   }
}
