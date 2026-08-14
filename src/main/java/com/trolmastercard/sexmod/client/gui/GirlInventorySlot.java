package com.trolmastercard.sexmod.client.gui;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class GirlInventorySlot extends SlotItemHandler {
   GirlInventorySlot.b slotType;

   public GirlInventorySlot(GirlInventorySlot.b var1, IItemHandler var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.slotType = var1;
   }

   public static boolean isSlotCompatible(ItemStack var0, int var1) {
      return isSlotTypeCompatible(var0, GirlInventorySlot.b.getSlotType(var1));
   }

   public boolean isItemValid(ItemStack var1) {
      return isSlotTypeCompatible(var1, this.slotType);
   }

   static boolean isSlotTypeCompatible(ItemStack var0, GirlInventorySlot.b var1) {
      Item var2 = var0.getItem();
      switch (var1) {
         case WEAPON:
            return var2 instanceof ItemSword || var2 instanceof ItemTool;
         case BOW:
            return var2 instanceof ItemBow;
         case HELMET:
            return var2 instanceof ItemArmor && ((ItemArmor)var2).armorType == EntityEquipmentSlot.HEAD;
         case CHEST_PLATE:
            return var2 instanceof ItemArmor && ((ItemArmor)var2).armorType == EntityEquipmentSlot.CHEST;
         case PANTS:
            return var2 instanceof ItemArmor && ((ItemArmor)var2).armorType == EntityEquipmentSlot.LEGS;
         case SHOES:
            return var2 instanceof ItemArmor && ((ItemArmor)var2).armorType == EntityEquipmentSlot.FEET;
         case ROD:
            return var2 instanceof ItemFishingRod;
         default:
            return false;
      }
   }

   public enum b {
      WEAPON(0),
      BOW(1),
      HELMET(2),
      CHEST_PLATE(3),
      PANTS(4),
      SHOES(5),
      ROD(6);

      public int id;

      public static GirlInventorySlot.b getSlotType(int var0) {
         switch (var0) {
            case 0:
               return WEAPON;
            case 1:
               return BOW;
            case 2:
               return HELMET;
            case 3:
               return CHEST_PLATE;
            case 4:
               return PANTS;
            case 5:
               return SHOES;
            case 6:
               return ROD;
            default:
               throw new NullPointerException("Girls don't have a slot nr. " + var0);
         }
      }

      b(int var3) {
         this.id = var3;
      }

   }
}
