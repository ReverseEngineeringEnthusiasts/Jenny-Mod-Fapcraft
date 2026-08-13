package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.util.KoboldManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TribeEggItem extends Item {
   public static final TribeEggItem TRIBE_EGG = new TribeEggItem();

   public TribeEggItem() {
      this.setCreativeTab(CreativeTabs.MISC);
      this.maxStackSize = 1;
   }

   public ActionResult<ItemStack> onItemRightClick(World var1, EntityPlayer var2, EnumHand var3) {
      ItemStack var4 = var2.getHeldItem(var3);
      Vec3d var5 = var2.getPositionEyes(0.0F);
      Vec3d var6 = var2.getLook(0.0F);
      Vec3d var7 = var5.add(var6.x * 5.0, var6.y * 5.0, var6.z * 5.0);
      RayTraceResult var8 = var1.rayTraceBlocks(var5, var7, false, false, true);
      if (var8 == null) {
         return new ActionResult(EnumActionResult.FAIL, var2.getHeldItem(var3));
      }

      if (var8.typeOfHit == Type.MISS) {
         return new ActionResult(EnumActionResult.FAIL, var2.getHeldItem(var3));
      }

      if (!var2.capabilities.isCreativeMode) {
         var4.shrink(1);
      }

      if (!var1.isRemote) {
         KoboldManager.spawnKoboldAt(var1, var8.hitVec);
      }

      return new ActionResult(EnumActionResult.SUCCESS, var2.getHeldItem(var3));
   }

   public static void register() {
      TRIBE_EGG.setRegistryName(new ResourceLocation("sexmod", "tribe_egg"));
      TRIBE_EGG.setTranslationKey("tribe_egg");
      MinecraftForge.EVENT_BUS.register(TribeEggItem.class);
   }

   @SubscribeEvent
   public static void registerItems(Register<Item> var0) {
      var0.getRegistry().register(TRIBE_EGG);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onModelRegistry(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(TRIBE_EGG, 0, new ModelResourceLocation("sexmod:tribe_egg"));
   }

}
