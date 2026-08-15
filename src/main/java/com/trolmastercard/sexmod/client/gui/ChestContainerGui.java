package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadInventoryToServerPacket;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Screen for the girl's equipment chest ({@link GirlInventoryContainer}, the
 * 27-slot girl inventory + vanilla player inventory), sized by the girl's row
 * count. Draws the generic-54 chest texture with a dynamic height and shows the
 * girl's name as title.
 * <p>
 * <b>Save flow.</b> On close it scans {@link ChestContainer#containers} for the
 * open container matching {@code containerUUID}, builds a 63-slot snapshot
 * (player main inventory 0..35, girl slots 36..62) and sends
 * {@link UploadInventoryToServerPacket} — the girl's inventory is thus synced
 * only when the GUI closes. CLIENT-side only.
 */
public class ChestContainerGui extends GuiContainer {
   private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
   private final IInventory playerInventory;
   private final IInventory chestInventory;
   private final int rowCount;
   UUID containerUUID;
   BaseGirlEntity girl;
   UUID girlUUID;

   public ChestContainerGui(EntityPlayer player, BaseGirlEntity girl, UUID uuid) {
      super(new GirlInventoryContainer(player.inventory, (IInventory)girl, player, uuid));
      this.containerUUID = uuid;
      this.girl = girl;
      this.girlUUID = player.getPersistentID();
      this.playerInventory = player.inventory;
      this.chestInventory = (IInventory)girl;
      this.allowUserInput = false;
      this.rowCount = ((IInventory)girl).getSizeInventory() / 9;
      this.ySize = 114 + this.rowCount * 18;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.renderHoveredToolTip(mouseX, mouseY);
   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.fontRenderer.drawString(this.girl.getDisplayNameText(), 8, 6, 4210752);
      this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(CONTAINER_TEXTURE);
      int left = (this.width - this.xSize) / 2;
      int top = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.rowCount * 18 + 17);
      this.drawTexturedModalRect(left, top + this.rowCount * 18 + 17, 0, 126, this.xSize, 96);
   }

   /**
    * Serializes the current container contents (player inventory + girl's 27
    * equipment slots, index 36+) into a 63-element stack array and sends it to
    * the server as {@link UploadInventoryToServerPacket}. Must only run once
    * per container lifetime — closing the screen is what persists the girl's
    * gear.
    */
   public void onGuiClosed() {
      super.onGuiClosed();

      for (ChestContainer container : ChestContainer.containers) {
         if (container.girlUUID.equals(this.containerUUID)) {
            ItemStack[] stacks = new ItemStack[63];
            Minecraft.getMinecraft().player.inventory.mainInventory.toArray(stacks);

            for (int i = 0; i < 27; i++) {
               stacks[i + 36] = container.getSlot(i).getStack();
            }

            PacketHandler.networkWrapper.sendToServer(new UploadInventoryToServerPacket(this.girl.getGirlId(), this.girlUUID, stacks));
         }
      }
   }

}
