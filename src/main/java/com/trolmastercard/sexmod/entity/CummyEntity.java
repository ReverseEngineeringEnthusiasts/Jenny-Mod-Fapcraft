package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.api.b8;
import com.trolmastercard.sexmod.util.ep;







import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CummyEntity {
   static final ResourceLocation b = new ResourceLocation("sexmod", "textures/cummy.png");
   static Minecraft c = Minecraft.func_71410_x();
   static List<ep> a = new ArrayList<>();

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderWorldLastEvent var1) {
      c.field_71446_o.func_110577_a(b);
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
      Tessellator var2 = Tessellator.func_178181_a();
      BufferBuilder var3 = var2.func_178180_c();
      float var4 = var1.getPartialTicks();
      GlStateManager.func_179140_f();
      GlStateManager.func_179141_d();
      if (c.field_71439_g != null) {
         for (ep var6 : a) {
            var6.a(c, var2, var3, var4);
         }

         GlStateManager.func_179126_j();
         GlStateManager.func_179145_e();
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(ClientTickEvent var1) {
      if (var1.phase != Phase.END) {
         for (ep var3 : a) {
            var3.a_clash449();
         }
      }
   }

   public static void a(ep var0) {
      a.add(var0);
   }

   public static void a(int var0, ar var1, b8 var2, BaseGirlEntity var3, float var4, float var5) {
      a.add(new ep(var0, var1, var2, var3, var4, var5));
   }

   public static void a_clash747(@Nonnull BaseGirlEntity var0) {
      ArrayList var1 = new ArrayList();

      for (ep var3 : a) {
         if (var3.e.getGirlId().equals(var0.getGirlId())) {
            var1.add(var3);
         }
      }

      a.removeAll(var1);
   }

}
