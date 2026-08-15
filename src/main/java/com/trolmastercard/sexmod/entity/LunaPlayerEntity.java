package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.LunaModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> Player-form Luna — the catgirl with touch-boobs
 * (intro/slow/fast/cum), cowgirl-sitting (intro/slow/fast/cum) and headpat
 * scenes. Her {@link #handleLunaOwner()} drives the cowgirl sitting scene with
 * a 25-tick approach countdown ({@code ar}) instead of the Bia-style contact
 * countdown.
 * <p>
 * <b>Scene flow.</b> {@code touchboobs}/{@code headpat} come in via
 * {@link #handleOwnerCommand(String, UUID)}; the interaction menu also offers
 * them. The cowgirl scene starts from {@link #handleInteraction()} -&gt;
 * {@link Action#WAIT_CAT}; {@link #handleLunaOwner()} then counts ticks while
 * the nearest player stays within 1.25 blocks — at tick 25 (SERVER) the
 * player is locked in, rotated to face her and {@link Action#COWGIRL_SITTING_INTRO}
 * starts; the CLIENT mirrors the countdown with
 * {@link #setFishingLevelFor(EntityPlayer, int)} (unlock movement at 0,
 * third-person at 25).
 * <p>
 * <b>Pitfalls.</b> {@code ar} resets to 0 whenever the action leaves
 * {@code WAIT_CAT} (see {@link #onUpdate()}). {@link #setCurrentAction(Action)}
 * forbids re-entering loop phases while the cum animation plays. The
 * movement controller uses a 10-tick transition length.
 */
public class LunaPlayerEntity extends AbstractPlayerGirlEntity {
   int ar = 0;
   boolean aq = false;
   boolean ap = false;
   boolean as = false;

   protected LunaPlayerEntity(World world) {
      super(world);
   }

   public LunaPlayerEntity(World world, UUID uuid) {
      super(world, uuid);
   }

   @Override
   public float getScaleFactor() {
      return 1.6F;
   }

   public float getEyeHeight() {
      return 1.34F;
   }

   @Override
   public IVanillaModel getHandModel(int index) {
      return new LunaModel();
   }

   @Override
   public String getHandTexture(int index) {
      return "textures/entity/cat/hand.png";
   }

   /**
    * SERVER: owner commands — {@code touchboobs} strips, broadcasts and starts
    * {@link Action#TOUCH_BOOBS_INTRO}; {@code headpat} plays the headpat
    * animation. Both teleport the acting player into the scene.
    */
   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      if ("action.names.touchboobs".equals(command)) {
         this.sendActionPacket(0, Action.TOUCH_BOOBS_INTRO);
         this.setCurrentAction(Action.TOUCH_BOOBS_INTRO);
         this.entityDataManager.set(OUTFIT_INDEX, 0);
         this.teleportPlayerToGirl(uuid);
      }

      if ("action.names.headpat".equals(command)) {
         this.setCurrentAction(Action.HEAD_PAT);
         this.teleportPlayerToGirl(uuid);
      }
   }

   /**
    * CLIENT: starts the cowgirl-sitting approach ({@link Action#WAIT_CAT}).
    */
   @Override
   public void handleInteraction() {
      this.setCurrentAction(Action.WAIT_CAT);
   }

   @Override
   public boolean canBeInteracted() {
      return true;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      openInventoryGui(player, this, new String[]{"action.names.touchboobs", "action.names.headpat"}, false);
      return true;
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.COWGIRL_SITTING_CUM || action != Action.COWGIRL_SITTING_SLOW && action != Action.COWGIRL_SITTING_FAST) {
         if (this.getCurrentAction() != Action.TOUCH_BOOBS_CUM || action != Action.TOUCH_BOOBS_FAST && action != Action.TOUCH_BOOBS_SLOW) {
            super.setCurrentAction(action);
         }
      }
   }

   /**
    * BOTH sides: while in {@link Action#WAIT_CAT} runs the 25-tick sitting
    * countdown ({@code ar}); any other action resets the counter to 0.
    * SERVER starts {@link Action#COWGIRL_SITTING_INTRO} at tick 25; CLIENT
    * mirrors the UI unlock at tick 0 and third-person switch at tick 25 via
    * {@link #setFishingLevelFor(EntityPlayer, int)}.
    */
   @Override
   public void onUpdate() {
      super.onUpdate();
      if (Action.WAIT_CAT.equals(this.getCurrentAction())) {
         this.handleLunaOwner();
      } else {
         this.ar = 0;
      }
   }

   /**
    * The cowgirl sitting countdown body (see {@link #onUpdate()}). SERVER:
    * at tick 25 binds the interaction player, locks their movement, rotates
    * them to face the girl and starts the intro. CLIENT: unlocks movement at
    * tick 0 and forces third-person at tick 25.
    */
   void handleLunaOwner() {
      EntityPlayer player = this.getNearestPlayer();
      if (player != null) {
         if (!(player.getDistance(this.posX, this.getPositionVec3d().y, this.posZ) > 1.25)) {
            if (this.world.isRemote) {
               this.setFishingLevelFor(player, this.ar);
            } else if (this.ar == 25) {
               this.setInteractionPlayerUUID(player.getPersistentID());
               player.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
               player.setPositionAndUpdate(this.getPositionVector().x, this.getPositionVec3d().y, this.getPositionVector().z);
               this.setCurrentAction(Action.COWGIRL_SITTING_INTRO);
               player.setRotationYawHead(this.getYawRotation() + 180.0F);
               player.rotationYaw = this.getYawRotation() + 180.0F;
               player.prevRotationYaw = this.getYawRotation() + 180.0F;
               this.cameraYaw = this.getYawRotation() + 180.0F;
               this.positionPlayerRelative(0.0, -0.075F, -0.7109375, 0.0F, 0.0F);
               this.entityDataManager.set(OUTFIT_INDEX, 0);
            }

            this.ar++;
         }
      }
   }

   /**
    * CLIENT: per-tick mirror of the sitting countdown for the local player —
    * at tick 0 enables interaction and unlocks movement, at tick 25 switches
    * to third-person view.
    */
   @SideOnly(Side.CLIENT)
   void setFishingLevelFor(EntityPlayer player, int level) {
      if (level == 0) {
         EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
         if (player.getPersistentID().equals(localPlayer.getPersistentID())) {
            BeeScreen.enableInteraction();
            localPlayer.setVelocity(0.0, 0.0, 0.0);
            HandlePlayerMovement.setMovementLock(false);
         }
      }

      if (level == 25) {
         EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
         if (player.getPersistentID().equals(localPlayer.getPersistentID())) {
            Minecraft.getMinecraft().gameSettings.thirdPersonView = 2;
         }
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.TOUCH_BOOBS_SLOW) {
         return Action.TOUCH_BOOBS_FAST;
      } else {
         return action == Action.COWGIRL_SITTING_SLOW ? Action.COWGIRL_SITTING_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.TOUCH_BOOBS_SLOW || action == Action.TOUCH_BOOBS_FAST) {
         return Action.TOUCH_BOOBS_CUM;
      } else {
         return action != Action.COWGIRL_SITTING_FAST && action != Action.COWGIRL_SITTING_SLOW ? null : Action.COWGIRL_SITTING_CUM;
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.cat.blink", true, event);
            } else {
               this.createAnimation("animation.cat.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.cat.null", true, event);
            } else if (this.ak) {
               this.createAnimation("animation.cat.sit", true, event);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.aq = !this.aq;
               }

               if (!this.af) {
                  this.createAnimation("animation.cat.fly" + (this.aq ? "2" : ""), true, event);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation("animation.cat.run", true, event);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.cat.fastwalk", true, event);
                  } else {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.cat.backwards_walk", true, event);
                  }
               } else {
                  this.createAnimation("animation.cat.idle", true, event);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.cat.null", true, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.cat.attack" + this.nextAttack, false, event);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.cat.sit", true, event);
                  break;
               case BOW:
                  this.createAnimation("animation.cat.bowcharge", false, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.cat.throwpearl", true, event);
                  break;
               case DOWNED:
                  this.createAnimation("animation.cat.downed", true, event);
                  break;
               case FISHING_START:
                  this.createAnimation("animation.cat.start_fishing", false, event);
                  break;
               case FISHING_IDLE:
                  this.createAnimation("animation.cat.idle_fishing", true, event);
                  break;
               case FISHING_EAT:
                  this.createAnimation("animation.cat.eat_fishing", false, event);
                  break;
               case FISHING_THROW_AWAY:
                  this.createAnimation("animation.cat.throw_away", false, event);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.cat.payment", false, event);
                  break;
               case TOUCH_BOOBS_INTRO:
                  this.createAnimation("animation.cat.touch_boobs_intro", false, event);
                  break;
               case TOUCH_BOOBS_SLOW:
                  this.createAnimation("animation.cat.touch_boobs_slow" + (this.ap ? "1" : ""), true, event);
                  break;
               case TOUCH_BOOBS_FAST:
                  this.createAnimation("animation.cat.touch_boobs_fast", true, event);
                  break;
               case TOUCH_BOOBS_CUM:
                  this.createAnimation("animation.cat.touch_boobs_cum", false, event);
                  break;
               case WAIT_CAT:
                  this.createAnimation("animation.cat.wait", false, event);
                  break;
               case COWGIRL_SITTING_INTRO:
                  this.createAnimation("animation.cat.sitting_intro", false, event);
                  break;
               case COWGIRL_SITTING_SLOW:
                  this.createAnimation("animation.cat.sitting_slow", true, event);
                  break;
               case COWGIRL_SITTING_FAST:
                  this.createAnimation("animation.cat.sitting_fast", true, event);
                  break;
               case COWGIRL_SITTING_CUM:
                  this.createAnimation("animation.cat.sitting_cum", true, event);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.cat.head_pat", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener that drives
    * the touch-boobs/sitting/headpat scenes. Key transitions:
    * {@code touch_boobs_introDone} -&gt; {@link Action#TOUCH_BOOBS_SLOW},
    * {@code sitting_introDone} -&gt; {@link Action#COWGIRL_SITTING_SLOW},
    * {@code fastDone}/{@code sitting_fastDone} -&gt; slow (jump keeps fast),
    * {@code touch_boobs_cumDone}/{@code resetGirl} -&gt;
    * {@code resetCameraAndPhysics()}, {@code paymentDone} -&gt; {@link #U()}.
    * {@code sitting_fastTp}/{@code sitting_fastDone} reposition the local
    * player relative to the girl's target position.
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
            case "idleDone":
               this.as = this.getRNG().nextInt(10) == 0;
               break;
            case "idle2Done":
               this.as = false;
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "paymentMSG1":
               this.sendChatMessageToPlayer(this.getInteractionPlayerUUID(), "Here, I know u like fish and yea.. these are for you");
               this.playSound(SoundHandler.MISC_PLOB[0]);
               break;
            case "paymentMSG2":
               this.sendChatMessage("huh~?");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "paymentMSG3":
               this.sendChatMessage("nyyyaaaa~ :D");
               int[] soundIds = new int[]{1, 7, 10, 11};
               int soundId = soundIds[this.getRNG().nextInt(soundIds.length)];
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[soundId]);
               break;
            case "paymentMSG4":
               this.sendChatMessage("tankuuuu owowowo");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_OWO);
               break;
            case "paymentDone":
               if (this.isLocalPlayerNearby()) {
                  this.U();
               }

               this.scaleFactor = 1.0F;
               break;
            case "breath":
            case "rod_breath":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_LIGHTBREATHING);
               break;
            case "happyOh":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HAPPYOH);
               break;
            case "cutenya3":
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[3]);
               break;
            case "cutenya2":
               this.playSound(SoundHandler.GIRLS_LUNA_CUTENYA[2]);
               break;
            case "huh":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "hmph":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HMPH);
               break;
            case "hehe":
            case "giggle":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "singing":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_SINGING);
               break;
            case "touch_boobsMSG1":
               this.sendChatMessage("comon~ touch me hihi~");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               break;
            case "touch":
               this.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "jump":
               this.playSoundAtVolume(SoundHandler.MISC_JUMP[0], 0.2F);
               break;
            case "horninya":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HORNINYA);
               break;
            case "horninya2":
            case "touch_boobs_cumMSG3":
            case "sitting_cumMSG1":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[1]);
               this.playSoundAtVolume(SoundHandler.MISC_CUMINFLATION[0], 5.0F);
               break;
            case "moan":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               break;
            case "touch_boobs_introDone":
               this.setCurrentAction(Action.TOUCH_BOOBS_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
                  HandlePlayerMovement.setMovementLock(false);
               }
               break;
            case "touch_boobs_slowDone":
               if (this.ap) {
                  this.ap = false;
               } else {
                  this.ap = Math.random() < 0.5;
               }
               break;
            case "addCumSlow":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "addCumFast":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.TOUCH_BOOBS_SLOW);
               }
               break;
            case "moanOrNya":
               if (Math.random() > 0.5) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
               }
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "touch_boobs_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "resetGirl":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
               }
               break;
            case "touch_boobs_cumMSG1":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[3]);
               break;
            case "touch_boobs_cumMSG2":
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[9]);
               break;
            case "call_playerMSG1":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.sendChatMessage("come here - big guy hehe~");
               break;
            case "pounding":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               break;
            case "sitting_introMSG1":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_GIGGLE);
               this.sendChatMessage("hehe~");
               break;
            case "sitting_introDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.COWGIRL_SITTING_SLOW);
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "sitting_slowMSG1":
               if (this.getRNG().nextBoolean()) {
                  if (this.getRNG().nextBoolean()) {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
                     break;
                  }

                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_LIGHTBREATHING));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "sitting_fastMSG1":
               if (this.getRNG().nextBoolean()) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_HORNINYA));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_LUNA_MOAN));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "sitting_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.COWGIRL_SITTING_SLOW);
                  Vec3d headOffset = new Vec3d(0.0, -0.075F, -0.7109375);
                  Vec3d rotatedHead = VectorMath.rotateByYaw(headOffset, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + rotatedHead.x,
                        this.getTargetPosition().y - 0.0 + rotatedHead.y,
                        this.getTargetPosition().z + rotatedHead.z
                     );
               }
               break;
            case "sitting_fastTp":
               if (this.isControlledByLocalPlayer()) {
                  Vec3d backOffset = new Vec3d(0.0, -0.160625, -0.9925);
                  Vec3d rotatedBack = VectorMath.rotateByYaw(backOffset, this.getYawRotation() + 180.0F);
                  Minecraft.getMinecraft()
                     .player
                     .setPosition(
                        this.getTargetPosition().x + rotatedBack.x,
                        this.getTargetPosition().y - 0.0 + rotatedBack.y,
                        this.getTargetPosition().z + rotatedBack.z
                     );
               }
               break;
            case "headpatMSG1":
               this.sendChatMessage("huh?~");
               this.playRandomSound(SoundHandler.GIRLS_LUNA_HUH);
               break;
            case "headpatMSG2":
               this.playRandomSound(SoundHandler.GIRLS_LUNA_MMM);
               break;
            case "headpatMSG3":
               this.sendChatMessage("nya~");
               this.playSound(SoundHandler.GIRLS_LUNA_HORNINYA[0]);
         }
      };
      this.movementController.transitionLengthTicks = 10.0;
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

}
