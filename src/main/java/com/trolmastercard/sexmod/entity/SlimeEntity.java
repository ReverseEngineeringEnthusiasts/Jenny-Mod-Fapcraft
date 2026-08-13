package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.dz;







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
   static final double Q = 0.7F;
   static final float W = 0.9F;
   static final double M = 100.0;
   static final float L = 0.1F;
   static final int O = 2400;
   SlimeEntity.SlimeEntityState S = SlimeEntity.SlimeEntityState.IDLE;
   public static DataParameter<Integer> U = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(113);
   public static DataParameter<Float> R = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.FLOAT).getSerializer().createKey(112);
   public static DataParameter<Integer> T = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   int N = 0;
   boolean K = true;
   boolean V = false;
   int P = 0;

   public SlimeEntity(World var1) {
      super(var1);
   }

   @Override
   public String getDisplayNameText() {
      return "Slime";
   }

   @Override
   public float i_clash226() {
      return 1.6F;
   }

   @Override
   public void b(fp var1) {
      if (this.getCurrentAction() != fp.CUMBLOWJOB || var1 != fp.THRUSTBLOWJOB && var1 != fp.SUCKBLOWJOB) {
         if (this.getCurrentAction() != fp.DOGGYCUM || var1 != fp.DOGGYFAST && var1 != fp.DOGGYSLOW) {
            super.b(var1);
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
      this.getDataManager().register(T, 0);
      this.getDataManager().register(R, 0.0F);
      this.getDataManager().register(U, -1);
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.SUCKBLOWJOB || var1 == fp.THRUSTBLOWJOB) {
         return fp.CUMBLOWJOB;
      } else {
         return var1 != fp.DOGGYSLOW && var1 != fp.DOGGYFAST ? null : fp.DOGGYCUM;
      }
   }

   @Override
   protected fp getNextAction(fp var1) {
      if (var1 == fp.SUCKBLOWJOB) {
         return fp.THRUSTBLOWJOB;
      } else {
         return var1 == fp.DOGGYSLOW ? fp.DOGGYFAST : null;
      }
   }

   protected float getJumpUpwardsMotion() {
      return 0.9F;
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("hornyLevel", (Integer)this.entityDataManager.get(T));
      var1.setInteger("ticksUntilBirth", (Integer)this.entityDataManager.get(U));
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.entityDataManager.set(T, var1.getInteger("hornyLevel"));
      this.entityDataManager.set(U, var1.getInteger("ticksUntilBirth"));
      if ((Integer)this.entityDataManager.get(T) != 0) {
         this.entityDataManager.set(OUTFIT_INDEX, 0);
      }

      this.noClip = false;
      this.setNoGravity(false);
   }

   @Override
   protected ResourceLocation getLootTable() {
      return dz.b;
   }

   @Override
   public void reinitTasks() {
      this.entityDataManager.set(T, 0);
      this.entityDataManager.set(OUTFIT_INDEX, 1);
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.a_clash725();
      this.c_clash724();
      if (this.isPotionActive(HornyPotion.b) && this.S == SlimeEntity.SlimeEntityState.IDLE && (Integer)this.entityDataManager.get(U) == -1) {
         this.entityDataManager.set(T, 2);
         if ((Integer)this.entityDataManager.get(OUTFIT_INDEX) == 1) {
            this.b(fp.UNDRESS);
         }

         this.removePotionEffect(HornyPotion.b);
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.getCurrentAction() == fp.NULL) {
         this.b_clash726();
      }

      if ((Integer)this.entityDataManager.get(T) >= 2 && this.ticksExisted % 10 == 0) {
         a(EnumParticleTypes.HEART, this);
      }

      if (this.world.isRemote) {
         this.d_clash723();
         this.i_clash722();
      }
   }

   @SideOnly(Side.CLIENT)
   void i_clash722() {
      if (this.getInteractionPlayerUUID() != null) {
         EntityPlayerSP var1 = Minecraft.getMinecraft().player;
         if (this.getInteractionPlayerUUID().equals(var1.getPersistentID())) {
            Vec3d var2 = this.getPositionVector();
            Vec3d var3 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 0.65F), this.getYawRotation());
            var2 = var2.add(var3);
            var1.setPosition(var2.x, var2.y, var2.z);
            var1.setVelocity(0.0, 0.0, 0.0);
         }
      }
   }

   void d_clash723() {
      int var1 = (Integer)this.entityDataManager.get(U);
      if (var1 != -1) {
         a(EnumParticleTypes.SPELL_WITCH, this);
         if (var1 == 0) {
            this.a(SoundHandler.MISC_PLOB[0]);
         }
      }
   }

   void c_clash724() {
      int var1 = (Integer)this.entityDataManager.get(U);
      if (var1 != -1) {
         this.entityDataManager.set(U, var1 - 1);
         if (--var1 < 0) {
            WildSlimeEntity var2 = new WildSlimeEntity(this.world);
            var2.setPosition(this.posX, this.posY, this.posZ);
            this.world.spawnEntity(var2);
            this.entityDataManager.set(U, -1);
         }
      }
   }

   void a_clash725() {
      int var1 = (Integer)this.entityDataManager.get(T);
      if (var1 >= 2) {
         if (var1 >= 4 && this.onGround && this.getCurrentAction() == fp.NULL) {
            this.setTargetPosition(this.getPositionVector());
            this.setYawRotation(this.rotationYaw);
            this.entityDataManager.set(IS_ANCHORED, true);
            this.setNoGravity(true);
            this.noClip = true;
            this.b(fp.STARTDOGGY);
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
               PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
               this.setInteractionPlayerUUID(var2.getPersistentID());
               var2.rotationYaw = this.getYawRotation();
               Vec3d var3 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 0.65F), this.getYawRotation());
               var2.setPosition(this.posX + var3.x, this.posY, this.posZ + var3.z);
               if (this.getCurrentAction() == fp.WAITDOGGY) {
                  this.b(fp.DOGGYSTART);
               } else {
                  this.b(fp.SUCKBLOWJOB);
               }
            }
         }
      }
   }

   void b_clash726() {
      if (this.world.isRemote) {
         if (this.N == 90.0) {
            this.S = SlimeEntity.SlimeEntityState.JUMP_START;
         }

         if (!this.K && this.onGround) {
            this.S = SlimeEntity.SlimeEntityState.JUMP_END;
            this.N = 0;
         }

         float var1 = (Float)this.entityDataManager.get(R);
         this.rotationYaw = var1;
         this.rotationYawHead = var1;
         this.renderYawOffset = var1;
      } else {
         if (this.N == 85.0) {
            this.entityDataManager.set(R, this.e_clash728());
         }

         if (this.N == 100.0) {
            this.h_clash727();
         }

         if (!this.K && this.onGround) {
            this.V = (Integer)this.entityDataManager.get(U) == -1 && this.getRNG().nextFloat() < 0.1F;
         }

         if (this.V && this.N == 50) {
            int var3 = (Integer)this.entityDataManager.get(T);
            int var2 = var3 + 1;
            this.entityDataManager.set(T, var2);
            if (var2 == 1) {
               this.b(fp.UNDRESS);
            }
         }
      }

      if (this.onGround) {
         this.N++;
      }

      this.K = this.onGround;
   }

   void h_clash727() {
      this.motionX = 0.0;
      this.motionY = 0.0;
      this.motionZ = 0.0;
      this.jump();
      float var1 = (Float)this.entityDataManager.get(R);
      this.rotationYaw = var1;
      this.prevRotationYaw = var1;
      Vec3d var2 = new Vec3d(0.0, 0.0, 0.7F);
      var2 = ck.rotateByYaw(var2, var1);
      this.motionX = var2.x;
      this.motionZ = var2.z;
      this.N = 0;
   }

   float e_clash728() {
      int var1 = (Integer)this.entityDataManager.get(T);
      if ((Integer)this.entityDataManager.get(U) != -1) {
         return this.f_clash729();
      } else if (var1 < 2) {
         return this.f_clash729();
      } else {
         EntityPlayer var2 = this.world.getClosestPlayerToEntity(this, 30.0);
         if (var2 == null) {
            return this.f_clash729();
         } else {
            return getActiveSceneInfo(var2) != null
               ? this.f_clash729()
               : (float)Math.atan2(this.posZ - var2.posZ, this.posX - var2.posX) * (float) (180.0 / Math.PI) + 90.0F;
         }
      }
   }

   float f_clash729() {
      return Reference.f.nextFloat() * 360.0F;
   }

   public void fall(float var1, float var2) {
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return null;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != fp.NULL && this.getCurrentAction().autoBlink) {
               this.a("animation.slime.fhappy", true, var1);
            } else {
               this.a("animation.slime.null", true, var1);
            }
            break;
         case "action":
            if (this.getCurrentAction() == fp.NULL) {
               this.a(this.S.animationId, true, var1);
            } else {
               switch (this.getCurrentAction()) {
                  case UNDRESS:
                     this.a("animation.slime.undress", false, var1);
                     break;
                  case DRESS:
                     this.a("animation.slime.dress", false, var1);
                     break;
                  case STRIP:
                     this.a("animation.slime.strip", false, var1);
                     break;
                  case STARTBLOWJOB:
                     this.a("animation.slime.blowjobintro", false, var1);
                     break;
                  case SUCKBLOWJOB:
                     this.a("animation.slime.blowjobsuck", true, var1);
                     break;
                  case THRUSTBLOWJOB:
                     this.a("animation.slime.blowjobthrust", true, var1);
                     break;
                  case CUMBLOWJOB:
                     this.a("animation.slime.blowjobcum", false, var1);
                     break;
                  case STARTDOGGY:
                     this.a("animation.slime.doggygoonbed", false, var1);
                     break;
                  case WAITDOGGY:
                     this.a("animation.slime.doggywait", true, var1);
                     break;
                  case DOGGYSTART:
                     this.a("animation.slime.doggystart", false, var1);
                     break;
                  case DOGGYSLOW:
                     this.a("animation.slime.doggyslow", true, var1);
                     break;
                  case DOGGYFAST:
                     this.a("animation.slime.doggyfast", true, var1);
                     break;
                  case DOGGYCUM:
                     this.a("animation.slime.doggycum", false, var1);
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
                  this.b(fp.NULL);
               }
               break;
            case "dress":
               if (this.isLocalPlayerNearby()) {
                  this.entityDataManager.set(OUTFIT_INDEX, 1);
                  this.b((fp) null);
                  this.resetCameraAndPhysics();
               }
               break;
            case "becomeNude":
               this.entityDataManager.set(OUTFIT_INDEX, 0);
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer() && !HornyMeterHud.d) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "bjiMSG10":
               if (this.isControlledByLocalPlayer()) {
                  this.positionPlayerRelative(-0.4, -0.8, -0.2, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
               this.a(SoundEvents.ENTITY_SLIME_SQUISH, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjiMSG12":
               if (Reference.f.nextInt(5) == 0) {
                  this.a(SoundEvents.ENTITY_SLIME_JUMP, 0.5F);
               }

               this.a(SoundEvents.ENTITY_SLIME_SQUISH, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjtMSG1":
               this.a(SoundEvents.BLOCK_SLIME_HIT);
               this.a(SoundEvents.ENTITY_SLIME_DEATH);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "bjiDone":
               this.b(fp.SUCKBLOWJOB);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "bjtDone":
               this.b(fp.SUCKBLOWJOB);
               break;
            case "bjtReady":
            case "doggyfastReady":
               if (this.isControlledByLocalPlayer() && d3.d) {
                  this.N();
               }
               break;
            case "bjcMSG1":
               this.a(SoundEvents.ENTITY_SLIME_JUMP);
               break;
            case "bjcMSG2":
               this.a(SoundEvents.ENTITY_SLIME_JUMP);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "doggyslowMSG2":
               this.a(SoundEvents.BLOCK_SLIME_HIT);
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
               this.a(SoundEvents.ENTITY_SLIME_SQUISH);
               this.cameraYaw = this.rotationYaw;
               break;
            case "doggyGoOnBedDone":
               this.b(fp.WAITDOGGY);
               break;
            case "doggystartMSG1":
               this.a(SoundHandler.MISC_TOUCH[0]);
               break;
            case "doggystartMSG2":
               this.a(SoundHandler.MISC_TOUCH[1]);
               break;
            case "doggystartMSG3":
               this.a(SoundEvents.ENTITY_SLIME_SQUISH, 0.25F);
               break;
            case "doggystartMSG4":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 1.5F);
               break;
            case "doggystartMSG5":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               this.a(SoundEvents.BLOCK_SLIME_HIT);
               break;
            case "doggystartDone":
               this.b(fp.DOGGYSLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doggyslowMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               int var4 = Reference.f.nextInt(4);
               if (var4 == 0) {
                  var4 = Reference.f.nextInt(2);
                  if (var4 == 0) {
                     this.a(SoundEvents.ENTITY_SLIME_JUMP);
                  } else {
                     this.a(SoundEvents.ENTITY_SLIME_SQUISH);
                  }
               } else {
                  this.a(SoundEvents.BLOCK_SLIME_HIT);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "doggyfastMSG1":
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }

               this.P++;
               if (this.P % 2 == 0) {
                  int var5 = Reference.f.nextInt(2);
                  if (var5 == 0) {
                     this.a(SoundEvents.ENTITY_SLIME_JUMP);
                  } else {
                     this.a(SoundEvents.ENTITY_SLIME_SQUISH);
                  }
               } else {
                  this.a(SoundEvents.BLOCK_SLIME_HIT);
               }
               break;
            case "doggyfastDone":
               this.b(fp.DOGGYSLOW);
               break;
            case "doggycumMSG1":
               this.a(SoundHandler.MISC_CUMINFLATION[0], 4.0F);
               this.a(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 2.0F);
               this.a(SoundEvents.ENTITY_SLIME_DEATH);
               break;
            case "jumpStart":
               this.a(SoundEvents.ENTITY_SLIME_JUMP);
               break;
            case "jumpStartDone":
               this.S = SlimeEntity.SlimeEntityState.JUMP_AIR;
               break;
            case "jumpEndSound":
               this.a(SoundEvents.ENTITY_SLIME_SQUISH);
               break;
            case "jumpEndDone":
               this.S = SlimeEntity.SlimeEntityState.IDLE;
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

      public String a_clash867() {
         return this.animationId;
      }

      SlimeEntityState(String var3) {
         this.animationId = var3;
      }
   }
}
