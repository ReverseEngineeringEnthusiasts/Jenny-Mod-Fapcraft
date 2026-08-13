package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.networking.GalathBackOffRapePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.gr;







import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EscapeMinigameHud extends Gui {
   static final ResourceLocation l = new ResourceLocation("sexmod", "textures/gui/escape_minigame_ui.png");
   static final int f = 52;
   static final float a = 20.0F;
   static final int p = 35;
   static final float n = 0.08F;
   static final float h = 0.006F;
   static final int m = 2;
   static final float i = 0.33F;
   static boolean g = false;
   static gr q = null;
   static float k = 0.0F;
   static float j = 0.0F;
   static boolean b = true;
   static float d = 0.0F;
   static boolean c = false;
   static Minecraft e = Minecraft.func_71410_x();
   static boolean o = false;

   public static void e_clash735() {
      if (g) {
         if (e.field_71441_e == null) {
            g = false;
            o = false;
            j = 0.0F;
            k = 0.0F;
            d = 0.0F;
            c = false;
         }

         if (c) {
            b = false;
            d++;
            if (d >= 20.0F) {
               g = false;
            }
         } else {
            j++;
            if (j % Math.max(1, 2) == 0.0F) {
               b = !b;
            }

            k = Math.max(0.0F, k - 0.006F);
            if (!(j < 20.0F)) {
               if (j % 35.0F == 0.0F || q == null) {
                  b_clash736();
               }
            }
         }
      }
   }

   static void b_clash736() {
      gr var0 = q;
      Random var1 = new Random();

      do {
         q = gr.values()[var1.nextInt(gr.values().length)];
      } while (var0 == q);
   }

   static void c_clash737() {
      if (g) {
         if (!o) {
            o = true;
            PacketHandler.b.sendToServer(new GalathBackOffRapePacket());
            d_clash739();
         }
      }
   }

   public static void a_clash738() {
      g = true;
      o = false;
      j = 0.0F;
      k = 0.0F;
      d = 0.0F;
      c = false;
   }

   public static void d_clash739() {
      c = true;
      d = 0.0F;
   }

   @SubscribeEvent
   public void a(RenderGameOverlayEvent var1) {
      if (g) {
         if (var1.getType() == ElementType.TEXT) {
            int var2 = var1.getResolution().func_78326_a();
            int var3 = var1.getResolution().func_78328_b();
            float var4 = var1.getPartialTicks();
            e.func_110434_K().func_110577_a(l);
            double var5;
            if (c) {
               var5 = 1.0 - RotationHelper.d((d + var4) / 20.0F);
            } else {
               var5 = Math.min(1.0, RotationHelper.c_clash26((j + var4) / 20.0F));
            }

            int var7 = var3 + 385;
            GlStateManager.func_179094_E();
            GlStateManager.func_179152_a(0.33F, 0.33F, 0.33F);
            GlStateManager.func_179109_b(485.0F, 0.0F, 0.0F);
            int var8 = 4 * var3;
            this.func_73729_b(var2 / 2 - 87, (int)RotationHelper.b(var8, var7, var5), 0, 104, 174, 48);
            this.func_73729_b((int)(var2 / 2.0F - 78.0F), (int)RotationHelper.b(var8, var7 - 52, var5), 52, b && q == gr.A ? 52 : 0, 52, 52);
            this.func_73729_b((int)(var2 / 2.0F - 26.0F), (int)RotationHelper.b(var8, var7 - 52, var5), 104, b && q == gr.S ? 52 : 0, 52, 52);
            this.func_73729_b((int)(var2 / 2.0F + 26.0F), (int)RotationHelper.b(var8, var7 - 52, var5), 156, b && q == gr.D ? 52 : 0, 52, 52);
            this.func_73729_b((int)(var2 / 2.0F - 26.0F), (int)RotationHelper.b(var8, var7 - 104, var5), 0, b && q == gr.W ? 52 : 0, 52, 52);
            this.func_73729_b(var2 / 2 - 87 + 8, (int)RotationHelper.b(var8 - 8, var7 + 8, var5), 8, 152, (int)(158.0F * k), 32);
            GlStateManager.func_179121_F();
         }
      }
   }

   @SubscribeEvent
   public void a(ClientTickEvent var1) {
      if (var1.phase != Phase.END) {
         e_clash735();
      }
   }

   @SubscribeEvent
   public void a(KeyInputEvent var1) {
      GameSettings var2 = Minecraft.func_71410_x().field_71474_y;
      if (GameSettings.func_100015_a(var2.field_74370_x)) {
         if (q == gr.A) {
            k += 0.08F;
         } else {
            k -= 0.04F;
         }
      } else if (GameSettings.func_100015_a(var2.field_74366_z)) {
         if (q == gr.D) {
            k += 0.08F;
         } else {
            k -= 0.04F;
         }
      } else if (GameSettings.func_100015_a(var2.field_74351_w)) {
         if (q == gr.W) {
            k += 0.08F;
         } else {
            k -= 0.04F;
         }
      } else if (GameSettings.func_100015_a(var2.field_74368_y)) {
         if (q == gr.S) {
            k += 0.08F;
         } else {
            k -= 0.04F;
         }
      } else {
         if (k >= 1.0F) {
            c_clash737();
         }
      }
   }

}
