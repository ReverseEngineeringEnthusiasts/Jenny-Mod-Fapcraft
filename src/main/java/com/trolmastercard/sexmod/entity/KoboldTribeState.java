package com.trolmastercard.sexmod.entity;

import com.google.common.base.Optional;
import com.google.common.collect.UnmodifiableIterator;
import com.trolmastercard.sexmod.block.SexFireBlock;
import com.trolmastercard.sexmod.item.DragonStaffItem;
import com.trolmastercard.sexmod.item.KoboldEggItem;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.KoboldTask;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.TribeState;
import com.trolmastercard.sexmod.util.WorldUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Tribe bookkeeping for {@link KoboldEntity}: the day/night REST/ACTIVE
 * schedule, task (re)assignment, bed and home requests, tribe combat, leader
 * and member sync, and the master-presence follow loop.
 * <p>
 * All tribe state is held by {@code KoboldManager} keyed by the entity's tribe
 * UUID ({@code aL}); the fields this code touches on the entity are the
 * navigation scratch state ({@code aM} wander target, {@code aF} home offset,
 * {@code aS} last position, {@code ao} work flag).
 * <p>
 * <b>Pitfalls.</b> {@code handleTribeRequest} runs on both sides but writes
 * tribe state only through {@code KoboldManager}, and the {@code DIST} checks
 * are in blocks (not squared) on purpose.
 */
final class KoboldTribeState {
   private KoboldTribeState() {
   }

   static void handleActionCooldown(KoboldEntity entity, UUID tribeId) {
      if (entity.actionCooldown != -1) {
         if (++entity.actionCooldown >= 132) {
            entity.actionCooldown = -1;
            if (entity.getCurrentAction() == Action.MATING_PRESS_CUM) {
               UUID uuid = entity.getInteractionPlayerUUID();
               if (uuid != null) {
                  EntityPlayer player = entity.world.getPlayerEntityByUUID(uuid);
                  if (player != null) {
                     EyeAndKoboldColor color = KoboldManager.getTribeColor(tribeId);
                     ItemStack eggStack = new ItemStack(KoboldEggItem.KOBOLD_EGG_ITEM, 1, color.getWoolMeta());
                     NBTTagCompound nbt = eggStack.getTagCompound();
                     if (nbt == null) {
                        nbt = new NBTTagCompound();
                     }

                     nbt.setString("tribeID", tribeId.toString());
                     nbt.setString("tribeColor", color.toString());
                     eggStack.setTagCompound(nbt);
                     player.inventory.addItemStackToInventory(eggStack);
                  }
               }
            }
         }
      }
   }

   static void handleTribeState(KoboldEntity entity) {
      if (!(Boolean)entity.getDataManager().get(KoboldEntity.aC)) {
         if (!entity.hasMaster()) {
            Optional tribeIdOpt = (Optional)entity.getDataManager().get(KoboldEntity.aL);
            if (tribeIdOpt.isPresent()) {
               for (EntityPlayer player : entity.world.playerEntities) {
                  double dist = player.getPositionVector().distanceTo(entity.getPositionVector());
                  double closestDist = dist;
                  if (!entity.world.isRemote) {
                     for (KoboldEntity kobold : KoboldManager.getTribeMembersList((UUID)tribeIdOpt.get())) {
                        double koboldDist = player.getPositionVector().distanceTo(kobold.getPositionVector());
                        if (koboldDist < closestDist) {
                           closestDist = koboldDist;
                        }
                     }
                  }

                  if (!(closestDist > 10.0)) {
                     if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() != DragonStaffItem.DRAGON_STAFF
                        && player.getHeldItem(EnumHand.OFF_HAND).getItem() != DragonStaffItem.DRAGON_STAFF) {
                        return;
                     }

                     PathNavigate navigator = entity.getNavigator();
                     navigator.clearPath();
                     if (entity.world.isRemote) {
                        entity.handleKoboldOwner(player);
                     } else if (dist > 2.0) {
                        BlockPos standPos = entity.findStandPos(player.getPosition());
                        navigator.tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   static void handleMasterPresence(KoboldEntity entity) {
      Optional tribeIdOpt = (Optional)entity.getDataManager().get(KoboldEntity.aL);
      if (tribeIdOpt.isPresent()) {
         UUID tribeId = (UUID)tribeIdOpt.get();
         if (!(Boolean)entity.getDataManager().get(KoboldEntity.aC) && KoboldManager.isTribeAlerted(tribeId)) {
            if (!entity.hasMaster()) {
               return;
            }

            EntityPlayer master = entity.getMasterPlayer();
            if (master == null) {
               return;
            }

            for (KoboldTask task : KoboldManager.getTribeTasks(tribeId)) {
               if (task.hasWorker(entity)) {
                  task.addWorker(entity);
                  entity.setCurrentAction(Action.NULL);
                  entity.getDataManager().set(KoboldEntity.IS_ANCHORED, false);
               }
            }

            entity.noClip = false;
            entity.setNoGravity(false);
            PathNavigate navigator = entity.getNavigator();
            double dist = entity.getPositionVector().distanceTo(master.getPositionVector());
            if (dist > 2.0) {
               navigator.tryMoveToEntityLiving(master, getKickDistance(entity, master, dist));
               entity.tickPathVelocity();
               if (dist > 15.0) {
                  handlePlayerDismount(entity, master);
               }
            }
         } else if (KoboldManager.isTribeMember(tribeId, entity)) {
            handleTribeRequest(entity, tribeId);
         } else {
            handleTribeJoin(entity, tribeId);
         }
      }
   }

   static double getKickDistance(KoboldEntity entity, EntityPlayer player, double dist) {
      double kickDistance;
      if (player.isSprinting()) {
         kickDistance = 0.7;
      } else {
         kickDistance = 0.35;
      }

      double extra = Math.floor(dist / 5.0) * 0.3;
      kickDistance += extra;
      if (entity.isInWater()) {
         kickDistance *= 60.0;
      }

      return kickDistance;
   }

   static void teleportToHome(KoboldEntity entity, UUID tribeId) {
      BlockPos homePos = KoboldManager.getTribeHomePos(tribeId);
      if (homePos != null) {
         if (entity.aX != null) {
            entity.world.setBlockState(homePos, entity.aX);
         }

         if (entity.blockBelowState != null) {
            entity.world.setBlockState(homePos.add(0, -1, 0), entity.blockBelowState);
         }
      }
   }

   static void handleTribeRequest(KoboldEntity entity, UUID tribeId) {
      if (!isTribeTaskDone(entity, tribeId)) {
         if (!entity.hasMaster() && KoboldManager.hasAssignedMaster(tribeId)) {
            entity.getNavigator().clearPath();
            entity.aM = null;
         } else {
            TribeState currentState = KoboldManager.getTribeState(tribeId);
            TribeState newState = getTribeStateForTime(entity);
            if (currentState != newState) {
KoboldManager.setTribeState(tribeId, newState);
               switch (newState) {
                  case REST:
                     handleTaskAssign(entity, tribeId);
                     KoboldManager.setTribeHome(tribeId, (BlockPos)null);
                     entity.sendGirlChatMessage("okay resting time owo");
                     break;
                  case ACTIVE:
                     teleportToHome(entity, tribeId);
                     handleMemberSync(entity, tribeId);
               }
            }

            switch (newState) {
               case REST:
                  handleTaskRequest(entity, tribeId);
                  break;
               case ACTIVE:
                  entity.aF = null;
                  handleHomeRelease(entity, tribeId);
            }
         }
      }
   }

   static void handleTaskAssign(KoboldEntity entity, UUID tribeId) {
      Collection tasks = KoboldManager.getTribeTasks(tribeId);
      if (tasks != null) {
         for (KoboldTask task : (java.util.Collection<KoboldTask>) (tasks) ) {
            task.releaseWorkers();
         }
      }
   }

   static void handleMemberSync(KoboldEntity entity, UUID tribeId) {
      if (entity.hasMaster()) {
         for (KoboldEntity kobold : KoboldManager.getTribeMembersList(tribeId)) {
            KoboldManager.setTribeLeader(kobold);
            if (kobold.getInteractionPlayerUUID() == null) {
               kobold.noClip = false;
               kobold.setNoGravity(false);
               kobold.getDataManager().set(KoboldEntity.IS_ANCHORED, false);
               kobold.setCurrentAction(Action.NULL);
            }
         }
      }
   }

   static void handleTaskRequest(KoboldEntity entity, UUID tribeId) {
      Collection tasks = KoboldManager.getTribeTasks(tribeId);
      if (tasks != null) {
         for (KoboldTask task : (java.util.Collection<KoboldTask>) (tasks) ) {
            task.addWorker(entity);
         }
      }

      if (entity.hasMaster()) {
         handleBedRequest(entity, tribeId);
      } else {
         handleHomeRequest(entity, tribeId);
      }
   }

   static void handleBedRequest(KoboldEntity entity, UUID tribeId) {
      BlockPos[] bedPositions = KoboldManager.getBedForKobold(entity);
      if (bedPositions != null) {
         Vec3d headVec = new Vec3d(bedPositions[0].getX() + 0.5F, bedPositions[0].getY() + 0.5625, bedPositions[0].getZ() + 0.5F);
         Vec3d footVec = new Vec3d(bedPositions[1].getX() + 0.5F, bedPositions[1].getY() + 0.5625, bedPositions[1].getZ() + 0.5F);
         boolean isVertical = headVec.subtract(footVec).x == 0.0;
         Vec3d midVec = RotationHelper.lerpVec3dDouble(headVec, footVec, 0.5);
         entity.getDataManager().set(KoboldEntity.IS_ANCHORED, true);
         entity.setTargetPosition(midVec);
         entity.setYawRotation(isVertical ? 0.0F : 90.0F);
         entity.noClip = true;
         entity.setNoGravity(true);
      } else {
         HashSet beds = KoboldManager.getTribeBeds(tribeId);
         BlockPos chosenBed = null;
         if (beds != null) {
            for (BlockPos bedPos : (java.util.Collection<BlockPos>) (beds) ) {
               IBlockState state = entity.world.getBlockState(bedPos);
               boolean occupied = false;
               UnmodifiableIterator iterator = state.getProperties().entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry property = (Entry)iterator.next();
                  if (property.getKey() instanceof PropertyBool) {
                     occupied = Boolean.valueOf((Boolean)property.getValue());
                     break;
                  }
               }

               if (!occupied && !KoboldManager.isBedAssigned(bedPos)) {
                  if (chosenBed == null) {
                     chosenBed = bedPos;
                  } else if (entity.getDistanceSq(chosenBed) > entity.getDistanceSq(bedPos)) {
                     chosenBed = bedPos;
                  }
               }
            }

            if (chosenBed != null) {
               if (chosenBed.getDistance((int)entity.posX, (int)entity.posY, (int)entity.posZ) > 2.0) {
                  if (Math.abs(chosenBed.subtract(entity.getPosition()).getY()) > 4) {
                     entity.syncTribeBlocks(chosenBed.add(0, 1, 0));
                  } else {
                     BlockPos standPos = entity.findStandPos(chosenBed);
                     entity.getNavigator().tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
                     if (entity.getNavigator().getPath() == null) {
                        entity.syncTribeBlocks(chosenBed.add(0, 1, 0));
                     }
                  }
               } else {
KoboldManager.assignBed(entity, chosenBed);
                  entity.setCurrentAction(Action.SLEEP);
               }
            }
         }
      }
   }

   static void handleHomeRequest(KoboldEntity entity, UUID tribeId) {
      BlockPos homePos = KoboldManager.getTribeHomePos(tribeId);
      if (homePos != null) {
         if (entity.aF == null) {
            entity.aF = homePos.add(
               (entity.getRNG().nextBoolean() ? 1 : -1) * (entity.getRNG().nextInt(2) + 1),
               0,
               (entity.getRNG().nextBoolean() ? 1 : -1) * (entity.getRNG().nextInt(2) + 1)
            );
         }

         entity.getNavigator().tryMoveToXYZ(entity.aF.getX(), entity.aF.getY(), entity.aF.getZ(), 0.35F);
         entity.tickPathVelocity();
      } else {
         if (KoboldManager.isTribeMember(tribeId, entity)) {
            BlockPos pos = entity.getPosition().add(1, 0, 0);
            entity.blockBelowState = entity.world.getBlockState(pos.add(0, -1, 0));
            entity.aX = entity.world.getBlockState(pos);
            entity.world.setBlockState(pos.add(0, -1, 0), Blocks.NETHERRACK.getDefaultState());
            entity.world.setBlockState(pos, SexFireBlock.FIRE.getDefaultState());
            KoboldManager.setTribeHome(tribeId, pos);
         }
      }
   }

   static void handleHomeRelease(KoboldEntity entity, UUID tribeId) {
      if (entity.hasMaster()) {
         KoboldManager.setTribeHome(tribeId, (BlockPos)null);
         handleTaskFollow(entity, tribeId);
      } else {
         Collection tasks = KoboldManager.getTribeTasks(tribeId);
         if (tasks != null) {
            if (entity.ao) {
               entity.aM = null;
               handleTribeTasks(entity, tribeId, tasks);
            } else {
               handleTribeTasksInit(entity, tribeId, tasks);
            }
         }
      }
   }

   static void handleTribeTasks(KoboldEntity entity, UUID tribeId, Collection<KoboldTask> tasks) {
      if (tasks.isEmpty()) {
         entity.ao = false;
         checkTribeHome(entity, tribeId);
         entity.sendGirlChatMessage("Lets go somewhere else");
      }
   }

   static void handleTribeTasksInit(KoboldEntity entity, UUID tribeId, Collection<KoboldTask> tasks) {
      BlockPos homePos = KoboldManager.getTribeHomePos(tribeId);
      if (homePos == null) {
         checkTribeHome(entity, tribeId);
      } else {
         if (entity.ticksExisted % 40 == 0) {
            if (entity.aS.equals(entity.getPositionVector())) {
               checkTribeHome(entity, tribeId);
               entity.aM = null;
            }

            entity.aS = entity.getPositionVector();
         }

         if (entity.aM == null || entity.aM.getDistance((int)entity.posX, (int)entity.posY, (int)entity.posZ) < 4.0) {
            entity.aM = getTribeHomePos(entity, tribeId);
         }

         entity.getNavigator().tryMoveToXYZ(entity.aM.getX(), entity.aM.getY(), entity.aM.getZ(), 0.35F);
         entity.tickPathVelocity();
         if (!(Math.sqrt(entity.getPosition().distanceSq(homePos)) > 5.0)) {
            entity.ao = true;
            entity.sendGirlChatMessage("Time to work bitches!");
            int memberCount = KoboldManager.getTribeMemberCount(tribeId);

            for (int i = 1; i < memberCount; i++) {
               findConnectedLogs(entity, tribeId, tasks);
            }

            KoboldManager.setTribeHome(tribeId, (BlockPos)null);
         }
      }
   }

   static void handlePlayerDismount(KoboldEntity entity, EntityPlayer player) {
      int attempts = 0;

      BlockPos teleportPos;
      do {
         teleportPos = player.getPosition().add(Reference.RANDOM.nextInt(10), 0, Reference.RANDOM.nextInt(10));
      } while (++attempts < 20 && !entity.attemptTeleport(teleportPos.getX(), teleportPos.getY(), teleportPos.getZ()));

      if (attempts == 20) {
         entity.setPosition(player.posX, player.posY, player.posZ);
      }

      entity.motionX = 0.0;
      entity.motionY = 0.0;
      entity.motionZ = 0.0;
   }

   static BlockPos getTribeHomePos(KoboldEntity entity, UUID tribeId) {
      BlockPos homePos = KoboldManager.getTribeHomePos(tribeId);
      return homePos == null ? BlockPos.ORIGIN : entity.findStandPos(homePos);
   }

   static void checkTribeHome(KoboldEntity entity, UUID tribeId) {
      int attempts = 0;

      BlockPos homePos;
      do {
         attempts++;
         homePos = entity.getPosition();
         homePos = homePos.add(
            (50 + entity.getRNG().nextInt(50)) * (entity.getRNG().nextBoolean() ? 1 : -1),
            0,
            (50 + entity.getRNG().nextInt(50)) * (entity.getRNG().nextBoolean() ? 1 : -1)
         );
         homePos = new BlockPos(homePos.getX(), WorldUtils.getHeightAt(entity.world, homePos.getX(), homePos.getZ()), homePos.getZ());
      } while ((homePos.getY() <= 0 || !entity.getNavigator().canEntityStandOnPos(homePos)) && attempts < 100);

      KoboldManager.setTribeHome(tribeId, homePos);
   }

   static void findConnectedLogs(KoboldEntity entity, UUID tribeId, Collection<KoboldTask> tasks) {
      List logs = entity.findBlocksInRadius(entity.getPosition(), BlockLog.class, 30, 4, null);
      BlockPos connectedLog = null;

      for (BlockPos logPos : (java.util.Collection<BlockPos>) (logs) ) {
         Block blockBelow = entity.world.getBlockState(logPos.down()).getBlock();
         if (!(blockBelow instanceof BlockLog) && blockBelow != Blocks.AIR) {
            boolean connected = false;

            for (KoboldTask task : tasks) {
               if (task.isMiningTarget(logPos)) {
                  connected = true;
                  break;
               }
            }

            if (!connected) {
               connectedLog = logPos;
               break;
            }
         }
      }

      if (connectedLog != null) {
         KoboldTask.findConnectedBlocks(entity.world, connectedLog, tribeId);
         entity.sendGirlChatMessage("Someone, go fall this tree!");
      }
   }

   static TribeState getTribeStateForTime(KoboldEntity entity) {
      long time = entity.world.getWorldTime();
      return time < 12000L ? TribeState.ACTIVE : TribeState.REST;
   }

   static boolean isTribeTaskDone(KoboldEntity entity, UUID tribeId) {
      return handleTribeCombat(entity, tribeId, true);
   }

   static boolean handleTribeCombat(KoboldEntity entity, UUID tribeId, boolean isLeader) {
      HashSet targets = KoboldManager.getTribeTargets(tribeId);
      KoboldEntity leader = KoboldManager.getTribeLeader(tribeId);
      if (leader == null) {
         return false;
      }

      for (KoboldEntity kobold : entity.world
         .getEntitiesWithinAABB(
            KoboldEntity.class,
            new AxisAlignedBB(
               leader.posX - 30.0,
               leader.posY - 30.0,
               leader.posZ - 30.0,
               leader.posX + 30.0,
               leader.posY + 30.0,
               leader.posZ + 30.0
            )
         )) {
         if (entity.canEntityBeSeen(kobold) && (!kobold.hasMaster() || !entity.hasMaster())) {
            Optional tribeOpt = (Optional)kobold.getDataManager().get(KoboldEntity.aL);
            if (!tribeOpt.isPresent()) {
               targets.add(kobold);
            } else if (!((UUID)tribeOpt.get()).equals(tribeId)) {
               targets.add(kobold);
            }
         }
      }

      EntityLivingBase closestTarget = null;
      ArrayList deadTargets = new ArrayList();

      for (EntityLivingBase target : (java.util.Collection<EntityLivingBase>) (targets) ) {
         if (target.isDead) {
            deadTargets.add(target);
         } else if (!(leader.getDistance(target) > 30.0F) && (closestTarget == null || entity.getDistance(closestTarget) > entity.getDistance(target))) {
            closestTarget = target;
         }
      }

      for (EntityLivingBase target : (java.util.Collection<EntityLivingBase>) (deadTargets) ) {
         KoboldManager.removeCombatant(tribeId, target);
      }

      if (closestTarget == null) {
         return false;
      }

      if (!isLeader) {
         return true;
      }

      if (entity.getCurrentAction() != Action.ATTACK) {
         entity.getDataManager().set(KoboldEntity.IS_ANCHORED, false);
         entity.setCurrentAction(Action.NULL);
      }

      BlockPos standPos = entity.findStandPos(closestTarget.getPosition());
      entity.getNavigator().tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.7);
      entity.tickPathVelocity();
      if (entity.getDistance(closestTarget) > 1.5F) {
         return true;
      }

      if (entity.aP > 0) {
         return true;
      }

      float yaw = (float)(Math.atan2(entity.posZ - closestTarget.posZ, entity.posX - closestTarget.posX) * (180.0 / Math.PI) + 90.0);
      entity.setYawRotation(yaw);
      entity.setCurrentAction(Action.ATTACK);
      entity.aP = 84;
      return true;
   }

   static void handleTribeJoin(KoboldEntity entity, UUID tribeId) {
      if (!isTribeTaskDone(entity, tribeId)) {
         TribeState state = KoboldManager.getTribeState(tribeId);
         switch (state) {
            case REST:
               handleTaskRequest(entity, tribeId);
               break;
            case ACTIVE:
               entity.aF = null;
               handleHomeTeleport(entity, tribeId);
         }
      }
   }

   static void handleHomeTeleport(KoboldEntity entity, UUID tribeId) {
      BlockPos homePos = KoboldManager.getTribeHomePos(tribeId);
      if (homePos == null) {
         entity.aM = null;
         handleTaskFollow(entity, tribeId);
      } else {
         KoboldEntity leader = KoboldManager.getTribeLeader(tribeId);
         if (KoboldManager.hasAssignedMaster(tribeId)) {
            entity.getNavigator().clearPath();
            entity.aM = null;
         } else if (leader == null) {
            System.out.println("leader of tribe " + tribeId + " is null");
         } else {
            if (leader.getDistance(entity) > 20.0F) {
               entity.setPosition(leader.posX, leader.posY, leader.posZ);
               entity.aM = null;
            }

            if (entity.ticksExisted % 40 == 0) {
               if (entity.aS.equals(entity.getPositionVector())) {
                  entity.aM = getTribeHomePos(entity, tribeId);
               }

               entity.aS = entity.getPositionVector();
            }

            if (entity.aM == null || entity.aM.getDistance((int)entity.posX, (int)entity.posY, (int)entity.posZ) < 4.0) {
               entity.aM = getTribeHomePos(entity, tribeId);
            }

            entity.getNavigator().tryMoveToXYZ(entity.aM.getX(), entity.aM.getY(), entity.aM.getZ(), 0.35F);
            entity.tickPathVelocity();
         }
      }
   }

   static void handleTaskFollow(KoboldEntity entity, UUID tribeId) {
      if (entity.getInteractionPlayerUUID() == null) {
         Collection tasks = KoboldManager.getTribeTasks(tribeId);
         if (tasks != null) {
            KoboldTask assignedTask = null;

            for (KoboldTask task : (java.util.Collection<KoboldTask>) (tasks) ) {
               if (task.hasWorker(entity)) {
                  assignedTask = task;
                  break;
               }
            }

            if (assignedTask == null) {
               for (KoboldTask task : (java.util.Collection<KoboldTask>) (tasks) ) {
                  if (!entity.hasMaster() || assignTaskToKobold(entity, tribeId, task)) {
                     if (!entity.canAssignTask(task)) {
                        entity.ax = true;
                     } else if (task.addWorker(entity)) {
                        assignedTask = task;
                        entity.aI = null;
                        if (task.getTaskType() == KoboldTask.TaskType.FALL_TREE) {
                           entity.sendGirlChatMessage("Ima fall this tree owo");
                        } else {
                           entity.sendGirlChatMessage("Ima go mine uwu");
                           entity.syncTribeBlocks(task.getTargetPos());
                           entity.world.setBlockState(task.getTargetPos(), Blocks.AIR.getDefaultState());
                        }
                        break;
                     }
                  }
               }
            }

            if (assignedTask == null) {
               entity.handleNearbyPlayerTick(tribeId);
            } else {
               if (assignedTask.getTaskType() == KoboldTask.TaskType.FALL_TREE) {
                  entity.startMiningTask(tribeId, assignedTask.getTargetPos(), assignedTask);
               }

               if (assignedTask.getTaskType() == KoboldTask.TaskType.MINE) {
                  handleTribeTasks(entity, tribeId, tasks);
               }
            }
         }
      }
   }

   static boolean assignTaskToKobold(KoboldEntity entity, UUID tribeId, KoboldTask task) {
      List members = KoboldManager.getTribeMembersList(tribeId);
      Collection tasks = KoboldManager.getTribeTasks(tribeId);
      KoboldEntity closestKobold = null;
      Vec3d taskPos = new Vec3d(task.getTargetPos().getX(), task.getTargetPos().getY(), task.getTargetPos().getZ());

      for (KoboldEntity kobold : (java.util.Collection<KoboldEntity>) (members) ) {
         boolean taskAssigned = false;

         for (KoboldTask workerTask : (java.util.Collection<KoboldTask>) (tasks) ) {
            if (workerTask.hasWorker(kobold)) {
               taskAssigned = true;
               break;
            }
         }

         if (!taskAssigned && kobold.getInteractionPlayerUUID() == null) {
            if (closestKobold == null) {
               closestKobold = kobold;
            } else if (closestKobold.getPositionVector().distanceTo(taskPos) > kobold.getPositionVector().distanceTo(taskPos)) {
               closestKobold = kobold;
            }
         }
      }

      return entity.equals(closestKobold);
   }

   static void handleModelSync(KoboldEntity entity) {
      if (!entity.aA) {
         Optional tribeIdOpt = (Optional)entity.getDataManager().get(KoboldEntity.aL);
         if (tribeIdOpt.isPresent()) {
            entity.getDataManager().set(KoboldEntity.CURRENT_ACTION, KoboldManager.getTribeColor((UUID)tribeIdOpt.get()).toString());
         }
      }
   }
}
