package com.trolmastercard.sexmod.reskin;

import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> First-run prompt when no custom-model packs are installed. It
 * used to offer one 560 MB archive download; it now gives the three sane paths:
 * <ul>
 *   <li><b>Browse archive</b> — {@link ModelDownloadScreen}: search, tick packs,
 *       Download all, pause/resume;</li>
 *   <li><b>Open folder</b> — creates {@code sexmod/custom_models/<group>} and
 *       opens it, so a user can drop their own .zip/.rar packs;</li>
 *   <li><b>View on GitHub</b> — the archive page for a manual download.</li>
 * </ul>
 * The folder path is printed on screen and the prompt returns on every wand
 * open until at least one pack exists.
 */
@SideOnly(Side.CLIENT)
public class NoModelsScreen extends GuiScreen {
   private final String girlFilter;
   private final String folder;
   private String status = "";

   public NoModelsScreen(String girlFilter) {
      this.girlFilter = girlFilter;
      this.folder = ServerWhitelistManager.getGlobalModelOverride();
      this.mc = Minecraft.getMinecraft();
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton != 0) {
         return;
      }

      int bw = 120;
      int bh = 20;
      int gap = 8;
      String[] labels = {"Browse archive", "Open folder", "View on GitHub", "Back"};
      int total = labels.length * bw + (labels.length - 1) * gap;
      int bx = (this.width - total) / 2;
      int by = this.height / 2 + 24;
      for (int i = 0; i < labels.length; i++) {
         int x0 = bx + i * (bw + gap);
         if (mouseX >= x0 && mouseX <= x0 + bw && mouseY >= by && mouseY <= by + bh) {
            this.onButton(labels[i]);
            return;
         }
      }
   }

   private void onButton(String label) {
      try {
         this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F));
      } catch (Throwable ignored) {
         // ignored: a missing click sound must not break the screen
      }

      switch (label) {
         case "Browse archive":
            this.mc.displayGuiScreen(new ModelDownloadScreen(null));
            break;
         case "Open folder":
            this.openFolder();
            break;
         case "View on GitHub":
            this.openGithub();
            break;
         case "Back":
         default:
            this.mc.displayGuiScreen(null);
            break;
      }
   }

   private void openFolder() {
      File dir = new File(this.folder);
      try {
         if (dir.exists() || dir.mkdirs()) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
               Desktop.getDesktop().open(dir);
               return;
            }
            this.status = "Open it yourself: " + dir.getAbsolutePath();
         } else {
            this.status = "Could not create " + dir.getAbsolutePath();
         }
      } catch (Throwable t) {
         this.status = "Could not open the folder: " + t.getClass().getSimpleName();
      }
   }

   private void openGithub() {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
         try {
            Desktop.getDesktop().browse(new URI(PackDownloader.PAGE_URL));
         } catch (Exception e) {
            this.status = "Could not open the browser: " + e.getClass().getSimpleName();
         }
      } else {
         this.status = "Open manually: " + PackDownloader.PAGE_URL;
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRenderer, "No models found.", this.width / 2, this.height / 2 - 62, 0xFFFFFF);
      this.drawCenteredString(this.fontRenderer, "Pick packs from the archive, or drop .zip/.rar files into:", this.width / 2, this.height / 2 - 44, 0xAAAAAA);
      this.drawCenteredString(this.fontRenderer, this.folder, this.width / 2, this.height / 2 - 28, 0x88CCFF);
      this.drawCenteredString(this.fontRenderer, "Then reopen the girl wand (or use /models).", this.width / 2, this.height / 2 - 12, 0x888888);

      int bw = 120;
      int bh = 20;
      int gap = 8;
      String[] labels = {"Browse archive", "Open folder", "View on GitHub", "Back"};
      int total = labels.length * bw + (labels.length - 1) * gap;
      int bx = (this.width - total) / 2;
      int by = this.height / 2 + 24;
      for (int i = 0; i < labels.length; i++) {
         int x0 = bx + i * (bw + gap);
         boolean hover = mouseX >= x0 && mouseX <= x0 + bw && mouseY >= by && mouseY <= by + bh;
         drawRect(x0, by, x0 + bw, by + bh, hover ? 0xCC33AAFF : 0xAA222222);
         this.drawCenteredString(this.fontRenderer, labels[i], x0 + bw / 2, by + 6, 0xFFFFFF);
      }

      if (this.status != null && !this.status.isEmpty()) {
         this.drawCenteredString(this.fontRenderer, this.status, this.width / 2, this.height / 2 + 54, 0xFFFF55);
      }
   }
}
