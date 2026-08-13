package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.client.SexWorldClient;







import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractKoboldPlayerEntity extends AbstractPlayerGirlEntity {
   public static final DataParameter<String> as = EntityDataManager.func_187226_a(AbstractKoboldPlayerEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(119);
   public static final DataParameter<BlockPos> au = EntityDataManager.func_187226_a(AbstractKoboldPlayerEntity.class, DataSerializers.field_187200_j)
      .func_187156_b()
      .func_187161_a(120);
   public static final DataParameter<String> at = EntityDataManager.func_187226_a(AbstractKoboldPlayerEntity.class, DataSerializers.field_187194_d)
      .func_187156_b()
      .func_187161_a(121);
   boolean ar = true;
   String ap = null;
   String av = null;
   BlockPos aq = null;

   protected AbstractKoboldPlayerEntity(World var1) {
      super(var1);
   }

   protected AbstractKoboldPlayerEntity(World var1, UUID var2) {
      super(var1, var2);
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      if (!this.field_70170_p.field_72995_K || !(this.field_70170_p instanceof SexWorldClient)) {
         this.m.func_187214_a(at, this.a(new StringBuilder()));
      }
   }

   protected abstract String a(StringBuilder var1);

   public static String[] a_clash702(BaseGirlEntity var0) {
      return ((String)var0.func_184212_Q().func_187225_a(at)).split("-");
   }

   @Override
   public void func_70071_h_() {
      super.func_70071_h_();
      this.b_clash703();
      if (this.ar) {
         if (this.field_70170_p.field_72995_K) {
            this.a_clash354();
            this.ar = true;
         } else {
            EntityPlayer var1 = this.k_clash584();
            if (var1 != null) {
               String var2 = var1.getEntityData().func_74779_i("sexmod:GirlSpecific" + NpcType.getNpcType(this));
               this.ar = false;
               if (!"".equals(var2)) {
                  this.a_clash245(c_clash554(var2));
               }
            }
         }
      }
   }

   void b_clash703() {
      if (this.field_70170_p.field_72995_K) {
         String var1 = (String)this.m.func_187225_a(as);
         String var2 = (String)this.m.func_187225_a(at);
         BlockPos var3 = (BlockPos)this.m.func_187225_a(au);
         if (this.ap == null) {
            this.ap = var1;
            this.av = var2;
            this.aq = var3;
         } else {
            if (!this.av.equals(var2) || !this.ap.equals(var1) || !this.aq.equals(var3)) {
               this.a_clash354();
            }

            this.ap = var1;
            this.av = var2;
            this.aq = var3;
         }
      }
   }

   protected abstract void a_clash354();

}
