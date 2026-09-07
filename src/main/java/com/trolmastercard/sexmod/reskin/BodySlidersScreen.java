package com.trolmastercard.sexmod.reskin;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BoneType;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

/**
 * <b>Role.</b> Body/character sliders. Mirrors vanilla {@code GuiSlider}: each row
 * has a 0..1 sliderPosition, drag clamps + updates, the thumb is drawn from
 * {@code position * trackWidth}. Each part shows its REAL NAME, current option
 * index and total count.
 * <p>
 * <b>NPE-proof.</b> This screen never depends on the girl being in any registry:
 * it reads part counts from {@code getCustomPartIdList()} and keeps its own
 * {@code positions[]}; initial values are read once via a guarded
 * {@code buildCustomPartsData} (which is now registry-independent too). Writers
 * go through {@code setCustomPartsData} with a fresh well-formed list.
 */
@SideOnly(Side.CLIENT)
public class BodySlidersScreen extends GuiScreen {
   private final BaseGirlEntity girl;
   private final List<Integer> partCounts = new ArrayList<>();
   private final List<String> partNames = new ArrayList<>();
   private final float[] positions;      // 0..1 per part
   private final java.util.UUID girlId;
   private final String modelCode;
   private int selected = 0;
   private int scroll = 0;
   private boolean dragging = false;
   private String status = "";

   public BodySlidersScreen(BaseGirlEntity girl) {
      this.girl = girl;
      this.girlId = girl.getGirlId();
      this.modelCode = girl.getCustomModelCode();
      this.mc = Minecraft.getMinecraft();
      try {
         List<?> ids = girl.getCustomPartIdList();
         if (ids != null) {
            for (Object o : ids) partCounts.add(((Number) o).intValue());
         }
      } catch (Throwable t) {
         partCounts.clear();
      }
      for (int i = 0; i < partCounts.size(); i++) {
         partNames.add(partNameFor(girl, i));
      }
      positions = new float[partCounts.size()];
      // initial values (guarded; falls back to 0)
      try {
         List<?> data = girl.buildCustomPartsData(girl.getGirlId());
         for (int i = 0; i < positions.length; i++) {
            if (data != null && i < data.size()) {
               Object item = data.get(i);
               if (item instanceof Entry) {
                  Object val = ((Entry) item).getValue();
                  if (val instanceof Entry) {
                     Object v = ((Entry) val).getValue();
                     if (v instanceof Integer) {
                        positions[i] = posFromOption(((Integer) v).intValue(), optionCount(i));
                        continue;
                     }
                  } else if (val instanceof Integer) {
                     positions[i] = posFromOption(((Integer) val).intValue(), optionCount(i));
                     continue;
                  }
               }
            }
            positions[i] = 0F;
         }
      } catch (Throwable t) {
         // leave 0 defaults
      }
      if (positions.length > 0) selected = 0;
   }

   /** 0..1 position for an option index (0..max-1). */
   private static float posFromOption(int option, int max) {
      if (max <= 1) return 1F;
      return Math.max(0F, Math.min(1F, option / (float) (max - 1)));
   }

   private int optionFromPos(int row) {
      int max = optionCount(row);
      if (max <= 1) return 0;
      return Math.round(positions[row] * (max - 1));
   }

   private int optionCount(int row) {
      if (row < 0 || row >= partCounts.size()) return 1;
      return Math.max(1, partCounts.get(row));
   }

   private String partNameFor(BaseGirlEntity girl, int idx) {
      String type = com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl) == null ? ""
         : com.trolmastercard.sexmod.entity.NpcType.getNpcType(girl).name();
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
      return "Part " + (idx + 1);
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   /* ------------------------------------------------------------------ */

   private int rowTop(int row) {
      return 30 + (row - scroll) * 26;
   }

   private int visibleRows() {
      return Math.max(1, (this.height - 60) / 26);
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
      if (mouseButton != 0 || partCounts.isEmpty()) return;

      // Done
      int bw = 60, bh = 20;
      int bx = this.width - bw - 8, by = this.height - 30;
      if (mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh) {
         applyToGirl();
         this.mc.displayGuiScreen(new ReskinBrowserScreen(currentFilterGirl()));
         return;
      }

      int top = 30;
      int rows = visibleRows();
      int trackX = 160;
      int trackW = this.width - trackX - 40;
      for (int r = 0; r < rows; r++) {
         int row = scroll + r;
         if (row >= partCounts.size()) break;
         int ty = top + r * 26;
         // label click = select
         if (mouseY >= ty && mouseY <= ty + 20 && mouseX >= 12 && mouseX <= 140) {
            selected = row;
            return;
         }
         // track click/drag start
         if (mouseY >= ty && mouseY <= ty + 20 && mouseX >= trackX && mouseX <= trackX + trackW) {
            selected = row;
            dragging = true;
            setPositionFromMouse(row, mouseX, trackX, trackW);
            return;
         }
      }
   }

   @Override
   protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
      super.mouseClickMove(mouseX, mouseY, mouseButton, time);
      if (dragging && mouseButton == 0 && selected >= 0 && selected < partCounts.size()) {
         int ty = rowTop(selected);
         int trackX = 160;
         int trackW = this.width - trackX - 40;
         if (mouseY >= ty - 8 && mouseY <= ty + 28) {
            setPositionFromMouse(selected, mouseX, trackX, trackW);
         }
      }
   }

   @Override
   protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      super.mouseReleased(mouseX, mouseY, mouseButton);
      if (mouseButton == 0) dragging = false;
   }

   private void setPositionFromMouse(int row, int mouseX, int trackX, int trackW) {
      float pos = (mouseX - trackX) / (float) Math.max(1, trackW);
      positions[row] = Math.max(0F, Math.min(1F, pos));
      int opt = optionFromPos(row);
      int max = optionCount(row);
      status = partNames.get(row) + " -> option " + (opt + 1) + "/" + max;
      writeToGirl(row, opt);
   }

   /** Push the option to the girl using a fresh well-formed customPartsData list. */
   private void writeToGirl(int row, int opt) {
      try {
         List<?> data = girl.buildCustomPartsData(girl.getGirlId());
         java.util.ArrayList fresh = new java.util.ArrayList();
         int size = Math.max(partCounts.size(), data == null ? 0 : data.size());
         for (int i = 0; i < size; i++) {
            int count = i < partCounts.size() ? Math.max(1, partCounts.get(i)) : 1;
            int val;
            if (i == row) {
               val = Math.max(0, Math.min(opt, count - 1));
            } else if (data != null && i < data.size()) {
               // PRESERVE the other parts' current values. Zeroing them made the
               // girl's other features revert the moment updateModelCodeDNA rebuilt
               // her DNA from customPartsData (live preview + confirm).
               Object item = data.get(i);
               Object v = item instanceof Entry ? ((Entry) item).getValue() : null;
               if (v instanceof Entry) v = ((Entry) v).getValue();
               val = (v instanceof Integer) ? Math.max(0, Math.min(((Integer) v).intValue(), count - 1)) : 0;
            } else {
               val = 0;
            }
            fresh.add(new SimpleEntry<>(BoneType.GIRL_SPECIFIC,
               new SimpleEntry<>(new ArrayList<>(), val)));
         }
         girl.setCustomPartsData(fresh);
      } catch (Throwable t) {
         // display-only feature; the preview keeps moving regardless
      }
   }

   /** Persist the current slider options to the (real) girl via UploadModelStringPacket. */
   private void applyToGirl() {
      try {
         if (partCounts.isEmpty()) return;
         java.util.ArrayList<Integer> partIds = new java.util.ArrayList<>();
         for (int i = 0; i < partCounts.size(); i++) {
            partIds.add(optionFromPos(i));
         }
         com.trolmastercard.sexmod.networking.PacketHandler.networkWrapper.sendToServer(
            new com.trolmastercard.sexmod.networking.UploadModelStringPacket(
               modelCode, girlId, partIds)
         );
         status = "Applied " + partCounts.size() + " part option(s).";
      } catch (Throwable t) {
         status = "Could not apply: " + t.getClass().getSimpleName();
      }
   }

   /* ------------------------------------------------------------------ */

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();

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
         int trackX = 160;
         int trackW = this.width - trackX - 40;
         int opt = optionFromPos(row);
         int max = optionCount(row);
         boolean sel = row == selected;

         drawRect(4, ty, this.width - 4, ty + 22, sel ? 0x8033AAFF : (r % 2 == 0 ? 0x44222222 : 0x22000000));
         this.fontRenderer.drawString(partNames.get(row), 12, ty + 4, sel ? 0xFFFF55 : 0xFFFFFF);
         this.fontRenderer.drawString("option " + (opt + 1) + "/" + max, 12, ty + 13, 0xAAAAAA);

         int fill = Math.round(trackW * positions[row]);
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