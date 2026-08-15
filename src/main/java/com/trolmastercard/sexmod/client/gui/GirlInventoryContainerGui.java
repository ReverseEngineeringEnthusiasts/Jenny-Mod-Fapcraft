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

/**
 * GUI screen for Luna's 7-slot equipment chest ({@link GirlInventoryContainer2}
 * with the rod slot). Draws the mod's {@code girlinventory.png} texture.
 * <p>
 * <b>Save flow.</b> On close, finds the matching container in
 * {@link GirlInventoryContainer2#containers} and sends a 43-slot snapshot
 * (player main inventory 0..35, Luna's 7 slots 36..42) to the server as
 * {@link UploadInventoryToServerPacket}. CLIENT-side only.
 */
public class GirlInventoryContainerGui extends GuiContainer {
   static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   UUID girlUUID;
   LunaEntity lunaEntity;
   UUID playerUUID;

   public GirlInventoryContainerGui(LunaEntity var1, InventoryPlayer var2, UUID var3) {
      super(new GirlInventoryContainer2(var1, var2, var3));
      this.girlUUID = var3;
      this.lunaEntity = var1;
      this.playerUUID = var2.player.getPersistentID();
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      super.drawScreen(var1, var2, var3);
      this.renderHoveredToolTip(var1, var2);
   }

   /**
    * Serializes Luna's 7 equipment slots + the player's main inventory into a
    * 43-element array and uploads it via {@link UploadInventoryToServerPacket}.
    * Fires exactly once per container lifetime (on GUI close).
    */
   public void onGuiClosed() {
      super.onGuiClosed();

      for (GirlInventoryContainer2 var2 : GirlInventoryContainer2.containers) {
         if (var2.girlUUID.equals(this.girlUUID)) {
            ItemStack[] var3 = new ItemStack[43];
            Minecraft.getMinecraft().player.inventory.mainInventory.toArray(var3);
            var3[36] = var2.getSlot(0).getStack();
            var3[37] = var2.getSlot(1).getStack();
            var3[38] = var2.getSlot(2).getStack();
            var3[39] = var2.getSlot(3).getStack();
            var3[40] = var2.getSlot(4).getStack();
            var3[41] = var2.getSlot(5).getStack();
            var3[42] = var2.getSlot(6).getStack();
            PacketHandler.networkWrapper.sendToServer(new UploadInventoryToServerPacket(this.lunaEntity.getGirlId(), this.playerUUID, var3));
         }
      }
   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      this.drawTexturedModalRect(this.width / 2 - 88, this.height / 2 - 7 - 24, 80, 142, 176, 114);
   }
}
