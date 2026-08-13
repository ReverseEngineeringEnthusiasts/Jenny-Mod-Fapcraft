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
      d = b.sneak;
      a = b.jump;
      if (!c) {
         if (b.jump) {
            AbstractPlayerGirlEntity.i_clash572();
         }

         if (b.sneak) {
            BaseGirlEntity.k(Minecraft.getMinecraft().player.getPersistentID());
         }

         if (b.jump && HornyMeterHud.c >= 1.0) {
            BaseGirlEntity.f_clash534(Minecraft.getMinecraft().player.getPersistentID());
         }

         b.backKeyDown = false;
         b.forwardKeyDown = false;
         b.leftKeyDown = false;
         b.rightKeyDown = false;
         b.sneak = false;
         b.jump = false;
         b.moveForward = 0.0F;
         b.moveStrafe = 0.0F;
         Minecraft.getMinecraft().player.setVelocity(0.0, 0.0, 0.0);
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
      EntityPlayerSP var0 = Minecraft.getMinecraft().player;
      if (AbstractPlayerGirlEntity.e(var0)) {
         var0.sendStatusMessage(new TextComponentString("Jump to get out of the animation"), true);
      }
   }

   @SubscribeEvent
   public void a(MouseEvent var1) {
      if (!c && var1.isButtonstate()) {
         var1.setCanceled(true);
      }
   }

}
