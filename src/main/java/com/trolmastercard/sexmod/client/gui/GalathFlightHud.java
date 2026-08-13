package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.bl;







import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GalathFlightHud extends Gui {
   static final ResourceLocation j = new ResourceLocation("sexmod", "textures/gui/galath_flight_ui.png");
   static final bl i = new bl(0, 77, 128, 41);
   static final bl w = new bl(0, 0, 23, 36);
   static final bl k = new bl(0, 36, 23, 36);
   static final bl p = new bl(23, 2, 20, 31);
   static final long l = 500L;
   static final float d = 150.0F;
   static final float m = 0.075F;
   static final float b = -11.25F;
   static final float[] x = new float[]{-14.25F, -15.5F, -16.875F};
   static final float h = 500.0F;
   static final float o = -0.15F;
   static final float r = 37.5F;
   static final float[] t = new float[]{37.5F, 43.0F, 45.0F};
   static final int v = 70;
   static final int a = 70;
   static boolean q = false;
   static Minecraft c = Minecraft.func_71410_x();
   static int e = 3;
   static long s = 0L;
   static long f = 0L;
   static long u = 0L;
   static long g = 9223372036854775307L;

   public static boolean d_clash788() {
      return e <= 0 ? false : System.currentTimeMillis() - s > 3000L;
   }

   public static void a_clash789() {
      e--;
      s = System.currentTimeMillis();
   }

   void b_clash790() {
      if (e != 3) {
         long var1 = System.currentTimeMillis();
         if (var1 - Math.max(s, f) >= 5000L) {
            e++;
            f = var1;
         }
      }
   }

   @SubscribeEvent
   public void a(RenderGameOverlayEvent var1) {
      this.b_clash790();
      if (q) {
         ScaledResolution var2 = var1.getResolution();
         int var3 = var2.func_78326_a();
         int var4 = var2.func_78328_b();
         int var5 = var3 / 2;
         long var6 = System.currentTimeMillis();
         if (var6 - g > 500L) {
            e_clash793();
         } else {
            c.func_110434_K().func_110577_a(j);
            GlStateManager.func_179147_l();
            GlStateManager.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.func_179141_d();
            float var8;
            if (var6 < u + 500L) {
               var8 = (float)(var6 - u) / 500.0F;
            } else if (var6 < g + 500L) {
               var8 = 1.0F + (float)(g - var6) / 500.0F;
            } else {
               var8 = 1.0F;
            }

            var8 = ThreadNames.b(var8, 0.0F, 1.0F);
            GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, var8);
            this.a(i, var5 - i.c / 2, var4 - 70);
            this.a(p, (int)(var5 - 1.5F * w.c + 1.0F), var4 - 70 + 3);
            this.a(p, var5 - w.c / 2 + 1, var4 - 70 + 3);
            this.a(p, var5 + w.c / 2 + 1, var4 - 70 + 3);
            float var9 = (float)RotationHelper.b(Math.min(1.0F, (float)(var6 - s) / 150.0F));
            float var10 = var9 == 1.0F ? ThreadNames.b(1.0F - (float)(var6 - f) / 500.0F, 0.0F, 1.0F) : 0.0F;
            this.a(1, -1.5F * w.c, var10, var9, var5, var4, var8);
            this.a(2, -w.c / 2.0F, var10, var9, var5, var4, var8);
            this.a(3, w.c / 2.0F, var10, var9, var5, var4, var8);
         }
      }
   }

   void a(int var1, float var2, float var3, float var4, int var5, int var6, float var7) {
      float var8;
      if (e >= var1) {
         var8 = 0.0F;
      } else if (e < var1 - 1) {
         var8 = 1.0F;
      } else {
         var8 = var4;
      }

      float var9;
      if (e == var1) {
         var9 = var3;
      } else {
         var9 = 0.0F;
      }

      float var10 = 1.0F + var8 * 0.075F + var9 * -0.15F;
      GlStateManager.func_179094_E();
      GlStateManager.func_179152_a(var10, var10, var10);
      GlStateManager.func_179109_b(var8 * x[var1 - 1] + var9 * t[var1 - 1], var8 * -11.25F + var9 * 37.5F, 0.0F);
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, var7 - var8 - var9);
      this.a(w, (int)(var5 + var2), var6 - 70);
      GlStateManager.func_179117_G();
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, (float)Math.sin(Math.PI * var8) * 0.5F);
      this.a(k, (int)(var5 + var2), var6 - 70);
      GlStateManager.func_179121_F();
      GlStateManager.func_179117_G();
   }

   public static void f_clash791() {
      if (!q) {
         q = true;
         u = System.currentTimeMillis();
         g = 9223372036854775307L;
      }
   }

   public static void c_clash792() {
      g = System.currentTimeMillis();
   }

   public static void e_clash793() {
      q = false;
      g = 9223372036854775307L;
      u = 0L;
   }

   void a(bl var1, int var2, int var3) {
      this.func_73729_b(var2, var3, var1.a, var1.d, var1.c, var1.b);
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
