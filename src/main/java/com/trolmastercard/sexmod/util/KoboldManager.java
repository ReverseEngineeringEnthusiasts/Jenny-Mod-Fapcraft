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

   public static void spawnKoboldAt(World var0, Vec3d var1) {
      UUID var2 = UUID.randomUUID();
      float[] var3 = new float[4];
      var3[0] = 0.25F;

      for (int var4 = 1; var4 < var3.length; var4++) {
         var3[var4] = KoboldEntity.getRandomThrowDelay();
      }

      ArrayList var10 = new ArrayList();

      for (float var8 : var3) {
         KoboldEntity var9 = KoboldEntity.createKoboldWithSpeed(var0, var2, var8);
         var10.add(var9);
      }

      EyeAndKoboldColor var11 = EyeAndKoboldColor.values()[Reference.RANDOM.nextInt(EyeAndKoboldColor.values().length)];
      KoboldManager.Tribe var12 = new KoboldManager.Tribe(var2, var11, (KoboldEntity)var10.get(0), var10);
      c.put(var2, var12);
      int var13 = 0;

      for (KoboldEntity var15 : (java.util.Collection<KoboldEntity>) (var10) ) {
         var15.setPosition(var1.x + b[var13].x, var1.y, var1.z + b[var13].z);
         var0.spawnEntity(var15);
         var13++;
      }
   }

   public static boolean doesTribeExist(UUID var0) {
      return c.get(var0) != null;
   }

   public static void assignMaster(UUID var0, UUID var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 != null) {
         var2.setMasterPlayerUUID(var1);
      }
   }

   public static void setTribeColor(UUID var0, EyeAndKoboldColor var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 != null) {
         System.out.println("tribe of UUID " + var0.toString() + " does already exist lol");
      } else {
         c.put(var0, new KoboldManager.Tribe(var0, var1));
      }
   }

   public static boolean isBedAssigned(BlockPos var0) {
      for (Entry var2 : a.entrySet()) {
         BlockPos[] var3 = (BlockPos[])var2.getValue();
         if (var3[0].equals(var0)) {
            return true;
         }

         if (var3[1].equals(var0)) {
            return true;
         }
      }

      return false;
   }

   public static BlockPos[] getBedForKobold(KoboldEntity var0) {
      return a.get(var0);
   }

   public static void assignBed(KoboldEntity var0, BlockPos var1) {
      World var2 = var0.world;
      BlockPos var3 = null;
      if (var2.getBlockState(var1.north()).getBlock() instanceof BlockBed) {
         var3 = var1.north();
      }

      if (var2.getBlockState(var1.east()).getBlock() instanceof BlockBed) {
         var3 = var1.east();
      }

      if (var2.getBlockState(var1.south()).getBlock() instanceof BlockBed) {
         var3 = var1.south();
      }

      if (var2.getBlockState(var1.west()).getBlock() instanceof BlockBed) {
         var3 = var1.west();
      }

      if (var3 == null) {
         System.out.println("bed @" + var1.toString() + " apparently doesn't have another half.. wtf");
      } else {
         a.put(var0, new BlockPos[]{var1, var3});
      }
   }

   public static void setTribeLeader(KoboldEntity var0) {
      a.remove(var0);
   }

   public static void setLeaderKobold(UUID var0, KoboldEntity var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.leaderKobold = var1;
      }
   }

   public static void addTribeMember(UUID var0, KoboldEntity var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.addMember(var1);
         c.replace(var0, var2);
         var1.getDataManager().set(KoboldEntity.aL, Optional.of(var0));
         if (!var1.aA) {
            var1.getDataManager().set(KoboldEntity.CURRENT_ACTION, var2.tribeColor.toString());
         }
      }
   }

   public static void triggerFastSexAction(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         KoboldEntity var2 = var1.leaderKobold;
         if (var2 == null || var2.isDead) {
            var1.leaderKobold = var1.getLeaderKobold();
         }
      }
   }

   public static void setTribeLeader(UUID var0, KoboldEntity var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.setLeaderKobold(var1);
         var2.savePosition(var1.getGirlId());
         if (var2.leaderKobold != null && var2.leaderKobold.getEntityId() == var1.getEntityId()) {
            KoboldEntity var3 = var2.getLeaderKobold();
            if (var3 != null) {
               var2.leaderKobold = var3;
            }
         }

         for (KoboldTask var4 : var2.tasks) {
            var4.addWorker(var1);
         }

         if (!var2.members.isEmpty()) {
            c.replace(var0, var2);
         } else if (var1.hasMaster()) {
            EntityPlayer var8 = var1.getMasterPlayer();
            if (var8 != null) {
               HashSet var9 = new HashSet();
               var9.addAll(var2.tribeBeds);
               var9.addAll(var2.tribeChests);

               for (KoboldTask var6 : var2.tasks) {
                  var9.addAll(var6.miningTargets);
               }

               PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var9, false), (EntityPlayerMP)var8);
               var8.sendMessage(
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
   public static KoboldEntity getTribeLeader(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return null;
      } else {
         return var1.leaderKobold;
      }
   }

   public static boolean isTribeMember(UUID var0, KoboldEntity var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return false;
      } else {
         return var2.leaderKobold == null ? false : var2.leaderKobold.getEntityId() == var1.getEntityId();
      }
   }

   public static EyeAndKoboldColor getTribeColor(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return KoboldEntity.aJ;
      } else {
         return var1.tribeColor;
      }
   }

   public static HashSet<BlockPos> getTribeBeds(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return new HashSet<>();
      } else {
         return var1.tribeChests;
      }
   }

   public static void addTribeChest(UUID var0, BlockPos var1) {
      if (var1 != null) {
         KoboldManager.Tribe var2 = c.get(var0);
         if (var2 == null) {
            System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         } else {
            var2.tribeChests.add(var1);
         }
      }
   }

   public static void removeTribeChest(UUID var0, BlockPos var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.tribeChests.remove(var1);
      }
   }

   public static HashSet<BlockPos> getTribeChests(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return null;
      } else {
         return var1.tribeBeds;
      }
   }

   public static void addTribeBed(UUID var0, BlockPos var1) {
      if (var1 != null) {
         KoboldManager.Tribe var2 = c.get(var0);
         if (var2 == null) {
            System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         } else {
            var2.tribeBeds.add(var1);
         }
      }
   }

   public static void removeTribeBed(UUID var0, BlockPos var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.tribeBeds.remove(var1);
      }
   }

   public static HashSet<BlockPos> removeTaskAndGetBlocks(UUID var0, KoboldTask var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return new HashSet<>();
      } else if (var1 != null) {
         var2.removeTask(var1);
         return var1.miningTargets;
      } else {
         return new HashSet<>();
      }
   }

   public static HashSet<BlockPos> removeMiningTargetsFor(UUID var0, BlockPos var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return new HashSet<>();
      }

      KoboldTask var3 = null;

      for (KoboldTask var5 : var2.tasks) {
         if (var5.miningTargets.contains(var1)) {
            var3 = var5;
            break;
         }
      }

      return removeTaskAndGetBlocks(var0, var3);
   }

   public static void addTask(UUID var0, KoboldTask var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.addTask(var1);
      }
   }

   public static void removeTaskForKobold(UUID var0, KoboldEntity var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         KoboldTask var3 = null;

         for (KoboldTask var5 : var2.tasks) {
            if (var5.hasWorker(var1)) {
               var3 = var5;
            }
         }

         if (var3 == null) {
            System.out.println("task of worker " + var1.getGirlId() + " not found uwu");
         } else {
            var2.removeTask(var3);
         }
      }
   }

   @Nullable
   public static Collection<KoboldTask> getTribeTasks(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return null;
      } else {
         return var1.tasks;
      }
   }

   public static TribeState getTribeState(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return TribeState.REST;
      } else {
         return var1.getState();
      }
   }

   public static void setTribeState(UUID var0, TribeState var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.setState(var1);
      }
   }

   public static int getTribeMemberCount(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return 0;
      } else {
         return var1.getTribeId();
      }
   }

   public static List<KoboldEntity> getTribeMembersList(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return new ArrayList<>();
      } else {
         return var1.members;
      }
   }

   public static void setTribeHome(UUID var0, BlockPos var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.setTribeHome(var1);
      }
   }

   @Nullable
   public static BlockPos getTribeHomePos(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return null;
      } else {
         return var1.getTribeHome();
      }
   }

   public static HashSet<EntityLivingBase> getTribeTargets(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return new HashSet<>();
      } else {
         return var1.getCombatants();
      }
   }

   public static void addCombatant(UUID var0, EntityLivingBase var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.addCombatant(var1);
      }
   }

   public static void removeCombatant(UUID var0, EntityLivingBase var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.removeCombatant(var1);
      }
   }

   public static boolean hasAssignedMaster(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return false;
      }

      for (KoboldEntity var3 : var1.members) {
         if (var3.getInteractionPlayerUUID() != null) {
            return true;
         }
      }

      return false;
   }

   public static boolean isTribeAlerted(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return false;
      } else {
         return var1.followModeEnabled;
      }
   }

   public static void setTribeFollowMode(UUID var0, boolean var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         var2.followModeEnabled = var1;
      }
   }

   @Nullable
   public static UUID getTribeUUID(UUID var0) {
      if (var0 == null) {
         return null;
      }

      for (Entry var2 : c.entrySet()) {
         KoboldManager.Tribe var3 = (KoboldManager.Tribe)var2.getValue();
         if ((var3.getSavedPositions().size() != 0 || var3.getTribeId() != 0) && var0.equals(((KoboldManager.Tribe)var2.getValue()).getMasterPlayerUUID())) {
            return (UUID)var2.getKey();
         }
      }

      return null;
   }

   @Nullable
   public static UUID findTribeIdWith(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return null;
      }

      List var2 = var1.members;
      if (var2.isEmpty()) {
         return null;
      }

      KoboldEntity var3 = (KoboldEntity)var2.get(0);
      if (!var3.hasMaster()) {
         return null;
      }

      String var4 = (String)((KoboldEntity)var2.get(0)).getDataManager().get(BaseGirlEntity.MASTER);
      return UUID.fromString(var4);
   }

   public static HashSet<BlockPos> getAllTribeBlocks(UUID var0) {
      KoboldManager.Tribe var1 = c.get(var0);
      HashSet var2 = new HashSet();
      if (var1 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return var2;
      }

      for (KoboldTask var4 : var1.tasks) {
         var2.addAll(var4.miningTargets);
      }

      var2.addAll(var1.tribeBeds);
      var2.addAll(var1.tribeChests);
      return var2;
   }

   public static HashMap<UUID, BlockPos> getTribeSavedPositions(UUID var0, World var1) {
      KoboldManager.Tribe var2 = c.get(var0);
      if (var2 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
         return new HashMap<>();
      }

      HashMap var3 = var2.k;
      ArrayList var4 = new ArrayList();

      for (Entry var6 : (java.util.Set<Entry>) var3.entrySet()) {
         BlockPos var7 = (BlockPos)var6.getValue();
         UUID var8 = (UUID)var6.getKey();
         if (var1.isAreaLoaded(var7, 5)) {
            AxisAlignedBB var9 = new AxisAlignedBB(var7.subtract(new Vec3i(-3, -3, -3)), var7.add(3, 3, 3));
            List var10 = var1.getEntitiesWithinAABB(KoboldEntity.class, var9);
            boolean var11 = false;

            for (KoboldEntity var13 : (java.util.Collection<KoboldEntity>) (var10) ) {
               if (var8.equals(var13.getGirlId())) {
                  var11 = true;
                  break;
               }
            }

            if (!var11) {
               var4.add(var8);
            }
         }
      }

      var2.k = var3;
      return var3;
   }

   public static void setTribeHomeForMember(UUID var0, UUID var1, BlockPos var2) {
      KoboldManager.Tribe var3 = c.get(var0);
      if (var3 == null) {
         System.out.println("tribe of UUID " + var0.toString() + " not found uwu");
      } else {
         KoboldManager.setTribeHome(var1, var2);
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

      public Tribe(UUID var1, EyeAndKoboldColor var2, KoboldEntity var3, List<KoboldEntity> var4) {
         this.tribeUUID = var1;
         this.tribeColor = var2;
         this.leaderKobold = var3;
         this.members = var4;
      }

      public Tribe(UUID var1, EyeAndKoboldColor var2) {
         this.tribeUUID = var1;
         this.tribeColor = var2;
         this.members = new ArrayList<>();
      }

      public void setMasterPlayerUUID(UUID var1) {
         this.masterPlayerUUID = var1;
      }

      public UUID getMasterPlayerUUID() {
         return this.masterPlayerUUID;
      }

      public void removeTask(KoboldTask var1) {
         if (this.tasks.contains(var1)) {
            for (KoboldEntity var3 : var1.workers) {
               var3.setCurrentAction(Action.NULL);
               var3.setNoGravity(false);
               var3.noClip = false;
               var3.getDataManager().set(BaseGirlEntity.IS_ANCHORED, false);
            }

            this.tasks.remove(var1);
            if (!var1.miningTargets.isEmpty() && this.masterPlayerUUID != null) {
               EntityPlayerMP var4 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(this.masterPlayerUUID);
               if (var4 != null) {
                  PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var1.miningTargets, false), var4);
               }
            }
         }
      }

      public HashMap<UUID, BlockPos> getSavedPositions() {
         return this.k;
      }

      public void setTribeHomePos(UUID var1, BlockPos var2) {
         this.k.put(var1, var2);
      }

      public void savePosition(UUID var1) {
         this.k.remove(var1);
      }

      public void removeCombatant(EntityLivingBase var1) {
         this.combatants.remove(var1);
      }

      public void addCombatant(EntityLivingBase var1) {
         this.combatants.add(var1);
      }

      public HashSet<EntityLivingBase> getCombatants() {
         return this.combatants;
      }

      public int getTribeId() {
         HashSet var1 = new HashSet();

         for (KoboldEntity var3 : this.members) {
            var1.add(var3.getGirlId());
         }

         for (Entry var5 : this.k.entrySet()) {
            var1.add(var5.getKey());
         }

         return var1.size();
      }

      public BlockPos getTribeHome() {
         return this.tribeHome;
      }

      public void setTribeHome(BlockPos var1) {
         this.tribeHome = var1;
      }

      public void addTask(KoboldTask var1) {
         this.tasks.add(var1);
      }

      public TribeState getState() {
         return this.state;
      }

      public void setState(TribeState var1) {
         this.state = var1;
      }

      public void addMember(KoboldEntity var1) {
         if (!this.members.contains(var1)) {
            UUID var2 = var1.getGirlId();
            ArrayList var3 = new ArrayList();

            for (KoboldEntity var5 : this.members) {
               if (var5.getGirlId().equals(var2)) {
                  var3.add(var5);
               }
            }

            for (KoboldEntity var7 : (java.util.Collection<KoboldEntity>) (var3) ) {
               Main.LOGGER.warn(String.format("Removed old entry of kobold called %s with UUID %s owned by %s", var7.getDisplayNameText(), var7.getGirlId(), this.masterPlayerUUID));
               this.setLeaderKobold(var7);
            }

            this.members.add(var1);
         }
      }

      public void setLeaderKobold(KoboldEntity var1) {
         this.members.remove(var1);
      }

      KoboldEntity getLeaderKobold() {
         KoboldEntity var1 = null;

         for (KoboldEntity var3 : this.members) {
            if (!var3.isDead) {
               if (var1 == null) {
                  var1 = var3;
               } else {
                  float var4 = (Float)var1.getDataManager().get(KoboldEntity.aE);
                  float var5 = (Float)var3.getDataManager().get(KoboldEntity.aE);
                  if (var5 < var4) {
                     var1 = var3;
                  }
               }
            }
         }

         return var1;
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
      public TribeWorldSavedData(String var1) {
         super(var1);
      }

      @SubscribeEvent
      public void onWorldSave(Save var1) {
         World var2 = var1.getWorld();
         var2.getMapStorage().setData("tribes", this);
         this.markDirty();
      }

      @SubscribeEvent
      public void onWorldLoad(Load var1) {
         World var2 = var1.getWorld();
         var2.getMapStorage().getOrLoadData(KoboldManager.TribeWorldSavedData.class, "tribes");
      }

      @SubscribeEvent
      public void onPlayerSleepInBed(PlayerSleepInBedEvent var1) {
         if (KoboldManager.isBedAssigned(var1.getPos())) {
            var1.setResult(SleepResult.OTHER_PROBLEM);
         }
      }

      @SubscribeEvent
      public void onBlockPlace(PlaceEvent var1) {
         BlockPos var2 = var1.getPos();
         IBlockState var3 = var1.getState();
         World var4 = var1.getWorld();
         if (!var4.isRemote) {
            if (var3.getBlock() instanceof BlockChest) {
               Type var5 = ((BlockChest)var4.getBlockState(var2).getBlock()).chestType;
               BlockPos var6 = null;
               if (var4.getBlockState(var2.north()).getBlock() instanceof BlockChest
                  && var5.equals(((BlockChest)var4.getBlockState(var2.north()).getBlock()).chestType)) {
                  var6 = var2.north();
               }

               if (var4.getBlockState(var2.east()).getBlock() instanceof BlockChest
                  && var5.equals(((BlockChest)var4.getBlockState(var2.east()).getBlock()).chestType)) {
                  var6 = var2.east();
               }

               if (var4.getBlockState(var2.south()).getBlock() instanceof BlockChest
                  && var5.equals(((BlockChest)var4.getBlockState(var2.south()).getBlock()).chestType)) {
                  var6 = var2.south();
               }

               if (var4.getBlockState(var2.west()).getBlock() instanceof BlockChest
                  && var5.equals(((BlockChest)var4.getBlockState(var2.west()).getBlock()).chestType)) {
                  var6 = var2.west();
               }

               if (var6 != null) {
                  for (Entry var8 : KoboldManager.c.entrySet()) {
                     KoboldManager.Tribe var9 = (KoboldManager.Tribe)var8.getValue();
                     if (var9.tribeBeds.contains(var6)) {
                        var9.tribeBeds.add(var2);
                        UUID var10 = KoboldManager.findTribeIdWith((UUID)var8.getKey());
                        if (var10 != null) {
                           EntityPlayerMP var11 = (EntityPlayerMP)var4.getPlayerEntityByUUID(var10);
                           if (var11 != null) {
                              PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var2, true), var11);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onEntityJoinWorld(EntityJoinWorldEvent var1) {
         Entity var2 = var1.getEntity();
         if (var2 instanceof EntityZombie) {
            EntityZombie var3 = (EntityZombie)var2;
            var3.targetTasks.addTask(3, new NearestAttackableGirlGoal(var3, true, false));
         }

         if (var2 instanceof AbstractSkeleton) {
            AbstractSkeleton var4 = (AbstractSkeleton)var2;
            var4.targetTasks.addTask(3, new NearestAttackableGirlGoal(var4, true, false));
         }

         if (var2 instanceof EntitySpider) {
            EntitySpider var5 = (EntitySpider)var2;
            var5.targetTasks.addTask(3, new NearestAttackableGirlGoal(var5, true, true));
         }
      }

      @SubscribeEvent
      public void onBlockBreak(BreakEvent var1) {
         BlockPos var2 = var1.getPos();
         World var3 = var1.getWorld();
         if (!var3.isRemote) {
            IBlockState var4 = var3.getBlockState(var2);
            Block var5 = var4.getBlock();
            if (var5 instanceof BlockChest) {
               for (Entry var7 : KoboldManager.c.entrySet()) {
                  KoboldManager.Tribe var8 = (KoboldManager.Tribe)var7.getValue();
                  if (var8.tribeBeds.contains(var2)) {
                     var8.tribeBeds.remove(var2);
                     UUID var9 = KoboldManager.findTribeIdWith((UUID)var7.getKey());
                     if (var9 != null) {
                        EntityPlayerMP var10 = (EntityPlayerMP)var3.getPlayerEntityByUUID(var9);
                        if (var10 != null) {
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var2, false), var10);
                        }
                     }
                  }
               }
            }

            if (var5 instanceof BlockBed) {
               for (Entry var14 : KoboldManager.c.entrySet()) {
                  KoboldManager.Tribe var15 = (KoboldManager.Tribe)var14.getValue();
                  if (var15.tribeChests.contains(var2)) {
                     BlockPos var16 = WorldUtils.getStatePos(var2, var4);
                     var15.tribeChests.remove(var2);
                     var15.tribeChests.remove(var16);
                     UUID var17 = KoboldManager.findTribeIdWith((UUID)var14.getKey());
                     if (var17 != null) {
                        EntityPlayerMP var11 = (EntityPlayerMP)var3.getPlayerEntityByUUID(var17);
                        if (var11 != null) {
                           HashSet var12 = new HashSet();
                           var12.add(var2);
                           var12.add(var16);
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(var12, false), var11);
                        }
                     }
                  }
               }
            }
         }
      }

      String readNBTValue(String var1, NBTTagCompound var2) {
         String var3 = var2.getString(var1);
         var2.setString(var1, "");
         return var3;
      }

      public void readFromNBT(NBTTagCompound var1) {
         int var2 = 0;

         label73:
         while (true) {
            String var3 = this.readNBTValue("tribeId" + var2, var1);
            if ("".equals(var3)) {
               return;
            }

            UUID var4 = UUID.fromString(var3);
            EyeAndKoboldColor var5 = EyeAndKoboldColor.valueOf(this.readNBTValue("tribeColor" + var2, var1));
            KoboldManager.setTribeColor(var4, var5);
            String var6 = this.readNBTValue("tribeMaster" + var2, var1);
            if (!"".equals(var6)) {
               KoboldManager.assignMaster(var4, UUID.fromString(var6));
            }

            int var7 = 0;

            while (true) {
               String var8 = this.readNBTValue(var4.toString() + "member" + var7 + "pos", var1);
               if ("".equals(var8)) {
                  break;
               }

               String var9 = this.readNBTValue(var4.toString() + "member" + var7 + "id", var1);
               if ("".equals(var9)) {
                  break;
               }

               String[] var10 = var8.split("\\|");
               BlockPos var11 = new BlockPos(Integer.parseInt(var10[0]), Integer.parseInt(var10[1]), Integer.parseInt(var10[2]));
               UUID var12 = UUID.fromString(var9);
               KoboldManager.setTribeHomeForMember(var4, var12, var11);
               var7++;
            }

            int var22 = 0;

            while (true) {
               String var23 = this.readNBTValue(var4.toString() + "bed" + var22, var1);
               if ("".equals(var23)) {
                  int var24 = 0;

                  while (true) {
                     String var26 = this.readNBTValue(var4.toString() + "chest" + var24, var1);
                     if ("".equals(var26)) {
                        int var27 = 0;

                        while (true) {
                           String var30 = this.readNBTValue(var4.toString() + var27 + "taskKind", var1);
                           if ("".equals(var30)) {
                              var2++;
                              continue label73;
                           }

                           String var32 = this.readNBTValue(var4.toString() + var27 + "facing", var1);
                           EnumFacing var13 = EnumFacing.NORTH;
                           if (!"".equals(var32)) {
                              var13 = EnumFacing.byName(var32);
                           }

                           String var14 = this.readNBTValue(var4.toString() + var27 + "pos", var1);
                           String[] var15 = var14.split("\\|");
                           BlockPos var16 = new BlockPos(Integer.parseInt(var15[0]), Integer.parseInt(var15[1]), Integer.parseInt(var15[2]));
                           HashSet var17 = new HashSet();
                           int var18 = 0;

                           while (true) {
                              String var19 = this.readNBTValue(var4.toString() + var27 + "block" + var18, var1);
                              if ("".equals(var19)) {
                                 KoboldManager.addTask(var4, new KoboldTask(var16, KoboldTask.TaskType.valueOf(var30), var17, var13));
                                 var27++;
                                 break;
                              }

                              String[] var20 = var19.split("\\|");
                              BlockPos var21 = new BlockPos(Integer.parseInt(var20[0]), Integer.parseInt(var20[1]), Integer.parseInt(var20[2]));
                              var17.add(var21);
                              var18++;
                           }
                        }
                     }

                     String[] var29 = var26.split("\\|");
                     BlockPos var31 = new BlockPos(Integer.parseInt(var29[0]), Integer.parseInt(var29[1]), Integer.parseInt(var29[2]));
                     KoboldManager.addTribeBed(var4, var31);
                     var24++;
                  }
               }

               String[] var25 = var23.split("\\|");
               BlockPos var28 = new BlockPos(Integer.parseInt(var25[0]), Integer.parseInt(var25[1]), Integer.parseInt(var25[2]));
               KoboldManager.addTribeBed(var4, var28);
               var22++;
            }
         }
      }

      public NBTTagCompound writeToNBT(NBTTagCompound var1) {
         int var2 = 0;

         for (Entry var4 : KoboldManager.c.entrySet()) {
            KoboldManager.Tribe var5 = (KoboldManager.Tribe)var4.getValue();
            UUID var6 = (UUID)var4.getKey();
            UUID var7 = var5.getMasterPlayerUUID();
            var1.setString("tribeId" + var2, var6.toString());
            var1.setString("tribeColor" + var2, var5.tribeColor.toString());
            if (var7 != null) {
               var1.setString("tribeMaster" + var2, var7.toString());
            }

            int var8 = 0;
            HashSet var9 = new HashSet();

            for (KoboldEntity var11 : var5.members) {
               if (!var11.isDead) {
                  BlockPos var12 = var11.getPosition();
                  UUID var13 = var11.getGirlId();
                  var1.setString(
                     var6.toString() + "member" + var8 + "pos", var12.getX() + "|" + var12.getY() + "|" + var12.getZ()
                  );
                  var1.setString(var6.toString() + "member" + var8 + "id", var13.toString());
                  var9.add(var13);
                  var8++;
               }
            }

            for (Entry var20 : var5.k.entrySet()) {
               UUID var23 = (UUID)var20.getKey();
               BlockPos var27 = (BlockPos)var20.getValue();
               if (!var9.contains(var23)) {
                  var1.setString(
                     var6.toString() + "member" + var8 + "pos", var27.getX() + "|" + var27.getY() + "|" + var27.getZ()
                  );
                  var1.setString(var6.toString() + "member" + var8 + "id", var23.toString());
                  var9.add(var23);
                  var8++;
               }
            }

            int var19 = 0;

            for (BlockPos var24 : var5.tribeChests) {
               var1.setString(var6.toString() + "bed" + var19, var24.getX() + "|" + var24.getY() + "|" + var24.getZ());
               var19++;
            }

            int var22 = 0;

            for (BlockPos var28 : var5.tribeBeds) {
               var1.setString(var6.toString() + "chest" + var22, var28.getX() + "|" + var28.getY() + "|" + var28.getZ());
               var22++;
            }

            int var26 = 0;

            for (KoboldTask var14 : var5.tasks) {
               var1.setString(var6.toString() + var26 + "taskKind", var14.taskType.toString());
               var1.setString(var6.toString() + var26 + "pos", var14.targetPos.getX() + "|" + var14.targetPos.getY() + "|" + var14.targetPos.getZ());
               var1.setString(var6.toString() + var26 + "facing", var14.facing.getName());
               int var15 = 0;

               for (BlockPos var17 : var14.miningTargets) {
                  var1.setString(
                     var6.toString() + var26 + "block" + var15, var17.getX() + "|" + var17.getY() + "|" + var17.getZ()
                  );
                  var15++;
               }

               var26++;
            }

            var2++;
         }

         return var1;
      }

   }
}
