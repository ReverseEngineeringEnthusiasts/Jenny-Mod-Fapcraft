package com.trolmastercard.sexmod.worldgen;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

/**
 * Generates the girls' houses in the overworld — the structures where NPC
 * girls spawn and call home ({@code homePos}).
 */
public class GirlHouseGenerator extends WorldGenerator implements IWorldGen {
   public String structureName;

   public GirlHouseGenerator(String structureName) {
      this.structureName = structureName;
   }

   public void generateStructure(World world, BlockPos pos) {
      MinecraftServer server = world.getMinecraftServer();
      TemplateManager templateManager = worldServer.getStructureTemplateManager();
      ResourceLocation location = new ResourceLocation("sexmod", this.structureName);
      Template template = templateManager.get(server, location);
      if (template != null) {
         IBlockState state = world.getBlockState(pos);
         world.notifyBlockUpdate(pos, state, state, 3);
         template.addBlocksToWorld(world, pos, settings);
      }
   }

   public void generateStructureRotated(World world, BlockPos pos, Rotation rotation) {
      MinecraftServer server = world.getMinecraftServer();
      TemplateManager templateManager = worldServer.getStructureTemplateManager();
      ResourceLocation location = new ResourceLocation("sexmod", this.structureName);
      Template template = templateManager.get(server, location);
      if (template != null) {
         IBlockState state = world.getBlockState(pos);
         world.notifyBlockUpdate(pos, state, state, 2);
         template.addBlocksToWorld(world, pos, settings.setRotation(rotation));
      }
   }

   public boolean generate(World world, Random random, BlockPos pos) {
      this.generateStructure(world, pos);
      return true;
   }
}
