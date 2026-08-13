package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UploadInventoryToServerPacket;







import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GirlInventoryContainerGui extends GuiContainer {
   static final ResourceLocation b = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   UUID c;
   LunaEntity d;
   UUID a;

   public GirlInventoryContainerGui(LunaEntity var1, InventoryPlayer var2, UUID var3) {
      super(new GirlInventoryContainer2(var1, var2, var3));
      this.c = var3;
      this.d = var1;
      this.a = var2.player.getPersistentID();
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      super.drawScreen(var1, var2, var3);
      this.renderHoveredToolTip(var1, var2);
   }

   public void onGuiClosed() {
      super.onGuiClosed();

      for (GirlInventoryContainer2 var2 : GirlInventoryContainer2.c) {
         if (var2.a.equals(this.c)) {
            ItemStack[] var3 = new ItemStack[43];
            Minecraft.getMinecraft().player.inventory.mainInventory.toArray(var3);
            var3[36] = var2.getSlot(0).getStack();
            var3[37] = var2.getSlot(1).getStack();
            var3[38] = var2.getSlot(2).getStack();
            var3[39] = var2.getSlot(3).getStack();
            var3[40] = var2.getSlot(4).getStack();
            var3[41] = var2.getSlot(5).getStack();
            var3[42] = var2.getSlot(6).getStack();
            PacketHandler.b.sendToServer(new UploadInventoryToServerPacket(this.d.getGirlId(), this.a, var3));
         }
      }
   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.renderEngine.bindTexture(b);
      this.drawTexturedModalRect(this.width / 2 - 88, this.height / 2 - 7 - 24, 80, 142, 176, 114);
   }
}
