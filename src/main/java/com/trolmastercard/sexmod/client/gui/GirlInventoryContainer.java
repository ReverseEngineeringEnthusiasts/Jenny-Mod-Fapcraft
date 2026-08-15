package com.trolmastercard.sexmod.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Vanilla 3x9 girl chest container: 27 girl inventory slots on top, the
 * player's 27+9 inventory below. Used by {@link GirlInventoryContainerGui} for
 * Luna's chest. Instances self-register in the static {@link #containers} list
 * so the GUI can locate the right container when saving on close.
 * <p>
 * Runs on both sides (vanilla container sync); shift-click moves items between
 * the girl's 27 slots and the player inventory.
 */
public class GirlInventoryContainer extends Container {
   private final IInventory inventory;
   private final int slotIndex;
   public static List<GirlInventoryContainer> containers = new ArrayList<>();
   public UUID girlUUID;

   public GirlInventoryContainer(IInventory playerInventory, IInventory girlInventory, EntityPlayer player, UUID uuid) {
      this.girlUUID = uuid;
      containers.add(this);
      this.inventory = girlInventory;
      girlInventory.openInventory(player);
      this.slotIndex = 3;
      byte yOffset = -18;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(girlInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
         }
      }

      for (int row2 = 0; row2 < 3; row2++) {
         for (int col2 = 0; col2 < 9; col2++) {
            this.addSlotToContainer(new Slot(playerInventory, col2 + row2 * 9 + 9, 8 + col2 * 18, 103 + row2 * 18 + yOffset));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 143));
      }
   }

   public boolean canInteractWith(EntityPlayer player) {
      return this.inventory.isUsableByPlayer(player);
   }

   /**
    * Standard shift-click between the girl's 27 slots (indices 0..26) and the
    * player's inventory; backwards-merge into the player side, forward-merge
    * into the girl side.
    */
   public ItemStack transferStackInSlot(EntityPlayer player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = (Slot)this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack stack = slot.getStack();
         copy = stack.copy();
         if (index < this.slotIndex * 9) {
            if (!this.mergeItemStack(stack, this.slotIndex * 9, this.inventorySlots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(stack, 0, this.slotIndex * 9, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }
      }

      return copy;
   }

   public void onContainerClosed(EntityPlayer player) {
      super.onContainerClosed(player);
      this.inventory.closeInventory(player);
   }

   public IInventory getInventoryRef() {
      return this.inventory;
   }

}
