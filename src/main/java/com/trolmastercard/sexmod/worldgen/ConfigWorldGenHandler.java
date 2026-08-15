package com.trolmastercard.sexmod.worldgen;

import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Forge world-gen event handler that routes structure generation to the
 * configured generators (girl houses etc.) — see GirlHouseGenerator.
 */
public class ConfigWorldGenHandler extends WorldSavedData implements IWorldGenerator {
   static final String DATA_NAME = "sexmod:generation";
   static final int ELLIE_MIN_DISTANCE = 156;
   static final int DEFAULT_MIN_DISTANCE = 62;
   static final int spacing = 6;
   final double KOBOBLIN_SPAWN_CHANCE = 0.004F;
   public static boolean GENERATION_ENABLED = true;
   final List<ConfigWorldGenHandler.BiomeRule> girlHouseConfigs = new ArrayList<>();
   final List<ConfigWorldGenHandler.StructureData> generatedPositions = new ArrayList<>();
   private static ConfigWorldGenHandler INSTANCE = null;
   static boolean IS_GENERATING = true;

   public static ConfigWorldGenHandler getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ConfigWorldGenHandler();
      }

      return INSTANCE;
   }

   public ConfigWorldGenHandler(String name) {
      this();
   }

   private ConfigWorldGenHandler() {
      super("sexmod:generation");
      INSTANCE = this;
      this.girlHouseConfigs
         .add(
            new ConfigWorldGenHandler.BiomeRule(
               "ellie",
               new HashSet<>(Arrays.asList(Biomes.REDWOOD_TAIGA, Biomes.COLD_TAIGA, Biomes.TAIGA, Biomes.ROOFED_FOREST)),
               new Vec3i(30, 27, 26),
               9,
               true
            )
         );
      this.girlHouseConfigs.add(new ConfigWorldGenHandler.BiomeRule("jenny", new HashSet<>(Arrays.asList(Biomes.PLAINS, Biomes.FOREST)), new Vec3i(9, 4, 9), 1, true));
      this.girlHouseConfigs
         .add(
            new ConfigWorldGenHandler.BiomeRule(
               "ellie",
               new HashSet<>(Arrays.asList(Biomes.REDWOOD_TAIGA, Biomes.COLD_TAIGA, Biomes.TAIGA, Biomes.ROOFED_FOREST)),
               new Vec3i(30, 27, 26),
               9,
               true
            )
         );
      this.girlHouseConfigs.add(new ConfigWorldGenHandler.BiomeRule("bia", new HashSet<>(Arrays.asList(Biomes.MUTATED_BIRCH_FOREST, Biomes.BIRCH_FOREST)), new Vec3i(11, 9, 15), 2, true));
      this.girlHouseConfigs.add(new ConfigWorldGenHandler.BiomeRule("luna", new HashSet<>(Arrays.asList(Biomes.OCEAN, Biomes.DEEP_OCEAN)), new Vec3i(3, 7, 10), 0, false));
   }

   public void clear() {
      this.generatedPositions.clear();
   }

   @SubscribeEvent
   public void onSave(Save event) {
      World world = event.getWorld();
      world.getMapStorage().setData("sexmod:generation", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void onLoad(Load event) {
      World world = event.getWorld();
      world.getMapStorage().getOrLoadData(ConfigWorldGenHandler.class, "sexmod:generation");
   }

   public void readFromNBT(NBTTagCompound tag) {
      this.clear();
      NBTTagCompound generation = tag.getCompoundTag("sexmod:generation");
      int i = 0;

      while (true) {
         String name = generation.getString("sexmod:name" + i);
         String pos = generation.getString("sexmod:pos" + i);
         if ("".equals(name) || "".equals(pos)) {
            return;
         }

         this.generatedPositions.add(new ConfigWorldGenHandler.StructureData(parseSpawnEntry(pos), name));
         i++;
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound tag) {
      tag.setTag("sexmod:generation", new NBTTagCompound());
      NBTTagCompound generation = new NBTTagCompound();
      int i = 0;

      for (ConfigWorldGenHandler.StructureData data : this.generatedPositions) {
         generation.setString("sexmod:name" + i, data.girlName);
         generation.setString("sexmod:pos" + i++, getChunkHash(data.pos));
      }

      tag.setTag("sexmod:generation", generation);
      return tag;
   }

   static String getChunkHash(Point2D pos) {
      return pos.x + "|" + pos.y;
   }

   static Point2D parseSpawnEntry(String entry) {
      String[] parts = entry.split("\\|");
      return new Point2D(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
   }

   public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      if (GENERATION_ENABLED) {
         if (world.getWorldType() != WorldType.FLAT) {
            this.spawnStructures(world, random, chunkX, chunkZ);
            this.generateChunk(world, random, chunkX, chunkZ);
            this.generateStructure(random, chunkX, chunkZ, world);
         }
      }
   }

   void generateStructure(Random random, int chunkX, int chunkZ, World world) {
      if (IS_GENERATING) {
         IS_GENERATING = false;

         for (ConfigWorldGenHandler.BiomeRule rule : this.girlHouseConfigs) {
            this.placeStructure(rule, random, chunkX, chunkZ, world);
         }

         IS_GENERATING = true;
      }
   }

   void placeStructure(ConfigWorldGenHandler.BiomeRule rule, Random random, int chunkX, int chunkZ, World world) {
      for (ConfigWorldGenHandler.StructureData data : this.generatedPositions) {
         int minDistance = data.girlName.equals(rule.girlName) ? 156 : 62;
         if (data.pos.distanceTo(chunkX, chunkZ) < minDistance) {
            return;
         }
      }

      int sizeX = rule.size.getX();
      int sizeZ = rule.size.getZ();
      int startX = chunkX * 16 + (16 - sizeX) / 2;
      int startZ = chunkZ * 16 + (16 - sizeZ) / 2;
      Biome biome = world.provider.getBiomeForCoords(new BlockPos(startX, 80, startZ));
      if (rule.biomes.contains(biome)) {
         int maxHeight = Integer.MIN_VALUE;
         int minHeight = Integer.MAX_VALUE;

         for (int x = startX; x < startX + sizeX; x++) {
            for (int z = startZ; z < startZ + sizeZ; z++) {
               int height = WorldUtils.getHeightAt(world, x, z);
               if (rule.flattenGround && world.getBlockState(new BlockPos(x, height, z)).getBlock() == Blocks.WATER) {
                  return;
               }

               if (height > maxHeight) {
                  maxHeight = height;
               }

               if (height < minHeight) {
                  minHeight = height;
               }
            }
         }

         if (maxHeight - minHeight <= rule.maxHeightDiff) {
            int flatHeight = maxHeight;
            this.generatedPositions.add(new ConfigWorldGenHandler.StructureData(new Point2D(chunkX, chunkZ), rule.girlName));
            rule.generator.generate(world, random, new BlockPos(startX, flatHeight, startZ));
            if (rule.flattenGround) {
               boolean modified = true;

               for (int y = flatHeight - 1; modified; y--) {
                  modified = false;
                  Vec3i extents = new Vec3i(sizeX + 2, 0, sizeZ + 2);
                  startX--;
                  startZ--;

                  for (int x2 = startX; x2 < startX + extents.getX(); x2++) {
                     for (int z2 = startZ; z2 < startZ + extents.getZ(); z2++) {
                        BlockPos pos = new BlockPos(x2, y, z2);
                        IBlockState state = world.getBlockState(pos);
                        if (state.getBlock().isPassable(world, pos)) {
                           state = world.canSeeSky(pos) ? Blocks.GRASS.getDefaultState() : Blocks.DIRT.getDefaultState();
                           world.setBlockState(pos, state);
                           modified = true;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   void spawnStructures(World world, Random random, int chunkX, int chunkZ) {
      if (!(random.nextDouble() > 0.004F)) {
         int x = chunkX * 16 + 8;
         int z = chunkZ * 16 + 8;
         int height = WorldUtils.getHeightAt(world, x, z);
         if (!world.getBlockState(new BlockPos(x, height, z)).getMaterial().isLiquid()) {
            KoboldManager.spawnKoboldAt(world, new Vec3d(x, height, z));
         }
      }
   }

   void generateChunk(World world, Random random, int chunkX, int chunkZ) {
      int x = 16 * chunkX + 3;
      int z = 16 * chunkZ + 3;
      int y = random.nextInt(255);
      BlockPos origin = new BlockPos(x, y, z);
      ArrayList spots = new ArrayList();

      for (int x2 = 0; x2 <= GoblinEntity.ah.getX(); x2++) {
         for (int y2 = -1; y2 <= GoblinEntity.ah.getY(); y2++) {
            for (int z2 = 0; z2 <= GoblinEntity.ah.getZ(); z2++) {
               BlockPos pos = origin.add(x2, y2, z2);
               Material material = world.getBlockState(pos).getMaterial();
               boolean solid = material.isSolid();
               if (!solid && (y2 == -1 || y2 == GoblinEntity.ah.getY())) {
                  return;
               }

               if ((x2 == 0 || x2 == GoblinEntity.ah.getX() || z2 == 0 || z2 == GoblinEntity.ah.getZ())
                  && y2 == 0
                  && world.isAirBlock(pos)
                  && world.isAirBlock(pos.up())) {
                  spots.add(pos);
               }
            }
         }
      }

      if (spots.size() != 0 && spots.size() <= 4) {
         BlockPos candidate = null;

         for (BlockPos spot : (java.util.Collection<BlockPos>) (spots) ) {
            BlockPos relative = spot;
            BlockPos center = origin.add(6, 0, 6);
            relative = relative.subtract(center);
            if (Math.abs(relative.getX()) != Math.abs(relative.getZ())
               && Math.abs(relative.getX()) != Math.abs(relative.getZ()) - 1
               && Math.abs(relative.getX()) - 1 != Math.abs(relative.getZ())) {
               candidate = relative;
               break;
            }
         }

         if (candidate != null) {
            Vec3i offset = new Vec3i(0, 0, 0);
            float yaw = 0.0F;
            Rotation rotation;
            Vec3d offsetPos;
            if (candidate.getZ() == -6) {
               rotation = Rotation.NONE;
               offsetPos = GoblinEntity.aB;
               yaw = 180.0F;
            } else if (candidate.getX() == 5) {
               rotation = Rotation.CLOCKWISE_90;
               offsetPos = GoblinEntity.ao;
               offset = new Vec3i(GoblinEntity.ah.getX() - 1, 0, 0);
               yaw = -90.0F;
            } else if (candidate.getZ() == 5) {
               rotation = Rotation.CLOCKWISE_180;
               offsetPos = GoblinEntity.aM;
               offset = new Vec3i(GoblinEntity.ah.getX() - 1, 0, GoblinEntity.ah.getZ() - 1);
            } else {
               rotation = Rotation.COUNTERCLOCKWISE_90;
               offsetPos = GoblinEntity.THROW_OFFSET_U;
               offset = new Vec3i(0, 0, GoblinEntity.ah.getZ() - 1);
               yaw = 90.0F;
            }

            new GirlHouseGenerator("goblin").generateStructureRotated(world, origin.add(0, -1, 0).add(offset), rotation);
            offsetPos.add(offset.getX(), offset.getY(), offset.getZ());
            offsetPos = new Vec3d(
               origin.getX() + offsetPos.x + 0.5, origin.getY() + offsetPos.y, origin.getZ() + offsetPos.z + 0.5
            );
            GoblinEntity goblin = new GoblinEntity(world, true, yaw, offsetPos);
            goblin.forceSpawn = true;
            world.spawnEntity(goblin);
            world.getChunk(chunkX, chunkZ).markDirty();
         }
      }
   }

   static class StructureData {
      Point2D pos;
      String girlName;

      public StructureData(Point2D pos, String name) {
         this.pos = pos;
         this.girlName = name;
      }
   }

   static class BiomeRule {
      public final String girlName;
      public final GirlHouseGenerator generator;
      public final HashSet<Biome> biomes;
      public final Vec3i size;
      public final boolean flattenGround;
      public final int maxHeightDiff;

      public BiomeRule(String name, HashSet<Biome> biomes, Vec3i size, int maxHeightDiff, boolean flatten) {
         this.girlName = name;
         this.biomes = biomes;
         this.size = size;
         this.flattenGround = flatten;
         this.maxHeightDiff = maxHeightDiff;
         this.generator = new GirlHouseGenerator(name);
      }
   }
}
