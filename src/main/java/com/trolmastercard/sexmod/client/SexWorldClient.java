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

/**
 * Client-only throwaway "preload" world, a flat plains world that is created
 * exclusively in {@code ClientProxy.initRegistries} (the {@code NpcType} loop)
 * so geckolib models/animation data can be loaded once at startup.
 * <p>
 * <b>It is NOT the scene world.</b> Nothing is ever spawned or rendered in it;
 * its only job is to be a valid {@link WorldClient} for model construction.
 * Overrides deliberately neuter gameplay: no weather, no block updates, no
 * freezing/snowing, solid ground only at {@code y <= 63}, zero creature counts,
 * full sun brightness, biome always plains, mining always forbidden.
 * <p>
 * <b>Pitfall:</b> entity code checks {@code world instanceof SexWorldClient}
 * to skip animation/preview logic — keep this type check working. Any
 * constructor change must keep the {@link ClientNetHandlerOverride} +
 * {@link SexNetworkManager} stub wiring intact, otherwise client startup
 * crashes while building the preload world.
 */
public class SexWorldClient extends WorldClient {
   public Biome getBiomeForCoordsBody(BlockPos var1) {
      return new BiomePlains(false, new BiomeProperties("Plains").setBaseHeight(0.125F).setHeightVariation(0.05F).setHeightVariation(0.8F).setRainfall(0.4F));
   }

   public void notifyNeighborsOfStateChange(BlockPos var1, Block var2, boolean var3) {
      super.notifyNeighborsOfStateChange(var1, var2, var3);
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
         new ClientNetHandlerOverride(Minecraft.getMinecraft()),
         new WorldSettings(0L, GameType.SURVIVAL, false, false, WorldType.FLAT),
         0,
         EnumDifficulty.HARD,
         new Profiler()
      );
      this.provider.setWorld(this);
   }

   public boolean canMineBlockBody(EntityPlayer var1, BlockPos var2) {
      return false;
   }

   public boolean isSideSolid(BlockPos var1, EnumFacing var2) {
      return var1.getY() <= 63;
   }

   public boolean isSideSolid(BlockPos var1, EnumFacing var2, boolean var3) {
      return var1.getY() <= 63;
   }

   public int countEntities(EnumCreatureType var1, boolean var2) {
      return 0;
   }

}
