package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

/**
 * <b>Role.</b> Nearest-target goal that only attacks tamed kobolds
 * ({@code hasMaster()}), with an optional darkness requirement
 * ({@code onlyAtNight}) and a 1-in-{@code targetChance} activation gate. Used
 * for hostile mobs that hunt kobold tribe members.
 */
public class NearestAttackableGirlGoal extends EntityAINearestAttackableTarget<KoboldEntity> {
   private final int targetChance;
   private final boolean onlyAtNight;

   public NearestAttackableGirlGoal(EntityCreature creature, boolean mustSee, boolean nearbyOnly) {
      this(creature, 10, mustSee, nearbyOnly, null, false);
   }

   public NearestAttackableGirlGoal(EntityCreature creature, boolean mustSee, boolean nearbyOnly, boolean onlyAtNight) {
      this(creature, 10, mustSee, nearbyOnly, null, onlyAtNight);
   }

   public NearestAttackableGirlGoal(EntityCreature creature, int targetChance, boolean checkSight, boolean onlyNearby, @Nullable Predicate targetSelector, boolean onlyAtNight) {
      super(creature, KoboldEntity.class, targetChance, checkSight, onlyNearby, targetSelector);
      this.targetChance = targetChance;
      this.onlyAtNight = onlyAtNight;
   }

   /**
    * Filters candidates: skips daylight when {@code onlyAtNight} is set, applies
    * the 1-in-{@code targetChance} random gate, then picks the nearest kobold
    * with a master.
    */
   public boolean shouldExecute() {
      if (this.onlyAtNight) {
         float brightness = this.taskOwner.getBrightness();
         if (brightness >= 0.5F) {
            return false;
         }
      }

      if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
         return false;
      }

      List candidates = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
      if (candidates.isEmpty()) {
         return false;
      }

      ArrayList masters = new ArrayList();

      for (KoboldEntity kobold : (java.util.Collection<KoboldEntity>) (candidates) ) {
         if (kobold.hasMaster()) {
            masters.add(kobold);
         }
      }

      if (masters.isEmpty()) {
         return false;
      }

      masters.sort(this.sorter);
      this.targetEntity = (KoboldEntity) masters.get(0);
      return true;
   }

}
