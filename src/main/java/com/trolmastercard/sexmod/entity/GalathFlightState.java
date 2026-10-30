package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.util.GalathDamageSource;
import com.trolmastercard.sexmod.util.SoundHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

/**
 * <b>Role.</b> Galath's kinematic state machines, split out of
 * {@link GalathEntity}: the wild flight machine (target acquisition, next
 * {@link GalathFlightData} action selection, CHANGE_POSITION clipping) and the
 * knock-out recovery ladder ({@code KNOCK_OUT_FLY} -&gt; {@code KNOCK_OUT_GROUND}
 * -&gt; {@code KNOCK_OUT_STAND_UP}). Every method is stateless bookkeeping over
 * the entity's package-private state ({@code activeFlight},
 * {@code standUpTicks}, {@code knockoutGroundTicks}, the data keys).
 * <p>
 * <b>Flow.</b> {@link GalathEntity#ao()} runs this in order: target cancel,
 * target acquisition, the flight tick chain, then the knock-out ladder.
 * <p>
 * <b>Pitfalls.</b> {@link #initFlightData(GalathEntity)} is the only place
 * that picks a random {@link GalathFlightData} — its retry loop relies on
 * {@code canInitFlight} always terminating. The flight target box is 7 blocks
 * for a tamed (master) girl and 20 for a wild one.
 */
final class GalathFlightState {
   private GalathFlightState() {
   }

   static void tickKnockoutStandUp(GalathEntity galath) {
      if (galath.getCurrentAction() == Action.KNOCK_OUT_STAND_UP) {
         galath.standUpTicks++;
         if (galath.standUpTicks == 39.0) {
            galath.setNoGravity(true);
            galath.setVelocity(0.0, 0.6F, 0.0);
            Vec3d pos = galath.getPositionVector();
            Vec3d min = pos.subtract(2.0, 2.0, 2.0);
            Vec3d max = pos.add(2.0, 2.0, 2.0);
            AxisAlignedBB aabb = new AxisAlignedBB(
               min.x, min.y, min.z, max.x, max.y, max.z
            );

            for (EntityLivingBase entity : galath.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
               if (!(entity instanceof GalathEntity)) {
                  Vec3d entityPos = entity.getPositionVector();
                  Vec3d dir = entityPos.subtract(pos).normalize();
                  entity.motionX = dir.x * 1.0;
                  entity.motionZ = dir.z * 1.0;
                  entity.motionY = 1.0;
                  entity.attackEntityFrom(new GalathDamageSource(galath), 0.5F);
                  if (entity instanceof EntityPlayerMP) {
                     EntityPlayerMP playerMP = (EntityPlayerMP)entity;
                     playerMP.connection.sendPacket(new SPacketEntityVelocity(playerMP));
                  }
               }
            }
         }

         if (!(galath.standUpTicks < 58.0)) {
            galath.setVelocity(Vec3d.ZERO);
            galath.getDataManager().set(GalathEntity.PARALYZED, false);
            galath.standUpTicks = 0;
         }
      }
   }

   static void handleKnockout(GalathEntity galath) {
      if (galath.getCurrentAction() == Action.KNOCK_OUT_GROUND) {
         if (!(Boolean)galath.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
            if (!(++galath.knockoutGroundTicks < 50.0)) {
               galath.setCurrentAction(Action.KNOCK_OUT_STAND_UP);
               galath.standUpTicks = 0;
               galath.knockoutGroundTicks = 0;
            }
         }
      }
   }

   static void freezeDuringKnockout(GalathEntity galath) {
      Action action = galath.getCurrentAction();
      if (action == Action.KNOCK_OUT_GROUND || action == Action.KNOCK_OUT_STAND_UP) {
         galath.motionX = 0.0;
         galath.motionZ = 0.0;
         if ((Boolean)galath.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
            galath.motionY = 0.0;
         }
      }
   }

   static void tickKnockoutFlight(GalathEntity galath) {
      if (galath.getCurrentAction() == Action.KNOCK_OUT_FLY) {
         BlockPos pos = galath.getPosition();
         if (!(galath.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) {
            if (galath.onGround) {
               galath.setCurrentAction(Action.KNOCK_OUT_GROUND);
            }
         } else {
            BlockPos surfacePos = pos;

            while (galath.world.getBlockState(surfacePos.up()).getBlock() instanceof BlockLiquid) {
               surfacePos = surfacePos.up();
            }

            for (int x = -1; x < 2; x++) {
               for (int z = -1; z < 2; z++) {
                  galath.world.setBlockState(surfacePos.add(x, 0, z), Blocks.OBSIDIAN.getDefaultState());
               }
            }

            surfacePos = surfacePos.up();
            galath.setPositionAndUpdate(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());
            galath.setTargetPosition(new Vec3d(surfacePos));
            PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(new Vec3d(surfacePos), true), galath);

            for (EntityPlayer player : (java.util.Collection<EntityPlayer>) ((WorldServer)galath.world).getEntityTracker().getTrackingPlayers(galath)) {
               ((EntityPlayerMP)player)
                  .connection
                  .sendPacket(
                     new SPacketSoundEffect(
                        SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.AMBIENT, galath.posX, galath.posY, galath.posZ, 1.0F, 1.0F
                     )
                  );
            }

            galath.setCurrentAction(Action.KNOCK_OUT_GROUND);
         }
      }
   }

   static void updateChangePositionNoClip(GalathEntity galath) {
      if (galath.activeFlight == GalathFlightData.CHANGE_POSITION) {
         int progress = galath.getAttackProgress();
         galath.noClip = progress == 0;
         if (!galath.world.isAirBlock(galath.getPosition())) {
            galath.noClip = true;
         }
      }
   }

   static void checkFlightFinished(GalathEntity galath) {
      if (galath.activeFlight != null) {
         galath.activeFlight.checkFinished(galath);
      }
   }

   static void tickFlightStateMachine(GalathEntity galath) {
      if (galath.getTargetEntity() == null) {
         clearActiveFlight(galath);
      } else if (galath.activeFlight == null) {
         initFlightData(galath);
      } else {
         if (galath.activeFlight.executeUpdate(galath)) {
            initFlightData(galath);
         }
      }
   }

   /**
    * SERVER: selects and starts the next flight action — if a scene player is
    * bound the current action stops; otherwise a random executable
    * {@link GalathFlightData} action is started (with the attack cooldown
    * collapsing into CHANGE_POSITION when it applies).
    */
   static void initFlightData(GalathEntity galath) {
      if (!(Boolean)galath.getDataManager().get(GalathEntity.PARALYZED)) {
         GalathFlightData flightData = galath.activeFlight;
         if (galath.getInteractionPlayerUUID() != null) {
            if (flightData != null) {
               flightData.updateFlight(galath);
            }

            galath.activeFlight = null;
         } else if (flightData != null && flightData.applyAttackCoolDown) {
            flightData.updateFlight(galath);
            galath.activeFlight = GalathFlightData.CHANGE_POSITION;
            galath.activeFlight.executeStart(galath);
         } else {
            GalathFlightData[] values = GalathFlightData.values();

            GalathFlightData chosen;
            do {
               chosen = values[galath.getRNG().nextInt(values.length)];
            } while (!canInitFlight(galath, chosen));

            galath.activeFlight = chosen;
            if (flightData != null) {
               flightData.updateFlight(galath);
            }

            galath.activeFlight.executeStart(galath);
         }
      }
   }

   static boolean canInitFlight(GalathEntity galath, GalathFlightData flightData) {
      return flightData.onlyDoThisOnPlayers && !(galath.getTargetEntity() instanceof EntityPlayer) ? false : flightData.canExecute(galath);
   }

   static void clearActiveFlight(GalathEntity galath) {
      galath.activeFlight = null;
   }

   /**
    * SERVER: acquires a flight target — the nearest player (wild) or mob
    * (tamed) within the search box, and switches to CHANGE_POSITION to
    * approach it. See {@link #getPlayerInBox(GalathEntity, AxisAlignedBB)} /
    * {@link #getMobInBox(GalathEntity, AxisAlignedBB)}.
    */
   static void acquireFlightTarget(GalathEntity galath) {
      if (!hasFlightTarget(galath)) {
         if (galath.getInteractionPlayerUUID() == null) {
            boolean hasMaster = galath.hasMaster();
            float offset = hasMaster ? 7.0F : 20.0F;
            Vec3d box = new Vec3d(offset, offset, offset);
            Vec3d pos = galath.getPositionVector();
            Vec3d min = pos.subtract(box);
            Vec3d max = pos.add(box);
            AxisAlignedBB aabb = new AxisAlignedBB(
               min.x, min.y, min.z, max.x, max.y, max.z
            );
            Object target = hasMaster ? getMobInBox(galath, aabb) : getPlayerInBox(galath, aabb);
            if (target == null) {
               clearFlightTarget(galath);
            } else {
               galath.setTargetEntity((EntityLivingBase)target);
               BaseGirlEntity.girlPlaySound(galath, SoundHandler.GIRLS_GALATH_DIALOG[1], true);
               if (galath.activeFlight != null) {
                  galath.activeFlight.updateFlight(galath);
               }

               galath.activeFlight = GalathFlightData.CHANGE_POSITION;
               galath.activeFlight.executeStart(galath);
            }
         }
      }
   }

   static EntityPlayer getPlayerInBox(GalathEntity galath, AxisAlignedBB aabb) {
      List players = galath.world
         .getEntitiesWithinAABB(EntityPlayer.class, aabb, player -> !AbstractPlayerGirlEntity.isOwnerPlayer(player) && !player.isCreative() && !player.isSpectator());
      return players.isEmpty() ? null : (EntityPlayer)players.get(0);
   }

   static EntityMob getMobInBox(GalathEntity galath, AxisAlignedBB aabb) {
      List mobs = galath.world.getEntitiesWithinAABB(EntityMob.class, aabb);
      if (mobs.isEmpty()) {
         return null;
      }

      ArrayList validMobs = new ArrayList();

      for (EntityMob mob : (java.util.Collection<EntityMob>) (mobs) ) {
         if (com.trolmastercard.sexmod.MobPredicates.isValidTarget(mob)) {
            validMobs.add(mob);
         }
      }

      Vec3d eyePos = galath.getPositionVector().add(0.0, galath.getEyeHeight(), 0.0);

      for (EntityMob mob : (java.util.Collection<EntityMob>) (validMobs) ) {
         if (com.trolmastercard.sexmod.MobPredicates.isDaylight(galath.world, eyePos, mob)) {
            return mob;
         }
      }

      return null;
   }

   static void clearFlightTarget(GalathEntity galath) {
      if (galath.getTargetEntity() != null) {
         galath.setTargetEntity(null);
         if (galath.activeFlight != null) {
            galath.activeFlight.updateFlight(galath);
         }

         galath.activeFlight = null;
         if (!(Boolean)galath.getDataManager().get(GalathEntity.PARALYZED)) {
            galath.setCurrentAction(Action.NULL);
         }
      }
   }

   static boolean hasFlightTarget(GalathEntity galath) {
      EntityLivingBase target = galath.getTargetEntity();
      if (target == null) {
         return false;
      } else if (target.isDead) {
         return false;
      } else if (target.dimension != galath.dimension) {
         return false;
      } else {
         float dist = galath.getDistance(target);
         float maxDist = galath.hasMaster() ? 16.0F : 30.0F;
         if (dist > maxDist) {
            return false;
         } else if (!(target instanceof EntityPlayer)) {
            return true;
         } else {
            EntityPlayer player = (EntityPlayer)target;
            if (BaseGirlEntity.getActiveSceneInfo(player.getPersistentID()) != null) {
               return false;
            } else {
               return player.isCreative() ? false : !player.isSpectator();
            }
         }
      }
   }

   static void cancelFlightOnLostTarget(GalathEntity galath) {
      if (galath.hasMaster()) {
         if (galath.getTargetEntity() == null) {
            int attackProgress = (Integer)galath.getDataManager().get(GalathEntity.TARGET_ENTITY_ID);
            if (attackProgress != -1) {
               if (galath.activeFlight != null) {
                  galath.activeFlight.updateFlight(galath);
               }

               galath.activeFlight = null;
               galath.setCurrentAction(Action.NULL);
            }
         }
      }
   }
}
