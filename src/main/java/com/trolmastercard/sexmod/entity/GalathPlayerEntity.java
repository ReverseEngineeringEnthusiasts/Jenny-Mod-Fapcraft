package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IPositionProvider;
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

public class GalathPlayerEntity extends AbstractPlayerGirlEntity implements IGalath {
   boolean ap = false;
   int ar = 0;
   boolean as = false;
   boolean aq = false;

   public GalathPlayerEntity(World var1) {
      super(var1);
   }

   public GalathPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new CatModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/galath/hand.png";
   }

   @Nullable
   @Override
   protected Action getNextAction(Action var1) {
      return null;
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.CORRUPT_FAST || var1 == Action.CORRUPT_SLOW) {
         return Action.CORRUPT_CUM;
      } else {
         return var1 == Action.RAPE_ON_GOING ? Action.RAPE_CUM : null;
      }
   }

   @Override
   public float i_clash226() {
      return 2.3F;
   }

   @Override
   public void b(String var1, UUID var2) {
      if ("cowgirl".equals(var1)) {
         this.b_clash577(var2);
         this.setCurrentAction(Action.RAPE_INTRO);
         this.a(this.getOutfitIndex(), Action.RAPE_INTRO);
      } else if ("mating press".equals(var1)) {
         this.b_clash577(var2);
         this.setCurrentAction(Action.CORRUPT_SLOW);
         this.a(this.getOutfitIndex(), Action.CORRUPT_SLOW);
         this.a_clash442();
      }
   }

   @Override
   public void setCurrentAction(Action action) {
      Action var2 = this.getCurrentAction();
      if (var2 != Action.CORRUPT_CUM || action != Action.CORRUPT_FAST && action != Action.CORRUPT_SLOW) {
         if (var2 != Action.RAPE_CUM || action != Action.RAPE_ON_GOING) {
            if (var2 != Action.RAPE_CUM || action != Action.RAPE_CUM_IDLE) {
               if (action == Action.CORRUPT_SLOW) {
                  this.as = false;
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   void a_clash442() {
      EntityPlayer var1 = this.getPlayerEntity();
      if (var1 != null) {
         Vec3d var2 = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - var1.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
         var1.setPositionAndUpdate(var2.x, var2.y, var2.z);
      }
   }

   @Override
   public boolean b_clash23() {
      return false;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      openInventoryGui(var1, this, new String[]{"cowgirl", "mating press", "ride"}, false);
      return true;
   }

   @Override
   public boolean v_clash227() {
      return false;
   }

   @Override
   public boolean A_clash381() {
      return false;
   }

   @Override
   public Vector4d d_clash20() {
      return new Vector4d(0.0, 0.0, 0.0, 0.0);
   }

   @Override
   public boolean c_clash21() {
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
      this.c_clash573(true);
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.b_clash444();
      if (this.world.isRemote) {
         this.d_clash443();
      }
   }

   @SideOnly(Side.CLIENT)
   void d_clash443() {
      if (this.isControlledByLocalPlayer()) {
         if (this.getCurrentAction() == Action.RAPE_INTRO) {
            HornyMeterHud.a_clash359(false);
         }
      }
   }

   void b_clash444() {
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

   boolean g_clash445() {
      EntityPlayer var1 = this.k_clash584();
      return var1 == null
         ? false
         : this.world.getBlockState(var1.getPosition().up().up()).getBlock() != Blocks.AIR;
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.galath.blink", true, var1);
            } else {
               this.createAnimation("animation.galath.null", true, var1);
            }
            break;
         case "movement":
            this.movementController.setAnimationSpeed(1.0);
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.galath.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.galath.sit", true, var1);
            } else if (!this.af) {
               this.createAnimation("animation.galath.controlled_flight", true, var1);
            } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) == 0.0F) {
               this.createAnimation(this.g_clash445() ? "animation.galath.crouchidle" : "animation.galath.idle", true, var1);
            } else if (this.aj) {
               this.movementController.setAnimationSpeed(1.5);
               this.createAnimation(this.g_clash445() ? "animation.galath.crouchwalk" : "animation.galath.run", true, var1);
            } else if (this.ao.y >= -0.1F) {
               this.movementController.setAnimationSpeed(2.0);
               this.createAnimation(this.g_clash445() ? "animation.galath.crouchwalk" : "animation.galath.walk", true, var1);
            } else {
               this.movementController.setAnimationSpeed(1.5);
               this.createAnimation(this.g_clash445() ? "animation.galath.crouchwalk" : "animation.galath.backwards_walk", true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case CORRUPT_CUM:
                  this.createAnimation("animation.galath.corrupt_cum", true, var1);
                  break;
               case CORRUPT_FAST:
                  this.createAnimation("animation.galath.corrupt_" + (this.as ? "hard" : "soft"), true, var1);
                  break;
               case CORRUPT_SLOW:
                  this.createAnimation("animation.galath.corrupt_slow", true, var1);
               case COWGIRLCUM:
               case RAPE_CHARGE:
               default:
                  break;
               case RAPE_INTRO:
                  this.createAnimation("animation.galath.rape_intro", true, var1);
                  break;
               case RAPE_ON_GOING:
                  this.createAnimation("animation.galath.rape" + this.ar, true, var1);
                  break;
               case RAPE_CUM:
                  this.createAnimation("animation.galath.rape_cum", true, var1);
                  break;
               case RAPE_CUM_IDLE:
                  this.createAnimation("animation.galath.rape_cum_idle", true, var1);
                  break;
               case NULL:
                  return PlayState.STOP;
               case STRIP:
                  this.createAnimation("animation.galath.strip", true, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.galath.attack" + this.S, true, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.galath.bowcharge", true, var1);
                  break;
               case RIDE:
               case SIT:
                  this.createAnimation("animation.galath.sit", true, var1);
                  break;
               case CORRUPT_INTRO:
                  this.createAnimation("animation.galath.corrupt_intro", true, var1);
                  break;
               case CONTROLLED_FLIGHT:
                  this.createAnimation("animation.galath.controlled_flight", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      this.initAnimationControllers();
      this.actionController
         .registerSoundListener(
            var1x -> {
               switch (var1x.sound) {
                  case "attackDone":
                     if (++this.S == 3) {
                        this.S = 0;
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
                     Vec3d var4 = this.getPositionVector();
                     Vec3d var5 = this.getCachedBoneOffset("slipR").add(var4);
                     Vec3d var6 = this.getCachedBoneOffset("slipL").add(var4);
                     Vec3d var7 = this.getCachedBoneOffset("turnable").add(var4);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, var5.x, var5.y, var5.z, 0.0, 0.0, 0.0, new int[0]);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, var6.x, var6.y, var6.z, 0.0, 0.0, 0.0, new int[0]);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, var7.x, var7.y, var7.z, 0.0, 0.0, 0.0, new int[0]);
                     break;
                  case "rapeIntroDone":
                     if (this.isControlledByLocalPlayer()) {
                        this.setCurrentAction(Action.RAPE_ON_GOING);
                     }
                     break;
                  case "rape_switch":
                     Random var8 = this.getRNG();
                     int var9 = this.ar;

                     do {
                        this.ar = var8.nextInt(3);
                     } while (this.ar == var9);

                     return;
                  case "poundRape":
                     this.playRandomSound(SoundHandler.MISC_POUNDING);
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.addToHornyMeter(0.03F);
                     }
                     break;
                  case "enableRapeUI":
                     if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.a_clash359(false);
                     }
                     break;
                  case "reloadRenderer":
                     if (!this.isControlledByLocalPlayer()) {
                        return;
                     }

                     Minecraft var18 = Minecraft.getMinecraft();
                     if (var18.gameSettings.thirdPersonView != 0) {
                        var18.renderGlobal.loadRenderers();
                     }
                     break;
                  case "corruptSwitch":
                     if (this.isControlledByLocalPlayer() && HandlePlayerMovement.d) {
                        this.setCurrentAction(Action.CORRUPT_FAST);
                     }
                     break;
                  case "corrupt_hard":
                     if (this.isControlledByLocalPlayer() && HandlePlayerMovement.d) {
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
                     CummyEntity.a_clash747(this);
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
                     EntityPlayerSP var11 = Minecraft.getMinecraft().player;
                     float var12 = this.getYawRotation() + 220.0F;
                     Vec3d var13 = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - var11.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
                     PacketHandler.b.sendToServer(new TeleportPlayerPacket(var11.getPersistentID().toString(), var13, var12, 15.0F));
                     HornyMeterHud.showHornyMeter();
                     break;
                  case "enableBoyCam":
                     if (this.isControlledByLocalPlayer()) {
                        this.aq = false;
                     }
                     break;
                  case "creampie":
                     CummyEntity.a(new DynamicTrailRenderer(130, var0 -> {
                        Vec3d var1xx = var0.getBoneWorldPosition("futaCockTip");
                        Vec3d var2 = var0.getBoneWorldPosition("futaCockTipDirHelp");
                        return var1xx.subtract(var2).normalize();
                     }, var0 -> var0.getCachedBoneOffset("futaCockTip").add(var0.getTargetPosition()), this, 0.3F, 0.3F));
                     CummyEntity.a(
                        new DynamicTrailRenderer(
                           100,
                           var1xx -> VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.6F), this.getYawRotation()),
                           var0 -> var0.getCachedBoneOffset("creampiePos").add(var0.getTargetPosition()),
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
                        GalathFlightHud.f_clash791();
                        this.playRandomSound(SoundHandler.MISC_FLAP);
                        Minecraft var10 = Minecraft.getMinecraft();
                        EntityPlayerSP var14 = var10.player;
                        MovementInput var15 = var14.movementInput;
                        Vec2f var16 = var15.getMoveVector();
                        if (var16.x != 0.0F || var16.y != 0.0F) {
                           Vec3d var17 = VectorMath.a(
                              new Vec3d(-var16.x, 0.0, var16.y),
                              RotationHelper.lerp(var14.prevRotationPitch, var14.rotationPitch, var10.getRenderPartialTicks()),
                              RotationHelper.lerp(var14.prevRotationYawHead, var14.rotationYawHead, var10.getRenderPartialTicks())
                           );
                           PacketHandler.b.sendToServer(new UpdateVelocityPacket(var17, this.getGirlId()));
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
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.eyesController);
      var1.addAnimationController(this.movementController);
   }

}
