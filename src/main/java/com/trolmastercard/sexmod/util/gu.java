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

public class gu {
   public gu() {
      gu.a.a(EntityEquipmentSlot.HEAD, ArmorMaterial.LEATHER, 1, 0);
      gu.a.a(EntityEquipmentSlot.HEAD, ArmorMaterial.GOLD, 2, 0);
      gu.a.a(EntityEquipmentSlot.HEAD, ArmorMaterial.CHAIN, 2, 0);
      gu.a.a(EntityEquipmentSlot.HEAD, ArmorMaterial.IRON, 2, 0);
      gu.a.a(EntityEquipmentSlot.HEAD, ArmorMaterial.DIAMOND, 3, 3);
      gu.a.a(EntityEquipmentSlot.CHEST, ArmorMaterial.LEATHER, 3, 0);
      gu.a.a(EntityEquipmentSlot.CHEST, ArmorMaterial.GOLD, 5, 0);
      gu.a.a(EntityEquipmentSlot.CHEST, ArmorMaterial.CHAIN, 5, 0);
      gu.a.a(EntityEquipmentSlot.CHEST, ArmorMaterial.IRON, 6, 0);
      gu.a.a(EntityEquipmentSlot.CHEST, ArmorMaterial.DIAMOND, 8, 3);
      gu.a.a(EntityEquipmentSlot.LEGS, ArmorMaterial.LEATHER, 2, 0);
      gu.a.a(EntityEquipmentSlot.LEGS, ArmorMaterial.GOLD, 3, 0);
      gu.a.a(EntityEquipmentSlot.LEGS, ArmorMaterial.CHAIN, 4, 0);
      gu.a.a(EntityEquipmentSlot.LEGS, ArmorMaterial.IRON, 5, 0);
      gu.a.a(EntityEquipmentSlot.LEGS, ArmorMaterial.DIAMOND, 6, 3);
      gu.a.a(EntityEquipmentSlot.FEET, ArmorMaterial.LEATHER, 1, 0);
      gu.a.a(EntityEquipmentSlot.FEET, ArmorMaterial.GOLD, 1, 0);
      gu.a.a(EntityEquipmentSlot.FEET, ArmorMaterial.CHAIN, 1, 0);
      gu.a.a(EntityEquipmentSlot.FEET, ArmorMaterial.IRON, 2, 0);
      gu.a.a(EntityEquipmentSlot.FEET, ArmorMaterial.DIAMOND, 3, 3);
   }

   @SubscribeEvent
   public void a(LivingDamageEvent var1) {
      if (var1.getEntity() instanceof AbstractGirlNpcEntity) {
         AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntity();
         ItemStack[] var3 = new ItemStack[]{var2.Q.getStackInSlot(2), var2.Q.getStackInSlot(3), var2.Q.getStackInSlot(4), var2.Q.getStackInSlot(5)};
         ArrayList var4 = new ArrayList();
         ArrayList var5 = new ArrayList();

         for (ItemStack var9 : var3) {
            if (var9.func_77973_b() instanceof ItemArmor) {
               var4.add((ItemArmor)var9.func_77973_b());
               var5.add(var9);
            }
         }

         if (var4.size() != 0) {
            DamageSource var17 = var1.getSource();
            int var18 = 0;
            int var19 = 0;
            if (!var17.func_76363_c()) {
               for (ItemArmor var10 : (java.util.Collection<ItemArmor>) (var4) ) {
                  var18 += gu.a.a(var10.field_77881_a, var10.func_82812_d());
                  var19 += gu.a.b(var10.field_77881_a, var10.func_82812_d());
               }
            }

            float var21 = var1.getAmount();
            var21 *= 1.0F - Math.min(20.0F, Math.max(var18 / 5.0F, var18 - 4.0F * var21 / (var19 + 8.0F))) / 25.0F;
            float var23 = 0.0F;

            for (ItemStack var13 : (java.util.Collection<ItemStack>) (var5) ) {
               int var14 = EnchantmentHelper.func_77506_a(Enchantments.field_180310_c, var13);
               var21 -= var14 * 0.04F * var21;
               int var15 = EnchantmentHelper.func_77506_a(Enchantments.field_92091_k, var13);
               var23 += Reference.f.nextFloat() < 0.15F * var15 ? Reference.f.nextFloat() * 4.0F + 1.0F : 0.0F;
               var23 = Math.min(4.0F, var23);
               if (var17.func_76347_k()) {
                  int var16 = EnchantmentHelper.func_77506_a(Enchantments.field_77329_d, var13);
                  var21 -= var16 * 0.08F * var21;
               }

               if (var17.func_94541_c()) {
                  int var26 = EnchantmentHelper.func_77506_a(Enchantments.field_185297_d, var13);
                  var21 -= var26 * 0.08F * var21;
               }

               if (var17.field_76373_n.equals("fall")) {
                  int var27 = EnchantmentHelper.func_77506_a(Enchantments.field_180309_e, var13);
                  var21 -= var27 * 0.12F * var21;
               }

               if (var17.func_76352_a()) {
                  int var28 = EnchantmentHelper.func_77506_a(Enchantments.field_180308_g, var13);
                  var21 -= var28 * 0.08F * var21;
               }
            }

            if (var23 > 0.0F && var17 instanceof EntityDamageSource) {
               EntityDamageSource var25 = (EntityDamageSource)var17;
               if (var25.func_76346_g() != null) {
                  var25.func_76346_g().func_70097_a(DamageSource.func_92087_a(var2), var23);
               }
            }

            var1.setAmount(var21);
         }
      }
   }


   static class a {
      public static HashMap<String, Integer[]> a = new HashMap<>();

      public static int a(EntityEquipmentSlot var0, ArmorMaterial var1) {
         try {
            return a.get(var0.toString() + var1.toString())[0];
         } catch (NullPointerException var2) {
            return 3;
         }
      }

      public static int b(EntityEquipmentSlot var0, ArmorMaterial var1) {
         try {
            return a.get(var0.toString() + var1.toString())[1];
         } catch (NullPointerException var2) {
            return 0;
         }
      }

      public static void a(EntityEquipmentSlot var0, ArmorMaterial var1, int var2, int var3) {
         a.put(var0.toString() + var1.toString(), new Integer[]{var2, var3});
      }
   }
}
