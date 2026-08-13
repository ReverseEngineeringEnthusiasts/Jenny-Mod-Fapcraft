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

public class ChestContainerGui extends GuiContainer {
   private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
   private final IInventory playerInventory;
   private final IInventory chestInventory;
   private final int rowCount;
   UUID containerUUID;
   BaseGirlEntity girl;
   UUID girlUUID;

   public ChestContainerGui(EntityPlayer var1, BaseGirlEntity var2, UUID var3) {
      super(new GirlInventoryContainer(var1.inventory, (IInventory)var2, var1, var3));
      this.containerUUID = var3;
      this.girl = var2;
      this.girlUUID = var1.getPersistentID();
      this.playerInventory = var1.inventory;
      this.chestInventory = (IInventory)var2;
      this.allowUserInput = false;
      this.rowCount = ((IInventory)var2).getSizeInventory() / 9;
      this.ySize = 114 + this.rowCount * 18;
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      super.drawScreen(var1, var2, var3);
      this.renderHoveredToolTip(var1, var2);
   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
      this.fontRenderer.drawString(this.girl.getDisplayNameText(), 8, 6, 4210752);
      this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(CONTAINER_TEXTURE);
      int var4 = (this.width - this.xSize) / 2;
      int var5 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var4, var5, 0, 0, this.xSize, this.rowCount * 18 + 17);
      this.drawTexturedModalRect(var4, var5 + this.rowCount * 18 + 17, 0, 126, this.xSize, 96);
   }

   public void onGuiClosed() {
      super.onGuiClosed();

      for (ChestContainer var2 : ChestContainer.containers) {
         if (var2.girlUUID.equals(this.containerUUID)) {
            ItemStack[] var3 = new ItemStack[63];
            Minecraft.getMinecraft().player.inventory.mainInventory.toArray(var3);

            for (int var4 = 0; var4 < 27; var4++) {
               var3[var4 + 36] = var2.getSlot(var4).getStack();
            }

            PacketHandler.networkWrapper.sendToServer(new UploadInventoryToServerPacket(this.girl.getGirlId(), this.girlUUID, var3));
         }
      }
   }

}
