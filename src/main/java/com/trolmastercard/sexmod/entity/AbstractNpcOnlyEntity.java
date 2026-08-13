package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.util.Reference;







import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractNpcOnlyEntity extends BaseGirlEntity {
   public static final DataParameter<String> N = EntityDataManager.func_187226_a(AbstractNpcOnlyEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(119);
   public static final DataParameter<BlockPos> K = EntityDataManager.func_187226_a(AbstractNpcOnlyEntity.class, DataSerializers.field_187200_j)
      .func_187156_b()
      .func_187161_a(120);
   public static final DataParameter<String> M = EntityDataManager.func_187226_a(AbstractNpcOnlyEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(121);
   String P = null;
   String O = null;
   BlockPos L = null;

   protected AbstractNpcOnlyEntity(World var1) {
      super(var1);
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      if (!this.field_70170_p.field_72995_K || !(this.field_70170_p instanceof SexWorldClient)) {
         this.m.func_187214_a(M, this.a(new StringBuilder()));
      }
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      this.c_clash221();
   }

   void c_clash221() {
      if (this.field_70170_p.field_72995_K) {
         String var1 = (String)this.m.func_187225_a(N);
         String var2 = (String)this.m.func_187225_a(M);
         BlockPos var3 = (BlockPos)this.m.func_187225_a(K);
         if (this.P == null) {
            this.P = var1;
            this.O = var2;
            this.L = var3;
         } else {
            if (!this.O.equals(var2) || !this.P.equals(var1) || !this.L.equals(var3)) {
               this.a_clash222();
            }

            this.P = var1;
            this.O = var2;
            this.L = var3;
         }
      }
   }

   protected abstract void a_clash222();

   protected abstract String a(StringBuilder var1);

   public static void c(StringBuilder var0, int var1) {
      if (var1 < 10) {
         var0.append(0);
      }

      var0.append(var1);
      var0.append("-");
   }

   public static void appendPaddedNumber(StringBuilder var0, int var1) {
      int var2 = Reference.f.nextInt(var1 + 1);
      if (var2 < 10) {
         var0.append(0);
      }

      var0.append(var2);
      var0.append("-");
   }

   public static void b_clash224(StringBuilder var0) {
      double var1 = Reference.f.nextDouble();
      double var3 = Math.pow(Math.E, -Math.pow(-2.5 + 5.0 * var1, 2.0));
      String var5 = String.format("%.2f", var3);
      String[] var6 = var5.split("\\.");
      if (var6.length < 2) {
         var6 = var5.split(",");
      }

      var5 = var6[1];
      var0.append(var5).append("-");
   }

   public static void b(StringBuilder var0, int var1) {
      int var2 = Reference.f.nextInt(var1);
      if (var2 < 10) {
         var0.append(0);
      }

      var0.append(var2);
      var0.append("-");
   }

   public static String[] getModelCodeParts(BaseGirlEntity var0) {
      return ((String)var0.func_184212_Q().func_187225_a(M)).split("-");
   }

}
