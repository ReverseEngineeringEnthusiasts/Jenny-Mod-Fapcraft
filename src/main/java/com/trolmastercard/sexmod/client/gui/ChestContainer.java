package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
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
 * Container wiring the girl's equipment inventory (weapon, bow, helmet,
 * chestplate, pants, shoes — {@link GirlInventorySlot}s bound to the girl's
 * forge {@code ITEM_HANDLER_CAPABILITY} at slot ids 0..5) together with the
 * player's own inventory. Every open instance registers itself in the static
 * {@link #containers} list, which {@link ChestContainerGui#onGuiClosed} uses to
 * snapshot the contents back to the server on close.
 * <p>
 * Rendered on the CLIENT side; the slot transfer logic in
 * {@link #transferStackInSlot} runs on both sides (vanilla container sync).
 */
public class ChestContainer extends Container {
   BaseGirlEntity girl;
   public Slot[] d;
   public UUID girlUUID;
   public static List<ChestContainer> containers = new ArrayList<>();

   public ChestContainer(BaseGirlEntity girl, InventoryPlayer playerInventory, UUID uuid) {
      this.girlUUID = uuid;
      containers.add(this);
      if (girl.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH)) {
         IItemHandler itemHandler = (IItemHandler)girl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
         this.girl = girl;
         this.d = new Slot[]{
            new GirlInventorySlot(GirlInventorySlot.b.WEAPON, itemHandler, GirlInventorySlot.b.WEAPON.id, 31, 60),
            new GirlInventorySlot(GirlInventorySlot.b.BOW, itemHandler, GirlInventorySlot.b.BOW.id, 50, 60),
            new GirlInventorySlot(GirlInventorySlot.b.HELMET, itemHandler, GirlInventorySlot.b.HELMET.id, 72, 60),
            new GirlInventorySlot(GirlInventorySlot.b.CHEST_PLATE, itemHandler, GirlInventorySlot.b.CHEST_PLATE.id, 91, 60),
            new GirlInventorySlot(GirlInventorySlot.b.PANTS, itemHandler, GirlInventorySlot.b.PANTS.id, 110, 60),
            new GirlInventorySlot(GirlInventorySlot.b.SHOES, itemHandler, GirlInventorySlot.b.SHOES.id, 129, 60)
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

         for (Slot slot : this.d) {
            this.addSlotToContainer(slot);
         }

         for (Slot slot2 : (java.util.Collection<Slot>) (slots) ) {
            this.addSlotToContainer(slot2);
         }
      }
   }

   /**
    * Standard shift-click transfer between the girl's equipment slots (first
    * {@code d.length} slots) and the player's inventory. Merges girl->player
    * backwards and player->girl forwards; returns the copied stack or
    * {@code EMPTY} when nothing moved.
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
