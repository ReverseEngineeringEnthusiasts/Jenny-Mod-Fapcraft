package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.PathUtils;
import com.trolmastercard.sexmod.util.TrigMath;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> Galath's tamed-side AI wiring, split out of
 * {@link GalathEntity}: the owner-nearby gate, the Manglelie partnership hunt
 * (walk up to an unpartnered Manglelie and hug her), the on-foot owner follow
 * (path/speed/sprint bookkeeping) and the flight reset that hands control back
 * to the flight state machine.
 * <p>
 * <b>Flow.</b> {@link GalathEntity#updateAITasks()} calls
 * {@link #tickTamedAi(GalathEntity)} when she has a master;
 * {@link #am(GalathEntity)} falls back to the flight AI
 * ({@link GalathEntity#ao()}) when the owner cannot be followed on foot.
 * <p>
 * <b>Pitfalls.</b> {@link #followOwnerOnFoot(GalathEntity)} mutates
 * {@code currentPath} and the navigator speed; it must always leave
 * {@code activeFlight} null before the flight machine picks again.
 */
final class GalathTamedAi {
   private GalathTamedAi() {
   }

   static void tickTamedAi(GalathEntity galath) {
      galath.aO.setVisible(false);
      if (!GirlSavedData.isOwnerNearby(galath)) {
         GirlSavedData.updateMangleliePartner(galath);
      } else if (galath.getRidingPlayer() != null) {
         clearFlightData(galath);
      } else {
         handleManglelieOwned(galath);
         if (galath.getMangleliePartnerUUID() == null) {
            aJ(galath);
         } else {
            am(galath);
         }
      }
   }

   static void handleManglelieOwned(GalathEntity galath) {
      if (GirlSavedData.isManglelieOwned(GirlSavedData.getManglelieOwnerOf(galath))) {
         boolean canLick = galath.canStartPussyLicking();
         if (canLick) {
            Main.LOGGER.warn("mommy thinks she got no daughter but she actually does have one. Failsafe called. Hopefully its fixed");
         }
      }
   }

   static void am(GalathEntity galath) {
      if (!followOwnerOnFoot(galath)) {
         galath.getDataManager().set(GalathEntity.bT, false);
         galath.ao();
      }
   }

   static boolean followOwnerOnFoot(GalathEntity galath) {
      UUID ownerUuid = GirlSavedData.getManglelieOwnerOf(galath);
      if (ownerUuid == null) {
         return false;
      }

      EntityPlayer owner = galath.world.getPlayerEntityByUUID(ownerUuid);
      if (owner == null) {
         return false;
      }

      BlockPos ownerPos = owner.getPosition();
      if (!isFlightBlocked(galath, ownerPos)) {
         return false;
      }

      if (galath.activeFlight != null) {
         galath.activeFlight.updateFlight(galath);
         galath.activeFlight = null;
      }

      float dist = galath.getDistance(owner);
      PathNavigate navigator = galath.getNavigator();
      if (dist < 4.0F) {
         navigator.clearPath();
         return false;
      }

      if (dist > 16.0F) {
         navigator.clearPath();
         galath.handlePlayerRide(owner);
         return true;
      }

      if (PathUtils.getPathEnd(galath.currentPath).distanceSq(ownerPos) > 16.0) {
         if (!galath.onGround) {
            return true;
         }

         galath.currentPath = getPathToPlayer(galath, owner, ownerPos);
         if (galath.currentPath == null) {
            galath.handlePlayerRide(owner);
         } else {
            navigator.setPath(galath.currentPath, 1.0);
         }
      }

      if (galath.currentPath != null && !galath.currentPath.isFinished()) {
         boolean sprinting = owner.isSprinting() || galath.getDistance(owner) > 7.0F;
         double speed = sprinting ? 0.55F : 0.5;
         double extra = Math.floor(dist / 5.0F) * 0.2;
         speed += extra;
         if (galath.isInWater()) {
            speed *= 60.0;
         }

         navigator.setSpeed(speed);
         galath.getDataManager().set(GalathEntity.bT, sprinting);
         galath.setCurrentAction((Action)null);
         return true;
      } else {
         return false;
      }
   }

   static boolean isFlightBlocked(GalathEntity galath, BlockPos pos) {
      if (galath.activeFlight == null) {
         return true;
      }

      BlockPos selfPos = galath.getPosition();
      int dist = Math.abs(pos.getX() - selfPos.getX()) + Math.abs(pos.getX() - selfPos.getX());
      return dist > 16;
   }

   static void at(GalathEntity galath) {
      if (galath.onGround) {
         if (galath.getMangleliePartnerUUID() == null) {
            if (galath.getCurrentAction() != Action.HUG_MANG) {
               if (!GirlSavedData.isManglelieOwned(GirlSavedData.getManglelieOwnerId(galath.getGirlId()))) {
                  BlockPos center = galath.getPosition();
                  BlockPos min = center.add(-15.0, -15.0, -15.0);
                  BlockPos max = center.add(15.0, 15.0, 15.0);
                  AxisAlignedBB aabb = new AxisAlignedBB(min, max);
                  List manglelies = galath.world.getEntitiesWithinAABB(ManglelieEntity.class, aabb);
                  ManglelieEntity chosen = null;

                  for (ManglelieEntity manglelie : (java.util.Collection<ManglelieEntity>) (manglelies) ) {
                     if (!manglelie.isDead && manglelie.getGalathPartner(true) == null) {
                        chosen = manglelie;
                        break;
                     }
                  }

                  if (chosen == null) {
                     if (galath.getCurrentAction() == Action.RUN) {
                        galath.setCurrentAction((Action)null);
                        galath.getNavigator().clearPath();
                     }
                  } else {
                     galath.pathNavigator = galath.getNavigator();
                     if (chosen.getDistance(galath) <= 3.65F) {
                        galath.pathNavigator.clearPath();
                        galath.setCurrentAction(Action.HUG_MANG);
                        galath.motionX = 0.0;
                        galath.motionY = 0.0;
                        galath.motionZ = 0.0;
                        galath.setTargetPosition(galath.getPositionVector());
                        galath.setAnchored(true);
                        galath.setMangleliePartnerUUID(chosen.getGirlId());
                        chosen.setGalathPartnerUUID(galath.getGirlId());
                        chosen.setCurrentAction(Action.RIDE_MOMMY_HEAD);
                        GirlSavedData.markAsManglelieOwned(galath.getGirlId());
                     } else {
                        Vec3d selfPos = galath.getPositionVector();
                        Vec3d partnerPos = chosen.getPositionVector();
                        Vec3d delta = partnerPos.subtract(selfPos);
                        float yaw = (float)TrigMath.sinDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
                        galath.setYawRotation(yaw);
                        galath.pathNavigator.clearPath();
                        galath.pathNavigator.tryMoveToEntityLiving(chosen, 0.65F);
                        galath.setCurrentAction(Action.RUN);
                     }
                  }
               }
            }
         }
      }
   }

   static void aJ(GalathEntity galath) {
      at(galath);
      galath.ay();
   }

   static void clearFlightData(GalathEntity galath) {
      galath.wanderTarget = null;
      galath.wanderTicks = 0;
      if (galath.activeFlight != null) {
         galath.activeFlight.updateFlight(galath);
         galath.activeFlight = null;
      }
   }

   @Nullable
   static Path getPathToPlayer(GalathEntity galath, EntityPlayer player, BlockPos pos) {
      PathNavigate navigator = galath.getNavigator();
      return navigator.getPathToEntityLiving(player);
   }
}
