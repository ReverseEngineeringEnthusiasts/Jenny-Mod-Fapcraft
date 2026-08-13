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

public class GirlHouseGenerator extends WorldGenerator implements IWorldGen {
   public String structureName;

   public GirlHouseGenerator(String var1) {
      this.structureName = var1;
   }

   public void generateStructure(World var1, BlockPos var2) {
      MinecraftServer var3 = var1.getMinecraftServer();
      TemplateManager var4 = worldServer.getStructureTemplateManager();
      ResourceLocation var5 = new ResourceLocation("sexmod", this.structureName);
      Template var6 = var4.get(var3, var5);
      if (var6 != null) {
         IBlockState var7 = var1.getBlockState(var2);
         var1.notifyBlockUpdate(var2, var7, var7, 3);
         var6.addBlocksToWorld(var1, var2, settings);
      }
   }

   public void generateStructureRotated(World var1, BlockPos var2, Rotation var3) {
      MinecraftServer var4 = var1.getMinecraftServer();
      TemplateManager var5 = worldServer.getStructureTemplateManager();
      ResourceLocation var6 = new ResourceLocation("sexmod", this.structureName);
      Template var7 = var5.get(var4, var6);
      if (var7 != null) {
         IBlockState var8 = var1.getBlockState(var2);
         var1.notifyBlockUpdate(var2, var8, var8, 2);
         var7.addBlocksToWorld(var1, var2, settings.setRotation(var3));
      }
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      this.generateStructure(var1, var3);
      return true;
   }
}
