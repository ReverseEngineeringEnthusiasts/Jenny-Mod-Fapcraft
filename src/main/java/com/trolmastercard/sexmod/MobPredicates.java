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
    * Whether {@code target} is an acceptable combat target. Creepers, zombie
    * pigmen, guardians and endermen are excluded (explosive/reflection hazards).
    *
    * @return {@code false} for the excluded vanilla mobs, {@code true} otherwise
    */
   public static boolean isValidTarget(Entity target) {
      if (target instanceof EntityCreeper) {
         return false;
      } else if (target instanceof EntityPigZombie) {
         return false;
      } else {
         return target instanceof EntityGuardian ? false : !(target instanceof EntityEnderman);
      }
   }

   /**
    * Checks whether the line of sight from {@code from} to the entity's eye
    * position is unobstructed (no block hit), i.e. the entity is "in daylight".
    *
    * @return {@code true} if no block occludes the ray, {@code false} otherwise
    */
   public static boolean isDaylight(World world, Vec3d from, Entity entity) {
      RayTraceResult result = world.rayTraceBlocks(from, entity.getPositionVector().add(0.0, entity.getEyeHeight(), 0.0), true, true, false);
      return result == null ? true : result.typeOfHit != Type.BLOCK;
   }

}
