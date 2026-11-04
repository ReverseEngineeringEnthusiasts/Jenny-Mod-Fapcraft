package com.trolmastercard.sexmod.reskin;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

/**
 * <b>Role.</b> Searchable archive browser with selective downloads. Replaces the
 * old "download the whole 560 MB main.zip" button:
 * <ul>
 *   <li>search filters by pack name, girl and path;</li>
 *   <li>tick packs and Download, or Download all for the full archive;</li>
 *   <li>pause / resume / cancel the batch;</li>
 *   <li>packs land in {@code sexmod/custom_models/<group>/...}, the folder is
 *       created before opening it, and the screen says where the files go;</li>
 *   <li>closing the screen does not stop a download; reopen the screen (or use
 *       {@code /models}) to see progress again.</li>
 * </ul>
 * Downloads run in {@link ModelDownloadManager} (one daemon thread, resumable
 * {@code .part} files); this screen only reads its immutable snapshot, so the
 * render thread never touches the network or the disk.
 */
@SideOnly(Side.CLIENT)
public class ModelDownloadScreen extends GuiScreen {
   private static final int ROW_H = 18;
   private static final int LIST_TOP = 54;

   private final GuiScreen parent;
   private final Set<String> selected = new LinkedHashSet<>();
   private final List<ArchiveIndex.Pack> visible = new ArrayList<>();
   private GuiTextField searchField;
   private GuiButton downloadButton;
   private GuiButton pauseButton;
   private int scroll;
   private String lastQuery = "";
   private List<ArchiveIndex.Pack> lastPacks;
   private boolean lastLoaded;
   private String status = "";
   private long statusUntil;

   public ModelDownloadScreen() {
      this(null);
   }

   public ModelDownloadScreen(GuiScreen parent) {
      this.parent = parent;
      this.mc = Minecraft.getMinecraft();
   }

   @Override
   public void initGui() {
      super.initGui();
      this.buttonList.clear();
      ArchiveIndex.loadAsync(false);
      int y = this.height - 58;
      this.downloadButton = new GuiButton(0, 95, y, 80, 20, "Download");
      this.buttonList.add(this.downloadButton);
      this.buttonList.add(new GuiButton(1, 179, y, 72, 20, "Download all"));
      this.pauseButton = new GuiButton(2, 255, y, 60, 20, "Pause");
      this.buttonList.add(this.pauseButton);
      this.buttonList.add(new GuiButton(3, 319, y, 55, 20, "Cancel"));
      this.buttonList.add(new GuiButton(4, 378, y, 70, 20, "Open folder"));
      this.buttonList.add(new GuiButton(5, 452, y, 55, 20, "Refresh"));
      this.buttonList.add(new GuiButton(6, 511, y, 45, 20, "Done"));
      if (this.searchField == null) {
         this.searchField = new GuiTextField(0, this.fontRenderer, 10, 30, 190, 16);
         this.searchField.setMaxStringLength(48);
         this.searchField.setCanLoseFocus(true);
      } else {
         this.searchField.x = 10;
         this.searchField.y = 30;
      }

      this.rebuild();
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   /** Reopens the browser from the wand, K key or /models. */
   public static void show(Minecraft mc, GuiScreen parent) {
      mc.displayGuiScreen(new ModelDownloadScreen(parent));
   }

   @Override
   public void handleMouseInput() {
      super.handleMouseInput();
      int dwheel = Mouse.getEventDWheel();
      if (dwheel != 0) {
         int rows = this.visibleRows();
         int max = Math.max(0, this.visible.size() - rows);
         this.scroll = Math.max(0, Math.min(max, this.scroll - Integer.signum(dwheel)));
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.searchField != null) {
         this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
      }

      int row = this.rowAt(mouseX, mouseY);
      if (row >= 0) {
         ArchiveIndex.Pack pack = this.visible.get(row);
         if (!this.selected.remove(pack.path)) {
            this.selected.add(pack.path);
         }
      }
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      if (this.searchField != null && this.searchField.textboxKeyTyped(typedChar, keyCode)) {
         if (!this.searchField.getText().equals(this.lastQuery)) {
            this.scroll = 0;
            this.rebuild();
         }
         return;
      }

      super.keyTyped(typedChar, keyCode);
   }

   @Override
   protected void actionPerformed(GuiButton button) {
      if (button == null) {
         return;
      }

      switch (button.id) {
         case 0:
            this.download(this.selectedPacks());
            break;
         case 1:
            this.download(allPacks());
            break;
         case 2:
            if (ModelDownloadManager.INSTANCE.snapshot().status == ModelDownloadManager.Status.PAUSED) {
               ModelDownloadManager.INSTANCE.resume();
            } else {
               ModelDownloadManager.INSTANCE.pause();
            }
            break;
         case 3:
            ModelDownloadManager.INSTANCE.cancel();
            break;
         case 4:
            this.openFolder();
            break;
         case 5:
            ArchiveIndex.loadAsync(true);
            this.status = "Refreshing the archive list...";
            this.statusUntil = System.currentTimeMillis() + 4000L;
            break;
         case 6:
            this.mc.displayGuiScreen(this.parent);
            break;
      }
   }

   private void download(List<ArchiveIndex.Pack> packs) {
      if (packs.isEmpty()) {
         this.status = this.selected.isEmpty()
            ? "Tick at least one pack first, or use Download all."
            : "The selected packs are not in the current list; refresh.";
         this.statusUntil = System.currentTimeMillis() + 5000L;
         return;
      }

      File root = new File(ServerWhitelistManager.getGlobalModelOverride());
      boolean started = ModelDownloadManager.INSTANCE.start(packs, root, new Runnable() {
         @Override
         public void run() {
            ReskinManager.INSTANCE.rescan();
         }
      });
      if (!started) {
         this.status = "A download is already running. Pause or cancel it first.";
         this.statusUntil = System.currentTimeMillis() + 5000L;
      }
   }

   private List<ArchiveIndex.Pack> selectedPacks() {
      List<ArchiveIndex.Pack> packs = new ArrayList<>();
      for (ArchiveIndex.Pack pack : ArchiveIndex.state().packs) {
         if (this.selected.contains(pack.path)) {
            packs.add(pack);
         }
      }
      return packs;
   }

   private static List<ArchiveIndex.Pack> allPacks() {
      return new ArrayList<>(ArchiveIndex.state().packs);
   }

   private void openFolder() {
      File folder = new File(ServerWhitelistManager.getGlobalModelOverride());
      try {
         if (folder.exists() || folder.mkdirs()) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
               Desktop.getDesktop().open(folder);
               return;
            }
            this.status = "Open it yourself: " + folder.getAbsolutePath();
         } else {
            this.status = "Could not create " + folder.getAbsolutePath();
         }
      } catch (Throwable t) {
         this.status = "Could not open the folder: " + t.getMessage();
      }
      this.statusUntil = System.currentTimeMillis() + 8000L;
   }

   private void rebuild() {
      ArchiveIndex.State state = ArchiveIndex.state();
      this.lastLoaded = state.loaded;
      this.lastPacks = state.packs;
      this.lastQuery = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase(Locale.ROOT);
      this.visible.clear();
      for (ArchiveIndex.Pack pack : state.packs) {
         if (this.lastQuery.isEmpty()
            || pack.name.toLowerCase(Locale.ROOT).contains(this.lastQuery)
            || pack.group.toLowerCase(Locale.ROOT).contains(this.lastQuery)
            || pack.path.toLowerCase(Locale.ROOT).contains(this.lastQuery)) {
            this.visible.add(pack);
         }
      }
      this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, this.visible.size() - this.visibleRows())));
   }

   private int visibleRows() {
      return Math.max(1, (this.height - 64 - LIST_TOP) / ROW_H);
   }

   private int rowAt(int mouseX, int mouseY) {
      if (mouseX < 6 || mouseX > this.width - 6 || mouseY < LIST_TOP || mouseY >= LIST_TOP + this.visibleRows() * ROW_H) {
         return -1;
      }

      int index = this.scroll + (mouseY - LIST_TOP) / ROW_H;
      return index >= 0 && index < this.visible.size() ? index : -1;
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      ArchiveIndex.State state = ArchiveIndex.state();
      if (state.packs != this.lastPacks || state.loaded != this.lastLoaded) {
         this.rebuild();
      }

      ModelDownloadManager.Snapshot snapshot = ModelDownloadManager.INSTANCE.snapshot();
      this.drawCenteredString(this.fontRenderer, "Custom model archive", this.width / 2, 8, 0xFFFFFF);
      this.drawCenteredString(this.fontRenderer, "pick packs, or Download all. Files go to "
         + ServerWhitelistManager.getGlobalModelOverride(), this.width / 2, 20, 0x999999);

      this.searchField.drawTextBox();
      if (this.searchField.getText().isEmpty() && !this.searchField.isFocused()) {
         this.drawString(this.fontRenderer, "search name or girl...", this.searchField.x + 4, this.searchField.y + 4, 0x777777);
      }

      String countText = state.loading && !state.loaded
         ? "Loading archive..."
         : this.visible.size() + " / " + state.packs.size() + " packs";
      this.drawString(this.fontRenderer, countText, this.width - 10 - this.fontRenderer.getStringWidth(countText), 34, 0xAAAAAA);

      if (!state.loaded) {
         String line = state.error == null ? "Loading the archive list from GitHub..." : state.error;
         this.drawCenteredString(this.fontRenderer, line, this.width / 2, this.height / 2 - 10, state.error == null ? 0xFFFFFF : 0xFF6666);
         this.drawCenteredString(this.fontRenderer, "You can also drop .zip/.rar packs in the folder and press Refresh.",
            this.width / 2, this.height / 2 + 6, 0x888888);
      } else if (this.visible.isEmpty()) {
         this.drawCenteredString(this.fontRenderer, "No packs match the search.", this.width / 2, this.height / 2 - 10, 0xFFFFFF);
      } else {
         this.drawList(mouseX, mouseY);
      }

      this.updateButtons(snapshot);
      this.drawStatus(snapshot);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawList(int mouseX, int mouseY) {
      int rows = this.visibleRows();
      int end = Math.min(this.visible.size(), this.scroll + rows);
      for (int i = this.scroll; i < end; i++) {
         ArchiveIndex.Pack pack = this.visible.get(i);
         int y = LIST_TOP + (i - this.scroll) * ROW_H;
         boolean hover = mouseX >= 6 && mouseX <= this.width - 6 && mouseY >= y && mouseY < y + ROW_H;
         boolean ticked = this.selected.contains(pack.path);
         if (ticked || hover) {
            drawRect(6, y, this.width - 6, y + ROW_H - 1, ticked ? 0x55336699 : 0x33FFFFFF);
         }

         this.drawString(this.fontRenderer, ticked ? "[x]" : "[ ]", 10, y + 5, ticked ? 0x55FF55 : 0xAAAAAA);
         String name = this.trim(pack.name, 190);
         this.drawString(this.fontRenderer, name, 36, y + 5, 0xFFFFFF);
         this.drawString(this.fontRenderer, this.trim(pack.group, 80), 235, y + 5, 0x8888CC);
         String size = ModelDownloadManager.formatBytes(pack.size);
         this.drawString(this.fontRenderer, size, this.width - 10 - this.fontRenderer.getStringWidth(size), y + 5, 0xAAAAAA);
      }

      if (this.visible.size() > rows) {
         int barHeight = Math.max(10, rows * ROW_H * rows / this.visible.size());
         int barY = LIST_TOP + (int)((rows * ROW_H - barHeight) * (this.scroll / (double)Math.max(1, this.visible.size() - rows)));
         drawRect(this.width - 5, barY, this.width - 2, barY + barHeight, 0x88FFFFFF);
      }
   }

   private void updateButtons(ModelDownloadManager.Snapshot snapshot) {
      boolean busy = snapshot.status == ModelDownloadManager.Status.RUNNING
         || snapshot.status == ModelDownloadManager.Status.PAUSED;
      if (this.downloadButton != null) {
         this.downloadButton.enabled = !busy;
      }
      if (this.pauseButton != null) {
         this.pauseButton.displayString = snapshot.status == ModelDownloadManager.Status.PAUSED ? "Resume" : "Pause";
         this.pauseButton.enabled = busy;
      }
   }

   private void drawStatus(ModelDownloadManager.Snapshot snapshot) {
      String line;
      if (this.status != null && !this.status.isEmpty() && System.currentTimeMillis() < this.statusUntil) {
         line = this.status;
      } else {
         switch (snapshot.status) {
            case RUNNING:
               line = "Downloading " + (snapshot.finished + 1) + "/" + snapshot.total + ": " + snapshot.current
                  + "  " + ModelDownloadManager.formatBytes(snapshot.currentDone) + " / "
                  + ModelDownloadManager.formatBytes(snapshot.currentTotal)
                  + (snapshot.speedBps > 0 ? "  (" + ModelDownloadManager.formatBytes(snapshot.speedBps) + "/s)" : "");
               break;
            case PAUSED:
               line = "Paused. Press Resume to continue; the .part file is kept.";
               break;
            case DONE:
               line = "Done. " + snapshot.finished + " pack(s) installed. Open the girl wand and press Reskins.";
               break;
            case FAILED:
               line = "Finished with errors (" + snapshot.failed + " failed). " + (snapshot.error == null ? "" : snapshot.error);
               break;
            case CANCELLED:
               line = "Cancelled. Partial files were removed.";
               break;
            default:
               line = snapshot.total == 0 ? "Ready." : snapshot.finished + " / " + snapshot.total + " pack(s) done.";
               break;
         }
      }

      this.drawString(this.fontRenderer, this.trim(line, this.width - 20), 10, this.height - 36, 0xDDDDDD);
      long total = snapshot.totalBytes;
      long done = snapshot.doneBytes;
      int barY = this.height - 24;
      drawRect(10, barY, this.width - 10, barY + 7, 0x66000000);
      if (total > 0L) {
         int filled = (int)((this.width - 20) * Math.min(1.0, done / (double)total));
         drawRect(10, barY, 10 + filled, barY + 7, 0xCC55AA55);
      }
      String overall = ModelDownloadManager.formatBytes(done) + " / " + ModelDownloadManager.formatBytes(total);
      this.drawString(this.fontRenderer, overall, this.width - 10 - this.fontRenderer.getStringWidth(overall), barY - 10, 0x888888);
   }

   private String trim(String text, int width) {
      if (text == null) {
         return "";
      }
      return this.fontRenderer.getStringWidth(text) <= width ? text : this.fontRenderer.trimStringToWidth(text, width - 6) + "...";
   }
}
