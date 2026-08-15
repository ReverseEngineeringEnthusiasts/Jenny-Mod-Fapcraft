package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * CLIENT: opens GUIs for girls/structures when the server requests an
 * {@code openGui} (complements GuiHandler).
 */
public class GuiOpenHandler {
   @SubscribeEvent
   public void onGuiOpen(GuiOpenEvent event) {
      if (event.getGui() instanceof GuiMainMenu || event.getGui() instanceof GuiMultiplayer) {
         AbstractPlayerGirlEntity.playerGirlList.clear();
         AbstractPlayerGirlEntity.al.clear();
      }
   }

}
