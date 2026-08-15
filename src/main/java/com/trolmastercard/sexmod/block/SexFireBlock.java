package com.trolmastercard.sexmod.block;

import net.minecraft.util.ResourceLocation;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * <b>Role.</b> Inert fire block registered as {@code sexmod:fire} — its
 * {@link #updateTick} is deliberately empty so the block never spreads or
 * burns. Used by the galath/dragon effects that place a non-spreading fire
 * block. {@link #register()} wires it into the block registry event.
 */
public class SexFireBlock extends BlockFire {
   public static final Block FIRE = new SexFireBlock();

   public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
   }

   public static void register() {
      FIRE.setRegistryName(new ResourceLocation("sexmod", "fire"));
      FIRE.setTranslationKey("fire");
      MinecraftForge.EVENT_BUS.register(SexFireBlock.class);
   }

   @SubscribeEvent
   public static void registerBlocks(Register<Block> event) {
      event.getRegistry().register(FIRE);
   }
}
