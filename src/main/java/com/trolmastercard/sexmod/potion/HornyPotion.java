package com.trolmastercard.sexmod.potion;

import com.trolmastercard.sexmod.entity.ai.GirlAiBase;
import com.trolmastercard.sexmod.networking.GirlDataPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * The horny potion — the item/effect that transforms the player into a girl
 * (spawns the XxxPlayerEntity form, hides the vanilla player). Girls check
 * {@code getActivePotionEffect(HORNY_POTION)} to decide whether scenes skip
 * the PAYMENT gate (Kobold U(), Jenny's menu) and to track the transformed
 * state (Jenny's {@code yFlag}, Luna's menu behavior).
 */
public class HornyPotion extends Potion {
   public static final Potion HORNY_POTION = new HornyPotion("horny potion", false, 16736968, 0, 0);
   public static final PotionType POTION_TYPE = (PotionType)new PotionType(
         "horny_potion", new PotionEffect[]{new PotionEffect(HORNY_POTION, 3600), new PotionEffect(MobEffects.NAUSEA, 200, 1)}
      )
      .setRegistryName("horny_potion");

   public HornyPotion() {
      super(false, 0);
   }

   public HornyPotion(String name, boolean isBadEffect, int liquidColor, int iconX, int iconY) {
      super(isBadEffect, liquidColor);
      this.setPotionName(name);
      this.setIconIndex(iconX, iconY);
      this.setRegistryName(new ResourceLocation("sexmod:" + name));
   }

   public static void register() {
      ForgeRegistries.POTIONS.register(HORNY_POTION);
      ForgeRegistries.POTION_TYPES.register(POTION_TYPE);
      PotionHelper.addMix(PotionTypes.MUNDANE, Item.getItemFromBlock(Blocks.RED_FLOWER), POTION_TYPE);
   }

   @SubscribeEvent
   public void onPlayerTick(PlayerTickEvent event) {
      EntityPlayer player = event.player;
      PotionEffect effect = player.getActivePotionEffect(HORNY_POTION);
      if (!player.world.isRemote) {
         if (effect != null) {
            if (effect.getDuration() <= 3500) {
               player.removePotionEffect(HORNY_POTION);
               PacketHandler.networkWrapper.sendTo(new GirlDataPacket(player), (EntityPlayerMP)player);
            }
         }
      }
   }

   @SubscribeEvent
   public void onLivingUpdate(LivingUpdateEvent event) {
      if (event.getEntity() instanceof EntityVillager) {
         EntityVillager villager = (EntityVillager)event.getEntity();
         if (villager.isPotionActive(HORNY_POTION)) {
            villager.tasks.addTask(2, new GirlAiBase(villager));
            villager.removePotionEffect(HORNY_POTION);
         }
      }

      if (event.getEntity() instanceof EntityAnimal) {
         EntityAnimal animal = (EntityAnimal)event.getEntity();
         if (animal.isPotionActive(HORNY_POTION)) {
            if (animal.getGrowingAge() >= 0) {
               animal.setGrowingAge(0);
               animal.resetInLove();
               animal.setInLove(animal.world.getClosestPlayerToEntity(animal, 30.0));
            }

            animal.removePotionEffect(HORNY_POTION);
         }
      }
   }

}
