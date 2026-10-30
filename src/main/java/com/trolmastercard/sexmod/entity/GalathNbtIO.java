package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.AllieWorldData;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;

/**
 * <b>Role.</b> Galath's extra NBT keys, split out of {@link GalathEntity}:
 * the bound master UUID ({@code sexmod:master}, mirrored into the
 * {@code MASTER} data key) and the despawn marker ({@code sexmod:despawned}).
 * The vanilla/BaseGirlEntity keys stay in the entity's own overrides, which
 * call {@code super} and then {@link #write(GalathEntity, NBTTagCompound)} /
 * {@link #read(GalathEntity, NBTTagCompound)}.
 * <p>
 * <b>Pitfalls.</b> Reading must restore the custom NPC name after the master
 * key, otherwise a renamed Galath loses her name on load.
 */
final class GalathNbtIO {
   private GalathNbtIO() {
   }

   static void write(GalathEntity galath, NBTTagCompound nbt) {
      nbt.setString("sexmod:master", (String)galath.getDataManager().get(BaseGirlEntity.MASTER));
      if (galath.markedDespawned) {
         nbt.setBoolean("sexmod:despawned", true);
      }
   }

   static void read(GalathEntity galath, NBTTagCompound nbt) {
      galath.getDataManager().set(BaseGirlEntity.MASTER, nbt.getString("sexmod:master"));
      if (nbt.getBoolean("sexmod:despawned")) {
         galath.despawned = true;
      }

      UUID masterUuid = galath.getMasterUUID();
      if (masterUuid != null) {
         String npcName = AllieWorldData.getNpcName(masterUuid, NpcType.GALATH);
         if (npcName != null) {
            galath.setCustomNameOverride(npcName);
         }
      }
   }
}
