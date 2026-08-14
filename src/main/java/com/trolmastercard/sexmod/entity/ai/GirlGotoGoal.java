package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;

public class GirlGotoGoal extends GirlFollowAiBase {
   int gotoTicks = 0;
   int retryTicks = 0;

   public GirlGotoGoal(BaseGirlEntity var1) {
      super(var1);
   }

   @Override
   public void resetTask() {
      super.resetTask();
      this.girl.jumpMovementFactor = 0.02F;
   }

   @Override
   protected GirlFollowAiBase.GirlFollowAiBaseState getCurrentState() {
      float var1 = this.girl.getDistance(this.master);
      boolean var2 = var1 > 5.0F;
      if (this.girl.getInteractionPlayerUUID() == null && !var2 && this.state == GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
         if (++this.gotoTicks > 60) {
            var2 = false;
            this.gotoTicks = 0;
         } else {
            var2 = true;
         }
      }

      return var2 ? GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW : GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
   }

   @Override
   protected void setState(GirlFollowAiBase.GirlFollowAiBaseState var1) {
      switch (var1) {
         case FOLLOW:
            double var2 = this.girl.getDistance(this.master);
            if (this.navigator.getPathSearchRange() > var2) {
               this.navigator.clearPath();
               this.navigator.tryMoveToEntityLiving(this.master, 0.5);
            } else {
               this.updateNavigation();
            }

            this.retryTicks = 300;
            this.getFollowDistance();
            break;
         case IDLE:
            this.getFollowDistance();
      }
   }

   @Override
   protected double getFollowDistance() {
      float var1 = this.girl.getDistance(this.master);
      double var3 = Math.min(0.7, Math.floor(var1 / 3.0F) * 0.05);
      float var2 = (float)(0.02F + var3);
      this.girl.jumpMovementFactor = var2;
      return var2;
   }

}
