package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







public class GirlGotoGoal extends GirlFollowAiBase {
   int j = 0;
   int i = 0;

   public GirlGotoGoal(BaseGirlEntity var1) {
      super(var1);
   }

   @Override
   public void func_75251_c() {
      super.func_75251_c();
      this.d.field_70747_aH = 0.02F;
   }

   @Override
   protected GirlFollowAiBase.GirlFollowAiBaseState a_clash807() {
      float var1 = this.d.func_70032_d(this.a);
      boolean var2 = var1 > 5.0F;
      if (this.d.getInteractionPlayerUUID() == null && !var2 && this.f == GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
         if (++this.j > 60) {
            var2 = false;
            this.j = 0;
         } else {
            var2 = true;
         }
      }

      return var2 ? GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW : GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
   }

   @Override
   protected void a(GirlFollowAiBase.GirlFollowAiBaseState var1) {
      switch (var1) {
         case FOLLOW:
            double var2 = this.d.func_70032_d(this.a);
            if (this.c.func_111269_d() > var2) {
               this.c.func_75499_g();
               this.c.func_75497_a(this.a, 0.5);
            } else {
               this.c_clash805();
            }

            this.i = 300;
            this.b_clash806();
            break;
         case IDLE:
            this.b_clash806();
      }
   }

   @Override
   protected double b_clash806() {
      float var1 = this.d.func_70032_d(this.a);
      double var3 = Math.min(0.7, Math.floor(var1 / 3.0F) * 0.05);
      float var2 = (float)(0.02F + var3);
      this.d.field_70747_aH = var2;
      return var2;
   }

}
