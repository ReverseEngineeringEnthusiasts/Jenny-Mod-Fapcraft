package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.WorldUtils;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Bed-side placement for the beddable girls: the four candidate stand
 * positions around a bed ({@link #SIDE_OFFSETS} with the matching
 * {@link #SIDE_YAWS}) and the scan that picks the free side nearest the girl.
 * <p>
 * Extracted from {@code JennyEntity.goToSexBed}, {@code LunaEntity.goToSexBed}
 * and Bia's {@code getBedVector}/{@code findNearestBedVector}, which each
 * carried a verbatim copy of both the table and the loop. The callers keep
 * their own "no bed" dialogue, sounds and follow-up state; only the geometry
 * and the selection rule live here.
 */
final class GirlBedPlacement {
   /** Yaw the girl faces on each bed side; index-aligned with {@link #SIDE_OFFSETS}. */
   static final int[] SIDE_YAWS = {0, 180, -90, 90};

   /**
    * Per bed side: {@code [0]} = where the girl stands, {@code [1]} = the
    * direction of the block that must be free (the open side of the bed).
    */
   static final Vec3d[][] SIDE_OFFSETS = {
      {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
      {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
      {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
      {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
   };

   private GirlBedPlacement() {
   }

   /**
    * @return index of the nearest bed side whose open block is air, or -1 when
    *         every side is blocked
    */
   static int findFreeSide(BaseGirlEntity girl, Vec3d bedVec, Vec3d[][] sides) {
      return findFreeSide(girl, bedVec, sides, false);
   }

   /**
    * As {@link #findFreeSide}, additionally requiring that a structure can be
    * placed behind the side (Bia's bed rule).
    */
   static int findFreeSideWithRoom(BaseGirlEntity girl, Vec3d bedVec, Vec3d[][] sides) {
      return findFreeSide(girl, bedVec, sides, true);
   }

   private static int findFreeSide(BaseGirlEntity girl, Vec3d bedVec, Vec3d[][] sides, boolean requireRoom) {
      int bestIndex = -1;

      for (int i = 0; i < sides.length; i++) {
         Vec3d openVec = bedVec.add(sides[i][1]);
         boolean free = girl.world.getBlockState(new BlockPos(openVec.x, openVec.y, openVec.z)).getBlock() == Blocks.AIR;
         if (free && requireRoom) {
            Vec3d roomVec = bedVec.subtract(sides[i][1]);
            free = WorldUtils.canPlaceStructure(girl.world, new BlockPos(roomVec));
         }

         if (!free) {
            continue;
         }

         if (bestIndex == -1) {
            bestIndex = i;
         } else {
            Vec3d bestVec = bedVec.add(sides[bestIndex][0]);
            Vec3d sideVec = bedVec.add(sides[i][0]);
            double bestDist = girl.getPosition().distanceSq(bestVec.x, bestVec.y, bestVec.z);
            double dist = girl.getPosition().distanceSq(sideVec.x, sideVec.y, sideVec.z);
            if (dist < bestDist) {
               bestIndex = i;
            }
         }
      }

      return bestIndex;
   }
}
