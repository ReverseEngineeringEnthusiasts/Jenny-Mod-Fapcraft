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

/**
 * A girl-equipment slot bound to the girl's forge item handler, enforcing
 * per-slot item types: swords/tools, bows, armor by body part, or the fishing
 * rod (Luna only).
 * <p>
 * Runs on both sides; {@link #isItemValid} is what keeps e.g. food out of the
 * weapon slot. The enum {@link b} maps slot ids 0..6 to their types.
 */
public class GirlInventorySlot extends SlotItemHandler {
   GirlInventorySlot.b slotType;

   public GirlInventorySlot(GirlInventorySlot.b slotType, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
      super(itemHandler, index, xPosition, yPosition);
      this.slotType = slotType;
   }

   /**
    * Whether the stack fits the slot type for the given slot id.
    *
    * @param stack the stack to test
    * @param slotId the girl slot id (0=weapon, 1=bow, 2=helmet, 3=chestplate,
    *              4=pants, 5=shoes, 6=rod)
    */
   public static boolean isSlotCompatible(ItemStack stack, int slotId) {
      return isSlotTypeCompatible(stack, GirlInventorySlot.b.getSlotType(slotId));
   }

   public boolean isItemValid(ItemStack stack) {
      return isSlotTypeCompatible(stack, this.slotType);
   }

   /**
    * The canonical slot-type test: the item class must match the slot type
    * (weapon = sword/tool, bow, armor matching the body part, rod).
    */
   static boolean isSlotTypeCompatible(ItemStack stack, GirlInventorySlot.b slotType) {
      Item item = stack.getItem();
      switch (slotType) {
         case WEAPON:
            return item instanceof ItemSword || item instanceof ItemTool;
         case BOW:
            return item instanceof ItemBow;
         case HELMET:
            return item instanceof ItemArmor && ((ItemArmor)item).armorType == EntityEquipmentSlot.HEAD;
         case CHEST_PLATE:
            return item instanceof ItemArmor && ((ItemArmor)item).armorType == EntityEquipmentSlot.CHEST;
         case PANTS:
            return item instanceof ItemArmor && ((ItemArmor)item).armorType == EntityEquipmentSlot.LEGS;
         case SHOES:
            return item instanceof ItemArmor && ((ItemArmor)item).armorType == EntityEquipmentSlot.FEET;
         case ROD:
            return item instanceof ItemFishingRod;
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

      public static GirlInventorySlot.b getSlotType(int slotId) {
         switch (slotId) {
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
               throw new NullPointerException("Girls don't have a slot nr. " + slotId);
         }
      }

      b(int id) {
         this.id = id;
      }

   }
}
