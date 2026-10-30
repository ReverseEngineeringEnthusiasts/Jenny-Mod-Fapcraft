package com.trolmastercard.sexmod.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * Spiral block searches around a girl (nearest bed, nearest structure block,
 * all blocks of a class in a radius).
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}; the entity keeps its instance
 * methods as delegates, so subclass call sites are unaffected.
 */
final class GirlBlockScan {
   private GirlBlockScan() {
   }

   static BlockPos getNearestBed(BaseGirlEntity girl, BlockPos pos) {
      return findNearestBed(girl, pos, 1);
   }

   static BlockPos findNearestBed(BaseGirlEntity girl, BlockPos origin, int radius) {
      return findNearestStructureBlock(girl, origin, radius, Blocks.BED, 22, 3, null);
   }

   static BlockPos findNearestStructureBlock(BaseGirlEntity girl, BlockPos origin, int requiredCount, Block targetBlock, int maxRadius, int heightRange, @Nullable HashSet<Biome> allowedBiomes) {
      int step = 1;
      byte dir = -1;
      BlockPos pos = origin;
      int found = 0;

      while (step < maxRadius) {
         for (int i = 0; i < 2; i++) {
            dir *= -1;

            for (int j = 0; j < step; j++) {
               pos = pos.add(0, 0, dir);

               for (int h = -heightRange; h < heightRange + 1; h++) {
                  if (girl.world.getBlockState(pos.add(0, h, dir)).getBlock() == targetBlock) {
                     found++;
                     if (found >= requiredCount && (allowedBiomes == null || allowedBiomes.contains(girl.world.getBiome(pos.add(dir, h, 0))))) {
                        return pos.add(0, h, dir);
                     }
                  }
               }
            }

            for (int k = 0; k < step; k++) {
               pos = pos.add(dir, 0, 0);

               for (int h2 = -heightRange; h2 < heightRange + 1; h2++) {
                  if (girl.world.getBlockState(pos.add(dir, h2, 0)).getBlock() == targetBlock) {
                     found++;
                     if (found >= requiredCount && (allowedBiomes == null || allowedBiomes.contains(girl.world.getBiome(pos.add(dir, h2, 0))))) {
                        return pos.add(dir, h2, 0);
                     }
                  }
               }
            }

            step++;
         }
      }

      return null;
   }

   static List<BlockPos> findBlocksInRadius(BaseGirlEntity girl, BlockPos center, Class blockClass, int maxRadius, int height, @Nullable HashSet<Biome> biomes) {
      int radius = 1;
      byte step = -1;
      BlockPos base = center;
      ArrayList found = new ArrayList();

      while (radius < maxRadius) {
         for (int pass = 0; pass < 2; pass++) {
            step *= -1;

            for (int i = 0; i < radius; i++) {
               base = base.add(0, 0, step);

               for (int yOffset = -height; yOffset < height + 1; yOffset++) {
                  if (blockClass.isInstance(girl.world.getBlockState(base.add(0, yOffset, step)).getBlock())
                     && (biomes == null || biomes.contains(girl.world.getBiome(base.add(step, yOffset, 0))))) {
                     found.add(base.add(0, yOffset, step));
                  }
               }
            }

            for (int j = 0; j < radius; j++) {
               base = base.add(step, 0, 0);

               for (int zOffset = -height; zOffset < height + 1; zOffset++) {
                  if (blockClass.isInstance(girl.world.getBlockState(base.add(step, zOffset, 0)).getBlock())
                     && (biomes == null || biomes.contains(girl.world.getBiome(base.add(step, zOffset, 0))))) {
                     found.add(base.add(step, zOffset, 0));
                  }
               }
            }

            radius++;
         }
      }

      return found;
   }
}
