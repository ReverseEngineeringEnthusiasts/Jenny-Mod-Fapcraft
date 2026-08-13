package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class d3 {
   private static boolean c = true;
   public static boolean d = false;
   public static boolean a = false;
   public static MovementInput b;

   @SubscribeEvent
   public void a(InputUpdateEvent var1) {
      b = var1.getMovementInput();
      d = b.field_78899_d;
      a = b.field_78901_c;
      if (!c) {
         if (b.field_78901_c) {
            AbstractPlayerGirlEntity.i_clash572();
         }

         if (b.field_78899_d) {
            BaseGirlEntity.k(Minecraft.func_71410_x().field_71439_g.getPersistentID());
         }

         if (b.field_78901_c && HornyMeterHud.c >= 1.0) {
            BaseGirlEntity.f_clash534(Minecraft.func_71410_x().field_71439_g.getPersistentID());
         }

         b.field_187256_d = false;
         b.field_187255_c = false;
         b.field_187257_e = false;
         b.field_187258_f = false;
         b.field_78899_d = false;
         b.field_78901_c = false;
         b.field_192832_b = 0.0F;
         b.field_78902_a = 0.0F;
         Minecraft.func_71410_x().field_71439_g.func_70016_h(0.0, 0.0, 0.0);
      }
   }

   public static boolean b_clash121() {
      return c;
   }

   public static void setMovementLock(boolean var0) {
      c = var0;
      if (!var0) {
         a_clash123();
      }
   }

   @SideOnly(Side.CLIENT)
   static void a_clash123() {
      EntityPlayerSP var0 = Minecraft.func_71410_x().field_71439_g;
      if (AbstractPlayerGirlEntity.e(var0)) {
         var0.func_146105_b(new TextComponentString("Jump to get out of the animation"), true);
      }
   }

   @SubscribeEvent
   public void a(MouseEvent var1) {
      if (!c && var1.isButtonstate()) {
         var1.setCanceled(true);
      }
   }

}
