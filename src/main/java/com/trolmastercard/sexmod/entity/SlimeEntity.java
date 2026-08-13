package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.LootTableHandler;







import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class SlimeEntity extends BaseGirlEntity {
   static final double PREG_SCALE_0_7 = 0.7F;
   static final float PREG_SCALE_0_9 = 0.9F;
   static final double PREG_MAX_100 = 100.0;
   static final float PREG_GROWTH_0_1 = 0.1F;
   static final int PREGNANT_TICKS = 2400;
   SlimeEntity.SlimeEntityState slimeState = SlimeEntity.SlimeEntityState.IDLE;
   public static DataParameter<Integer> HORNY_LEVEL = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(113);
   public static DataParameter<Float> TARGET_YAW = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.FLOAT).getSerializer().createKey(112);
   public static DataParameter<Integer> TICKS_UNTIL_BIRTH = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   int jumpTicks = 0;
   boolean wasOnGround = true;
   boolean isJumping = false;
   int blinkTicks = 0;

   public SlimeEntity(World var1) {
      super(var1);
   }

   @Override
   public String getDisplayNameText() {
      return "Slime";
   }

   @Override
   public float getScaleFactor() {
      return 1.6F;
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.CUMBLOWJOB || action != Action.THRUSTBLOWJOB && action != Action.SUCKBLOWJOB) {
         if (this.getCurrentAction() != Action.DOGGYCUM || action != Action.DOGGYFAST && action != Action.DOGGYSLOW) {
            super.setCurrentAction(action);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean shouldRenderNameTag() {
      return false;
   }

   @Override
   protected void initEntityAI() {
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.getDataManager().register(TICKS_UNTIL_BIRTH, 0);
      this.getDataManager().register(TARGET_YAW, 0.0F);
      this.getDataManager().register(HORNY_LEVEL, -1);
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.SUCKBLOWJOB || var1 == Action.THRUSTBLOWJOB) {
         return Action.CUMBLOWJOB;
      } else {
         return var1 != Action.DOGGYSLOW && var1 != Action.DOGGYFAST ? null : Action.DOGGYCUM;
      }
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.SUCKBLOWJOB) {
         return Action.THRUSTBLOWJOB;
      } else {
         return var1 == Action.DOGGYSLOW ? Action.DOGGYFAST : null;
      }
   }

   protected float getJumpUpwardsMotion() {
      return 0.9F;
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("hornyLevel", (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH));
      var1.setInteger("ticksUntilBirth", (Integer)this.entityDataManager.get(HORNY_LEVEL));
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.entityDataManager.set(TICKS_UNTIL_BIRTH, var1.getInteger("hornyLevel"));
      this.entityDataManager.set(HORNY_LEVEL, var1.getInteger("ticksUntilBirth"));
      if ((Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH) != 0) {
         this.entityDataManager.set(OUTFIT_INDEX, 0);
      }

      this.noClip = false;
      this.setNoGravity(false);
   }

   @Override
   protected ResourceLocation getLootTable() {
      return LootTableHandler.SLIME_TABLE;
   }

   @Override
   public void reinitTasks() {
      this.entityDataManager.set(TICKS_UNTIL_BIRTH, 0);
      this.entityDataManager.set(OUTFIT_INDEX, 1);
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.checkInteractionTrigger();
      this.handleHornyJump();
      if (this.isPotionActive(HornyPotion.HORNY_POTION) && this.slimeState == SlimeEntity.SlimeEntityState.IDLE && (Integer)this.entityDataManager.get(HORNY_LEVEL) == -1) {
         this.entityDataManager.set(TICKS_UNTIL_BIRTH, 2);
         if ((Integer)this.entityDataManager.get(OUTFIT_INDEX) == 1) {
            this.setCurrentAction(Action.UNDRESS);
         }

         this.removePotionEffect(HornyPotion.HORNY_POTION);
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.getCurrentAction() == Action.NULL) {
         this.handleJumpState();
      }

      if ((Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH) >= 2 && this.ticksExisted % 10 == 0) {
         spawnParticlesAround(EnumParticleTypes.HEART, this);
      }

      if (this.world.isRemote) {
         this.handleHornyLevel();
         this.handlePlayerInteraction();
      }
   }

   @SideOnly(Side.CLIENT)
   void handlePlayerInteraction() {
      if (this.getInteractionPlayerUUID() != null) {
         EntityPlayerSP var1 = Minecraft.getMinecraft().player;
         if (this.getInteractionPlayerUUID().equals(var1.getPersistentID())) {
            Vec3d var2 = this.getPositionVector();
            Vec3d var3 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.65F), this.getYawRotation());
            var2 = var2.add(var3);
            var1.setPosition(var2.x, var2.y, var2.z);
            var1.setVelocity(0.0, 0.0, 0.0);
         }
      }
   }

   void handleHornyLevel() {
      int var1 = (Integer)this.entityDataManager.get(HORNY_LEVEL);
      if (var1 != -1) {
         spawnParticlesAround(EnumParticleTypes.SPELL_WITCH, this);
         if (var1 == 0) {
            this.playSound(SoundHandler.MISC_PLOB[0]);
         }
      }
   }

   void handleHornyJump() {
      int var1 = (Integer)this.entityDataManager.get(HORNY_LEVEL);
      if (var1 != -1) {
         this.entityDataManager.set(HORNY_LEVEL, var1 - 1);
         if (--var1 < 0) {
            WildSlimeEntity var2 = new WildSlimeEntity(this.world);
            var2.setPosition(this.posX, this.posY, this.posZ);
            this.world.spawnEntity(var2);
            this.entityDataManager.set(HORNY_LEVEL, -1);
         }
      }
   }

   void checkInteractionTrigger() {
      int var1 = (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH);
      if (var1 >= 2) {
         if (var1 >= 4 && this.onGround && this.getCurrentAction() == Action.NULL) {
            this.setTargetPosition(this.getPositionVector());
            this.setYawRotation(this.rotationYaw);
            this.entityDataManager.set(IS_ANCHORED, true);
            this.setNoGravity(true);
            this.noClip = true;
            this.setCurrentAction(Action.STARTDOGGY);
         } else {
            EntityPlayer var2 = this.world.getClosestPlayerToEntity(this, 1.0);
            if (var2 != null && var2.onGround && getActiveSceneInfo(var2) == null) {
               this.setTargetPosition(this.getPositionVector());
               this.setYawRotation(this.rotationYaw);
               this.entityDataManager.set(IS_ANCHORED, true);
               this.setNoGravity(true);
               this.noClip = true;
               var2.setNoGravity(true);
               var2.noClip = true;
               PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
               this.setInteractionPlayerUUID(var2.getPersistentID());
               var2.rotationYaw = this.getYawRotation();
               Vec3d var3 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.65F), this.getYawRotation());
               var2.setPosition(this.posX + var3.x, this.posY, this.posZ + var3.z);
               if (this.getCurrentAction() == Action.WAITDOGGY) {
                  this.setCurrentAction(Action.DOGGYSTART);
               } else {
                  this.setCurrentAction(Action.SUCKBLOWJOB);
               }
            }
         }
      }
   }

   void handleJumpState() {
      if (this.world.isRemote) {
         if (this.jumpTicks == 90.0) {
            this.slimeState = SlimeEntity.SlimeEntityState.JUMP_START;
         }

         if (!this.wasOnGround && this.onGround) {
            this.slimeState = SlimeEntity.SlimeEntityState.JUMP_END;
            this.jumpTicks = 0;
         }

         float var1 = (Float)this.entityDataManager.get(TARGET_YAW);
         this.rotationYaw = var1;
         this.rotationYawHead = var1;
         this.renderYawOffset = var1;
      } else {
         if (this.jumpTicks == 85.0) {
            this.entityDataManager.set(TARGET_YAW, this.getBirthProgress());
         }

         if (this.jumpTicks == 100.0) {
            this.stopMovement();
         }

         if (!this.wasOnGround && this.onGround) {
            this.isJumping = (Integer)this.entityDataManager.get(HORNY_LEVEL) == -1 && this.getRNG().nextFloat() < 0.1F;
         }

         if (this.isJumping && this.jumpTicks == 50) {
            int var3 = (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH);
            int var2 = var3 + 1;
            this.entityDataManager.set(TICKS_UNTIL_BIRTH, var2);
            if (var2 == 1) {
               this.setCurrentAction(Action.UNDRESS);
            }
         }
      }

      if (this.onGround) {
         this.jumpTicks++;
      }

      this.wasOnGround = this.onGround;
   }

   void stopMovement() {
      this.motionX = 0.0;
      this.motionY = 0.0;
      this.motionZ = 0.0;
      this.jump();
      float var1 = (Float)this.entityDataManager.get(TARGET_YAW);
      this.rotationYaw = var1;
      this.prevRotationYaw = var1;
      Vec3d var2 = new Vec3d(0.0, 0.0, 0.7F);
      var2 = VectorMath.rotateByYaw(var2, var1);
      this.motionX = var2.x;
      this.motionZ = var2.z;
      this.jumpTicks = 0;
   }

   float getBirthProgress() {
      int var1 = (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH);
      if ((Integer)this.entityDataManager.get(HORNY_LEVEL) != -1) {
         return this.getRandomAngle();
      } else if (var1 < 2) {
         return this.getRandomAngle();
      } else {
         EntityPlayer var2 = this.world.getClosestPlayerToEntity(this, 30.0);
         if (var2 == null) {
            return this.getRandomAngle();
         } else {
            return getActiveSceneInfo(var2) != null
               ? this.getRandomAngle()
               : (float)Math.atan2(this.posZ - var2.posZ, this.posX - var2.posX) * (float) (180.0 / Math.PI) + 90.0F;
         }
      }
   }

   float getRandomAngle() {
      return Reference.RANDOM.nextFloat() * 360.0F;
   }

   public void fall(float var1, float var2) {
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return null;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.slime.fhappy", true, var1);
            } else {
               this.createAnimation("animation.slime.null", true, var1);
            }
            break;
         case "action":
            if (this.getCurrentAction() == Action.NULL) {
               this.createAnimation(this.slimeState.animationId, true, var1);
            } else {
               switch (this.getCurrentAction()) {
                  case UNDRESS:
                     this.createAnimation("animation.slime.undress", false, var1);
                     break;
                  case DRESS:
                     this.createAnimation("animation.slime.dress", false, var1);
                     break;
                  case STRIP:
                     this.createAnimation("animation.slime.strip", false, var1);
                     break;
                  case STARTBLOWJOB:
                     this.createAnimation("animation.slime.blowjobintro", false, var1);
                     break;
                  case SUCKBLOWJOB:
                     this.createAnimation("animation.slime.blowjobsuck", true, var1);
                     break;
                  case THRUSTBLOWJOB:
                     this.createAnimation("animation.slime.blowjobthrust", true, var1);
                     break;
                  case CUMBLOWJOB:
                     this.createAnimation("animation.slime.blowjobcum", false, var1);
                     break;
                  case STARTDOGGY:
                     this.createAnimation("animation.slime.doggygoonbed", false, var1);
                     break;
                  case WAITDOGGY:
                     this.createAnimation("animation.slime.doggywait", true, var1);
                     break;
                  case DOGGYSTART:
                     this.createAnimation("animation.slime.doggystart", false, var1);
                     break;
                  case DOGGYSLOW:
                     this.createAnimation("animation.slime.doggyslow", true, var1);
                     break;
                  case DOGGYFAST:
                     this.createAnimation("animation.slime.doggyfast", true, var1);
                     break;
                  case DOGGYCUM:
                     this.createAnimation("animation.slime.doggycum", false, var1);
               }
            }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(AnimationData var1) {
      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
            case "undress":
               if (this.isLocalPlayerNearby()) {
                  this.changeDataParameterFromClient("currentModel", "0");
                  this.setCurrentAction(Action.NULL);
               }
               break;
            case "dress":
               if (this.isLocalPlayerNearby()) {
                  this.entityDataManager.set(OUTFIT_INDEX, 1);
                  this.setCurrentAction((Action)null);
                  this.resetCameraAndPhysics();
               }
               break;
            case "becomeNude":
               this.entityDataManager.set(OUTFIT_INDEX, 0);
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer() && !HornyMeterHud.isVisible) {
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
            case "bjtReady":
            case "doggyfastReady":
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
                  this.changeDataParameterFromClient("pregnant", String.valueOf(2400));
               }
               break;
            case "doggyGoOnBedMSG1":
               this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
               this.cameraYaw = this.rotationYaw;
               break;
            case "doggyGoOnBedDone":
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
               int var4 = Reference.RANDOM.nextInt(4);
               if (var4 == 0) {
                  var4 = Reference.RANDOM.nextInt(2);
                  if (var4 == 0) {
                     this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
                  } else {
                     this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
                  }
               } else {
                  this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "doggyfastMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }

               this.blinkTicks++;
               if (this.blinkTicks % 2 == 0) {
                  int var5 = Reference.RANDOM.nextInt(2);
                  if (var5 == 0) {
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
               break;
            case "jumpStart":
               this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
               break;
            case "jumpStartDone":
               this.slimeState = SlimeEntity.SlimeEntityState.JUMP_AIR;
               break;
            case "jumpEndSound":
               this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
               break;
            case "jumpEndDone":
               this.slimeState = SlimeEntity.SlimeEntityState.IDLE;
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.eyesController);
   }


   enum SlimeEntityState {
      IDLE("animation.slime.idle"),
      JUMP_START("animation.slime.jumpstart"),
      JUMP_AIR("animation.slime.jumpair"),
      JUMP_END("animation.slime.jumpend");

      String animationId;

      public String getAnimationId() {
         return this.animationId;
      }

      SlimeEntityState(String var3) {
         this.animationId = var3;
      }
   }
}
