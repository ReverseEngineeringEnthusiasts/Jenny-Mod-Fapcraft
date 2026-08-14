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

public class KoboldTask {
   public static final int maxWorkers = 30;
   BlockPos targetPos;
   KoboldTask.TaskType taskType;
   HashSet<BlockPos> miningTargets;
   List<KoboldEntity> workers = new ArrayList<>();
   EnumFacing facing = EnumFacing.NORTH;

   public KoboldTask(BlockPos var1, KoboldTask.TaskType var2, HashSet<BlockPos> var3) {
      this.targetPos = var1;
      this.taskType = var2;
      this.miningTargets = var3;
   }

   public KoboldTask(BlockPos var1, KoboldTask.TaskType var2, HashSet<BlockPos> var3, EnumFacing var4) {
      this.targetPos = var1;
      this.taskType = var2;
      this.miningTargets = var3;
      this.facing = var4;
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

   public void addMiningTarget(BlockPos var1) {
      this.miningTargets.add(var1);
   }

   public void addMiningTargets(HashSet<BlockPos> var1) {
      this.miningTargets.addAll(var1);
   }

   public void removeMiningTarget(BlockPos var1) {
      this.miningTargets.remove(var1);
   }

   public void setMiningTargets(HashSet<BlockPos> var1) {
      if (!var1.isEmpty()) {
         this.miningTargets.removeAll(var1);
      }
   }

   public boolean isMiningTarget(BlockPos var1) {
      return this.miningTargets.contains(var1);
   }

   public boolean addWorker(KoboldEntity var1) {
      if (this.taskType.targetPos <= this.workers.size()) {
         return false;
      }

      this.workers.add(var1);
      return true;
   }

   public List<KoboldEntity> getWorkers() {
      return this.workers;
   }

   public void releaseWorkers() {
      for (KoboldEntity var2 : this.workers) {
         if (var2.getInteractionPlayerUUID() == null) {
            var2.setNoGravity(false);
            var2.noClip = false;
            var2.setCurrentAction(Action.NULL);
            var2.getDataManager().set(BaseGirlEntity.IS_ANCHORED, false);
         }
      }

      this.workers.clear();
   }

   public void removeWorker(KoboldEntity var1) {
      this.workers.remove(var1);
   }

   public boolean isFull() {
      return this.taskType.targetPos <= this.workers.size();
   }

   public boolean hasWorker(KoboldEntity var1) {
      return this.workers.contains(var1);
   }

   public static HashSet<BlockPos> a(World var0, BlockPos var1, UUID var2) {
      BlockPos var3 = var1;

      while (!isAboveMineable(var0, var3)) {
         var3 = var1.down();
      }

      BlockPos var4 = var1;

      while (!isMineable(var0, var4)) {
         var4 = var4.up();
      }

      HashSet var5 = new HashSet();
      int var6 = var4.getY() - var3.getY();

      for (int var7 = 0; var7 <= var6; var7++) {
         var5.add(var3.add(0, var7, 0));
      }

      HashSet var15 = findConnectedLogs(var0, var3);
      HashSet var8 = new HashSet();

      for (BlockPos var10 : (java.util.Collection<BlockPos>) (var15) ) {
         if (var10.getX() == var3.getX() && var10.getZ() == var3.getZ()) {
            var8.add(var10);
         }
      }

      for (BlockPos var18 : (java.util.Collection<BlockPos>) (var8) ) {
         var15.remove(var18);
      }

      var5.addAll(var15);
      HashSet var17 = new HashSet();

      for (BlockPos var11 : (java.util.Collection<BlockPos>) (var5) ) {
         for (KoboldTask var13 : KoboldManager.getTribeTasks(var2)) {
            HashSet var14 = var13.getMiningTargets();
            if (var14.contains(var11)) {
               var17.add(var11);
               break;
            }
         }
      }

      var5.removeAll(var17);
      KoboldTask var20 = new KoboldTask(var3, KoboldTask.TaskType.FALL_TREE, var5);
      KoboldManager.addTask(var2, var20);
      return var5;
   }

   static boolean isMineable(World var0, BlockPos var1) {
      Block var2 = var0.getBlockState(var1.up()).getBlock();
      return !(var2 instanceof BlockLog);
   }

   static boolean isAboveMineable(World var0, BlockPos var1) {
      IBlockState var2 = var0.getBlockState(var1.down());
      return !(var2 instanceof BlockLog) && var2.getMaterial() != Material.AIR;
   }

   static HashSet<BlockPos> findConnectedLogs(World var0, BlockPos var1) {
      return a(var0, var1, new HashSet<>());
   }

   static HashSet<BlockPos> a(World var0, BlockPos var1, HashSet<BlockPos> var2) {
      if (var2.contains(var1)) {
         return new HashSet<>();
      }

      var2.add(var1);
      if (var0.getBlockState(var1.add(1, 0, 0)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(1, 0, 0), var2));
      }

      if (var0.getBlockState(var1.add(-1, 0, 0)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(-1, 0, 0), var2));
      }

      if (var0.getBlockState(var1.add(0, 0, 1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(0, 0, 1), var2));
      }

      if (var0.getBlockState(var1.add(0, 0, -1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(0, 0, -1), var2));
      }

      if (var0.getBlockState(var1.add(1, 0, 1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(1, 0, 1), var2));
      }

      if (var0.getBlockState(var1.add(-1, 0, -1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(-1, 0, -1), var2));
      }

      if (var0.getBlockState(var1.add(-1, 0, 1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(-1, 0, 1), var2));
      }

      if (var0.getBlockState(var1.add(1, 0, -1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(1, 0, -1), var2));
      }

      if (var0.getBlockState(var1.add(0, 1, 0)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(0, 1, 0), var2));
      }

      if (var0.getBlockState(var1.add(1, 1, 0)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(1, 1, 0), var2));
      }

      if (var0.getBlockState(var1.add(-1, 1, 0)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(-1, 1, 0), var2));
      }

      if (var0.getBlockState(var1.add(0, 1, 1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(0, 1, 1), var2));
      }

      if (var0.getBlockState(var1.add(0, 1, -1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(0, 1, -1), var2));
      }

      if (var0.getBlockState(var1.add(1, 1, 1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(1, 1, 1), var2));
      }

      if (var0.getBlockState(var1.add(-1, 1, -1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(-1, 1, -1), var2));
      }

      if (var0.getBlockState(var1.add(-1, 1, 1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(-1, 1, 1), var2));
      }

      if (var0.getBlockState(var1.add(1, 1, -1)).getBlock() instanceof BlockLog) {
         var2.addAll(a(var0, var1.add(1, 1, -1), var2));
      }

      return var2;
   }

   public enum TaskType {
      FALL_TREE(1),
      MINE(3);

      int targetPos;

      TaskType(int var3) {
         this.targetPos = var3;
      }

      int getMaxWorkers() {
         return this.targetPos;
      }
   }
}
