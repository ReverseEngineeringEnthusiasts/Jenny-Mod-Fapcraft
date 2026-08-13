package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiOpenHandler {
   @SubscribeEvent
   public void a(GuiOpenEvent var1) {
      if (var1.getGui() instanceof GuiMainMenu || var1.getGui() instanceof GuiMultiplayer) {
         AbstractPlayerGirlEntity.playerGirlList.clear();
         AbstractPlayerGirlEntity.al.clear();
      }
   }

}
