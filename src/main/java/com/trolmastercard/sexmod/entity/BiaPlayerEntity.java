package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.JennyModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
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

   public BiaPlayerEntity(World var1) {
      super(var1);
   }

   public BiaPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
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
   public boolean handleActionRequest(String var1) {
      if ("anal".equals(var1)) {
         this.setCurrentAction(Action.ANAL_PREPARE);
         this.setOutfitIndex(0);
         return true;
      } else if ("doggy".equals(var1)) {
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
   public void handleOwnerCommand(String var1, UUID var2) {
      if ("action.names.headpat".equals(var1)) {
         this.teleportPlayerToGirl(var2);
         this.setCurrentAction(Action.HEAD_PAT);
         this.sendActionPacket(this.getOutfitIndex(), Action.HEAD_PAT);
      }
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new JennyModel();
   }

   @Override
   public String getHandTexture(int var1) {
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
   public boolean openInteractionMenu(EntityPlayer var1) {
      BaseGirlEntity.openInventoryGui(var1, this, new String[]{"action.names.headpat"}, false);
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
   protected Action getNextAction(Action var1) {
      if (var1 == Action.ANAL_SLOW) {
         return Action.ANAL_FAST;
      } else {
         return var1 == Action.PRONE_DOGGY_INTRO ? Action.PRONE_DOGGY_INSERT : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.ANAL_SLOW || var1 == Action.ANAL_FAST) {
         return Action.ANAL_CUM;
      } else {
         return var1 != Action.PRONE_DOGGY_SOFT && var1 != Action.PRONE_DOGGY_HARD ? null : Action.PRONE_DOGGY_CUM;
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
   public boolean isOwnPlayer(EntityPlayer var1) {
      return Minecraft.getMinecraft().player.getPersistentID().equals(var1.getPersistentID());
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
      Action var1 = this.getCurrentAction();
      if (var1 == Action.ANAL_WAIT || var1 == Action.SITDOWNIDLE) {
         EntityPlayer var2 = this.getNearestPlayer();
         if (var2 != null) {
            if (!(var2.getDistance(this) > 1.0F)) {
               if (!this.world.isRemote || this.isOwnPlayer(var2)) {
                  if (this.ar == -1) {
                     if (this.world.isRemote) {
                        BeeScreen.enableInteraction();
                        HandlePlayerMovement.setMovementLock(false);
                     } else {
                        this.setInteractionPlayerUUID(var2.getPersistentID());
                     }

                     this.ar = 22;
                  } else if (--this.ar <= 0) {
                     this.ar = -1;
                     var2.noClip = true;
                     var2.setNoGravity(true);
                     if (var1 == Action.ANAL_WAIT) {
                        if (!this.world.isRemote) {
                           this.setCurrentAction(Action.ANAL_START);
                           Vec3d var8 = this.getTargetPosition().add(VectorMath.rotateByYaw(-0.3, -1.0, -0.5, this.getYawRotation()));
                           var2.setPositionAndUpdate(var8.x, var8.y, var8.z);
                        } else if (this.isControlledByLocalPlayer()) {
                           HornyMeterHud.showHornyMeter();
                        }
                     } else {
                        float var3 = this.getYawRotation();
                        var2.rotationYaw = var3;
                        var2.rotationPitch = 60.0F;
                        if (!this.world.isRemote) {
                           this.setOutfitIndex(0);
                           this.setCurrentAction(Action.PRONE_DOGGY_INTRO);
                           Vec3d var4 = this.getTargetPosition();
                           Vec3d var5 = var4.add(VectorMath.rotateByYaw(0.0, 0.0, 1.0, var3));
                           this.setTargetPosition(var5);
                           EntityPlayer var6 = this.getOwnerPlayer();
                           if (var6 != null) {
                              var6.setPositionAndUpdate(var5.x, var5.y, var5.z);
                           }

                           Vec3d var7 = var4.add(VectorMath.rotateByYaw(0.0, 1.1875 - var2.getEyeHeight(), 0.5, var3));
                           var2.setPositionAndUpdate(var7.x, var7.y, var7.z);
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
         int var1 = this.aq;

         do {
            this.aq = this.getRNG().nextInt(3) + 1;
         } while (var1 == this.aq);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.bia.fhappy", true, var1);
            } else {
               this.createAnimation("animation.bia.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.bia.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.bia.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.createAnimation("animation.bia.fly" + (this.ap ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2);
                     this.createAnimation("animation.bia.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(1.2);
                     this.createAnimation("animation.bia.fastwalk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(1.2);
                     this.createAnimation("animation.bia.backwards_walk", true, var1);
                  }
               } else {
                  this.createAnimation("animation.bia.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.bia.null", true, var1);
                  break;
               case STRIP:
                  this.createAnimation("animation.bia.strip", false, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.bia.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.bia.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.createAnimation("animation.bia.ride", true, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.bia.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.bia.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.createAnimation("animation.bia.downed", true, var1);
                  break;
               case TALK_HORNY:
                  this.createAnimation("animation.bia.talk_horny", false, var1);
                  break;
               case TALK_IDLE:
                  this.createAnimation("animation.bia.talk_idle", true, var1);
                  break;
               case TALK_RESPONSE:
                  this.createAnimation("animation.bia.talk_response", true, var1);
                  break;
               case ANAL_PREPARE:
                  this.createAnimation("animation.bia.anal_prepare", false, var1);
                  break;
               case ANAL_WAIT:
                  this.createAnimation("animation.bia.anal_wait", true, var1);
                  break;
               case ANAL_START:
                  this.createAnimation("animation.bia.anal_start", true, var1);
                  break;
               case ANAL_SLOW:
                  this.createAnimation("animation.bia.anal_slow", true, var1);
                  break;
               case ANAL_FAST:
                  this.createAnimation("animation.bia.anal_fast", true, var1);
                  break;
               case ANAL_CUM:
                  this.createAnimation("animation.bia.anal_cum", false, var1);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.bia.headpat", false, var1);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.bia.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.bia.sitdownidle", true, var1);
                  break;
               case PRONE_DOGGY_INTRO:
                  this.createAnimation("animation.bia.prone_doggy_intro", true, var1);
                  break;
               case PRONE_DOGGY_INSERT:
                  this.createAnimation("animation.bia.prone_doggy_insert", true, var1);
                  break;
               case PRONE_DOGGY_SOFT:
                  this.createAnimation("animation.bia.prone_doggy_soft", true, var1);
                  break;
               case PRONE_DOGGY_HARD:
                  this.createAnimation("animation.bia.prone_doggy_hard" + this.aq, true, var1);
                  break;
               case PRONE_DOGGY_CUM:
                  this.createAnimation("animation.bia.prone_doggy_cum", true, var1);
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
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
      var1.addAnimationController(this.actionController);
   }

}
