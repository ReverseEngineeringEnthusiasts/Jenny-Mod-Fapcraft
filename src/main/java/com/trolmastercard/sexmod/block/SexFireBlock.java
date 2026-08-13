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

public class SexFireBlock extends BlockFire {
   public static final Block FIRE = new SexFireBlock();

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public static void register() {
      FIRE.setRegistryName(new ResourceLocation("sexmod", "fire"));
      FIRE.setTranslationKey("fire");
      MinecraftForge.EVENT_BUS.register(SexFireBlock.class);
   }

   @SubscribeEvent
   public static void a(Register<Block> var0) {
      var0.getRegistry().register(FIRE);
   }
}
