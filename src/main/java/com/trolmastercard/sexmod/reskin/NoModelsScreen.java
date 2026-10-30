package com.trolmastercard.sexmod.reskin;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> First-run / empty-folder prompt: shown when the user opens the
 * girl wand but no custom-model packs are installed. Asks
 * "No models found. Would you like to download some from github?" with
 * <ul>
 *   <li><b>Download</b> — fetches the GitHub archive ({@link PackDownloader})
 *       on a background thread and extracts it into the instance's custom-models
 *       folder, then opens the pack browser prefilled with the newly installed
 *       packs.</li>
 *   <li><b>View on Github</b> — opens the archive page in the system browser.</li>
 *   <li><b>No</b> — closes. The prompt reappears on the next wand open until at
 *       least one pack exists in the directory (no persistent flag: "unless they
 *       have a model in the directory" is exactly the empty-scan check).</li>
 * </ul>
 */
@SideOnly(Side.CLIENT)
public class NoModelsScreen extends GuiScreen {
   private final String girlFilter;
   private String status = "";
   private boolean working = false;
   private boolean clicked = false;

   public NoModelsScreen(String girlFilter) {
      this.girlFilter = girlFilter;
      this.mc = Minecraft.getMinecraft();
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton != 0 || working || clicked) return;

      // buttons: Download | View on Github | No
      int bw = 110;
      int bh = 20;
      int gap = 10;
      String[] labels = {"Download", "View on Github", "No"};
      int total = labels.length * bw + (labels.length - 1) * gap;
      int bx = (this.width - total) / 2;
      int by = this.height / 2 + 10;
      for (int i = 0; i < labels.length; i++) {
         int x0 = bx + i * (bw + gap);
         if (mouseX >= x0 && mouseX <= x0 + bw && mouseY >= by && mouseY <= by + bh) {
            onButton(labels[i]);
            return;
         }
      }
   }

   private void onButton(String label) {
      clicked = true;
      try {
         this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F));
      } catch (Throwable ignored) {
         // ignored: a missing click sound must not break the screen
      }
      switch (label) {
         case "Download": {
            startDownload();
            break;
         }
         case "View on Github": {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
               try {
                  Desktop.getDesktop().browse(new URI(PackDownloader.PAGE_URL));
               } catch (Exception e) {
                  status = "Could not open browser: " + e.getClass().getSimpleName();
               }
            } else {
               status = "Open manually: " + PackDownloader.PAGE_URL;
            }
            clicked = false; // allow other buttons again
            break;
         }
         case "No":
         default: {
            this.mc.displayGuiScreen(null);
            break;
         }
      }
   }

   /** Downloads + extracts on a background thread, then opens the browser. */
   private void startDownload() {
      working = true;
      status = "Downloading models from GitHub...";
      ReskinManager.INSTANCE.submit(() -> {
         String result;
         File zip = null;
         try {
            File dest = new File(com.trolmastercard.sexmod.util.ServerWhitelistManager.getGlobalModelOverride());
            dest.mkdirs();
            zip = PackDownloader.downloadToTemp(PackDownloader.ARCHIVE_URL);
            result = PackDownloader.extractTo(zip, dest);
         } catch (Throwable t) {
            result = "Download failed: " + t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
         } finally {
            if (zip != null) zip.delete();
         }
         final String msg = result;
         Minecraft.getMinecraft().addScheduledTask(() -> {
            working = false;
            try {
               ReskinManager.INSTANCE.rescan();
            } catch (Throwable t) {
               // still open the browser below
            }
            if (msg.toLowerCase().contains("failed") || msg.toLowerCase().contains("missing") || msg.toLowerCase().contains("extraction failed")) {
               status = msg;
               clicked = false;
            } else {
               // success: open the browser (it will show the newly installed packs)
               this.mc.displayGuiScreen(new ReskinBrowserScreen(girlFilter, msg));
            }
         });
      });
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRenderer, "No models found.", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
      this.drawCenteredString(this.fontRenderer, "Would you like to download some from github?", this.width / 2, this.height / 2 - 24, 0xFFFFFF);

      int bw = 110;
      int bh = 20;
      int gap = 10;
      String[] labels = {"Download", "View on Github", "No"};
      int total = labels.length * bw + (labels.length - 1) * gap;
      int bx = (this.width - total) / 2;
      int by = this.height / 2 + 10;
      for (int i = 0; i < labels.length; i++) {
         int x0 = bx + i * (bw + gap);
         boolean hover = mouseX >= x0 && mouseX <= x0 + bw && mouseY >= by && mouseY <= by + bh;
         drawRect(x0, by, x0 + bw, by + bh, (working && i == 0) ? 0x66333333 : (hover ? 0xCC33AAFF : 0xAA222222));
         this.drawCenteredString(this.fontRenderer, labels[i], x0 + bw / 2, by + 6, (working && i == 0) ? 0x888888 : 0xFFFFFF);
      }

      if (status != null && !status.isEmpty()) {
         this.drawCenteredString(this.fontRenderer, status, this.width / 2, this.height / 2 + 48, 0xFFFF55);
      }
   }
}