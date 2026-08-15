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

   public CustomModelList(Minecraft var1, ClothingScreen var2) {
      super(var1, var2.width / 2, var2.height, 0, var2.height, 30);
      SCROLL_SPEED = var2.width / 2;
      this.parentScreen = var2;
   }

   public IGuiListEntry getListEntry(int var1) {
      return this.entries.get(var1);
   }

   protected int getSize() {
      return this.entries.size();
   }

   protected int getScrollBarX() {
      return 0;
   }

   protected void drawContainerBackground(Tessellator var1) {
   }

   /**
    * Scrolls by one half-slot per wheel notch, but only while the cursor is
    * within the list bounds.
    */
   public void handleMouseInput() {
      if (this.isMouseYWithinSlotBounds(this.mouseY)) {
         int var1 = Mouse.getEventDWheel();
         if (var1 != 0) {
            byte var2;
            if (var1 > 0) {
               var2 = -1;
            } else {
               var2 = 1;
            }

            this.amountScrolled = this.amountScrolled + var2 * this.slotHeight / 2;
         }
      }
   }

   protected void overlayBackground(int var1, int var2, int var3, int var4) {
   }

   /**
    * Vertically centers the list on screen when the content fits, otherwise
    * pins it to the top so scrolling covers the full content height.
    */
   void updateScrollbar() {
      int var1 = this.entries.size() * this.slotHeight;
      if (var1 > this.height) {
         this.top = 0;
      } else {
         int var2 = this.height - var1;
         this.top = var2 / 2;
      }
   }

   /**
    * Rebuilds and sorts the entries from {@link ClothingScreen#m} (BoneType ->
    * (model names, selected index)), prepends the {@code cross} entry to the
    * custom-bone models, appends the "add custom part" row (only when at least
    * one custom bone exists), then refreshes scroll state.
    */
   public void drawScreen(int var1, int var2, float var3) {
      this.entries.clear();
      int var4 = 0;

      for (Entry var6 : ClothingScreen.m) {
         BoneType var7 = (BoneType)var6.getKey();
         Entry var8 = (Entry)var6.getValue();
         this.entries.add(new CustomModelList.ModelListEntry(var7, (List<String>)var8.getKey(), (Integer)var8.getValue()));
         if (BoneType.CUSTOM_BONE.equals(var6.getKey())) {
            var4++;
         }
      }

      this.entries.sort(Comparator.comparingInt(var0 -> boneTypes.indexOf(var0.boneType)));
      List var9 = ServerWhitelistManager.getModelParts(this.parentScreen.previewGirl).get(BoneType.CUSTOM_BONE);
      var9.add(0, "cross");
      this.entries.add(new CustomModelList.ModelListEntry(var4 > 1));
      this.updateScrollbar();
      this.onMouseMove(var1, var2, var3);
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
   void onMouseMove(int var1, int var2, float var3) {
      if (this.visible) {
         this.mouseX = var1;
         this.mouseY = var2;
         this.drawBackground();
         int var4 = this.getScrollBarX();
         int var5 = var4 + 6;
         this.bindAmountScrolled();
         GlStateManager.disableLighting();
         GlStateManager.disableFog();
         Tessellator var6 = Tessellator.getInstance();
         BufferBuilder var7 = var6.getBuffer();
         this.drawContainerBackground(var6);
         int var8 = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
         int var9 = this.top + 4 - (int)this.amountScrolled;
         if (this.hasListHeader) {
            this.drawListHeader(var8, var9, var6);
         }

         this.drawSelectionBox(var8, var9, var1, var2, var3);
         GlStateManager.disableDepth();
         this.overlayBackground(0, this.top, 255, 255);
         this.overlayBackground(this.bottom, this.height, 255, 255);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
         GlStateManager.disableAlpha();
         GlStateManager.shadeModel(7425);
         GlStateManager.disableTexture2D();
         int var10 = this.getMaxScroll();
         if (var10 > 0) {
            int var11 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            var11 = MathHelper.clamp(var11, 32, this.bottom - this.top - 8);
            int var12 = (int)this.amountScrolled * (this.bottom - this.top - var11) / var10 + this.top;
            if (var12 < this.top) {
               var12 = this.top;
            }

            var7.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            var7.pos(var4, this.bottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
            var7.pos(var5, this.bottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
            var7.pos(var5, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
            var7.pos(var4, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
            var6.draw();
            var7.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            var7.pos(var4, var12 + var11, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
            var7.pos(var5, var12 + var11, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
            var7.pos(var5, var12, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
            var7.pos(var4, var12, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
            var6.draw();
            var7.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            var7.pos(var4, var12 + var11 - 1, 0.0).tex(0.0, 1.0).color(192, 192, 192, 255).endVertex();
            var7.pos(var5 - 1, var12 + var11 - 1, 0.0).tex(1.0, 1.0).color(192, 192, 192, 255).endVertex();
            var7.pos(var5 - 1, var12, 0.0).tex(1.0, 0.0).color(192, 192, 192, 255).endVertex();
            var7.pos(var4, var12, 0.0).tex(0.0, 0.0).color(192, 192, 192, 255).endVertex();
            var6.draw();
         }

         this.renderDecorations(var1, var2);
         GlStateManager.enableTexture2D();
         GlStateManager.shadeModel(7424);
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
      }
   }

   public boolean mouseClicked(int var1, int var2, int var3) {
      this.onMouseMove(var1, var2, var3);
      return super.mouseClicked(var1, var2, var3);
   }

   /**
    * Computes which entry the mouse is over and routes the click to its
    * row-background handler ({@link ModelListEntry#drawBackground}), which is
    * what implements the per-row buttons.
    */
   void getSlotIndex(int var1, int var2, int var3) {
      if (var1 <= this.width) {
         int var4 = this.getAmountScrolled();
         float var5 = var4 + var2 - 5 - this.top;
         int var6 = Math.round((float)Math.floor(var5 / this.slotHeight));
         int var7 = (int)Math.round((var5 / this.slotHeight - Math.floor(var5 / this.slotHeight)) * this.slotHeight);
         if (var6 >= 0) {
            if (var6 < this.entries.size()) {
               this.entries.get(var6).drawBackground(var1, var7, var3, var6);
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

      public ModelListEntry(BoneType var2, List<String> var3, int var4) {
         this.boneType = var2;
         this.modelNames = var3;
         this.selectedIndex = var4;
         this.fontRenderer = CustomModelList.this.mc.fontRenderer;
      }

      public ModelListEntry(boolean var2) {
         this.isSelected = var2;
         this.isVisible = true;
      }

      boolean isInBounds(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (var1 < var3) {
            return false;
         } else if (var1 > var5) {
            return false;
         } else {
            return var2 < var4 ? false : var2 <= var6;
         }
      }

      void getScrollY(int var1, int var2, int var3) {
         int var4 = 30;
         var1 += 5;
         CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
         CustomModelList.this.parentScreen.drawTexturedModalRect(var4, var1, 40, this.isInBounds(var2, var3, var4, var1, 50, var1 + 20) ? 40 : 20, 20, 20);
         var4 += 40;
         CustomModelList.this.parentScreen.drawTexturedModalRect(var4, var1, this.isSelected ? 60 : 80, this.isSelected && this.isInBounds(var2, var3, var4, var1, var4 + 20, var1 + 20) ? 40 : 20, 20, 20);
      }

      /**
    * Renders the row: background, part icon, preview of the selected model
    * (scaled/offset by {@link ServerWhitelistManager.ModelData}) and the
    * model-name + model-code labels with hover tooltips. A temporary
    * {@link SexSceneEntity} is spawned for the preview and removed before the
    * method returns.
    */
   void drawEntry(int var1, int var2, int var3) {
         CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
         CustomModelList.this.parentScreen.drawTexturedModalRect(5, var1, 0, 60, this.selectedIndex == 0 ? 119 : 256, 30);
         int var4 = 15;
         var1 += 5;
         CustomModelList.this.parentScreen.drawPartIcon(var4, var1, this.boneType.iconXPos);
         var4 += 25;
         var4 = this.getEntryWidth(var4, var1, var2, var3);
         BaseGirlEntity var5 = CustomModelList.this.parentScreen.getPreviewGirl();
         SexSceneEntity var6;
         if (this.selectedIndex == 0) {
            var6 = SexSceneEntity.createSceneEntity(CustomModelList.this.mc.world, var5.getGirlId(), this.boneType);
         } else {
            var6 = new SexSceneEntity(var5.world, var5.getGirlId(), this.modelNames.get(this.selectedIndex));
         }

         ServerWhitelistManager.ModelData var7 = ServerWhitelistManager.getModelDataForGirl(var6.getModelCode());
         if (var7 != null) {
            float var8 = !var6.isItemModel ? var7.getScale() : 1.0F;
            int var27 = (int)(-var7.getXOffset());
            CustomModelList.this.parentScreen.drawPart(var4, var1 + 10 + (var6.isItemModel ? 0 : 6) + var27, 30.0F * var8, var6);
            if (this.selectedIndex != 0) {
               CustomModelList.this.parentScreen.drawPreviewModel(var6);
            }

            CustomModelList.this.mc.world.removeEntityDangerously(var6);
            var4 = (int)(var4 + 30.0F);
            if (this.selectedIndex != 0) {
               int var28 = var4;
               String var29 = this.modelNames.get(this.selectedIndex);
               String var30 = var29.length() > 10 ? var29.substring(0, 7) + "..." : var29;
               this.drawEntryLabel(var30, var4, var1 + 10);
               var4 += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int var31 = var4;
               int var32 = var4;
               String var33 = ServerWhitelistManager.getModelCode(var29);
               String var34 = var33.length() > 10 ? var33.substring(0, 7) + "..." : var33;
               this.drawEntryLabel(var34, var4, var1 + 10);
               var4 += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int var35 = var4;
               if (this.isInBounds(var2, var3, var28, var1 + 10, var31, var1 + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(var29, var2, var3);
               }

               if (this.isInBounds(var2, var3, var32, var1 + 10, var35, var1 + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(var33, var2, var3);
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.color(255.0F, 255.0F, 255.0F, 255.0F);
            }
         } else {
            if (!var6.isItemModel) {
            }

            byte var9 = 0;
            CustomModelList.this.parentScreen.drawPart(var4, var1 + 10 + (var6.isItemModel ? 0 : 6) + var9, 30.0F, var6);
            if (this.selectedIndex != 0) {
               CustomModelList.this.parentScreen.drawPreviewModel(var6);
            }

            CustomModelList.this.mc.world.removeEntityDangerously(var6);
            var4 = (int)(var4 + 30.0F);
            if (this.selectedIndex != 0) {
               int var10 = var4;
               String var11 = this.modelNames.get(this.selectedIndex);
               String var12 = var11.length() > 10 ? var11.substring(0, 7) + "..." : var11;
               this.drawEntryLabel(var12, var4, var1 + 10);
               var4 += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int var13 = var4;
               int var14 = var4;
               String var15 = ServerWhitelistManager.getModelCode(var11);
               String var16 = var15.length() > 10 ? var15.substring(0, 7) + "..." : var15;
               this.drawEntryLabel(var16, var4, var1 + 10);
               var4 += this.fontRenderer.getStringWidth("MMMMMMMMMM");
               int var17 = var4;
               if (this.isInBounds(var2, var3, var10, var1 + 10, var13, var1 + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(var11, var2, var3);
               }

               if (this.isInBounds(var2, var3, var14, var1 + 10, var17, var1 + 10 + this.fontRenderer.FONT_HEIGHT)) {
                  CustomModelList.this.parentScreen.drawHoverText(var15, var2, var3);
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
   int getEntryWidth(int var1, int var2, int var3, int var4) {
         CustomModelList.this.parentScreen.drawPartBackground(var1, var2, 0, 20 * (this.isInBounds(var3, var4, var1, var2, var1 + 20, var2 + 20) ? 2 : 1));
         var1 += 20;
         CustomModelList.this.parentScreen.drawPartBackground(var1, var2, 20, 20 * (this.isInBounds(var3, var4, var1, var2, var1 + 20, var2 + 20) ? 2 : 1));
         return var1 + 40;
      }

      /**
    * Draws the scale slider track and thumb, and immediately persists the
    * slider value (0..100) onto the preview girl via
    * {@code setCustomPartValue} — the preview updates live as the slider is
    * dragged.
    */
   void onEntryClick(int var1, int var2, int var3, int var4, int var5) {
         CustomModelList.this.parentScreen.drawTexturedModalRect(var1, var2, 140, 20, 79, 20);
         var1 += 4;
         int var6 = var1;
         int var7 = var1 + 71 - 4;
         float var8 = this.getPartScale(var2, var6, var7, var3, var4, var5);
         int var9 = (int)RotationHelper.lerp(var6, var7, var8);
         CustomModelList.this.parentScreen.drawTexturedModalRect(var9, var2, this.isInBounds(var3, var4, var9, var2, var9 + 4, var2 + 20) ? 223 : 219, 20, 4, 20);
         CustomModelList.this.parentScreen.previewGirl.setCustomPartValue(var5, (int)(var8 * 100.0F));
      }

      /**
    * Maps the mouse X to a slider fraction in 0..1. When not editing, or when
    * the mouse is outside the slider track, returns the current stored value
    * instead, so the slider only changes while actively dragged.
    */
   float getPartScale(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (!CustomModelList.this.parentScreen.isEditing) {
            return this.getPartValue(var6);
         }

         if (var4 > 0.33333334F * CustomModelList.this.parentScreen.width) {
            return this.getPartValue(var6);
         }

         if (var5 < var1 || var5 > var1 + 20) {
            return this.getPartValue(var6);
         }

         if (var4 < var2) {
            return 0.0F;
         }

         if (var4 > var3) {
            return 1.0F;
         }

         var3 -= var2;
         var4 -= var2;
         return (float)var4 / var3;
      }

      float getPartValue(int var1) {
         Entry var2 = CustomModelList.this.parentScreen.previewGirl.buildCustomPartsData(CustomModelList.this.parentScreen.girlId).get(var1);
         return ((Integer)((Entry)var2.getValue()).getValue()).intValue() / 100.0F;
      }

      /**
    * Draws a girl-specific row: the part toggle + slider when the part is
    * enabled, or just the (dimmed) toggle when disabled.
    */
   void isEntrySelected(int var1, int var2, int var3, int var4) {
         if (CustomModelList.this.parentScreen.previewGirl.isPartEnabled(var4)) {
            CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
            CustomModelList.this.parentScreen.drawTexturedModalRect(5, var1, 0, 60, 119, 30);
            int var10 = 15;
            var1 += 5;
            CustomModelList.this.parentScreen.drawPartAt(var10, var1, CustomModelList.this.parentScreen.previewGirl.getModelPartByIndex(var4));
            var10 += 25;
            this.onEntryClick(var10, var1, var2, var3, var4);
         } else {
            CustomModelList.this.mc.renderEngine.bindTexture(ClothingScreen.GUI_TEXTURE);
            CustomModelList.this.parentScreen.drawTexturedModalRect(5, var1, 0, 90, 95, 30);
            int var6 = 15;
            var1 += 5;
            CustomModelList.this.parentScreen.drawPartAt(var6, var1, CustomModelList.this.parentScreen.previewGirl.getModelPartByIndex(var4));
            var6 += 25;
            this.getEntryWidth(var6, var1, var2, var3);
         }
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, float var9) {
         if (this.isVisible) {
            this.getScrollY(var3, var6, var7);
         } else if (this.boneType == BoneType.GIRL_SPECIFIC) {
            this.isEntrySelected(var3, var6, var7, var1);
         } else {
            this.drawEntry(var3, var6, var7);
         }
      }

      void drawEntryLabel(String var1, int var2, int var3) {
         this.fontRenderer.drawString(var1, var2, var3, 3809871);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

      void handleScrollClick(int var1, int var2) {
         int var3 = 30;
         if (var1 > var3 && var1 < 50) {
            CustomModelList.this.needsRefresh = true;
            CustomModelList.this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ArrayList var4 = new ArrayList();
            var4.add("cross");
            var4.addAll(ServerWhitelistManager.getModelParts(CustomModelList.this.parentScreen.previewGirl).get(BoneType.CUSTOM_BONE));
            ClothingScreen.m.add(ClothingScreen.getCustomPartData(CustomModelList.this.parentScreen.previewGirl));
         }

         if (this.isSelected) {
            var3 += 40;
            if (var1 > var3 && var1 < var3 + 20) {
               CustomModelList.this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               ClothingScreen.m.remove(ClothingScreen.m.size() - 1);
            }
         }
      }

      void handleSlotClick(int var1, int var2) {
         if (var1 > 40 && var1 < 60) {
            CustomModelList.this.parentScreen.onBoneTypeToggle(this.boneType, false, var2);
         }

         if (var1 > 60 && var1 < 80) {
            CustomModelList.this.parentScreen.onBoneTypeToggle(this.boneType, true, var2);
         }
      }

      void isGirlSpecific(int var1, int var2) {
         if (!CustomModelList.this.parentScreen.previewGirl.isPartEnabled(var2)) {
            this.handleSlotClick(var1, var2);
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
   void drawBackground(int var1, int var2, int var3, int var4) {
         if (var3 == 0) {
            if (var2 >= 5) {
               if (var2 <= 25) {
                  if (this.isVisible) {
                     this.handleScrollClick(var1, var2);
                  } else if (this.boneType == BoneType.GIRL_SPECIFIC) {
                     this.isGirlSpecific(var1, var4);
                  } else {
                     this.handleSlotClick(var1, var4);
                  }
               }
            }
         }
      }

      public void updatePosition(int var1, int var2, int var3, float var4) {
      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         return false;
      }

      public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
      }
   }
}
