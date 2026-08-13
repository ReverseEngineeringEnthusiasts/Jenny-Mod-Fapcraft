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
   static final int MAX_NAME_LENGTH = 15;
   static final int WIDTH_100 = 100;
   static final int HEIGHT_20 = 20;
   UUID koboldId;
   GuiTextField nameField;

   public TribeNameScreen(UUID var1) {
      this.koboldId = var1;
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public void initGui() {
      super.initGui();
      this.nameField = new GuiTextField(0, this.mc.fontRenderer, this.width / 2 - 50, this.height / 2 - 10, 100, 20);
      this.nameField.setFocused(true);
      this.buttonList.add(new GuiButton(0, this.width / 2 - 25, this.height / 2 + 20, 50, 20, "set"));
   }

   public void updateScreen() {
      this.nameField.updateCursorCounter();
      super.updateScreen();
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawHoveringText("Name Tribe", this.width / 2 - 39, this.height / 2 - 10);
      this.nameField.drawTextBox();
      super.drawScreen(var1, var2, var3);
   }

   protected void keyTyped(char var1, int var2) {
      this.nameField.textboxKeyTyped(var1, var2);
      String var3 = this.nameField.getText();
      if (var3.length() > 15) {
         this.nameField.setText(var3.substring(0, 15));
      }

      super.keyTyped(var1, var2);
   }

   protected void actionPerformed(GuiButton var1) {
      super.actionPerformed(var1);
      String var2 = this.nameField.getText().trim();
      if (var2.length() != 0) {
         PacketHandler.networkWrapper.sendToServer(new ClaimTribePacket(this.koboldId, Minecraft.getMinecraft().player.getPersistentID(), var2));
         Minecraft.getMinecraft().player.closeScreen();
      }
   }

}
