package com.trolmastercard.sexmod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

/**
 * Static predicate helpers used by the mod's mob AI (e.g. targeting and
 * daylight checks for entities like the slime/goblin NPCs). Pure functions —
 * no state, no side effects; safe to call from any thread.
 */
public class MobPredicates {
   /**
    * Whether {@code var0} is an acceptable combat target. Creepers, zombie
    * pigmen, guardians and endermen are excluded (explosive/reflection hazards).
    *
    * @return {@code false} for the excluded vanilla mobs, {@code true} otherwise
    */
   public static boolean isValidTarget(Entity var0) {
      if (var0 instanceof EntityCreeper) {
         return false;
      } else if (var0 instanceof EntityPigZombie) {
         return false;
      } else {
         return var0 instanceof EntityGuardian ? false : !(var0 instanceof EntityEnderman);
      }
   }

   /**
    * Checks whether the line of sight from {@code var1} to the entity's eye
    * position is unobstructed (no block hit), i.e. the entity is "in daylight".
    *
    * @return {@code true} if no block occludes the ray, {@code false} otherwise
    */
   public static boolean isDaylight(World var0, Vec3d var1, Entity var2) {
      RayTraceResult var3 = var0.rayTraceBlocks(var1, var2.getPositionVector().add(0.0, var2.getEyeHeight(), 0.0), true, true, false);
      return var3 == null ? true : var3.typeOfHit != Type.BLOCK;
   }

}
