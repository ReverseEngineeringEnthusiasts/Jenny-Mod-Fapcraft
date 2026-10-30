package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * GeckoLib animation wiring for {@link GoblinPlayerEntity}: the per-controller
 * pose predicate ({@code eyes}/{@code movement}/{@code action}) and the
 * controller registration with the sound listener that drives the catch
 * dialogue, paizuri, nelson and breeding scenes.
 * <p>
 * <b>Pitfalls.</b> The pose flags are per-entity and mutated here from the
 * listener: {@code aF} (nelson fast "c" pose), {@code ay} (nelson slow base
 * pose), {@code aB} (breeding side), {@code aH} (breeding fast pose) and
 * {@code aD} (paizuri slow suffix). The movement controller uses a 2-tick
 * transition. {@code breedingCam2} intentionally falls through to
 * {@code breedingIntroDone}.
 */
final class GoblinPlayerAnimations {
   private GoblinPlayerAnimations() {
   }

   static <E extends IAnimatable> PlayState predicate(GoblinPlayerEntity goblin, AnimationEvent<E> event) {
      if (goblin.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (goblin.getCurrentAction() == Action.NULL && goblin.getCurrentAction().autoBlink) {
               goblin.createAnimation("animation.goblin.blink", true, event);
            } else {
               goblin.createAnimation("animation.goblin.null", true, event);
            }
            break;
         case "movement":
            if (goblin.getCurrentAction() != Action.NULL) {
               goblin.createAnimation("animation.goblin.null", true, event);
            } else if (goblin.playerRiding) {
               goblin.createAnimation("animation.goblin.sit", true, event);
            } else {
               if (goblin.movementController.getCurrentAnimation() != null
                  && goblin.movementController.getCurrentAnimation().animationName.contains("fly")
                  && goblin.playerOnGround) {
                  goblin.aC = !goblin.aC;
               }

               if (!goblin.playerOnGround) {
                  goblin.createAnimation("animation.goblin.fly" + (goblin.aC ? "2" : ""), true, event);
               } else if (Math.abs(goblin.playerMotionOffset.x) + Math.abs(goblin.playerMotionOffset.y) > 0.0F) {
                  if (goblin.playerSprinting) {
                     goblin.movementController.setAnimationSpeed(1.2F);
                     goblin.createAnimation("animation.goblin.running", true, event);
                  } else if (goblin.playerMotionOffset.y >= -0.1F) {
                     goblin.movementController.setAnimationSpeed(2.0);
                     goblin.createAnimation("animation.goblin.walk", true, event);
                  } else {
                     goblin.movementController.setAnimationSpeed(1.5);
                     goblin.createAnimation("animation.goblin.backwards_walk", true, event);
                  }
               } else {
                  goblin.createAnimation("animation.goblin.idle", true, event);
               }
            }
            break;
         case "action":
            Minecraft mc = Minecraft.getMinecraft();
            String camMode = mc.player.getPersistentID().equals(goblin.getOwnerUUID()) && mc.gameSettings.thirdPersonView == 0 ? "1" : "3";
            switch (goblin.getCurrentAction()) {
               case NELSON_CUM:
                  goblin.createAnimation("animation.goblin.nelson_cum", true, event);
                  break;
               case NELSON_FAST:
                  goblin.createAnimation("animation.goblin.nelson_fast" + (goblin.aF ? "c" : "s"), true, event);
                  break;
               case NELSON_INTRO:
                  goblin.createAnimation("animation.goblin.nelson_intro", true, event);
                  break;
               case NELSON_SLOW:
                  goblin.createAnimation("animation.goblin.nelson_slow" + (goblin.ay ? "" : "2"), true, event);
                  break;
               case PAIZURI_IDLE:
                  goblin.createAnimation("animation.goblin.paizuri_idle", true, event);
                  break;
               case PAIZURI_SLOW:
                  goblin.createAnimation("animation.goblin.paizuri_slow" + goblin.aD, true, event);
                  break;
               case BREEDING_SLOW_0:
                  goblin.createAnimation("animation.goblin.breeding_slow_1" + (goblin.aB ? "l" : "r"), true, event);
                  break;
               case BREEDING_SLOW_2:
                  goblin.createAnimation("animation.goblin.breeding_slow_3", true, event);
                  break;
               case PAIZURI_FAST:
                  goblin.createAnimation("animation.goblin.paizuri_fast", true, event);
                  break;
               case PAIZURI_FAST_CONTINUES:
                  goblin.createAnimation("animation.goblin.paizuri_fast_countinues", true, event);
                  break;
               case BREEDING_1:
                  goblin.createAnimation("animation.goblin.breeding_2", true, event);
                  break;
               case BREEDING_FAST_2:
                  goblin.createAnimation("animation.goblin.breeding_fast_3", true, event);
                  break;
               case SHOULDER_IDLE:
                  goblin.createAnimation("animation.goblin.shoulder_idle", true, event);
                  break;
               case PICK_UP:
                  goblin.createAnimation(String.format("animation.goblin.pick_up_%sperson", camMode), true, event);
                  break;
               case START_THROWING:
                  goblin.createAnimation(String.format("animation.goblin.throw_%sperson", camMode), true, event);
                  break;
               case THROWN:
                  goblin.createAnimation("animation.goblin.thrown", true, event);
                  break;
               case NULL:
                  goblin.createAnimation("animation.goblin.null", true, event);
                  break;
               case STAND_UP:
                  goblin.createAnimation("animation.goblin.stand_up", false, event);
                  break;
               case STRIP:
                  goblin.createAnimation("animation.goblin.strip", false, event);
                  break;
               case ATTACK:
                  goblin.createAnimation("animation.goblin.attack" + goblin.nextAttack, false, event);
                  break;
               case BOW:
                  goblin.createAnimation("animation.goblin.bowcharge", false, event);
                  break;
               case SIT:
                  goblin.createAnimation("animation.goblin.sit", true, event);
                  break;
               case BREEDING_INTRO_0:
                  goblin.createAnimation("animation.goblin.breeding_intro_1", true, event);
                  break;
               case BREEDING_INTRO_1:
                  goblin.createAnimation("animation.goblin.breeding_intro_2", true, event);
                  break;
               case BREEDING_INTRO_2:
                  goblin.createAnimation("animation.goblin.breeding_intro_3", true, event);
                  break;
               case BREEDING_FAST_0:
                  goblin.createAnimation("animation.goblin.breeding_fast_1" + (goblin.aH ? "c" : "s"), true, event);
                  break;
               case BREEDING_CUM_0:
                  goblin.createAnimation("animation.goblin.breeding_cum_1", true, event);
                  break;
               case BREEDING_CUM_1:
                  goblin.createAnimation("animation.goblin.breeding_cum_2", true, event);
                  break;
               case BREEDING_CUM_2:
                  goblin.createAnimation("animation.goblin.breeding_cum_3", true, event);
                  break;
               case PAIZURI_START:
                  goblin.createAnimation("animation.goblin.paizuri_start", true, event);
                  break;
               case PAIZURI_CUM:
                  goblin.createAnimation("animation.goblin.paizuri_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   static void registerControllers(GoblinPlayerEntity goblin, AnimationData data) {
      if (goblin.actionController == null) {
         goblin.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "attackDone":
               if (++goblin.nextAttack == 3) {
                  goblin.nextAttack = 0;
               }
               break;
            case "catchEh":
               goblin.sendChatMessage("ehh..");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchAkward":
               goblin.sendChatMessage("awkward..");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchWell":
               goblin.sendChatMessage("well...");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchRather":
               goblin.sendChatMessage("would you rather have this stupid... thing?");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchMe":
               goblin.sendChatMessage("...or use me?~");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchDone":
               if ("bj".equals(goblin.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES))) {
                  goblin.setCurrentAction(Action.CATCH_BJ);
               }
               break;
            case "catchBjDone":
               goblin.setCurrentAction(Action.CATCH_BJ_IDLE);
               if (goblin.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  BaseGirlEntity.openInventoryGui(player, goblin, new String[]{"use her", "take ur stuff back"}, null, false);
               }
               break;
            case "paizuriChoice":
               goblin.sendChatMessage("good choice!~");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriBoth":
               goblin.sendChatMessage("...for both of us!");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizruiUse":
               goblin.sendChatMessage("now use me like a fuck toy!~");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriSwitch":
               if (!goblin.getRNG().nextBoolean()) {
                  goblin.aD = "".equals(goblin.aD) ? "2" : "";
               }
               break;
            case "touch":
               goblin.playRandomSoundAtVolume(SoundHandler.MISC_TOUCH, 3.0F);
               break;
            case "pound":
               goblin.playRandomSound(SoundHandler.MISC_POUNDING);
               if (goblin.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "paizuri_startDone":
               goblin.setCurrentAction(Action.PAIZURI_IDLE);
               if (goblin.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastDone":
               goblin.setCurrentAction(Action.PAIZURI_SLOW);
               break;
            case "paizuriFastReady":
               if (goblin.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  goblin.setCurrentAction(Action.PAIZURI_FAST_CONTINUES);
               }
               break;
            case "paizuriFastContinuesReady":
            case "neslon_fastBackSwitch":
               if (goblin.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  goblin.resetAnimationControllerOffset();
               }
               break;
            case "smallPound":
               goblin.playRandomSoundAtVolume(SoundHandler.MISC_POUNDING, 0.25F);
               if (goblin.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "paizruiCam":
               if (goblin.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  player.rotationPitch = 70.0F;
                  player.prevRotationPitch = 70.0F;
               }
               break;
            case "blackScreen":
               if (goblin.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "cumSound":
               goblin.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "jumpCam":
               if (goblin.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.player.rotationYaw = goblin.getYawRotation() + 170.0F;
                  mc.player.rotationPitch = -20.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 2;
               }
               break;
            case "breedingHmm":
               if (goblin.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.player.rotationYaw = goblin.getYawRotation() + 180.0F;
                  mc.player.rotationPitch = -15.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 0;
               }

               goblin.sendChatMessage("hmm...");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingFound":
               goblin.sendChatMessage("guess we found a worthy breeding partner!");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingEnough":
               goblin.sendChatMessage("Eh.. go pin him down, before he runs off!");
               goblin.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingCam2":
               if (goblin.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.gameSettings.thirdPersonView = 2;
                  mc.player.rotationYaw = goblin.getYawRotation() - 120.0F;
                  mc.player.rotationPitch = -30.0F;
               }
            case "breedingIntroDone":
               goblin.setCurrentAction(Action.BREEDING_SLOW_0);
               if (goblin.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "breeding_slow1Done":
               if (goblin.getRNG().nextBoolean()) {
                  goblin.aB = !goblin.aB;
               }

               if (goblin.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  goblin.setCurrentAction(Action.BREEDING_FAST_0);
                  goblin.aH = false;
               }
               break;
            case "breeding_fast1Done":
               goblin.setCurrentAction(Action.BREEDING_SLOW_0);
               if (goblin.isControlledByLocalPlayer()) {
                  goblin.aH = false;
               }
               break;
            case "breeding_fast1Ready":
               if (goblin.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  goblin.aH = true;
                  goblin.resetAnimationControllerOffset();
                  goblin.actionController.tickOffset = 0.0;
               }
               break;
            case "cum":
               goblin.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "breeding_intro_3Done":
               goblin.setCurrentAction(Action.BREEDING_SLOW_2);
               break;
            case "breeding_3_wiggle":
               if (goblin.getRNG().nextBoolean()) {
                  goblin.actionController.tickOffset = 0.0;
               }
               break;
            case "breeding_fast_3Done":
               if (goblin.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  goblin.setCurrentAction(Action.BREEDING_SLOW_2);
               }
               break;
            case "breeding_intro_2Done":
               goblin.setCurrentAction(Action.BREEDING_1);
               break;
            case "breeding_cumCam":
               if (goblin.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.gameSettings.thirdPersonView = 0;
                  mc.player.rotationYaw = goblin.getYawRotation() + 180.0F;
                  mc.player.rotationPitch = -15.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 0;
               }
               break;
            case "neslon_introDone":
               goblin.setCurrentAction(Action.NELSON_SLOW);
               if (goblin.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "nelson_slowDone":
               if (goblin.getRNG().nextBoolean()) {
                  goblin.ay = !goblin.ay;
               }
               break;
            case "neslon_fastSwitch":
               if (!goblin.isControlledByLocalPlayer()) {
                  goblin.aF = true;
                  return;
               }

               if (HandlePlayerMovement.isJumping) {
                  goblin.aF = true;
               }
               break;
            case "nelsonFastDone":
               goblin.aF = false;
               if (goblin.isControlledByLocalPlayer()) {
                  goblin.setCurrentAction(Action.NELSON_SLOW);
               }
               break;
            case "paizuriCumDone":
            case "nelson_cumDone":
               if (goblin.isControlledByLocalPlayer()) {
                  goblin.resetCameraAndPhysics();
                  goblin.setCurrentAction(Action.NULL);
               }
         }
      };
      goblin.actionController.registerSoundListener(soundListener);
      goblin.movementController.transitionLengthTicks = 2.0;
      data.addAnimationController(goblin.actionController);
      data.addAnimationController(goblin.movementController);
      data.addAnimationController(goblin.eyesController);
   }
}
