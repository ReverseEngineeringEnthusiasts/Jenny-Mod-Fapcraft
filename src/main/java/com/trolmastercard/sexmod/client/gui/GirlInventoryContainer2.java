package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.LunaEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Luna's equipment container ({@link GirlInventoryContainer2} variant with an
 * extra fishing-rod slot): weapon/bow/armor slots 0..5 plus rod slot 6, and
 * the player inventory below. Open instances register in the static
 * {@link #containers} list, consumed by
 * {@link GirlInventoryContainerGui#onGuiClosed} on close.
 * <p>
 * Runs on both sides.
 */
public class GirlInventoryContainer2 extends Container {
   LunaEntity lunaEntity;
   public Slot[] b;
   public UUID girlUUID;
   public static List<GirlInventoryContainer2> containers = new ArrayList<>();

   public GirlInventoryContainer2(LunaEntity luna, InventoryPlayer playerInventory, UUID uuid) {
      this.girlUUID = uuid;
      containers.add(this);
      if (luna.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH)) {
         IItemHandler itemHandler = (IItemHandler) luna.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
         this.lunaEntity = luna;
         this.b = new Slot[]{
            new GirlInventorySlot(GirlInventorySlot.b.WEAPON, itemHandler, GirlInventorySlot.b.WEAPON.id, 41, 60),
            new GirlInventorySlot(GirlInventorySlot.b.BOW, itemHandler, GirlInventorySlot.b.BOW.id, 59, 60),
            new GirlInventorySlot(GirlInventorySlot.b.HELMET, itemHandler, GirlInventorySlot.b.HELMET.id, 81, 60),
            new GirlInventorySlot(GirlInventorySlot.b.CHEST_PLATE, itemHandler, GirlInventorySlot.b.CHEST_PLATE.id, 100, 60),
            new GirlInventorySlot(GirlInventorySlot.b.PANTS, itemHandler, GirlInventorySlot.b.PANTS.id, 119, 60),
            new GirlInventorySlot(GirlInventorySlot.b.SHOES, itemHandler, GirlInventorySlot.b.SHOES.id, 138, 60),
            new GirlInventorySlot(GirlInventorySlot.b.ROD, itemHandler, GirlInventorySlot.b.ROD.id, 22, 60)
         };
         ArrayList slots = new ArrayList();

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               slots.add(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
         }

         for (int i = 0; i < 9; i++) {
            slots.add(new Slot(playerInventory, i, 8 + i * 18, 142));
         }

         for (Slot slot : this.b) {
            this.addSlotToContainer(slot);
         }

         for (Slot slot2 : (java.util.Collection<Slot>) (slots) ) {
            this.addSlotToContainer(slot2);
         }
      }
   }

   /**
    * Standard shift-click between the 7 girl slots and the player inventory
    * (see {@link ChestContainer#transferStackInSlot} for the merge pattern).
    */
   public ItemStack transferStackInSlot(EntityPlayer player, int index) {
      ItemStack copy = ItemStack.EMPTY;
      Slot slot = (Slot)this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack stack = slot.getStack();
         copy = stack.copy();
         int girlSlots = this.inventorySlots.size() - player.inventory.mainInventory.size();
         if (index < girlSlots) {
            if (!this.mergeItemStack(stack, girlSlots, this.inventorySlots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(stack, 0, girlSlots, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.getCount() == 0) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }

         slot.onTake(player, stack);
      }

      return copy;
   }

   public void putStackInSlot(int index, ItemStack stack) {
      super.putStackInSlot(index, stack);
   }

   public boolean canInteractWith(EntityPlayer player) {
      return true;
   }

   public void onContainerClosed(EntityPlayer player) {
      super.onContainerClosed(player);
   }

}
