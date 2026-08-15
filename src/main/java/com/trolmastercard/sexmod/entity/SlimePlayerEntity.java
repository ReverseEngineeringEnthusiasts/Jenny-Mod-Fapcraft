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

/**
 * <b>Role.</b> Player-form Slime — the transformation with the same blowjob and
 * doggy scene set as the slime NPC (no pregnancy lifecycle).
 * <p>
 * <b>Scene flow.</b> The only owner command is {@code blowjob} (starts
 * {@link Action#SUCKBLOWJOB} directly); the doggy scene waits in
 * {@link Action#WAITDOGGY} inside {@link #updateAITasks()} for a player within
 * 1 block, then locks both players in and starts {@link Action#DOGGYSTART}.
 * Progression and scene end run in the {@code ISoundListener}
 * ({@code bjcDone}/{@code doggyCumDone} -&gt; {@code resetCameraAndPhysics()}).
 */
public class SlimePlayerEntity extends AbstractPlayerGirlEntity {
   boolean ap = false;
   int aq = 0;

   protected SlimePlayerEntity(World world) {
      super(world);
   }

   public SlimePlayerEntity(World world, UUID uuid) {
      super(world, uuid);
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
   public IVanillaModel getHandModel(int index) {
      return new KoboldModel();
   }

   @Override
   public String getHandTexture(int index) {
      return "textures/entity/slime/hand.png";
   }

   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      if ("action.names.blowjob".equals(command)) {
         this.sendActionPacket(0, Action.SUCKBLOWJOB);
         this.setCurrentAction(Action.SUCKBLOWJOB);
         this.teleportPlayerToGirl(uuid);
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      openInventoryGui(player, this, new String[]{"action.names.blowjob"}, false);
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
   protected Action getNextAction(Action action) {
      if (action == Action.SUCKBLOWJOB) {
         return Action.THRUSTBLOWJOB;
      } else {
         return action == Action.DOGGYSLOW ? Action.DOGGYFAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.SUCKBLOWJOB || action == Action.THRUSTBLOWJOB) {
         return Action.CUMBLOWJOB;
      } else {
         return action != Action.DOGGYSLOW && action != Action.DOGGYFAST ? null : Action.DOGGYCUM;
      }
   }

   /**
    * SERVER (and CLIENT mirror): the doggy bed phase — waits in
    * {@link Action#WAITDOGGY} for a player within 1 block, locks them in
    * (movement lock, noClip, noGravity, flying) and starts
    * {@link Action#DOGGYSTART}.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getCurrentAction() == Action.WAITDOGGY) {
         EntityPlayer player = this.getNearestPlayer();
         if (player != null) {
            if (!(player.getPositionVector().distanceTo(this.getPositionVec3d()) > 1.0)) {
               PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
               this.setInteractionPlayerUUID(player.getPersistentID());
               player.rotationYaw = this.getYawRotation();
               this.cameraYaw = this.getYawRotation();
               player.setPosition(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z);
               player.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
               this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);
               this.setCurrentAction(Action.DOGGYSTART);
               player.setNoGravity(true);
               player.noClip = true;
               EntityPlayer ownerPlayer = this.world.getPlayerEntityByUUID(this.getOwnerUserUUID());
               if (ownerPlayer != null) {
                  ownerPlayer.setNoGravity(true);
                  ownerPlayer.capabilities.isFlying = true;
               }

               player.noClip = true;
               player.capabilities.isFlying = true;
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.slime.fhappy", true, event);
            } else {
               this.createAnimation("animation.slime.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.slime.null", true, event);
            } else if (this.ak) {
               this.createAnimation("animation.slime.sit", true, event);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.createAnimation("animation.slime.fly" + (this.ap ? "2" : ""), true, event);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.createAnimation("animation.slime.run", true, event);
                  } else if (this.ao.y >= -0.1F) {
                     this.createAnimation("animation.slime.walk", true, event);
                  } else {
                     this.createAnimation("animation.slime.backwards_walk", true, event);
                  }
               } else {
                  this.createAnimation("animation.slime.idle", true, event);
               }
            }
            break;
         case "action":
            if (this.getCurrentAction() == Action.NULL) {
               this.createAnimation("animation.slime.null", true, event);
            } else {
               switch (this.getCurrentAction()) {
                  case UNDRESS:
                     this.createAnimation("animation.slime.undress", false, event);
                     break;
                  case DRESS:
                     this.createAnimation("animation.slime.dress", false, event);
                     break;
                  case STRIP:
                     this.createAnimation("animation.slime.strip", false, event);
                     break;
                  case SUCKBLOWJOB:
                     this.createAnimation("animation.slime.blowjobsuck", true, event);
                     break;
                  case THRUSTBLOWJOB:
                     this.createAnimation("animation.slime.blowjobthrust", true, event);
                     break;
                  case CUMBLOWJOB:
                     this.createAnimation("animation.slime.blowjobcum", false, event);
                     break;
                  case STARTDOGGY:
                     this.createAnimation("animation.slime.doggygoonbed", false, event);
                     break;
                  case WAITDOGGY:
                     this.createAnimation("animation.slime.doggywait", true, event);
                     break;
                  case DOGGYSTART:
                     this.createAnimation("animation.slime.doggystart", false, event);
                     break;
                  case DOGGYSLOW:
                     this.createAnimation("animation.slime.doggyslow", true, event);
                     break;
                  case DOGGYFAST:
                     this.createAnimation("animation.slime.doggyfast", true, event);
                     break;
                  case DOGGYCUM:
                     this.createAnimation("animation.slime.doggycum", false, event);
                     break;
                  case ATTACK:
                     this.createAnimation("animation.slime.attack" + this.nextAttack, false, event);
                     break;
                  case BOW:
                     this.createAnimation("animation.slime.bowcharge", false, event);
                     break;
                  case RIDE:
                     this.createAnimation("animation.slime.ride", true, event);
                     break;
                  case SIT:
                     this.createAnimation("animation.slime.sit", true, event);
               }
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * blowjob/doggy scenes; {@code bjcDone}/{@code doggyCumDone} -&gt;
    * {@code resetCameraAndPhysics()}, {@code undress}/{@code dress} -&gt;
    * outfit change + reset.
    */
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         String soundName = sound.sound;
         switch (soundName) {
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
               int choice = Reference.RANDOM.nextInt(4);
               if (choice == 0) {
                  choice = Reference.RANDOM.nextInt(2);
                  if (choice == 0) {
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
                  int choice2 = Reference.RANDOM.nextInt(2);
                  if (choice2 == 0) {
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
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.eyesController);
      data.addAnimationController(this.movementController);
   }

}
