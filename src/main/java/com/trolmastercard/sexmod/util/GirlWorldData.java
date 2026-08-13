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

public class GirlWorldData extends WorldSavedData {
   static final String a = "sexmod:static_custom_model_manager";
   static final String d = "sexmod:static_custom_model_manager";
   public static HashMap<UUID, String> c = new HashMap<>();
   public static HashMap<UUID, String> b = new HashMap<>();

   public GirlWorldData() {
      super("sexmod:static_custom_model_manager");
   }

   public GirlWorldData(String var1) {
      super("sexmod:static_custom_model_manager");
   }

   public static String c_clash151(BaseGirlEntity var0) {
      String var1 = b_clash152(var0);
      return var1 == null ? "" : var1;
   }

   private static String b_clash152(BaseGirlEntity var0) {
      if (var0 instanceof GalathEntity) {
         UUID var3 = var0.getGirlId();
         UUID var2 = GirlSavedData.f_clash850(var3);
         if (var2 == null) {
            var2 = var3;
         }

         return c.get(var2);
      } else if (var0 instanceof ManglelieEntity) {
         UUID var1 = GirlSavedData.f_clash850(((ManglelieEntity)var0).v_clash412());
         return b.get(var1 == null ? var0.getGirlId() : var1);
      } else {
         return null;
      }
   }

   public static void a_clash153(BaseGirlEntity var0) {
      if (var0 instanceof GalathEntity) {
         UUID var3 = var0.getGirlId();
         UUID var2 = GirlSavedData.f_clash850(var3);
         if (var2 == null) {
            var2 = var3;
         }

         c.put(var2, var0.getCustomModelCode());
      } else {
         if (var0 instanceof ManglelieEntity) {
            UUID var1 = GirlSavedData.f_clash850(((ManglelieEntity)var0).v_clash412());
            b.put(var1 == null ? var0.getGirlId() : var1, var0.getCustomModelCode());
         }
      }
   }

   @SubscribeEvent
   public void a(Save var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().setData("sexmod:static_custom_model_manager", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void a(Load var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().getOrLoadData(GirlWorldData.class, "sexmod:static_custom_model_manager");
   }

   public void readFromNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = var1.getCompoundTag("sexmod:static_custom_model_manager");
      this.a(var2.getCompoundTag("galath"), c);
      this.a(var2.getCompoundTag("mang"), b);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = new NBTTagCompound();
      var2.setTag("galath", this.a_clash154(c));
      var2.setTag("mang", this.a_clash154(b));
      var1.setTag("sexmod:static_custom_model_manager", var2);
      return var1;
   }

   NBTTagCompound a_clash154(HashMap<UUID, String> var1) {
      NBTTagCompound var2 = new NBTTagCompound();
      int var3 = 0;

      for (Entry var5 : var1.entrySet()) {
         UUID var6 = (UUID)var5.getKey();
         var2.setString("UUID" + var3, var6.toString());
         var2.setString("MODEL" + var3, (String)var5.getValue());
         var3++;
      }

      return var2;
   }

   void a(NBTTagCompound var1, HashMap<UUID, String> var2) {
      int var3 = 0;

      while (true) {
         String var4 = var1.getString("UUID" + var3);
         if ("".equals(var4)) {
            return;
         }

         var2.put(UUID.fromString(var4), var1.getString("MODEL" + var3));
         var3++;
      }
   }

   public static void clearAll() {
      c.clear();
      b.clear();
   }

}
