package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * <b>Role.</b> Persistence of custom model codes for Galath and Manglelie —
 * a {@link WorldSavedData} ("sexmod:static_custom_model_manager") storing
 * UUID -> model-code maps (one for galaths, one for manglelies) so a girl's
 * custom outfit survives world reloads. Lookups fall back to the girl's own
 * UUID when she has no owner.
 * <p>
 * <b>Who uses it.</b> {@link BaseGirlEntity}/{@link GalathEntity} model-code
 * resolution and the clothing editor. Keep the two maps ({@code c} = galath,
 * {@code b} = manglelie) distinct — mixing them garbles outfits on reload.
 */
public class GirlWorldData extends WorldSavedData {
   static final String DATA_KEY = "sexmod:static_custom_model_manager";
   static final String SAVE_KEY = "sexmod:static_custom_model_manager";
   public static HashMap<UUID, String> c = new HashMap<>();
   public static HashMap<UUID, String> b = new HashMap<>();

   public GirlWorldData() {
      super("sexmod:static_custom_model_manager");
   }

   public GirlWorldData(String dataId) {
      super("sexmod:static_custom_model_manager");
   }

   public static String getCustomModelCode(BaseGirlEntity girl) {
      String code = buildModelCode(girl);
      return code == null ? "" : code;
   }

   private static String buildModelCode(BaseGirlEntity girl) {
      if (girl instanceof GalathEntity) {
         UUID girlId = girl.getGirlId();
         UUID ownerId = GirlSavedData.getManglelieOwnerId(girlId);
         if (ownerId == null) {
            ownerId = girlId;
         }

         return c.get(ownerId);
      } else if (girl instanceof ManglelieEntity) {
         UUID ownerId2 = GirlSavedData.getManglelieOwnerId(((ManglelieEntity)girl).getCorruptPlayerUUID());
         return b.get(ownerId2 == null ? girl.getGirlId() : ownerId2);
      } else {
         return null;
      }
   }

   public static void setCustomModelCode(BaseGirlEntity girl) {
      if (girl instanceof GalathEntity) {
         UUID girlId = girl.getGirlId();
         UUID ownerId = GirlSavedData.getManglelieOwnerId(girlId);
         if (ownerId == null) {
            ownerId = girlId;
         }

         c.put(ownerId, girl.getCustomModelCode());
      } else {
         if (girl instanceof ManglelieEntity) {
            UUID ownerId2 = GirlSavedData.getManglelieOwnerId(((ManglelieEntity)girl).getCorruptPlayerUUID());
            b.put(ownerId2 == null ? girl.getGirlId() : ownerId2, girl.getCustomModelCode());
         }
      }
   }

   @SubscribeEvent
   public void onSave(Save event) {
      World world = event.getWorld();
      world.getMapStorage().setData("sexmod:static_custom_model_manager", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void onLoad(Load event) {
      World world = event.getWorld();
      world.getMapStorage().getOrLoadData(GirlWorldData.class, "sexmod:static_custom_model_manager");
   }

   public void readFromNBT(NBTTagCompound nbt) {
      NBTTagCompound tag = nbt.getCompoundTag("sexmod:static_custom_model_manager");
      this.writeNBT(tag.getCompoundTag("galath"), c);
      this.writeNBT(tag.getCompoundTag("mang"), b);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setTag("galath", this.serializeOwnership(c));
      tag.setTag("mang", this.serializeOwnership(b));
      nbt.setTag("sexmod:static_custom_model_manager", tag);
      return nbt;
   }

   NBTTagCompound serializeOwnership(HashMap<UUID, String> ownershipMap) {
      NBTTagCompound tag = new NBTTagCompound();
      int i = 0;

      for (Entry entry : ownershipMap.entrySet()) {
         UUID uuid = (UUID)entry.getKey();
         tag.setString("UUID" + i, uuid.toString());
         tag.setString("MODEL" + i, (String)entry.getValue());
         i++;
      }

      return tag;
   }

   void writeNBT(NBTTagCompound tag, HashMap<UUID, String> ownershipMap) {
      int i = 0;

      while (true) {
         String uuidString = tag.getString("UUID" + i);
         if ("".equals(uuidString)) {
            return;
         }

         ownershipMap.put(UUID.fromString(uuidString), tag.getString("MODEL" + i));
         i++;
      }
   }

   public static void clearAll() {
      c.clear();
      b.clear();
   }

}
