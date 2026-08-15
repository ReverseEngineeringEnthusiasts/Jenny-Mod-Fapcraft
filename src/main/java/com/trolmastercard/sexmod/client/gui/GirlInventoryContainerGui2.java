package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
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
 * GUI for a generic girl's 6-slot equipment chest ({@link ChestContainer}:
 * weapon, bow, helmet, chestplate, pants, shoes), used by the non-Luna girls.
 * <p>
 * <b>Save flow.</b> On close, finds the matching {@link ChestContainer} in
 * {@link ChestContainer#containers} and sends a 42-slot snapshot (player main
 * inventory 0..35, the 6 girl slots 36..41) to the server via
 * {@link UploadInventoryToServerPacket}. CLIENT-side only.
 */
public class GirlInventoryContainerGui2 extends GuiContainer {
   static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   UUID playerUUID;
   BaseGirlEntity girlEntity;
   UUID girlUUID;

   public GirlInventoryContainerGui2(BaseGirlEntity girl, InventoryPlayer playerInventory, UUID uuid) {
      super(new ChestContainer(girl, playerInventory, uuid));
      this.playerUUID = uuid;
      this.girlEntity = girl;
      this.girlUUID = playerInventory.player.getPersistentID();
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.renderHoveredToolTip(mouseX, mouseY);
   }

   /**
    * Serializes the 6 girl equipment slots + the player's main inventory into
    * a 42-element array and uploads it via {@link UploadInventoryToServerPacket}.
    * Fires exactly once per container lifetime (on GUI close).
    */
   public void onGuiClosed() {
      super.onGuiClosed();

      for (ChestContainer container : ChestContainer.containers) {
         if (container.girlUUID.equals(this.playerUUID)) {
            ItemStack[] stacks = new ItemStack[42];
            Minecraft.getMinecraft().player.inventory.mainInventory.toArray(stacks);
            stacks[36] = container.getSlot(0).getStack();
            stacks[37] = container.getSlot(1).getStack();
            stacks[38] = container.getSlot(2).getStack();
            stacks[39] = container.getSlot(3).getStack();
            stacks[40] = container.getSlot(4).getStack();
            stacks[41] = container.getSlot(5).getStack();
            PacketHandler.networkWrapper.sendToServer(new UploadInventoryToServerPacket(this.girlEntity.getGirlId(), this.girlUUID, stacks));
         }
      }
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      this.drawTexturedModalRect(this.width / 2 - 88, this.height / 2 - 7 - 24, 33, 16, 176, 114);
   }
}
