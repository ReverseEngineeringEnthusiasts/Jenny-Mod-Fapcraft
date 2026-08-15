package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> The "pyrocinical" ambient entity — a generic girl-shaped
 * filler mob (NOT a {@link BaseGirlEntity}) that spawns rarely in the Nether,
 * wanders/follows players and despawns 60 seconds after being hit by a
 * player. Minimal implementation: procedural wander AI, no inventory, no
 * scenes.
 * <p>
 * <b>Pitfalls.</b> {@link #attackEntityFrom(DamageSource, float)} never deals
 * damage — player hits only stop it and schedule the 6.25 s removal;
 * out-of-world damage removes it instantly.
 */
public class BasicGirlEntity extends EntityLiving {
   public static final long LIFETIME_MS = 60000L;
   public static final float FOLLOW_DISTANCE = 3.0F;
   static final float WANDER_DISTANCE = 30.0F;
   static final int SOUND_COOLDOWN = 175;
   static final int TICK_COUNT = 10;
   BlockPos wanderTarget = null;
   int wanderTicks = 0;
   boolean shouldStopMoving = false;
   public int lastSoundTick = -1;

   public BasicGirlEntity(World world) {
      super(world);
   }

   protected void updateAITasks() {
      super.updateAITasks();
      this.updateWanderAndFollowAI();
   }

   /**
    * BOTH sides: wander/follow — picks a random ground point (respecting the
    * Nether ceiling) within 30 blocks and paths there; stays put when a
    * player is within 3 blocks or when stopped by a player hit.
    */
   void updateWanderAndFollowAI() {
      if (this.shouldStopMoving) {
         this.getNavigator().clearPath();
      } else {
         EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);
         if (player != null && player.getDistance(this) < 3.0F) {
            this.getNavigator().clearPath();
         } else {
            if (this.wanderTarget == null || this.getDistance(this.wanderTarget.getX(), this.wanderTarget.getY(), this.wanderTarget.getZ()) > this.getDespawnDistance() || this.wanderTicks > 175) {
               int xOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
               int zOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
               int height = this.world.provider.getDimensionType() == DimensionType.NETHER
                  ? (int)Math.ceil(this.posY)
                  : WorldUtils.getHeightAt(this.world, this.getPosition().getX() + xOffset, this.getPosition().getZ() + zOffset);
               this.wanderTarget = new BlockPos(this.getPosition().getX() + xOffset, height, this.getPosition().getZ() + zOffset);
               this.wanderTicks = 0;
            }

            if (Math.sqrt(this.wanderTarget.distanceSq(this.getPosition())) > 2.0) {
               this.getNavigator().tryMoveToXYZ(this.wanderTarget.getX(), this.wanderTarget.getY(), this.wanderTarget.getZ(), 0.35F);
               this.updateWanderAI();
            } else {
               this.wanderTicks++;
            }
         }
      }
   }

   protected void updateWanderAI() {
      Path path = this.getNavigator().getPath();
      if (path != null) {
         if (!this.onGround && !this.isInWater()) {
            int currentIndex = path.getCurrentPathIndex();
            int length = path.getCurrentPathLength();
            if (length != currentIndex && length - 1 != currentIndex) {
               PathPoint currentPoint = path.getPathPointFromIndex(currentIndex);
               PathPoint nextPoint = path.getPathPointFromIndex(currentIndex + 1);
               Vec3d delta = new Vec3d(nextPoint.x - currentPoint.x, nextPoint.y - currentPoint.y, nextPoint.z - currentPoint.z);
               this.motionX = delta.x / 7.0;
               this.motionZ = delta.z / 7.0;
            }
         }
      }
   }

   /**
    * Player hits never damage: they stop the mob, play the spawn sound
    * (CLIENT) and schedule its removal 6250 ms later; out-of-world damage
    * removes it immediately.
    */
   public boolean attackEntityFrom(DamageSource source, float amount) {
      if (source == DamageSource.OUT_OF_WORLD) {
         this.world.removeEntity(this);
         return true;
      }

      if (!(source.getTrueSource() instanceof EntityPlayer)) {
         return false;
      }

      if (this.world.isRemote) {
         this.playSpawnSound();
      }

      this.shouldStopMoving = true;
      ThreadNames.createDaemonThread(6250, () -> this.world.removeEntity(this));
      return false;
   }

   @SideOnly(Side.CLIENT)
   void playSpawnSound() {
      EntityPlayerSP player = Minecraft.getMinecraft().player;
      this.lastSoundTick = player.ticksExisted;
      player.playSound(SoundHandler.MISC_WEOWEO[3], 1.0F, 1.0F);
   }

   double getDespawnDistance() {
      return Math.sqrt(1800.0);
   }

   public boolean getCanSpawnHere() {
      if (this.getRNG().nextInt(100) < 1 && this.getRNG().nextInt(100) < 10) {
         return true;
      }

      this.world.removeEntity(this);
      return false;
   }

}
