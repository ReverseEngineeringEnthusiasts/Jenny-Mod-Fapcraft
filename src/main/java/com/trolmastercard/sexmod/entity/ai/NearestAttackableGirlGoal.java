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
 * ({@code hasMaster()}), with optional darkness requirement and a
 * radius-chance gate. Used for hostile mobs that hunt kobold tribe members.
 */
public class NearestAttackableGirlGoal extends EntityAINearestAttackableTarget<KoboldEntity> {
   private final int targetRadius;
   private final boolean isValid;

   public NearestAttackableGirlGoal(EntityCreature creature, boolean mustSee, boolean nearbyOnly) {
      this(creature, mustSee, false, nearbyOnly);
   }

   public NearestAttackableGirlGoal(EntityCreature creature, boolean mustSee, boolean nearbyOnly, boolean onlyNearby) {
      this(creature, 10, mustSee, nearbyOnly, null, onlyNearby);
   }

   public NearestAttackableGirlGoal(EntityCreature creature, int targetChance, boolean checkSight, boolean onlyNearby, @Nullable Predicate targetSelector, boolean isValid) {
      super(creature, KoboldEntity.class, targetChance, checkSight, onlyNearby, targetSelector);
      this.targetRadius = targetChance;
      this.isValid = isValid;
   }

   /**
    * Filters candidates: skips daylight when {@code isValid} is set, applies
    * the 1/radius random gate, then picks the nearest kobold with a master.
    */
   public boolean shouldExecute() {
      if (this.isValid) {
         float brightness = this.taskOwner.getBrightness();
         if (brightness >= 0.5F) {
            return false;
         }
      }

      if (this.targetRadius > 0 && this.taskOwner.getRNG().nextInt(this.targetRadius) != 0) {
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
