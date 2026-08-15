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

   public ChestContainer(BaseGirlEntity var1, InventoryPlayer var2, UUID var3) {
      this.girlUUID = var3;
      containers.add(this);
      if (var1.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH)) {
         IItemHandler var4 = (IItemHandler)var1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
         this.girl = var1;
         this.d = new Slot[]{
            new GirlInventorySlot(GirlInventorySlot.b.WEAPON, var4, GirlInventorySlot.b.WEAPON.id, 31, 60),
            new GirlInventorySlot(GirlInventorySlot.b.BOW, var4, GirlInventorySlot.b.BOW.id, 50, 60),
            new GirlInventorySlot(GirlInventorySlot.b.HELMET, var4, GirlInventorySlot.b.HELMET.id, 72, 60),
            new GirlInventorySlot(GirlInventorySlot.b.CHEST_PLATE, var4, GirlInventorySlot.b.CHEST_PLATE.id, 91, 60),
            new GirlInventorySlot(GirlInventorySlot.b.PANTS, var4, GirlInventorySlot.b.PANTS.id, 110, 60),
            new GirlInventorySlot(GirlInventorySlot.b.SHOES, var4, GirlInventorySlot.b.SHOES.id, 129, 60)
         };
         ArrayList var5 = new ArrayList();

         for (int var6 = 0; var6 < 3; var6++) {
            for (int var7 = 0; var7 < 9; var7++) {
               var5.add(new Slot(var2, var7 + var6 * 9 + 9, 8 + var7 * 18, 84 + var6 * 18));
            }
         }

         for (int var10 = 0; var10 < 9; var10++) {
            var5.add(new Slot(var2, var10, 8 + var10 * 18, 142));
         }

         for (Slot var9 : this.d) {
            this.addSlotToContainer(var9);
         }

         for (Slot var14 : (java.util.Collection<Slot>) (var5) ) {
            this.addSlotToContainer(var14);
         }
      }
   }

   /**
    * Standard shift-click transfer between the girl's equipment slots (first
    * {@code d.length} slots) and the player's inventory. Merges girl->player
    * backwards and player->girl forwards; returns the copied stack or
    * {@code EMPTY} when nothing moved.
    */
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = ItemStack.EMPTY;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         int var6 = this.inventorySlots.size() - var1.inventory.mainInventory.size();
         if (var2 < var6) {
            if (!this.mergeItemStack(var5, var6, this.inventorySlots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(var5, 0, var6, false)) {
            return ItemStack.EMPTY;
         }

         if (var5.getCount() == 0) {
            var4.putStack(ItemStack.EMPTY);
         } else {
            var4.onSlotChanged();
         }

         var4.onTake(var1, var5);
      }

      return var3;
   }

   public void putStackInSlot(int var1, ItemStack var2) {
      super.putStackInSlot(var1, var2);
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return true;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
   }

}
