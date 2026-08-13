package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;







import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AvoidPlayerGoal extends EntityAIAvoidEntity<EntityPlayer> {
   final ManglelieEntity a;
   final float b;

   public AvoidPlayerGoal(ManglelieEntity var1, float var2, double var3, double var5) {
      super(var1, EntityPlayer.class, var2, var3, var5);
      this.a = var1;
      this.b = var2;
   }

   boolean a_clash220() {
      if (this.a.v_clash412() != null) {
         return true;
      }

      BlockPos var1 = this.a.func_180425_c();
      BlockPos var2 = new BlockPos(this.b, this.b, this.b);

      for (GalathEntity var5 : this.a.field_70170_p.func_72872_a(GalathEntity.class, new AxisAlignedBB(var1.func_177971_a(var2), var1.func_177973_b(var2)))) {
         if (!var5.field_70170_p.field_72995_K && !var5.field_70128_L && var5.k_clash637()) {
            return true;
         }
      }

      return false;
   }

   public boolean func_75250_a() {
      return this.a_clash220() ? false : super.func_75250_a();
   }

   public boolean func_75253_b() {
      return this.a_clash220() ? false : super.func_75253_b();
   }

   public void func_75249_e() {
      this.a.func_184212_Q().func_187227_b(ManglelieEntity.ar, true);
      super.func_75249_e();
   }

   public void func_75251_c() {
      this.a.func_184212_Q().func_187227_b(ManglelieEntity.ar, false);
      super.func_75251_c();
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
