package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.SlimeModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetPlayerForGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.EyeColor;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> Player-form Jenny — the horny-potion transformation of the Jenny
 * NPC. Implements her scene set: blowjob (suck/thrust/cum), doggy on a bed
 * (start/wait/slow/fast/cum) and paizuri (start/slow/fast/cum).
 * <p>
 * <b>Scene flow.</b> Owner-command actions ({@code blowjob}/{@code boobjob})
 * teleport the players in via {@link AbstractPlayerGirlEntity#teleportPlayerToGirl}
 * and start the intro action; the geckolib {@code ISoundListener} in
 * {@link #registerControllers(AnimationData)} advances each phase on the
 * animation sound keyframes (e.g. {@code bjiDone} -&gt; {@link Action#SUCKBLOWJOB},
 * {@code doggyGoOnBedDone} -&gt; {@link Action#WAITDOGGY}). The doggy bed phase
 * waits in {@link #updateAITasks()} for the player to come within 1 block, then
 * starts {@link Action#DOGGYSTART}. Cum ends via the
 * {@code bjcDone}/{@code paizuri_cumDone}/{@code doggyCumDone} sounds which call
 * {@code resetCameraAndPhysics()} (single-arg {@link ResetGirlPacket} path).
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} forbids re-entering a
 * loop phase while the cum animation plays (doggy/cum and similar) — keep the
 * guards. {@code doggyfastReady} resets the animation offset and sets the
 * "hard" flag while the player jumps; the {@code ar}/{@code as} flags gate the
 * hard variant and the one-shot paizuri camera reposition.
 */
public class JennyPlayerEntity extends AbstractPlayerGirlEntity {
   boolean ap = false;
   boolean ar = false;
   int aq = 0;
   boolean as = false;

   protected JennyPlayerEntity(World var1) {
      super(var1);
   }

   public JennyPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public float getScaleFactor() {
      return 1.75F;
   }

   @Override
   public float getLeftArmAngle() {
      return 35.0F;
   }

   @Override
   public float getRightArmAngle() {
      return 140.0F;
   }

   public float getEyeHeight() {
      return 1.64F;
   }

   /**
    * CLIENT/SERVER: starts the doggy bed scene — strips the outfit, sets the
    * camera yaw from the anchor and enters {@link Action#STARTDOGGY}.
    */
   @Override
   public void handleInteraction() {
      this.setCurrentAction(Action.STARTDOGGY);
      this.entityDataManager.set(BaseGirlEntity.OUTFIT_INDEX, 0);
      this.cameraYaw = (Float)this.entityDataManager.get(BaseGirlEntity.YAW_ROTATION);
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new SlimeModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return var1 == 0 ? "textures/entity/jenny/hand_nude.png" : "textures/entity/jenny/hand.png";
   }

   /**
    * SERVER: owner-command entry point. {@code boobjob} strips and starts
    * {@link Action#PAIZURI_START}; {@code blowjob} starts {@link Action#STARTBLOWJOB}.
    * Both broadcast the action to tracking players and teleport the acting
    * player into the scene.
    */
   @Override
   public void handleOwnerCommand(String var1, UUID var2) {
      if ("action.names.boobjob".equals(var1)) {
         this.entityDataManager.set(BaseGirlEntity.OUTFIT_INDEX, 0);
         this.setCurrentAction(Action.PAIZURI_START);
         this.sendActionPacket(0, Action.PAIZURI_START);
         this.teleportPlayerToGirl(var2);
      }

      if ("action.names.blowjob".equals(var1)) {
         this.setCurrentAction(Action.STARTBLOWJOB);
         this.sendActionPacket(this.getOutfitIndex(), Action.PAIZURI_START);
         this.teleportPlayerToGirl(var2);
      }
   }

   /**
    * SERVER (and CLIENT mirror): the doggy bed phase. While in
    * {@link Action#WAITDOGGY}, waits for the nearest non-owner player within
    * 1 block, locks them into the scene (movement lock, flying, noClip),
    * positions the camera and advances to {@link Action#DOGGYSTART}.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.getCurrentAction() == Action.WAITDOGGY) {
         EntityPlayer var1 = this.getNearestPlayer();
         if (var1 != null && var1.getDistance(this.getPositionVec3d().x, this.getPositionVec3d().y, this.getPositionVec3d().z) < 1.0) {
            if (this.isOwnerUUID(var1.getPersistentID())) {
               var1.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "sowy no lesbo action yet uwu"));
               return;
            }

            this.setInteractionPlayerUUID(var1.getPersistentID());
            var1.setPositionAndUpdate(this.getPositionVector().x, this.getPositionVec3d().y, this.getPositionVector().z);
            this.alignPlayerToGirl((EntityPlayerMP)var1, false);
            var1.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
            var1.capabilities.isFlying = true;
            this.world.getPlayerEntityByUUID(this.getOwnerUserUUID()).capabilities.isFlying = true;
            this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);
            this.cameraOriginPos = null;
            this.setCurrentAction(Action.DOGGYSTART);
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
         }
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      BaseGirlEntity.openInventoryGui(var1, this, new String[]{"action.names.blowjob", "action.names.boobjob"}, false);
      return true;
   }

   /**
    * CLIENT: returns the fast variant of the current loop action
    * ({@code SUCKBLOWJOB} -&gt; {@code THRUSTBLOWJOB}, {@code DOGGYSLOW} -&gt;
    * {@code DOGGYFAST}, {@code PAIZURI_SLOW} -&gt; {@code PAIZURI_FAST}).
    * The paizuri transition repositions the camera once (guarded by
    * {@code as}).
    */
   @Override
   protected Action getNextAction(Action var1) {
      switch (var1) {
         case SUCKBLOWJOB:
            return Action.THRUSTBLOWJOB;
         case DOGGYSLOW:
            return Action.DOGGYFAST;
         case PAIZURI_SLOW:
            if (this.as) {
               this.as = false;
               this.positionPlayerRelative(0.0, 0.0, 0.0, 0.0F, 70.0F);
            }

            return Action.PAIZURI_FAST;
         default:
            return null;
      }
   }

   /**
    * CLIENT: maps the current loop action to its cum action and repositions
    * the camera for the blowjob cum (pitch 70).
    */
   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.SUCKBLOWJOB || var1 == Action.THRUSTBLOWJOB) {
         this.positionPlayerRelative(0.0, 0.0, 0.0, 0.0F, 70.0F);
         return Action.CUMBLOWJOB;
      } else if (var1 == Action.DOGGYSLOW || var1 == Action.DOGGYFAST) {
         return Action.DOGGYCUM;
      } else {
         return var1 != Action.PAIZURI_FAST && var1 != Action.PAIZURI_SLOW ? null : Action.PAIZURI_CUM;
      }
   }

   /**
    * Guards the action state machine against re-entering a loop phase while
    * the corresponding cum animation is still playing (doggy/blowjob/paizuri).
    */
   @Override
   public void setCurrentAction(Action action) {
      Action var2 = this.getCurrentAction();
      if (var2 != Action.DOGGYCUM || action != Action.DOGGYSLOW && action != Action.DOGGYFAST) {
         if (var2 != Action.CUMBLOWJOB || action != Action.THRUSTBLOWJOB && action != Action.SUCKBLOWJOB) {
            if (var2 != Action.PAIZURI_CUM || action != Action.PAIZURI_SLOW && action != Action.PAIZURI_FAST) {
               super.setCurrentAction(action);
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.jenny.fhappy", true, var1);
            } else {
               this.createAnimation("animation.jenny.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.jenny.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.jenny.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.ap = !this.ap;
               }

               if (!this.af) {
                  this.createAnimation("animation.jenny.fly" + (this.ap ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.createAnimation("animation.jenny.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(1.5);
                     this.createAnimation("animation.jenny.fastwalk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.createAnimation("animation.jenny.backwards_walk", true, var1);
                  }
               } else {
                  this.createAnimation("animation.jenny.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case SUCKBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobsuck", true, var1);
                  break;
               case DOGGYSLOW:
                  this.createAnimation("animation.jenny.doggyslow", true, var1);
                  break;
               case PAIZURI_SLOW:
                  this.createAnimation("animation.jenny.paizuri_slow", true, var1);
                  break;
               case NULL:
                  this.createAnimation("animation.jenny.null", true, var1);
                  break;
               case STRIP:
                  this.createAnimation("animation.jenny.strip", false, var1);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.jenny.payment", false, var1);
                  break;
               case STARTBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobintro", false, var1);
                  break;
               case THRUSTBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobthrust", true, var1);
                  break;
               case CUMBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobcum", false, var1);
                  break;
               case STARTDOGGY:
                  this.createAnimation("animation.jenny.doggygoonbed", false, var1);
                  break;
               case WAITDOGGY:
                  this.createAnimation("animation.jenny.doggywait", true, var1);
                  break;
               case DOGGYSTART:
                  this.createAnimation("animation.jenny.doggystart", false, var1);
                  break;
               case DOGGYFAST:
                  this.createAnimation("animation.jenny.doggyfast_" + (this.ar ? "hard" : "soft"), true, var1);
                  break;
               case DOGGYCUM:
                  this.createAnimation("animation.jenny.doggycum", false, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.jenny.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.jenny.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.createAnimation("animation.jenny.ride", true, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.jenny.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.jenny.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.createAnimation("animation.jenny.downed", true, var1);
                  break;
               case PAIZURI_START:
                  this.createAnimation("animation.jenny.paizuri_start", false, var1);
                  break;
               case PAIZURI_FAST:
                  this.createAnimation("animation.jenny.paizuri_fast", true, var1);
                  break;
               case PAIZURI_CUM:
                  this.createAnimation("animation.jenny.paizuri_cum", false, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the action/movement/eyes controllers and the
    * geckolib {@link AnimationController.ISoundListener} that drives the whole
    * scene — every phase transition, dialogue line, sound and the horny-meter
    * updates fire on animation sound keyframes here. Scene end: the
    * {@code bjcDone}/{@code paizuri_cumDone}/{@code doggyCumDone} keyframes
    * call {@code resetCameraAndPhysics()}; {@code paymentDone} dispatches the
    * selected scene via {@link #U()}. Ordering matters: the listener must be
    * registered before the controllers are added.
    */
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
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_GIGGLE));
               break;
            case "paymentMSG1":
               this.sendGirlChatMessage("Huh?");
               this.playSound(SoundHandler.GIRLS_JENNY_HUH[1]);
               break;
            case "paymentMSG2":
               this.playSoundAtVolume(SoundHandler.MISC_PLOB[0], 0.5F);
               String var4 = "<" + Minecraft.getMinecraft().player.getName() + "> ";
               switch ((String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES)) {
                  case "strip":
                     this.broadcastChatAround(var4 + "show Bobs and vegana pls", true);
                     return;
                  case "blowjob":
                     this.broadcastChatAround(var4 + "Give me the sucky sucky and these are yours", true);
                     return;
                  case "doggy":
                     this.broadcastChatAround(var4 + "Give me the sex pls :)", true);
                     return;
                  case "boobjob":
                     this.broadcastChatAround(var4 + "gib boba OwO", true);
                     return;
                  default:
                     this.broadcastChatAround(var4 + "sex pls", true);
                     return;
               }
            case "paymentMSG3":
               this.sendGirlChatMessage("Hehe~");
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_GIGGLE));
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paymentMSG4":
               this.playSoundAtVolume(SoundHandler.MISC_PLOB[0], 0.25F);
               break;
            case "paymentDone":
               this.U();
               break;
            case "bjiMSG1":
               this.sendGirlChatMessage("What are you...");
               this.playSound(SoundHandler.GIRLS_JENNY_MMM[8]);
               this.cameraYaw = 180.0F;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG2":
               this.sendGirlChatMessage("eh... boys...");
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG3":
               this.sendGirlChatMessage("OHOhh...!");
               this.playSound(SoundHandler.GIRLS_JENNY_AFTERSESSIONMOAN[0]);
               break;
            case "bjiMSG4":
               this.playSound(SoundHandler.MISC_BELLJINGLE[0]);
               break;
            case "bjiMSG5":
               this.sendGirlChatMessage("Was this really necessary?!");
               this.playSoundAtVolume(SoundHandler.GIRLS_JENNY_HMPH[1], 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG6":
               this.sendGirlChatMessage("Oh~");
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG7":
               this.sendGirlChatMessage("You like it?~");
               this.playSound(SoundHandler.GIRLS_JENNY_GIGGLE[4]);
               break;
            case "bjiMSG8":
               this.broadcastChatAround("<" + Minecraft.getMinecraft().player.getName() + "> Yee", true);
               this.playSoundAtVolume(SoundHandler.MISC_PLOB[0], 0.5F);
               break;
            case "bjiMSG9":
               this.sendGirlChatMessage("Hihihi~");
               this.playSound(SoundHandler.GIRLS_JENNY_GIGGLE[2]);
               break;
            case "bjiMSG10":
               if (this.isControlledByLocalPlayer()) {
                  this.positionPlayerRelative(-0.4, -0.8, -0.2, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjiMSG12":
               if (Reference.RANDOM.nextInt(5) == 0) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_BJMOAN));
               }

               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjtMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
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
                  this.ar = true;
               }
               break;
            case "bjtReady":
            case "paizuriReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "bjcMSG1":
               this.playSound(SoundHandler.GIRLS_JENNY_BJMOAN[1]);
               break;
            case "bjcMSG2":
               this.playSound(SoundHandler.GIRLS_JENNY_BJMOAN[7]);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "bjcMSG3":
               this.playSound(SoundHandler.GIRLS_JENNY_AFTERSESSIONMOAN[1]);
               break;
            case "bjcMSG4":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[0]);
               break;
            case "bjcMSG5":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[1]);
               break;
            case "bjcMSG6":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[2]);
               break;
            case "bjcMSG7":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[3]);
               break;
            case "bjcBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "bjcDone":
            case "paizuri_cumDone":
            case "doggyCumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "doggyGoOnBedMSG1":
               this.playSound(SoundHandler.MISC_BEDRUSTLE[0]);
               this.cameraYaw = this.rotationYaw;
               break;
            case "doggyGoOnBedMSG2":
               this.sendChatMessage("what are you waiting for?~");
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[9]);
               break;
            case "doggyGoOnBedMSG3":
               this.sendChatMessage("this ass ain't gonna fuck itself...");
               this.playSound(SoundHandler.GIRLS_JENNY_GIGGLE[0]);
               break;
            case "doggyGoOnBedMSG4":
               this.playSoundAtVolume(SoundHandler.MISC_SLAP[0], 0.75F);
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
               this.playSoundAtVolume(SoundHandler.MISC_BEDRUSTLE[1], 0.5F);
               break;
            case "doggystartMSG4":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS));
               this.playSound(SoundHandler.GIRLS_JENNY_MMM[1]);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "doggystartMSG5":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
               break;
            case "doggystartDone":
               this.setCurrentAction(Action.DOGGYSLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doggyslowMSG1":
               this.ar = false;
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               int var5 = Reference.RANDOM.nextInt(4);
               if (var5 == 0) {
                  var5 = Reference.RANDOM.nextInt(2);
                  if (var5 == 0) {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
                  } else {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
                  }
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.00666);
               }
               break;
            case "doggyslowMSG2":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING), 0.5F);
               break;
            case "doggyfastMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.aq++;
               if (this.aq % 2 == 0) {
                  int var9 = Reference.RANDOM.nextInt(2);
                  if (var9 == 0) {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
                  } else {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING));
                  }
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_AHH));
               }
               break;
            case "doggyfastDone":
               this.ar = false;
               this.setCurrentAction(Action.DOGGYSLOW);
               break;
            case "doggycumMSG1":
               this.playSoundAtVolume(SoundHandler.MISC_CUMINFLATION[0], 2.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 2.0F);
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
               break;
            case "doggycumMSG2":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[4]);
               break;
            case "doggycumMSG3":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[5]);
               break;
            case "doggycumMSG4":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[6]);
               break;
            case "doggycumMSG5":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[7]);
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "boobjob_camera":
               if (this.isControlledByLocalPlayer() && !this.as) {
                  this.as = true;
                  this.cameraYaw = 180.0F;
                  this.positionPlayerRelative(-0.7, -0.6, -0.2, 60.0F, -3.0F);
               }
               break;
            case "paizuri_startDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.PAIZURI_SLOW);
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.getRNG().nextBoolean()) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_AHH));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "paizuriSlowMSG1":
            case "paizuriStartMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "paizuri_fastDone":
               this.setCurrentAction(Action.PAIZURI_SLOW);
               if (this.isControlledByLocalPlayer() && !this.as) {
                  this.as = true;
                  this.positionPlayerRelative(-0.7, -0.6, -0.2, 60.0F, -3.0F);
               }
               break;
            case "paizuri_startStep":
               IBlockState var6 = this.world.getBlockState(this.getPosition().subtract(new Vec3i(0, 1, 0)));
               this.playSound(var6.getBlock().getSoundType(var6, this.world, this.getPosition(), this).getStepSound());
               break;
            case "paizuri_cumStart":
               if (this.isControlledByLocalPlayer() && !this.as) {
                  this.positionPlayerRelative(-0.7, -0.6, -0.2, 60.0F, -3.0F);
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
