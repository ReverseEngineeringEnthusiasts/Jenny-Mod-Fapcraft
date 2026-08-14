package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.BeeModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class BeePlayerEntity extends AbstractPlayerGirlEntity {
   protected BeePlayerEntity(World var1) {
      super(var1);
   }

   public BeePlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public void B_clash233() {
      this.handleOwnerUUID(true);
   }

   @Override
   public void onTickClient() {
      this.handleOwnerUUID(false);
   }

   @Override
   public float getScaleFactor() {
      return 1.4F;
   }

   public float getEyeHeight() {
      return 1.3F;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new BeeModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/bee/hand.png";
   }

   @Override
   public void handleOwnerCommand(String var1, UUID var2) {
      this.sendActionPacket(0, Action.CITIZEN_START);
      this.setOutfitIndex(0);
      this.setCurrentAction(Action.CITIZEN_START);
      this.teleportPlayerToGirl(var2);
      EntityPlayer var3 = this.world.getPlayerEntityByUUID(var2);
      if (var3 != null) {
         Vec3d var4 = this.getVectorTowardPlayer(-0.2);
         var3.setPositionAndUpdate(var4.x, var4.y, var4.z);
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      openInventoryGui(var1, this, new String[]{"action.names.sex"}, false);
      return true;
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.CITIZEN_CUM || action != Action.CITIZEN_FAST && action != Action.COWGIRLSLOW) {
         super.setCurrentAction(action);
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
   }

   @Override
   public boolean canBeInteracted() {
      return false;
   }

   @Override
   protected Action getNextAction(Action var1) {
      return var1 == Action.CITIZEN_SLOW ? Action.CITIZEN_FAST : null;
   }

   @Override
   protected Action getCumAction(Action var1) {
      return var1 != Action.CITIZEN_FAST && var1 != Action.CITIZEN_SLOW ? null : Action.CITIZEN_CUM;
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.setOutfitIndex(1);
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.bee.null", true, var1);
            } else {
               this.createAnimation("animation.bee.idle", true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.bee.null", false, var1);
                  break;
               case CITIZEN_START:
                  this.createAnimation("animation.bee.sex_start", false, var1);
                  break;
               case CITIZEN_SLOW:
                  this.createAnimation("animation.bee.sex_slow", true, var1);
                  break;
               case CITIZEN_FAST:
                  this.createAnimation("animation.bee.sex_fast", true, var1);
                  break;
               case CITIZEN_CUM:
                  this.createAnimation("animation.bee.sex_cum", false, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.bee.throw_pearl", true, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.bee.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.bee.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.createAnimation("animation.bee.ride", true, var1);
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
         switch (var1x.sound) {
            case "attackDone":
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "pearl":
               if (this.isLocalPlayerNearby() && this.getCurrentAction() == Action.THROW_PEARL) {
                  PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               }
               break;
            case "resetCumPercentage":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "sex_fastMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "sex_startMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "sex_fastReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "sex_fastDone":
               if (!this.isControlledByLocalPlayer() || HandlePlayerMovement.isJumping) {
                  return;
               }
            case "sex_startDone":
               this.setCurrentAction(Action.CITIZEN_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "sex_cumMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_CUMINFLATION), 2.0F);
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               break;
            case "blackscreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "sex_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
   }

}
