package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.util.Reference;







import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractNpcOnlyEntity extends BaseGirlEntity {
   public static final DataParameter<String> CURRENT_ACTION = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<BlockPos> ACTION_TARGET_POS = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.BLOCK_POS)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<String> APPEARANCE_DNA = EntityDataManager.createKey(AbstractNpcOnlyEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(121);
   String lastCachedAction = null;
   String lastCachedDNA = null;
   BlockPos lastCachedTargetPos = null;

   protected AbstractNpcOnlyEntity(World var1) {
      super(var1);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      if (!this.world.isRemote || !(this.world instanceof SexWorldClient)) {
         this.entityDataManager.register(APPEARANCE_DNA, this.a(new StringBuilder()));
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.c_clash221();
   }

   void c_clash221() {
      if (this.world.isRemote) {
         String var1 = (String)this.entityDataManager.get(CURRENT_ACTION);
         String var2 = (String)this.entityDataManager.get(APPEARANCE_DNA);
         BlockPos var3 = (BlockPos)this.entityDataManager.get(ACTION_TARGET_POS);
         if (this.lastCachedAction == null) {
            this.lastCachedAction = var1;
            this.lastCachedDNA = var2;
            this.lastCachedTargetPos = var3;
         } else {
            if (!this.lastCachedDNA.equals(var2) || !this.lastCachedAction.equals(var1) || !this.lastCachedTargetPos.equals(var3)) {
               this.clearBoneColors();
            }

            this.lastCachedAction = var1;
            this.lastCachedDNA = var2;
            this.lastCachedTargetPos = var3;
         }
      }
   }

   protected abstract void clearBoneColors();

   protected abstract String a(StringBuilder var1);

   public static void c(StringBuilder var0, int var1) {
      if (var1 < 10) {
         var0.append(0);
      }

      var0.append(var1);
      var0.append("-");
   }

   public static void appendPaddedNumber(StringBuilder var0, int var1) {
      int var2 = Reference.RANDOM.nextInt(var1 + 1);
      if (var2 < 10) {
         var0.append(0);
      }

      var0.append(var2);
      var0.append("-");
   }

   public static void b_clash224(StringBuilder var0) {
      double var1 = Reference.RANDOM.nextDouble();
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
      int var2 = Reference.RANDOM.nextInt(var1);
      if (var2 < 10) {
         var0.append(0);
      }

      var0.append(var2);
      var0.append("-");
   }

   public static String[] getModelCodeParts(BaseGirlEntity var0) {
      return ((String)var0.getDataManager().get(APPEARANCE_DNA)).split("-");
   }

}
