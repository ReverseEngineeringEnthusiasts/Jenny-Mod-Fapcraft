package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.client.gui.StructureCommandScreen;
import com.trolmastercard.sexmod.client.renderer.DragonStaffRenderer;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.networking.GetTribeUiValuesPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class DragonStaffItem extends Item implements IAnimatable {
   public static final DragonStaffItem DRAGON_STAFF = new DragonStaffItem();
   private final AnimationFactory animationFactory = new AnimationFactory(this);

   public DragonStaffItem() {
      this.setCreativeTab(CreativeTabs.TOOLS);
      this.maxStackSize = 1;
   }

   public static void register() {
      DRAGON_STAFF.setRegistryName(new ResourceLocation("sexmod", "dragon_staff"));
      DRAGON_STAFF.setTranslationKey("dragon_staff");
      MinecraftForge.EVENT_BUS.register(DragonStaffItem.class);
   }

   public ActionResult<ItemStack> onItemRightClick(World var1, EntityPlayer var2, EnumHand var3) {
      return new ActionResult(EnumActionResult.FAIL, var2.getHeldItem(var3));
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(DRAGON_STAFF);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(DRAGON_STAFF, 0, new ModelResourceLocation("sexmod:dragon_staff"));
      DRAGON_STAFF.setTileEntityItemStackRenderer(new DragonStaffRenderer());
   }

   @Override
   public void registerControllers(AnimationData var1) {
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }

   public static class a {
      @SubscribeEvent
      public void a(RightClickItem var1) {
         World var2 = var1.getWorld();
         if (var2.isRemote) {
            EntityPlayer var3 = var1.getEntityPlayer();
            if (var3.getHeldItem(EnumHand.MAIN_HAND).getItem() == DragonStaffItem.DRAGON_STAFF
               || var3.getHeldItem(EnumHand.OFF_HAND).getItem() == DragonStaffItem.DRAGON_STAFF) {
               if (!KoboldEntity.aY.isEmpty()) {
                  this.openStructureCommand();
               }
            }
         }
      }

      @SideOnly(Side.CLIENT)
      void openStructureCommand() {
         Minecraft.getMinecraft().displayGuiScreen(new StructureCommandScreen());
         PacketHandler.networkWrapper.sendToServer(new GetTribeUiValuesPacket());
      }

      @SubscribeEvent
      public void a(RightClickBlock var1) {
         EntityPlayer var2 = var1.getEntityPlayer();
         if (var2.getHeldItem(EnumHand.MAIN_HAND).getItem() == DragonStaffItem.DRAGON_STAFF
            || var2.getHeldItem(EnumHand.OFF_HAND).getItem() == DragonStaffItem.DRAGON_STAFF) {
            Block var3 = var1.getWorld().getBlockState(var1.getPos()).getBlock();
            if (var3 instanceof BlockBed) {
               var1.setCancellationResult(EnumActionResult.FAIL);
               var1.setResult(Result.DENY);
               var1.setCanceled(true);
            }

            if (var3 instanceof BlockChest) {
               var1.setCancellationResult(EnumActionResult.FAIL);
               var1.setResult(Result.DENY);
               var1.setCanceled(true);
            }
         }
      }

   }
}
