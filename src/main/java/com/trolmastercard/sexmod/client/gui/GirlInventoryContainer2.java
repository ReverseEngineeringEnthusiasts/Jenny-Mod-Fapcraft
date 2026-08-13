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

public class GirlInventoryContainer2 extends Container {
   LunaEntity d;
   public Slot[] b;
   public UUID a;
   public static List<GirlInventoryContainer2> c = new ArrayList<>();

   public GirlInventoryContainer2(LunaEntity var1, InventoryPlayer var2, UUID var3) {
      this.a = var3;
      c.add(this);
      if (var1.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH)) {
         IItemHandler var4 = (IItemHandler) var1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
         this.d = var1;
         this.b = new Slot[]{
            new GirlInventorySlot(GirlInventorySlot.b.WEAPON, var4, GirlInventorySlot.b.WEAPON.id, 41, 60),
            new GirlInventorySlot(GirlInventorySlot.b.BOW, var4, GirlInventorySlot.b.BOW.id, 59, 60),
            new GirlInventorySlot(GirlInventorySlot.b.HELMET, var4, GirlInventorySlot.b.HELMET.id, 81, 60),
            new GirlInventorySlot(GirlInventorySlot.b.CHEST_PLATE, var4, GirlInventorySlot.b.CHEST_PLATE.id, 100, 60),
            new GirlInventorySlot(GirlInventorySlot.b.PANTS, var4, GirlInventorySlot.b.PANTS.id, 119, 60),
            new GirlInventorySlot(GirlInventorySlot.b.SHOES, var4, GirlInventorySlot.b.SHOES.id, 138, 60),
            new GirlInventorySlot(GirlInventorySlot.b.ROD, var4, GirlInventorySlot.b.ROD.id, 22, 60)
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

         for (Slot var9 : this.b) {
            this.func_75146_a(var9);
         }

         for (Slot var14 : (java.util.Collection<Slot>) (var5) ) {
            this.func_75146_a(var14);
         }
      }
   }

   public ItemStack func_82846_b(EntityPlayer var1, int var2) {
      ItemStack var3 = ItemStack.field_190927_a;
      Slot var4 = (Slot)this.field_75151_b.get(var2);
      if (var4 != null && var4.func_75216_d()) {
         ItemStack var5 = var4.func_75211_c();
         var3 = var5.func_77946_l();
         int var6 = this.field_75151_b.size() - var1.field_71071_by.field_70462_a.size();
         if (var2 < var6) {
            if (!this.func_75135_a(var5, var6, this.field_75151_b.size(), true)) {
               return ItemStack.field_190927_a;
            }
         } else if (!this.func_75135_a(var5, 0, var6, false)) {
            return ItemStack.field_190927_a;
         }

         if (var5.func_190916_E() == 0) {
            var4.func_75215_d(ItemStack.field_190927_a);
         } else {
            var4.func_75218_e();
         }

         var4.func_190901_a(var1, var5);
      }

      return var3;
   }

   public void func_75141_a(int var1, ItemStack var2) {
      super.func_75141_a(var1, var2);
   }

   public boolean func_75145_c(EntityPlayer var1) {
      return true;
   }

   public void func_75134_a(EntityPlayer var1) {
      super.func_75134_a(var1);
   }

}
