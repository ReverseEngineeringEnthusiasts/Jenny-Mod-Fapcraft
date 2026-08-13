package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.networking.ClaimTribePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;







import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class TribeNameScreen extends GuiScreen {
   static final int b = 15;
   static final int a = 100;
   static final int c = 20;
   UUID e;
   GuiTextField d;

   public TribeNameScreen(UUID var1) {
      this.e = var1;
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public void initGui() {
      super.initGui();
      this.d = new GuiTextField(0, this.mc.fontRenderer, this.width / 2 - 50, this.height / 2 - 10, 100, 20);
      this.d.setFocused(true);
      this.buttonList.add(new GuiButton(0, this.width / 2 - 25, this.height / 2 + 20, 50, 20, "set"));
   }

   public void updateScreen() {
      this.d.updateCursorCounter();
      super.updateScreen();
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawHoveringText("Name Tribe", this.width / 2 - 39, this.height / 2 - 10);
      this.d.drawTextBox();
      super.drawScreen(var1, var2, var3);
   }

   protected void keyTyped(char var1, int var2) {
      this.d.textboxKeyTyped(var1, var2);
      String var3 = this.d.getText();
      if (var3.length() > 15) {
         this.d.setText(var3.substring(0, 15));
      }

      super.keyTyped(var1, var2);
   }

   protected void actionPerformed(GuiButton var1) {
      super.actionPerformed(var1);
      String var2 = this.d.getText().trim();
      if (var2.length() != 0) {
         PacketHandler.b.sendToServer(new ClaimTribePacket(this.e, Minecraft.getMinecraft().player.getPersistentID(), var2));
         Minecraft.getMinecraft().player.closeScreen();
      }
   }

}
