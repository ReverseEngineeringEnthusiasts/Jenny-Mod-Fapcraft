package com.trolmastercard.sexmod;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;







import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PositionData {
   Vec3d b = null;
   Vec3d a = null;

   @SubscribeEvent
   public void a(Pre var1) {
      try {
         for (BaseGirlEntity var3 : BaseGirlEntity.ad_clash509()) {
            if (!var3.field_70128_L && var3.ae_clash498() != null && var3.y_clash492() != fp.NULL) {
               EntityPlayer var4 = var1.getEntityPlayer();
               if (var3.y_clash492().hasPlayer && (var3.ae_clash498().equals(var4.getPersistentID()) || var3.ae_clash498().equals(var4.func_110124_au()))) {
                  var1.setCanceled(true);
                  return;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   @SubscribeEvent
   public void a(RenderHandEvent var1) {
      Minecraft var2 = Minecraft.func_71410_x();
      EntityPlayerSP var3 = var2.field_71439_g;
      AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.g(var3);
      if (var4 != null && var4.Q_clash505()) {
         var1.setCanceled(true);
      } else {
         try {
            for (BaseGirlEntity var6 : BaseGirlEntity.ad_clash509()) {
               UUID var7 = var6.ae_clash498();
               fp var8 = var6.y_clash492();
               if (!var6.field_70128_L
                  && var7 != null
                  && var8 != null
                  && var8.hasPlayer
                  && (var7.equals(var3.func_110124_au()) || var7.equals(var3.getPersistentID()))) {
                  var1.setCanceled(true);
                  return;
               }
            }
         } catch (ConcurrentModificationException var9) {
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderTickEvent var1) {
      Minecraft var2 = Minecraft.func_71410_x();
      if (var2.field_71439_g != null) {
         if (var1.phase == Phase.END) {
            if (this.b != null) {
               var2.field_71439_g.func_70107_b(this.b.field_72450_a, this.b.field_72448_b, this.b.field_72449_c);
               var2.field_71439_g.field_70142_S = this.a.field_72450_a;
               var2.field_71439_g.field_70137_T = this.a.field_72448_b;
               var2.field_71439_g.field_70136_U = this.a.field_72449_c;
               this.b = null;
               this.a = null;
            }
         } else if (var2.field_71474_y.field_74320_O == 0) {
            BaseGirlEntity var3 = BaseGirlEntity.a(var2.field_71439_g.getPersistentID(), Boolean.valueOf(false));
            if (var3 != null) {
               if (var3.y_clash492().useBoyCam) {
                  if (!var3.m_clash494()) {
                     this.b = var2.field_71439_g.func_174791_d();
                     this.a = new Vec3d(var2.field_71439_g.field_70142_S, var2.field_71439_g.field_70137_T, var2.field_71439_g.field_70136_U);
                     Vec3d var4 = var3.Q_clash505()
                        ? var3.b_clash547("boyCam").func_178787_e(var3.o_clash501())
                        : var3.b_clash547("boyCam")
                           .func_178787_e(
                              RotationHelper.a(new Vec3d(var3.field_70142_S, var3.field_70137_T, var3.field_70136_U), var3.func_174791_d(), var1.renderTickTime)
                           );
                     var2.field_71439_g.field_70165_t = var4.field_72450_a;
                     var2.field_71439_g.field_70163_u = var4.field_72448_b - var2.field_71439_g.func_70047_e();
                     var2.field_71439_g.field_70161_v = var4.field_72449_c;
                     var2.field_71439_g.field_70142_S = var4.field_72450_a;
                     var2.field_71439_g.field_70137_T = var4.field_72448_b - var2.field_71439_g.func_70047_e();
                     var2.field_71439_g.field_70136_U = var4.field_72449_c;
                  }
               }
            }
         }
      }
   }

   private static ConcurrentModificationException a(ConcurrentModificationException var0) {
      return var0;
   }
}
