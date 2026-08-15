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
   static final String DATA_KEY = "sexmod:customstaticgirlnames";
   static final HashMap<UUID, HashMap<NpcType, String>> b = new HashMap<>();

   public AllieWorldData() {
      super("sexmod:customstaticgirlnames");
   }

   public AllieWorldData(String var1) {
      super("sexmod:customstaticgirlnames");
   }

   @SubscribeEvent
   public void onSave(Save var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().setData("sexmod:customstaticgirlnames", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void onLoad(Load var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().getOrLoadData(AllieWorldData.class, "sexmod:customstaticgirlnames");
   }

   public static void addAllie(UUID var0, NpcType var1, String var2) {
      HashMap var3 = b.get(var0);
      if (var3 == null) {
         var3 = new HashMap();
      }

      var3.put(var1, var2);
      b.put(var0, var3);
   }

   @Nullable
   public static String getNpcName(UUID var0, NpcType var1) {
      HashMap var2 = b.get(var0);
      return var2 == null ? null : (String)var2.get(var1);
   }

   public void readFromNBT(NBTTagCompound var1) {
      for (String var3 : var1.getKeySet()) {
         UUID var4;
         try {
            var4 = UUID.fromString(var3);
         } catch (IllegalArgumentException var5) {
            continue;
         }

         b.put(var4, this.a(var1.getCompoundTag(var3)));
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      for (Entry var3 : b.entrySet()) {
         UUID var4 = (UUID)var3.getKey();
         var1.setTag(var4.toString(), this.serializeNpcTypes((HashMap<NpcType, String>)var3.getValue()));
      }

      return var1;
   }

   private NBTTagCompound serializeNpcTypes(HashMap<NpcType, String> var1) {
      NBTTagCompound var2 = new NBTTagCompound();

      for (Entry var4 : var1.entrySet()) {
         var2.setString(((NpcType)var4.getKey()).name(), (String)var4.getValue());
      }

      return var2;
   }

   private HashMap<NpcType, String> a(NBTTagCompound var1) {
      HashMap var2 = new HashMap();

      for (NpcType var6 : NpcType.values()) {
         String var7 = var1.getString(var6.name());
         if (!"".equals(var7)) {
            var2.put(var6, var7);
         }
      }

      return var2;
   }

}
