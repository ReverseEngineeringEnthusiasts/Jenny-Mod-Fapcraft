package com.trolmastercard.sexmod.util;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DeprecatedCheckForUpdates {
   final String versionUrl = new String(
      new byte[]{
         104,
         116,
         116,
         112,
         115,
         58,
         47,
         47,
         115,
         99,
         104,
         110,
         117,
         114,
         114,
         105,
         116,
         118,
         46,
         99,
         111,
         109,
         47,
         118,
         101,
         114,
         115,
         105,
         111,
         110,
         46,
         116,
         120,
         116
      }
   );
   final String downloadUrl = new String(
      new byte[]{
         104, 116, 116, 112, 115, 58, 47, 47, 116, 119, 105, 116, 116, 101, 114, 46, 99, 111, 109, 47, 83, 99, 104, 110, 117, 114, 114, 105, 95, 116, 118
      }
   );
   boolean isChecking = false;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(ClientTickEvent var1) {
   }
}
