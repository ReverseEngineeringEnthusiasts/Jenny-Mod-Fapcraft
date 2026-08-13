package com.trolmastercard.sexmod.util;


import javax.swing.JFrame;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class DebugWindow extends JFrame {
   public boolean isVisible = false;

   @SubscribeEvent
   public void a(ClientTickEvent var1) {
      if (!this.isVisible) {
         this.isVisible = true;
         DebugWindow2.showDebugWindow();
      }
   }

}
