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
   public static final DataParameter<String> as = EntityDataManager.createKey(AbstractKoboldPlayerEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<BlockPos> au = EntityDataManager.createKey(AbstractKoboldPlayerEntity.class, DataSerializers.BLOCK_POS)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<String> at = EntityDataManager.createKey(AbstractKoboldPlayerEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(121);
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
   protected void entityInit() {
      super.entityInit();
      if (!this.world.isRemote || !(this.world instanceof SexWorldClient)) {
         this.entityDataManager.register(at, this.a(new StringBuilder()));
      }
   }

   protected abstract String a(StringBuilder var1);

   public static String[] a_clash702(BaseGirlEntity var0) {
      return ((String)var0.getDataManager().get(at)).split("-");
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      this.b_clash703();
      if (this.ar) {
         if (this.world.isRemote) {
            this.a_clash354();
            this.ar = true;
         } else {
            EntityPlayer var1 = this.k_clash584();
            if (var1 != null) {
               String var2 = var1.getEntityData().getString("sexmod:GirlSpecific" + NpcType.getNpcType(this));
               this.ar = false;
               if (!"".equals(var2)) {
                  this.setCustomPartList(decodePartIdList(var2));
               }
            }
         }
      }
   }

   void b_clash703() {
      if (this.world.isRemote) {
         String var1 = (String)this.entityDataManager.get(as);
         String var2 = (String)this.entityDataManager.get(at);
         BlockPos var3 = (BlockPos)this.entityDataManager.get(au);
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
