package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;





import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class eo {
   @SubscribeEvent(priority = EventPriority.LOW)
   public void a(LivingDeathEvent var1) {
      if (var1.getEntity() instanceof BaseGirlEntity) {
         BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntity();
         BaseGirlEntity.getGirlEntityList().remove(var2);
      } else if (var1.getEntity() instanceof EntityPlayer) {
         EntityPlayer var3 = (EntityPlayer)var1.getEntity();
         AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.a_clash568(var3.getPersistentID());
         if (var4 != null) {
            ResetGirlPacket.Handler.a_clash10(var4);
         }
      }
   }
}
