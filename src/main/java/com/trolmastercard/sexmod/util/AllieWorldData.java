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

public class AllieWorldData extends WorldSavedData {
   static final String a = "sexmod:customstaticgirlnames";
   static final HashMap<UUID, HashMap<NpcType, String>> b = new HashMap<>();

   public AllieWorldData() {
      super("sexmod:customstaticgirlnames");
   }

   public AllieWorldData(String var1) {
      super("sexmod:customstaticgirlnames");
   }

   @SubscribeEvent
   public void a(Save var1) {
      World var2 = var1.getWorld();
      var2.func_175693_T().func_75745_a("sexmod:customstaticgirlnames", this);
      this.func_76185_a();
   }

   @SubscribeEvent
   public void a(Load var1) {
      World var2 = var1.getWorld();
      var2.func_175693_T().func_75742_a(AllieWorldData.class, "sexmod:customstaticgirlnames");
   }

   public static void a(UUID var0, NpcType var1, String var2) {
      HashMap var3 = b.get(var0);
      if (var3 == null) {
         var3 = new HashMap();
      }

      var3.put(var1, var2);
      b.put(var0, var3);
   }

   @Nullable
   public static String a(UUID var0, NpcType var1) {
      HashMap var2 = b.get(var0);
      return var2 == null ? null : (String)var2.get(var1);
   }

   public void func_76184_a(NBTTagCompound var1) {
      for (String var3 : var1.func_150296_c()) {
         UUID var4;
         try {
            var4 = UUID.fromString(var3);
         } catch (IllegalArgumentException var5) {
            continue;
         }

         b.put(var4, this.a(var1.func_74775_l(var3)));
      }
   }

   public NBTTagCompound func_189551_b(NBTTagCompound var1) {
      for (Entry var3 : b.entrySet()) {
         UUID var4 = (UUID)var3.getKey();
         var1.func_74782_a(var4.toString(), this.a_clash796((HashMap<NpcType, String>)var3.getValue()));
      }

      return var1;
   }

   private NBTTagCompound a_clash796(HashMap<NpcType, String> var1) {
      NBTTagCompound var2 = new NBTTagCompound();

      for (Entry var4 : var1.entrySet()) {
         var2.func_74778_a(((NpcType)var4.getKey()).name(), (String)var4.getValue());
      }

      return var2;
   }

   private HashMap<NpcType, String> a(NBTTagCompound var1) {
      HashMap var2 = new HashMap();

      for (NpcType var6 : NpcType.values()) {
         String var7 = var1.func_74779_i(var6.name());
         if (!"".equals(var7)) {
            var2.put(var6, var7);
         }
      }

      return var2;
   }

}
