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

/**
 * <b>Role.</b> The wild slime — the mob a pregnant {@link SlimeEntity} births
 * after its horny countdown. It hops around with vanilla slime AI (jump/
 * wander/float helpers), grows over 8400 ticks, and then matures back into a
 * Slime girl at its position. Tracked in the static {@link #ALL_SLIMES} list
 * used by {@link #findSlimesNear(Vec3d)} (e.g. for egg-laying checks).
 * <p>
 * <b>Pitfalls.</b> {@code SIZE} (110) and {@code AGE_IN_TICKS} (111) are the
 * only data keys. Dying splits it into two smaller slimes (vanilla behavior,
 * see {@link #setDead()}). Keep the maturation timing (8400) in sync with the
 * Slime pregnancy lifecycle.
 */
public class WildSlimeEntity extends EntityLiving {
   public static List<WildSlimeEntity> ALL_SLIMES = new ArrayList<>();
   private static final DataParameter<Integer> AGE_IN_TICKS = EntityDataManager.createKey(WildSlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   private static final DataParameter<Integer> SIZE = EntityDataManager.createKey(WildSlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(110);
   public float squishAmount;
   public float squishFactor;
   public float prevSquishFactor;
   private boolean wasOnGround;

   public WildSlimeEntity(World world) {
      super(world);
      this.moveHelper = new WildSlimeEntity.SlimeMoveHelper(this);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new WildSlimeEntity.SlimeWanderAI(this));
      this.tasks.addTask(5, new WildSlimeEntity.SlimeJumpAI(this));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SIZE, 1);
      this.dataManager.register(AGE_IN_TICKS, 0);
   }

   public void fall(float distance, float multiplier) {
   }

   protected boolean canDespawn() {
      return false;
   }

   protected void setSlimeSize(int size, boolean flag) {
      this.dataManager.set(SIZE, size);
      this.setSize(0.51000005F * size, 0.51000005F * size);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(size * size);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F + 0.1F * size);
      if (flag) {
         this.setHealth(this.getMaxHealth());
      }

      this.experienceValue = size;
   }

   public int getSquishFactor() {
      return (Integer)this.dataManager.get(SIZE);
   }

   public static void registerFixes(DataFixer fixer) {
      EntityLiving.registerFixesMob(fixer, WildSlimeEntity.class);
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setInteger("Size", this.getSquishFactor() - 1);
      nbt.setBoolean("wasOnGround", this.wasOnGround);
      nbt.setInteger("ageInTicks", (Integer)this.dataManager.get(AGE_IN_TICKS));
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      int size = nbt.getInteger("Size");
      if (size < 0) {
         size = 0;
      }

      this.setSlimeSize(size + 1, false);
      this.wasOnGround = nbt.getBoolean("wasOnGround");
      this.dataManager.set(AGE_IN_TICKS, nbt.getInteger("ageInTicks"));
   }

   public boolean isSmallSlime() {
      return this.getSquishFactor() <= 1;
   }

   protected EnumParticleTypes getParticleType() {
      return EnumParticleTypes.SLIME;
   }

   public static ArrayList<WildSlimeEntity> findSlimesNear(Vec3d pos) {
      ArrayList slimes = findSlimesNearRadius(pos, 0.1);
      if (slimes.isEmpty()) {
         slimes = findSlimesNearRadius(pos, 0.5);
      }

      return slimes;
   }

   private static ArrayList<WildSlimeEntity> findSlimesNearRadius(Vec3d pos, double radius) {
      ArrayList found = new ArrayList();

      try {
         for (WildSlimeEntity slime : ALL_SLIMES) {
            if (slime != null) {
               double dist = Math.abs(slime.prevPosX - pos.x)
                  + Math.abs(slime.prevPosY - pos.y)
                  + Math.abs(slime.prevPosZ - pos.z);
               if (slime.world != null && dist < radius) {
                  found.add(slime);
               }
            }
         }
      } catch (Exception ex) {
         System.out.println("couldnt find slimes at distance " + radius);
      }

      return found;
   }

   public Vec3d getPrevPosition() {
      return new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ);
   }

   void spawnParticle(EnumParticleTypes particleType) {
      double vx = Reference.RANDOM.nextGaussian() * 0.02;
      double vy = Reference.RANDOM.nextGaussian() * 0.02;
      double vz = Reference.RANDOM.nextGaussian() * 0.02;
      this.world
         .spawnParticle(
            particleType,
            this.posX + Reference.RANDOM.nextFloat() * this.width * 2.0F - this.width,
            this.posY + 0.15 + Reference.RANDOM.nextFloat() * this.height,
            this.posZ + Reference.RANDOM.nextFloat() * this.width * 2.0F - this.width,
            vx,
            vy,
            vz,
            new int[0]
         );
   }

   /**
    * SERVER/CLIENT: the maturation lifecycle — ages every tick; CLIENT spawns
    * cloud (7980+) and happy (5880+) particles, SERVER converts to a
    * {@link SlimeEntity} at 8400 ticks. Also runs the vanilla squish
    * animation states.
    */
   public void onUpdate() {
      this.dataManager.set(AGE_IN_TICKS, (Integer)this.dataManager.get(AGE_IN_TICKS) + 1);
      if (this.world.isRemote) {
         if (((Integer)this.dataManager.get(AGE_IN_TICKS)).intValue() > 7980.0) {
            this.spawnParticle(EnumParticleTypes.CLOUD);
         } else if (((Integer)this.dataManager.get(AGE_IN_TICKS)).intValue() > 5880.0 && this.ticksExisted % 10 == 0) {
            this.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY);
         }
      } else if ((Integer)this.dataManager.get(AGE_IN_TICKS) > 8400) {
         SlimeEntity slime = new SlimeEntity(this.world);
         slime.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         this.world.spawnEntity(slime);
         slime.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
         this.world.removeEntity(this);
      }

      this.squishFactor = this.squishFactor + (this.squishAmount - this.squishFactor) * 0.5F;
      this.prevSquishFactor = this.squishFactor;
      super.onUpdate();
      if (this.onGround && !this.wasOnGround) {
         int squish = this.getSquishFactor();
         if (this.canDrop()) {
            squish = 0;
         }

         for (int i = 0; i < squish * 8; i++) {
            float theta = this.rand.nextFloat() * (float) (Math.PI * 2);
            float scale = this.rand.nextFloat() * 0.5F + 0.5F;
            float xOffset = MathHelper.sin(theta) * squish * 0.5F * scale;
            float zOffset = MathHelper.cos(theta) * squish * 0.5F * scale;
            World world = this.world;
            EnumParticleTypes particleType = this.getParticleType();
            double x = this.posX + xOffset;
            double z = this.posZ + zOffset;
            world.spawnParticle(particleType, x, this.getEntityBoundingBox().minY, z, 0.0, 0.0, 0.0, new int[0]);
         }

         this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         this.squishAmount = -0.5F;
      } else if (!this.onGround && this.wasOnGround) {
         this.squishAmount = 1.0F;
      }

      this.wasOnGround = this.onGround;
      this.decaySquish();
   }

   protected void decaySquish() {
      this.squishAmount *= 0.6F;
   }

   protected int getRandomDespawnDelay() {
      return this.rand.nextInt(100) + 50;
   }

   protected WildSlimeEntity createChild() {
      return new WildSlimeEntity(this.world);
   }

   public void notifyDataManagerChange(DataParameter<?> key) {
      if (SIZE.equals(key)) {
         int squish = this.getSquishFactor();
         this.setSize(0.51000005F * squish, 0.51000005F * squish);
         this.rotationYaw = this.rotationYawHead;
         this.renderYawOffset = this.rotationYawHead;
         if (this.isInWater() && this.rand.nextInt(20) == 0) {
            this.doWaterSplashEffect();
         }
      }

      super.notifyDataManagerChange(key);
   }

   public void setDead() {
      int squish = this.getSquishFactor();
      if (!this.world.isRemote && squish > 1 && this.getHealth() <= 0.0F) {
         int count = 2 + this.rand.nextInt(3);

         for (int i = 0; i < count; i++) {
            float xOffset = (i % 2 - 0.5F) * squish / 4.0F;
            float zOffset = (i / 2 - 0.5F) * squish / 4.0F;
            WildSlimeEntity child = this.createChild();
            if (this.hasCustomName()) {
               child.setCustomNameTag(this.getCustomNameTag());
            }

            if (this.isNoDespawnRequired()) {
               child.enablePersistence();
            }

            child.setSlimeSize(squish / 2, true);
            child.setLocationAndAngles(this.posX + xOffset, this.posY + 0.5, this.posZ + zOffset, this.rand.nextFloat() * 360.0F, 0.0F);
            this.world.spawnEntity(child);
         }
      }

      super.setDead();
   }

   public float getEyeHeight() {
      return 0.625F * this.height;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_HURT : SoundEvents.ENTITY_SLIME_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_DEATH : SoundEvents.ENTITY_SLIME_DEATH;
   }

   protected SoundEvent getSquishSound() {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_SQUISH : SoundEvents.ENTITY_SLIME_SQUISH;
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

   protected boolean canSquish() {
      return this.getSquishFactor() > 0;
   }

   protected void jump() {
      this.motionY = 0.42F;
      this.isAirBorne = true;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingData) {
      this.setSlimeSize(1, true);
      return super.onInitialSpawn(difficulty, livingData);
   }

   protected SoundEvent getJumpSound() {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_JUMP : SoundEvents.ENTITY_SLIME_JUMP;
   }

   protected boolean canDrop() {
      return false;
   }

   static class SlimeFloatAI extends EntityAIBase {
      private final WildSlimeEntity ownerSlime;
      private float squishAngle;
      private int floatDelay;

      public SlimeFloatAI(WildSlimeEntity slime) {
         this.ownerSlime = slime;
         this.setMutexBits(2);
      }

      public boolean shouldExecute() {
         return this.ownerSlime.getAttackTarget() == null
            && (this.ownerSlime.onGround || this.ownerSlime.isInWater() || this.ownerSlime.isInLava() || this.ownerSlime.isPotionActive(MobEffects.LEVITATION));
      }

      public void updateTask() {
         if (--this.floatDelay <= 0) {
            this.floatDelay = 40 + this.ownerSlime.getRNG().nextInt(60);
            this.squishAngle = this.ownerSlime.getRNG().nextInt(360);
         }

         ((WildSlimeEntity.SlimeMoveHelper)this.ownerSlime.getMoveHelper()).setMoveHelperTarget(this.squishAngle, false);
      }

   }

   static class SlimeMoveHelper extends EntityMoveHelper {
      private float rotationYaw;
      private int squishDelay;
      private final WildSlimeEntity slime;
      private boolean wasOnGround;

      public SlimeMoveHelper(WildSlimeEntity slime) {
         super(slime);
         this.slime = slime;
         this.rotationYaw = 180.0F * slime.rotationYaw / (float) Math.PI;
      }

      public void setMoveHelperTarget(float yaw, boolean onGround) {
         this.rotationYaw = yaw;
         this.wasOnGround = onGround;
      }

      public void setMoveHelperSpeed(double speed) {
         this.speed = speed;
         this.action = Action.MOVE_TO;
      }

      public void onUpdateMoveHelper() {
         this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, this.rotationYaw, 90.0F);
         this.entity.rotationYawHead = this.entity.rotationYaw;
         this.entity.renderYawOffset = this.entity.rotationYaw;
         if (this.action != Action.MOVE_TO) {
            this.entity.setMoveForward(0.0F);
         } else {
            this.action = Action.WAIT;
            if (this.entity.onGround) {
               this.entity
                  .setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
               if (this.squishDelay-- <= 0) {
                  this.squishDelay = this.slime.getRandomDespawnDelay();
                  if (this.wasOnGround) {
                     this.squishDelay /= 3;
                  }

                  float yaw = Reference.RANDOM.nextInt(360);
                  ((WildSlimeEntity.SlimeMoveHelper)this.slime.getMoveHelper()).setMoveHelperTarget(yaw, false);
                  this.slime.getJumpHelper().setJumping();
                  if (this.slime.canSquish()) {
                     this.slime
                        .playSound(
                           this.slime.getJumpSound(),
                           this.slime.getSoundVolume(),
                           ((this.slime.getRNG().nextFloat() - this.slime.getRNG().nextFloat()) * 0.2F + 1.0F) * 0.8F
                        );
                  }
               } else {
                  this.slime.moveStrafing = 0.0F;
                  this.slime.moveForward = 0.0F;
                  this.entity.setAIMoveSpeed(0.0F);
               }
            } else {
               this.entity
                  .setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            }
         }
      }

   }

   static class SlimeJumpAI extends EntityAIBase {
      private final WildSlimeEntity squishAmount;

      public SlimeJumpAI(WildSlimeEntity slime) {
         this.squishAmount = slime;
         this.setMutexBits(5);
      }

      public boolean shouldExecute() {
         return true;
      }

      public void updateTask() {
         ((WildSlimeEntity.SlimeMoveHelper)this.squishAmount.getMoveHelper()).setMoveHelperSpeed(1.0);
      }
   }

   static class SlimeWanderAI extends EntityAIBase {
      private final WildSlimeEntity squishAmount;

      public SlimeWanderAI(WildSlimeEntity slime) {
         this.squishAmount = slime;
         this.setMutexBits(5);
         ((PathNavigateGround)slime.getNavigator()).setCanSwim(true);
      }

      public boolean shouldExecute() {
         return this.squishAmount.isInWater() || this.squishAmount.isInLava();
      }

      public void updateTask() {
         if (this.squishAmount.getRNG().nextFloat() < 0.8F) {
            this.squishAmount.getJumpHelper().setJumping();
         }

         ((WildSlimeEntity.SlimeMoveHelper)this.squishAmount.getMoveHelper()).setMoveHelperSpeed(1.2);
      }

   }
}
