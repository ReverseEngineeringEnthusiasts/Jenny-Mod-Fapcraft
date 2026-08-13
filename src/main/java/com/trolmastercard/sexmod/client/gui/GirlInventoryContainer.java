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
   private final IInventory a;
   private final int d;
   public static List<GirlInventoryContainer> b = new ArrayList<>();
   public UUID c;

   public GirlInventoryContainer(IInventory var1, IInventory var2, EntityPlayer var3, UUID var4) {
      this.c = var4;
      b.add(this);
      this.a = var2;
      var2.func_174889_b(var3);
      this.d = 3;
      byte var5 = -18;

      for (int var6 = 0; var6 < 3; var6++) {
         for (int var7 = 0; var7 < 9; var7++) {
            this.func_75146_a(new Slot(var2, var7 + var6 * 9, 8 + var7 * 18, 18 + var6 * 18));
         }
      }

      for (int var8 = 0; var8 < 3; var8++) {
         for (int var10 = 0; var10 < 9; var10++) {
            this.func_75146_a(new Slot(var1, var10 + var8 * 9 + 9, 8 + var10 * 18, 103 + var8 * 18 + var5));
         }
      }

      for (int var9 = 0; var9 < 9; var9++) {
         this.func_75146_a(new Slot(var1, var9, 8 + var9 * 18, 143));
      }
   }

   public boolean func_75145_c(EntityPlayer var1) {
      return this.a.func_70300_a(var1);
   }

   public ItemStack func_82846_b(EntityPlayer var1, int var2) {
      ItemStack var3 = ItemStack.field_190927_a;
      Slot var4 = (Slot)this.field_75151_b.get(var2);
      if (var4 != null && var4.func_75216_d()) {
         ItemStack var5 = var4.func_75211_c();
         var3 = var5.func_77946_l();
         if (var2 < this.d * 9) {
            if (!this.func_75135_a(var5, this.d * 9, this.field_75151_b.size(), true)) {
               return ItemStack.field_190927_a;
            }
         } else if (!this.func_75135_a(var5, 0, this.d * 9, false)) {
            return ItemStack.field_190927_a;
         }

         if (var5.func_190926_b()) {
            var4.func_75215_d(ItemStack.field_190927_a);
         } else {
            var4.func_75218_e();
         }
      }

      return var3;
   }

   public void func_75134_a(EntityPlayer var1) {
      super.func_75134_a(var1);
      this.a.func_174886_c(var1);
   }

   public IInventory a_clash197() {
      return this.a;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
