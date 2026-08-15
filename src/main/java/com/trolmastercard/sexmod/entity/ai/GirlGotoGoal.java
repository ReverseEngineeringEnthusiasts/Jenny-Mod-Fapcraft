package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;

/**
 * <b>Role.</b> Simplified follow goal for the Bee NPC — follows the master
 * (walking, no combat/ride states) and raises her air-jump factor with
 * distance so she can hop terrain. Only FOLLOW/IDLE states are used.
 */
public class GirlGotoGoal extends GirlFollowAiBase {
   int gotoTicks = 0;
   int retryTicks = 0;

   public GirlGotoGoal(BaseGirlEntity girl) {
      super(girl);
   }

   @Override
   public void resetTask() {
      super.resetTask();
      this.girl.jumpMovementFactor = 0.02F;
   }

   @Override
   protected GirlFollowAiBase.GirlFollowAiBaseState getCurrentState() {
      float dist = this.girl.getDistance(this.master);
      boolean tooFar = dist > 5.0F;
      if (this.girl.getInteractionPlayerUUID() == null && !tooFar && this.state == GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
         if (++this.gotoTicks > 60) {
            tooFar = false;
            this.gotoTicks = 0;
         } else {
            tooFar = true;
         }
      }

      return tooFar ? GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW : GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
   }

   @Override
   protected void setState(GirlFollowAiBase.GirlFollowAiBaseState state) {
      switch (state) {
         case FOLLOW:
            double dist = this.girl.getDistance(this.master);
            if (this.navigator.getPathSearchRange() > dist) {
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

   /**
    * Scales the girl's jump-movement factor with the distance to the master
    * (0.02 + up to 0.07) so closer follows are more stable.
    */
   @Override
   protected double getFollowDistance() {
      float dist = this.girl.getDistance(this.master);
      double speedMod = Math.min(0.7, Math.floor(dist / 3.0F) * 0.05);
      float jumpFactor = (float)(0.02F + speedMod);
      this.girl.jumpMovementFactor = jumpFactor;
      return jumpFactor;
   }

}
