package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.KoboldModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerForGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class SlimePlayerEntity extends AbstractPlayerGirlEntity {
   boolean ap = false;
   int aq = 0;

   protected SlimePlayerEntity(World var1) {
      super(var1);
   }

   public SlimePlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float getScaleFactor() {
      return 1.6F;
   }

   public float getEyeHeight() {
      return 1.64F;
   }

   @Override
   public boolean canBeInteracted() {
      return false;
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new KoboldModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/slime/hand.png";
   }

   @Override
   public void handleOwnerCommand(String var1, UUID var2) {
      if ("action.names.blowjob".equals(var1)) {
         this.sendActionPacket(0, Action.SUCKBLOWJOB);
         this.setCurrentAction(Action.SUCKBLOWJOB);
         this.teleportPlayerToGirl(var2);
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      openInventoryGui(var1, this, new String[]{"action.names.blowjob"}, false);
      return true;
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.CUMBLOWJOB || action != Action.THRUSTBLOWJOB && action != Action.SUCKBLOWJOB) {
         if (this.getCurrentAction() != Action.DOGGYCUM || action != Action.DOGGYFAST && action != Action.DOGGYSLOW) {
            super.setCurrentAction(action);
         }
      }
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.SUCKBLOWJOB) {
         return Action.THRUSTBLOWJOB;
      } else {
         return var1 == Action.DOGGYSLOW ? Action.DOGGYFAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.SUCKBLOWJOB || var1 == Action.THRUSTBLOWJOB) {
         return Action.CUMBLOWJOB;
      } else {
         return var1 != Action.DOGGYSLOW && var1 != Action.DOGGYFAST ? null : Action.DOGGYCUM;
      }
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getCurrentAction() == Action.WAITDOGGY) {
         EntityPlayer var1 = this.getNearestPlayer();
         if (var1 != null) {
            if (!(var1.getPositionVector().distanceTo(this.getPositionVec3d()) > 1.0)) {
               PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
               this.setInteractionPlayerUUID(var1.getPersistentID());
               var1.rotationYaw = this.getYawRotation();
               this.cameraYaw = this.getYawRotation();
               var1.setPosition(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z);
               var1.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
               this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);
               this.setCurrentAction(Action.DOGGYSTART);
               var1.setNoGravity(true);
               var1.noClip = true;
               EntityPlayer var2 = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
               var2.setNoGravity(true);
               var1.noClip = true;
               var1.capabilities.isFlying = true;
               var2.capabilities.isFlying = true;
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.slime.fhappy", true, var1);
            } else {
               this.createAnimation("animation.slime.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.slime.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.slime.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.createAnimation("animation.slime.fly" + (this.ap ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.createAnimation("animation.slime.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.createAnimation("animation.slime.walk", true, var1);
                  } else {
                     this.createAnimation("animation.slime.backwards_walk", true, var1);
                  }
               } else {
                  this.createAnimation("animation.slime.idle", true, var1);
               }
            }
            break;
         case "action":
            if (this.getCurrentAction() == Action.NULL) {
               this.createAnimation("animation.slime.null", true, var1);
            } else {
               switch (this.getCurrentAction()) {
                  case UNDRESS:
                     this.createAnimation("animation.slime.undress", false, var1);
                     break;
                  case DRESS:
                     this.createAnimation("animation.slime.dress", false, var1);
                     break;
                  case STRIP:
                     this.createAnimation("animation.slime.strip", false, var1);
                     break;
                  case SUCKBLOWJOB:
                     this.createAnimation("animation.slime.blowjobsuck", true, var1);
                     break;
                  case THRUSTBLOWJOB:
                     this.createAnimation("animation.slime.blowjobthrust", true, var1);
                     break;
                  case CUMBLOWJOB:
                     this.createAnimation("animation.slime.blowjobcum", false, var1);
                     break;
                  case STARTDOGGY:
                     this.createAnimation("animation.slime.doggygoonbed", false, var1);
                     break;
                  case WAITDOGGY:
                     this.createAnimation("animation.slime.doggywait", true, var1);
                     break;
                  case DOGGYSTART:
                     this.createAnimation("animation.slime.doggystart", false, var1);
                     break;
                  case DOGGYSLOW:
                     this.createAnimation("animation.slime.doggyslow", true, var1);
                     break;
                  case DOGGYFAST:
                     this.createAnimation("animation.slime.doggyfast", true, var1);
                     break;
                  case DOGGYCUM:
                     this.createAnimation("animation.slime.doggycum", false, var1);
                     break;
                  case ATTACK:
                     this.createAnimation("animation.slime.attack" + this.nextAttack, false, var1);
                     break;
                  case BOW:
                     this.createAnimation("animation.slime.bowcharge", false, var1);
                     break;
                  case RIDE:
                     this.createAnimation("animation.slime.ride", true, var1);
                     break;
                  case SIT:
                     this.createAnimation("animation.slime.sit", true, var1);
               }
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData var1) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         String var2x = var1x.sound;
         switch (var2x) {
            case "attackDone":
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "undress":
               if (this.isLocalPlayerNearby()) {
                  this.entityDataManager.set(OUTFIT_INDEX, 0);
                  this.resetCameraAndPhysics();
               }
               break;
            case "dress":
               if (this.isLocalPlayerNearby()) {
                  this.entityDataManager.set(OUTFIT_INDEX, 1);
                  this.setCurrentAction((Action)null);
                  this.resetCameraAndPhysics();
               }
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "bjiMSG10":
               if (this.isControlledByLocalPlayer()) {
                  this.positionPlayerRelative(-0.4, -0.8, -0.2, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
               this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_SQUISH, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjiMSG12":
               if (Reference.RANDOM.nextInt(5) == 0) {
                  this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_JUMP, 0.5F);
               }

               this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_SQUISH, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjtMSG1":
               this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               this.playSound(SoundEvents.ENTITY_SLIME_DEATH);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "bjiDone":
               this.setCurrentAction(Action.SUCKBLOWJOB);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "bjtDone":
               this.setCurrentAction(Action.SUCKBLOWJOB);
               break;
            case "doggyfastReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "bjtReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "bjcMSG1":
               this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
               break;
            case "bjcMSG2":
               this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "doggyslowMSG2":
               this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               break;
            case "bjcBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "bjcDone":
            case "doggyCumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "doggyGoOnBedMSG1":
               this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
               this.cameraYaw = this.rotationYaw;
               break;
            case "doggyGoOnBedDone":
               PacketHandler.networkWrapper.sendToServer(new SetPlayerForGirlPacket(this.getGirlId(), Minecraft.getMinecraft().player.getPersistentID()));
               this.setCurrentAction(Action.WAITDOGGY);
               break;
            case "doggystartMSG1":
               this.playSound(SoundHandler.MISC_TOUCH[0]);
               break;
            case "doggystartMSG2":
               this.playSound(SoundHandler.MISC_TOUCH[1]);
               break;
            case "doggystartMSG3":
               this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_SQUISH, 0.25F);
               break;
            case "doggystartMSG4":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 1.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "doggystartMSG5":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               break;
            case "doggystartDone":
               this.setCurrentAction(Action.DOGGYSLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doggyslowMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               int var5 = Reference.RANDOM.nextInt(4);
               if (var5 == 0) {
                  var5 = Reference.RANDOM.nextInt(2);
                  if (var5 == 0) {
                     this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
                  } else {
                     this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
                  }
               } else {
                  this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.00666);
               }
               break;
            case "doggyfastMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.aq++;
               if (this.aq % 2 == 0) {
                  int var6 = Reference.RANDOM.nextInt(2);
                  if (var6 == 0) {
                     this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
                  } else {
                     this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
                  }
               } else {
                  this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               }
               break;
            case "doggyfastDone":
               this.setCurrentAction(Action.DOGGYSLOW);
               break;
            case "doggycumMSG1":
               this.playSoundAtVolume(SoundHandler.MISC_CUMINFLATION[0], 4.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 2.0F);
               this.playSound(SoundEvents.ENTITY_SLIME_DEATH);
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.eyesController);
      var1.addAnimationController(this.movementController);
   }

}
