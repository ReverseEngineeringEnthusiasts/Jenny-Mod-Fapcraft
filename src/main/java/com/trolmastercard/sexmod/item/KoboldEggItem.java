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

/**
 * <b>Role.</b> Kobold egg — placing it spawns a {@link KoboldEggEntity} colored
 * by the item's metadata (wool color); a {@code tribeID} NBT tag (written by
 * {@link SendEggPacket}) binds the hatched kobold to that tribe.
 */
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
   public void registerControllers(AnimationData animationData) {
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onModelRegistry(ModelRegistryEvent event) {
      ModelResourceLocation location = new ModelResourceLocation("sexmod:kobold_egg_item");
      ModelLoader.setCustomMeshDefinition(KOBOLD_EGG_ITEM, stack -> location);
      ModelBakery.registerItemVariants(KOBOLD_EGG_ITEM, new ResourceLocation[]{location});
      KOBOLD_EGG_ITEM.setTileEntityItemStackRenderer(new KoboldEggItemRenderer());
   }

   @SubscribeEvent
   public static void registerItems(Register<Item> event) {
      event.getRegistry().register(KOBOLD_EGG_ITEM);
   }

   @SubscribeEvent
   public static void onRightClickBlock(RightClickBlock event) {
      World world = event.getWorld();
      ItemStack stack = event.getItemStack();
      Vec3d hitVec = event.getHitVec();
      if (!world.isRemote) {
         if (stack.getItem() == KOBOLD_EGG_ITEM) {
            KoboldEggEntity egg = new KoboldEggEntity(world);
            egg.setPosition(hitVec.x, hitVec.y, hitVec.z);
            egg.getDataManager().set(KoboldEggEntity.EGG_COLOR, EyeAndKoboldColor.getColorByWoolId(stack.getMetadata()).toString());
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
               egg.tribeId = UUID.fromString(tag.getString("tribeID"));
            }

            world.spawnEntity(egg);
            stack.shrink(1);
         }
      }
   }

}
