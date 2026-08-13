package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.Reference;







import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntityMoveHelper.Action;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class WildSlimeEntity extends EntityLiving {
   public static List<WildSlimeEntity> g = new ArrayList<>();
   private static final DataParameter<Integer> d = EntityDataManager.createKey(WildSlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   private static final DataParameter<Integer> c = EntityDataManager.createKey(WildSlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(110);
   public float a;
   public float e;
   public float h;
   private boolean f;

   public WildSlimeEntity(World var1) {
      super(var1);
      this.moveHelper = new WildSlimeEntity.b(this);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new WildSlimeEntity.d(this));
      this.tasks.addTask(5, new WildSlimeEntity.c(this));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(c, 1);
      this.dataManager.register(d, 0);
   }

   public void fall(float var1, float var2) {
   }

   protected boolean canDespawn() {
      return false;
   }

   protected void a(int var1, boolean var2) {
      this.dataManager.set(c, var1);
      this.setSize(0.51000005F * var1, 0.51000005F * var1);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(var1 * var1);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F + 0.1F * var1);
      if (var2) {
         this.setHealth(this.getMaxHealth());
      }

      this.experienceValue = var1;
   }

   public int getSquishFactor() {
      return (Integer)this.dataManager.get(c);
   }

   public static void a(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, WildSlimeEntity.class);
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("Size", this.getSquishFactor() - 1);
      var1.setBoolean("wasOnGround", this.f);
      var1.setInteger("ageInTicks", (Integer)this.dataManager.get(d));
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      int var2 = var1.getInteger("Size");
      if (var2 < 0) {
         var2 = 0;
      }

      this.a(var2 + 1, false);
      this.f = var1.getBoolean("wasOnGround");
      this.dataManager.set(d, var1.getInteger("ageInTicks"));
   }

   public boolean j_clash93() {
      return this.getSquishFactor() <= 1;
   }

   protected EnumParticleTypes g_clash94() {
      return EnumParticleTypes.SLIME;
   }

   public static ArrayList<WildSlimeEntity> a_clash95(Vec3d var0) {
      ArrayList var1 = a_clash96(var0, 0.1);
      if (var1.isEmpty()) {
         var1 = a_clash96(var0, 0.5);
      }

      return var1;
   }

   private static ArrayList<WildSlimeEntity> a_clash96(Vec3d var0, double var1) {
      ArrayList var3 = new ArrayList();

      try {
         for (WildSlimeEntity var5 : g) {
            if (var5 != null) {
               double var6 = Math.abs(var5.prevPosX - var0.x)
                  + Math.abs(var5.prevPosY - var0.y)
                  + Math.abs(var5.prevPosZ - var0.z);
               if (var5.world != null && var6 < var1) {
                  var3.add(var5);
               }
            }
         }
      } catch (Exception var8) {
         System.out.println("couldnt find slimes at distance " + var1);
      }

      return var3;
   }

   public Vec3d e_clash97() {
      return new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ);
   }

   void a(EnumParticleTypes var1) {
      double var2 = Reference.f.nextGaussian() * 0.02;
      double var4 = Reference.f.nextGaussian() * 0.02;
      double var6 = Reference.f.nextGaussian() * 0.02;
      this.world
         .spawnParticle(
            var1,
            this.posX + Reference.f.nextFloat() * this.width * 2.0F - this.width,
            this.posY + 0.15 + Reference.f.nextFloat() * this.height,
            this.posZ + Reference.f.nextFloat() * this.width * 2.0F - this.width,
            var2,
            var4,
            var6,
            new int[0]
         );
   }

   public void onUpdate() {
      this.dataManager.set(d, (Integer)this.dataManager.get(d) + 1);
      if (this.world.isRemote) {
         if (((Integer)this.dataManager.get(d)).intValue() > 7980.0) {
            this.a(EnumParticleTypes.CLOUD);
         } else if (((Integer)this.dataManager.get(d)).intValue() > 5880.0 && this.ticksExisted % 10 == 0) {
            this.a(EnumParticleTypes.VILLAGER_HAPPY);
         }
      } else if ((Integer)this.dataManager.get(d) > 8400) {
         SlimeEntity var1 = new SlimeEntity(this.world);
         var1.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         this.world.spawnEntity(var1);
         var1.a(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
         this.world.removeEntity(this);
      }

      this.e = this.e + (this.a - this.e) * 0.5F;
      this.h = this.e;
      super.onUpdate();
      if (this.onGround && !this.f) {
         int var13 = this.getSquishFactor();
         if (this.k_clash104()) {
            var13 = 0;
         }

         for (int var2 = 0; var2 < var13 * 8; var2++) {
            float var3 = this.rand.nextFloat() * (float) (Math.PI * 2);
            float var4 = this.rand.nextFloat() * 0.5F + 0.5F;
            float var5 = MathHelper.sin(var3) * var13 * 0.5F * var4;
            float var6 = MathHelper.cos(var3) * var13 * 0.5F * var4;
            World var7 = this.world;
            EnumParticleTypes var8 = this.g_clash94();
            double var9 = this.posX + var5;
            double var11 = this.posZ + var6;
            var7.spawnParticle(var8, var9, this.getEntityBoundingBox().minY, var11, 0.0, 0.0, 0.0, new int[0]);
         }

         this.playSound(this.f_clash101(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         this.a = -0.5F;
      } else if (!this.onGround && this.f) {
         this.a = 1.0F;
      }

      this.f = this.onGround;
      this.b_clash98();
   }

   protected void b_clash98() {
      this.a *= 0.6F;
   }

   protected int a_clash99() {
      return this.rand.nextInt(100) + 50;
   }

   protected WildSlimeEntity d_clash100() {
      return new WildSlimeEntity(this.world);
   }

   public void notifyDataManagerChange(DataParameter<?> var1) {
      if (c.equals(var1)) {
         int var2 = this.getSquishFactor();
         this.setSize(0.51000005F * var2, 0.51000005F * var2);
         this.rotationYaw = this.rotationYawHead;
         this.renderYawOffset = this.rotationYawHead;
         if (this.isInWater() && this.rand.nextInt(20) == 0) {
            this.doWaterSplashEffect();
         }
      }

      super.notifyDataManagerChange(var1);
   }

   public void setDead() {
      int var1 = this.getSquishFactor();
      if (!this.world.isRemote && var1 > 1 && this.getHealth() <= 0.0F) {
         int var2 = 2 + this.rand.nextInt(3);

         for (int var3 = 0; var3 < var2; var3++) {
            float var4 = (var3 % 2 - 0.5F) * var1 / 4.0F;
            float var5 = (var3 / 2 - 0.5F) * var1 / 4.0F;
            WildSlimeEntity var6 = this.d_clash100();
            if (this.hasCustomName()) {
               var6.setCustomNameTag(this.getCustomNameTag());
            }

            if (this.isNoDespawnRequired()) {
               var6.enablePersistence();
            }

            var6.a(var1 / 2, true);
            var6.setLocationAndAngles(this.posX + var4, this.posY + 0.5, this.posZ + var5, this.rand.nextFloat() * 360.0F, 0.0F);
            this.world.spawnEntity(var6);
         }
      }

      super.setDead();
   }

   public float getEyeHeight() {
      return 0.625F * this.height;
   }

   protected SoundEvent getHurtSound(DamageSource var1) {
      return this.j_clash93() ? SoundEvents.ENTITY_SMALL_SLIME_HURT : SoundEvents.ENTITY_SLIME_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.j_clash93() ? SoundEvents.ENTITY_SMALL_SLIME_DEATH : SoundEvents.ENTITY_SLIME_DEATH;
   }

   protected SoundEvent f_clash101() {
      return this.j_clash93() ? SoundEvents.ENTITY_SMALL_SLIME_SQUISH : SoundEvents.ENTITY_SLIME_SQUISH;
   }

   protected Item getDropItem() {
      return this.getSquishFactor() == 1 ? Items.SLIME_BALL : null;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return this.getSquishFactor() == 1 ? LootTableList.ENTITIES_SLIME : LootTableList.EMPTY;
   }

   protected float getSoundVolume() {
      return 0.4F * this.getSquishFactor();
   }

   public int getVerticalFaceSpeed() {
      return 0;
   }

   protected boolean i_clash102() {
      return this.getSquishFactor() > 0;
   }

   protected void jump() {
      this.motionY = 0.42F;
      this.isAirBorne = true;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      this.a(1, true);
      return super.onInitialSpawn(var1, var2);
   }

   protected SoundEvent c_clash103() {
      return this.j_clash93() ? SoundEvents.ENTITY_SMALL_SLIME_JUMP : SoundEvents.ENTITY_SLIME_JUMP;
   }

   protected boolean k_clash104() {
      return false;
   }


   static class a extends EntityAIBase {
      private final WildSlimeEntity b;
      private float a;
      private int c;

      public a(WildSlimeEntity var1) {
         this.b = var1;
         this.setMutexBits(2);
      }

      public boolean shouldExecute() {
         return this.b.getAttackTarget() == null
            && (this.b.onGround || this.b.isInWater() || this.b.isInLava() || this.b.isPotionActive(MobEffects.LEVITATION));
      }

      public void updateTask() {
         if (--this.c <= 0) {
            this.c = 40 + this.b.getRNG().nextInt(60);
            this.a = this.b.getRNG().nextInt(360);
         }

         ((WildSlimeEntity.b)this.b.getMoveHelper()).a_clash0(this.a, false);
      }

   }

   static class b extends EntityMoveHelper {
      private float b;
      private int c;
      private final WildSlimeEntity d;
      private boolean a;

      public b(WildSlimeEntity var1) {
         super(var1);
         this.d = var1;
         this.b = 180.0F * var1.rotationYaw / (float) Math.PI;
      }

      public void a_clash0(float var1, boolean var2) {
         this.b = var1;
         this.a = var2;
      }

      public void a_clash1(double var1) {
         this.speed = var1;
         this.action = Action.MOVE_TO;
      }

      public void onUpdateMoveHelper() {
         this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, this.b, 90.0F);
         this.entity.rotationYawHead = this.entity.rotationYaw;
         this.entity.renderYawOffset = this.entity.rotationYaw;
         if (this.action != Action.MOVE_TO) {
            this.entity.setMoveForward(0.0F);
         } else {
            this.action = Action.WAIT;
            if (this.entity.onGround) {
               this.entity
                  .setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
               if (this.c-- <= 0) {
                  this.c = this.d.a_clash99();
                  if (this.a) {
                     this.c /= 3;
                  }

                  float var1 = Reference.f.nextInt(360);
                  ((WildSlimeEntity.b)this.d.getMoveHelper()).a_clash0(var1, false);
                  this.d.getJumpHelper().setJumping();
                  if (this.d.i_clash102()) {
                     this.d
                        .playSound(
                           this.d.c_clash103(),
                           this.d.getSoundVolume(),
                           ((this.d.getRNG().nextFloat() - this.d.getRNG().nextFloat()) * 0.2F + 1.0F) * 0.8F
                        );
                  }
               } else {
                  this.d.moveStrafing = 0.0F;
                  this.d.moveForward = 0.0F;
                  this.entity.setAIMoveSpeed(0.0F);
               }
            } else {
               this.entity
                  .setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            }
         }
      }

   }

   static class c extends EntityAIBase {
      private final WildSlimeEntity a;

      public c(WildSlimeEntity var1) {
         this.a = var1;
         this.setMutexBits(5);
      }

      public boolean shouldExecute() {
         return true;
      }

      public void updateTask() {
         ((WildSlimeEntity.b)this.a.getMoveHelper()).a_clash1(1.0);
      }
   }

   static class d extends EntityAIBase {
      private final WildSlimeEntity a;

      public d(WildSlimeEntity var1) {
         this.a = var1;
         this.setMutexBits(5);
         ((PathNavigateGround)var1.getNavigator()).setCanSwim(true);
      }

      public boolean shouldExecute() {
         return this.a.isInWater() || this.a.isInLava();
      }

      public void updateTask() {
         if (this.a.getRNG().nextFloat() < 0.8F) {
            this.a.getJumpHelper().setJumping();
         }

         ((WildSlimeEntity.b)this.a.getMoveHelper()).a_clash1(1.2);
      }

   }
}
