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
   public String c;

   public GirlHouseGenerator(String var1) {
      this.c = var1;
   }

   public void a_clash30(World var1, BlockPos var2) {
      MinecraftServer var3 = var1.func_73046_m();
      TemplateManager var4 = b.func_184163_y();
      ResourceLocation var5 = new ResourceLocation("sexmod", this.c);
      Template var6 = var4.func_189942_b(var3, var5);
      if (var6 != null) {
         IBlockState var7 = var1.func_180495_p(var2);
         var1.func_184138_a(var2, var7, var7, 3);
         var6.func_186253_b(var1, var2, a);
      }
   }

   public void a(World var1, BlockPos var2, Rotation var3) {
      MinecraftServer var4 = var1.func_73046_m();
      TemplateManager var5 = b.func_184163_y();
      ResourceLocation var6 = new ResourceLocation("sexmod", this.c);
      Template var7 = var5.func_189942_b(var4, var6);
      if (var7 != null) {
         IBlockState var8 = var1.func_180495_p(var2);
         var1.func_184138_a(var2, var8, var8, 2);
         var7.func_186253_b(var1, var2, a.func_186220_a(var3));
      }
   }

   public boolean func_180709_b(World var1, Random var2, BlockPos var3) {
      this.a_clash30(var1, var3);
      return true;
   }
}
