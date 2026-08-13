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
   public static final TribeEggItem a = new TribeEggItem();

   public TribeEggItem() {
      this.func_77637_a(CreativeTabs.field_78026_f);
      this.field_77777_bU = 1;
   }

   public ActionResult<ItemStack> func_77659_a(World var1, EntityPlayer var2, EnumHand var3) {
      ItemStack var4 = var2.func_184586_b(var3);
      Vec3d var5 = var2.func_174824_e(0.0F);
      Vec3d var6 = var2.func_70676_i(0.0F);
      Vec3d var7 = var5.func_72441_c(var6.field_72450_a * 5.0, var6.field_72448_b * 5.0, var6.field_72449_c * 5.0);
      RayTraceResult var8 = var1.func_147447_a(var5, var7, false, false, true);
      if (var8 == null) {
         return new ActionResult(EnumActionResult.FAIL, var2.func_184586_b(var3));
      }

      if (var8.field_72313_a == Type.MISS) {
         return new ActionResult(EnumActionResult.FAIL, var2.func_184586_b(var3));
      }

      if (!var2.field_71075_bZ.field_75098_d) {
         var4.func_190918_g(1);
      }

      if (!var1.field_72995_K) {
         KoboldManager.a(var1, var8.field_72307_f);
      }

      return new ActionResult(EnumActionResult.SUCCESS, var2.func_184586_b(var3));
   }

   public static void register() {
      a.setRegistryName(new ResourceLocation("sexmod", "tribe_egg"));
      a.func_77655_b("tribe_egg");
      MinecraftForge.EVENT_BUS.register(TribeEggItem.class);
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(a);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(a, 0, new ModelResourceLocation("sexmod:tribe_egg"));
   }

}
