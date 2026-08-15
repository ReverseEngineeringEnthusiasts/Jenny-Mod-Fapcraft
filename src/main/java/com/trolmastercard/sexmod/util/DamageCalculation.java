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

/**
 * <b>Role.</b> Custom armor damage reduction for NPC girls
 * ({@link AbstractGirlNpcEntity}). The constructor precomputes the vanilla
 * armor/toughness table per slot+material into {@code DamageCalculation.a};
 * {@link #onLivingDamage} then applies the full armor formula (reduction,
 * toughness, Protection/Thorns/Fire/Blast/Feather-Falling/Projectile
 * enchantments) to damage dealt to NPC girls, and reflects Thorns damage back
 * to the attacker.
 * <p>
 * <b>Pitfall.</b> The table is keyed by {@code slot+material} strings and
 * defaulted to {@code 3} reduction / {@code 0} toughness on a miss — an
 * unseeded entry silently weakens/strengthens armor instead of failing.
 */
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
   public void onLivingDamage(LivingDamageEvent event) {
      if (event.getEntity() instanceof AbstractGirlNpcEntity) {
         AbstractGirlNpcEntity girl = (AbstractGirlNpcEntity)event.getEntity();
         ItemStack[] armorStacks = new ItemStack[]{girl.inventory.getStackInSlot(2), girl.inventory.getStackInSlot(3), girl.inventory.getStackInSlot(4), girl.inventory.getStackInSlot(5)};
         ArrayList armorItems = new ArrayList();
         ArrayList toughnessItems = new ArrayList();

         for (ItemStack stack : armorStacks) {
            if (stack.getItem() instanceof ItemArmor) {
               armorItems.add((ItemArmor)stack.getItem());
               toughnessItems.add(stack);
            }
         }

         if (armorItems.size() != 0) {
            DamageSource source = event.getSource();
            int armorReduction = 0;
            int toughness = 0;
            if (!source.isUnblockable()) {
               for (ItemArmor armorItem : (java.util.Collection<ItemArmor>) (armorItems) ) {
                  armorReduction += DamageCalculation.a.getArmorDamageReduction(armorItem.armorType, armorItem.getArmorMaterial());
                  toughness += DamageCalculation.a.getArmorToughness(armorItem.armorType, armorItem.getArmorMaterial());
               }
            }

            float damage = event.getAmount();
            damage *= 1.0F - Math.min(20.0F, Math.max(armorReduction / 5.0F, armorReduction - 4.0F * damage / (toughness + 8.0F))) / 25.0F;
            float thornsDamage = 0.0F;

            for (ItemStack protectionStack : (java.util.Collection<ItemStack>) (toughnessItems) ) {
               int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, protectionStack);
               damage -= protectionLevel * 0.04F * damage;
               int thornsLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.THORNS, protectionStack);
               thornsDamage += Reference.RANDOM.nextFloat() < 0.15F * thornsLevel ? Reference.RANDOM.nextFloat() * 4.0F + 1.0F : 0.0F;
               thornsDamage = Math.min(4.0F, thornsDamage);
               if (source.isFireDamage()) {
                  int fireProtLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, protectionStack);
                  damage -= fireProtLevel * 0.08F * damage;
               }

               if (source.isExplosion()) {
                  int blastProtLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, protectionStack);
                  damage -= blastProtLevel * 0.08F * damage;
               }

               if (source.damageType.equals("fall")) {
                  int featherFallLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FEATHER_FALLING, protectionStack);
                  damage -= featherFallLevel * 0.12F * damage;
               }

               if (source.isProjectile()) {
                  int projectileProtLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION, protectionStack);
                  damage -= projectileProtLevel * 0.08F * damage;
               }
            }

            if (thornsDamage > 0.0F && source instanceof EntityDamageSource) {
               EntityDamageSource entitySource = (EntityDamageSource)source;
               if (entitySource.getTrueSource() != null) {
                  entitySource.getTrueSource().attackEntityFrom(DamageSource.causeThornsDamage(girl), thornsDamage);
               }
            }

            event.setAmount(damage);
         }
      }
   }

   static class a {
      public static HashMap<String, Integer[]> a = new HashMap<>();

      public static int getArmorDamageReduction(EntityEquipmentSlot slot, ArmorMaterial material) {
         try {
            return a.get(slot.toString() + material.toString())[0];
         } catch (NullPointerException exception) {
            return 3;
         }
      }

      public static int getArmorToughness(EntityEquipmentSlot slot, ArmorMaterial material) {
         try {
            return a.get(slot.toString() + material.toString())[1];
         } catch (NullPointerException exception) {
            return 0;
         }
      }

      public static void calculateDamage(EntityEquipmentSlot slot, ArmorMaterial material, int damage, int toughness) {
         a.put(slot.toString() + material.toString(), new Integer[]{damage, toughness});
      }
   }
}
