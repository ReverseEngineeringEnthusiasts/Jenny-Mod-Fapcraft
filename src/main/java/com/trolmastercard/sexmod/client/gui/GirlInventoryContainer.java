package com.trolmastercard.sexmod.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class GirlInventoryContainer extends Container {
   private final IInventory inventory;
   private final int slotIndex;
   public static List<GirlInventoryContainer> containers = new ArrayList<>();
   public UUID girlUUID;

   public GirlInventoryContainer(IInventory var1, IInventory var2, EntityPlayer var3, UUID var4) {
      this.girlUUID = var4;
      containers.add(this);
      this.inventory = var2;
      var2.openInventory(var3);
      this.slotIndex = 3;
      byte var5 = -18;

      for (int var6 = 0; var6 < 3; var6++) {
         for (int var7 = 0; var7 < 9; var7++) {
            this.addSlotToContainer(new Slot(var2, var7 + var6 * 9, 8 + var7 * 18, 18 + var6 * 18));
         }
      }

      for (int var8 = 0; var8 < 3; var8++) {
         for (int var10 = 0; var10 < 9; var10++) {
            this.addSlotToContainer(new Slot(var1, var10 + var8 * 9 + 9, 8 + var10 * 18, 103 + var8 * 18 + var5));
         }
      }

      for (int var9 = 0; var9 < 9; var9++) {
         this.addSlotToContainer(new Slot(var1, var9, 8 + var9 * 18, 143));
      }
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.inventory.isUsableByPlayer(var1);
   }

   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = ItemStack.EMPTY;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 < this.slotIndex * 9) {
            if (!this.mergeItemStack(var5, this.slotIndex * 9, this.inventorySlots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(var5, 0, this.slotIndex * 9, false)) {
            return ItemStack.EMPTY;
         }

         if (var5.isEmpty()) {
            var4.putStack(ItemStack.EMPTY);
         } else {
            var4.onSlotChanged();
         }
      }

      return var3;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
      this.inventory.closeInventory(var1);
   }

   public IInventory getInventoryRef() {
      return this.inventory;
   }

}
