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
   public static final DataParameter<Boolean> K = EntityDataManager.func_187226_a(BaseGirlEntity.class, DataSerializers.field_187198_h)
      .func_187156_b()
      .func_187161_a(111);
   public ItemStackHandler L = new ItemStackHandler(27);

   protected BeeEntityBase(World var1) {
      super(var1);
   }

   @Override
   protected void func_70088_a() {
      super.func_70088_a();
      this.m.func_187214_a(K, false);
   }

   public int func_70302_i_() {
      return 27;
   }

   public boolean func_191420_l() {
      return false;
   }

   public ItemStack func_70301_a(int var1) {
      return var1 >= this.L.getSlots() ? ItemStack.field_190927_a : this.L.getStackInSlot(var1);
   }

   public ItemStack func_70298_a(int var1, int var2) {
      return this.L.extractItem(var1, var2, false);
   }

   public ItemStack func_70304_b(int var1) {
      return this.L.extractItem(var1, this.L.getStackInSlot(var1).func_190916_E(), false);
   }

   public void func_70299_a(int var1, ItemStack var2) {
      this.L.setStackInSlot(var1, var2);
   }

   public int func_70297_j_() {
      return 64;
   }

   public void func_70296_d() {
   }

   public boolean func_70300_a(EntityPlayer var1) {
      return true;
   }

   public void func_174889_b(EntityPlayer var1) {
   }

   public void func_174886_c(EntityPlayer var1) {
   }

   public boolean func_94041_b(int var1, ItemStack var2) {
      return true;
   }

   public int func_174887_a_(int var1) {
      return var1;
   }

   public void func_174885_b(int var1, int var2) {
   }

   public int func_174890_g() {
      return 0;
   }

   public void func_174888_l() {
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
