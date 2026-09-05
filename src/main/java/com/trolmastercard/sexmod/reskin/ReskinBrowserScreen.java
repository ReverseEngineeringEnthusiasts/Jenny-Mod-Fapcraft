package com.trolmastercard.sexmod.reskin;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

/**
 * <b>Role.</b> Two-column pack browser modelled EXACTLY on vanilla's
 * {@code GuiScreenResourcePacks}:
 * <ul>
 *   <li>Left = AVAILABLE packs (filtered by girl chips + search).</li>
 *   <li>Right = APPLIED stack — index 0 at top = highest priority (overrides), lower = fallback.</li>
 *   <li>32x32 thumbnail in BOTH columns.</li>
 *   <li>Click a LEFT thumbnail -> moves pack to TOP of the applied stack.</li>
 *   <li>Click a RIGHT thumbnail top-left corner -> removes from stack (back to available).</li>
 *   <li>Click a RIGHT thumbnail top-right half -> move UP; bottom-right half -> move DOWN.</li>
 *   <li>Applied packs are excluded from the left list exactly like vanilla excludes selected
 *       resourcepacks from the available list.</li>
 * </ul>
 */
@SideOnly(Side.CLIENT)
public class ReskinBrowserScreen extends GuiScreen {
   private static final int ROW_H = 36;
   private static final int ICON = 32;
   private static final int COL_W = 200;
   private static final int CHIP_W = 60;
   private static final int CHIP_H = 15;
   private static final int CHIP_GAP = 3;
   private static final int LIST_BOTTOM_MARGIN = 44;

   private final ReskinManager manager = ReskinManager.INSTANCE;
   private List<ReskinEntry> entries;
   private int scrollAvailable;
   private int scrollStack;
   private int selectedAvailable = -1;
   private int selectedStack = -1;
   private String status = "";
   private String filterGirl;
   private GuiTextField searchField;
   private int chipRows = 1;
   private boolean scanDone;
   private final Map<String, ResourceLocation> thumbTextures = new HashMap<>();
   private final Map<String, BufferedImage> pendingThumbs = new HashMap<>();
   private boolean thumbsDirty;

   // double-click detection for apply (left) / remove (right)
   private long dblLastClickMs = 0;
   private int dblLastRow = -1;

   public ReskinBrowserScreen() {
      this(null);
   }

   public ReskinBrowserScreen(String startGirl) {
      this(startGirl, null);
   }

   /** Opens the browser with a pre-set status line (e.g. after a download). */
   public ReskinBrowserScreen(String startGirl, String initialStatus) {
      this.filterGirl = startGirl;
      if (initialStatus != null && !initialStatus.isEmpty()) this.status = initialStatus;
      this.mc = Minecraft.getMinecraft();
      // Original behavior: decode thumbnails eagerly (background thread) into
      // pendingThumbs; GL upload happens on the render thread in drawThumbsIfReady.
      // This is NOT cached across opens (gl textures must upload on render thread),
      // so each GUI open re-decodes but shows real thumbnails, not grey.
      manager.submit(() -> {
         try {
            manager.scan();
            for (ReskinEntry e : manager.scan()) {
               if (e.previewName != null) {
                  BufferedImage img = manager.preview(e);
                  if (img != null) {
                     synchronized (pendingThumbs) {
                        pendingThumbs.put(e.source.getAbsolutePath(), img);
                     }
                     thumbsDirty = true;
                  }
               }
            }
         } catch (Throwable t) {
            t.printStackTrace();
         } finally {
            scanDone = true;
         }
      });
   }

   /**
    * Routing helper used by the wand and K key: if there are no packs installed,
    * show the download prompt instead of an empty browser. Otherwise open the
    * browser (optionally pre-filtered to a girl).
    */
   public static void show(Minecraft mc, String girlFilter) {
      try {
         List<ReskinEntry> packs = ReskinManager.INSTANCE.scan();
         boolean empty = packs == null || packs.isEmpty();
         if (empty) {
            mc.displayGuiScreen(new NoModelsScreen(girlFilter));
         } else {
            mc.displayGuiScreen(new ReskinBrowserScreen(girlFilter));
         }
      } catch (Throwable t) {
         mc.displayGuiScreen(new ReskinBrowserScreen(girlFilter));
      }
   }

   @Override
   public void initGui() {
      super.initGui();
      this.buttonList.clear();
      if (this.searchField == null) {
         this.searchField = new GuiTextField(0, this.fontRenderer, this.width - 168, 6, 160, 16);
         this.searchField.setMaxStringLength(40);
         this.searchField.setCanLoseFocus(true);
      } else {
         this.searchField.x = this.width - 168;
         this.searchField.y = 6;
      }
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   /* ------------------------------------------------------------------ */
   /* Layout & data                                                     */
   /* ------------------------------------------------------------------ */

   private int listTop() {
      return 26 + chipRows * (CHIP_H + 2);
   }

   private int listBottom() {
      return this.height - LIST_BOTTOM_MARGIN;
   }

   /** Left (available) column X — centered around width/2 like vanilla's pack GUI. */
   private int colLeftX() {
      return Math.max(4, this.width / 2 - 4 - COL_W);
   }

   /** Right (applied) column X. */
   private int colRightX() {
      return this.width / 2 + 4;
   }

   private int visibleRows() {
      return Math.max(1, (listBottom() - listTop()) / ROW_H);
   }

   /** Available = all packs minus any currently applied (vanilla excludes selected). */
   private List<ReskinEntry> displayList() {
      List<ReskinEntry> all = manager.scan();
      if (all == null) return null;
      String q = searchField == null ? "" : searchField.getText().trim().toLowerCase(java.util.Locale.ROOT);
      List<ReskinEntry> applied = manager.appliedCopy();
      List<ReskinEntry> out = new ArrayList<>();
      for (ReskinEntry e : all) {
         if (applied.contains(e)) continue; // hide applied from available (vanilla behavior)
         if (filterGirl != null && !e.girls.contains(filterGirl)) continue;
         if (!q.isEmpty()) {
            if (!e.displayName.toLowerCase(java.util.Locale.ROOT).contains(q)
               && !e.girlSummary().toLowerCase(java.util.Locale.ROOT).contains(q)) continue;
         }
         out.add(e);
      }
      return out;
   }

   /** Applied stack ordered top-first (index 0 = highest priority / most recent). */
   private List<ReskinEntry> appliedStack() {
      List<ReskinEntry> out = manager.appliedCopy();
      java.util.Collections.reverse(out);
      return out;
   }

   private void setFilter(String girl) {
      filterGirl = girl;
      scrollAvailable = 0;
      status = girl == null ? "Showing ALL character packs" : "Showing " + girl.toUpperCase() + " packs";
   }

   /* ------------------------------------------------------------------ */
   /* Input                                                             */
   /* ------------------------------------------------------------------ */

   @Override
   public void handleMouseInput() {
      super.handleMouseInput();
      int dw = Mouse.getEventDWheel();
      if (dw == 0 || entries == null) return;
      int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
      if (mx < colRightX()) {
         scrollAvailable -= Integer.signum(dw);
         scrollAvailable = Math.max(0, Math.min(scrollAvailable, Math.max(0, entries.size() - visibleRows())));
      } else {
         scrollStack -= Integer.signum(dw);
         scrollStack = Math.max(0, Math.min(scrollStack, Math.max(0, appliedStack().size() - visibleRows())));
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton != 0) return;
      if (searchField != null) searchField.mouseClicked(mouseX, mouseY, mouseButton);

      // chips
      int cx = 8;
      int cy = 26;
      chipRows = 1;
      List<String> chips = new ArrayList<>();
      chips.add(null);
      for (String g : ReskinScanner.GIRLS) chips.add(g);
      for (String g : chips) {
         if (cx + CHIP_W > this.width - 4) { cx = 8; cy += CHIP_H + 2; chipRows++; }
         if (mouseX >= cx && mouseX <= cx + CHIP_W && mouseY >= cy && mouseY <= cy + CHIP_H) {
            setFilter(g);
            return;
         }
         cx += CHIP_W + CHIP_GAP;
      }

      if (entries == null) return;
      int top = listTop();
      int rows = visibleRows();

      // LEFT (available): single click = select; double-click = apply (moves to right)
      for (int i = 0; i < rows; i++) {
         int idx = scrollAvailable + i;
         if (idx >= entries.size()) break;
         int y = top + i * ROW_H;
         if (mouseX >= colLeftX() && mouseX <= colLeftX() + COL_W && mouseY >= y && mouseY < y + ROW_H) {
            selectedAvailable = idx;
            long now = System.currentTimeMillis();
            boolean dbl = (now - dblLastClickMs < 350 && dblLastRow == idx);
            dblLastClickMs = now;
            dblLastRow = idx;
            if (dbl) {
               applyRow(entries.get(idx));    // double-click = apply
            }
            return;
         }
      }

      // RIGHT (applied stack): single click = select; double-click = remove
      List<ReskinEntry> st = appliedStack();
      for (int i = 0; i < rows; i++) {
         int idx = scrollStack + i;
         if (idx >= st.size()) break;
         int y = top + i * ROW_H;
         if (mouseX >= colRightX() && mouseX <= colRightX() + COL_W && mouseY >= y && mouseY < y + ROW_H) {
            selectedStack = idx;
            long now = System.currentTimeMillis();
            boolean dbl = (now < dblLastClickMs ? false : now - dblLastClickMs < 350) && dblLastRow == idx + 10000; // separate right-list double-click tracking
            // NOTE: shared tracker is fine since a double-click on same row/col is impossible
            dblLastClickMs = now;
            dblLastRow = idx + 10000; // offset to distinguish from left list
            if (dbl) {
               revertRow(st.get(idx));         // double-click = remove from stack
               selectedStack = -1;
            }
            return;
         }
      }

      // external Move up / Move down buttons (beside right column, top area)
      int bw2 = 74;
      int bh2 = 18;
      int ux = colRightX() + COL_W + 8;
      int uy = listTop();
      if (mouseX >= ux && mouseX <= ux + bw2 && mouseY >= uy && mouseY <= uy + bh2) {
         moveSelected(-1);   // Move up
         return;
      }
      if (mouseX >= ux && mouseX <= ux + bw2 && mouseY >= uy + bh2 + 4 && mouseY <= uy + bh2 + 4 + bh2) {
         moveSelected(+1);   // Move down
         return;
      }

      // bottom buttons
      int bw = 64;
      int bh = 20;
      int gap = 5;
      String[] labels = {"Apply", "Remove", "Revert All", "Legacy", "Body", "Done"};
      int total = labels.length * bw + (labels.length - 1) * gap;
      int bx = (this.width - total) / 2;
      int by = this.height - 32;
      for (int li = 0; li < labels.length; li++) {
         int x0 = bx + li * (bw + gap);
         if (mouseX >= x0 && mouseX <= x0 + bw && mouseY >= by && mouseY <= by + bh) {
            onButton(labels[li]);
            return;
         }
      }
   }

   /** Apply the pack (moves to TOP of stack, loads immediately). */
   private void applyRow(ReskinEntry e) {
      status = manager.apply(e);
      scrollStack = 0;
   }

   /** Applies the selected stack row (UP/DOWN). */
   private void moveSelected(int delta) {
      List<ReskinEntry> st = appliedStack();
      if (selectedStack < 0 || selectedStack >= st.size()) {
         status = "Select a pack on the RIGHT first.";
         return;
      }
      moveRow(st.get(selectedStack), delta);
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) {
      if (searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
         scrollAvailable = 0;
         return;
      }
      super.keyTyped(typedChar, keyCode);
   }

   private void revertRow(ReskinEntry e) {
      status = manager.revert(e);
   }

   /** Move an applied pack up/down within the stack (re-order = change priority). */
   private void moveRow(ReskinEntry e, int delta) {
      status = manager.move(e, delta);
   }

   private void onButton(String label) {
      try {
         this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F));
      } catch (Throwable ignored) { }
      switch (label) {
         case "Apply": {
            // apply the selected (highlighted) available pack; if none selected, the first visible
            if (entries != null && !entries.isEmpty()) {
               int idx = selectedAvailable >= 0 ? selectedAvailable : Math.min(scrollAvailable, entries.size() - 1);
               if (idx < entries.size()) applyRow(entries.get(idx));
               else status = "No pack selected.";
            } else {
               status = "No packs available.";
            }
            break;
         }
         case "Remove": {
            List<ReskinEntry> st = appliedStack();
            if (st != null && !st.isEmpty()) revertRow(st.get(0));
            else status = "Nothing applied.";
            break;
         }
         case "Revert All": {
            status = manager.revertAll();
            break;
         }
         case "Legacy": {
            openLegacy();
            break;
         }
         case "Body": {
            openBodySliders();
            break;
         }
         case "Done": {
            this.mc.displayGuiScreen(null);
            break;
         }
         default:
            break;
      }
   }

   private void openLegacy() {
      try {
         com.trolmastercard.sexmod.entity.NpcType npcType = filterGirl == null ? null
            : com.trolmastercard.sexmod.entity.NpcType.getNpcTypeByName(filterGirl.toUpperCase(java.util.Locale.ROOT));
         if (npcType == null || npcType.npcClass == null) {
            status = "Select a girl chip first (e.g. JENNY) so the old wardrobe has a subject.";
            return;
         }
         // Prefer a REAL girl of this type from the world (the old wardrobe was
         // designed for live girls); fall back to a fresh preview instance.
         com.trolmastercard.sexmod.entity.BaseGirlEntity girl = null;
         try {
            List<com.trolmastercard.sexmod.entity.BaseGirlEntity> girls =
               com.trolmastercard.sexmod.entity.BaseGirlEntity.getGirlEntityList();
            if (girls != null) {
               for (com.trolmastercard.sexmod.entity.BaseGirlEntity g : girls) {
                  if (g != null && com.trolmastercard.sexmod.entity.NpcType.getNpcType(g) == npcType) {
                     girl = g;
                     break;
                  }
               }
            }
         } catch (Throwable t) { }
         if (girl == null) {
            girl = (com.trolmastercard.sexmod.entity.BaseGirlEntity) npcType.npcClass.getConstructor(net.minecraft.world.World.class).newInstance(this.mc.world);
            girl.setLocallyRegistered(true);
         }
         // Use the original proven entry point (schedules + guards whitelist).
         com.trolmastercard.sexmod.client.gui.ClothingScreen.openClothingScreen(girl);
         status = "Opened legacy wardrobe for " + npcType.name() + ".";
      } catch (Throwable t) {
         status = "Could not open legacy wardrobe: " + t.getClass().getSimpleName() + ": " + t.getMessage();
      }
   }

   private void openBodySliders() {
      try {
         com.trolmastercard.sexmod.entity.NpcType npcType = filterGirl == null ? null
            : com.trolmastercard.sexmod.entity.NpcType.getNpcTypeByName(filterGirl.toUpperCase(java.util.Locale.ROOT));
         if (npcType == null || npcType.npcClass == null) {
            status = "Select a girl chip first to see her body sliders.";
            return;
         }
         com.trolmastercard.sexmod.entity.BaseGirlEntity girl =
            (com.trolmastercard.sexmod.entity.BaseGirlEntity) npcType.npcClass.getConstructor(net.minecraft.world.World.class).newInstance(this.mc.world);
         girl.setLocallyRegistered(true);
         this.mc.displayGuiScreen(new BodySlidersScreen(girl));
      } catch (Throwable t) {
         status = "Body sliders unavailable: " + t.getClass().getSimpleName();
      }
   }

   /* ------------------------------------------------------------------ */
   /* Render                                                            */
   /* ------------------------------------------------------------------ */

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      List<ReskinEntry> list = displayList();
      this.entries = list;

      this.drawString(this.fontRenderer,
         "Available: " + (list == null ? 0 : list.size()) + "   |   Applied stack (top overrides)",
         8, 7, 0xFFFFFF);
      if (searchField != null) {
         searchField.drawTextBox();
         if (searchField.getText().isEmpty() && !searchField.isFocused()) {
            this.drawString(this.fontRenderer, "search...", searchField.x + 4, searchField.y + 4, 0x888888);
         }
      }

      drawChips(mouseX, mouseY);

      if (list == null || list.isEmpty()) {
         this.drawCenteredString(this.fontRenderer, scanDone ? "No packs available." : "Scanning...",
            this.width / 2, this.height / 2 - 10, 0xFFFFFF);
      } else {
         drawThumbsIfReady();
         int top = listTop();
         int rows = visibleRows();

         // column backgrounds
         drawRect(colLeftX() - 2, listTop() - 2, colLeftX() + COL_W + 2, listBottom() + 2, 0x22000000);
         drawRect(colRightX() - 2, listTop() - 2, colRightX() + COL_W + 2, listBottom() + 2, 0x22000000);

         // LEFT: available
         int count = Math.min(rows, list.size() - scrollAvailable);
         for (int i = 0; i < count; i++) {
            int idx = scrollAvailable + i;
            if (idx >= list.size()) break;
            drawRow(list.get(idx), colLeftX(), top + i * ROW_H, i, mouseX, mouseY, false, selectedAvailable == idx);
         }

         // RIGHT: applied stack
         List<ReskinEntry> st = appliedStack();
         int sc = Math.min(rows, st.size() - scrollStack);
         for (int i = 0; i < sc; i++) {
            int idx = scrollStack + i;
            if (idx >= st.size()) break;
            drawRow(st.get(idx), colRightX(), top + i * ROW_H, i, mouseX, mouseY, true, false);
         }
      }

      // scrollbars per column (only when content overflows); placed just right of each column
      int topL = listTop();
      int botL = listBottom();
      if (list != null) drawScrollbar(colLeftX() + COL_W + 3, topL, botL, list.size(), scrollAvailable);
      List<ReskinEntry> stackForBar = appliedStack();
      if (stackForBar != null) drawScrollbar(colRightX() + COL_W + 3, topL, botL, stackForBar.size(), scrollStack);

      if (status != null && !status.isEmpty()) {
         this.drawCenteredString(this.fontRenderer, status, this.width / 2, this.height - 44, 0xFFFF55);
      }

      // external Move up / Move down text buttons beside the right column
      List<ReskinEntry> stL = appliedStack();
      boolean hasSel = selectedStack >= 0 && selectedStack < stL.size();
      boolean canUp = hasSel && selectedStack > 0;
      boolean canDown = hasSel && selectedStack < stL.size() - 1;
      int bw2 = 74;
      int bh2 = 18;
      int ux = colRightX() + COL_W + 8;
      int uy = listTop();
      // Move up
      drawRect(ux, uy, ux + bw2, uy + bh2, (hasSel && canUp) ? 0xCC33AAFF : 0x88222222);
      this.drawCenteredString(this.fontRenderer, canUp ? "Move up" : "Move up", ux + bw2 / 2, uy + 5, canUp ? 0xFFFFFF : 0x888888);
      // Move down
      drawRect(ux, uy + bh2 + 4, ux + bw2, uy + bh2 + 4 + bh2, (hasSel && canDown) ? 0xCC33AAFF : 0x88222222);
      this.drawCenteredString(this.fontRenderer, canDown ? "Move down" : "Move down", ux + bw2 / 2, uy + bh2 + 4 + 5, canDown ? 0xFFFFFF : 0x888888);

      // flush debounced applies after 600ms idle
      // (debounce removed — double-click applies immediately)

      int bw = 64;
      int bh = 20;
      int gap = 5;
      String[] labels = {"Apply", "Remove", "Revert All", "Legacy", "Body", "Done"};
      int total = labels.length * bw + (labels.length - 1) * gap;
      int bx = (this.width - total) / 2;
      int by = this.height - 32;
      for (int li = 0; li < labels.length; li++) {         int x0 = bx + li * (bw + gap);         drawRect(x0, by, x0 + bw, by + bh, 0xAA222222);         drawCenteredString(this.fontRenderer, labels[li], x0 + bw / 2, by + 6, 0xFFFFFF);      }
   }

   private void drawChips(int mouseX, int mouseY) {
      int cx = 8;
      int cy = 26;
      chipRows = 1;
      List<String> chips = new ArrayList<>();
      chips.add(null);
      for (String g : ReskinScanner.GIRLS) chips.add(g);
      for (String g : chips) {
         if (cx + CHIP_W > this.width - 4) { cx = 8; cy += CHIP_H + 2; chipRows++; }
         String label = g == null ? "ALL" : g.toUpperCase();
         boolean active = (g == null && filterGirl == null) || (g != null && g.equals(filterGirl));
         int bg = active ? 0xCC33AAFF : (mouseX >= cx && mouseX <= cx + CHIP_W && mouseY >= cy && mouseY <= cy + CHIP_H ? 0xAA444444 : 0x88222222);
         drawRect(cx, cy, cx + CHIP_W, cy + CHIP_H, bg);
         this.drawCenteredString(this.fontRenderer, label, cx + CHIP_W / 2, cy + 3, active ? 0xFFFFFFFF : 0xCCCCCC);
         cx += CHIP_W + CHIP_GAP;
      }
   }

   private void drawRow(ReskinEntry e, int colX, int y, int rowIdx, int mouseX, int mouseY, boolean applied, boolean selected) {
      boolean zebra = rowIdx % 2 == 0;
      drawRect(colX, y, colX + COL_W, y + ROW_H, zebra ? 0x55222222 : 0x33000000);
      if (selected) {
         drawRect(colX - 1, y - 1, colX + COL_W + 1, y + ROW_H + 1, 0xFF33AAFF); // highlight border
      }
      // icon 32x32
      ResourceLocation tex = thumbTextures.get(e.source.getAbsolutePath());
      if (tex != null) {
         try {
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableTexture2D();
            this.mc.renderEngine.bindTexture(tex);
            drawModalRectWithCustomSizedTexture(colX + 2, y + 2, 0, 0, ICON, ICON, ICON, ICON);
         } catch (Throwable t) {}
      } else {
         drawRect(colX + 2, y + 2, colX + 2 + ICON, y + 2 + ICON, 0xFF333333);
      }

      // APPLIED (right) column: no overlay sprites; selection via double-click +
      // external Up/Down buttons beside the list (drawn in drawScreen).
      if (applied) {
         // (thumbnails drawn above; no arrow overlay)
      }

      // name + girls
      this.fontRenderer.drawString(e.displayName.length() > 16 ? e.displayName.substring(0, 15) + "…" : e.displayName, colX + 2 + ICON + 4, y + 4, 0xFFFFFF);
      // girls desc trimmed to fit the column width (no run-off under the zip name)
      String gi = e.girlSummary().toUpperCase();
      int maxW = COL_W - ICON - 6;
      if (this.fontRenderer.getStringWidth(gi) > maxW) {
         gi = this.fontRenderer.trimStringToWidth(gi, maxW - this.fontRenderer.getStringWidth("…")) + "…";
      }
      this.fontRenderer.drawString(gi, colX + 2 + ICON + 4, y + 16, 0xAAAAAA);
      if (applied) {
         String prio = rowIdx == 0 ? "[TOP]" : "[" + (rowIdx + 1) + "]";
         this.fontRenderer.drawString(prio, colX + COL_W - 30, y + 4, rowIdx == 0 ? 0x55FF55 : 0xAAAAAA);
      }
   }

   /** Draws a slim vertical scrollbar (track + thumb) for a column list. */
   private void drawScrollbar(int x, int top, int bottom, int total, int scroll) {
      if (total <= 0) return;
      int viewH = bottom - top;
      int rowH = ROW_H;
      if (total * rowH <= viewH) return; // no overflow -> no scrollbar
      int maxScroll = Math.max(0, total - viewH / rowH);
      drawRect(x, top, x + 3, bottom, 0x77000000); // track
      int thumbH = Math.max(16, viewH * viewH / (total * rowH));
      int maxThumbY = bottom - thumbH - top;
      int thumbY = top + (maxScroll <= 0 ? 0 : (maxThumbY * scroll / maxScroll));
      drawRect(x, thumbY, x + 3, thumbY + thumbH, 0xFFCCCCCC); // thumb
      drawRect(x, thumbY, x + 3, thumbY + 1, 0xFFFFFFFF);      // top edge
      drawRect(x, thumbY + thumbH - 1, x + 3, thumbY + thumbH, 0xFF888888); // bottom edge
   }

   /** Convert buffered thumbs (decoded off-thread) into GL textures on the render thread. */
   private void drawThumbsIfReady() {
      if (!thumbsDirty && pendingThumbs.isEmpty()) return;
      synchronized (pendingThumbs) {
         for (Map.Entry<String, BufferedImage> en : pendingThumbs.entrySet()) {
            try {
               String k = en.getKey();
               if (thumbTextures.containsKey(k)) continue;
               DynamicTexture dt = new DynamicTexture(en.getValue());
               ResourceLocation rl = this.mc.renderEngine.getDynamicTextureLocation("reskin_thumb_" + k.hashCode(), dt);
               thumbTextures.put(k, rl);
            } catch (Throwable t) {
               // unreadable image -> leave placeholder
            }
         }
         pendingThumbs.clear();
      }
      thumbsDirty = false;
   }

   @Override
   public void onGuiClosed() { super.onGuiClosed(); }

   public static void debugHeadless(String groupDir) {
      List<ReskinEntry> found = new ReskinScanner().scan(new java.io.File(groupDir));
      System.out.println("=== reskin scan of " + groupDir + " ===");
      System.out.println("archives found: " + found.size());
      int zip = 0, rar = 0;
      for (ReskinEntry e : found) { if (e.kind == ReskinEntry.Kind.ZIP) zip++; else rar++; }
      System.out.println("zips=" + zip + " rars=" + rar);
   }
}