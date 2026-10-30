package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.renderer.WildSlimeFaceLayer;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.resource.GeckoLibCache;

/**
 * GeckoLib animation glue for {@link KoboldEntity}: the per-tick animation
 * predicate (eyes / movement / action controllers) and the controller
 * registration with its sound listener.
 * <p>
 * <b>Pitfalls.</b> The sound listener drives scene progression: the
 * {@code *Done} keys move the action state machine forward, {@code switch}/
 * {@code endSwitch} toggle the blowjob side ({@code WildSlimeFaceLayer} /
 * {@code switchVariant}) and must clear the animation cache, and {@code paymentDone}
 * calls {@code startActionFromHandState()} which re-dispatches the hand state. Do not reorder the
 * switch cases: {@code mating_press_startDone} intentionally falls through to
 * {@code mating_press_hardDone}.
 */
final class KoboldAnimation {
   private KoboldAnimation() {
   }

   static <E extends IAnimatable> PlayState animationPredicate(KoboldEntity entity, AnimationEvent<E> event) {
      if (entity.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      if (entity.actionController == null) {
         entity.initAnimationControllers();
      }

      float shrink = 0.25F - (Float)entity.getDataManager().get(KoboldEntity.SIZE_SCALAR);
      GeckoLibCache.getInstance().parser.setValue("size", shrink);
      switch (event.getController().getName()) {
         case "eyes":
            if (entity.getCurrentAction() != Action.NULL) {
               entity.createAnimation("animation.kobold.null", true, event);
            } else {
               entity.createAnimation("animation.kobold.blink", true, event);
            }
            break;
         case "movement":
            if (entity.getCurrentAction() != Action.NULL) {
               entity.createAnimation("animation.kobold.null", true, event);
            } else if (entity.isRiding()) {
               entity.createAnimation("animation.kobold.sit", true, event);
            } else {
               double moved = Math.abs(entity.prevPosX - entity.posX) + Math.abs(entity.prevPosZ - entity.posZ);
               if (!(Boolean)entity.getDataManager().get(KoboldEntity.IS_ANCHORED) && moved > 0.0) {
                  if (entity.onGround && Math.abs(Math.abs(entity.prevPosY) - Math.abs(entity.posY)) < 0.1F) {
                     entity.rotationYaw = entity.rotationYawHead;
                     double speed = 1.0 + shrink * 2.0F;
                     entity.movementController.setAnimationSpeed(speed);
                     if (entity.isBlockedByCeiling()) {
                        entity.createAnimation("animation.kobold.crouch_walk", true, event);
                     } else if ((Boolean)entity.getDataManager().get(KoboldEntity.ARMED)) {
                        entity.createAnimation("animation.kobold.run_armed", true, event);
                     } else if (moved > 0.2F) {
                        entity.createAnimation("animation.kobold.run", true, event);
                     } else {
                        entity.createAnimation("animation.kobold.walk", true, event);
                     }
                  } else {
                     entity.createAnimation("animation.kobold.fly", true, event);
                  }
               } else if (entity.isBlockedByCeiling()) {
                  entity.createAnimation("animation.kobold.crouch_idle", true, event);
               } else {
                  entity.createAnimation(entity.getDataManager().get(KoboldEntity.ARMED) ? "animation.kobold.idle_armed" : "animation.kobold.idle", true, event);
               }
            }
            break;
         case "action":
            switch (entity.getCurrentAction()) {
               case NULL:
                  entity.createAnimation("animation.kobold.null", true, event);
                  break;
               case ATTACK:
                  entity.createAnimation("animation.kobold.attack", false, event);
                  break;
               case RIDE:
               case SIT:
                  entity.createAnimation("animation.kobold.sit", true, event);
                  break;
               case MINE:
                  entity.createAnimation("animation.kobold.fall_tree", true, event);
                  break;
               case PAYMENT:
                  entity.createAnimation("animation.kobold.paymentBackpack", true, event);
                  break;
               case STARTBLOWJOB:
                  entity.createAnimation("animation.kobold.blowjobStart", false, event);
                  break;
               case SUCKBLOWJOB_BLINK:
                  String side = entity.WildSlimeFaceLayer ? "R" : "L";
                  String animSuffix = entity.switchVariant ? "Switch" : "";
                  entity.createAnimation("animation.kobold.blowjobSlow" + side + animSuffix, true, event);
                  break;
               case THRUSTBLOWJOB:
                  entity.createAnimation("animation.kobold.blowjobFast", true, event);
                  break;
               case CUMBLOWJOB:
                  entity.createAnimation("animation.kobold.blowjobCum", false, event);
                  break;
               case KOBOLD_ANAL_START:
                  entity.createAnimation("animation.kobold.analStart", false, event);
                  break;
               case KOBOLD_ANAL_SLOW:
                  entity.createAnimation("animation.kobold.analSoft", true, event);
                  break;
               case KOBOLD_ANAL_FAST:
                  entity.createAnimation("animation.kobold.analHard", true, event);
                  break;
               case KOBOLD_ANAL_CUM:
                  entity.createAnimation("animation.kobold.analCum", true, event);
                  break;
               case SLEEP:
                  entity.createAnimation("animation.kobold.sleep", true, event);
                  break;
               case MATING_PRESS_START:
                  entity.createAnimation("animation.kobold.mating_press_start", false, event);
                  break;
               case MATING_PRESS_SOFT:
                  entity.createAnimation("animation.kobold.mating_press_soft", true, event);
                  break;
               case MATING_PRESS_HARD:
                  entity.createAnimation("animation.kobold.mating_press_hard", true, event);
                  break;
               case MATING_PRESS_CUM:
                  entity.createAnimation("animation.kobold.mating_press_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   static void registerControllers(KoboldEntity entity, AnimationData data) {
      if (entity.actionController == null) {
         entity.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "attackSound":
               entity.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG);
               break;
            case "paymentMSG1":
               entity.sendChatMessageToPlayer(entity.getInteractionPlayerUUID(), "I'd like to use ur services owo");
               entity.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "plob":
               entity.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "blackScreen":
               if (entity.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "paymentDone":
               if (entity.isControlledByLocalPlayer()) {
                  entity.startActionFromHandState();
               }
               break;
            case "blowjobStartMSG1":
               if (entity.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.625 - player.getEyeHeight(), -1.0), entity.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(entity.getInteractionPlayerUUID().toString(), entity.getTargetPosition().add(pos), entity.getYawRotation() + 180.0F, 0.0F)
                     );
               }
               break;
            case "blowjobStartMSG2":
               if (entity.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5 - player.getEyeHeight(), -0.6875), entity.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           entity.getInteractionPlayerUUID().toString(), entity.getTargetPosition().add(pos), entity.getYawRotation() + 180.0F - 40.0F, 0.0F
                        )
                     );
               }
               break;
            case "lipsound":
               if (entity.getRNG().nextBoolean()) {
                  entity.playRandomSoundAtVolume(SoundHandler.GIRLS_ALLIE_LIPSOUND, 1.5F);
               } else {
                  entity.playRandomSoundAtVolume(SoundHandler.GIRLS_JENNY_LIPSOUND, 1.5F);
               }

               HornyMeterHud.addToHornyMeter(0.02F);
               break;
            case "touch":
               entity.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "blowjobStartDone":
               entity.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               entity.switchVariant = false;
               entity.WildSlimeFaceLayer = true;
               if (entity.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "switch":
               entity.switchVariant = entity.getRNG().nextBoolean();
               entity.actionController.clearAnimationCache();
               break;
            case "endSwitch":
               entity.switchVariant = false;
               entity.WildSlimeFaceLayer = !entity.WildSlimeFaceLayer;
               entity.actionController.clearAnimationCache();
               break;
            case "blowjobFastDone":
               if (entity.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  entity.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               }
               break;
            case "cumLoud":
               entity.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "cumQuiet":
               entity.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "analCumDone":
            case "blowjobCumDone":
               if (entity.isControlledByLocalPlayer()) {
                  entity.resetCameraAndPhysics();
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "analStartDone":
               entity.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               if (entity.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "analStartCam":
               if (entity.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.5625 - player.getEyeHeight(), 0.5625), entity.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(entity.getInteractionPlayerUUID().toString(), entity.getTargetPosition().add(pos), entity.getYawRotation(), 0.0F));
               }
               break;
            case "pounding":
               entity.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "analFastRapid":
               if (entity.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  if (entity.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                     entity.actionController.tickOffset = 0.0;
                  }

                  entity.setCurrentAction(Action.KOBOLD_ANAL_FAST);
               }
               break;
            case "analDone":
               if (entity.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                  entity.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               }
               break;
            case "analHard":
               if (entity.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "analSoft":
               if (entity.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cum":
               entity.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "giggle":
               entity.playRandomSounds(SoundHandler.GIRLS_KOBOLD_GIGGLE);
               break;
            case "moan":
               entity.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               break;
            case "moanMating":
               entity.moanCooldown--;
               if (entity.moanCooldown <= 0) {
                  entity.moanCooldown = 3;
                  entity.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "analHardMSG1":
               entity.moanCooldown--;
               if (entity.moanCooldown <= 0) {
                  entity.moanCooldown = 4;
                  entity.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "orgasm":
               entity.playRandomSounds(SoundHandler.GIRLS_KOBOLD_ORGASM);
               break;
            case "breath":
               entity.playRandomSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING, 0.5F);
               break;
            case "haa":
               entity.playRandomSound(SoundHandler.GIRLS_KOBOLD_HAA, 0.7F);
               break;
            case "interested":
               entity.playRandomSounds(SoundHandler.GIRLS_KOBOLD_INTERESTED);
               break;
            case "yep":
               entity.playRandomSounds(SoundHandler.GIRLS_KOBOLD_YEP);
               break;
            case "bjmoan":
               entity.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_BJMOAN));
               break;
            case "blowjobStartbreath":
               int soundId = entity.getRNG().nextInt(3);
               entity.playSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING[soundId]);
               break;
            case "matingCam":
               if (entity.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 0.4375 - player.eyeHeight, -0.6875);
                  pos = VectorMath.rotateByYaw(pos, entity.getYawRotation() + 180.0F);
                  pos = pos.add(entity.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, entity.getYawRotation() + 180.0F, 10.0F));
               }
               break;
            case "mating_press_startDone":
               if (entity.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
            case "mating_press_hardDone":
               if (entity.isControlledByLocalPlayer()) {
                  entity.setCurrentAction(Action.MATING_PRESS_SOFT);
               }
               break;
            case "mating_press_softReady":
               if (entity.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (entity.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  entity.setCurrentAction(Action.MATING_PRESS_HARD);
               }
               break;
            case "mating_press_hardReady":
               if (entity.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (entity.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  entity.resetAnimationControllerOffset();
               }
               break;
            case "mating_cum_cam":
               if (entity.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 1.1875 - player.eyeHeight, 0.125);
                  pos = VectorMath.rotateByYaw(pos, entity.getYawRotation() + 180.0F);
                  pos = pos.add(entity.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, entity.getYawRotation() + 180.0F, 70.0F));
               }
               break;
            case "cumMsg":
               entity.sendChatMessage("I.. hope I am satisfying you sir");
               entity.playSound(SoundHandler.GIRLS_KOBOLD_SAD[entity.getRNG().nextInt(1)]);
               break;
            case "renderEgg":
               entity.isRenderEgg = true;
               entity.playRandomSoundAtVolume(SoundHandler.MISC_PLOB, 0.5F);
               break;
            case "mating_press_cumDone":
               if (entity.isControlledByLocalPlayer()) {
                  entity.resetCameraAndPhysics();
               }
         }
      };
      entity.movementController.transitionLengthTicks = 10.0;
      entity.actionController.registerSoundListener(soundListener);
      data.addAnimationController(entity.actionController);
      data.addAnimationController(entity.movementController);
      data.addAnimationController(entity.eyesController);
   }
}
