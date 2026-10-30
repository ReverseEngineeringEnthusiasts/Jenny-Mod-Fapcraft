package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.KoboldEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * <b>Role.</b> The block side of the kobold tribe system for
 * {@link KoboldManager}: pairing a bed with its other half, finding the chest
 * half a chest was placed/broken against, testing whether a bed is already
 * assigned to a kobold, gathering the blocks a tribe owns (beds, chests, every
 * task's mining targets) and clearing stale saved positions.
 * <p>
 * <b>Pitfalls.</b> The bed/chest scans check north, east, south and west in
 * order and keep the *last* match, so a bed touching several others pairs with
 * the western one. Block lookups run on the server thread only; the saved
 * position scan needs the position's area loaded and silently keeps positions
 * whose area is not loaded; positions whose kobold is gone are pruned.
 */
final class KoboldBlockLookup {
   private KoboldBlockLookup() {
   }

   static boolean isBedAssigned(Map<KoboldEntity, BlockPos[]> beds, BlockPos pos) {
      for (Entry entry : beds.entrySet()) {
         BlockPos[] bedPair = (BlockPos[])entry.getValue();
         if (bedPair[0].equals(pos)) {
            return true;
         }

         if (bedPair[1].equals(pos)) {
            return true;
         }
      }

      return false;
   }

   static BlockPos findBedHead(World world, BlockPos bedPos) {
      BlockPos headPos = null;
      if (world.getBlockState(bedPos.north()).getBlock() instanceof BlockBed) {
         headPos = bedPos.north();
      }

      if (world.getBlockState(bedPos.east()).getBlock() instanceof BlockBed) {
         headPos = bedPos.east();
      }

      if (world.getBlockState(bedPos.south()).getBlock() instanceof BlockBed) {
         headPos = bedPos.south();
      }

      if (world.getBlockState(bedPos.west()).getBlock() instanceof BlockBed) {
         headPos = bedPos.west();
      }

      return headPos;
   }

   static BlockPos findPairedChest(World world, BlockPos pos, BlockChest.Type chestType) {
      BlockPos pairedPos = null;
      if (world.getBlockState(pos.north()).getBlock() instanceof BlockChest
         && chestType.equals(((BlockChest)world.getBlockState(pos.north()).getBlock()).chestType)) {
         pairedPos = pos.north();
      }

      if (world.getBlockState(pos.east()).getBlock() instanceof BlockChest
         && chestType.equals(((BlockChest)world.getBlockState(pos.east()).getBlock()).chestType)) {
         pairedPos = pos.east();
      }

      if (world.getBlockState(pos.south()).getBlock() instanceof BlockChest
         && chestType.equals(((BlockChest)world.getBlockState(pos.south()).getBlock()).chestType)) {
         pairedPos = pos.south();
      }

      if (world.getBlockState(pos.west()).getBlock() instanceof BlockChest
         && chestType.equals(((BlockChest)world.getBlockState(pos.west()).getBlock()).chestType)) {
         pairedPos = pos.west();
      }

      return pairedPos;
   }

   static HashSet<BlockPos> collectTribeBlocks(Collection<KoboldTask> tasks, Set<BlockPos> beds, Set<BlockPos> chests) {
      HashSet blocks = new HashSet();

      for (KoboldTask task : tasks) {
         blocks.addAll(task.miningTargets);
      }

      blocks.addAll(beds);
      blocks.addAll(chests);
      return blocks;
   }

   static HashMap<UUID, BlockPos> scanSavedPositions(HashMap<UUID, BlockPos> savedPositions, World world) {
      ArrayList staleMemberIds = new ArrayList();

      for (Entry<UUID, BlockPos> entry : savedPositions.entrySet()) {
         BlockPos pos = entry.getValue();
         UUID memberUuid = entry.getKey();
         if (world.isAreaLoaded(pos, 5)) {
            AxisAlignedBB aabb = new AxisAlignedBB(pos.subtract(new Vec3i(-3, -3, -3)), pos.add(3, 3, 3));
            List koboldsNearby = world.getEntitiesWithinAABB(KoboldEntity.class, aabb);
            boolean hasKobold = false;

            for (KoboldEntity kobold : (java.util.Collection<KoboldEntity>) (koboldsNearby) ) {
               if (memberUuid.equals(kobold.getGirlId())) {
                  hasKobold = true;
                  break;
               }
            }

            if (!hasKobold) {
               staleMemberIds.add(memberUuid);
            }
         }
      }

      if (!staleMemberIds.isEmpty()) {
         savedPositions.keySet().removeAll(staleMemberIds);
      }

      return savedPositions;
   }
}
