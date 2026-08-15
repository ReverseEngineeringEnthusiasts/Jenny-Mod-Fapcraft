package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.Action;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * <b>Role.</b> A single tribe work order (MINE / FALL_TREE): target position,
 * facing, the set of blocks to break and the kobolds assigned as workers. Created
 * by {@link MinePacket}/{@link FallTreePacket} and executed by the kobold AI;
 * persisted by {@link KoboldManager.TribeWorldSavedData}.
 * <p>
 * <b>Capacity.</b> {@code TaskType} caps workers (FALL_TREE: 1, MINE: 3);
 * {@link #addWorker} and {@link #isFull} must agree on that cap.
 * <p>
 * <b>Pitfalls.</b> {@link #releaseWorkers} restores physics + anchor flags and
 * only skips kobolds that are themselves in an interaction. The static
 * {@link #findConnectedBlocks} flood-fills logs both horizontally and one level
 * up and registers the FALL_TREE task itself — callers must not double-register.
 */
public class KoboldTask {
   public static final int maxWorkers = 30;
   BlockPos targetPos;
   KoboldTask.TaskType taskType;
   HashSet<BlockPos> miningTargets;
   List<KoboldEntity> workers = new ArrayList<>();
   EnumFacing facing = EnumFacing.NORTH;

   public KoboldTask(BlockPos targetPos, KoboldTask.TaskType taskType, HashSet<BlockPos> miningTargets) {
      this.targetPos = targetPos;
      this.taskType = taskType;
      this.miningTargets = miningTargets;
   }

   public KoboldTask(BlockPos targetPos, KoboldTask.TaskType taskType, HashSet<BlockPos> miningTargets, EnumFacing facing) {
      this.targetPos = targetPos;
      this.taskType = taskType;
      this.miningTargets = miningTargets;
      this.facing = facing;
   }

   public EnumFacing getFacing() {
      return this.facing;
   }

   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   public KoboldTask.TaskType getTaskType() {
      return this.taskType;
   }

   public HashSet<BlockPos> getMiningTargets() {
      return this.miningTargets;
   }

   public void addMiningTarget(BlockPos pos) {
      this.miningTargets.add(pos);
   }

   public void addMiningTargets(HashSet<BlockPos> targets) {
      this.miningTargets.addAll(targets);
   }

   public void removeMiningTarget(BlockPos pos) {
      this.miningTargets.remove(pos);
   }

   public void setMiningTargets(HashSet<BlockPos> targets) {
      if (!targets.isEmpty()) {
         this.miningTargets.removeAll(targets);
      }
   }

   public boolean isMiningTarget(BlockPos pos) {
      return this.miningTargets.contains(pos);
   }

   public boolean addWorker(KoboldEntity kobold) {
      if (this.taskType.targetPos <= this.workers.size()) {
         return false;
      }

      this.workers.add(kobold);
      return true;
   }

   public List<KoboldEntity> getWorkers() {
      return this.workers;
   }

   public void releaseWorkers() {
      for (KoboldEntity worker : this.workers) {
         if (worker.getInteractionPlayerUUID() == null) {
            worker.setNoGravity(false);
            worker.noClip = false;
            worker.setCurrentAction(Action.NULL);
            worker.getDataManager().set(BaseGirlEntity.IS_ANCHORED, false);
         }
      }

      this.workers.clear();
   }

   public void removeWorker(KoboldEntity kobold) {
      this.workers.remove(kobold);
   }

   public boolean isFull() {
      return this.taskType.targetPos <= this.workers.size();
   }

   public boolean hasWorker(KoboldEntity kobold) {
      return this.workers.contains(kobold);
   }

   public static HashSet<BlockPos> findConnectedBlocks(World world, BlockPos startPos, UUID tribeUuid) {
      BlockPos groundPos = startPos;

      while (!isAboveMineable(world, groundPos)) {
         groundPos = startPos.down();
      }

      BlockPos topPos = startPos;

      while (!isMineable(world, topPos)) {
         topPos = topPos.up();
      }

      HashSet blocks = new HashSet();
      int height = topPos.getY() - groundPos.getY();

      for (int i = 0; i <= height; i++) {
         blocks.add(groundPos.add(0, i, 0));
      }

      HashSet logs = findConnectedLogs(world, groundPos);
      HashSet leafBlocks = new HashSet();

      for (BlockPos log : (java.util.Collection<BlockPos>) (logs) ) {
         if (log.getX() == groundPos.getX() && log.getZ() == groundPos.getZ()) {
            leafBlocks.add(log);
         }
      }

      for (BlockPos log2 : (java.util.Collection<BlockPos>) (leafBlocks) ) {
         logs.remove(log2);
      }

      blocks.addAll(logs);
      HashSet overlapping = new HashSet();

      for (BlockPos block : (java.util.Collection<BlockPos>) (blocks) ) {
         for (KoboldTask task : KoboldManager.getTribeTasks(tribeUuid)) {
            HashSet taskTargets = task.getMiningTargets();
            if (taskTargets.contains(block)) {
               overlapping.add(block);
               break;
            }
         }
      }

      blocks.removeAll(overlapping);
      KoboldTask fallTask = new KoboldTask(groundPos, KoboldTask.TaskType.FALL_TREE, blocks);
      KoboldManager.addTask(tribeUuid, fallTask);
      return blocks;
   }

   static boolean isMineable(World world, BlockPos pos) {
      Block blockAbove = world.getBlockState(pos.up()).getBlock();
      return !(blockAbove instanceof BlockLog);
   }

   static boolean isAboveMineable(World world, BlockPos pos) {
      IBlockState stateBelow = world.getBlockState(pos.down());
      return !(stateBelow instanceof BlockLog) && stateBelow.getMaterial() != Material.AIR;
   }

   static HashSet<BlockPos> findConnectedLogs(World world, BlockPos pos) {
      return findMineableBlocks(world, pos, new HashSet<>());
   }

   static HashSet<BlockPos> findMineableBlocks(World world, BlockPos pos, HashSet<BlockPos> visited) {
      if (visited.contains(pos)) {
         return new HashSet<>();
      }

      visited.add(pos);
      if (world.getBlockState(pos.add(1, 0, 0)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(1, 0, 0), visited));
      }

      if (world.getBlockState(pos.add(-1, 0, 0)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(-1, 0, 0), visited));
      }

      if (world.getBlockState(pos.add(0, 0, 1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(0, 0, 1), visited));
      }

      if (world.getBlockState(pos.add(0, 0, -1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(0, 0, -1), visited));
      }

      if (world.getBlockState(pos.add(1, 0, 1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(1, 0, 1), visited));
      }

      if (world.getBlockState(pos.add(-1, 0, -1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(-1, 0, -1), visited));
      }

      if (world.getBlockState(pos.add(-1, 0, 1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(-1, 0, 1), visited));
      }

      if (world.getBlockState(pos.add(1, 0, -1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(1, 0, -1), visited));
      }

      if (world.getBlockState(pos.add(0, 1, 0)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(0, 1, 0), visited));
      }

      if (world.getBlockState(pos.add(1, 1, 0)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(1, 1, 0), visited));
      }

      if (world.getBlockState(pos.add(-1, 1, 0)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(-1, 1, 0), visited));
      }

      if (world.getBlockState(pos.add(0, 1, 1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(0, 1, 1), visited));
      }

      if (world.getBlockState(pos.add(0, 1, -1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(0, 1, -1), visited));
      }

      if (world.getBlockState(pos.add(1, 1, 1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(1, 1, 1), visited));
      }

      if (world.getBlockState(pos.add(-1, 1, -1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(-1, 1, -1), visited));
      }

      if (world.getBlockState(pos.add(-1, 1, 1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(-1, 1, 1), visited));
      }

      if (world.getBlockState(pos.add(1, 1, -1)).getBlock() instanceof BlockLog) {
         visited.addAll(findMineableBlocks(world, pos.add(1, 1, -1), visited));
      }

      return visited;
   }

   public enum TaskType {
      FALL_TREE(1),
      MINE(3);

      int targetPos;

      TaskType(int id) {
         this.targetPos = id;
      }

      int getMaxWorkers() {
         return this.targetPos;
      }
   }
}
