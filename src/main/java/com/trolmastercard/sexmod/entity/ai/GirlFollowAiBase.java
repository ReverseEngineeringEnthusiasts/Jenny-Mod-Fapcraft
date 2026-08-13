package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.Reference;







import java.util.UUID;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class GirlFollowAiBase extends EntityAIBase {
   public BaseGirlEntity d;
   public EntityPlayer a;
   public PathNavigate c;
   public EntityDataManager e;
   public GirlFollowAiBase.GirlFollowAiBaseState f = GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
   public static final double g = 0.5;
   public static final double h = 0.7;
   public static final int b = 60;

   public GirlFollowAiBase(BaseGirlEntity var1) {
      this.d = var1;
      this.c = var1.func_70661_as();
      this.e = var1.func_184212_Q();
   }

   protected void c_clash805() {
      int var2 = 0;

      BlockPos var1;
      do {
         var1 = this.a.func_180425_c().func_177982_a(Reference.f.nextInt(10), 0, Reference.f.nextInt(10));
      } while (++var2 < 20 && !this.d.func_184595_k(var1.func_177958_n(), var1.func_177956_o(), var1.func_177952_p()));

      if (var2 >= 20) {
         this.d.func_70107_b(this.a.field_70165_t, this.a.field_70163_u, this.a.field_70161_v);
      }

      this.d.field_70159_w = 0.0;
      this.d.field_70181_x = 0.0;
      this.d.field_70179_y = 0.0;
   }

   protected double b_clash806() {
      float var1 = this.d.func_70032_d(this.a);
      double var2;
      BaseGirlEntity.BaseGirlEntityState var4;
      if (this.a.func_70051_ag()) {
         var2 = 0.7;
         var4 = BaseGirlEntity.BaseGirlEntityState.RUN;
      } else {
         var2 = 0.5;
         var4 = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      double var5 = Math.floor(var1 / 5.0F) * 0.2;
      var2 += var5;
      if (this.d.func_70090_H()) {
         var2 *= 60.0;
         var4 = BaseGirlEntity.BaseGirlEntityState.WALK;
      }

      this.c.func_75489_a(var2);
      this.d.a(var4);
      return var2;
   }

   public void func_75251_c() {
      this.c.func_75499_g();
      this.f = GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      this.d.b(fp.NULL);
      this.e.func_187227_b(BaseGirlEntity.v, "");
      this.c = null;
      this.e = null;
      this.a = null;
   }

   public boolean func_75250_a() {
      return !((String)this.d.func_184212_Q().func_187225_a(BaseGirlEntity.v)).equals("");
   }

   public boolean func_75253_b() {
      String var1 = (String)this.e.func_187225_a(BaseGirlEntity.v);
      return !var1.equals("") && this.d.field_70170_p.func_152378_a(UUID.fromString(var1)) != null;
   }

   public void func_75249_e() {
      this.c = this.d.func_70661_as();
      this.e = this.d.func_184212_Q();
      this.a = this.d.field_70170_p.func_152378_a(UUID.fromString((String)this.e.func_187225_a(BaseGirlEntity.v)));
   }

   public void func_75246_d() {
      this.f = this.a_clash807();
      if (this.d.o != null) {
         this.d.o.a = this.f == GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      }

      this.a(this.f);
   }

   protected abstract GirlFollowAiBase.GirlFollowAiBaseState a_clash807();

   protected abstract void a(GirlFollowAiBase.GirlFollowAiBaseState var1);

   @SubscribeEvent
   public void a(LivingDeathEvent var1) {
      if (var1.getEntityLiving() instanceof BaseGirlEntity) {
         BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntityLiving();
         if (!((String)var2.func_184212_Q().func_187225_a(BaseGirlEntity.v)).equals("")) {
            var1.setCanceled(true);
         }
      }
   }


   public enum GirlFollowAiBaseState {
      ATTACK,
      FOLLOW,
      IDLE,
      RIDE,
      DOWNED;
   }
}
