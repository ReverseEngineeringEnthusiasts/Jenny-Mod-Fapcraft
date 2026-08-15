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
   public Biome getBiomeForCoordsBody(BlockPos pos) {
      return new BiomePlains(false, new BiomeProperties("Plains").setBaseHeight(0.125F).setHeightVariation(0.05F).setHeightVariation(0.8F).setRainfall(0.4F));
   }

   public void notifyNeighborsOfStateChange(BlockPos pos, Block neighbor, boolean updateObservers) {
      super.notifyNeighborsOfStateChange(pos, neighbor, updateObservers);
   }

   public void markAndNotifyBlock(BlockPos pos, Chunk chunk, IBlockState oldState, IBlockState newState, int flags) {
   }

   public float getSunBrightnessFactor(float partialTicks) {
      return 1.0F;
   }

   @SideOnly(Side.CLIENT)
   public float getSunBrightnessBody(float partialTicks) {
      return 1.0F;
   }

   public void updateWeatherBody() {
   }

   public boolean canBlockFreezeBody(BlockPos pos, boolean doWater) {
      return false;
   }

   public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
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

   public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
      return false;
   }

   public boolean isSideSolid(BlockPos pos, EnumFacing side) {
      return pos.getY() <= 63;
   }

   public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean defaultValue) {
      return pos.getY() <= 63;
   }

   public int countEntities(EnumCreatureType type, boolean countSpawns) {
      return 0;
   }

}
