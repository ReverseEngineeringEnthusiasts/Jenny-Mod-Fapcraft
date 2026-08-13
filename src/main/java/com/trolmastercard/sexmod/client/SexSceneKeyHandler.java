package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;

import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SexSceneKeyHandler {
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(KeyInputEvent var1) {
      if (ClientProxy.keyBindings[2].func_151468_f()) {
         Minecraft var2 = Minecraft.func_71410_x();
         if (var2.field_71439_g == null) {
            return;
         }

         UUID var3 = var2.field_71439_g.getPersistentID();

         try {
            for (BaseGirlEntity var5 : BaseGirlEntity.ad_clash509()) {
               if (var5.field_70170_p.field_72995_K && !var5.field_70128_L && var5.ae_clash498() != null && var5.y_clash492() != fp.NULL) {
                  UUID var6 = var5.ae_clash498();
                  if (var3.equals(var6) || var2.field_71439_g.func_110124_au().equals(var6)) {
                     PacketHandler.b.sendToServer(new ResetGirlPacket(var5.f_clash491()));
                  }
               }
            }
         } catch (ConcurrentModificationException var7) {
         }

         AbstractPlayerGirlEntity var8 = AbstractPlayerGirlEntity.d_clash567(var3);
         if (var8 != null && var8.y_clash492() != fp.NULL) {
            PacketHandler.b.sendToServer(new ResetGirlPacket(var8.f_clash491()));
         }
      }
   }

   private static ConcurrentModificationException a(ConcurrentModificationException var0) {
      return var0;
   }
}
