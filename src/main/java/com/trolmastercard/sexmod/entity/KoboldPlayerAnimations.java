package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.resource.GeckoLibCache;

/**
 * GeckoLib animation glue for {@link KoboldPlayerEntity}: the per-tick pose
 * predicate (eyes / movement / action controllers) and the controller
 * registration with the sound listener that drives the blowjob, anal and
 * mating-press scenes.
 * <p>
 * <b>Pitfalls.</b> The predicate publishes the size scalar to the GeckoLib
 * parser ({@code "size"} = {@code 0.25F - aA}) on every call — the model reads
 * it, so it must run before any animation is created. The pose flags are
 * per-entity and mutated from the listener: {@code ay} = blowjob "switch"
 * suffix, {@code az} = blowjob side (R/L), {@code ax} = moan counter. The
 * movement controller uses a 3-tick transition. {@code mating_press_startDone}
 * intentionally falls through to {@code mating_press_hardDone}.
 */
final class KoboldPlayerAnimations {
   private KoboldPlayerAnimations() {
   }

   static <E extends IAnimatable> PlayState predicate(KoboldPlayerEntity kobold, AnimationEvent<E> event) {
      if (kobold.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      float shrink = 0.25F - (Float)kobold.getDataManager().get(KoboldEntity.aE);
      GeckoLibCache.getInstance().parser.setValue("size", shrink);
      switch (event.getController().getName()) {
         case "eyes":
            if (kobold.getCurrentAction() == Action.NULL && kobold.getCurrentAction().autoBlink) {
               kobold.createAnimation("animation.kobold.blink", true, event);
            } else {
               kobold.createAnimation("animation.kobold.null", true, event);
            }
            break;
         case "movement":
            if (kobold.getCurrentAction() != Action.NULL) {
               kobold.createAnimation("animation.kobold.null", true, event);
            } else if (kobold.playerRiding) {
               kobold.createAnimation("animation.kobold.sit", true, event);
            } else {
               if (kobold.movementController.getCurrentAnimation() != null
                  && kobold.movementController.getCurrentAnimation().animationName.contains("fly")
                  && kobold.playerOnGround) {
                  kobold.aB = !kobold.aB;
               }

               if (!kobold.playerOnGround) {
                  kobold.createAnimation("animation.kobold.fly" + (kobold.aB ? "2" : ""), true, event);
               } else if (Math.abs(kobold.playerMotionOffset.x) + Math.abs(kobold.playerMotionOffset.y) > 0.0F) {
                  if (kobold.playerSprinting) {
                     kobold.movementController.setAnimationSpeed(1.2F);
                     kobold.createAnimation("animation.kobold.run", true, event);
                  } else if (kobold.playerMotionOffset.y >= -0.1F) {
                     kobold.movementController.setAnimationSpeed(2.0);
                     kobold.createAnimation("animation.kobold.walk", true, event);
                  } else {
                     kobold.movementController.setAnimationSpeed(1.75);
                     kobold.createAnimation("animation.kobold.backwards_walk", true, event);
                  }
               } else {
                  kobold.createAnimation("animation.kobold.idle", true, event);
               }
            }
            break;
         case "action":
            switch (kobold.getCurrentAction()) {
               case NULL:
                  kobold.createAnimation("animation.kobold.null", true, event);
                  break;
               case STRIP:
                  kobold.createAnimation("animation.kobold.strip", false, event);
                  break;
               case ATTACK:
                  kobold.createAnimation("animation.kobold.attack" + kobold.nextAttack, false, event);
                  break;
               case BOW:
                  kobold.createAnimation("animation.kobold.bowcharge", false, event);
                  break;
               case SIT:
                  kobold.createAnimation("animation.kobold.sit", true, event);
                  break;
               case MINE:
                  kobold.createAnimation("animation.kobold.fall_tree", true, event);
                  break;
               case PAYMENT:
                  kobold.createAnimation("animation.kobold.paymentBackpack", true, event);
                  break;
               case STARTBLOWJOB:
                  kobold.createAnimation("animation.kobold.blowjobStart", false, event);
                  break;
               case SUCKBLOWJOB_BLINK:
                  String side = kobold.az ? "R" : "L";
                  String animSuffix = kobold.ay ? "Switch" : "";
                  kobold.createAnimation("animation.kobold.blowjobSlow" + side + animSuffix, true, event);
                  break;
               case THRUSTBLOWJOB:
                  kobold.createAnimation("animation.kobold.blowjobFast", true, event);
                  break;
               case CUMBLOWJOB:
                  kobold.createAnimation("animation.kobold.blowjobCum", false, event);
                  break;
               case KOBOLD_ANAL_START:
                  kobold.createAnimation("animation.kobold.analStart", false, event);
                  break;
               case KOBOLD_ANAL_SLOW:
                  kobold.createAnimation("animation.kobold.analSoft", true, event);
                  break;
               case KOBOLD_ANAL_FAST:
                  kobold.createAnimation("animation.kobold.analHard", true, event);
                  break;
               case KOBOLD_ANAL_CUM:
                  kobold.createAnimation("animation.kobold.analCum", true, event);
                  break;
               case SLEEP:
                  kobold.createAnimation("animation.kobold.sleep", true, event);
                  break;
               case MATING_PRESS_START:
                  kobold.createAnimation("animation.kobold.mating_press_start", false, event);
                  break;
               case MATING_PRESS_SOFT:
                  kobold.createAnimation("animation.kobold.mating_press_soft", true, event);
                  break;
               case MATING_PRESS_HARD:
                  kobold.createAnimation("animation.kobold.mating_press_hard", true, event);
                  break;
               case MATING_PRESS_CUM:
                  kobold.createAnimation("animation.kobold.mating_press_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   static void registerControllers(KoboldPlayerEntity kobold, AnimationData data) {
      if (kobold.actionController == null) {
         kobold.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "attackDone":
               if (++kobold.nextAttack == 3) {
                  kobold.nextAttack = 0;
               }
               break;
            case "paymentMSG1":
               kobold.sendChatMessageToPlayer(kobold.getInteractionPlayerUUID(), "I'd like to use ur services owo");
               kobold.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "plob":
               kobold.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "blackScreen":
               if (kobold.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "paymentDone":
               if (kobold.isControlledByLocalPlayer()) {
                  kobold.U();
               }
               break;
            case "blowjobStartMSG1":
               if (kobold.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.625 - player.getEyeHeight(), -1.0), kobold.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           kobold.getInteractionPlayerUUID().toString(), kobold.getTargetPosition().add(pos), kobold.getYawRotation() + 180.0F, 0.0F
                        )
                     );
               }
               break;
            case "blowjobStartMSG2":
               if (kobold.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5 - player.getEyeHeight(), -0.6875), kobold.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           kobold.getInteractionPlayerUUID().toString(), kobold.getTargetPosition().add(pos), kobold.getYawRotation() + 180.0F - 40.0F, 0.0F
                        )
                     );
               }
               break;
            case "lipsound":
               if (kobold.getRNG().nextBoolean()) {
                  kobold.playRandomSoundAtVolume(SoundHandler.GIRLS_ALLIE_LIPSOUND, 1.5F);
               } else {
                  kobold.playRandomSoundAtVolume(SoundHandler.GIRLS_JENNY_LIPSOUND, 1.5F);
               }

               HornyMeterHud.addToHornyMeter(0.02F);
               break;
            case "touch":
               kobold.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "blowjobStartDone":
               kobold.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               kobold.ay = false;
               kobold.az = true;
               if (kobold.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "switch":
               kobold.ay = kobold.getRNG().nextBoolean();
               kobold.actionController.clearAnimationCache();
               break;
            case "endSwitch":
               kobold.ay = false;
               kobold.az = !kobold.az;
               kobold.actionController.clearAnimationCache();
               break;
            case "blowjobFastDone":
               if (kobold.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  kobold.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               }
               break;
            case "cumLoud":
               kobold.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "cumQuiet":
               kobold.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "analCumDone":
            case "blowjobCumDone":
               if (kobold.isControlledByLocalPlayer()) {
                  kobold.resetCameraAndPhysics();
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "analStartDone":
               kobold.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               if (kobold.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "analStartCam":
               if (kobold.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.5625 - player.getEyeHeight(), 0.5625), kobold.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(kobold.getInteractionPlayerUUID().toString(), kobold.getTargetPosition().add(pos), kobold.getYawRotation(), 0.0F));
               }
               break;
            case "pounding":
               kobold.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "analFastRapid":
               if (kobold.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  if (kobold.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                     kobold.resetAnimationControllerOffset();
                  } else {
                     kobold.setCurrentAction(Action.KOBOLD_ANAL_FAST);
                  }
               }
               break;
            case "analDone":
               if (kobold.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                  kobold.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               }
               break;
            case "analHard":
               if (kobold.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "analSoft":
               if (kobold.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cum":
               kobold.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "giggle":
               kobold.playRandomSounds(SoundHandler.GIRLS_KOBOLD_GIGGLE);
               break;
            case "moan":
               kobold.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               break;
            case "moanMating":
               kobold.ax--;
               if (kobold.ax <= 0) {
                  kobold.ax = 3;
                  kobold.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "analHardMSG1":
               kobold.ax--;
               if (kobold.ax <= 0) {
                  kobold.ax = 4;
                  kobold.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "orgasm":
               kobold.playRandomSounds(SoundHandler.GIRLS_KOBOLD_ORGASM);
               break;
            case "breath":
               kobold.playRandomSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING, 0.5F);
               break;
            case "haa":
               kobold.playRandomSound(SoundHandler.GIRLS_KOBOLD_HAA, 0.7F);
               break;
            case "interested":
               kobold.playRandomSounds(SoundHandler.GIRLS_KOBOLD_INTERESTED);
               break;
            case "yep":
               kobold.playRandomSounds(SoundHandler.GIRLS_KOBOLD_YEP);
               break;
            case "bjmoan":
               kobold.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_BJMOAN));
               break;
            case "blowjobStartbreath":
               int soundId = kobold.getRNG().nextInt(3);
               kobold.playSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING[soundId]);
               break;
            case "matingCam":
               if (kobold.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 0.4375 - player.eyeHeight, -0.6875);
                  pos = VectorMath.rotateByYaw(pos, kobold.getYawRotation() + 180.0F);
                  pos = pos.add(kobold.getTargetPosition());
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, kobold.getYawRotation() + 180.0F, 10.0F));
               }
               break;
            case "mating_press_startDone":
               if (kobold.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
            case "mating_press_hardDone":
               if (kobold.isControlledByLocalPlayer()) {
                  kobold.setCurrentAction(Action.MATING_PRESS_SOFT);
               }
               break;
            case "mating_press_softReady":
               if (kobold.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (kobold.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  kobold.setCurrentAction(Action.MATING_PRESS_HARD);
               }
               break;
            case "mating_press_hardReady":
               if (kobold.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (kobold.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  kobold.resetAnimationControllerOffset();
               }
               break;
            case "mating_cum_cam":
               if (kobold.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 1.1875 - player.eyeHeight, 0.125);
                  pos = VectorMath.rotateByYaw(pos, kobold.getYawRotation() + 180.0F);
                  pos = pos.add(kobold.getTargetPosition());
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, kobold.getYawRotation() + 180.0F, 70.0F));
               }
               break;
            case "cumMsg":
               kobold.sendChatMessage("I.. hope I am satisfying you sir");
               kobold.playSound(SoundHandler.GIRLS_KOBOLD_SAD[kobold.getRNG().nextInt(1)]);
               break;
            case "mating_press_cumDone":
               if (kobold.isControlledByLocalPlayer()) {
                  kobold.resetCameraAndPhysics();
               }
         }
      };
      kobold.movementController.transitionLengthTicks = 3.0;
      kobold.actionController.registerSoundListener(soundListener);
      data.addAnimationController(kobold.actionController);
      data.addAnimationController(kobold.movementController);
      data.addAnimationController(kobold.eyesController);
   }
}
