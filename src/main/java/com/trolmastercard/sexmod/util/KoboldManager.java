package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.SkinColor;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.ai.NearestAttackableGirlGoal;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockChest.Type;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * <b>Role.</b> The kobold tribe system — all tribe state lives here:
 * tribes keyed by UUID ({@link KoboldManager.Tribe}), each holding members,
 * leader, color, state ({@link TribeState}), tasks ({@link KoboldTask}),
 * combatants, beds/chests, saved positions and the follow-mode flag. Used by
 * {@link KoboldEntity}/{@link KoboldPlayerEntity} AI and the dragon-staff
 * networking packets (Mine/FallTree/Claim/GetTribeUiValues/...).
 * <p>
 * <b>Persistence.</b> {@link KoboldManager.TribeWorldSavedData} serializes all
 * tribes (members, saved positions, beds, chests, tasks with mining targets)
 * into the "tribes" world data and also reacts to block place/break (bed/chest
 * tracking) and hostile spawns (girl-targeting AI). {@link #clearAll} on world
 * close.
 * <p>
 * <b>Pitfalls.</b> Many lookups print "tribe not found" and return neutral
 * values — that is the intended fallback, not an error path to fix. Task
 * removal releases workers (gravity/clip/anchor restored) and re-sends the
 * block markers. The bed-count check (members <= beds/2) gates all work
 * commands.
 */
public class KoboldManager {
   static final int TRIBE_SPAWN_RADIUS = 4;
   private static final HashMap<UUID, KoboldManager.Tribe> c = new HashMap<>();
   static final Vec3d[] b = new Vec3d[]{
      new Vec3d(0.0, 0.0, 0.0), new Vec3d(0.5, 0.0, 0.0), new Vec3d(-0.5, 0.0, 0.0), new Vec3d(0.0, 0.0, 0.5), new Vec3d(0.0, 0.0, -0.5)
   };
   static HashMap<KoboldEntity, BlockPos[]> a = new HashMap<>();

   public static void clearAll() {
      c.clear();
      a.clear();
   }

   public static void spawnKoboldAt(World world, Vec3d pos) {
      UUID tribeId = UUID.randomUUID();
      float[] speeds = new float[4];
      speeds[0] = 0.25F;

      for (int i = 1; i < speeds.length; i++) {
         speeds[i] = KoboldEntity.getRandomThrowDelay();
      }

      ArrayList kobolds = new ArrayList();

      for (float speed : speeds) {
         KoboldEntity kobold = KoboldEntity.createKoboldWithSpeed(world, tribeId, speed);
         kobolds.add(kobold);
      }

      EyeAndKoboldColor color = EyeAndKoboldColor.values()[Reference.RANDOM.nextInt(EyeAndKoboldColor.values().length)];
      KoboldManager.Tribe tribe = new KoboldManager.Tribe(tribeId, color, (KoboldEntity)kobolds.get(0), kobolds);
      c.put(tribeId, tribe);
      int i2 = 0;

      for (KoboldEntity kobold2 : (java.util.Collection<KoboldEntity>) (kobolds) ) {
         kobold2.setPosition(pos.x + b[i2].x, pos.y, pos.z + b[i2].z);
         world.spawnEntity(kobold2);
         i2++;
      }
   }

   public static boolean doesTribeExist(UUID tribeId) {
      return c.get(tribeId) != null;
   }

   public static void assignMaster(UUID tribeId, UUID playerUuid) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe != null) {
         tribe.setMasterPlayerUUID(playerUuid);
      }
   }

   public static void setTribeColor(UUID tribeId, EyeAndKoboldColor color) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe != null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " does already exist lol");
      } else {
         c.put(tribeId, new KoboldManager.Tribe(tribeId, color));
      }
   }

   public static boolean isBedAssigned(BlockPos bedPos) {
      for (Entry entry : a.entrySet()) {
         BlockPos[] bedPair = (BlockPos[])entry.getValue();
         if (bedPair[0].equals(bedPos)) {
            return true;
         }

         if (bedPair[1].equals(bedPos)) {
            return true;
         }
      }

      return false;
   }

   public static BlockPos[] getBedForKobold(KoboldEntity kobold) {
      return a.get(kobold);
   }

   public static void assignBed(KoboldEntity kobold, BlockPos bedPos) {
      World world = kobold.world;
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

      if (headPos == null) {
         System.out.println("bed @" + bedPos.toString() + " apparently doesn't have another half.. wtf");
      } else {
         a.put(kobold, new BlockPos[]{bedPos, headPos});
      }
   }

   public static void setTribeLeader(KoboldEntity kobold) {
      a.remove(kobold);
   }

   public static void setLeaderKobold(UUID tribeId, KoboldEntity kobold) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.leaderKobold = kobold;
      }
   }

   public static void addTribeMember(UUID tribeId, KoboldEntity kobold) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.addMember(kobold);
         c.replace(tribeId, tribe);
         kobold.getDataManager().set(KoboldEntity.aL, Optional.of(tribeId));
         if (!kobold.aA) {
            kobold.getDataManager().set(KoboldEntity.CURRENT_ACTION, tribe.tribeColor.toString());
         }
      }
   }

   public static void triggerFastSexAction(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         KoboldEntity leader = tribe.leaderKobold;
         if (leader == null || leader.isDead) {
            tribe.leaderKobold = tribe.getLeaderKobold();
         }
      }
   }

   public static void setTribeLeader(UUID tribeId, KoboldEntity kobold) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.setLeaderKobold(kobold);
         tribe.savePosition(kobold.getGirlId());
         if (tribe.leaderKobold != null && tribe.leaderKobold.getEntityId() == kobold.getEntityId()) {
            KoboldEntity oldLeader = tribe.getLeaderKobold();
            if (oldLeader != null) {
               tribe.leaderKobold = oldLeader;
            }
         }

         for (KoboldTask task : tribe.tasks) {
            task.addWorker(kobold);
         }

         if (!tribe.members.isEmpty()) {
            c.replace(tribeId, tribe);
         } else if (kobold.hasMaster()) {
            EntityPlayer master = kobold.getMasterPlayer();
            if (master != null) {
               HashSet blocks = new HashSet();
               blocks.addAll(tribe.tribeBeds);
               blocks.addAll(tribe.tribeChests);

               for (KoboldTask task2 : tribe.tasks) {
                  blocks.addAll(task2.miningTargets);
               }

               PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, false), (EntityPlayerMP)master);
               master.sendMessage(
                  new TextComponentString(
                     String.format(
                        "ur %stribe %shas been %seradicated %suwu", TextFormatting.RED, TextFormatting.WHITE, TextFormatting.RED, TextFormatting.WHITE
                     )
                  )
               );
            }
         }
      }
   }

   @Nullable
   public static KoboldEntity getTribeLeader(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return null;
      } else {
         return tribe.leaderKobold;
      }
   }

   public static boolean isTribeMember(UUID tribeId, KoboldEntity kobold) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return false;
      } else {
         return tribe.leaderKobold == null ? false : tribe.leaderKobold.getEntityId() == kobold.getEntityId();
      }
   }

   public static EyeAndKoboldColor getTribeColor(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return KoboldEntity.aJ;
      } else {
         return tribe.tribeColor;
      }
   }

   public static HashSet<BlockPos> getTribeBeds(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return new HashSet<>();
      } else {
         return tribe.tribeChests;
      }
   }

   public static void addTribeChest(UUID tribeId, BlockPos pos) {
      if (pos != null) {
         KoboldManager.Tribe tribe = c.get(tribeId);
         if (tribe == null) {
            System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         } else {
            tribe.tribeChests.add(pos);
         }
      }
   }

   public static void removeTribeChest(UUID tribeId, BlockPos pos) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.tribeChests.remove(pos);
      }
   }

   public static HashSet<BlockPos> getTribeChests(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return null;
      } else {
         return tribe.tribeBeds;
      }
   }

   public static void addTribeBed(UUID tribeId, BlockPos pos) {
      if (pos != null) {
         KoboldManager.Tribe tribe = c.get(tribeId);
         if (tribe == null) {
            System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         } else {
            tribe.tribeBeds.add(pos);
         }
      }
   }

   public static void removeTribeBed(UUID tribeId, BlockPos pos) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.tribeBeds.remove(pos);
      }
   }

   public static HashSet<BlockPos> removeTaskAndGetBlocks(UUID tribeId, KoboldTask task) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return new HashSet<>();
      } else if (task != null) {
         tribe.removeTask(task);
         return task.miningTargets;
      } else {
         return new HashSet<>();
      }
   }

   public static HashSet<BlockPos> removeMiningTargetsFor(UUID tribeId, BlockPos pos) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return new HashSet<>();
      }

      KoboldTask taskToRemove = null;

      for (KoboldTask task : tribe.tasks) {
         if (task.miningTargets.contains(pos)) {
            taskToRemove = task;
            break;
         }
      }

      return removeTaskAndGetBlocks(tribeId, taskToRemove);
   }

   public static void addTask(UUID tribeId, KoboldTask task) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.addTask(task);
      }
   }

   public static void removeTaskForKobold(UUID tribeId, KoboldEntity kobold) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         KoboldTask taskToRemove = null;

         for (KoboldTask task : tribe.tasks) {
            if (task.hasWorker(kobold)) {
               taskToRemove = task;
            }
         }

         if (taskToRemove == null) {
            System.out.println("task of worker " + kobold.getGirlId() + " not found uwu");
         } else {
            tribe.removeTask(taskToRemove);
         }
      }
   }

   @Nullable
   public static Collection<KoboldTask> getTribeTasks(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return null;
      } else {
         return tribe.tasks;
      }
   }

   public static TribeState getTribeState(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return TribeState.REST;
      } else {
         return tribe.getState();
      }
   }

   public static void setTribeState(UUID tribeId, TribeState state) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.setState(state);
      }
   }

   public static int getTribeMemberCount(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return 0;
      } else {
         return tribe.getTribeId();
      }
   }

   public static List<KoboldEntity> getTribeMembersList(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return new ArrayList<>();
      } else {
         return tribe.members;
      }
   }

   public static void setTribeHome(UUID tribeId, BlockPos pos) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.setTribeHome(pos);
      }
   }

   @Nullable
   public static BlockPos getTribeHomePos(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return null;
      } else {
         return tribe.getTribeHome();
      }
   }

   public static HashSet<EntityLivingBase> getTribeTargets(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return new HashSet<>();
      } else {
         return tribe.getCombatants();
      }
   }

   public static void addCombatant(UUID tribeId, EntityLivingBase entity) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.addCombatant(entity);
      }
   }

   public static void removeCombatant(UUID tribeId, EntityLivingBase entity) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.removeCombatant(entity);
      }
   }

   public static boolean hasAssignedMaster(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return false;
      }

      for (KoboldEntity member : tribe.members) {
         if (member.getInteractionPlayerUUID() != null) {
            return true;
         }
      }

      return false;
   }

   public static boolean isTribeAlerted(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return false;
      } else {
         return tribe.followModeEnabled;
      }
   }

   public static void setTribeFollowMode(UUID tribeId, boolean enabled) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         tribe.followModeEnabled = enabled;
      }
   }

   @Nullable
   public static UUID getTribeUUID(UUID playerUuid) {
      if (playerUuid == null) {
         return null;
      }

      for (Entry entry : c.entrySet()) {
         KoboldManager.Tribe tribe = (KoboldManager.Tribe)entry.getValue();
         if ((tribe.getSavedPositions().size() != 0 || tribe.getTribeId() != 0) && playerUuid.equals(((KoboldManager.Tribe)entry.getValue()).getMasterPlayerUUID())) {
            return (UUID)entry.getKey();
         }
      }

      return null;
   }

   @Nullable
   public static UUID findTribeIdWith(UUID masterUuid) {
      KoboldManager.Tribe tribe = c.get(masterUuid);
      if (tribe == null) {
         System.out.println("tribe of UUID " + masterUuid.toString() + " not found uwu");
         return null;
      }

      List members = tribe.members;
      if (members.isEmpty()) {
         return null;
      }

      KoboldEntity firstMember = (KoboldEntity)members.get(0);
      if (!firstMember.hasMaster()) {
         return null;
      }

      String masterStr = (String)((KoboldEntity)members.get(0)).getDataManager().get(BaseGirlEntity.MASTER);
      return UUID.fromString(masterStr);
   }

   public static HashSet<BlockPos> getAllTribeBlocks(UUID tribeId) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      HashSet blocks = new HashSet();
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return blocks;
      }

      for (KoboldTask task : tribe.tasks) {
         blocks.addAll(task.miningTargets);
      }

      blocks.addAll(tribe.tribeBeds);
      blocks.addAll(tribe.tribeChests);
      return blocks;
   }

   public static HashMap<UUID, BlockPos> getTribeSavedPositions(UUID tribeId, World world) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
         return new HashMap<>();
      }

      HashMap savedPositions = tribe.k;
      ArrayList staleMemberIds = new ArrayList();

      for (Entry entry : (java.util.Set<Entry>) savedPositions.entrySet()) {
         BlockPos pos = (BlockPos)entry.getValue();
         UUID memberUuid = (UUID)entry.getKey();
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

      tribe.k = savedPositions;
      return savedPositions;
   }

   public static void setTribeHomeForMember(UUID tribeId, UUID memberUuid, BlockPos homePos) {
      KoboldManager.Tribe tribe = c.get(tribeId);
      if (tribe == null) {
         System.out.println("tribe of UUID " + tribeId.toString() + " not found uwu");
      } else {
         KoboldManager.setTribeHome(memberUuid, homePos);
      }
   }

   /**
 * <b>Role.</b> A single tribe's mutable state inside {@link KoboldManager}:
 * master player, leader kobold, members, color, {@link TribeState}, tasks,
 * combatants (hostile mobs the tribe is fighting), registered beds/chests and
 * saved member positions. Methods are only called through {@link KoboldManager}
 * statics.
 * <p>
 * <b>Pitfalls.</b> {@link #getTribeId()} counts *distinct girl UUIDs* (members +
 * saved positions) — it is the value used by the bed-count gate, not the member
 * list size. {@link #removeTask} restores every worker's physics and re-sends
 * the removed mining targets as un-highlight markers. {@link #addMember} drops
 * stale entries with the same girl id (a kobold re-joining after a reload).
 */
public static class Tribe {
      UUID tribeUUID;
      UUID masterPlayerUUID;
      KoboldEntity leaderKobold;
      List<KoboldEntity> members;
      EyeAndKoboldColor tribeColor;
      TribeState state = TribeState.REST;
      BlockPos tribeHome = null;
      Collection<KoboldTask> tasks = new ArrayList<>();
      HashSet<EntityLivingBase> combatants = new HashSet<>();
      HashSet<BlockPos> tribeBeds = new HashSet<>();
      HashSet<BlockPos> tribeChests = new HashSet<>();
      HashMap<UUID, BlockPos> k = new HashMap<>();
      boolean followModeEnabled = false;

      public Tribe(UUID tribeUUID, EyeAndKoboldColor tribeColor, KoboldEntity leaderKobold, List<KoboldEntity> members) {
         this.tribeUUID = tribeUUID;
         this.tribeColor = tribeColor;
         this.leaderKobold = leaderKobold;
         this.members = members;
      }

      public Tribe(UUID tribeUUID, EyeAndKoboldColor tribeColor) {
         this.tribeUUID = tribeUUID;
         this.tribeColor = tribeColor;
         this.members = new ArrayList<>();
      }

      public void setMasterPlayerUUID(UUID uuid) {
         this.masterPlayerUUID = uuid;
      }

      public UUID getMasterPlayerUUID() {
         return this.masterPlayerUUID;
      }

      public void removeTask(KoboldTask task) {
         if (this.tasks.contains(task)) {
            for (KoboldEntity worker : task.workers) {
               worker.setCurrentAction(Action.NULL);
               worker.setNoGravity(false);
               worker.noClip = false;
               worker.getDataManager().set(BaseGirlEntity.IS_ANCHORED, false);
            }

            this.tasks.remove(task);
            if (!task.miningTargets.isEmpty() && this.masterPlayerUUID != null) {
               EntityPlayerMP master = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(this.masterPlayerUUID);
               if (master != null) {
                  PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(task.miningTargets, false), master);
               }
            }
         }
      }

      public HashMap<UUID, BlockPos> getSavedPositions() {
         return this.k;
      }

      public void setTribeHomePos(UUID memberUuid, BlockPos pos) {
         this.k.put(memberUuid, pos);
      }

      public void savePosition(UUID memberUuid) {
         this.k.remove(memberUuid);
      }

      public void removeCombatant(EntityLivingBase entity) {
         this.combatants.remove(entity);
      }

      public void addCombatant(EntityLivingBase entity) {
         this.combatants.add(entity);
      }

      public HashSet<EntityLivingBase> getCombatants() {
         return this.combatants;
      }

      public int getTribeId() {
         HashSet distinctIds = new HashSet();

         for (KoboldEntity member : this.members) {
            distinctIds.add(member.getGirlId());
         }

         for (Entry entry : this.k.entrySet()) {
            distinctIds.add(entry.getKey());
         }

         return distinctIds.size();
      }

      public BlockPos getTribeHome() {
         return this.tribeHome;
      }

      public void setTribeHome(BlockPos pos) {
         this.tribeHome = pos;
      }

      public void addTask(KoboldTask task) {
         this.tasks.add(task);
      }

      public TribeState getState() {
         return this.state;
      }

      public void setState(TribeState state) {
         this.state = state;
      }

      public void addMember(KoboldEntity kobold) {
         if (!this.members.contains(kobold)) {
            UUID girlId = kobold.getGirlId();
            ArrayList staleMembers = new ArrayList();

            for (KoboldEntity member : this.members) {
               if (member.getGirlId().equals(girlId)) {
                  staleMembers.add(member);
               }
            }

            for (KoboldEntity stale : (java.util.Collection<KoboldEntity>) (staleMembers) ) {
               Main.LOGGER.warn(String.format("Removed old entry of kobold called %s with UUID %s owned by %s", stale.getDisplayNameText(), stale.getGirlId(), this.masterPlayerUUID));
               this.setLeaderKobold(stale);
            }

            this.members.add(kobold);
         }
      }

      public void setLeaderKobold(KoboldEntity kobold) {
         this.members.remove(kobold);
      }

      KoboldEntity getLeaderKobold() {
         KoboldEntity fastest = null;

         for (KoboldEntity member : this.members) {
            if (!member.isDead) {
               if (fastest == null) {
                  fastest = member;
               } else {
                  float fastestSpeed = (Float)fastest.getDataManager().get(KoboldEntity.aE);
                  float memberSpeed = (Float)member.getDataManager().get(KoboldEntity.aE);
                  if (memberSpeed < fastestSpeed) {
                     fastest = member;
                  }
               }
            }
         }

         return fastest;
      }

   }

   /**
 * <b>Role.</b> World persistence + tribe block/combat events for
 * {@link KoboldManager}: saves/loads all tribes under the "tribes" world-data
 * key, blocks players from sleeping in assigned kobold beds, tracks bed/chest
 * placement and breaking against the tribe sets (with marker echoes to the
 * master), and gives zombies/skeletons/spiders a girl-targeting AI goal.
 * <p>
 * <b>Pitfall.</b> The NBT layout is string-indexed ({@code tribeId0},
 * {@code <tribeId>member0pos}, {@code <tribeId>0taskKind}, ...) — read and write
 * must stay in lockstep; {@link #readNBTValue} consumes (empties) keys as it
 * reads, so a re-read of the same compound returns nothing.
 */
public static class TribeWorldSavedData extends WorldSavedData {
      public TribeWorldSavedData(String dataId) {
         super(dataId);
      }

      @SubscribeEvent
      public void onWorldSave(Save event) {
         World world = event.getWorld();
         world.getMapStorage().setData("tribes", this);
         this.markDirty();
      }

      @SubscribeEvent
      public void onWorldLoad(Load event) {
         World world = event.getWorld();
         world.getMapStorage().getOrLoadData(KoboldManager.TribeWorldSavedData.class, "tribes");
      }

      @SubscribeEvent
      public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
         if (KoboldManager.isBedAssigned(event.getPos())) {
            event.setResult(SleepResult.OTHER_PROBLEM);
         }
      }

      @SubscribeEvent
      public void onBlockPlace(PlaceEvent event) {
         BlockPos pos = event.getPos();
         IBlockState state = event.getState();
         World world = event.getWorld();
         if (!world.isRemote) {
            if (state.getBlock() instanceof BlockChest) {
               Type chestType = ((BlockChest)world.getBlockState(pos).getBlock()).chestType;
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

               if (pairedPos != null) {
                  for (Entry entry : KoboldManager.c.entrySet()) {
                     KoboldManager.Tribe tribe = (KoboldManager.Tribe)entry.getValue();
                     if (tribe.tribeBeds.contains(pairedPos)) {
                        tribe.tribeBeds.add(pos);
                        UUID masterUuid = KoboldManager.findTribeIdWith((UUID)entry.getKey());
                        if (masterUuid != null) {
                           EntityPlayerMP master = (EntityPlayerMP)world.getPlayerEntityByUUID(masterUuid);
                           if (master != null) {
                              PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(pos, true), master);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onEntityJoinWorld(EntityJoinWorldEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof EntityZombie) {
            EntityZombie zombie = (EntityZombie)entity;
            zombie.targetTasks.addTask(3, new NearestAttackableGirlGoal(zombie, true, false));
         }

         if (entity instanceof AbstractSkeleton) {
            AbstractSkeleton skeleton = (AbstractSkeleton)entity;
            skeleton.targetTasks.addTask(3, new NearestAttackableGirlGoal(skeleton, true, false));
         }

         if (entity instanceof EntitySpider) {
            EntitySpider spider = (EntitySpider)entity;
            spider.targetTasks.addTask(3, new NearestAttackableGirlGoal(spider, true, true));
         }
      }

      @SubscribeEvent
      public void onBlockBreak(BreakEvent event) {
         BlockPos pos = event.getPos();
         World world = event.getWorld();
         if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BlockChest) {
               for (Entry entry : KoboldManager.c.entrySet()) {
                  KoboldManager.Tribe tribe = (KoboldManager.Tribe)entry.getValue();
                  if (tribe.tribeBeds.contains(pos)) {
                     tribe.tribeBeds.remove(pos);
                     UUID masterUuid = KoboldManager.findTribeIdWith((UUID)entry.getKey());
                     if (masterUuid != null) {
                        EntityPlayerMP master = (EntityPlayerMP)world.getPlayerEntityByUUID(masterUuid);
                        if (master != null) {
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(pos, false), master);
                        }
                     }
                  }
               }
            }

            if (block instanceof BlockBed) {
               for (Entry entry2 : KoboldManager.c.entrySet()) {
                  KoboldManager.Tribe tribe2 = (KoboldManager.Tribe)entry2.getValue();
                  if (tribe2.tribeChests.contains(pos)) {
                     BlockPos pairedPos = WorldUtils.getStatePos(pos, state);
                     tribe2.tribeChests.remove(pos);
                     tribe2.tribeChests.remove(pairedPos);
                     UUID masterUuid2 = KoboldManager.findTribeIdWith((UUID)entry2.getKey());
                     if (masterUuid2 != null) {
                        EntityPlayerMP master2 = (EntityPlayerMP)world.getPlayerEntityByUUID(masterUuid2);
                        if (master2 != null) {
                           HashSet blocks = new HashSet();
                           blocks.add(pos);
                           blocks.add(pairedPos);
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, false), master2);
                        }
                     }
                  }
               }
            }
         }
      }

      String readNBTValue(String key, NBTTagCompound nbt) {
         String value = nbt.getString(key);
         nbt.setString(key, "");
         return value;
      }

      public void readFromNBT(NBTTagCompound nbt) {
         int i = 0;

         label73:
         while (true) {
            String tribeIdStr = this.readNBTValue("tribeId" + i, nbt);
            if ("".equals(tribeIdStr)) {
               return;
            }

            UUID tribeUuid = UUID.fromString(tribeIdStr);
            EyeAndKoboldColor color = EyeAndKoboldColor.valueOf(this.readNBTValue("tribeColor" + i, nbt));
            KoboldManager.setTribeColor(tribeUuid, color);
            String masterStr = this.readNBTValue("tribeMaster" + i, nbt);
            if (!"".equals(masterStr)) {
               KoboldManager.assignMaster(tribeUuid, UUID.fromString(masterStr));
            }

            int i2 = 0;

            while (true) {
               String memberPosStr = this.readNBTValue(tribeUuid.toString() + "member" + i2 + "pos", nbt);
               if ("".equals(memberPosStr)) {
                  break;
               }

               String memberIdStr = this.readNBTValue(tribeUuid.toString() + "member" + i2 + "id", nbt);
               if ("".equals(memberIdStr)) {
                  break;
               }

               String[] parts = memberPosStr.split("\\|");
               BlockPos pos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
               UUID memberUuid = UUID.fromString(memberIdStr);
               KoboldManager.setTribeHomeForMember(tribeUuid, memberUuid, pos);
               i2++;
            }

            int i3 = 0;

            while (true) {
               String bedStr = this.readNBTValue(tribeUuid.toString() + "bed" + i3, nbt);
               if ("".equals(bedStr)) {
                  int i4 = 0;

                  while (true) {
                     String chestStr = this.readNBTValue(tribeUuid.toString() + "chest" + i4, nbt);
                     if ("".equals(chestStr)) {
                        int i5 = 0;

                        while (true) {
                           String taskKind = this.readNBTValue(tribeUuid.toString() + i5 + "taskKind", nbt);
                           if ("".equals(taskKind)) {
                              i++;
                              continue label73;
                           }

                           String facingStr = this.readNBTValue(tribeUuid.toString() + i5 + "facing", nbt);
                           EnumFacing facing = EnumFacing.NORTH;
                           if (!"".equals(facingStr)) {
                              facing = EnumFacing.byName(facingStr);
                           }

                           String taskPosStr = this.readNBTValue(tribeUuid.toString() + i5 + "pos", nbt);
                           String[] taskParts = taskPosStr.split("\\|");
                           BlockPos taskPos = new BlockPos(Integer.parseInt(taskParts[0]), Integer.parseInt(taskParts[1]), Integer.parseInt(taskParts[2]));
                           HashSet targetBlocks = new HashSet();
                           int i6 = 0;

                           while (true) {
                              String blockStr = this.readNBTValue(tribeUuid.toString() + i5 + "block" + i6, nbt);
                              if ("".equals(blockStr)) {
                                 KoboldManager.addTask(tribeUuid, new KoboldTask(taskPos, KoboldTask.TaskType.valueOf(taskKind), targetBlocks, facing));
                                 i5++;
                                 break;
                              }

                              String[] blockParts = blockStr.split("\\|");
                              BlockPos blockPos = new BlockPos(Integer.parseInt(blockParts[0]), Integer.parseInt(blockParts[1]), Integer.parseInt(blockParts[2]));
                              targetBlocks.add(blockPos);
                              i6++;
                           }
                        }
                     }

                     String[] chestParts = chestStr.split("\\|");
                     BlockPos bedPos = new BlockPos(Integer.parseInt(chestParts[0]), Integer.parseInt(chestParts[1]), Integer.parseInt(chestParts[2]));
                     KoboldManager.addTribeBed(tribeUuid, bedPos);
                     i4++;
                  }
               }

               String[] bedParts = bedStr.split("\\|");
               BlockPos bedPos2 = new BlockPos(Integer.parseInt(bedParts[0]), Integer.parseInt(bedParts[1]), Integer.parseInt(bedParts[2]));
               KoboldManager.addTribeBed(tribeUuid, bedPos2);
               i3++;
            }
         }
      }

      public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
         int i = 0;

         for (Entry entry : KoboldManager.c.entrySet()) {
            KoboldManager.Tribe tribe = (KoboldManager.Tribe)entry.getValue();
            UUID tribeUuid = (UUID)entry.getKey();
            UUID masterUuid = tribe.getMasterPlayerUUID();
            nbt.setString("tribeId" + i, tribeUuid.toString());
            nbt.setString("tribeColor" + i, tribe.tribeColor.toString());
            if (masterUuid != null) {
               nbt.setString("tribeMaster" + i, masterUuid.toString());
            }

            int i2 = 0;
            HashSet seenMemberIds = new HashSet();

            for (KoboldEntity member : tribe.members) {
               if (!member.isDead) {
                  BlockPos memberPos = member.getPosition();
                  UUID memberUuid = member.getGirlId();
                  nbt.setString(
                     tribeUuid.toString() + "member" + i2 + "pos", memberPos.getX() + "|" + memberPos.getY() + "|" + memberPos.getZ()
                  );
                  nbt.setString(tribeUuid.toString() + "member" + i2 + "id", memberUuid.toString());
                  seenMemberIds.add(memberUuid);
                  i2++;
               }
            }

            for (Entry entry2 : tribe.k.entrySet()) {
               UUID memberUuid2 = (UUID)entry2.getKey();
               BlockPos savedPos = (BlockPos)entry2.getValue();
               if (!seenMemberIds.contains(memberUuid2)) {
                  nbt.setString(
                     tribeUuid.toString() + "member" + i2 + "pos", savedPos.getX() + "|" + savedPos.getY() + "|" + savedPos.getZ()
                  );
                  nbt.setString(tribeUuid.toString() + "member" + i2 + "id", memberUuid2.toString());
                  seenMemberIds.add(memberUuid2);
                  i2++;
               }
            }

            int i3 = 0;

            for (BlockPos chestPos : tribe.tribeChests) {
               nbt.setString(tribeUuid.toString() + "bed" + i3, chestPos.getX() + "|" + chestPos.getY() + "|" + chestPos.getZ());
               i3++;
            }

            int i4 = 0;

            for (BlockPos bedPos : tribe.tribeBeds) {
               nbt.setString(tribeUuid.toString() + "chest" + i4, bedPos.getX() + "|" + bedPos.getY() + "|" + bedPos.getZ());
               i4++;
            }

            int i5 = 0;

            for (KoboldTask task : tribe.tasks) {
               nbt.setString(tribeUuid.toString() + i5 + "taskKind", task.taskType.toString());
               nbt.setString(tribeUuid.toString() + i5 + "pos", task.targetPos.getX() + "|" + task.targetPos.getY() + "|" + task.targetPos.getZ());
               nbt.setString(tribeUuid.toString() + i5 + "facing", task.facing.getName());
               int i6 = 0;

               for (BlockPos targetPos : task.miningTargets) {
                  nbt.setString(
                     tribeUuid.toString() + i5 + "block" + i6, targetPos.getX() + "|" + targetPos.getY() + "|" + targetPos.getZ()
                  );
                  i6++;
               }

               i5++;
            }

            i++;
         }

         return nbt;
      }

   }
}
