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

public class HandlePlayerMovement {
   private static boolean isSneaking = true;
   public static boolean isJumping = false;
   public static boolean isInAction = false;
   public static MovementInput input;

   @SubscribeEvent
   public void a(InputUpdateEvent var1) {
      input = var1.getMovementInput();
      isJumping = input.sneak;
      isInAction = input.jump;
      if (!isSneaking) {
         if (input.jump) {
            AbstractPlayerGirlEntity.rebuildPlayerGirlTable();
         }

         if (input.sneak) {
            BaseGirlEntity.triggerFastSexAction(Minecraft.getMinecraft().player.getPersistentID());
         }

         if (input.jump && HornyMeterHud.meterValue >= 1.0) {
            BaseGirlEntity.triggerCumAction(Minecraft.getMinecraft().player.getPersistentID());
         }

         input.backKeyDown = false;
         input.forwardKeyDown = false;
         input.leftKeyDown = false;
         input.rightKeyDown = false;
         input.sneak = false;
         input.jump = false;
         input.moveForward = 0.0F;
         input.moveStrafe = 0.0F;
         Minecraft.getMinecraft().player.setVelocity(0.0, 0.0, 0.0);
      }
   }

   public static boolean isSneakingState() {
      return isSneaking;
   }

   public static void setMovementLock(boolean var0) {
      isSneaking = var0;
      if (!var0) {
         handlePlayerMovementTick();
      }
   }

   @SideOnly(Side.CLIENT)
   static void handlePlayerMovementTick() {
      EntityPlayerSP var0 = Minecraft.getMinecraft().player;
      if (AbstractPlayerGirlEntity.e(var0)) {
         var0.sendStatusMessage(new TextComponentString("Jump to get out of the animation"), true);
      }
   }

   @SubscribeEvent
   public void a(MouseEvent var1) {
      if (!isSneaking && var1.isButtonstate()) {
         var1.setCanceled(true);
      }
   }

}
