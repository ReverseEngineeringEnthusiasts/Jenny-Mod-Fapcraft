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

/**
 * <b>Role.</b> Player-form Bee — the transformation with the single citizen
 * scene (start/slow/fast/cum). Flight is granted to the owner while the
 * transformation is active ({@code B_clash233}/{@code onTickClient} pair).
 * Progression and scene end run in the sound listener
 * ({@code sex_cumDone} -&gt; {@code resetCameraAndPhysics()}).
 */
public class BeePlayerEntity extends AbstractPlayerGirlEntity {
   protected BeePlayerEntity(World world) {
      super(world);
   }

   public BeePlayerEntity(World world, UUID uuid) {
      super(world, uuid);
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
   public IVanillaModel getHandModel(int index) {
      return new BeeModel();
   }

   @Override
   public String getHandTexture(int index) {
      return "textures/entity/bee/hand.png";
   }

   /**
    * SERVER: the single owner command (sex) — broadcasts, strips, starts
    * {@link Action#CITIZEN_START} and positions the acting player in front of
    * the girl.
    */
   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      this.sendActionPacket(0, Action.CITIZEN_START);
      this.setOutfitIndex(0);
      this.setCurrentAction(Action.CITIZEN_START);
      this.teleportPlayerToGirl(uuid);
      EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
      if (player != null) {
         Vec3d offset = this.getVectorTowardPlayer(-0.2);
         player.setPositionAndUpdate(offset.x, offset.y, offset.z);
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      openInventoryGui(player, this, new String[]{"action.names.sex"}, false);
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
   protected Action getNextAction(Action action) {
      return action == Action.CITIZEN_SLOW ? Action.CITIZEN_FAST : null;
   }

   @Override
   protected Action getCumAction(Action action) {
      return action != Action.CITIZEN_FAST && action != Action.CITIZEN_SLOW ? null : Action.CITIZEN_CUM;
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.setOutfitIndex(1);
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      switch (event.getController().getName()) {
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.bee.null", true, event);
            } else {
               this.createAnimation("animation.bee.idle", true, event);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.bee.null", false, event);
                  break;
               case CITIZEN_START:
                  this.createAnimation("animation.bee.sex_start", false, event);
                  break;
               case CITIZEN_SLOW:
                  this.createAnimation("animation.bee.sex_slow", true, event);
                  break;
               case CITIZEN_FAST:
                  this.createAnimation("animation.bee.sex_fast", true, event);
                  break;
               case CITIZEN_CUM:
                  this.createAnimation("animation.bee.sex_cum", false, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.bee.throw_pearl", true, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.bee.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.bee.bowcharge", false, event);
                  break;
               case RIDE:
                  this.createAnimation("animation.bee.ride", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * citizen scene; {@code sex_startDone}/{@code sex_fastDone} -&gt;
    * {@link Action#CITIZEN_SLOW} (jump keeps fast), {@code sex_cumDone} -&gt;
    * {@code resetCameraAndPhysics()}.
    */
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
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
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
   }

}
