package com.trolmastercard.sexmod.util;

import java.util.ArrayList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

/**
 * Pathfinding helpers for girl movement goals.
 */
public class PathUtils {
   public static BlockPos getPathEnd(Path path) {
      if (path == null) {
         return BlockPos.ORIGIN;
      }

      PathPoint endPoint = path.getFinalPathPoint();
      return endPoint == null ? BlockPos.ORIGIN : new BlockPos(endPoint.x, endPoint.y, endPoint.z);
   }

   public static BlockPos getNavigatorTarget(EntityLiving entity) {
      PathNavigate navigator = entity.getNavigator();
      Path path = navigator.getPath();
      return getPathEnd(path);
   }

   public static boolean isPathValid(Path path, BlockPos[] targets) {
      int length = path.getCurrentPathLength();
      ArrayList points = new ArrayList();

      for (int i = 0; i < length; i++) {
         points.add(path.getPathPointFromIndex(i));
      }

      for (PathPoint point : (java.util.Collection<PathPoint>) (points) ) {
         for (BlockPos target : targets) {
            if (point.x == target.getX() && point.y == target.getY() && point.z == target.getZ()) {
               return true;
            }
         }
      }

      return false;
   }

}
