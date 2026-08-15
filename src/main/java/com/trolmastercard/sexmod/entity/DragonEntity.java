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

   public DragonEntity(World world) {
      super(world);
      this.setSize(0.5F, 0.5F);
   }

   public DragonEntity(World world, GalathEntity galath) {
      super(world);
      this.setSize(0.5F, 0.5F);
      this.ownerGalath = galath;
   }

   public DragonEntity(World world, GalathEntity galath, Vec3d direction) {
      this(world);
      this.direction = direction;
      this.ownerGalath = galath;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void collideWithEntity(Entity entity) {
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
            Vec3d pos = this.getPositionVector();
            Vec3d min = pos.subtract(0.75, 0.75, 0.75);
            Vec3d max = pos.add(0.75, 0.75, 0.75);
            AxisAlignedBB aabb = new AxisAlignedBB(
               min.x, min.y, min.z, max.x, max.y, max.z
            );
            List entities = this.world.getEntitiesWithinAABB(GalathEntity.class, aabb);
            if (!entities.isEmpty()) {
               this.world.createExplosion(this, this.posX, this.posY, this.posZ, 1.0F, true);

               for (GalathEntity galath : (java.util.Collection<GalathEntity>) (entities) ) {
                  galath.setFlightVelocity(this.getPositionVector());
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

   void spawnBreathParticles(double x, double y, double z) {
      Random random = this.getRNG();
      this.world
         .spawnParticle(
            EnumParticleTypes.DRAGON_BREATH,
            x + random.nextDouble() * 0.3F,
            y + 0.25 + random.nextDouble() * 0.3F,
            z + random.nextDouble() * 0.3F,
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
               Vec3d pos = new Vec3d(this.posX, this.getPosition().getY() + 1, this.posZ);
               if (!this.isInRangeOfTarget(pos)) {
                  this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2.0F, true);
                  this.shouldSpawnSkeleton = false;
               } else {
                  EntityWitherSkeleton skeleton = new EntityWitherSkeleton(this.world);
                  skeleton.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));
                  skeleton.setPositionAndUpdate(pos.x, pos.y, pos.z);
                  this.world.spawnEntity(skeleton);
                  PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(pos, true), this);
                  this.ownerGalath.bI.add(skeleton);
               }
            }
         }
      }
   }

   boolean isInRangeOfTarget(Vec3d pos) {
      if (this.ownerGalath == null) {
         return true;
      }

      EntityLivingBase target = this.ownerGalath.getTargetEntity();
      return target == null ? true : target.getDistance(pos.x, pos.y, pos.z) < 15.0;
   }

   /**
    * CLIENT: renders a smoke ring burst at the given position (used for the
    * energy-ball despawn effects).
    */
   @SideOnly(Side.CLIENT)
   public static void spawnDragonBreath(Vec3d pos) {
      WorldClient world = Minecraft.getMinecraft().world;
      float step = TrigMath.wrapDegrees(1.8F);
      Random random = Reference.RANDOM;

      for (float angle = 0.0F; angle < Math.PI * 2; angle += step) {
         double sin = Math.sin(angle);
         double cos = Math.cos(angle);
         double x = pos.x + sin * 0.5;
         double vx = sin * 0.15F;
         double z = pos.z + cos * 0.5;
         double vz = cos * 0.15F;
         double y = pos.y;
         double vy = random.nextDouble() * 0.15F;
         world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, vx, vy, vz, new int[0]);
      }
   }

   @SideOnly(Side.CLIENT)
   public static void spawnDragonBreathRandom(Vec3d pos) {
      WorldClient world = Minecraft.getMinecraft().world;
      Random random = Reference.RANDOM;

      for (int i = 0; i < 100; i++) {
         world.spawnParticle(
            EnumParticleTypes.DRAGON_BREATH,
            pos.x,
            pos.y,
            pos.z,
            random.nextDouble() * 0.15F,
            random.nextDouble() * 0.15F,
            random.nextDouble() * 0.15F,
            new int[0]
         );
      }

      world.playSound(pos.x, pos.y, pos.z, SoundHandler.MISC_SHATTER[0], SoundCategory.AMBIENT, 0.7F, 1.0F, false);
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      if (DamageSource.OUT_OF_WORLD.equals(source)) {
         this.setHealth(0.0F);
         this.shouldSpawnSkeleton = false;
         this.world.removeEntity(this);
         return true;
      }

      if (!this.world.isRemote && "arrow".equals(source.damageType)) {
         this.setHealth(0.0F);
         this.shouldSpawnSkeleton = false;
         PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.getPositionVector(), false), this);
         Entity immediateSource = source.getImmediateSource();
         if (immediateSource != null) {
            this.world.removeEntity(immediateSource);
         }

         this.world.removeEntity(this);
         return true;
      } else {
         Entity trueSource = source.getTrueSource();
         if (!(trueSource instanceof EntityPlayer)) {
            return false;
         }

         this.direction = trueSource.getLookVec();
         this.isCharging = true;
         return true;
      }
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      this.world.removeEntity(this);
   }

}
