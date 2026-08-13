package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.d3;







import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class HornyMeterHud extends Gui {
   static ResourceLocation e = new ResourceLocation("sexmod", "textures/gui/buttons.png");
   static ResourceLocation b = new ResourceLocation("sexmod", "textures/gui/hornymeter.png");
   public static boolean d = false;
   public static double c = 0.0;
   static double a = c;
   static float f = 0.0F;
   static float g = 0.0F;
   static boolean i = false;
   static boolean h = true;

   public static void d_clash358() {
      if (!d) {
         b_clash363();
         d = true;
         h = true;
      }
   }

   public static void a_clash359(boolean var0) {
      if (!d) {
         b_clash363();
         d = true;
         h = var0;
      }
   }

   public static void c_clash360() {
      b_clash363();
      d = false;
      h = true;
   }

   public static boolean a_clash361() {
      return d;
   }

   @SubscribeEvent
   public void a(RenderGameOverlayEvent var1) {
      if (d && var1.getType() == ElementType.TEXT) {
         Minecraft var2 = Minecraft.func_71410_x();
         if (f < 1.0F) {
            f = f + var2.func_193989_ak() / 25.0F;
         } else {
            f = 1.0F;
         }

         GL11.glPushMatrix();
         var2.field_71446_o.func_110577_a(e);
         GL11.glScalef(0.35F, 0.35F, 0.35F);
         if (c >= 1.0) {
            if (d3.a) {
               i = true;
            }

            int var3 = i ? 54 : 0;
            this.func_73729_b(240, 160, 0, 108 + var3, 256, 52);
         }

         if (h && !i) {
            int var7 = d3.d ? 54 : 0;
            this.func_73729_b((int)RotationHelper.a_clash25(-200.0F, 98.0F, f), 405, 0, var7, 158, 54);
         }

         GL11.glScalef(2.857143F, 2.857143F, 2.857143F);
         var2.field_71446_o.func_110577_a(b);
         GL11.glScalef(0.75F, 0.75F, 0.75F);
         this.func_73729_b(10, (int)RotationHelper.a_clash25(-200.0F, 10.0F, f), 0, 0, 146, 175);
         a = RotationHelper.b(a, c, var2.func_193989_ak());
         int var8 = (int)RotationHelper.b(0.0, 160.0, a);
         int var4 = (int)RotationHelper.b(167.0, 8.0, a);
         double var5 = RotationHelper.b(178.0, 18.0, a);
         if (!i) {
            this.func_73729_b(67, (int)RotationHelper.b(-45.0, var5, f), 159, var4, 32, var8);
            this.func_73729_b(
               120,
               (int)RotationHelper.b(-58.0, RotationHelper.b(178.0, 149.0, 1.0 - a), f),
               212,
               (int)RotationHelper.b(169.0, 141.0, 1.0 - a),
               28,
               (int)RotationHelper.b(1.0, 29.0, 1.0 - a)
            );
            this.func_73729_b(
               18,
               (int)RotationHelper.b(-58.0, RotationHelper.b(178.0, 149.0, 1.0 - a), f),
               212,
               (int)RotationHelper.b(169.0, 141.0, 1.0 - a),
               28,
               (int)RotationHelper.b(1.0, 29.0, 1.0 - a)
            );
         } else {
            g = g + var2.func_193989_ak() / 15.0F;
            this.func_73729_b(67, (int)RotationHelper.a_clash25(18.0F, -300.0F, g), 159, 8, 32, 160);
         }

         GL11.glPopMatrix();
      }
   }

   public static void a_clash362(double var0) {
      c += var0;
      c = c > 1.0 ? 1.0 : c;
   }

   public static void b_clash363() {
      c = 0.0;
      i = false;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
