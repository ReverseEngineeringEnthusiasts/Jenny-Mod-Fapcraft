package com.trolmastercard.sexmod.reskin;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

/**
 * <b>Role.</b> Body/character sliders — shows each custom part of the selected
 * girl with a REAL NAME (hair/skin/eyes/etc.), its current OPTION index and
 * total count, and a slider to change the option. Also shows the character's
 * real display name (e.g. kobold names, not "entity.kobold.name").
 * <p>
 * The values come from {@link BaseGirlEntity#buildCustomPartsData} — each part
 * is a 0-based option index within that part's own count; we display
 * "option X of N" and the slider maps 0..100 -> 0..N-1.
 */
@SideOnly(Side.CLIENT)
public class BodySlidersScreen extends GuiScreen {
   private final BaseGirlEntity girl;
   private final List<Integer> partCounts = new ArrayList<>(); // option counts per part
   private final List<String> partNames = new ArrayList<>();   // friendly names
   private int selected = 0;
   private int scroll = 0;
   private String status = "";

   public BodySlidersScreen(BaseGirlEntity girl) {
      this.girl = girl;
      this.mc = Minecraft.getMinecraft();
      try {
         List<?> ids = girl.getCustomPartIdList();
         if (ids != null) {
            for (Object o : ids) partCounts.add(((Number) o).intValue());
         }
      } catch (Throwable t) {
         partCounts.clear();
      }
      // friendly names (one per part; fall back to "Part N")
      for (int i = 0; i < partCounts.size(); i++) {
         partNames.add(partNameFor(girl, i, partCounts.get(i)));
      }
   }

   /** Human-readable name for a part based on girl type + index. */
   private String partNameFor(BaseGirlEntity girl, int idx, int count) {
      String type = com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl) == null ? ""
         : com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl).name();
      // Order follows each entity's getCustomPartIdList()
      if ("GOBLIN".equals(type)) {
         String[] names = {"Head", "Horns", "Ears", "Hairstyle", "Hair Color", "Skin Color", "Eye Color", "Mouth", "Nose", "Extra"};
         if (idx < names.length) return names[idx];
      } else if ("KOBOLD".equals(type)) {
         String[] names = {"Height", "Pattern", "Markings", "Tail", "Ears", "Horn", "Head Shape", "Mouth", "Eye Color", "Chest", "Extra"};
         if (idx < names.length) return names[idx];
      } else if ("JENNY".equals(type) || "ELLIE".equals(type)) {
         String[] names = {"Outfit", "Hair Color", "Eye Color", "Skin Tone", "Breasts", "Hips", "Legs", "Feet", "Tail", "Accessory"};
         if (idx < names.length) return names[idx];
      }
      // generic
      return "Part " + (idx + 1);
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   private int rowTop(int row) {
      return 30 + (row - scroll) * 26;
   }

   private int visibleRows() {
      return Math.max(1, (this.height - 60) / 26);
   }

   private int optionCount(int row) {
      if (row < 0 || row >= partCounts.size()) return 1;
      return Math.max(1, partCounts.get(row));
   }

   private int optionIndex(int row) {
      try {
         // make sure the girl's part data is built (fresh entities may have
         // empty/absent APPEARANCE_DNA -> buildCustomPartsData return empty list)
         List<?> data = girl.buildCustomPartsData(girl.getGirlId());
         if (data == null || data.isEmpty()) return 0;
         if (row >= 0 && row < data.size()) {
            Object item = data.get(row);
            if (item instanceof Entry) {
               Object val = ((Entry) item).getValue();
               // some girls store a PlainEntry where value() is itself the option
               if (val instanceof Entry) {
                  Object v = ((Entry) val).getValue();
                  if (v instanceof Integer) {
                     int i = ((Integer) v).intValue();
                     int max = optionCount(row);
                     return Math.max(0, Math.min(i, max - 1));
                  }
               } else if (val instanceof Integer) {
                  int i = ((Integer) val).intValue();
                  int max = optionCount(row);
                  return Math.max(0, Math.min(i, max - 1));
               }
            }
         }
      } catch (Throwable t) { }
      return 0;
   }

   @Override
   public void handleMouseInput() {
      super.handleMouseInput();
      int dw = Mouse.getEventDWheel();
      if (dw != 0) {
         scroll -= Integer.signum(dw);
         scroll = Math.max(0, Math.min(scroll, Math.max(0, partCounts.size() - visibleRows())));
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton != 0) return;

      // Done
      int bw = 60, bh = 20;
      int bx = this.width - bw - 8, by = this.height - 30;
      if (mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh) {
         this.mc.displayGuiScreen(new ReskinBrowserScreen(currentFilterGirl()));
         return;
      }

      int top = 30;
      int rows = visibleRows();
      for (int r = 0; r < rows; r++) {
         int row = scroll + r;
         if (row >= partCounts.size()) break;
         int ty = top + r * 26;
         int trackX = 160;
         int trackW = this.width - trackX - 40;
         // click = select row; drag on track = set option
         if (mouseY >= ty && mouseY <= ty + 20 && mouseX >= trackX && mouseX <= trackX + trackW) {
            selected = row;
            setOptionFromMouse(row, mouseX, trackX, trackW);
            return;
         }
         if (mouseY >= ty && mouseY <= ty + 20 && mouseX >= 12 && mouseX <= 140) {
            selected = row;
            return;
         }
      }
   }

   @Override
   protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
      super.mouseClickMove(mouseX, mouseY, mouseButton, time);
      if (mouseButton == 0 && selected >= 0 && selected < partCounts.size()) {
         int ty = rowTop(selected);
         int trackX = 160;
         int trackW = this.width - trackX - 40;
         // allow dragging anywhere horizontally (not just on the row box)
         if (mouseY >= ty - 6 && mouseY <= ty + 26) {
            setOptionFromMouse(selected, mouseX, trackX, trackW);
         }
      }
   }

   private void setOptionFromMouse(int row, int mouseX, int trackX, int trackW) {
      int max = optionCount(row);
      float frac = Math.max(0F, Math.min(1F, (mouseX - trackX) / (float) trackW));
      int opt = Math.round(frac * (max - 1));
      try {
         // Build a fresh well-formed part-data list (only GIRL_SPECIFIC rows,
         // inner value = Integer option index). Avoids the fragile base
         // setCustomPartValue which NPEs when the girl's APPEARANCE_DNA is
         // absent/empty (entry value null -> ClassCastException / NPE).
         List<?> data = girl.buildCustomPartsData(girl.getGirlId());
         java.util.ArrayList fresh = new java.util.ArrayList();
         int size = Math.max(partCounts.size(), data == null ? 0 : data.size());
         for (int i = 0; i < size; i++) {
            int count = i < partCounts.size() ? Math.max(1, partCounts.get(i)) : 1;
            int val = (i == row) ? Math.max(0, Math.min(opt, count - 1)) : 0;
            fresh.add(new java.util.AbstractMap.SimpleEntry<>(com.trolmastercard.sexmod.entity.BoneType.GIRL_SPECIFIC,
               new java.util.AbstractMap.SimpleEntry<>(new java.util.ArrayList<>(), val)));
         }
         girl.setCustomPartsData(fresh);
         status = partNames.get(row) + " -> option " + (opt + 1) + "/" + max;
      } catch (Throwable t) {
         status = "Cannot adjust " + partNames.get(row) + " (" + t.getClass().getSimpleName() + ")";
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();

      // real name (kobold names are stored, not the translation key)
      String girlName;
      try {
         String dn = girl.getDisplayNameText();
         girlName = (dn == null || dn.isEmpty()) ? girl.getClass().getSimpleName() : dn;
      } catch (Throwable t) {
         girlName = girl.getClass().getSimpleName();
      }
      String type = com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl) == null ? ""
         : com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl).name();
      this.drawCenteredString(this.fontRenderer, girlName + " (" + type + ") — character parts", this.width / 2, 8, 0xFFFFFF);

      int top = 30;
      int rows = visibleRows();
      for (int r = 0; r < rows; r++) {
         int row = scroll + r;
         if (row >= partCounts.size()) break;
         int ty = top + r * 26;
         boolean sel = row == selected;
         int trackX = 160;
         int trackW = this.width - trackX - 40;
         int opt = optionIndex(row);
         int max = optionCount(row);

         // row bg
         drawRect(4, ty, this.width - 4, ty + 22, sel ? 0x8033AAFF : (r % 2 == 0 ? 0x44222222 : 0x22000000));

         // name + current
         this.fontRenderer.drawString(partNames.get(row), 12, ty + 4, sel ? 0xFFFF55 : 0xFFFFFF);
         this.fontRenderer.drawString("option " + (opt + 1) + "/" + max, 12, ty + 13, 0xAAAAAA);

         // track + thumb
         int fill = Math.round(trackW * (max <= 1 ? 1.0F : (float) opt / (max - 1)));
         drawRect(trackX, ty + 9, trackX + trackW, ty + 12, 0xFF333333);
         if (fill > 0) drawRect(trackX, ty + 9, trackX + fill, ty + 12, 0xFF33AAFF);
         int thumbX = trackX + fill - 2;
         drawRect(thumbX, ty + 6, thumbX + 4, ty + 15, sel ? 0xFFFFFFFF : 0xFFCCCCCC);
      }

      if (partCounts.isEmpty()) {
         this.drawCenteredString(this.fontRenderer, "This character has no adjustable parts.", this.width / 2, 60, 0x888888);
      }
      if (status != null && !status.isEmpty()) {
         this.drawCenteredString(this.fontRenderer, status, this.width / 2, this.height - 42, 0xFFFF55);
      }

      // Done
      int bw = 60, bh = 20;
      int bx = this.width - bw - 8, by = this.height - 30;
      drawRect(bx, by, bx + bw, by + bh, 0xAA222222);
      this.drawCenteredString(this.fontRenderer, "Done", bx + bw / 2, by + 6, 0xFFFFFF);
   }

   private String currentFilterGirl() {
      try {
         com.trolmastercard.sexmod.entity.NpcType type = com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl);
         if (type != null) return type.name().toLowerCase(java.util.Locale.ROOT);
      } catch (Throwable t) { }
      return null;
   }
}