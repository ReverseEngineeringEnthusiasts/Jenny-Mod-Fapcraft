package com.trolmastercard.sexmod.entity;

import com.google.common.base.Optional;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> The "luna hook" — Luna's fishing-rod bobber entity
 * ({@link LunaEntity#av}). Vanilla fishing-fight simulation: flies from the
 * rod, hooks entities or the ground, bobs in water, rolls loot
 * ({@link LootTableList#GAMEPLAY_FISHING}) and hands the catch to Luna.
 * <p>
 * <b>State.</b> Data keys 111 ({@code CAUGHT_ENTITY_ID}, entity id + 1,
 * 0 = none) and 110 ({@code OWNER_UUID} = Luna's girl id). {@code hookState}
 * cycles FLYING -&gt; HOOKED_IN_ENTITY/BOBBING; {@code caughtEntity} is
 * mirrored from the data key via {@link #notifyDataManagerChange(DataParameter)}.
 * <p>
 * <b>Pitfalls.</b> {@link #canCatch()} always returns false — the catch check
 * runs from {@link #checkCatch()} in the FLYING state instead. The hook
 * despawns 1200 ticks after being hooked. NBT is intentionally empty
 * ({@code writeEntityToNBT}/{@code readEntityFromNBT} no-ops) — the hook
 * never persists. {@link #getCatchResult()} resolves the catch outcome on the
 * SERVER (entity -&gt; 5/3, lure -&gt; 1, hooked -&gt; 2) and is the link back
 * into Luna's catch UI.
 */
public class SexEntity extends Entity {
   public static final int MAX_HOOK_RANGE = 15;
   private static final DataParameter<Integer> CAUGHT_ENTITY_ID = EntityDataManager.createKey(SexEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   private static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.createKey(SexEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID)
      .getSerializer()
      .createKey(110);
   private boolean isHooked;
   private int despawnTimer;
   private int waterBobCounter;
   public int lureTimer;
   private int catchDelay;
   private int bobMotion;
   private float bobAngle;
   public Entity caughtEntity;
   private SexEntity.SexEntityState hookState = SexEntity.SexEntityState.FLYING;
   private int phase;
   private int fishingLevel;
   public static LunaEntity ownerLuna = null;

   public SexEntity(World world, LunaEntity luna, double height) {
      super(world);
      this.setOwnerLuna(luna);
      this.positionLunaAbove(height);
   }

   public SexEntity(World world) {
      super(world);
   }

   private void setOwnerLuna(LunaEntity luna) {
      this.setSize(0.25F, 0.25F);
      this.ignoreFrustumCheck = true;
      luna.av = this;
   }

   protected void entityInit() {
      this.getDataManager().register(CAUGHT_ENTITY_ID, 0);
      this.getDataManager().register(OWNER_UUID, Optional.of(ownerLuna.getGirlId()));
   }

   public AxisAlignedBB getRenderBoundingBox() {
      return this.getEntityBoundingBox().grow(10.0);
   }

   LunaEntity getOwnerLunaInternal() {
      Optional uuidOpt = (Optional)this.dataManager.get(OWNER_UUID);
      if (!uuidOpt.isPresent()) {
         return null;
      } else {
         BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity((UUID)uuidOpt.get());
         if (girl == null) {
            return null;
         } else {
            return !(girl instanceof LunaEntity) ? null : (LunaEntity)girl;
         }
      }
   }

   public LunaEntity getOwnerLuna() {
      Optional uuidOpt = (Optional)this.dataManager.get(OWNER_UUID);
      if (!uuidOpt.isPresent()) {
         return null;
      }

      BaseGirlEntity girl = BaseGirlEntity.getClientGirlEntity((UUID)uuidOpt.get());
      return !(girl instanceof LunaEntity) ? null : (LunaEntity)girl;
   }

   public void setFishingLevel(int level) {
      this.fishingLevel = level;
   }

   public void setPhase(int phase) {
      this.phase = phase;
   }

   public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
         if ((this.caughtEntity != null || this.onGround) && this.lureTimer == 0) {
            this.getOwnerLunaInternal().addCaughtItem();
         }
      }
   }

   public void positionLunaAbove(double height) {
      LunaEntity luna = this.getOwnerLunaInternal();
      if (luna != null) {
         BlockPos anchorPos = luna.ai;
         float dist = (float)Math.sqrt(luna.getPositionVector().squareDistanceTo(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ()));
         float angle = -22.5F + 45.0F * (dist / 7.0F);
         float yaw = luna.getYawRotation();
         float cosYaw = MathHelper.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
         float sinYaw = MathHelper.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
         float cosTilt = -MathHelper.cos(-angle * (float) (Math.PI / 180.0));
         float sinTilt = MathHelper.sin(-angle * (float) (Math.PI / 180.0));
         double interpX = luna.prevPosX + (luna.posX - luna.prevPosX) - sinYaw * 0.3;
         double interpY = luna.prevPosY + (luna.posY - luna.prevPosY) + luna.getEyeHeight();
         double interpZ = luna.prevPosZ + (luna.posZ - luna.prevPosZ) - cosYaw * 0.3;
         this.setLocationAndAngles(interpX, interpY, interpZ, yaw, angle);
         this.motionX = height * -sinYaw;
         this.motionY = height * MathHelper.clamp(-(sinTilt / cosTilt), -5.0F, 5.0F);
         this.motionZ = height * -cosYaw;
         float speed = MathHelper.sqrt(
            this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ
         );
         this.motionX = this.motionX * (0.6 / speed + 0.5 + this.rand.nextGaussian() * 0.0045);
         this.motionY = this.motionY * (0.6 / speed + 0.5 + this.rand.nextGaussian() * 0.0045);
         this.motionZ = this.motionZ * (0.6 / speed + 0.5 + this.rand.nextGaussian() * 0.0045);
         float horizontalSpeed = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180.0 / Math.PI));
         this.rotationPitch = (float)(MathHelper.atan2(this.motionY, horizontalSpeed) * (180.0 / Math.PI));
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }
   }

   public void notifyDataManagerChange(DataParameter<?> key) {
      if (CAUGHT_ENTITY_ID.equals(key)) {
         int caughtId = (Integer)this.getDataManager().get(CAUGHT_ENTITY_ID);
         this.caughtEntity = caughtId > 0 ? this.world.getEntityByID(caughtId - 1) : null;
      }

      super.notifyDataManagerChange(key);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double dist) {
      return dist < 4096.0;
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
   }

   /**
    * SERVER: the fishing flight/bob simulation — flies while FLYING (catch
    * checks, water bob transition, hook despawn timer), rides the caught
    * entity when HOOKED_IN_ENTITY, and bobs + spawns loot when BOBBING.
    */
   public void onUpdate() {
      super.onUpdate();
      if (this.getOwnerLunaInternal() == null) {
         this.setDead();
      } else if (this.world.isRemote || !this.canCatch()) {
         if (this.isHooked) {
            this.despawnTimer++;
            if (this.despawnTimer >= 1200) {
               this.setDead();
               return;
            }
         }

         float liquidHeight = 0.0F;
         BlockPos pos = new BlockPos(this);
         IBlockState state = this.world.getBlockState(pos);
         if (state.getMaterial() == Material.WATER) {
            liquidHeight = BlockLiquid.getBlockLiquidHeight(state, this.world, pos);
         }

         if (this.hookState == SexEntity.SexEntityState.FLYING) {
            if (this.caughtEntity != null) {
               this.motionX = 0.0;
               this.motionY = 0.0;
               this.motionZ = 0.0;
               this.hookState = SexEntity.SexEntityState.HOOKED_IN_ENTITY;
               return;
            }

            if (liquidHeight > 0.0F) {
               this.motionX *= 0.3;
               this.motionY *= 0.2;
               this.motionZ *= 0.3;
               this.hookState = SexEntity.SexEntityState.BOBBING;
               return;
            }

            if (!this.world.isRemote) {
               this.checkCatch();
            }

            if (!this.isHooked && !this.onGround && !this.collidedHorizontally) {
               this.waterBobCounter++;
            } else {
               this.waterBobCounter = 0;
               this.motionX = 0.0;
               this.motionY = 0.0;
               this.motionZ = 0.0;
            }
         } else {
            if (this.hookState == SexEntity.SexEntityState.HOOKED_IN_ENTITY) {
               if (this.caughtEntity != null) {
                  if (this.caughtEntity.isDead) {
                     this.caughtEntity = null;
                     this.hookState = SexEntity.SexEntityState.FLYING;
                  } else {
                     this.posX = this.caughtEntity.posX;
                     double height = this.caughtEntity.height;
                     this.posY = this.caughtEntity.getEntityBoundingBox().minY + height * 0.8;
                     this.posZ = this.caughtEntity.posZ;
                     this.setPosition(this.posX, this.posY, this.posZ);
                  }
               }

               return;
            }

            if (this.hookState == SexEntity.SexEntityState.BOBBING) {
               this.motionX *= 0.9;
               this.motionZ *= 0.9;
               double deltaY = this.posY + this.motionY - pos.getY() - liquidHeight;
               if (Math.abs(deltaY) < 0.01) {
                  deltaY += Math.signum(deltaY) * 0.1;
               }

               this.motionY = this.motionY - deltaY * this.rand.nextFloat() * 0.2;
               if (!this.world.isRemote && liquidHeight > 0.0F) {
                  this.spawnLootBlocks(pos);
               }
            }
         }

         if (state.getMaterial() != Material.WATER) {
            this.motionY -= 0.03;
         }

         this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         this.updateVelocity();
         this.motionX *= 0.92;
         this.motionY *= 0.92;
         this.motionZ *= 0.92;
         this.setPosition(this.posX, this.posY, this.posZ);
      }
   }

   private boolean canCatch() {
      return false;
   }

   private void updateVelocity() {
      float horizontalSpeed = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180.0 / Math.PI));
      this.rotationPitch = (float)(MathHelper.atan2(this.motionY, horizontalSpeed) * (180.0 / Math.PI));

      while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
         this.prevRotationPitch -= 360.0F;
      }

      while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
         this.prevRotationPitch += 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
         this.prevRotationYaw -= 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
         this.prevRotationYaw += 360.0F;
      }

      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
   }

   /**
    * SERVER: ray-traces the hook's motion and locks onto the nearest
    * collidable entity (or the ground) ahead of it.
    */
   private void checkCatch() {
      Vec3d start = new Vec3d(this.posX, this.posY, this.posZ);
      Vec3d end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      RayTraceResult rayTrace = this.world.rayTraceBlocks(start, end, false, true, false);
      start = new Vec3d(this.posX, this.posY, this.posZ);
      end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      if (rayTrace != null) {
         end = new Vec3d(rayTrace.hitVec.x, rayTrace.hitVec.y, rayTrace.hitVec.z);
      }

      Entity target = null;
      List entities = this.world
         .getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0));
      double closestDist = 0.0;

      for (Entity entity : (java.util.Collection<Entity>) (entities) ) {
         if (this.isCollidableEntity(entity) && (entity != this.getOwnerLunaInternal() || this.waterBobCounter >= 5)) {
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(0.3F);
            RayTraceResult entityRayTrace = aabb.calculateIntercept(start, end);
            if (entityRayTrace != null) {
               double distSq = start.squareDistanceTo(entityRayTrace.hitVec);
               if (distSq < closestDist || closestDist == 0.0) {
                  target = entity;
                  closestDist = distSq;
               }
            }
         }
      }

      if (target != null) {
         rayTrace = new RayTraceResult(target);
      }

      if (rayTrace != null && rayTrace.typeOfHit != Type.MISS) {
         if (rayTrace.typeOfHit == Type.ENTITY) {
            this.caughtEntity = rayTrace.entityHit;
            this.bindTargetEntity();
         } else {
            this.isHooked = true;
         }
      }
   }

   private void bindTargetEntity() {
      this.getDataManager().set(CAUGHT_ENTITY_ID, this.caughtEntity.getEntityId() + 1);
   }

   /**
    * SERVER: the bobbing fight — lure timer, bubble/wake particles, splash
    * sounds and the randomized catch-delay chain (scaled by the fishing
    * level). Vanilla bobber mechanics, adapted for Luna.
    */
   private void spawnLootBlocks(BlockPos centerPos) {
      WorldServer worldServer = (WorldServer)this.world;
      int lootCount = 1;
      BlockPos pos = centerPos.up();
      if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(pos)) {
         lootCount++;
      }

      if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(pos)) {
         lootCount--;
      }

      if (this.lureTimer > 0) {
         this.lureTimer--;
         if (this.lureTimer <= 0) {
            this.catchDelay = 0;
            this.bobMotion = 0;
         } else {
            this.motionY = this.motionY - 0.2 * this.rand.nextFloat() * this.rand.nextFloat();
         }
      } else if (this.bobMotion > 0) {
         this.bobMotion -= lootCount;
         if (this.bobMotion > 0) {
            this.bobAngle = (float)(this.bobAngle + this.rand.nextGaussian() * 4.0);
            float angle = this.bobAngle * (float) (Math.PI / 180.0);
            float sinAngle = MathHelper.sin(angle);
            float cosAngle = MathHelper.cos(angle);
            double x = this.posX + sinAngle * this.bobMotion * 0.1F;
            double y = MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F;
            double z = this.posZ + cosAngle * this.bobMotion * 0.1F;
            IBlockState state = worldServer.getBlockState(new BlockPos(x, y - 1.0, z));
            if (state.getMaterial() == Material.WATER) {
               if (this.rand.nextFloat() < 0.15F) {
                  worldServer.spawnParticle(EnumParticleTypes.WATER_BUBBLE, x, y - 0.1F, z, 1, sinAngle, 0.1, cosAngle, 0.0, new int[0]);
               }

               float xVel = sinAngle * 0.04F;
               float zVel = cosAngle * 0.04F;
               worldServer.spawnParticle(EnumParticleTypes.WATER_WAKE, x, y, z, 0, zVel, 0.01, -xVel, 1.0, new int[0]);
               worldServer.spawnParticle(EnumParticleTypes.WATER_WAKE, x, y, z, 0, -zVel, 0.01, xVel, 1.0, new int[0]);
            }
         } else {
            this.motionY = -0.4F * MathHelper.nextFloat(this.rand, 0.6F, 1.0F);
            this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
            double y = this.getEntityBoundingBox().minY + 0.5;
            worldServer.spawnParticle(
               EnumParticleTypes.WATER_BUBBLE,
               this.posX,
               y,
               this.posZ,
               (int)(1.0F + this.width * 20.0F),
               this.width,
               0.0,
               this.width,
               0.2F,
               new int[0]
            );
            worldServer.spawnParticle(
               EnumParticleTypes.WATER_WAKE,
               this.posX,
               y,
               this.posZ,
               (int)(1.0F + this.width * 20.0F),
               this.width,
               0.0,
               this.width,
               0.2F,
               new int[0]
            );
            this.lureTimer = MathHelper.getInt(this.rand, 20, 40);
         }
      } else if (this.catchDelay > 0) {
         this.catchDelay -= lootCount;
         float radius = 0.15F;
         if (this.catchDelay < 20) {
            radius = (float)(0.15F + (20 - this.catchDelay) * 0.05);
         } else if (this.catchDelay < 40) {
            radius = (float)(0.15F + (40 - this.catchDelay) * 0.02);
         } else if (this.catchDelay < 60) {
            radius = (float)(0.15F + (60 - this.catchDelay) * 0.01);
         }

         if (this.rand.nextFloat() < radius) {
            float angle = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
            float spread = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
            double x = this.posX + MathHelper.sin(angle) * spread * 0.1F;
            double y = MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F;
            double z = this.posZ + MathHelper.cos(angle) * spread * 0.1F;
            IBlockState state = worldServer.getBlockState(new BlockPos((int)x, (int)y - 1, (int)z));
            if (state.getMaterial() == Material.WATER) {
               worldServer.spawnParticle(EnumParticleTypes.WATER_SPLASH, x, y, z, 2 + this.rand.nextInt(2), 0.1F, 0.0, 0.1F, 0.0, new int[0]);
            }
         }

         if (this.catchDelay <= 0) {
            this.bobAngle = MathHelper.nextFloat(this.rand, 0.0F, 360.0F);
            this.bobMotion = MathHelper.getInt(this.rand, 20, 80);
         }
      } else {
         this.catchDelay = MathHelper.getInt(this.rand, 100, 600);
         this.catchDelay = this.catchDelay - this.fishingLevel * 20 * 5;
      }
   }

   protected boolean isCollidableEntity(Entity entity) {
      return entity.canBeCollidedWith() || entity instanceof EntityItem;
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
   }

   /**
    * SERVER: resolves the catch — converts the caught entity into its result
    * code (entity item 3 / entity 5 via {@link #handleCatch()}), rolls a
    * fishing loot table into Luna's held stack ({@code lureTimer = 9999}) or
    * reports the hooked state (2). 0 = nothing.
    */
   public int getCatchResult() {
      if (!this.world.isRemote && this.getOwnerLunaInternal() != null) {
         byte result = 0;
         if (this.caughtEntity != null) {
            this.handleCatch();
            this.world.setEntityState(this, (byte)31);
            result = (byte)(this.caughtEntity instanceof EntityItem ? 3 : 5);
         } else if (this.lureTimer > 0) {
            Builder lootBuilder = new Builder((WorldServer)this.world);

            for (ItemStack stack : this.world
               .getLootTableManager()
               .getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING)
               .generateLootForPools(this.rand, lootBuilder.build())) {
               LunaEntity luna = this.getOwnerLunaInternal();
               luna.setHeldItemStack(stack);
            }

            this.lureTimer = 9999;
            result = 1;
         }

         if (this.isHooked) {
            result = 2;
         }

         return result;
      } else {
         return 0;
      }
   }

   protected void handleCatch() {
      LunaEntity luna = this.getOwnerLunaInternal();
      if (luna != null) {
         double dx = luna.posX - this.posX;
         double dy = luna.posY - this.posY;
         double dz = luna.posZ - this.posZ;
         this.caughtEntity.motionX += dx * 0.1;
         this.caughtEntity.motionY += dy * 0.1;
         this.caughtEntity.motionZ += dz * 0.1;
      }
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public void readFromNBT(NBTTagCompound nbt) {
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      return null;
   }

   enum SexEntityState {
      FLYING,
      HOOKED_IN_ENTITY,
      BOBBING;
   }
}
