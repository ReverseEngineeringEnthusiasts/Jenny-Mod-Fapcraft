package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetControllerPacket;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.KoboldTask;
import com.trolmastercard.sexmod.util.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

/**
 * Mining and tree-felling behaviour for {@link KoboldEntity}: picking a block out
 * of the tribe task, walking to it, standing still long enough to start, then
 * destroying it (including the falling-log cascade and sapling replant).
 * <p>
 * <b>State lives on the entity</b> ({@code miningTargetPos} target, {@code ad} replant item,
 * {@code mineCooldown} attempts, {@code cooldownTicks}, {@code taskTimer}, {@code miningCooldown},
 * {@code miningStandTicks}) because the tribe task loop in {@code KoboldEntity}
 * reads {@code miningTargetPos} too. The methods here are stateless and take the entity as
 * their first argument.
 */
final class KoboldMining {
   private KoboldMining() {
   }

   static void handleTaskState(KoboldEntity entity, UUID tribeId, KoboldTask task) {
      if (entity.getCurrentAction() != Action.MINE) {
         handleTaskNavigation(entity, tribeId, task);
      } else {
         entity.taskTimer--;
         entity.miningCooldown--;
         if (entity.miningCooldown == 0) {
            IBlockState fallingState = entity.world.getBlockState(entity.miningTargetPos.up());
            if (!(fallingState.getBlock() instanceof BlockFalling)) {
               task.removeMiningTarget(entity.miningTargetPos);
               EntityPlayer master = entity.getMasterPlayer();
               if (master != null) {
                  PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(entity.miningTargetPos, false), (EntityPlayerMP)master);
               }
            }

            IBlockState blockState = entity.world.getBlockState(entity.miningTargetPos);
            entity.canExtractItem(new ItemStack(blockState.getBlock().getItemDropped(blockState, entity.getRNG(), 0), 1, blockState.getBlock().damageDropped(blockState)));
            entity.world.destroyBlock(entity.miningTargetPos, false);
         }

         if (entity.taskTimer <= 0) {
            entity.taskTimer = 100;
            entity.miningCooldown = 24;
            entity.setCurrentAction(Action.NULL);
         }
      }
   }

   static void handleTaskNavigation(KoboldEntity entity, UUID tribeId, KoboldTask task) {
      PathNavigate navigator = entity.getNavigator();
      if (entity.miningTargetPos != null && task.getMiningTargets().contains(entity.miningTargetPos)) {
         IBlockState blockState = entity.world.getBlockState(entity.miningTargetPos);
         if (!entity.canInsertItem(new ItemStack(blockState.getBlock().getItemDropped(blockState, Reference.RANDOM, 0)))) {
            entity.tribeBlocked = true;
            entity.canStoreInventory(tribeId, true);
         } else if (entity.motionX == 0.0
            && entity.motionZ == 0.0
            && entity.onGround
            && !(entity.getDistance(entity.miningTargetPos.getX(), entity.miningTargetPos.getY(), entity.miningTargetPos.getZ()) > 3.0)
            && ++entity.miningStandTicks >= 10) {
            navigator.clearPath();
            entity.miningStandTicks = 0;
            entity.setCurrentAction(Action.MINE);
            entity.rotationYawHead = (float)(
               Math.atan2(entity.posZ - entity.miningTargetPos.getZ(), entity.posX - entity.miningTargetPos.getX()) * (180.0 / Math.PI) + 90.0
            );
            entity.rotationYaw = entity.rotationYawHead;
            entity.getDataManager().set(KoboldEntity.MINING_WITH_AXE, false);
         } else {
            BlockPos standPos = entity.miningTargetPos.add(task.getFacing().getOpposite().getDirectionVec());
            navigator.tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
         }
      } else {
         entity.miningTargetPos = executeMiningTask(entity, task, tribeId);
         if (entity.miningTargetPos == null) {
            boolean noTargets = task.getMiningTargets().isEmpty();
            HashSet blocks = KoboldManager.removeTaskAndGetBlocks(tribeId, task);
            UUID tribeUuid = KoboldManager.findTribeIdWith(tribeId);
            if (tribeUuid != null) {
               EntityPlayer player = entity.world.getPlayerEntityByUUID(tribeUuid);
               if (player != null) {
                  if (!noTargets) {
                     player.sendMessage(new TextComponentString(String.format("<%s> It's impossible to mine here...", entity.getDisplayNameText())));
                  }

                  PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, false), (EntityPlayerMP)player);
               }
            }
         } else {
            if (Math.abs(entity.getPosition().getY() - task.getTargetPos().getY()) > 3) {
               BlockPos targetPos = task.getTargetPos().add(task.getFacing().getOpposite().getDirectionVec());
               entity.world.setBlockState(targetPos, Blocks.AIR.getDefaultState());
               entity.syncTribeBlocks(targetPos);
            }

            BlockPos standPos = entity.miningTargetPos.add(task.getFacing().getOpposite().getDirectionVec());
            navigator.tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
         }
      }
   }

   static BlockPos executeMiningTask(KoboldEntity entity, KoboldTask task, UUID tribeId) {
      HashSet miningTargets = task.getMiningTargets();
      EnumFacing facing = task.getFacing();
      ArrayList row = new ArrayList();
      Integer maxZ = null;
      if (miningTargets.isEmpty()) {
         return null;
      }

      for (BlockPos target : (java.util.Collection<BlockPos>) (miningTargets) ) {
         switch (facing) {
            case NORTH:
               if (maxZ == null || target.getZ() >= maxZ) {
                  maxZ = target.getZ();
                  row.add(target);
               }
               break;
            case SOUTH:
               if (maxZ == null || target.getZ() <= maxZ) {
                  maxZ = target.getZ();
                  row.add(target);
               }
               break;
            case EAST:
               if (maxZ == null || target.getX() <= maxZ) {
                  maxZ = target.getX();
                  row.add(target);
               }
               break;
            case WEST:
               if (maxZ == null || target.getX() >= maxZ) {
                  maxZ = target.getX();
                  row.add(target);
               }
         }
      }

      ArrayList column = new ArrayList();

      for (BlockPos pos : (java.util.Collection<BlockPos>) (row) ) {
         if ((facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) && pos.getZ() == maxZ) {
            column.add(pos);
         }

         if ((facing == EnumFacing.EAST || facing == EnumFacing.WEST) && pos.getX() == maxZ) {
            column.add(pos);
         }
      }

      if (column.isEmpty()) {
         return null;
      }

      ArrayList positions = new ArrayList();
      EnumFacing taskFacing = task.getFacing();
      BlockPos targetPos = task.getTargetPos();
      BlockPos minePos;
      if (taskFacing.getAxis() == Axis.Z) {
         minePos = new BlockPos(targetPos.getX(), targetPos.getY(), ((BlockPos)column.get(0)).getZ());
         if (taskFacing == EnumFacing.NORTH) {
            minePos = minePos.north();
         } else {
            minePos = minePos.south();
         }

         positions.add(minePos.down());
         positions.add(minePos.down().east());
         positions.add(minePos.down().west());
         positions.add(minePos);
         positions.add(minePos.up());
         positions.add(minePos.up().up());
         positions.add(minePos.up().up().up());
         positions.add(minePos.west());
         positions.add(minePos.west().up());
         positions.add(minePos.west().up().up());
         positions.add(minePos.west().up().up().up());
         positions.add(minePos.west().west());
         positions.add(minePos.west().west().up());
         positions.add(minePos.west().west().up().up());
         positions.add(minePos.east());
         positions.add(minePos.east().up());
         positions.add(minePos.east().up().up());
         positions.add(minePos.east().up().up().up());
         positions.add(minePos.east().east());
         positions.add(minePos.east().east().up());
         positions.add(minePos.east().east().up().up());
      } else {
         minePos = new BlockPos(((BlockPos)column.get(0)).getX(), targetPos.getY(), targetPos.getZ());
         if (taskFacing == EnumFacing.EAST) {
            minePos = minePos.east();
         } else {
            minePos = minePos.west();
         }

         positions.add(minePos.down());
         positions.add(minePos.down().north());
         positions.add(minePos.down().south());
         positions.add(minePos);
         positions.add(minePos.up());
         positions.add(minePos.up().up());
         positions.add(minePos.up().up().up());
         positions.add(minePos.south());
         positions.add(minePos.south().up());
         positions.add(minePos.south().up().up());
         positions.add(minePos.south().up().up().up());
         positions.add(minePos.south().south());
         positions.add(minePos.south().south().up());
         positions.add(minePos.south().south().up().up());
         positions.add(minePos.north());
         positions.add(minePos.north().up());
         positions.add(minePos.north().up().up());
         positions.add(minePos.north().up().up().up());
         positions.add(minePos.north().north());
         positions.add(minePos.north().north().up());
         positions.add(minePos.north().north().up().up());
      }

      HashSet liquidBlocks = new HashSet();

      for (BlockPos pos : (java.util.Collection<BlockPos>) (positions) ) {
         if (entity.world.getBlockState(pos).getMaterial().isLiquid()) {
            entity.world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState(), 2);
            if (column.contains(pos)) {
               liquidBlocks.add(pos);
            }
         }
      }

      if (!liquidBlocks.isEmpty()) {
         task.addMiningTargets(liquidBlocks);
         EntityPlayer master = entity.getMasterPlayer();
         if (master != null) {
            PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(liquidBlocks, true), (EntityPlayerMP)master);
         }
      }

      positions.clear();
      positions.add(minePos.down());
      if (taskFacing.getAxis() == Axis.Z) {
         positions.add(minePos.down().west());
         positions.add(minePos.down().east());
      } else {
         positions.add(minePos.down().north());
         positions.add(minePos.down().south());
      }

      for (BlockPos pos : (java.util.Collection<BlockPos>) (positions) ) {
         if (entity.world.getBlockState(pos).getBlock().isPassable(entity.world, pos)) {
            entity.world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
         }
      }

      HashSet airBlocks = new HashSet();

      for (BlockPos pos : (java.util.Collection<BlockPos>) (column) ) {
         Block block = entity.world.getBlockState(pos).getBlock();
         if (block == Blocks.AIR) {
            airBlocks.add(pos);
         }
      }

      if (!airBlocks.isEmpty()) {
         column.removeAll(airBlocks);
         task.setMiningTargets(airBlocks);
         UUID tribeUuid = KoboldManager.findTribeIdWith(tribeId);
         if (tribeUuid != null) {
            EntityPlayer player = entity.world.getPlayerEntityByUUID(tribeUuid);
            if (player != null) {
               PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(airBlocks, false), (EntityPlayerMP)player);
            }
         }
      }

      if (column.isEmpty()) {
         return executeMiningTask(entity, task, tribeId);
      }

      BlockPos workerMinePos = null;
      List workers = task.getWorkers();

      for (int i = 0; i < workers.size(); i++) {
         if (((KoboldEntity)workers.get(i)).getEntityId() == entity.getEntityId()) {
            if (i == 0) {
               workerMinePos = findTaskBlock(entity, column, -1, task.getFacing(), task.getTargetPos());
               if (workerMinePos == null) {
                  workerMinePos = findTaskBlock(entity, column, 0, task.getFacing(), task.getTargetPos());
                  if (workerMinePos == null) {
                     workerMinePos = findTaskBlock(entity, column, 1, task.getFacing(), task.getTargetPos());
                  }
               }
               break;
            }

            if (i == 1) {
               workerMinePos = findTaskBlock(entity, column, 1, task.getFacing(), task.getTargetPos());
               if (workerMinePos == null) {
                  workerMinePos = findTaskBlock(entity, column, 0, task.getFacing(), task.getTargetPos());
                  if (workerMinePos == null) {
                     workerMinePos = findTaskBlock(entity, column, -1, task.getFacing(), task.getTargetPos());
                  }
               }
               break;
            }

            if (i == 2) {
               workerMinePos = findTaskBlock(entity, column, 0, task.getFacing(), task.getTargetPos());
               if (workerMinePos == null) {
                  workerMinePos = findTaskBlock(entity, column, 1, task.getFacing(), task.getTargetPos());
                  if (workerMinePos == null) {
                     workerMinePos = findTaskBlock(entity, column, -1, task.getFacing(), task.getTargetPos());
                  }
               }
               break;
            }
         }
      }

      return workerMinePos;
   }

   static BlockPos findTaskBlock(KoboldEntity entity, List<BlockPos> blocks, int index, EnumFacing facing, BlockPos targetPos) {
      if (blocks.isEmpty()) {
         return null;
      }

      ArrayList extraBlocks = new ArrayList();
      ArrayList sideBlocks = new ArrayList();
      ArrayList rowBlocks = new ArrayList();
      int dir = facing != EnumFacing.SOUTH && facing != EnumFacing.WEST ? 1 : -1;
      if (facing.getAxis() == Axis.Z) {
         BlockPos rowAnchor = new BlockPos(targetPos.getX(), targetPos.getY(), ((BlockPos)blocks.get(0)).getZ());
         rowBlocks.add(rowAnchor);
         rowBlocks.add(rowAnchor.up());
         rowBlocks.add(rowAnchor.up().up());
         rowBlocks.add(rowAnchor.west());
         rowBlocks.add(rowAnchor.west().up());
         rowBlocks.add(rowAnchor.west().up().up());
         rowBlocks.add(rowAnchor.east());
         rowBlocks.add(rowAnchor.east().up());
         rowBlocks.add(rowAnchor.east().up().up());
         if (index == 0) {
            for (BlockPos rowPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(rowPos.east(2));
               sideBlocks.add(rowPos.east(-2));
            }

            for (BlockPos blockPos : blocks) {
               if (!sideBlocks.contains(blockPos)) {
                  extraBlocks.add(blockPos);
               }
            }
         } else {
            for (BlockPos rowPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(rowPos.east(dir * 2 * index));
            }

            for (BlockPos sidePos : (java.util.Collection<BlockPos>) (sideBlocks) ) {
               if (blocks.contains(sidePos)) {
                  extraBlocks.add(sidePos);
               }
            }
         }
      }

      if (facing.getAxis() == Axis.X) {
         BlockPos colAnchor = new BlockPos(((BlockPos)blocks.get(0)).getX(), targetPos.getY(), targetPos.getZ());
         rowBlocks.add(colAnchor);
         rowBlocks.add(colAnchor.up());
         rowBlocks.add(colAnchor.up().up());
         rowBlocks.add(colAnchor.north());
         rowBlocks.add(colAnchor.north().up());
         rowBlocks.add(colAnchor.north().up().up());
         rowBlocks.add(colAnchor.south());
         rowBlocks.add(colAnchor.south().up());
         rowBlocks.add(colAnchor.south().up().up());
         if (index == 0) {
            for (BlockPos colPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(colPos.south(2));
               sideBlocks.add(colPos.south(-2));
            }

            for (BlockPos blockPos : blocks) {
               if (!sideBlocks.contains(blockPos)) {
                  extraBlocks.add(blockPos);
               }
            }
         } else {
            for (BlockPos colPos : (java.util.Collection<BlockPos>) (rowBlocks) ) {
               sideBlocks.add(colPos.south(dir * 2 * index));
            }

            for (BlockPos sidePos : (java.util.Collection<BlockPos>) (sideBlocks) ) {
               if (blocks.contains(sidePos)) {
                  extraBlocks.add(sidePos);
               }
            }
         }
      }

      return extraBlocks.isEmpty() ? null : (BlockPos)extraBlocks.get(entity.getRNG().nextInt(extraBlocks.size()));
   }

   static void navigateToTask(KoboldEntity entity, UUID tribeId, KoboldTask task, BlockPos pos) {
      if (entity.miningDropItem == null) {
         entity.mineCooldown = 24;
         entity.cooldownTicks = 0;
         entity.setCurrentAction(Action.NULL);
         entity.getDataManager().set(KoboldEntity.IS_ANCHORED, false);
         EntityPlayer master = entity.getMasterPlayer();
         HashSet blocks = task.getMiningTargets();
         if (master != null && !blocks.isEmpty()) {
            PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, false), (EntityPlayerMP)master);
         }

         KoboldManager.setLeaderKobold(tribeId, entity);
      } else {
         switch (entity.miningDropItem.getMetadata()) {
            case 3:
            case 5:
               entity.world
                  .setBlockState(
                     pos,
                     Blocks.SAPLING
                        .getStateForPlacement(
                           entity.world,
                           pos,
                           EnumFacing.NORTH,
                           pos.getX(),
                           pos.getY(),
                           pos.getZ(),
                           entity.miningDropItem.getMetadata(),
                           entity,
                           EnumHand.MAIN_HAND
                        )
                  );
               entity.world
                  .setBlockState(
                     pos.north(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           entity.world,
                           pos.north(),
                           EnumFacing.NORTH,
                           pos.getX(),
                           pos.getY(),
                           pos.getZ() + 1,
                           entity.miningDropItem.getMetadata(),
                           entity,
                           EnumHand.MAIN_HAND
                        )
                  );
               entity.world
                  .setBlockState(
                     pos.west(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           entity.world,
                           pos.west(),
                           EnumFacing.NORTH,
                           pos.getX() + 1,
                           pos.getY(),
                           pos.getZ(),
                           entity.miningDropItem.getMetadata(),
                           entity,
                           EnumHand.MAIN_HAND
                        )
                  );
               entity.world
                  .setBlockState(
                     pos.north().west(),
                     Blocks.SAPLING
                        .getStateForPlacement(
                           entity.world,
                           pos.north().west(),
                           EnumFacing.NORTH,
                           pos.getX() + 1,
                           pos.getY(),
                           pos.getZ() + 1,
                           entity.miningDropItem.getMetadata(),
                           entity,
                           EnumHand.MAIN_HAND
                        )
                  );
               break;
            default:
               entity.world
                  .setBlockState(
                     pos,
                     Blocks.SAPLING
                        .getStateForPlacement(
                           entity.world,
                           pos,
                           EnumFacing.NORTH,
                           pos.getX(),
                           pos.getY(),
                           pos.getZ(),
                           entity.miningDropItem.getMetadata(),
                           entity,
                           EnumHand.MAIN_HAND
                        )
                  );
         }

         entity.mineCooldown = 24;
         entity.cooldownTicks = 0;
         entity.miningDropItem = null;
         entity.setCurrentAction(Action.NULL);
         entity.setAnchored(false);
         EntityPlayer master = entity.getMasterPlayer();
         HashSet blocks = task.getMiningTargets();
         if (master != null && !blocks.isEmpty()) {
            PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, false), (EntityPlayerMP)master);
         }

         KoboldManager.setLeaderKobold(tribeId, entity);
      }
   }

   static void startMiningTask(KoboldEntity entity, UUID tribeId, BlockPos pos, KoboldTask task) {
      if (entity.getCurrentAction() != Action.MINE) {
         mineBlockAt(entity, pos, tribeId);
      } else {
         entity.cooldownTicks--;
         if (entity.cooldownTicks <= 0) {
            if (entity.cooldownTicks == 0) {
               PacketHandler.networkWrapper.sendToAllAround(new ResetControllerPacket(entity.getGirlId()), entity.getTargetNetworkPoint());
            }

            if (entity.world.getBlockState(pos).getBlock() == Blocks.AIR) {
               navigateToTask(entity, tribeId, task, pos);
            } else {
               entity.mineCooldown--;
               if (entity.mineCooldown < 0) {
                  entity.mineCooldown = 24;
                  entity.cooldownTicks = 78;
                  HashSet targets = new HashSet();
                  EntityPlayer master = entity.getMasterPlayer();

                  for (BlockPos target : task.getMiningTargets()) {
                     if (entity.world.getBlockState(target).getBlock() != Blocks.AIR) {
                        if (target.getX() != pos.getX() || target.getZ() != pos.getZ()) {
                           try {
                              ItemStack dropStack = entity.world
                                 .getBlockState(target)
                                 .getBlock()
                                 .getItem(entity.world, target, entity.world.getBlockState(target));
                              if (dropStack.getItem() != Items.AIR) {
                                 entity.canExtractItem(dropStack);
                              }
                           } catch (IllegalArgumentException ex) {
                              Main.LOGGER
                                 .error(
                                    "Couldn't get an item out of the block that a kobold just destroyed when falling a tree. As a result, the block wasn't added into the kobolds inventory. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
                                 );
                              Main.LOGGER.warn("block in question: " + entity.world.getBlockState(target).getBlock().getTranslationKey());
                              Main.LOGGER.error(ex.getMessage());
                           }

                           entity.miningDropItem = getBlockItem(entity, target);
                           entity.world.destroyBlock(target, false);
                           task.removeMiningTarget(target);
                           targets.add(target);
                           task.setMiningTargets(targets);
                           if (master != null) {
                              PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(targets, false), (EntityPlayerMP)master);
                           }

                           return;
                        }
                     } else {
                        targets.add(target);
                     }
                  }

                  try {
                     ItemStack dropStack = entity.world
                        .getBlockState(pos)
                        .getBlock()
                        .getItem(entity.world, pos, entity.world.getBlockState(pos));
                     if (dropStack.getItem() != Items.AIR) {
                        entity.canExtractItem(dropStack);
                     }
                  } catch (IllegalArgumentException ex) {
                     Main.LOGGER
                        .error(
                           "Couldn't get an item out of the block that a kobold just destroyed when falling a tree. As a result, the block wasn't added into the kobolds inventory. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
                        );
                     Main.LOGGER.warn("block in question: " + entity.world.getBlockState(pos).getBlock().getTranslationKey());
                     Main.LOGGER.error(ex.getMessage());
                  }

                  entity.miningDropItem = getBlockItem(entity, pos);
                  entity.world.destroyBlock(pos, false);
                  int logCount = 0;

                  for (BlockPos target : task.getMiningTargets()) {
                     if (entity.world.getBlockState(target).getBlock() instanceof BlockLog) {
                        logCount++;
                     }
                  }

                  HashSet trunkBlocks = new HashSet();

                  for (int i = 0; i < logCount; i++) {
                     trunkBlocks.add(pos.add(0, i, 0));
                  }

                  HashSet otherBlocks = new HashSet();

                  for (BlockPos target : task.getMiningTargets()) {
                     if (!trunkBlocks.contains(target)) {
                        otherBlocks.add(target);
                     }
                  }

                  if (!otherBlocks.isEmpty() && master != null) {
                     PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(otherBlocks, false), (EntityPlayerMP)master);
                  }

                  int height = 1;

                  while (true) {
                     BlockPos logPos = pos.add(0, height, 0);
                     IBlockState logState = entity.world.getBlockState(logPos);
                     if (entity.world.getBlockState(logPos).getBlock() instanceof BlockLog) {
                        entity.world.destroyBlock(logPos, false);
                        EntityFallingBlock fallingBlock = new EntityFallingBlock(
                           entity.world, logPos.getX() + 0.5, logPos.getY(), logPos.getZ() + 0.5, logState
                        );
                        fallingBlock.fallTime = 1;
                        entity.world.spawnEntity(fallingBlock);
                     }

                     if (!task.getMiningTargets().contains(logPos)) {
                        return;
                     }

                     height++;
                  }
               }
            }
         }
      }
   }

   static ItemStack getBlockItem(KoboldEntity entity, BlockPos pos) {
      ItemStack stack;
      try {
         stack = entity.world.getBlockState(pos).getBlock().getItem(entity.world, pos, entity.world.getBlockState(pos));
      } catch (IllegalArgumentException ex) {
         Main.LOGGER
            .error(
               "Couldn't turn a wooden block into an item to get its meta data. As a result the kobold is just gonna plant a oak saplinig instead. If you see this message, pls tell trol about it and send her the following stacktrace. Do you maybe remember what block the kobold just removed? Stacktrace follwing:"
            );
         Main.LOGGER.warn("block in question: " + entity.world.getBlockState(pos).getBlock().getTranslationKey());
         Main.LOGGER.error(ex.getMessage());
         return new ItemStack(Blocks.SAPLING, 1, 0);
      }

      int blockId = ItemBlock.getIdFromItem(stack.getItem());
      int metadata = stack.getItem().getMetadata(stack);
      if (blockId == 17 && metadata == 1) {
         return new ItemStack(Blocks.SAPLING, 1, 1);
      } else if (blockId == 17 && metadata == 2) {
         return new ItemStack(Blocks.SAPLING, 1, 2);
      } else if (blockId == 17 && metadata == 3) {
         return new ItemStack(Blocks.SAPLING, 1, 3);
      } else if (blockId == 162 && metadata == 0) {
         return new ItemStack(Blocks.SAPLING, 1, 4);
      } else {
         return blockId == 162 && metadata == 1 ? new ItemStack(Blocks.SAPLING, 1, 5) : new ItemStack(Blocks.SAPLING, 1, 0);
      }
   }

   static void mineBlockAt(KoboldEntity entity, BlockPos pos, UUID tribeId) {
      BlockPos bestBlock = null;
      ArrayList neighbors = new ArrayList();
      if (entity.world.getBlockState(pos.north().down()).isFullCube()
         && !entity.world.getBlockState(pos.north()).isFullBlock()) {
         neighbors.add(pos.north());
      }

      if (entity.world.getBlockState(pos.east().down()).isFullCube()
         && !entity.world.getBlockState(pos.east()).isFullBlock()) {
         neighbors.add(pos.east());
      }

      if (entity.world.getBlockState(pos.south().down()).isFullCube()
         && !entity.world.getBlockState(pos.south()).isFullBlock()) {
         neighbors.add(pos.south());
      }

      if (entity.world.getBlockState(pos.west().down()).isFullCube()
         && !entity.world.getBlockState(pos.west()).isFullBlock()) {
         neighbors.add(pos.west());
      }

      for (BlockPos neighbor : (java.util.Collection<BlockPos>) (neighbors) ) {
         if (bestBlock == null) {
            bestBlock = neighbor;
         } else {
            double bestDist = new Vec3d(bestBlock.getX() + 0.5F, bestBlock.getY(), bestBlock.getZ() + 0.5F).distanceTo(entity.getPositionVector());
            double dist = new Vec3d(neighbor.getX() + 0.5F, neighbor.getY(), neighbor.getZ() + 0.5F).distanceTo(entity.getPositionVector());
            if (dist < bestDist) {
               bestBlock = neighbor;
            }
         }
      }

      if (bestBlock == null) {
         KoboldManager.setLeaderKobold(tribeId, entity);
         EntityPlayer master = entity.getMasterPlayer();
         if (master != null) {
            master.sendStatusMessage(new TextComponentString("Your kobolds cannot fall this tree because it starts underground"), true);
         }
      } else if (!(entity.getPosition().getDistance(bestBlock.getX(), bestBlock.getY(), bestBlock.getZ()) > 1.0)) {
         float yaw = 0.0F;
         if (bestBlock.subtract(pos).equals(new BlockPos(0, 0, -1))) {
            yaw = 0.0F;
         }

         if (bestBlock.subtract(pos).equals(new BlockPos(1, 0, 0))) {
            yaw = 90.0F;
         }

         if (bestBlock.subtract(pos).equals(new BlockPos(0, 0, 1))) {
            yaw = 180.0F;
         }

         if (bestBlock.subtract(pos).equals(new BlockPos(-1, 0, 0))) {
            yaw = -90.0F;
         }

         entity.setTargetPosition(new Vec3d(bestBlock.getX() + 0.5, bestBlock.getY(), bestBlock.getZ() + 0.5));
         entity.setYawRotation(yaw);
         entity.getDataManager().set(KoboldEntity.IS_ANCHORED, true);
         entity.getDataManager().set(KoboldEntity.MINING_WITH_AXE, true);
         entity.setCurrentAction(Action.MINE);
         entity.world.destroyBlock(bestBlock.up(), false);
      } else if (Math.abs(entity.getPosition().getY() - bestBlock.getY()) > 4) {
         entity.syncTribeBlocks(bestBlock);
      } else {
         BlockPos standPos = entity.findStandPos(bestBlock);
         entity.getNavigator().tryMoveToXYZ(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5, 0.35);
         entity.tickPathVelocity();
      }
   }
}
