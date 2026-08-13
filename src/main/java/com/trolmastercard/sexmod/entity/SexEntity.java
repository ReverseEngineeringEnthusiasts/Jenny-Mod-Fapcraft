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

public class SexEntity extends Entity {
   public static final int m = 15;
   private static final DataParameter<Integer> g = EntityDataManager.createKey(SexEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   private static final DataParameter<Optional<UUID>> f = EntityDataManager.createKey(SexEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID)
      .getSerializer()
      .createKey(110);
   private boolean k;
   private int l;
   private int h;
   public int d;
   private int c;
   private int j;
   private float e;
   public Entity i;
   private SexEntity.SexEntityState n = SexEntity.SexEntityState.FLYING;
   private int a;
   private int o;
   public static LunaEntity b = null;

   public SexEntity(World var1, LunaEntity var2, double var3) {
      super(var1);
      this.a(var2);
      this.positionLunaAbove(var3);
   }

   public SexEntity(World var1) {
      super(var1);
   }

   private void a(LunaEntity var1) {
      this.setSize(0.25F, 0.25F);
      this.ignoreFrustumCheck = true;
      var1.av = this;
   }

   protected void entityInit() {
      this.getDataManager().register(g, 0);
      this.getDataManager().register(f, Optional.of(b.getGirlId()));
   }

   public AxisAlignedBB getRenderBoundingBox() {
      return this.getEntityBoundingBox().grow(10.0);
   }

   LunaEntity b_clash775() {
      Optional var1 = (Optional)this.dataManager.get(f);
      if (!var1.isPresent()) {
         return null;
      } else {
         BaseGirlEntity var2 = BaseGirlEntity.getServerGirlEntity((UUID)var1.get());
         if (var2 == null) {
            return null;
         } else {
            return !(var2 instanceof LunaEntity) ? null : (LunaEntity)var2;
         }
      }
   }

   public LunaEntity g_clash776() {
      Optional var1 = (Optional)this.dataManager.get(f);
      if (!var1.isPresent()) {
         return null;
      }

      BaseGirlEntity var2 = BaseGirlEntity.getClientGirlEntity((UUID)var1.get());
      return !(var2 instanceof LunaEntity) ? null : (LunaEntity)var2;
   }

   public void b_clash777(int var1) {
      this.o = var1;
   }

   public void setPhase(int var1) {
      this.a = var1;
   }

   public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
         if ((this.i != null || this.onGround) && this.d == 0) {
            this.b_clash775().o_clash390();
         }
      }
   }

   public void positionLunaAbove(double var1) {
      LunaEntity var3 = this.b_clash775();
      if (var3 != null) {
         BlockPos var4 = var3.ai;
         float var5 = (float)Math.sqrt(var3.getPositionVector().squareDistanceTo(var4.getX(), var4.getY(), var4.getZ()));
         float var6 = -22.5F + 45.0F * (var5 / 7.0F);
         float var7 = var3.getYawRotation();
         float var8 = MathHelper.cos(-var7 * (float) (Math.PI / 180.0) - (float) Math.PI);
         float var9 = MathHelper.sin(-var7 * (float) (Math.PI / 180.0) - (float) Math.PI);
         float var10 = -MathHelper.cos(-var6 * (float) (Math.PI / 180.0));
         float var11 = MathHelper.sin(-var6 * (float) (Math.PI / 180.0));
         double var12 = var3.prevPosX + (var3.posX - var3.prevPosX) - var9 * 0.3;
         double var14 = var3.prevPosY + (var3.posY - var3.prevPosY) + var3.getEyeHeight();
         double var16 = var3.prevPosZ + (var3.posZ - var3.prevPosZ) - var8 * 0.3;
         this.setLocationAndAngles(var12, var14, var16, var7, var6);
         this.motionX = var1 * -var9;
         this.motionY = var1 * MathHelper.clamp(-(var11 / var10), -5.0F, 5.0F);
         this.motionZ = var1 * -var8;
         float var18 = MathHelper.sqrt(
            this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ
         );
         this.motionX = this.motionX * (0.6 / var18 + 0.5 + this.rand.nextGaussian() * 0.0045);
         this.motionY = this.motionY * (0.6 / var18 + 0.5 + this.rand.nextGaussian() * 0.0045);
         this.motionZ = this.motionZ * (0.6 / var18 + 0.5 + this.rand.nextGaussian() * 0.0045);
         float var19 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180.0 / Math.PI));
         this.rotationPitch = (float)(MathHelper.atan2(this.motionY, var19) * (180.0 / Math.PI));
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }
   }

   public void notifyDataManagerChange(DataParameter<?> var1) {
      if (g.equals(var1)) {
         int var2 = (Integer)this.getDataManager().get(g);
         this.i = var2 > 0 ? this.world.getEntityByID(var2 - 1) : null;
      }

      super.notifyDataManagerChange(var1);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      return var1 < 4096.0;
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.b_clash775() == null) {
         this.setDead();
      } else if (this.world.isRemote || !this.f_clash780()) {
         if (this.k) {
            this.l++;
            if (this.l >= 1200) {
               this.setDead();
               return;
            }
         }

         float var1 = 0.0F;
         BlockPos var2 = new BlockPos(this);
         IBlockState var3 = this.world.getBlockState(var2);
         if (var3.getMaterial() == Material.WATER) {
            var1 = BlockLiquid.getBlockLiquidHeight(var3, this.world, var2);
         }

         if (this.n == SexEntity.SexEntityState.FLYING) {
            if (this.i != null) {
               this.motionX = 0.0;
               this.motionY = 0.0;
               this.motionZ = 0.0;
               this.n = SexEntity.SexEntityState.HOOKED_IN_ENTITY;
               return;
            }

            if (var1 > 0.0F) {
               this.motionX *= 0.3;
               this.motionY *= 0.2;
               this.motionZ *= 0.3;
               this.n = SexEntity.SexEntityState.BOBBING;
               return;
            }

            if (!this.world.isRemote) {
               this.e_clash782();
            }

            if (!this.k && !this.onGround && !this.collidedHorizontally) {
               this.h++;
            } else {
               this.h = 0;
               this.motionX = 0.0;
               this.motionY = 0.0;
               this.motionZ = 0.0;
            }
         } else {
            if (this.n == SexEntity.SexEntityState.HOOKED_IN_ENTITY) {
               if (this.i != null) {
                  if (this.i.isDead) {
                     this.i = null;
                     this.n = SexEntity.SexEntityState.FLYING;
                  } else {
                     this.posX = this.i.posX;
                     double var6 = this.i.height;
                     this.posY = this.i.getEntityBoundingBox().minY + var6 * 0.8;
                     this.posZ = this.i.posZ;
                     this.setPosition(this.posX, this.posY, this.posZ);
                  }
               }

               return;
            }

            if (this.n == SexEntity.SexEntityState.BOBBING) {
               this.motionX *= 0.9;
               this.motionZ *= 0.9;
               double var4 = this.posY + this.motionY - var2.getY() - var1;
               if (Math.abs(var4) < 0.01) {
                  var4 += Math.signum(var4) * 0.1;
               }

               this.motionY = this.motionY - var4 * this.rand.nextFloat() * 0.2;
               if (!this.world.isRemote && var1 > 0.0F) {
                  this.spawnLootBlocks(var2);
               }
            }
         }

         if (var3.getMaterial() != Material.WATER) {
            this.motionY -= 0.03;
         }

         this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         this.h_clash781();
         this.motionX *= 0.92;
         this.motionY *= 0.92;
         this.motionZ *= 0.92;
         this.setPosition(this.posX, this.posY, this.posZ);
      }
   }

   private boolean f_clash780() {
      return false;
   }

   private void h_clash781() {
      float var1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180.0 / Math.PI));
      this.rotationPitch = (float)(MathHelper.atan2(this.motionY, var1) * (180.0 / Math.PI));

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

   private void e_clash782() {
      Vec3d var1 = new Vec3d(this.posX, this.posY, this.posZ);
      Vec3d var2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      RayTraceResult var3 = this.world.rayTraceBlocks(var1, var2, false, true, false);
      var1 = new Vec3d(this.posX, this.posY, this.posZ);
      var2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      if (var3 != null) {
         var2 = new Vec3d(var3.hitVec.x, var3.hitVec.y, var3.hitVec.z);
      }

      Entity var4 = null;
      List var5 = this.world
         .getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0));
      double var6 = 0.0;

      for (Entity var9 : (java.util.Collection<Entity>) (var5) ) {
         if (this.isCollidableEntity(var9) && (var9 != this.b_clash775() || this.h >= 5)) {
            AxisAlignedBB var10 = var9.getEntityBoundingBox().grow(0.3F);
            RayTraceResult var11 = var10.calculateIntercept(var1, var2);
            if (var11 != null) {
               double var12 = var1.squareDistanceTo(var11.hitVec);
               if (var12 < var6 || var6 == 0.0) {
                  var4 = var9;
                  var6 = var12;
               }
            }
         }
      }

      if (var4 != null) {
         var3 = new RayTraceResult(var4);
      }

      if (var3 != null && var3.typeOfHit != Type.MISS) {
         if (var3.typeOfHit == Type.ENTITY) {
            this.i = var3.entityHit;
            this.bindTargetEntity();
         } else {
            this.k = true;
         }
      }
   }

   private void bindTargetEntity() {
      this.getDataManager().set(g, this.i.getEntityId() + 1);
   }

   private void spawnLootBlocks(BlockPos var1) {
      WorldServer var2 = (WorldServer)this.world;
      int var3 = 1;
      BlockPos var4 = var1.up();
      if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(var4)) {
         var3++;
      }

      if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(var4)) {
         var3--;
      }

      if (this.d > 0) {
         this.d--;
         if (this.d <= 0) {
            this.c = 0;
            this.j = 0;
         } else {
            this.motionY = this.motionY - 0.2 * this.rand.nextFloat() * this.rand.nextFloat();
         }
      } else if (this.j > 0) {
         this.j -= var3;
         if (this.j > 0) {
            this.e = (float)(this.e + this.rand.nextGaussian() * 4.0);
            float var5 = this.e * (float) (Math.PI / 180.0);
            float var6 = MathHelper.sin(var5);
            float var7 = MathHelper.cos(var5);
            double var8 = this.posX + var6 * this.j * 0.1F;
            double var10 = MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F;
            double var12 = this.posZ + var7 * this.j * 0.1F;
            IBlockState var14 = var2.getBlockState(new BlockPos(var8, var10 - 1.0, var12));
            if (var14.getMaterial() == Material.WATER) {
               if (this.rand.nextFloat() < 0.15F) {
                  var2.spawnParticle(EnumParticleTypes.WATER_BUBBLE, var8, var10 - 0.1F, var12, 1, var6, 0.1, var7, 0.0, new int[0]);
               }

               float var15 = var6 * 0.04F;
               float var16 = var7 * 0.04F;
               var2.spawnParticle(EnumParticleTypes.WATER_WAKE, var8, var10, var12, 0, var16, 0.01, -var15, 1.0, new int[0]);
               var2.spawnParticle(EnumParticleTypes.WATER_WAKE, var8, var10, var12, 0, -var16, 0.01, var15, 1.0, new int[0]);
            }
         } else {
            this.motionY = -0.4F * MathHelper.nextFloat(this.rand, 0.6F, 1.0F);
            this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
            double var17 = this.getEntityBoundingBox().minY + 0.5;
            var2.spawnParticle(
               EnumParticleTypes.WATER_BUBBLE,
               this.posX,
               var17,
               this.posZ,
               (int)(1.0F + this.width * 20.0F),
               this.width,
               0.0,
               this.width,
               0.2F,
               new int[0]
            );
            var2.spawnParticle(
               EnumParticleTypes.WATER_WAKE,
               this.posX,
               var17,
               this.posZ,
               (int)(1.0F + this.width * 20.0F),
               this.width,
               0.0,
               this.width,
               0.2F,
               new int[0]
            );
            this.d = MathHelper.getInt(this.rand, 20, 40);
         }
      } else if (this.c > 0) {
         this.c -= var3;
         float var18 = 0.15F;
         if (this.c < 20) {
            var18 = (float)(0.15F + (20 - this.c) * 0.05);
         } else if (this.c < 40) {
            var18 = (float)(0.15F + (40 - this.c) * 0.02);
         } else if (this.c < 60) {
            var18 = (float)(0.15F + (60 - this.c) * 0.01);
         }

         if (this.rand.nextFloat() < var18) {
            float var19 = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
            float var20 = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
            double var21 = this.posX + MathHelper.sin(var19) * var20 * 0.1F;
            double var22 = MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F;
            double var23 = this.posZ + MathHelper.cos(var19) * var20 * 0.1F;
            IBlockState var24 = var2.getBlockState(new BlockPos((int)var21, (int)var22 - 1, (int)var23));
            if (var24.getMaterial() == Material.WATER) {
               var2.spawnParticle(EnumParticleTypes.WATER_SPLASH, var21, var22, var23, 2 + this.rand.nextInt(2), 0.1F, 0.0, 0.1F, 0.0, new int[0]);
            }
         }

         if (this.c <= 0) {
            this.e = MathHelper.nextFloat(this.rand, 0.0F, 360.0F);
            this.j = MathHelper.getInt(this.rand, 20, 80);
         }
      } else {
         this.c = MathHelper.getInt(this.rand, 100, 600);
         this.c = this.c - this.o * 20 * 5;
      }
   }

   protected boolean isCollidableEntity(Entity var1) {
      return var1.canBeCollidedWith() || var1 instanceof EntityItem;
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
   }

   public int c_clash786() {
      if (!this.world.isRemote && this.b_clash775() != null) {
         byte var1 = 0;
         if (this.i != null) {
            this.d_clash787();
            this.world.setEntityState(this, (byte)31);
            var1 = (byte)(this.i instanceof EntityItem ? 3 : 5);
         } else if (this.d > 0) {
            Builder var3 = new Builder((WorldServer)this.world);

            for (ItemStack var6 : this.world
               .getLootTableManager()
               .getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING)
               .generateLootForPools(this.rand, var3.build())) {
               LunaEntity var7 = this.b_clash775();
               var7.b_clash383(var6);
            }

            this.d = 9999;
            var1 = 1;
         }

         if (this.k) {
            var1 = 2;
         }

         return var1;
      } else {
         return 0;
      }
   }

   protected void d_clash787() {
      LunaEntity var1 = this.b_clash775();
      if (var1 != null) {
         double var2 = var1.posX - this.posX;
         double var4 = var1.posY - this.posY;
         double var6 = var1.posZ - this.posZ;
         this.i.motionX += var2 * 0.1;
         this.i.motionY += var4 * 0.1;
         this.i.motionZ += var6 * 0.1;
      }
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public void readFromNBT(NBTTagCompound var1) {
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      return null;
   }


   enum SexEntityState {
      FLYING,
      HOOKED_IN_ENTITY,
      BOBBING;
   }
}
