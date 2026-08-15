package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.networking.ClaimTribePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/**
 * Small name-input screen for claiming a kobold tribe: a single text field
 * (max 15 chars) and a "set" button that sends {@link ClaimTribePacket} with
 * the trimmed name to the server. Does not pause the game.
 * <p>
 * CLIENT-side only. Nothing is sent when the field is empty.
 */
public class TribeNameScreen extends GuiScreen {
   UUID koboldId;
   GuiTextField nameField;

   public TribeNameScreen(UUID koboldId) {
      this.koboldId = koboldId;
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

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawHoveringText("Name Tribe", this.width / 2 - 39, this.height / 2 - 10);
      this.nameField.drawTextBox();
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   /**
    * Clamps the tribe name to 15 characters while
    * typing.
    */
   protected void keyTyped(char typedChar, int keyCode) {
      this.nameField.textboxKeyTyped(typedChar, keyCode);
      String text = this.nameField.getText();
      if (text.length() > 15) {
         this.nameField.setText(text.substring(0, 15));
      }

      super.keyTyped(typedChar, keyCode);
   }

   /**
    * Sends {@link ClaimTribePacket} with the entered name (non-empty after
    * trimming) and closes the screen.
    */
   protected void actionPerformed(GuiButton button) {
      super.actionPerformed(button);
      String name = this.nameField.getText().trim();
      if (name.length() != 0) {
         PacketHandler.networkWrapper.sendToServer(new ClaimTribePacket(this.koboldId, Minecraft.getMinecraft().player.getPersistentID(), name));
         Minecraft.getMinecraft().player.closeScreen();
      }
   }

}
