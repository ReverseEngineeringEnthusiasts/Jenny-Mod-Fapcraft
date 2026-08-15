package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.TrigMath;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> The dragon charge projectile fired by Galath's energy-ball
 * attack ({@link GalathFlightData#SUMMON_SKELETON}). A no-clip projectile that
 * flies in a fixed direction; on CLIENT it spawns dragon-breath particles
 * along its path. When it hits a block it converts into a wither skeleton
 * guard (once) or, if charging, explodes on contact with a
 * {@link GalathEntity} and flings her into the knockout state
 * ({@link GalathEntity#setFlightVelocity(Vec3d)}).
 * <p>
 * <b>Pitfalls.</b> {@code attackEntityFrom} by a player turns the charge back
 * onto the attacker ({@code isCharging} + direction = attacker's look);
 * arrows destroy it. Fall/water NBT is intentionally empty
 * ({@code readEntityFromNBT} despawns it).
 */
public class DragonEntity extends EntityLiving {
   public static final float SCALE_0_4 = 0.4F;
   public static final float SCALE_0_3 = 0.3F;
   static final int MAX_TICKS_200 = 200;
   static final int MAX_TICKS_100 = 100;
   static final float SIZE_0_5 = 0.5F;
   static final float SCALE_0_15 = 0.15F;
   public static final float SCALE_0_75 = 0.75F;
   public double SCALE_1_0 = 1.0;
   Vec3d direction = Vec3d.ZERO;
   boolean isCharging = false;
   boolean shouldSpawnSkeleton = true;
   GalathEntity ownerGalath;

   public DragonEntity(World var1) {
      super(var1);
      this.setSize(0.5F, 0.5F);
   }

   public DragonEntity(World var1, GalathEntity var2) {
      super(var1);
      this.setSize(0.5F, 0.5F);
      this.ownerGalath = var2;
   }

   public DragonEntity(World var1, GalathEntity var2, Vec3d var3) {
      this(var1);
      this.direction = var3;
      this.ownerGalath = var2;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void collideWithEntity(Entity var1) {
   }

   /**
    * BOTH sides: no-clip flight along {@code direction} with breath
    * particles; the SERVER charges explode against Galaths
    * ({@link #tickChargeState()}) and block hits trigger
    * {@link #tickDragonLife()} (explode or spawn the skeleton guard).
    */
   public void onUpdate() {
      if (!this.isDead) {
         this.noClip = true;
         this.setNoGravity(true);
         this.motionX = this.direction.x;
         this.motionY = this.direction.y;
         this.motionZ = this.direction.z;
         super.onUpdate();
         if (this.world.isRemote) {
            this.spawnChargedBreath();
         }

         this.tickChargeState();
         if (!this.world.isAirBlock(this.getPosition())) {
            this.tickDragonLife();
            this.world.removeEntity(this);
         }
      }
   }

   /**
    * SERVER: the charging hit — when a charged dragon overlaps a Galath it
    * explodes, flings her away ({@link GalathEntity#setFlightVelocity(Vec3d)})
    * and despawns.
    */
   void tickChargeState() {
      if (!this.world.isRemote) {
         if (this.isCharging) {
            Vec3d var1 = this.getPositionVector();
            Vec3d var2 = var1.subtract(0.75, 0.75, 0.75);
            Vec3d var3 = var1.add(0.75, 0.75, 0.75);
            AxisAlignedBB var4 = new AxisAlignedBB(
               var2.x, var2.y, var2.z, var3.x, var3.y, var3.z
            );
            List var5 = this.world.getEntitiesWithinAABB(GalathEntity.class, var4);
            if (!var5.isEmpty()) {
               this.world.createExplosion(this, this.posX, this.posY, this.posZ, 1.0F, true);

               for (GalathEntity var7 : (java.util.Collection<GalathEntity>) (var5) ) {
                  var7.setFlightVelocity(this.getPositionVector());
               }

               this.world.removeEntity(this);
            }
         }
      }
   }

   void spawnChargedBreath() {
      this.spawnBreathParticles(
         RotationHelper.lerpDouble(this.lastTickPosX, this.posX, 0.5),
         RotationHelper.lerpDouble(this.lastTickPosY, this.posY, 0.5),
         RotationHelper.lerpDouble(this.lastTickPosZ, this.posZ, 0.5)
      );
      this.spawnBreathParticles(this.posX, this.posY, this.posZ);
   }

   void spawnBreathParticles(double var1, double var3, double var5) {
      Random var7 = this.getRNG();
      this.world
         .spawnParticle(
            EnumParticleTypes.DRAGON_BREATH,
            var1 + var7.nextDouble() * 0.3F,
            var3 + 0.25 + var7.nextDouble() * 0.3F,
            var5 + var7.nextDouble() * 0.3F,
            0.0,
            0.0,
            0.0,
            new int[0]
         );
   }

   /**
    * SERVER: the block-impact outcome — near the Galath's target she spawns
    * a wither-skeleton guard (sword-wielding, tracked in
    * {@code GalathEntity#bI}), otherwise she explodes.
    */
   void tickDragonLife() {
      if (!this.world.isRemote) {
         if (!this.isDead) {
            if (this.shouldSpawnSkeleton) {
               Vec3d var1 = new Vec3d(this.posX, this.getPosition().getY() + 1, this.posZ);
               if (!this.isInRangeOfTarget(var1)) {
                  this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2.0F, true);
                  this.shouldSpawnSkeleton = false;
               } else {
                  EntityWitherSkeleton var2 = new EntityWitherSkeleton(this.world);
                  var2.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));
                  var2.setPositionAndUpdate(var1.x, var1.y, var1.z);
                  this.world.spawnEntity(var2);
                  PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(var1, true), this);
                  this.ownerGalath.bI.add(var2);
               }
            }
         }
      }
   }

   boolean isInRangeOfTarget(Vec3d var1) {
      if (this.ownerGalath == null) {
         return true;
      }

      EntityLivingBase var2 = this.ownerGalath.getTargetEntity();
      return var2 == null ? true : var2.getDistance(var1.x, var1.y, var1.z) < 15.0;
   }

   /**
    * CLIENT: renders a smoke ring burst at the given position (used for the
    * energy-ball despawn effects).
    */
   @SideOnly(Side.CLIENT)
   public static void spawnDragonBreath(Vec3d var0) {
      WorldClient var1 = Minecraft.getMinecraft().world;
      float var2 = TrigMath.wrapDegrees(1.8F);
      Random var3 = Reference.RANDOM;

      for (float var4 = 0.0F; var4 < Math.PI * 2; var4 += var2) {
         double var5 = Math.sin(var4);
         double var7 = Math.cos(var4);
         double var9 = var0.x + var5 * 0.5;
         double var11 = var5 * 0.15F;
         double var13 = var0.z + var7 * 0.5;
         double var15 = var7 * 0.15F;
         double var17 = var0.y;
         double var19 = var3.nextDouble() * 0.15F;
         var1.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var9, var17, var13, var11, var19, var15, new int[0]);
      }
   }

   @SideOnly(Side.CLIENT)
   public static void spawnDragonBreathRandom(Vec3d var0) {
      WorldClient var1 = Minecraft.getMinecraft().world;
      Random var2 = Reference.RANDOM;

      for (int var3 = 0; var3 < 100; var3++) {
         var1.spawnParticle(
            EnumParticleTypes.DRAGON_BREATH,
            var0.x,
            var0.y,
            var0.z,
            var2.nextDouble() * 0.15F,
            var2.nextDouble() * 0.15F,
            var2.nextDouble() * 0.15F,
            new int[0]
         );
      }

      var1.playSound(var0.x, var0.y, var0.z, SoundHandler.MISC_SHATTER[0], SoundCategory.AMBIENT, 0.7F, 1.0F, false);
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (DamageSource.OUT_OF_WORLD.equals(var1)) {
         this.setHealth(0.0F);
         this.shouldSpawnSkeleton = false;
         this.world.removeEntity(this);
         return true;
      }

      if (!this.world.isRemote && "arrow".equals(var1.damageType)) {
         this.setHealth(0.0F);
         this.shouldSpawnSkeleton = false;
         PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.getPositionVector(), false), this);
         Entity var4 = var1.getImmediateSource();
         if (var4 != null) {
            this.world.removeEntity(var4);
         }

         this.world.removeEntity(this);
         return true;
      } else {
         Entity var3 = var1.getTrueSource();
         if (!(var3 instanceof EntityPlayer)) {
            return false;
         }

         this.direction = var3.getLookVec();
         this.isCharging = true;
         return true;
      }
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.world.removeEntity(this);
   }

}
