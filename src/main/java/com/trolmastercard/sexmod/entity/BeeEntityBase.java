package com.trolmastercard.sexmod.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

/**
 * <b>Role.</b> Base class for the bee girls ({@link BeeEntity} NPC and its
 * variants): a girl that doubles as a 27-slot chest inventory ({@link IInventory}
 * delegating to {@link #inventory}) and carries a synced "horny" flag
 * ({@link #HORNY_FLAG}, data-manager id 111 — used as the tamed/chest flag).
 * <p>
 * <b>Pitfalls.</b> The id 111 key lives in this class but is re-declared in
 * {@link BeeEntity} with id 112 — the subclass key wins at runtime; do not
 * "deduplicate" without checking every register/set call site.
 */
public abstract class BeeEntityBase extends BaseGirlEntity implements IInventory {
   public static final DataParameter<Boolean> HORNY_FLAG = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(111);
   public ItemStackHandler inventory = new ItemStackHandler(27);

   protected BeeEntityBase(World world) {
      super(world);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(HORNY_FLAG, false);
   }

   public int getSizeInventory() {
      return 27;
   }

   public boolean isEmpty() {
      return false;
   }

   public ItemStack getStackInSlot(int slot) {
      return slot >= this.inventory.getSlots() ? ItemStack.EMPTY : this.inventory.getStackInSlot(slot);
   }

   public ItemStack decrStackSize(int slot, int amount) {
      return this.inventory.extractItem(slot, amount, false);
   }

   public ItemStack removeStackFromSlot(int slot) {
      return this.inventory.extractItem(slot, this.inventory.getStackInSlot(slot).getCount(), false);
   }

   public void setInventorySlotContents(int slot, ItemStack stack) {
      this.inventory.setStackInSlot(slot, stack);
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer player) {
      return true;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int slot, ItemStack stack) {
      return true;
   }

   public int getField(int id) {
      return id;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
   }

}
