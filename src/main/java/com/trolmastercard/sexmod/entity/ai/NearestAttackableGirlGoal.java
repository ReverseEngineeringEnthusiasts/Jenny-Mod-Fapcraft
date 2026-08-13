package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.KoboldEntity;







import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

public class NearestAttackableGirlGoal extends EntityAINearestAttackableTarget<KoboldEntity> {
   private final int targetRadius;
   private final boolean isValid;

   public NearestAttackableGirlGoal(EntityCreature var1, boolean var2, boolean var3) {
      this(var1, var2, false, var3);
   }

   public NearestAttackableGirlGoal(EntityCreature var1, boolean var2, boolean var3, boolean var4) {
      this(var1, 10, var2, var3, null, var4);
   }

   public NearestAttackableGirlGoal(EntityCreature var1, int var2, boolean var3, boolean var4, @Nullable Predicate var5, boolean var6) {
      super(var1, KoboldEntity.class, var2, var3, var4, var5);
      this.targetRadius = var2;
      this.isValid = var6;
   }

   public boolean shouldExecute() {
      if (this.isValid) {
         float var1 = this.taskOwner.getBrightness();
         if (var1 >= 0.5F) {
            return false;
         }
      }

      if (this.targetRadius > 0 && this.taskOwner.getRNG().nextInt(this.targetRadius) != 0) {
         return false;
      }

      List var5 = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
      if (var5.isEmpty()) {
         return false;
      }

      ArrayList var2 = new ArrayList();

      for (KoboldEntity var4 : (java.util.Collection<KoboldEntity>) (var5) ) {
         if (var4.hasMaster()) {
            var2.add(var4);
         }
      }

      if (var2.isEmpty()) {
         return false;
      }

      var2.sort(this.sorter);
      this.targetEntity = (KoboldEntity) var2.get(0);
      return true;
   }

}
