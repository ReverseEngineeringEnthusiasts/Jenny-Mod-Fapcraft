package com.trolmastercard.sexmod.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Per-save record of who already received the {@link GuideBook}, so the book is
 * handed out on the first join of a world and not on every login.
 * <p>
 * Stored in the world's {@link MapStorage} (shared by every dimension of the
 * save). Server-side only; {@link GuideBook#giveOnce} is the only writer.
 */
public class GuideBookData extends WorldSavedData {
   private static final String DATA_NAME = "sexmod_guide_book";
   private final Set<UUID> received = new HashSet<>();
   private int version;

   /** Required by {@link MapStorage#getOrLoadData}. */
   public GuideBookData() {
      super(DATA_NAME);
   }

   public GuideBookData(String name) {
      super(name);
   }

   public static GuideBookData get(World world) {
      MapStorage storage = world.getMapStorage();
      GuideBookData data = (GuideBookData)storage.getOrLoadData(GuideBookData.class, DATA_NAME);
      if (data == null) {
         data = new GuideBookData();
         storage.setData(DATA_NAME, data);
      }

      return data;
   }

   public boolean hasReceived(UUID playerId) {
      return this.received.contains(playerId);
   }

   /** Version of the page set the players in this save last received. */
   public int version() {
      return this.version;
   }

   public void markReceived(UUID playerId, int pageVersion) {
      boolean changed = this.received.add(playerId);
      if (pageVersion > this.version) {
         this.version = pageVersion;
         changed = true;
      }

      if (changed) {
         this.markDirty();
      }
   }

   @Override
   public void readFromNBT(NBTTagCompound nbt) {
      this.received.clear();
      this.version = nbt.getInteger("version");
      NBTTagList players = nbt.getTagList("players", 8);

      for (int i = 0; i < players.tagCount(); i++) {
         try {
            this.received.add(UUID.fromString(players.getStringTagAt(i)));
         } catch (IllegalArgumentException ignored) {
            // a malformed id in an old save must not stop the rest from loading
         }
      }
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      nbt.setInteger("version", this.version);
      NBTTagList players = new NBTTagList();

      for (UUID playerId : this.received) {
         players.appendTag(new NBTTagString(playerId.toString()));
      }

      nbt.setTag("players", players);
      return nbt;
   }
}
