package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GalathFlightHud;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.CatModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.entity.api.IGalath;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.networking.UpdateVelocityPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.DynamicTrailRenderer;
import com.trolmastercard.sexmod.util.Vector4d;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> Player-form Galath — the transformation with the rape
 * (intro/ongoing/cum) and corrupt (slow/fast/cum) scenes. The owner gets
 * flight while transformed ({@code B_clash233}/{@code onTickClient} pair).
 * <p>
 * <b>Scene flow.</b> Owner commands {@code cowgirl}/{@code mating press}
 * start the scenes via {@link #handleOwnerCommand(String, UUID)};
 * {@code handleCumState()} keeps the wings animated during the scenes
 * ({@code ap} flag), {@code handlePlayerAction()} hides the horny meter during
 * the rape intro. Rape variant cycling ({@code ar}) and scene end
 * ({@code reset} -&gt; {@code resetCameraAndPhysics()}) run in the sound
 * listener.
 */
public class GalathPlayerEntity extends AbstractPlayerGirlEntity implements IGalath {
   boolean ap = false;
   int ar = 0;
   boolean as = false;
   boolean aq = false;

   public GalathPlayerEntity(World world) {
      super(world);
   }

   public GalathPlayerEntity(World world, UUID uuid) {
      super(world, uuid);
   }

   @Override
   public IVanillaModel getHandModel(int index) {
      return new CatModel();
   }

   @Override
   public String getHandTexture(int index) {
      return "textures/entity/galath/hand.png";
   }

   @Nullable
   @Override
   protected Action getNextAction(Action action) {
      return null;
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.CORRUPT_FAST || action == Action.CORRUPT_SLOW) {
         return Action.CORRUPT_CUM;
      } else {
         return action == Action.RAPE_ON_GOING ? Action.RAPE_CUM : null;
      }
   }

   @Override
   public float getScaleFactor() {
      return 2.3F;
   }

   /**
    * SERVER: owner commands — {@code cowgirl} starts {@link Action#RAPE_INTRO},
    * {@code mating press} starts {@link Action#CORRUPT_SLOW} and repositions
    * the player via {@link #handleGalathPlayerOwner()}. Both broadcast and
    * teleport the acting player into the scene.
    */
   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      if ("cowgirl".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.RAPE_INTRO);
         this.sendActionPacket(this.getOutfitIndex(), Action.RAPE_INTRO);
      } else if ("mating press".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.CORRUPT_SLOW);
         this.sendActionPacket(this.getOutfitIndex(), Action.CORRUPT_SLOW);
         this.handleGalathPlayerOwner();
      }
   }

   /**
    * Guards the state machine: refuses re-entry into loop phases while the
    * corrupt/rape cum animations play and resets the hard-variant flag
    * ({@code as}) when entering {@link Action#CORRUPT_SLOW}.
    */
   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.CORRUPT_CUM || action != Action.CORRUPT_FAST && action != Action.CORRUPT_SLOW) {
         if (currentAction != Action.RAPE_CUM || action != Action.RAPE_ON_GOING) {
            if (currentAction != Action.RAPE_CUM || action != Action.RAPE_CUM_IDLE) {
               if (action == Action.CORRUPT_SLOW) {
                  this.as = false;
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   void handleGalathPlayerOwner() {
      EntityPlayer player = this.getPlayerEntity();
      if (player != null) {
         Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - player.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
         player.setPositionAndUpdate(pos.x, pos.y, pos.z);
      }
   }

   @Override
   public boolean isHuggingManglelie() {
      return false;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      openInventoryGui(player, this, new String[]{"cowgirl", "mating press", "ride"}, false);
      return true;
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
   public Vector4d getFlightData() {
      return new Vector4d(0.0, 0.0, 0.0, 0.0);
   }

   @Override
   public boolean isWingsAnimated() {
      return this.getOutfitIndex() == 0 || this.ap;
   }

   @Override
   public boolean areWingsAnimated() {
      switch (this.getCurrentAction()) {
         case CORRUPT_CUM:
         case CORRUPT_FAST:
         case CORRUPT_SLOW:
         case COWGIRLCUM:
            return false;
         default:
            return true;
      }
   }

   @Override
   public void B_clash233() {
      this.handleOwnerUUID(true);
   }

   /**
    * BOTH sides: keeps the wing-animation flag ({@code ap}) set while any
    * rape/corrupt action plays, and CLIENT-side hides the horny meter during
    * the rape intro.
    */
   @Override
   public void onUpdate() {
      super.onUpdate();
      this.handleCumState();
      if (this.world.isRemote) {
         this.handlePlayerAction();
      }
   }

   @SideOnly(Side.CLIENT)
   void handlePlayerAction() {
      if (this.isControlledByLocalPlayer()) {
         if (this.getCurrentAction() == Action.RAPE_INTRO) {
            HornyMeterHud.setHornyMeterVisible(false);
         }
      }
   }

   void handleCumState() {
      switch (this.getCurrentAction()) {
         case CORRUPT_CUM:
         case CORRUPT_FAST:
         case CORRUPT_SLOW:
         case RAPE_INTRO:
         case RAPE_ON_GOING:
         case RAPE_CUM:
         case RAPE_CHARGE:
         case RAPE_CUM_IDLE:
            this.ap = true;
            return;
         case COWGIRLCUM:
         default:
            this.ap = false;
      }
   }

   boolean hasNoGalathOwner() {
      EntityPlayer player = this.getOwnerPlayer();
      return player == null
         ? false
         : this.world.getBlockState(player.getPosition().up().up()).getBlock() != Blocks.AIR;
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.galath.blink", true, event);
            } else {
               this.createAnimation("animation.galath.null", true, event);
            }
            break;
         case "movement":
            this.movementController.setAnimationSpeed(1.0);
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.galath.null", true, event);
            } else if (this.ak) {
               this.createAnimation("animation.galath.sit", true, event);
            } else if (!this.af) {
               this.createAnimation("animation.galath.controlled_flight", true, event);
            } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) == 0.0F) {
               this.createAnimation(this.hasNoGalathOwner() ? "animation.galath.crouchidle" : "animation.galath.idle", true, event);
            } else if (this.aj) {
               this.movementController.setAnimationSpeed(1.5);
               this.createAnimation(this.hasNoGalathOwner() ? "animation.galath.crouchwalk" : "animation.galath.run", true, event);
            } else if (this.ao.y >= -0.1F) {
               this.movementController.setAnimationSpeed(2.0);
               this.createAnimation(this.hasNoGalathOwner() ? "animation.galath.crouchwalk" : "animation.galath.walk", true, event);
            } else {
               this.movementController.setAnimationSpeed(1.5);
               this.createAnimation(this.hasNoGalathOwner() ? "animation.galath.crouchwalk" : "animation.galath.backwards_walk", true, event);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case CORRUPT_CUM:
                  this.createAnimation("animation.galath.corrupt_cum", true, event);
                  break;
               case CORRUPT_FAST:
                  this.createAnimation("animation.galath.corrupt_" + (this.as ? "hard" : "soft"), true, event);
                  break;
               case CORRUPT_SLOW:
                  this.createAnimation("animation.galath.corrupt_slow", true, event);
               case COWGIRLCUM:
               case RAPE_CHARGE:
               default:
                  break;
               case RAPE_INTRO:
                  this.createAnimation("animation.galath.rape_intro", true, event);
                  break;
               case RAPE_ON_GOING:
                  this.createAnimation("animation.galath.rape" + this.ar, true, event);
                  break;
               case RAPE_CUM:
                  this.createAnimation("animation.galath.rape_cum", true, event);
                  break;
               case RAPE_CUM_IDLE:
                  this.createAnimation("animation.galath.rape_cum_idle", true, event);
                  break;
               case NULL:
                  return PlayState.STOP;
               case STRIP:
                  this.createAnimation("animation.galath.strip", true, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.galath.attack" + this.nextAttack, true, event);
                  break;
               case BOW:
                  this.createAnimation("animation.galath.bowcharge", true, event);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.galath.sit", true, event);
                  break;
               case CORRUPT_INTRO:
                  this.createAnimation("animation.galath.corrupt_intro", true, event);
                  break;
               case CONTROLLED_FLIGHT:
                  this.createAnimation("animation.galath.controlled_flight", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * rape/corrupt scenes — {@code rapeIntroDone} -&gt;
    * {@link Action#RAPE_ON_GOING}, {@code rape_switch} re-rolls the variant
    * ({@code ar}), jump toggles corrupt hard, {@code reset} -&gt;
    * {@code resetCameraAndPhysics()}, and the creampie trails spawn on
    * {@code creampie}.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      this.initAnimationControllers();
      this.actionController
         .registerSoundListener(
            sound -> {
               switch (sound.sound) {
                  case "attackDone":
                     if (++this.nextAttack == 3) {
                        this.nextAttack = 0;
                     }
                     break;
                  case "cum":
                     this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 2.0F);
                     break;
                  case "pound":
                     this.playRandomSound(SoundHandler.MISC_POUNDING);
                     break;
                  case "flap":
                     this.playRandomSound(SoundHandler.MISC_FLAP);
                     break;
                  case "setNude":
                     this.ap = true;
                     Vec3d pos = this.getPositionVector();
                     Vec3d slipRPos = this.getCachedBoneOffset("slipR").add(pos);
                     Vec3d slipLPos = this.getCachedBoneOffset("slipL").add(pos);
                     Vec3d turnablePos = this.getCachedBoneOffset("turnable").add(pos);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, slipRPos.x, slipRPos.y, slipRPos.z, 0.0, 0.0, 0.0, new int[0]);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, slipLPos.x, slipLPos.y, slipLPos.z, 0.0, 0.0, 0.0, new int[0]);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, turnablePos.x, turnablePos.y, turnablePos.z, 0.0, 0.0, 0.0, new int[0]);
                     break;
                  case "rapeIntroDone":
                     if (this.isControlledByLocalPlayer()) {
                        this.setCurrentAction(Action.RAPE_ON_GOING);
                     }
                     break;
                  case "rape_switch":
                     Random random = this.getRNG();
                     int oldState = this.ar;

                     do {
                        this.ar = random.nextInt(3);
                     } while (this.ar == oldState);

                     return;
                  case "poundRape":
                     this.playRandomSound(SoundHandler.MISC_POUNDING);
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.addToHornyMeter(0.03F);
                     }
                     break;
                  case "enableRapeUI":
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.setHornyMeterVisible(false);
                     }
                     break;
                  case "reloadRenderer":
                     if (!this.isControlledByLocalPlayer()) {
                        return;
                     }

                     Minecraft flapMc = Minecraft.getMinecraft();
                     if (flapMc.gameSettings.thirdPersonView != 0) {
                        flapMc.renderGlobal.loadRenderers();
                     }
                     break;
                  case "corruptSwitch":
                     if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                        this.setCurrentAction(Action.CORRUPT_FAST);
                     }
                     break;
                  case "corrupt_hard":
                     if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                        this.as = true;
                        this.resetAnimationControllerOffset();
                     }
                     break;
                  case "corrupt_hard_end":
                     this.setCurrentAction(Action.CORRUPT_SLOW);
                     this.as = false;
                     break;
                  case "addCum":
                     HornyMeterHud.addToHornyMeter(0.03);
                     break;
                  case "clearcum":
                     CummyEntity.spawnCummyTrails(this);
                  case "reset":
                     if (this.isControlledByLocalPlayer()) {
                        this.resetCameraAndPhysics();
                     }
                     break;
                  case "setCamCorrupt":
                     if (!this.isControlledByLocalPlayer()) {
                        return;
                     }

                     this.aq = true;
                     EntityPlayerSP corruptPlayer = Minecraft.getMinecraft().player;
                     float corruptYaw = this.getYawRotation() + 220.0F;
                     Vec3d corruptPos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - corruptPlayer.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
                     PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(corruptPlayer.getPersistentID().toString(), corruptPos, corruptYaw, 15.0F));
                     HornyMeterHud.showHornyMeter();
                     break;
                  case "enableBoyCam":
                     if (this.isControlledByLocalPlayer()) {
                        this.aq = false;
                     }
                     break;
                  case "creampie":
                     CummyEntity.registerTrail(new DynamicTrailRenderer(130, girl -> {
                        Vec3d cockTipPos = girl.getBoneWorldPosition("futaCockTip");
                        Vec3d tipDir = girl.getBoneWorldPosition("futaCockTipDirHelp");
                        return cockTipPos.subtract(tipDir).normalize();
                     }, girl -> girl.getCachedBoneOffset("futaCockTip").add(girl.getTargetPosition()), this, 0.3F, 0.3F));
                     CummyEntity.registerTrail(
                        new DynamicTrailRenderer(
                           100,
                           girl -> VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.6F), this.getYawRotation()),
                           girl -> girl.getCachedBoneOffset("creampiePos").add(girl.getTargetPosition()),
                           this,
                           0.6F,
                           0.5F
                        )
                     );
                     this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 3.0F);
                     break;
                  case "blackScreenTamed":
                  case "blackScreen":
                     if (this.isControlledByLocalPlayer()) {
                        BeeScreen.enableInteraction();
                     }
                     break;
                  case "flapControlled":
                     if (this.isControlledByLocalPlayer()) {
                        GalathFlightHud.showHud();
                        this.playRandomSound(SoundHandler.MISC_FLAP);
                        Minecraft mc = Minecraft.getMinecraft();
                        EntityPlayerSP player = mc.player;
                        MovementInput input = player.movementInput;
                        Vec2f moveVec = input.getMoveVector();
                        if (moveVec.x != 0.0F || moveVec.y != 0.0F) {
                           Vec3d vel = VectorMath.rotateByYawPitch(
                              new Vec3d(-moveVec.x, 0.0, moveVec.y),
                              RotationHelper.lerp(player.prevRotationPitch, player.rotationPitch, mc.getRenderPartialTicks()),
                              RotationHelper.lerp(player.prevRotationYawHead, player.rotationYawHead, mc.getRenderPartialTicks())
                           );
                           PacketHandler.networkWrapper.sendToServer(new UpdateVelocityPacket(vel, this.getGirlId()));
                        }
                     }
                     break;
                  case "clap":
                     this.playRandomSound(SoundHandler.MISC_CLAP);
                     break;
                  case "energysound":
                     this.playSound(SoundHandler.MISC_BEEW[1]);
                     break;
                  case "energy2":
                     this.playSound(SoundHandler.MISC_BEEW[2]);
                     break;
                  case "tpSound":
                     this.playSound(SoundHandler.MISC_WEOWEO[2]);
                     break;
                  case "sexui":
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.showHornyMeter();
                     }
               }
            }
         );
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.eyesController);
      data.addAnimationController(this.movementController);
   }

}
