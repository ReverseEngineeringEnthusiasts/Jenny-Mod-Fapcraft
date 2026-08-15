package com.trolmastercard.sexmod.util;

import javax.swing.JFrame;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

/**
 * Developer debug overlay window (obfuscated-era leftover, largely dormant).
 */
public class DebugWindow extends JFrame {
   public boolean isVisible = false;

   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      if (!this.isVisible) {
         this.isVisible = true;
         DebugWindow2.showDebugWindow();
      }
   }

}
