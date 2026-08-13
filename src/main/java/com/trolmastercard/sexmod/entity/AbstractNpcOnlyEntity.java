package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.util.Reference;







import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractNpcOnlyEntity extends BaseGirlEntity {
   public static final DataParameter<String> N = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<BlockPos> K = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.BLOCK_POS)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<String> M = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(121);
   String P = null;
   String O = null;
   BlockPos L = null;

   protected AbstractNpcOnlyEntity(World var1) {
      super(var1);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      if (!this.world.isRemote || !(this.world instanceof SexWorldClient)) {
         this.entityDataManager.register(M, this.a(new StringBuilder()));
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.c_clash221();
   }

   void c_clash221() {
      if (this.world.isRemote) {
         String var1 = (String)this.entityDataManager.get(N);
         String var2 = (String)this.entityDataManager.get(M);
         BlockPos var3 = (BlockPos)this.entityDataManager.get(K);
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
      return ((String)var0.getDataManager().get(M)).split("-");
   }

}
