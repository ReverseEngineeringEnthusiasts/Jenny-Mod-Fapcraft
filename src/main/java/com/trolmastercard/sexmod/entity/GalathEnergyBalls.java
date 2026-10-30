package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.VectorMath;
import java.util.ArrayList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

/**
 * <b>Role.</b> Bookkeeping for Galath's SUMMON_SKELETON attack, split out of
 * {@link GalathEntity}: the two energy-ball hitbox parts, the summon progress
 * counter, and the lifecycle of the skeleton charges she keeps alive (dragon
 * breath particle sync, despawning charges that fell behind, clearing them
 * when she is paralyzed or dead).
 * <p>
 * <b>Flow.</b> {@link GalathEntity#updateFlightUI()} ticks the progress,
 * {@link GalathSceneTicks#tickWildSystems(GalathEntity)} places the hitboxes
 * every tick, and {@link GalathEntity#ao()} prunes/despawns the skeleton list.
 * <p>
 * <b>Pitfalls.</b> The hitboxes must be active exactly while
 * {@code summonSkeletonTicks} is in [9, 30] — the entity's
 * {@code getParts()} hit detection depends on it.
 */
final class GalathEnergyBalls {
   private GalathEnergyBalls() {
   }

   /**
    * SERVER: places the two energy-ball hitbox parts at the summoning hands
    * while the summon action is charging ({@code summonSkeletonTicks} in [9, 30]); both parts
    * are inactive outside that window.
    */
   static void resetEnergyBalls(GalathEntity galath) {
      galath.b2.isActive = false;
      galath.energyBallHitboxRight.isActive = false;
      if (!(galath.summonSkeletonTicks < 9.0F)) {
         if (!(galath.summonSkeletonTicks > 30.0F)) {
            galath.b2.isActive = true;
            galath.energyBallHitboxRight.isActive = true;
            boolean reset = (Boolean)galath.getDataManager().get(GalathEntity.ay);
            Vec3d pos = galath.getPositionVector();
            Vec3d tempPos;
            Vec3d tempOffset;
            if (reset) {
               tempPos = pos;
               tempOffset = VectorMath.MirrorXZ(GalathEntity.bz);
            } else {
               tempPos = pos;
               tempOffset = GalathEntity.bz;
            }

            Vec3d leftPos = tempPos.add(VectorMath.rotateByYaw(tempOffset, 180.0F + galath.renderYawOffset));
            Vec3d rightPos = galath.getPositionVector();
            if (reset) {
               tempPos = rightPos;
               tempOffset = VectorMath.MirrorXZ(GalathEntity.bC);
            } else {
               tempPos = rightPos;
               tempOffset = GalathEntity.bC;
            }

            Vec3d mirrorRightPos = tempPos.add(VectorMath.rotateByYaw(tempOffset, 180.0F + galath.renderYawOffset));
            galath.b2.setLocationAndAngles(leftPos.x, leftPos.y, leftPos.z, galath.renderYawOffset, 0.0F);
            galath.energyBallHitboxRight.setLocationAndAngles(mirrorRightPos.x, mirrorRightPos.y, mirrorRightPos.z, galath.renderYawOffset, 0.0F);
            galath.b2.onUpdate();
            galath.energyBallHitboxRight.onUpdate();
         }
      }
   }

   static void tickSummonSkeletonProgress(GalathEntity galath) {
      if (galath.getCurrentAction() != Action.SUMMON_SKELETON) {
         galath.summonSkeletonTicks = 0;
      } else {
         if (galath.summonSkeletonTicks++ > 45) {
            galath.summonSkeletonTicks = 0;
         }
      }
   }

   static void despawnFarSummonedSkeletons(GalathEntity galath) {
      EntityLivingBase target = galath.getTargetEntity();
      if (target != null) {
         for (EntityWitherSkeleton skeleton : galath.summonedSkeletons) {
            if (!skeleton.isDead && !(target.getDistance(skeleton) < 15.0F)) {
               PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(skeleton.getPositionVector(), true), galath);
               skeleton.setDead();
               galath.world.removeEntity(skeleton);
            }
         }
      }
   }

   static void despawnSummonedSkeletons(GalathEntity galath) {
      if ((Boolean)galath.getDataManager().get(GalathEntity.bP)) {
         for (EntityWitherSkeleton skeleton : galath.summonedSkeletons) {
            if (!skeleton.isDead) {
               PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(skeleton.getPositionVector(), true), galath);
               skeleton.setDead();
               galath.world.removeEntity(skeleton);
            }
         }

         galath.summonedSkeletons.clear();
      }
   }

   static void spawnSkeletonBreathParticles(GalathEntity galath) {
      for (EntityWitherSkeleton skeleton : galath.summonedSkeletons) {
         if (!skeleton.isDead && skeleton.ticksExisted % 10 == 0) {
            for (EntityPlayer player : (java.util.Collection<EntityPlayer>) ((WorldServer)galath.world).getEntityTracker().getTrackingPlayers(skeleton)) {
               ((EntityPlayerMP)player)
                  .connection
                  .sendPacket(
                     new SPacketParticles(
                        EnumParticleTypes.DRAGON_BREATH,
                        false,
                        (float)skeleton.posX,
                        (float)skeleton.posY,
                        (float)skeleton.posZ,
                        0.2F * ThreadNames.randomSign(),
                        skeleton.getEyeHeight() / 2.0F,
                        0.2F * ThreadNames.randomSign(),
                        0.0F,
                        5,
                        new int[0]
                     )
                  );
            }
         }
      }
   }

   static void pruneDeadSkeletons(GalathEntity galath) {
      ArrayList deadSkeletons = new ArrayList();

      for (EntityWitherSkeleton skeleton : galath.summonedSkeletons) {
         if (skeleton.isDead) {
            deadSkeletons.add(skeleton);
         }
      }

      for (EntityWitherSkeleton skeleton : (java.util.Collection<EntityWitherSkeleton>) (deadSkeletons) ) {
         galath.summonedSkeletons.remove(skeleton);
      }
   }
}
