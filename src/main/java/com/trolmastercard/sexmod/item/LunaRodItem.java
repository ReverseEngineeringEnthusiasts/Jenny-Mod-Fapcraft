package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.SexEntity;







import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LunaRodItem extends ItemFishingRod {
   public static final LunaRodItem a = new LunaRodItem();

   public LunaRodItem() {
      this.setMaxDamage(64);
      this.setMaxStackSize(1);
      this.addPropertyOverride(new ResourceLocation("cast"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            if (var3 == null) {
               return 0.0F;
            } else if (!(var3 instanceof LunaEntity)) {
               return 0.0F;
            } else {
               return var3.getDataManager().get(LunaEntity.af) ? 1.0F : 0.0F;
            }
         }
      });
   }

   public static void register() {
      a.setRegistryName(new ResourceLocation("sexmod", "luna_rod"));
      a.setTranslationKey("luna_rod");
      MinecraftForge.EVENT_BUS.register(LunaRodItem.class);
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(a);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(a, 0, new ModelResourceLocation("fishing_rod"));
   }

   public ActionResult<ItemStack> a(World var1, LunaEntity var2, EnumHand var3) {
      ItemStack var4 = var2.getHeldItem(var3);
      if (var2.av != null) {
         int var5 = var2.av.c_clash786();
         var4.damageItem(var5, var2);
         var2.swingArm(var3);
         var1.playSound(
            (EntityPlayer)null,
            var2.posX,
            var2.posY,
            var2.posZ,
            SoundEvents.ENTITY_BOBBER_RETRIEVE,
            SoundCategory.NEUTRAL,
            1.0F,
            0.4F / (itemRand.nextFloat() * 0.4F + 0.8F)
         );
      } else {
         var1.playSound(
            (EntityPlayer)null,
            var2.posX,
            var2.posY,
            var2.posZ,
            SoundEvents.ENTITY_BOBBER_THROW,
            SoundCategory.NEUTRAL,
            0.5F,
            0.4F / (itemRand.nextFloat() * 0.4F + 0.8F)
         );
         if (!var1.isRemote) {
            SexEntity.b = var2;
            double var10 = var2.getPositionVector().distanceTo(new Vec3d(var2.ai.getX(), var2.ai.getY(), var2.ai.getZ()));
            SexEntity var7 = new SexEntity(var1, var2, var10 * 0.01);
            int var8 = EnchantmentHelper.getFishingSpeedBonus(var4);
            if (var8 > 0) {
               var7.b_clash777(var8);
            }

            int var9 = EnchantmentHelper.getFishingLuckBonus(var4);
            if (var9 > 0) {
               var7.a_clash778(var9);
            }

            var1.spawnEntity(var7);
         }

         var2.swingArm(var3);
      }

      return new ActionResult(EnumActionResult.SUCCESS, var4);
   }
}
