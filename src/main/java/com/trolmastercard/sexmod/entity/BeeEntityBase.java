package com.trolmastercard.sexmod.entity;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public abstract class BeeEntityBase extends BaseGirlEntity implements IInventory {
   public static final DataParameter<Boolean> K = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(111);
   public ItemStackHandler L = new ItemStackHandler(27);

   protected BeeEntityBase(World var1) {
      super(var1);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(K, false);
   }

   public int getSizeInventory() {
      return 27;
   }

   public boolean isEmpty() {
      return false;
   }

   public ItemStack getStackInSlot(int var1) {
      return var1 >= this.L.getSlots() ? ItemStack.EMPTY : this.L.getStackInSlot(var1);
   }

   public ItemStack decrStackSize(int var1, int var2) {
      return this.L.extractItem(var1, var2, false);
   }

   public ItemStack removeStackFromSlot(int var1) {
      return this.L.extractItem(var1, this.L.getStackInSlot(var1).getCount(), false);
   }

   public void setInventorySlotContents(int var1, ItemStack var2) {
      this.L.setStackInSlot(var1, var2);
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return true;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public int getField(int var1) {
      return var1;
   }

   public void setField(int var1, int var2) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
   }

}
