package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.JennyModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.UUID;
import net.minecraft.client.Minecraft;
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
 * <b>Role.</b> Player-form Bia — the catgirl with anal (prepare/wait/start/
 * slow/fast/cum) and prone-doggy (sitdown/sitdownidle/intro/insert/soft/hard/
 * cum) bed scenes plus headpats.
 * <p>
 * <b>Scene flow.</b> {@link #handleActionRequest(String)} starts the bed
 * scenes ({@code anal} -&gt; {@link Action#ANAL_PREPARE}, {@code doggy} -&gt;
 * {@link Action#SITDOWN}); headpats come via owner command. The core waiting
 * mechanic is {@link #handleBiaAnalState()} (ticked from
 * {@link #onUpdate()}): while in {@link Action#ANAL_WAIT}/{@link Action#SITDOWNIDLE}
 * with a player within 1 block, it runs a countdown that MUST start at
 * {@code ac = 22} (jar-verified) — a deobf regression to {@code -1} permanently
 * stalled every Bia bed scene. When the countdown expires the scene locks the
 * player in (noClip/noGravity) and either starts {@link Action#ANAL_START} or
 * positions for the prone doggy ({@link Action#PRONE_DOGGY_INTRO} + anchor).
 * <p>
 * <b>Pitfalls.</b> {@code ar} is the countdown field: {@code -1} = idle,
 * {@code 22} = first contact. {@link #setCurrentAction(Action)} forbids
 * re-entering loop phases while the cum animation plays. The countdown is
 * reset to {@code -1} in {@link #resetLocalPlayerClientState()} so a scene
 * exit re-arms it.
 */
public class BiaPlayerEntity extends AbstractPlayerGirlEntity {
   int ar = -1;
   boolean ap = false;
   int aq = 1;

   public BiaPlayerEntity(World world) {
      super(world);
   }

   public BiaPlayerEntity(World world, UUID uuid) {
      super(world, uuid);
   }

   @Override
   public float getScaleFactor() {
      return 1.5F;
   }

   public float getEyeHeight() {
      return 1.5F;
   }

   @Override
   public void handleInteraction() {
   }

   /**
    * Handles the local scene-start requests: {@code anal} and {@code doggy}
    * strip Bia and enter the bed-scene intro actions. Returns true when
    * handled so {@link AbstractPlayerGirlEntity#doAction} does not forward to
    * the server.
    */
   @Override
   public boolean handleActionRequest(String action) {
      if ("anal".equals(action)) {
         this.setCurrentAction(Action.ANAL_PREPARE);
         this.setOutfitIndex(0);
         return true;
      } else if ("doggy".equals(action)) {
         this.setCurrentAction(Action.SITDOWN);
         this.setOutfitIndex(0);
         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void H_clash570() {
      BaseGirlEntity.openInventoryGui(Minecraft.getMinecraft().player, this, new String[]{"anal", "doggy"}, false);
   }

   /**
    * SERVER: owner command {@code headpat} — teleports the acting player in
    * and plays the headpat animation.
    */
   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      if ("action.names.headpat".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.HEAD_PAT);
         this.sendActionPacket(this.getOutfitIndex(), Action.HEAD_PAT);
      }
   }

   @Override
   public IVanillaModel getHandModel(int index) {
      return new JennyModel();
   }

   @Override
   public String getHandTexture(int index) {
      return "textures/entity/bia/hand.png";
   }

   @Override
   public float getLeftArmAngle() {
      return 35.0F;
   }

   @Override
   public float getRightArmAngle() {
      return 140.0F;
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      BaseGirlEntity.openInventoryGui(player, this, new String[]{"action.names.headpat"}, false);
      return true;
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.ANAL_CUM || action != Action.ANAL_FAST && action != Action.ANAL_SLOW) {
         if (this.getCurrentAction() != Action.PRONE_DOGGY_CUM || action != Action.PRONE_DOGGY_HARD && action != Action.PRONE_DOGGY_SOFT) {
            super.setCurrentAction(action);
         }
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.ANAL_SLOW) {
         return Action.ANAL_FAST;
      } else {
         return action == Action.PRONE_DOGGY_INTRO ? Action.PRONE_DOGGY_INSERT : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.ANAL_SLOW || action == Action.ANAL_FAST) {
         return Action.ANAL_CUM;
      } else {
         return action != Action.PRONE_DOGGY_SOFT && action != Action.PRONE_DOGGY_HARD ? null : Action.PRONE_DOGGY_CUM;
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.handleBiaAnalState();
   }

   /**
    * CLIENT: on top of the inherited reset, re-arms the anal countdown to
    * {@code -1} so the next scene restarts from first contact.
    */
   @Override
   protected void resetLocalPlayerClientState() {
      super.resetLocalPlayerClientState();
      this.ar = -1;
   }

   @SideOnly(Side.CLIENT)
   public boolean isOwnPlayer(EntityPlayer player) {
      return Minecraft.getMinecraft().player.getPersistentID().equals(player.getPersistentID());
   }

   /**
    * BOTH sides, every tick: the bed-scene contact countdown (see class
    * javadoc — {@code ac} must start at 22). On first contact (countdown -1)
    * the client enables interaction and unlocks movement while the server
    * binds the interaction player; when the countdown expires the scene locks
    * the player in and starts {@link Action#ANAL_START} (anal) or the prone
    * doggy intro (sitdown). SERVER performs the action/position changes; the
    * CLIENT only mirrors the horny-meter/UI side.
    */
   void handleBiaAnalState() {
      Action action = this.getCurrentAction();
      if (action == Action.ANAL_WAIT || action == Action.SITDOWNIDLE) {
         EntityPlayer player = this.getNearestPlayer();
         if (player != null) {
            if (!(player.getDistance(this) > 1.0F)) {
               if (!this.world.isRemote || this.isOwnPlayer(player)) {
                  if (this.ar == -1) {
                     if (this.world.isRemote) {
                        BeeScreen.enableInteraction();
                        HandlePlayerMovement.setMovementLock(false);
                     } else {
                        this.setInteractionPlayerUUID(player.getPersistentID());
                     }

                     this.ar = 22;
                  } else if (--this.ar <= 0) {
                     this.ar = -1;
                     player.noClip = true;
                     player.setNoGravity(true);
                     if (action == Action.ANAL_WAIT) {
                        if (!this.world.isRemote) {
                           this.setCurrentAction(Action.ANAL_START);
                           Vec3d pos = this.getTargetPosition().add(VectorMath.rotateByYaw(-0.3, -1.0, -0.5, this.getYawRotation()));
                           player.setPositionAndUpdate(pos.x, pos.y, pos.z);
                        } else if (this.isControlledByLocalPlayer()) {
                           HornyMeterHud.showHornyMeter();
                        }
                     } else {
                        float yaw = this.getYawRotation();
                        player.rotationYaw = yaw;
                        player.rotationPitch = 60.0F;
                        if (!this.world.isRemote) {
                           this.setOutfitIndex(0);
                           this.setCurrentAction(Action.PRONE_DOGGY_INTRO);
                           Vec3d targetPos = this.getTargetPosition();
                           Vec3d followPos = targetPos.add(VectorMath.rotateByYaw(0.0, 0.0, 1.0, yaw));
                           this.setTargetPosition(followPos);
                           EntityPlayer owner = this.getOwnerPlayer();
                           if (owner != null) {
                              owner.setPositionAndUpdate(followPos.x, followPos.y, followPos.z);
                           }

                           Vec3d playerPos = targetPos.add(VectorMath.rotateByYaw(0.0, 1.1875 - player.getEyeHeight(), 0.5, yaw));
                           player.setPositionAndUpdate(playerPos.x, playerPos.y, playerPos.z);
                           this.setAnchored(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * CLIENT: while the prone-doggy hard loop plays, re-rolls the variant
    * suffix ({@code aq} in 1..3) so consecutive hard loops use different
    * animations.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void resetAnimationControllerTicks() {
      super.resetAnimationControllerTicks();
      if (this.getCurrentAction() == Action.PRONE_DOGGY_HARD) {
         int oldState = this.aq;

         do {
            this.aq = this.getRNG().nextInt(3) + 1;
         } while (oldState == this.aq);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.bia.fhappy", true, event);
            } else {
               this.createAnimation("animation.bia.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.bia.null", true, event);
            } else if (this.ak) {
               this.createAnimation("animation.bia.sit", true, event);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.createAnimation("animation.bia.fly" + (this.ap ? "2" : ""), true, event);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2);
                     this.createAnimation("animation.bia.run", true, event);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(1.2);
                     this.createAnimation("animation.bia.fastwalk", true, event);
                  } else {
                     this.movementController.setAnimationSpeed(1.2);
                     this.createAnimation("animation.bia.backwards_walk", true, event);
                  }
               } else {
                  this.createAnimation("animation.bia.idle", true, event);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.bia.null", true, event);
                  break;
               case STRIP:
                  this.createAnimation("animation.bia.strip", false, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.bia.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.bia.bowcharge", false, event);
                  break;
               case RIDE:
                  this.createAnimation("animation.bia.ride", true, event);
                  break;
               case SIT:
                  this.createAnimation("animation.bia.sit", true, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.bia.throwpearl", false, event);
                  break;
               case DOWNED:
                  this.createAnimation("animation.bia.downed", true, event);
                  break;
               case TALK_HORNY:
                  this.createAnimation("animation.bia.talk_horny", false, event);
                  break;
               case TALK_IDLE:
                  this.createAnimation("animation.bia.talk_idle", true, event);
                  break;
               case TALK_RESPONSE:
                  this.createAnimation("animation.bia.talk_response", true, event);
                  break;
               case ANAL_PREPARE:
                  this.createAnimation("animation.bia.anal_prepare", false, event);
                  break;
               case ANAL_WAIT:
                  this.createAnimation("animation.bia.anal_wait", true, event);
                  break;
               case ANAL_START:
                  this.createAnimation("animation.bia.anal_start", true, event);
                  break;
               case ANAL_SLOW:
                  this.createAnimation("animation.bia.anal_slow", true, event);
                  break;
               case ANAL_FAST:
                  this.createAnimation("animation.bia.anal_fast", true, event);
                  break;
               case ANAL_CUM:
                  this.createAnimation("animation.bia.anal_cum", false, event);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.bia.headpat", false, event);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.bia.sitdown", false, event);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.bia.sitdownidle", true, event);
                  break;
               case PRONE_DOGGY_INTRO:
                  this.createAnimation("animation.bia.prone_doggy_intro", true, event);
                  break;
               case PRONE_DOGGY_INSERT:
                  this.createAnimation("animation.bia.prone_doggy_insert", true, event);
                  break;
               case PRONE_DOGGY_SOFT:
                  this.createAnimation("animation.bia.prone_doggy_soft", true, event);
                  break;
               case PRONE_DOGGY_HARD:
                  this.createAnimation("animation.bia.prone_doggy_hard" + this.aq, true, event);
                  break;
               case PRONE_DOGGY_CUM:
                  this.createAnimation("animation.bia.prone_doggy_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers and the geckolib sound listener that
    * advances the anal/prone-doggy/headpat scenes. Key transitions:
    * {@code anal_prepareDone} -&gt; {@link Action#ANAL_WAIT},
    * {@code anal_startDone}/{@code anal_fastDone} -&gt; {@link Action#ANAL_SLOW}
    * (jump keeps the fast loop), {@code doggySwitch} -&gt; hard variant,
    * {@code anal_cumDone}/{@code doggy_cumDone} -&gt;
    * {@code resetCameraAndPhysics()}. {@code talk_responseDone} exits the talk
    * scene ({@code resetGirlState()}) and either strips or dispatches the
    * chosen scene via {@link #U()}.
    */
   @SideOnly(Side.CLIENT)
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
            case "stripMSG1":
               this.sendGirlChatMessage("Hihi~");
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_GIGGLE));
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "talk_hornyMSG1":
               this.sendChatMessage("Heyaaa~");
               this.playSound(SoundHandler.GIRLS_BIA_HEY[3]);
               break;
            case "talk_hornyMSG2":
               this.sendChatMessage("I am Hornyyyyy~");
               this.playSound(SoundHandler.GIRLS_BIA_GIGGLE[2]);
               break;
            case "talk_hornyMSG3":
               this.sendChatMessage("So...");
               this.playSound(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "talk_hornyMSG4":
               this.sendChatMessage("Are we gonna have some fun nyaa?");
               this.playSound(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "talk_responseMSG1":
               this.sendChatMessage("Huh?!...");
               this.playSound(SoundHandler.GIRLS_BIA_HUH[2]);
               break;
            case "talk_responseMSG2":
               this.sendChatMessage("I... uhm...");
               this.playSound(SoundHandler.GIRLS_BIA_BREATH[1]);
               break;
            case "talk_responseMSG3":
               this.sendChatMessage("yes~");
               this.playSound(SoundHandler.GIRLS_BIA_GIGGLE[0]);
               break;
            case "talk_responseDone":
               this.resetGirlState();
               if ((Integer)this.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) != 0) {
                  this.setCurrentAction(Action.STRIP);
               } else {
                  this.U();
               }
               break;
            case "anal_prepareMSG1":
               this.playSound(SoundHandler.MISC_PLOB[0]);
               break;
            case "anal_prepareMSG2":
               this.playSound(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "anal_prepareDone":
               this.setCurrentAction(Action.ANAL_WAIT);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "anal_startMSG1":
               this.playSound(SoundHandler.GIRLS_BIA_MMM[3]);
               this.playSound(SoundHandler.MISC_POUNDING[34]);
               break;
            case "anal_fastMSG1":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.5F);
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_slowMSG1":
            case "anal_startMSG2":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.5F);
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_fastDone":
               if (!this.isControlledByLocalPlayer() || HandlePlayerMovement.isJumping) {
                  return;
               }
            case "anal_startDone":
               this.setCurrentAction(Action.ANAL_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "anal_cumMSG2":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_cumBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "doggy_cumDone":
            case "anal_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }

               this.resetCameraAndPhysics();
               break;
            case "headpatMSG1":
               this.sendChatMessage("Ooh headpats!");
               this.playSound(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "headpatMSG2":
               this.sendChatMessage("Hmmm.... :D");
               this.playSound(SoundHandler.GIRLS_BIA_MMM[0]);
               break;
            case "headpatMSG3":
               this.sendChatMessage("huh...?");
               this.playSound(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "headpatMSG4":
               this.sendChatMessage("Tanku hehe");
               this.playSound(SoundHandler.GIRLS_BIA_GIGGLE[1]);
               break;
            case "headpatDone":
               if (this.isLocalPlayerNearby()) {
                  this.resetCameraAndPhysics();
               }
               break;
            case "sitdownMSG1":
               this.sendChatMessage("come here big boy~");
               this.playRandomSound(SoundHandler.GIRLS_BIA_BREATH);
               break;
            case "sitdownDone":
               this.setCurrentAction(Action.SITDOWNIDLE);
               break;
            case "slide":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_SLIDE));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.005);
               }
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "doggyMoan":
               this.playRandomSound(this.getRNG().nextBoolean() ? SoundHandler.GIRLS_BIA_AHH : SoundHandler.GIRLS_BIA_MMM);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "doggySwitch":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.PRONE_DOGGY_HARD);
               }
               break;
            case "doggyReset":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_INSERTS, 6.0F);
               break;
            case "orgasm1":
               this.playSound(SoundHandler.GIRLS_BIA_MMM[6]);
               break;
            case "orgasm2":
               this.playSound(SoundHandler.GIRLS_BIA_MMM[7]);
               break;
            case "openSexUI":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
         }
      };
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
      data.addAnimationController(this.actionController);
   }

}
