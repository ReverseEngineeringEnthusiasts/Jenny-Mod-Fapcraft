package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.AllieModel;
import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.entity.api.IKobold;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.PlayerKoboldRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.MatrixStack;

public class KoboldPlayerEntity extends AbstractKoboldPlayerEntity implements IKobold {
   public static final EyeAndKoboldColor aw = EyeAndKoboldColor.PURPLE;
   public static final DataParameter<Float> aA = EntityDataManager.createKey(KoboldPlayerEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(122);
   boolean aB = false;
   boolean az = true;
   boolean ay = false;
   int ax = 0;

   protected KoboldPlayerEntity(World var1) {
      super(var1);
   }

   public KoboldPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      EyeAndKoboldColor var1 = EyeAndKoboldColor.values()[this.getRNG().nextInt(EyeAndKoboldColor.values().length)];
      this.entityDataManager.register(au, new BlockPos(var1.getMainColor()));
      this.entityDataManager.register(as, aw.name());
      this.entityDataManager.register(aA, 0.0F);
   }

   @Override
   public AxisAlignedBB getPlayerCollisionBox(EntityPlayer var1) {
      return new AxisAlignedBB(
         var1.posX - 0.3F,
         var1.posY,
         var1.posZ - 0.3F,
         var1.posX + 0.3F,
         var1.posY + 0.9F,
         var1.posZ + 0.3F
      );
   }

   @Override
   public void setCustomPartList(List<Integer> var1) {
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var1.size(); var3++) {
         int var4 = (Integer)var1.get(var3);
         switch (var3) {
            case 0:
               this.entityDataManager.set(aA, var4 / 100.0F * 0.25F);
               break;
            case 1:
               this.entityDataManager.set(as, EyeAndKoboldColor.values()[var4].toString());
               break;
            case 2:
               this.entityDataManager.set(au, new BlockPos(EyeAndKoboldColor.values()[var4].getMainColor()));
               break;
            default:
               AbstractNpcOnlyEntity.c(var2, var4);
         }
      }

      this.entityDataManager.set(at, var2.toString());
      if (this.world.isRemote) {
         PlayerKoboldRenderer.clearRenderCache();
      }
   }

   @Override
   public ArrayList<Integer> getBasePartIdList() {
      ArrayList var1 = new ArrayList();
      var1.add(Math.round((Float)this.entityDataManager.get(aA) * 100.0F / 0.25F));
      var1.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((String)this.entityDataManager.get(as))));
      var1.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((Vec3i)this.entityDataManager.get(au))));
      return var1;
   }

   @Override
   protected String buildModelCodeDNA(StringBuilder var1) {
      AbstractNpcOnlyEntity.b(var1, 8);
      AbstractNpcOnlyEntity.b(var1, 3);
      AbstractNpcOnlyEntity.appendRandomGene(var1);
      AbstractNpcOnlyEntity.appendRandomGene(var1);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(var1, 1);
      return var1.toString();
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return new ArrayList<Integer>() {
         {
            this.add(101);
            this.add(EyeAndKoboldColor.values().length);
            this.add(EyeAndKoboldColor.values().length);
            this.add(8);
            this.add(3);
            this.add(101);
            this.add(101);
            this.add(3);
            this.add(3);
            this.add(4);
            this.add(2);
         }
      };
   }

   @Override
   protected void clearBoneColors() {
      PlayerKoboldRenderer.clearRenderCache();
      KoboldRenderer.clearBoneColors();
   }

   @Override
   public float getScaleFactor() {
      float var1 = 0.25F - (Float)this.entityDataManager.get(aA);
      return 1.4F - var1;
   }

   @Override
   public void handleOwnerCommand(String var1, UUID var2) {
      if ("anal".equals(var1)) {
         this.teleportPlayerToGirl(var2);
         this.setCurrentAction(Action.KOBOLD_ANAL_START);
         this.sendActionPacket(this.getOutfitIndex(), Action.KOBOLD_ANAL_START);
         this.setOutfitIndex(0);
      }

      if ("oral".equals(var1)) {
         this.teleportPlayerToGirl(var2);
         this.setCurrentAction(Action.STARTBLOWJOB);
         this.sendActionPacket(this.getOutfitIndex(), Action.STARTBLOWJOB);
         this.setOutfitIndex(0);
      }

      if ("mating".equals(var1)) {
         this.teleportPlayerToGirl(var2);
         this.setCurrentAction(Action.MATING_PRESS_START);
         this.sendActionPacket(this.getOutfitIndex(), Action.MATING_PRESS_START);
         this.setOutfitIndex(0);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, var1, new String[]{"anal", "oral", "mating"}, null, false));
      return true;
   }

   @Override
   public boolean isBlockedByCeiling() {
      Block var1 = this.world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
      return !var1.isPassable(this.world, this.getPosition().add(0, 1, 0));
   }

   @Override
   protected MatrixStack applyAdditionalMatrixTransformations(MatrixStack var1) {
      float var2 = 0.25F - (Float)this.entityDataManager.get(aA);
      var1.scale(1.0F - var2, 1.0F - var2, 1.0F - var2);
      return var1;
   }

   @Override
   protected float transformCameraPivotY(float var1) {
      float var2 = 1.0F - (0.25F - (Float)this.entityDataManager.get(aA));
      return var1 * var2;
   }

   @Override
   public IVanillaModel getHandModel(int var1) {
      return new AllieModel();
   }

   @Override
   public String getHandTexture(int var1) {
      return "textures/entity/kobold/hand.png";
   }

   @Override
   public Vec3i getHandColor(int var1) {
      try {
         return EyeAndKoboldColor.valueOf((String)this.entityDataManager.get(as)).getMainColor();
      } catch (Exception var3) {
         var3.printStackTrace();
         return super.getHandColor(var1);
      }
   }

   @Nullable
   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.SUCKBLOWJOB_BLINK) {
         return Action.THRUSTBLOWJOB;
      } else {
         return var1 == Action.KOBOLD_ANAL_SLOW ? Action.KOBOLD_ANAL_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.THRUSTBLOWJOB || var1 == Action.SUCKBLOWJOB_BLINK) {
         return Action.CUMBLOWJOB;
      } else if (var1 == Action.KOBOLD_ANAL_SLOW || var1 == Action.KOBOLD_ANAL_FAST) {
         return Action.KOBOLD_ANAL_CUM;
      } else {
         return var1 != Action.MATING_PRESS_HARD && var1 != Action.MATING_PRESS_SOFT ? null : Action.MATING_PRESS_CUM;
      }
   }

   @Override
   public void setCurrentAction(Action action) {
      Action var2 = this.getCurrentAction();
      if (var2 != Action.MATING_PRESS_CUM || action != Action.MATING_PRESS_SOFT && action != Action.MATING_PRESS_HARD) {
         if (var2 != Action.KOBOLD_ANAL_CUM || action != Action.KOBOLD_ANAL_SLOW && action != Action.KOBOLD_ANAL_FAST) {
            if (var2 != Action.CUMBLOWJOB || action != Action.SUCKBLOWJOB && action != Action.THRUSTBLOWJOB) {
               super.setCurrentAction(action);
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      float var2 = 0.25F - (Float)this.getDataManager().get(KoboldEntity.aE);
      GeckoLibCache.getInstance().parser.setValue("size", var2);
      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.kobold.blink", true, var1);
            } else {
               this.createAnimation("animation.kobold.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.kobold.null", true, var1);
            } else if (this.ak) {
               this.createAnimation("animation.kobold.sit", true, var1);
            } else {
               if (this.movementController.getCurrentAnimation() != null && this.movementController.getCurrentAnimation().animationName.contains("fly") && this.af) {
                  this.aB = !this.aB;
               }

               if (!this.af) {
                  this.createAnimation("animation.kobold.fly" + (this.aB ? "2" : ""), true, var1);
               } else if (Math.abs(this.ao.x) + Math.abs(this.ao.y) > 0.0F) {
                  if (this.aj) {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.createAnimation("animation.kobold.run", true, var1);
                  } else if (this.ao.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.kobold.walk", true, var1);
                  } else {
                     this.movementController.setAnimationSpeed(1.75);
                     this.createAnimation("animation.kobold.backwards_walk", true, var1);
                  }
               } else {
                  this.createAnimation("animation.kobold.idle", true, var1);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.kobold.null", true, var1);
                  break;
               case STRIP:
                  this.createAnimation("animation.kobold.strip", false, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.kobold.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.kobold.bowcharge", false, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.kobold.sit", true, var1);
                  break;
               case MINE:
                  this.createAnimation("animation.kobold.fall_tree", true, var1);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.kobold.paymentBackpack", true, var1);
                  break;
               case STARTBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobStart", false, var1);
                  break;
               case SUCKBLOWJOB_BLINK:
                  String var5 = this.az ? "R" : "L";
                  String var6 = this.ay ? "Switch" : "";
                  this.createAnimation("animation.kobold.blowjobSlow" + var5 + var6, true, var1);
                  break;
               case THRUSTBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobFast", true, var1);
                  break;
               case CUMBLOWJOB:
                  this.createAnimation("animation.kobold.blowjobCum", false, var1);
                  break;
               case KOBOLD_ANAL_START:
                  this.createAnimation("animation.kobold.analStart", false, var1);
                  break;
               case KOBOLD_ANAL_SLOW:
                  this.createAnimation("animation.kobold.analSoft", true, var1);
                  break;
               case KOBOLD_ANAL_FAST:
                  this.createAnimation("animation.kobold.analHard", true, var1);
                  break;
               case KOBOLD_ANAL_CUM:
                  this.createAnimation("animation.kobold.analCum", true, var1);
                  break;
               case SLEEP:
                  this.createAnimation("animation.kobold.sleep", true, var1);
                  break;
               case MATING_PRESS_START:
                  this.createAnimation("animation.kobold.mating_press_start", false, var1);
                  break;
               case MATING_PRESS_SOFT:
                  this.createAnimation("animation.kobold.mating_press_soft", true, var1);
                  break;
               case MATING_PRESS_HARD:
                  this.createAnimation("animation.kobold.mating_press_hard", true, var1);
                  break;
               case MATING_PRESS_CUM:
                  this.createAnimation("animation.kobold.mating_press_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   void b(SoundEvent var1) {
      this.b(var1, 1.0F);
   }

   void b(SoundEvent[] var1) {
      this.b(var1, 1.0F);
   }

   void b(SoundEvent[] var1, float var2) {
      this.b(var1[this.getRNG().nextInt(var1.length)], var2);
   }

   void b(SoundEvent var1, float var2) {
      float var3 = 0.25F - (Float)this.entityDataManager.get(aA);
      double var4 = var3 / 0.25F;
      float var6 = (float)RotationHelper.lerpDouble(0.9F, 1.1F, var4);
      this.playSoundAtPosition(var1, var2, var6);
   }

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
            case "paymentMSG1":
               this.sendChatMessageToPlayer(this.getInteractionPlayerUUID(), "I'd like to use ur services owo");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "plob":
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "paymentDone":
               if (this.isControlledByLocalPlayer()) {
                  this.U();
               }
               break;
            case "blowjobStartMSG1":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var11 = Minecraft.getMinecraft().player;
                  Vec3d var13 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.625 - var11.getEyeHeight(), -1.0), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(var13), this.getYawRotation() + 180.0F, 0.0F)
                     );
               }
               break;
            case "blowjobStartMSG2":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var10 = Minecraft.getMinecraft().player;
                  Vec3d var12 = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5 - var10.getEyeHeight(), -0.6875), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(var12), this.getYawRotation() + 180.0F - 40.0F, 0.0F
                        )
                     );
               }
               break;
            case "lipsound":
               if (this.getRNG().nextBoolean()) {
                  this.playRandomSoundAtVolume(SoundHandler.GIRLS_ALLIE_LIPSOUND, 1.5F);
               } else {
                  this.playRandomSoundAtVolume(SoundHandler.GIRLS_JENNY_LIPSOUND, 1.5F);
               }

               HornyMeterHud.addToHornyMeter(0.02F);
               break;
            case "touch":
               this.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "blowjobStartDone":
               this.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               this.ay = false;
               this.az = true;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "switch":
               this.ay = this.getRNG().nextBoolean();
               this.actionController.clearAnimationCache();
               break;
            case "endSwitch":
               this.ay = false;
               this.az = !this.az;
               this.actionController.clearAnimationCache();
               break;
            case "blowjobFastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               }
               break;
            case "cumLoud":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "cumQuiet":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "analCumDone":
            case "blowjobCumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "analStartDone":
               this.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "analStartCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var9 = Minecraft.getMinecraft().player;
                  Vec3d var5 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.5625 - var9.getEyeHeight(), 0.5625), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(var5), this.getYawRotation(), 0.0F));
               }
               break;
            case "pounding":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "analFastRapid":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  if (this.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                     this.resetAnimationControllerOffset();
                  } else {
                     this.setCurrentAction(Action.KOBOLD_ANAL_FAST);
                  }
               }
               break;
            case "analDone":
               if (this.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                  this.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               }
               break;
            case "analHard":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "analSoft":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "giggle":
               this.b(SoundHandler.GIRLS_KOBOLD_GIGGLE);
               break;
            case "moan":
               this.b(SoundHandler.GIRLS_KOBOLD_MOAN);
               break;
            case "moanMating":
               this.ax--;
               if (this.ax <= 0) {
                  this.ax = 3;
                  this.b(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "analHardMSG1":
               this.ax--;
               if (this.ax <= 0) {
                  this.ax = 4;
                  this.b(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "orgasm":
               this.b(SoundHandler.GIRLS_KOBOLD_ORGASM);
               break;
            case "breath":
               this.b(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING, 0.5F);
               break;
            case "haa":
               this.b(SoundHandler.GIRLS_KOBOLD_HAA, 0.7F);
               break;
            case "interested":
               this.b(SoundHandler.GIRLS_KOBOLD_INTERESTED);
               break;
            case "yep":
               this.b(SoundHandler.GIRLS_KOBOLD_YEP);
               break;
            case "bjmoan":
               this.b(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_BJMOAN));
               break;
            case "blowjobStartbreath":
               int var6 = this.getRNG().nextInt(3);
               this.b(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING[var6]);
               break;
            case "matingCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var8 = Minecraft.getMinecraft().player;
                  Vec3d var16 = new Vec3d(0.0, 0.4375 - var8.eyeHeight, -0.6875);
                  var16 = VectorMath.rotateByYaw(var16, this.getYawRotation() + 180.0F);
                  var16 = var16.add(this.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(var8.getPersistentID().toString(), var16, this.getYawRotation() + 180.0F, 10.0F));
               }
               break;
            case "mating_press_startDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
            case "mating_press_hardDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.MATING_PRESS_SOFT);
               }
               break;
            case "mating_press_softReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.MATING_PRESS_HARD);
               }
               break;
            case "mating_press_hardReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "mating_cum_cam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP var4 = Minecraft.getMinecraft().player;
                  Vec3d var7 = new Vec3d(0.0, 1.1875 - var4.eyeHeight, 0.125);
                  var7 = VectorMath.rotateByYaw(var7, this.getYawRotation() + 180.0F);
                  var7 = var7.add(this.getTargetPosition());
                  PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(var4.getPersistentID().toString(), var7, this.getYawRotation() + 180.0F, 70.0F));
               }
               break;
            case "cumMsg":
               this.sendChatMessage("I.. hope I am satisfying you sir");
               this.b(SoundHandler.GIRLS_KOBOLD_SAD[this.getRNG().nextInt(1)]);
               break;
            case "mating_press_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
               }
         }
      };
      this.movementController.transitionLengthTicks = 3.0;
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
