package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.NpcType;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * WorldSavedData for Allie companions (tamed Allie state per world).
 */
public class AllieWorldData extends WorldSavedData {
   static final HashMap<UUID, HashMap<NpcType, String>> b = new HashMap<>();

   public AllieWorldData() {
      super("sexmod:customstaticgirlnames");
   }

   public AllieWorldData(String dataId) {
      super("sexmod:customstaticgirlnames");
   }

   @SubscribeEvent
   public void onSave(Save event) {
      World world = event.getWorld();
      world.getMapStorage().setData("sexmod:customstaticgirlnames", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void onLoad(Load event) {
      World world = event.getWorld();
      world.getMapStorage().getOrLoadData(AllieWorldData.class, "sexmod:customstaticgirlnames");
   }

   public static void addAllie(UUID playerUuid, NpcType type, String modelName) {
      HashMap typeMap = b.get(playerUuid);
      if (typeMap == null) {
         typeMap = new HashMap();
      }

      typeMap.put(type, modelName);
      b.put(playerUuid, typeMap);
   }

   @Nullable
   public static String getNpcName(UUID playerUuid, NpcType type) {
      HashMap typeMap = b.get(playerUuid);
      return typeMap == null ? null : (String)typeMap.get(type);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      for (String key : nbt.getKeySet()) {
         UUID uuid;
         try {
            uuid = UUID.fromString(key);
         } catch (IllegalArgumentException exception) {
            continue;
         }

         b.put(uuid, this.a(nbt.getCompoundTag(key)));
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      for (Entry entry : b.entrySet()) {
         UUID uuid = (UUID)entry.getKey();
         nbt.setTag(uuid.toString(), this.serializeNpcTypes((HashMap<NpcType, String>)entry.getValue()));
      }

      return nbt;
   }

   private NBTTagCompound serializeNpcTypes(HashMap<NpcType, String> typeMap) {
      NBTTagCompound tag = new NBTTagCompound();

      for (Entry entry : typeMap.entrySet()) {
         tag.setString(((NpcType)entry.getKey()).name(), (String)entry.getValue());
      }

      return tag;
   }

   private HashMap<NpcType, String> a(NBTTagCompound tag) {
      HashMap typeMap = new HashMap();

      for (NpcType type : NpcType.values()) {
         String modelName = tag.getString(type.name());
         if (!"".equals(modelName)) {
            typeMap.put(type, modelName);
         }
      }

      return typeMap;
   }

}
