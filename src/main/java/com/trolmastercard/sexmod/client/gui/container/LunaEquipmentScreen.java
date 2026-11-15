package com.trolmastercard.sexmod.client.gui.container;

import com.trolmastercard.sexmod.common.inventory.LunaEquipmentContainer;

import com.trolmastercard.sexmod.common.entity.girl.LunaEntity;
import com.trolmastercard.sexmod.network.PacketHandler;
import com.trolmastercard.sexmod.network.girl.UploadGirlInventoryPacket;
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
 * <b>Save flow.</b> On close, snapshots this screen's own
 * {@link GirlInventoryContainer2} (player main inventory 0..35, Luna's 7 slots
 * 36..42) and sends it to the server as {@link UploadInventoryToServerPacket}.
 * CLIENT-side only.
 */
public class LunaEquipmentScreen extends GuiContainer {
   static final ResourceLocation GUI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/girlinventory.png");
   UUID girlUUID;
   LunaEntity lunaEntity;
   UUID playerUUID;

   public LunaEquipmentScreen(LunaEntity luna, InventoryPlayer playerInventory, UUID uuid) {
      super(new LunaEquipmentContainer(luna, playerInventory, uuid));
      this.girlUUID = uuid;
      this.lunaEntity = luna;
      this.playerUUID = playerInventory.player.getPersistentID();
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.renderHoveredToolTip(mouseX, mouseY);
   }

   /**
    * Serializes Luna's 7 equipment slots + the player's main inventory into a
    * 43-element array and uploads it via {@link UploadInventoryToServerPacket}.
    * Reads this screen's own container only (never the static list), so a close
    * uploads exactly once.
    */
   public void onGuiClosed() {
      super.onGuiClosed();
      LunaEquipmentContainer container = (LunaEquipmentContainer)this.inventorySlots;
      ItemStack[] stacks = new ItemStack[43];
      Minecraft.getMinecraft().player.inventory.mainInventory.toArray(stacks);
      stacks[36] = container.getSlot(0).getStack();
      stacks[37] = container.getSlot(1).getStack();
      stacks[38] = container.getSlot(2).getStack();
      stacks[39] = container.getSlot(3).getStack();
      stacks[40] = container.getSlot(4).getStack();
      stacks[41] = container.getSlot(5).getStack();
      stacks[42] = container.getSlot(6).getStack();
      PacketHandler.networkWrapper.sendToServer(new UploadGirlInventoryPacket(this.lunaEntity.getGirlId(), this.playerUUID, stacks));
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.renderEngine.bindTexture(GUI_TEXTURE);
      this.drawTexturedModalRect(this.width / 2 - 88, this.height / 2 - 7 - 24, 80, 142, 176, 114);
   }
}
