package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.RemoveItemsPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The girl interaction menu: the "action.names.*" button list (follow me / go
 * home / set new home / equipment plus the girl's dynamic action buttons with
 * item costs) shown when right-clicking a girl. Buttons are sent to the server
 * via {@code girl.doAction(actionName, playerUUID)}, which drives the girl's
 * state machine (actions requiring a boy cam/anchor etc.).
 * <p>
 * <b>Cost handling.</b> Dynamic action buttons (id >= 5) may carry an item
 * cost ({@code actionCosts}); non-creative players must own a matching stack
 * or the action is refused with a chat line and a sad sound. The button click
 * also removes the paid items via {@link RemoveItemsPacket}.
 * <p>
 * CLIENT-side only. The girl's equipment is previewed from her
 * {@link EntityDataManager} entries ({@code WEAPON}, {@code BOW},
 * {@code HELMET_SLOT}, ...) with animated slide-in. Closing the screen calls
 * {@code girl.ac()} to reset the interaction state.
 */
public class GirlInventoryScreen extends GuiScreen {
   final BaseGirlEntity girl;
   final EntityPlayer player;
   final String[] actionNames;
   @Nullable
   final ItemStack[] actionCosts;
   static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   EntityDataManager dataManager;
   final boolean isReady;
   float animProgress = 0.0F;
   float animProgress2 = 0.0F;
   String[] labelIds = new String[]{"action.names.followme", "action.names.stopfollowme", "action.names.gohome", "action.names.setnewhome", "action.names.equipment"};
   int[] scrollOffsets = new int[]{0, 0, 0, 0, 0};
   int[] buttonWidths = new int[]{64, 80, 47, 32, 96};
   int[] starCounts = new int[]{4, 4, 5, 5, 4};
   int[] maxScrollOffsets = new int[]{50, 90, 50, 80, 60};

   public GirlInventoryScreen(BaseGirlEntity girl, EntityPlayer player) {
      this.girl = girl;
      this.player = player;
      this.actionNames = new String[0];
      this.actionCosts = new ItemStack[0];
      this.isReady = true;
      this.dataManager = girl.getDataManager();
   }

   public GirlInventoryScreen(BaseGirlEntity girl, EntityPlayer player, String[] actionNames, @Nullable ItemStack[] actionCosts, boolean isReady) {
      this.girl = girl;
      this.player = player;
      this.actionNames = actionNames;
      this.actionCosts = actionCosts;
      this.isReady = isReady;
      this.dataManager = girl.getDataManager();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   /**
    * Releases the girl's interaction state ({@code girl.ac()}) when the menu
    * closes, so the girl returns to idle behavior. Must not be removed —
    * without it the girl stays locked in interaction mode.
    */
   @SideOnly(Side.CLIENT)
   public void onGuiClosed() {
      super.onGuiClosed();
      this.girl.ac();
   }

   /**
    * Button handler. For costed dynamic actions (non-creative): validates the
    * player has a stack matching the cost (item, count, metadata), removes it
    * via {@link RemoveItemsPacket} and executes; otherwise refuses with a chat
    * line and sad sound. Static buttons (id < 5) execute directly.
    */
   protected void actionPerformed(GuiButton button) {
      if (button.id >= 5 && this.actionCosts != null && this.actionCosts[button.id - 5] != null && !this.player.capabilities.isCreativeMode) {
         for (ItemStack stack : this.player.inventory.mainInventory) {
            if (stack.getItem().equals(this.actionCosts[button.id - 5].getItem())
               && stack.getCount() >= this.actionCosts[button.id - 5].getCount()
               && stack.getMetadata() == this.actionCosts[button.id - 5].getMetadata()) {
               PacketHandler.networkWrapper.sendToServer(new RemoveItemsPacket(this.player.getPersistentID(), this.actionCosts[button.id - 5]));
               this.onButtonClick(button);
               return;
            }
         }

         this.player.sendMessage(new TextComponentString("<" + this.girl.getName() + "> you cannot afford that..."));
         this.girl.playSound(SoundHandler.GIRLS_JENNY_SADOH[1]);
      } else {
         this.onButtonClick(button);
      }
   }

   /**
    * Executes the clicked action on the girl
    * ({@code girl.doAction(actionName, player.getPersistentID())}) and closes
    * the screen. This is the single funnel for every interaction button — the
    * action name must match the {@code action.names.*} translation keys the
    * server-side {@code doAction} switches on.
    */
   void onButtonClick(GuiButton button) {
      String actionName;
      if (button.id < 5) {
         actionName = this.labelIds[button.id];
      } else {
         actionName = this.actionNames[button.id - 5];
      }

      this.girl.doAction(actionName, this.player.getPersistentID());
      Minecraft.getMinecraft().player.closeScreen();
   }

   /**
    * Rebuilds the button list every frame: dynamic action buttons slide in
    * from the right (with item-cost icons + tooltips when applicable), then
    * the static interaction buttons and the girl preview animate in. Button
    * layout depends on two staged animation progress values.
    */
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.buttonList.clear();
      ScaledResolution resolution = new ScaledResolution(this.mc);
      int screenWidth = resolution.getScaledWidth();
      int screenHeight = resolution.getScaledHeight();
      this.animProgress = Math.min(1.0F, this.animProgress + this.mc.getTickLength() / 5.0F);
      if (this.animProgress == 1.0F) {
         this.animProgress2 = Math.min(1.0F, this.animProgress2 + this.mc.getTickLength() / 5.0F);
      }

      int tooltipX = (int)RotationHelper.lerp(115.0F, 161.0F, this.animProgress2);
      int itemX = (int)RotationHelper.lerp(91.0F, 137.0F, this.animProgress2);
      int buttonX = (int)RotationHelper.lerp(-30.0F, 120.0F, this.animProgress);
      byte buttonY = 70;
      byte itemY = 52;
      byte itemZ = 68;

      for (int id = 5; id < this.actionNames.length + 5; id++) {
         if (this.animProgress2 > 0.0F && this.actionCosts != null && this.actionCosts[id - 5] != null && this.actionCosts[id - 5].getCount() != 0) {
            this.zLevel = -300.0F;
            this.itemRender.zLevel = -300.0F;
            this.drawTooltip(Arrays.asList(this.actionCosts[id - 5].getCount() + "x    "), screenWidth - tooltipX, screenHeight - itemY, this.fontRenderer);
            this.itemRender.renderItemIntoGUI(this.actionCosts[id - 5], screenWidth - itemX, screenHeight - itemZ);
            this.zLevel = 0.0F;
            this.itemRender.zLevel = 0.0F;
         }

         this.buttonList.add(new GuiButton(id, screenWidth - buttonX, screenHeight - buttonY, 100, 20, I18n.format(this.actionNames[id - 5], new Object[0])));
         buttonY += 30;
         itemY += 30;
         itemZ += 30;
      }

      if (this.isReady) {
         this.drawGirlPreview(mouseX, mouseY);
      }
   }

   /**
    * Draws the girl's equipped items (weapon/bow/armor from the data manager)
    * and the five static action buttons with animated star icons; the
    * follow/equipment buttons get a per-button scroll offset driven by mouse
    * hover. Two variants exist for the with-master / without-master cases
    * (identical layout, different button id mapping).
    */
   void drawGirlPreview(int mouseX, int mouseY) {
      int itemX = (int)RotationHelper.lerp(-30.0F, 120.0F, this.animProgress);
      this.itemRender.renderItemIntoGUI((ItemStack)this.dataManager.get(AbstractGirlNpcEntity.WEAPON), itemX - 105, 68);
      this.itemRender.renderItemIntoGUI((ItemStack)this.dataManager.get(AbstractGirlNpcEntity.BOW), itemX - 105, 87);
      this.itemRender.renderItemIntoGUI((ItemStack)this.dataManager.get(AbstractGirlNpcEntity.HELMET_SLOT), itemX - 105, 109);
      this.itemRender.renderItemIntoGUI((ItemStack)this.dataManager.get(AbstractGirlNpcEntity.CHEST_SLOT), itemX - 105, 127);
      this.itemRender.renderItemIntoGUI((ItemStack)this.dataManager.get(AbstractGirlNpcEntity.LEGS_SLOT), itemX - 105, 146);
      this.itemRender.renderItemIntoGUI((ItemStack)this.dataManager.get(AbstractGirlNpcEntity.BOOTS_SLOT), itemX - 105, 166);
      if (this.animProgress2 != 0.0F) {
         if (!((String)this.dataManager.get(BaseGirlEntity.MASTER)).equals("")) {
            byte xStart = 35;
            byte y = 70;

            for (int id = 0; id < 5; id++) {
               if (id == 0) {
                  id = 1;
               } else if (id == 1) {
               }

               if (mouseX >= xStart && mouseX <= 58 + this.scrollOffsets[id] && mouseY >= y && mouseY <= y + 20) {
                  this.scrollOffsets[id] = Math.min(this.maxScrollOffsets[id], this.scrollOffsets[id] + 7);
               } else {
                  this.scrollOffsets[id] = Math.max(0, this.scrollOffsets[id] - 7);
               }

               StringBuilder label = new StringBuilder(I18n.format(this.labelIds[id], new Object[0]));

               for (int star = 0; star < this.starCounts[id]; star++) {
                  label.append(" ");
               }

               this.mc.renderEngine.bindTexture(GUI_TEXTURE);
               this.drawTexturedModalRect(this.scrollOffsets[id] + xStart - 18 + (int)RotationHelper.lerp(0.0F, 23.0F, this.animProgress2), y + 2, this.buttonWidths[id], 0, 16, 16);
               this.buttonList.add(new GuiButton(id, 36, y, 100, 20, label.toString()));
               y += 30;
            }

            this.mc.renderEngine.bindTexture(GUI_TEXTURE);
            this.drawTexturedModalRect(itemX - 113, 60, 0, 0, 32, 130);
         } else {
            byte xStart2 = 35;
            byte y2 = 70;

            for (int id2 = 0; id2 < 5; id2++) {
               if (id2 == 0) {
               }

               if (id2 == 1) {
                  id2 = 2;
               }

               if (mouseX >= xStart2 && mouseX <= 58 + this.scrollOffsets[id2] && mouseY >= y2 && mouseY <= y2 + 20) {
                  this.scrollOffsets[id2] = Math.min(this.maxScrollOffsets[id2], this.scrollOffsets[id2] + 7);
               } else {
                  this.scrollOffsets[id2] = Math.max(0, this.scrollOffsets[id2] - 7);
               }

               StringBuilder label2 = new StringBuilder(I18n.format(this.labelIds[id2], new Object[0]));

               for (int star2 = 0; star2 < this.starCounts[id2]; star2++) {
                  label2.append(" ");
               }

               this.mc.renderEngine.bindTexture(GUI_TEXTURE);
               this.drawTexturedModalRect(this.scrollOffsets[id2] + xStart2 - 18 + (int)RotationHelper.lerp(0.0F, 23.0F, this.animProgress2), y2 + 2, this.buttonWidths[id2], 0, 16, 16);
               this.buttonList.add(new GuiButton(id2, 36, y2, 100, 20, label2.toString()));
               y2 += 30;
            }

            this.mc.renderEngine.bindTexture(GUI_TEXTURE);
            this.drawTexturedModalRect(itemX - 113, 60, 0, 0, 32, 130);
         }
      }
   }

   /**
    * Hand-rolled tooltip renderer (dark border + gradient fill) drawn outside
    * the vanilla tooltip system so cost hints can appear near the item icon.
    */
   void drawTooltip(List<String> lines, int x, int y, FontRenderer font) {
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
      int maxWidth = 0;

      for (String line : lines) {
         int width = this.fontRenderer.getStringWidth(line);
         if (width > maxWidth) {
            maxWidth = width;
         }
      }

      int tooltipX = x + 12;
      int tooltipY = y - 12;
      int tooltipHeight = 8;
      if (lines.size() > 1) {
         tooltipHeight += 2 + (lines.size() - 1) * 10;
      }

      if (tooltipX + maxWidth > this.width) {
         tooltipX -= 28 + maxWidth;
      }

      if (tooltipY + tooltipHeight + 6 > this.height) {
         tooltipY = this.height - tooltipHeight - 6;
      }

      this.drawGradientRect(tooltipX - 3, tooltipY - 4, tooltipX + maxWidth + 3, tooltipY - 3, -267386864, -267386864);
      this.drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 4, -267386864, -267386864);
      this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3, -267386864, -267386864);
      this.drawGradientRect(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, -267386864, -267386864);
      this.drawGradientRect(tooltipX + maxWidth + 3, tooltipY - 3, tooltipX + maxWidth + 4, tooltipY + tooltipHeight + 3, -267386864, -267386864);
      this.drawGradientRect(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, 1347420415, 1344798847);
      this.drawGradientRect(tooltipX + maxWidth + 2, tooltipY - 3 + 1, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3 - 1, 1347420415, 1344798847);
      this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + maxWidth + 3, tooltipY - 3 + 1, 1347420415, 1347420415);
      this.drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3, 1344798847, 1344798847);

      for (int i = 0; i < lines.size(); i++) {
         String text = (String)lines.get(i);
         this.fontRenderer.drawStringWithShadow(text, tooltipX, tooltipY, -1);
         if (i == 0) {
            tooltipY += 2;
         }

         tooltipY += 10;
      }

      GlStateManager.enableLighting();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enableRescaleNormal();
   }

}
