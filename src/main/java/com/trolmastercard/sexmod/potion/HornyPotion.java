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

public class HornyPotion extends Potion {
   public static final Potion b = new HornyPotion("horny potion", false, 16736968, 0, 0);
   public static final PotionType a = (PotionType)new PotionType(
         "horny_potion", new PotionEffect[]{new PotionEffect(b, 3600), new PotionEffect(MobEffects.NAUSEA, 200, 1)}
      )
      .setRegistryName("horny_potion");

   public HornyPotion() {
      super(false, 0);
   }

   public HornyPotion(String var1, boolean var2, int var3, int var4, int var5) {
      super(var2, var3);
      this.setPotionName(var1);
      this.setIconIndex(var4, var5);
      this.setRegistryName(new ResourceLocation("sexmod:" + var1));
   }

   public static void register() {
      ForgeRegistries.POTIONS.register(b);
      ForgeRegistries.POTION_TYPES.register(a);
      PotionHelper.addMix(PotionTypes.MUNDANE, Item.getItemFromBlock(Blocks.RED_FLOWER), a);
   }

   @SubscribeEvent
   public void a(PlayerTickEvent var1) {
      EntityPlayer var2 = var1.player;
      PotionEffect var3 = var2.getActivePotionEffect(b);
      if (!var2.world.isRemote) {
         if (var3 != null) {
            if (var3.getDuration() <= 3500) {
               var2.removePotionEffect(b);
               PacketHandler.b.sendTo(new GirlDataPacket(var2), (EntityPlayerMP)var2);
            }
         }
      }
   }

   @SubscribeEvent
   public void a(LivingUpdateEvent var1) {
      if (var1.getEntity() instanceof EntityVillager) {
         EntityVillager var2 = (EntityVillager)var1.getEntity();
         if (var2.isPotionActive(b)) {
            var2.tasks.addTask(2, new GirlAiBase(var2));
            var2.removePotionEffect(b);
         }
      }

      if (var1.getEntity() instanceof EntityAnimal) {
         EntityAnimal var3 = (EntityAnimal)var1.getEntity();
         if (var3.isPotionActive(b)) {
            if (var3.getGrowingAge() >= 0) {
               var3.setGrowingAge(0);
               var3.resetInLove();
               var3.setInLove(var3.world.getClosestPlayerToEntity(var3, 30.0));
            }

            var3.removePotionEffect(b);
         }
      }
   }

}
