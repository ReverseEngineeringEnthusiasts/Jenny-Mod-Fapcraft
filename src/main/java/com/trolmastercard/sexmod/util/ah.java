package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ah {
   @SubscribeEvent
   public void b(LivingAttackEvent var1) {
      if (var1.getSource() != DamageSource.field_76380_i) {
         if (var1.getEntity() instanceof BaseGirlEntity) {
            BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntity();
            if (var2 instanceof AbstractPlayerGirlEntity) {
               var1.setCanceled(true);
            } else {
               var1.setCanceled(var2.getInteractionPlayerUUID() != null);
            }
         }
      }
   }

   @SubscribeEvent
   public void a(LivingAttackEvent var1) {
      DamageSource var2 = var1.getSource();
      if (var2 != DamageSource.field_76380_i && !(var2 instanceof SuccubusDamageSource)) {
         if (var1.getEntity() instanceof EntityPlayer) {
            EntityPlayer var3 = (EntityPlayer)var1.getEntity();
            BaseGirlEntity var4 = BaseGirlEntity.i_clash530(var3.getPersistentID());
            if (var4 != null) {
               if (var4.func_70032_d(var3) < 1.0F) {
                  var1.setCanceled(true);
               }
            }
         }
      }
   }

}
