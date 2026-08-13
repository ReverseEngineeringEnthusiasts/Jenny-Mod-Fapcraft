package com.trolmastercard.sexmod.client;


import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomePlains;
import net.minecraft.world.biome.Biome.BiomeProperties;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SexWorldClient extends WorldClient {
   public Biome getBiomeForCoordsBody(BlockPos var1) {
      return new BiomePlains(false, new BiomeProperties("Plains").func_185398_c(0.125F).func_185400_d(0.05F).func_185400_d(0.8F).func_185395_b(0.4F));
   }

   public void func_175685_c(BlockPos var1, Block var2, boolean var3) {
      super.func_175685_c(var1, var2, var3);
   }

   public void markAndNotifyBlock(BlockPos var1, Chunk var2, IBlockState var3, IBlockState var4, int var5) {
   }

   public float getSunBrightnessFactor(float var1) {
      return 1.0F;
   }

   @SideOnly(Side.CLIENT)
   public float getSunBrightnessBody(float var1) {
      return 1.0F;
   }

   public void updateWeatherBody() {
   }

   public boolean canBlockFreezeBody(BlockPos var1, boolean var2) {
      return false;
   }

   public boolean canSnowAtBody(BlockPos var1, boolean var2) {
      return false;
   }

   public SexWorldClient() {
      super(
         new ClientNetHandlerOverride(Minecraft.func_71410_x()),
         new WorldSettings(0L, GameType.SURVIVAL, false, false, WorldType.field_77138_c),
         0,
         EnumDifficulty.HARD,
         new Profiler()
      );
      this.field_73011_w.func_76558_a(this);
   }

   public boolean canMineBlockBody(EntityPlayer var1, BlockPos var2) {
      return false;
   }

   public boolean isSideSolid(BlockPos var1, EnumFacing var2) {
      return var1.func_177956_o() <= 63;
   }

   public boolean isSideSolid(BlockPos var1, EnumFacing var2, boolean var3) {
      return var1.func_177956_o() <= 63;
   }

   public int countEntities(EnumCreatureType var1, boolean var2) {
      return 0;
   }

}
