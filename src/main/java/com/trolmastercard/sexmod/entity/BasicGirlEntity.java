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

   public BasicGirlEntity(World var1) {
      super(var1);
   }

   protected void updateAITasks() {
      super.updateAITasks();
      this.updateWanderAndFollowAI();
   }

   void updateWanderAndFollowAI() {
      if (this.shouldStopMoving) {
         this.getNavigator().clearPath();
      } else {
         EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 15.0);
         if (var1 != null && var1.getDistance(this) < 3.0F) {
            this.getNavigator().clearPath();
         } else {
            if (this.wanderTarget == null || this.getDistance(this.wanderTarget.getX(), this.wanderTarget.getY(), this.wanderTarget.getZ()) > this.getDespawnDistance() || this.wanderTicks > 175) {
               int var2 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
               int var3 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
               int var4 = this.world.provider.getDimensionType() == DimensionType.NETHER
                  ? (int)Math.ceil(this.posY)
                  : WorldUtils.a(this.world, this.getPosition().getX() + var2, this.getPosition().getZ() + var3);
               this.wanderTarget = new BlockPos(this.getPosition().getX() + var2, var4, this.getPosition().getZ() + var3);
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
      Path var1 = this.getNavigator().getPath();
      if (var1 != null) {
         if (!this.onGround && !this.isInWater()) {
            int var2 = var1.getCurrentPathIndex();
            int var3 = var1.getCurrentPathLength();
            if (var3 != var2 && var3 - 1 != var2) {
               PathPoint var4 = var1.getPathPointFromIndex(var2);
               PathPoint var5 = var1.getPathPointFromIndex(var2 + 1);
               Vec3d var6 = new Vec3d(var5.x - var4.x, var5.y - var4.y, var5.z - var4.z);
               this.motionX = var6.x / 7.0;
               this.motionZ = var6.z / 7.0;
            }
         }
      }
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (var1 == DamageSource.OUT_OF_WORLD) {
         this.world.removeEntity(this);
         return true;
      }

      if (!(var1.getTrueSource() instanceof EntityPlayer)) {
         return false;
      }

      if (this.world.isRemote) {
         this.playSpawnSound();
      }

      this.shouldStopMoving = true;
      ThreadNames.a(6250, () -> this.world.removeEntity(this));
      return false;
   }

   @SideOnly(Side.CLIENT)
   void playSpawnSound() {
      EntityPlayerSP var1 = Minecraft.getMinecraft().player;
      this.lastSoundTick = var1.ticksExisted;
      var1.playSound(SoundHandler.MISC_WEOWEO[3], 1.0F, 1.0F);
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
