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

/**
 * <b>Role.</b> The dragon staff — the tribe-command tool. Right-clicking opens
 * the {@link StructureCommandScreen} (requesting tribe UI values via
 * {@link GetTribeUiValuesPacket}); right-clicking a bed/chest while holding it
 * is blocked so tribe blocks can only be managed through the staff UI. The
 * in-hand model and markers are handled by {@code DragonStaffRenderer} and
 * {@link StructureMarkerRenderer}. Right-click on air/block never places —
 * {@link #onItemRightClick} always fails.
 */
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

   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      return new ActionResult(EnumActionResult.FAIL, player.getHeldItem(hand));
   }

   @SubscribeEvent
   public static void registerItems(Register<Item> event) {
      event.getRegistry().register(DRAGON_STAFF);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onModelRegistry(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(DRAGON_STAFF, 0, new ModelResourceLocation("sexmod:dragon_staff"));
      DRAGON_STAFF.setTileEntityItemStackRenderer(new DragonStaffRenderer());
   }

   @Override
   public void registerControllers(AnimationData animationData) {
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }

   public static class a {
      @SubscribeEvent
      public void onRightClickItem(RightClickItem event) {
         World world = event.getWorld();
         if (world.isRemote) {
            EntityPlayer player = event.getEntityPlayer();
            if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() == DragonStaffItem.DRAGON_STAFF
               || player.getHeldItem(EnumHand.OFF_HAND).getItem() == DragonStaffItem.DRAGON_STAFF) {
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
      public void onRightClickBlock(RightClickBlock event) {
         EntityPlayer player = event.getEntityPlayer();
         if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() == DragonStaffItem.DRAGON_STAFF
            || player.getHeldItem(EnumHand.OFF_HAND).getItem() == DragonStaffItem.DRAGON_STAFF) {
            Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
            if (block instanceof BlockBed) {
               event.setCancellationResult(EnumActionResult.FAIL);
               event.setResult(Result.DENY);
               event.setCanceled(true);
            }

            if (block instanceof BlockChest) {
               event.setCancellationResult(EnumActionResult.FAIL);
               event.setResult(Result.DENY);
               event.setCanceled(true);
            }
         }
      }

   }
}
