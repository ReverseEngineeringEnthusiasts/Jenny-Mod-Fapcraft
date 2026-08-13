package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.client.renderer.KoboldEggItemRenderer;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEggEntity;
import java.util.UUID;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class KoboldEggItem extends Item implements IAnimatable {
   private final AnimationFactory animationFactory = new AnimationFactory(this);
   public static KoboldEggItem KOBOLD_EGG_ITEM = new KoboldEggItem();

   public KoboldEggItem() {
      this.setMaxStackSize(1);
   }

   public static void register() {
      KOBOLD_EGG_ITEM.setRegistryName(new ResourceLocation("sexmod", "kobold_egg_item"));
      KOBOLD_EGG_ITEM.setTranslationKey("kobold_egg_item");
      MinecraftForge.EVENT_BUS.register(KoboldEggItem.class);
   }

   @Override
   public void registerControllers(AnimationData var1) {
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelResourceLocation var1 = new ModelResourceLocation("sexmod:kobold_egg_item");
      ModelLoader.setCustomMeshDefinition(KOBOLD_EGG_ITEM, var1x -> var1);
      ModelBakery.registerItemVariants(KOBOLD_EGG_ITEM, new ResourceLocation[]{var1});
      KOBOLD_EGG_ITEM.setTileEntityItemStackRenderer(new KoboldEggItemRenderer());
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(KOBOLD_EGG_ITEM);
   }

   @SubscribeEvent
   public static void a(RightClickBlock var0) {
      World var1 = var0.getWorld();
      ItemStack var2 = var0.getItemStack();
      Vec3d var3 = var0.getHitVec();
      if (!var1.isRemote) {
         if (var2.getItem() == KOBOLD_EGG_ITEM) {
            KoboldEggEntity var4 = new KoboldEggEntity(var1);
            var4.setPosition(var3.x, var3.y, var3.z);
            var4.getDataManager().set(KoboldEggEntity.EGG_COLOR, EyeAndKoboldColor.getColorByWoolId(var2.getMetadata()).toString());
            NBTTagCompound var5 = var2.getTagCompound();
            if (var5 != null) {
               var4.tribeId = UUID.fromString(var5.getString("tribeID"));
            }

            var1.spawnEntity(var4);
            var2.shrink(1);
         }
      }
   }

}
