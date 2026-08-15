package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import com.trolmastercard.sexmod.networking.InformOfOwnershipPacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import org.apache.logging.log4j.Level;

/**
 * <b>Role.</b> Galath/Manglelie ownership persistence — a
 * {@link WorldSavedData} ("sexmod:galath_owner_ship") storing player<->galath
 * pairs, per-owner last-cum-dosage time and the set of manglelie-owned players.
 * This is the *ownership* system, NOT the scene system
 * (see {@link KoboldManager} and the networking scene packets for that).
 * <p>
 * <b>State.</b> {@code h} = bidirectional player UUID <-> girl UUID map,
 * {@code b} = last cum-time per player (drives despawn/cooldown), {@code
 * mangOwnershipSet} = players who own a manglelie. Static accessors are used by
 * {@link GalathCoinItem}, {@link RequestRidingPacket} and the galath AI.
 * <p>
 * <b>Server tick.</b> Ownership is dropped (and the client informed via
 * {@link InformOfOwnershipPacket}) when the owner is online but his galath is
 * gone — no stale pairs survive a missing girl.
 * <p>
 * <b>Pitfall.</b> {@link #debugEnabled} is a *client-side* mirror of "am I
 * owned" (see {@link InformOfOwnershipPacket}); do not use it for server logic.
 */
public class GirlSavedData extends WorldSavedData {
   public static boolean debugEnabled = true;
   public static final float CUM_TIMEOUT = 60.0F;
   public static final String GALATH_OWNERSHIP_KEY = "sexmod:galath_owner_ship";
   public static final String OWNERSHIP_DATA_KEY = "sexmod:ownershipdata";
   public static final String MANG_OWNERSHIP_DATA_KEY = "sexmod:mangownershipdata";
   static final long lastSaveTime = 0L;
   static BiDirectionalMap<UUID, UUID> h = new BiDirectionalMap<>();
   static HashMap<UUID, Long> b = new HashMap<>();
   static HashSet<UUID> mangOwnershipSet = new HashSet<>();

   public GirlSavedData() {
      super("sexmod:galath_owner_ship");
   }

   public GirlSavedData(String dataId) {
      super("sexmod:galath_owner_ship");
   }

   public static void clearAll() {
      mangOwnershipSet.clear();
      h.clear();
   }

   public static void markAsManglelieOwned(UUID girlUuid) {
      UUID ownerId = getManglelieOwnerId(girlUuid);
      if (ownerId != null) {
         mangOwnershipSet.add(ownerId);
      }
   }

   public static boolean isManglelieOwned(UUID girlUuid) {
      return mangOwnershipSet.contains(girlUuid);
   }

   public static boolean isOwnerNearby(GalathEntity galath) {
      UUID ownerId = h.getByValue(galath.getGirlId());
      if (ownerId == null) {
         return false;
      } else {
         World world = galath.world;
         EntityPlayer owner = world.getPlayerEntityByUUID(ownerId);
         if (owner == null) {
            return true;
         } else {
            return owner.dimension != galath.dimension ? false : !(owner.getDistance(galath) > 60.0F);
         }
      }
   }

   public static boolean isOwnerOf(EntityPlayer player, GalathEntity galath) {
      return galath.getGirlId().equals(h.getByKey(player.getPersistentID()));
   }

   public static void updateMangleliePartner(GalathEntity galath) {
      ManglelieEntity partner = galath.getMangleliePartner(true);
      if (partner != null) {
         galath.world.removeEntity(partner);
      }

      UUID ownerId = h.getByValue(galath.getGirlId());
      if (ownerId == null) {
         galath.world.removeEntity(galath);
      } else {
         World world = galath.world;
         EntityPlayer owner = world.getPlayerEntityByUUID(ownerId);
         galath.world.removeEntity(galath);
         h.removeByKey(ownerId);
         if (owner != null) {
            PacketHandler.networkWrapper.sendTo(new InformOfOwnershipPacket(false), (EntityPlayerMP)owner);
         }
      }
   }

   public static boolean hasOwner(UUID uuid) {
      return h.getByKey(uuid) != null;
   }

   public static UUID getManglelieOwnerId(UUID uuid) {
      return h.getByValue(uuid);
   }

   public static UUID getManglelieOwnerOf(GalathEntity galath) {
      return galath == null ? null : getManglelieOwnerId(galath.getGirlId());
   }

   public static UUID getOwnerId(UUID uuid) {
      return h.getByKey(uuid);
   }

   public static UUID getOwnerOf(EntityPlayer player) {
      return player == null ? null : getOwnerId(player.getPersistentID());
   }

   public static void setOwnerShip(UUID playerUuid, UUID girlUuid) {
      h.put(playerUuid, girlUuid);
   }

   public static void grantOwnership(EntityPlayer player, GalathEntity galath) {
      if (player != null) {
         if (galath != null) {
            setOwnerShip(player.getPersistentID(), galath.getGirlId());
         }
      }
   }

   public static void removeOwner(UUID uuid) {
      h.removeByKey(uuid);
   }

   public static void removeOwnerOf(EntityPlayer player) {
      if (player != null) {
         removeOwner(player.getPersistentID());
      }
   }

   public static boolean shouldDespawn(UUID girlUuid, World world) {
      Long lastCumTime = b.get(girlUuid);
      if (!isManglelieOwned(girlUuid)) {
         return false;
      } else {
         return lastCumTime == null ? true : world.getTotalWorldTime() - lastCumTime > 0L;
      }
   }

   public static void saveCumTime(UUID playerUuid, Long time) {
      if (playerUuid == null) {
         Main.LOGGER.log(Level.WARN, "tried to save last cum dosage time on NULL player");
      } else {
         b.put(playerUuid, time);
      }
   }

   @SubscribeEvent
   public void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
         ArrayList playersToNotify = new ArrayList();

         for (Entry entry : h.entrySet()) {
            UUID ownerUuid = (UUID)entry.getKey();
            UUID girlUuid = (UUID)entry.getValue();
            EntityPlayer owner = world.getPlayerEntityByUUID(ownerUuid);
            if (owner != null && BaseGirlEntity.getServerGirlEntity(girlUuid) == null) {
               playersToNotify.add(owner);
            }
         }

         for (EntityPlayer player : (java.util.Collection<EntityPlayer>) (playersToNotify) ) {
            h.removeByKey(player.getPersistentID());
            PacketHandler.networkWrapper.sendTo(new InformOfOwnershipPacket(false), (EntityPlayerMP)player);
         }
      }
   }

   @SubscribeEvent
   public void onSave(Save event) {
      World world = event.getWorld();
      world.getMapStorage().setData("sexmod:galath_owner_ship", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void onLoad(Load event) {
      World world = event.getWorld();
      world.getMapStorage().getOrLoadData(GirlSavedData.class, "sexmod:galath_owner_ship");
   }

   public void readFromNBT(NBTTagCompound nbt) {
      NBTTagCompound tag = nbt.getCompoundTag("sexmod:ownershipdata");
      int count = tag.getInteger("amount");

      for (int i = 0; i < count; i++) {
         UUID masterUuid = tag.getUniqueId("master" + i);
         UUID galathUuid = tag.getUniqueId("galath" + i);
         long lastCumTime = tag.getLong("lastcumdosage" + i);
         if (masterUuid != null && galathUuid != null) {
            h.put(masterUuid, galathUuid);
            b.put(masterUuid, lastCumTime);
         } else {
            Main.LOGGER.fatal("OMFG WHOOP WHOOP SAVING DIDNT WORK CORRECTLY AAAAAAAAAAA");
         }
      }

      NBTTagCompound mangTag = nbt.getCompoundTag("sexmod:mangownershipdata");

      for (int i2 = 0; mangTag.hasUniqueId("mang" + i2); i2++) {
         mangOwnershipSet.add(mangTag.getUniqueId("mang" + i2));
      }

      nbt.setTag("sexmod:mangownershipdata", new NBTTagCompound());
      nbt.setTag("sexmod:ownershipdata", new NBTTagCompound());
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setInteger("amount", h.size());
      int i = 0;

      for (Entry entry : h.entrySet()) {
         UUID masterUuid = (UUID)entry.getKey();
         UUID galathUuid = (UUID)entry.getValue();
         Long cumTime = b.get(masterUuid);
         if (cumTime == null) {
            cumTime = 0L;
         }

         tag.setUniqueId("galath" + i, galathUuid);
         tag.setUniqueId("master" + i, masterUuid);
         tag.setLong("lastcumdosage" + i, cumTime);
         i++;
      }

      NBTTagCompound mangTag = new NBTTagCompound();
      i = 0;

      for (UUID uuid : mangOwnershipSet) {
         mangTag.setUniqueId("mang" + i++, uuid);
      }

      nbt.setTag("sexmod:ownershipdata", tag);
      nbt.setTag("sexmod:mangownershipdata", mangTag);
      return nbt;
   }

}
