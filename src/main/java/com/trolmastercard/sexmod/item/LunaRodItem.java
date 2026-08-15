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

/**
 * <b>Role.</b> The Luna rod — a fishing rod for the cat-girl Luna with a
 * {@code cast} model override driven by her data manager. {@link #castFishingRod}
 * is invoked by {@link CatActivateFishingPacket} server-side: it retrieves the
 * existing {@code SexEntity} bobber or spawns a new one aimed at her fishing
 * target, applying fishing-speed/luck enchantments from the rod.
 */
public class LunaRodItem extends ItemFishingRod {
   public static final LunaRodItem LUNA_ROD = new LunaRodItem();

   public LunaRodItem() {
      this.setMaxDamage(64);
      this.setMaxStackSize(1);
      this.addPropertyOverride(new ResourceLocation("cast"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            if (entity == null) {
               return 0.0F;
            } else if (!(entity instanceof LunaEntity)) {
               return 0.0F;
            } else {
               return entity.getDataManager().get(LunaEntity.af) ? 1.0F : 0.0F;
            }
         }
      });
   }

   public static void register() {
      LUNA_ROD.setRegistryName(new ResourceLocation("sexmod", "luna_rod"));
      LUNA_ROD.setTranslationKey("luna_rod");
      MinecraftForge.EVENT_BUS.register(LunaRodItem.class);
   }

   @SubscribeEvent
   public static void registerItems(Register<Item> event) {
      event.getRegistry().register(LUNA_ROD);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onModelRegistry(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(LUNA_ROD, 0, new ModelResourceLocation("fishing_rod"));
   }

   public ActionResult<ItemStack> castFishingRod(World world, LunaEntity luna, EnumHand hand) {
      ItemStack stack = luna.getHeldItem(hand);
      if (luna.av != null) {
         int catchResult = luna.av.getCatchResult();
         stack.damageItem(catchResult, luna);
         luna.swingArm(hand);
         world.playSound(
            (EntityPlayer)null,
            luna.posX,
            luna.posY,
            luna.posZ,
            SoundEvents.ENTITY_BOBBER_RETRIEVE,
            SoundCategory.NEUTRAL,
            1.0F,
            0.4F / (itemRand.nextFloat() * 0.4F + 0.8F)
         );
      } else {
         world.playSound(
            (EntityPlayer)null,
            luna.posX,
            luna.posY,
            luna.posZ,
            SoundEvents.ENTITY_BOBBER_THROW,
            SoundCategory.NEUTRAL,
            0.5F,
            0.4F / (itemRand.nextFloat() * 0.4F + 0.8F)
         );
         if (!world.isRemote) {
            SexEntity.ownerLuna = luna;
            double distance = luna.getPositionVector().distanceTo(new Vec3d(luna.ai.getX(), luna.ai.getY(), luna.ai.getZ()));
            SexEntity sexEntity = new SexEntity(world, luna, distance * 0.01);
            int speedBonus = EnchantmentHelper.getFishingSpeedBonus(stack);
            if (speedBonus > 0) {
               sexEntity.setFishingLevel(speedBonus);
            }

            int luckBonus = EnchantmentHelper.getFishingLuckBonus(stack);
            if (luckBonus > 0) {
               sexEntity.setPhase(luckBonus);
            }

            world.spawnEntity(sexEntity);
         }

         luna.swingArm(hand);
      }

      return new ActionResult(EnumActionResult.SUCCESS, stack);
   }
}
