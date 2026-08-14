package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DamageCalculation {
   public DamageCalculation() {
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.HEAD, ArmorMaterial.LEATHER, 1, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.HEAD, ArmorMaterial.GOLD, 2, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.HEAD, ArmorMaterial.CHAIN, 2, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.HEAD, ArmorMaterial.IRON, 2, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.HEAD, ArmorMaterial.DIAMOND, 3, 3);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.CHEST, ArmorMaterial.LEATHER, 3, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.CHEST, ArmorMaterial.GOLD, 5, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.CHEST, ArmorMaterial.CHAIN, 5, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.CHEST, ArmorMaterial.IRON, 6, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.CHEST, ArmorMaterial.DIAMOND, 8, 3);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.LEGS, ArmorMaterial.LEATHER, 2, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.LEGS, ArmorMaterial.GOLD, 3, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.LEGS, ArmorMaterial.CHAIN, 4, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.LEGS, ArmorMaterial.IRON, 5, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.LEGS, ArmorMaterial.DIAMOND, 6, 3);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.FEET, ArmorMaterial.LEATHER, 1, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.FEET, ArmorMaterial.GOLD, 1, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.FEET, ArmorMaterial.CHAIN, 1, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.FEET, ArmorMaterial.IRON, 2, 0);
      DamageCalculation.a.calculateDamage(EntityEquipmentSlot.FEET, ArmorMaterial.DIAMOND, 3, 3);
   }

   @SubscribeEvent
   public void onLivingDamage(LivingDamageEvent var1) {
      if (var1.getEntity() instanceof AbstractGirlNpcEntity) {
         AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntity();
         ItemStack[] var3 = new ItemStack[]{var2.inventory.getStackInSlot(2), var2.inventory.getStackInSlot(3), var2.inventory.getStackInSlot(4), var2.inventory.getStackInSlot(5)};
         ArrayList var4 = new ArrayList();
         ArrayList var5 = new ArrayList();

         for (ItemStack var9 : var3) {
            if (var9.getItem() instanceof ItemArmor) {
               var4.add((ItemArmor)var9.getItem());
               var5.add(var9);
            }
         }

         if (var4.size() != 0) {
            DamageSource var17 = var1.getSource();
            int var18 = 0;
            int var19 = 0;
            if (!var17.isUnblockable()) {
               for (ItemArmor var10 : (java.util.Collection<ItemArmor>) (var4) ) {
                  var18 += DamageCalculation.a.getArmorDamageReduction(var10.armorType, var10.getArmorMaterial());
                  var19 += DamageCalculation.a.getArmorToughness(var10.armorType, var10.getArmorMaterial());
               }
            }

            float var21 = var1.getAmount();
            var21 *= 1.0F - Math.min(20.0F, Math.max(var18 / 5.0F, var18 - 4.0F * var21 / (var19 + 8.0F))) / 25.0F;
            float var23 = 0.0F;

            for (ItemStack var13 : (java.util.Collection<ItemStack>) (var5) ) {
               int var14 = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, var13);
               var21 -= var14 * 0.04F * var21;
               int var15 = EnchantmentHelper.getEnchantmentLevel(Enchantments.THORNS, var13);
               var23 += Reference.RANDOM.nextFloat() < 0.15F * var15 ? Reference.RANDOM.nextFloat() * 4.0F + 1.0F : 0.0F;
               var23 = Math.min(4.0F, var23);
               if (var17.isFireDamage()) {
                  int var16 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, var13);
                  var21 -= var16 * 0.08F * var21;
               }

               if (var17.isExplosion()) {
                  int var26 = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, var13);
                  var21 -= var26 * 0.08F * var21;
               }

               if (var17.damageType.equals("fall")) {
                  int var27 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FEATHER_FALLING, var13);
                  var21 -= var27 * 0.12F * var21;
               }

               if (var17.isProjectile()) {
                  int var28 = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION, var13);
                  var21 -= var28 * 0.08F * var21;
               }
            }

            if (var23 > 0.0F && var17 instanceof EntityDamageSource) {
               EntityDamageSource var25 = (EntityDamageSource)var17;
               if (var25.getTrueSource() != null) {
                  var25.getTrueSource().attackEntityFrom(DamageSource.causeThornsDamage(var2), var23);
               }
            }

            var1.setAmount(var21);
         }
      }
   }

   static class a {
      public static HashMap<String, Integer[]> a = new HashMap<>();

      public static int getArmorDamageReduction(EntityEquipmentSlot var0, ArmorMaterial var1) {
         try {
            return a.get(var0.toString() + var1.toString())[0];
         } catch (NullPointerException var2) {
            return 3;
         }
      }

      public static int getArmorToughness(EntityEquipmentSlot var0, ArmorMaterial var1) {
         try {
            return a.get(var0.toString() + var1.toString())[1];
         } catch (NullPointerException var2) {
            return 0;
         }
      }

      public static void calculateDamage(EntityEquipmentSlot var0, ArmorMaterial var1, int var2, int var3) {
         a.put(var0.toString() + var1.toString(), new Integer[]{var2, var3});
      }
   }
}
