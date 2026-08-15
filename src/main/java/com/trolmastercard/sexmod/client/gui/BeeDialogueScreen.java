package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeeEntityBase;
import com.trolmastercard.sexmod.networking.BeeOpenChestPacket;
import com.trolmastercard.sexmod.networking.ChangeDataParameterPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetNewHomePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

/**
 * Dialogue GUI for interacting with a bee companion ({@link BeeEntityBase}):
 * "follow me / stop following", "go home", "set home here" and — when the bee
 * is horny (HORNY_FLAG data-manager entry) — opening the bee's chest inventory.
 * <p>
 * <b>Flow.</b> Opened by right-clicking the bee. All actions are sent to the
 * server as packets ({@code ChangeDataParameterPacket} toggling the
 * {@code master} data parameter, {@code SendCompanionHomePacket},
 * {@code SetNewHomePacket} at the bee's current position, or
 * {@link BeeOpenChestPacket} on the horny-icon click). The follow-mode buttons
 * flip a local flag and print localized feedback chat lines.
 * <p>
 * CLIENT-side only. Does not pause the game; the buttons slide in with
 * {@code animProgress} on draw.
 */
public class BeeDialogueScreen extends GuiScreen {
   BeeEntityBase beeEntity;
   EntityPlayer player;
   boolean isFollowMode;
   static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   double animProgress = 0.0;

   public BeeDialogueScreen(BeeEntityBase var1, EntityPlayer var2) {
      this.beeEntity = var1;
      this.player = var2;
      this.isFollowMode = !"".equals(var1.getDataManager().get(BaseGirlEntity.MASTER));
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   /**
    * Builds the three dialogue buttons (follow toggle, go home, set home) and
    * the horny-bee icon; button positions and sizes animate in from the left
    * based on {@code animProgress}. Button list is rebuilt every frame.
    */
   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      this.buttonList.clear();
      ScaledResolution var4 = new ScaledResolution(this.mc);
      int var5 = var4.getScaledWidth();
      this.animProgress = Math.min(1.0, this.animProgress + this.mc.getTickLength() / 5.0F);
      this.buttonList
         .add(
            new GuiButton(
               0,
               var5 / 2 - 119 + (int)(100.0 - 100.0 * this.animProgress),
               30,
               (int)(this.animProgress * 100.0),
               20,
               this.isFollowMode ? I18n.format("action.names.stopfollowme", new Object[0]) : I18n.format("action.names.followme", new Object[0])
            )
         );
      this.buttonList.add(new GuiButton(1, var5 / 2 + 19, 30, (int)(this.animProgress * 100.0), 20, I18n.format("action.names.gohome", new Object[0])));
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      this.drawTexturedModalRect(var5 / 2 - 7, 61 - (int)(15.0 - this.animProgress * 15.0), 32, 0, 15, 15);
      this.buttonList.add(new GuiButton(2, var5 / 2 - 10, 59 - (int)(15.0 - this.animProgress * 15.0), 20, 20, ""));
      this.drawTexturedModalRect(var5 / 2 - 20, 20, this.beeEntity.getDataManager().get(BeeEntityBase.HORNY_FLAG) ? 0 : 40, 130, 40, 40);
   }

   /**
    * Handles a click on the horny-bee chest icon (center of the screen, only
    * when {@code HORNY_FLAG} is set): sends {@link BeeOpenChestPacket} to the
    * server and closes this GUI, letting the server open the girl-inventory
    * chest container instead.
    */
   protected void mouseClicked(int var1, int var2, int var3) {
      ScaledResolution var4 = new ScaledResolution(this.mc);
      int var5 = var4.getScaledWidth();
      if ((Boolean)this.beeEntity.getDataManager().get(BeeEntityBase.HORNY_FLAG) && var1 >= var5 / 2 - 20 && var1 <= var5 / 2 + 20 && var2 >= 20 && var2 <= 60) {
         PacketHandler.networkWrapper.sendToServer(new BeeOpenChestPacket(this.beeEntity.getGirlId(), this.player.getPersistentID()));
         this.onGuiClosed();
      }

      super.mouseClicked(var1, var2, var3);
   }

   /**
    * Dispatches the dialogue buttons to their server packets and closes the
    * screen. Button 0 toggles follow mode (writes the player UUID to the bee's
    * {@code master} data parameter, or clears it); button 1 sends the bee home;
    * button 2 pins the bee's current position as its new home.
    */
   protected void actionPerformed(GuiButton var1) {
      super.actionPerformed(var1);
      if (var1.id == 0) {
         if (this.isFollowMode) {
            PacketHandler.networkWrapper.sendToServer(new ChangeDataParameterPacket(this.beeEntity.getGirlId(), "master", ""));
            this.player.sendMessage(new TextComponentString(I18n.format("bee.dialogue.sad", new Object[0])));
         } else {
            PacketHandler.networkWrapper.sendToServer(new ChangeDataParameterPacket(this.beeEntity.getGirlId(), "master", this.player.getPersistentID().toString()));
            this.player.sendMessage(new TextComponentString(I18n.format("bee.dialogue.exited", new Object[0])));
         }

         this.isFollowMode = !this.isFollowMode;
         this.player.closeScreen();
      }

      if (var1.id == 1) {
         PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.beeEntity.getGirlId()));
         this.player.closeScreen();
      }

      if (var1.id == 2) {
         PacketHandler.networkWrapper.sendToServer(new SetNewHomePacket(this.beeEntity.getGirlId(), new Vec3d(this.beeEntity.posX, this.beeEntity.posY, this.beeEntity.posZ)));
         this.player.closeScreen();
         this.player.sendMessage(new TextComponentString(I18n.format("bee.dialogue.home", new Object[0])));
      }
   }

}
