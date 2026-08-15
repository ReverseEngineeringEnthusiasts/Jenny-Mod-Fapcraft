package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

/**
 * Scrollable list of model/part entries shown in the {@link ClothingScreen}
 * customization GUI. One row per {@link BoneType} (plus a special "add custom
 * part" row), each row rendering a preview of the currently selected model via
 * a temporary {@link SexSceneEntity} and offering the girl-specific part
 * toggles and the "cross/next" buttons.
 * <p>
 * <b>Data flow.</b> Entries are rebuilt every frame from
 * {@link ClothingScreen#m} (static custom-part registry map) and
 * {@link ServerWhitelistManager#getModelParts}. Preview {@link SexSceneEntity}s
 * are created and {@code removeEntityDangerously}-removed within a single draw
 * call — they must never outlive the frame, and drawing must not happen while
 * the entity list is being ticked.
 * <p>
 * CLIENT-side only. Scrolling is mouse-wheel driven and ignores the vanilla
 * scroll bar (bar is drawn but non-functional by design).
 */
public class CustomModelList extends GuiListExtended {
   static final int TEXT_COLOR = 3809871;
   static final List<BoneType> boneTypes = Arrays.asList(BoneType.values());
   static final String PLACEHOLDER_NAME = "MMMMMMMMMM";
   protected static int SCROLL_SPEED = 200;
   private List<CustomModelList.ModelListEntry> entries = new ArrayList<>();
   ClothingScreen parentScreen;
   boolean needsRefresh = false;
   float scrollOffset = 0.0F;

   public CustomModelList(Minecraft mc, ClothingScreen parentScreen) {
      super(mc, parentScreen.width / 2, parentScreen.height, 0, parentScreen.height, 30);
      SCROLL_SPEED = parentScreen.width / 2;
      this.parentScreen = parentScreen;
   }

   public IGuiListEntry getListEntry(int index) {
      return this.entries.get(index);
   }

   protected int getSize() {
      return this.entries.size();
   }

   protected int getScrollBarX() {
      return 0;
   }

   protected void drawContainerBackground(Tessellator tessellator) {
   }

   /**
    * Scrolls by one half-slot per wheel notch, but only while the cursor is
    * within the list bounds.
    */
   public void handleMouseInput() {
      if (this.isMouseYWithinSlotBounds(this.mouseY)) {
         int wheelDelta = Mouse.getEventDWheel();
         if (wheelDelta != 0) {
            byte direction;
            if (wheelDelta > 0) {
               direction = -1;
            } else {
               direction = 1;
            }

            this.amountScrolled = this.amountScrolled + direction * this.slotHeight / 2;
         }
      }
   }

   protected void overlayBackground(int left, int top, int alpha1, int alpha2) {
   }

   /**
    * Vertically centers the list on screen when the content fits, otherwise
    * pins it to the top so scrolling covers the full content height.
    */
   void updateScrollbar() {
      int contentHeight = this.entries.size() * this.slotHeight;
      if (contentHeight > this.height) {
         this.top = 0;
      } else {
         int remaining = this.height - contentHeight;
         this.top = remaining / 2;
      }
   }

   /**
    * Rebuilds and sorts the entries from {@link ClothingScreen#m} (BoneType ->
    * (model names, selected index)), prepends the {@code cross} entry to the
    * custom-bone models, appends the "add custom part" row (only when at least
    * one custom bone exists), then refreshes scroll state.
    */
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.entries.clear();
      int customBoneCount = 0;

      for (Entry entry : ClothingScreen.m) {
         BoneType boneType = (BoneType)entry.getKey();
         Entry data = (Entry)entry.getValue();
         this.entries.add(new CustomModelList.ModelListEntry(boneType, (List<String>)data.getKey(), (Integer)data.getValue()));
         if (BoneType.CUSTOM_BONE.equals(entry.getKey())) {
            customBoneCount++;
         }
      }

      this.entries.sort(Comparator.comparingInt(entry -> boneTypes.indexOf(entry.boneType)));
      List customParts = ServerWhitelistManager.getModelParts(this.parentScreen.previewGirl).get(BoneType.CUSTOM_BONE);
      customParts.add(0, "cross");
      this.entries.add(new CustomModelList.ModelListEntry(customBoneCount > 1));
      this.updateScrollbar();
      this.onMouseMove(mouseX, mouseY, partialTicks);
      if (this.needsRefresh) {
         this.scrollBy(999999);
         this.needsRefresh = false;
      }
   }

   /**
    * Full draw pass: background, selection box and a hand-rolled scroll bar
    * (track, thumb and highlight) rendered with immediate-mode GL quads.
    * Mostly vanilla {@link GuiListExtended} machinery, kept because the custom
    * entries need precise mouse-hit testing.
    */
   void onMouseMove(int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         this.mouseX = mouseX;
         this.mouseY = mouseY;
         this.drawBackground();
         int scrollBarX = this.getScrollBarX();
         int thumbX = scrollBarX + 6;
         this.bindAmountScrolled();
         GlStateManager.disableLighting();
         GlStateManager.disableFog();
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         this.drawContainerBackground(tessellator);
         int left = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
         int top = this.top + 4 - (int)this.amountScrolled;
         if (this.hasListHeader) {
            this.drawListHeader(left, top, tessellator);
         }

         this.drawSelectionBox(left, top, mouseX, mouseY, partialTicks);
         GlStateManager.disableDepth();
         this.overlayBackground(0, this.top, 255, 255);
         this.overlayBackground(this.bottom, this.height, 255, 255);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
         GlStateManager.disableAlpha();
         GlStateManager.shadeModel(7425);
         GlStateManager.disableTexture2D();
         int maxScroll = this.getMaxScroll();
         if (maxScroll > 0) {
            int thumbHeight = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            thumbHeight = MathHelper.clamp(thumbHeight, 32, this.bottom - this.top - 8);
            int thumbY = (int)this.amountScrolled * (this.bottom - this.top - thumbHeight) / maxScroll + this.top;
            if (thumbY < this.top) {
               thumbY = this.top;
            }

            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarX, this.bottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
            buffer.pos(thumbX, this.bottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
            buffer.pos(thumbX, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.pos(scrollBarX, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarX, thumbY + thumbHeight, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
            buffer.pos(thumbX, thumbY + thumbHeight, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
            buffer.pos(thumbX, thumbY, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollBarX, thumbY, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarX, thumbY + thumbHeight - 1, 0.0).tex(0.0, 1.0).color(192, 192, 192, 255).endVertex();
            buffer.pos(thumbX - 1, thumbY + thumbHeight - 1, 0.0).tex(1.0, 1.0).color(192, 192, 192, 255).endVertex();
            buffer.pos(thumbX - 1, thumbY, 0.0).tex(1.0, 0.0).color(192, 192, 192, 255).endVertex();
            buffer.pos(scrollBarX, thumbY, 0.0).tex(0.0, 0.0).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
         }

         this.renderDecorations(mouseX, mouseY);
         GlStateManager.enableTexture2D();
         GlStateManager.shadeModel(7424);
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
      }
   }

   public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
      this.onMouseMove(mouseX, mouseY, mouseButton);
      return super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   /**
    * Computes which entry the mouse is over and routes the click to its
    * row-background handler ({@link ModelListEntry#drawBackground}), which is
    * what implements the per-row buttons.
    */
   void getSlotIndex(int mouseX, int mouseY, int mouseButton) {
      if (mouseX <= this.width) {
         int scrolled = this.getAmountScrolled();
         float relativeY = scrolled + mouseY - 5 - this.top;
         int rowIndex = Math.round((float)Math.floor(relativeY / this.slotHeight));
         int rowY = (int)Math.round((relativeY / this.slotHeight - Math.floor(relativeY / this.slotHeight)) * this.slotHeight);
         if (rowIndex >= 0) {
            if (rowIndex < this.entries.size()) {
               this.entries.get(rowIndex).drawBackground(mouseX, rowY, mouseButton, rowIndex);
            }
         }
      }
   }

   /**
    * One list row. Three kinds, selected by constructor: a model row for a
    * {@link BoneType} (preview + model-name labels), the custom-part header row
    * (add/remove buttons), and the girl-specific part rows (toggle + scale
    * slider). Draws a temporary {@link SexSceneEntity} as the part preview and
    * forwards clicks to the parent {@link ClothingScreen} for state changes.
    */
   @SideOnly(Side.CLIENT)
   public class ModelListEntry implements IGuiListEntry {
      static final int ICON_SIZE = 4;
      public BoneType boneType;
      public List<String> modelNames;
      public int selectedIndex;
      FontRenderer fontRenderer;
      boolean isVisible = false;
      boolean isSelected = false;

      public ModelListEntry(BoneType boneType, List<String> modelNames, int selectedIndex) {
         this.boneType = boneType;
         this.modelNames = modelNames;
         this.selectedIndex = selectedIndex;
         this.fontRenderer = CustomModelList.this.mc.fontRenderer;
      }

      public ModelListEntry(boolean isSelected) {
         this.isSelected = isSelected;
         this.isVisible = true;
      }

      boolean isInBounds(int x, int y, int left, int top, int right, int bottom) {
         if (x < left) {
            return false;
         } else if (x > right) {
            return false;
         } else {
            return y < top ? false : y <= bottom;
         }
      }

      void getScrollY(int y, int mouseX, int mouseY) {
         int x = 30;
         y += 5;
         CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
         CustomModelList.this.parentScreen.drawTexturedModalRect(x, y, 40, this.isInBounds(mouseX, mouseY, x, y, 50, y + 20) ? 40 : 20, 20, 20);
         x += 40;
         CustomModelList.this.parentScreen.drawTexturedModalRect(x, y, this.isSelected ? 60 : 80, this.isSelected && this.isInBounds(mouseX, mouseY, x, y, x + 20, y + 20) ? 40 : 20, 20, 20);
      }

      /**
    * Renders the row: background, part icon, preview of the selected model
    * (scaled/offset by {@link ServerWhitelistManager.ModelData}) and the
    * model-name + model-code labels with hover tooltips. A temporary
    * {@link SexSceneEntity} is spawned for the preview and removed before the
    * method returns.
    */
   void drawEntry(int y, int mouseX, int mouseY) {
         CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
         CustomModelList.this.parentScreen.drawTexturedModalRect(5, y, 0, 60, this.selectedIndex == 0 ? 119 : 256, 30);
         int x = 15;
         y += 5;
         CustomModelList.this.parentScreen.drawPartIcon(x, y, this.boneType.iconXPos);
         x += 25;
         x = this.getEntryWidth(x, y, mouseX, mouseY);
         BaseGirlEntity previewGirl = CustomModelList.this.parentScreen.getPreviewGirl();
         SexSceneEntity sceneEntity;
         if (this.selectedIndex == 0) {
            sceneEntity = SexSceneEntity.createSceneEntity(CustomModelList.this.mc.world, previewGirl.getGirlId(), this.boneType);
         } else {
            sceneEntity = new SexSceneEntity(previewGirl.world, previewGirl.getGirlId(), this.modelNames.get(this.selectedIndex));
         }

         ServerWhitelistManager.ModelData modelData = ServerWhitelistManager.getModelDataForGirl(sceneEntity.getModelCode());
         if (modelData != null) {
            float scale = !sceneEntity.isItemModel ? modelData.getScale() : 1.0F;
            int xOffset = (int)(-modelData.getXOffset());
            CustomModelList.this.parentScreen.drawPart(x, y + 10 + (sceneEntity.isItemModel ? 0 : 6) + xOffset, 30.0F * scale, sceneEntity);
            if (this.selectedIndex != 0) {
               CustomModelList.this.parentScreen.drawPreviewModel(sceneEntity);
            }

            CustomModelList.this.mc.world.removeEntityDangerously(sceneEntity);
            x = (int)(x + 30.0F);
            if (this.selectedIndex != 0) {
               int labelX = x;
               String modelName = this.modelNames.get(this.selectedIndex);
               String shortName = modelName.length() > 10 ? modelName.substring(0, 7) + "..." : modelName;
               this.drawEntryLabel(shortName, x, y + 10);
               x += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int labelEndX = x;
               int codeStartX = x;
               String modelCode = ServerWhitelistManager.getModelCode(modelName);
               String shortCode = modelCode.length() > 10 ? modelCode.substring(0, 7) + "..." : modelCode;
               this.drawEntryLabel(shortCode, x, y + 10);
               x += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int codeEndX = x;
               if (this.isInBounds(mouseX, mouseY, labelX, y + 10, labelEndX, y + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(modelName, mouseX, mouseY);
               }

               if (this.isInBounds(mouseX, mouseY, codeStartX, y + 10, codeEndX, y + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(modelCode, mouseX, mouseY);
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.color(255.0F, 255.0F, 255.0F, 255.0F);
            }
         } else {
            if (!sceneEntity.isItemModel) {
            }

            byte xOffset2 = 0;
            CustomModelList.this.parentScreen.drawPart(x, y + 10 + (sceneEntity.isItemModel ? 0 : 6) + xOffset2, 30.0F, sceneEntity);
            if (this.selectedIndex != 0) {
               CustomModelList.this.parentScreen.drawPreviewModel(sceneEntity);
            }

            CustomModelList.this.mc.world.removeEntityDangerously(sceneEntity);
            x = (int)(x + 30.0F);
            if (this.selectedIndex != 0) {
               int labelX2 = x;
               String modelName2 = this.modelNames.get(this.selectedIndex);
               String shortName2 = modelName2.length() > 10 ? modelName2.substring(0, 7) + "..." : modelName2;
               this.drawEntryLabel(shortName2, x, y + 10);
               x += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int labelEndX2 = x;
               int codeStartX2 = x;
               String modelCode2 = ServerWhitelistManager.getModelCode(modelName2);
               String shortCode2 = modelCode2.length() > 10 ? modelCode2.substring(0, 7) + "..." : modelCode2;
               this.drawEntryLabel(shortCode2, x, y + 10);
               x += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int codeEndX2 = x;
               if (this.isInBounds(mouseX, mouseY, labelX2, y + 10, labelEndX2, y + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(modelName2, mouseX, mouseY);
               }

               if (this.isInBounds(mouseX, mouseY, codeStartX2, y + 10, codeEndX2, y + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(modelCode2, mouseX, mouseY);
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.color(255.0F, 255.0F, 255.0F, 255.0F);
            }
         }
      }

      /**
    * Draws the girl-specific part toggle button (left: off, right: on,
    * highlighted when hovered).
    */
   int getEntryWidth(int x, int y, int mouseX, int mouseY) {
         CustomModelList.this.parentScreen.drawPartBackground(x, y, 0, 20 * (this.isInBounds(mouseX, mouseY, x, y, x + 20, y + 20) ? 2 : 1));
         x += 20;
         CustomModelList.this.parentScreen.drawPartBackground(x, y, 20, 20 * (this.isInBounds(mouseX, mouseY, x, y, x + 20, y + 20) ? 2 : 1));
         return x + 40;
      }

      /**
    * Draws the scale slider track and thumb, and immediately persists the
    * slider value (0..100) onto the preview girl via
    * {@code setCustomPartValue} — the preview updates live as the slider is
    * dragged.
    */
   void onEntryClick(int x, int y, int mouseX, int mouseY, int partIndex) {
         CustomModelList.this.parentScreen.drawTexturedModalRect(x, y, 140, 20, 79, 20);
         x += 4;
         int trackStart = x;
         int trackEnd = x + 71 - 4;
         float scale = this.getPartScale(y, trackStart, trackEnd, mouseX, mouseY, partIndex);
         int thumbX = (int)RotationHelper.lerp(trackStart, trackEnd, scale);
         CustomModelList.this.parentScreen.drawTexturedModalRect(thumbX, y, this.isInBounds(mouseX, mouseY, thumbX, y, thumbX + 4, y + 20) ? 223 : 219, 20, 4, 20);
         CustomModelList.this.parentScreen.previewGirl.setCustomPartValue(partIndex, (int)(scale * 100.0F));
      }

      /**
    * Maps the mouse X to a slider fraction in 0..1. When not editing, or when
    * the mouse is outside the slider track, returns the current stored value
    * instead, so the slider only changes while actively dragged.
    */
   float getPartScale(int trackStart, int trackEnd, int mouseX, int mouseY, int x, int partIndex) {
         if (!CustomModelList.this.parentScreen.isEditing) {
            return this.getPartValue(partIndex);
         }

         if (mouseY > 0.33333334F * CustomModelList.this.parentScreen.width) {
            return this.getPartValue(partIndex);
         }

         if (x < trackStart || x > trackStart + 20) {
            return this.getPartValue(partIndex);
         }

         if (mouseY < trackEnd) {
            return 0.0F;
         }

         if (mouseY > mouseX) {
            return 1.0F;
         }

         mouseX -= trackEnd;
         mouseY -= trackEnd;
         return (float)mouseY / mouseX;
      }

      float getPartValue(int partIndex) {
         Entry data = CustomModelList.this.parentScreen.previewGirl.buildCustomPartsData(CustomModelList.this.parentScreen.girlId).get(partIndex);
         return ((Integer)((Entry)data.getValue()).getValue()).intValue() / 100.0F;
      }

      /**
    * Draws a girl-specific row: the part toggle + slider when the part is
    * enabled, or just the (dimmed) toggle when disabled.
    */
   void isEntrySelected(int y, int mouseX, int mouseY, int partIndex) {
         if (CustomModelList.this.parentScreen.previewGirl.isPartEnabled(partIndex)) {
            CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
            CustomModelList.this.parentScreen.drawTexturedModalRect(5, y, 0, 60, 119, 30);
            int x = 15;
            y += 5;
            CustomModelList.this.parentScreen.drawPartAt(x, y, CustomModelList.this.parentScreen.previewGirl.getModelPartByIndex(partIndex));
            x += 25;
            this.onEntryClick(x, y, mouseX, mouseY, partIndex);
         } else {
            CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
            CustomModelList.this.parentScreen.drawTexturedModalRect(5, y, 0, 90, 95, 30);
            int x2 = 15;
            y += 5;
            CustomModelList.this.parentScreen.drawPartAt(x2, y, CustomModelList.this.parentScreen.previewGirl.getModelPartByIndex(partIndex));
            x2 += 25;
            this.getEntryWidth(x2, y, mouseX, mouseY);
         }
      }

      public void drawEntry(int slotIndex, int x, int y, int mouseX, int mouseY, int rowTop, int rowBottom, boolean isHovered, float partialTicks) {
         if (this.isVisible) {
            this.getScrollY(y, rowTop, rowBottom);
         } else if (this.boneType == BoneType.GIRL_SPECIFIC) {
            this.isEntrySelected(y, rowTop, rowBottom, slotIndex);
         } else {
            this.drawEntry(y, rowTop, rowBottom);
         }
      }

      void drawEntryLabel(String text, int x, int y) {
         this.fontRenderer.drawString(text, x, y, 3809871);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

      void handleScrollClick(int x, int y) {
         int buttonX = 30;
         if (x > buttonX && x < 50) {
            CustomModelList.this.needsRefresh = true;
            CustomModelList.this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ArrayList parts = new ArrayList();
            parts.add("cross");
            parts.addAll(ServerWhitelistManager.getModelParts(CustomModelList.this.parentScreen.previewGirl).get(BoneType.CUSTOM_BONE));
            ClothingScreen.m.add(ClothingScreen.getCustomPartData(CustomModelList.this.parentScreen.previewGirl));
         }

         if (this.isSelected) {
            buttonX += 40;
            if (x > buttonX && x < buttonX + 20) {
               CustomModelList.this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               ClothingScreen.m.remove(ClothingScreen.m.size() - 1);
            }
         }
      }

      void handleSlotClick(int x, int partIndex) {
         if (x > 40 && x < 60) {
            CustomModelList.this.parentScreen.onBoneTypeToggle(this.boneType, false, partIndex);
         }

         if (x > 60 && x < 80) {
            CustomModelList.this.parentScreen.onBoneTypeToggle(this.boneType, true, partIndex);
         }
      }

      void isGirlSpecific(int x, int partIndex) {
         if (!CustomModelList.this.parentScreen.previewGirl.isPartEnabled(partIndex)) {
            this.handleSlotClick(x, partIndex);
         }
      }

      /**
    * Row-background click routing (the "buttons" of a row). For the custom-part
    * header row: the add button re-registers the current custom part into
    * {@link ClothingScreen#m} and marks the list for refresh; the remove button
    * (only when the header is selected) drops the last entry. For all other
    * rows: girl-specific rows toggle the part via
    * {@code ClothingScreen.onBoneTypeToggle}.
    */
   void drawBackground(int mouseX, int mouseY, int mouseButton, int rowIndex) {
         if (mouseButton == 0) {
            if (mouseY >= 5) {
               if (mouseY <= 25) {
                  if (this.isVisible) {
                     this.handleScrollClick(mouseX, mouseY);
                  } else if (this.boneType == BoneType.GIRL_SPECIFIC) {
                     this.isGirlSpecific(mouseX, rowIndex);
                  } else {
                     this.handleSlotClick(mouseX, rowIndex);
                  }
               }
            }
         }
      }

      public void updatePosition(int x, int y, int mouseX, float partialTicks) {
      }

      public boolean mousePressed(int slotIndex, int x, int y, int mouseX, int mouseY, int partialTicks) {
         return false;
      }

      public void mouseReleased(int slotIndex, int x, int y, int mouseX, int mouseY, int partialTicks) {
      }
   }
}
