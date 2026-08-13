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
   private static final ResourceLocation f = new ResourceLocation("textures/gui/container/generic_54.png");
   private final IInventory e;
   private final IInventory d;
   private final int g;
   UUID c;
   BaseGirlEntity b;
   UUID a;

   public ChestContainerGui(EntityPlayer var1, BaseGirlEntity var2, UUID var3) {
      super(new GirlInventoryContainer(var1.inventory, (IInventory)var2, var1, var3));
      this.c = var3;
      this.b = var2;
      this.a = var1.getPersistentID();
      this.e = var1.inventory;
      this.d = (IInventory)var2;
      this.allowUserInput = false;
      this.g = ((IInventory)var2).getSizeInventory() / 9;
      this.ySize = 114 + this.g * 18;
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      super.drawScreen(var1, var2, var3);
      this.renderHoveredToolTip(var1, var2);
   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
      this.fontRenderer.drawString(this.b.getDisplayNameText(), 8, 6, 4210752);
      this.fontRenderer.drawString(this.e.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(f);
      int var4 = (this.width - this.xSize) / 2;
      int var5 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var4, var5, 0, 0, this.xSize, this.g * 18 + 17);
      this.drawTexturedModalRect(var4, var5 + this.g * 18 + 17, 0, 126, this.xSize, 96);
   }

   public void onGuiClosed() {
      super.onGuiClosed();

      for (ChestContainer var2 : ChestContainer.c) {
         if (var2.a.equals(this.c)) {
            ItemStack[] var3 = new ItemStack[63];
            Minecraft.getMinecraft().player.inventory.mainInventory.toArray(var3);

            for (int var4 = 0; var4 < 27; var4++) {
               var3[var4 + 36] = var2.getSlot(var4).getStack();
            }

            PacketHandler.b.sendToServer(new UploadInventoryToServerPacket(this.b.getGirlId(), this.a, var3));
         }
      }
   }

}
