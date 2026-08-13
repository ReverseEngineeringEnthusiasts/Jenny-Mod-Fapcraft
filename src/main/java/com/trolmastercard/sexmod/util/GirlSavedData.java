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

public class GirlSavedData extends WorldSavedData {
   public static boolean f = true;
   public static final float c = 60.0F;
   public static final String e = "sexmod:galath_owner_ship";
   public static final String d = "sexmod:ownershipdata";
   public static final String g = "sexmod:mangownershipdata";
   static final long a = 0L;
   static BiDirectionalMap<UUID, UUID> h = new BiDirectionalMap<>();
   static HashMap<UUID, Long> b = new HashMap<>();
   static HashSet<UUID> i = new HashSet<>();

   public GirlSavedData() {
      super("sexmod:galath_owner_ship");
   }

   public GirlSavedData(String var1) {
      super("sexmod:galath_owner_ship");
   }

   public static void clearAll() {
      i.clear();
      h.b_clash769();
   }

   public static void e_clash845(UUID var0) {
      UUID var1 = f_clash850(var0);
      if (var1 != null) {
         i.add(var1);
      }
   }

   public static boolean b_clash846(UUID var0) {
      return i.contains(var0);
   }

   public static boolean c_clash847(GalathEntity var0) {
      UUID var1 = h.b(var0.getGirlId());
      if (var1 == null) {
         return false;
      } else {
         World var2 = var0.world;
         EntityPlayer var3 = var2.getPlayerEntityByUUID(var1);
         if (var3 == null) {
            return true;
         } else {
            return var3.dimension != var0.dimension ? false : !(var3.getDistance(var0) > 60.0F);
         }
      }
   }

   public static boolean b(EntityPlayer var0, GalathEntity var1) {
      return var1.getGirlId().equals(h.c(var0.getPersistentID()));
   }

   public static void a_clash848(GalathEntity var0) {
      ManglelieEntity var1 = var0.getMangleliePartner(true);
      if (var1 != null) {
         var0.world.removeEntity(var1);
      }

      UUID var2 = h.b(var0.getGirlId());
      if (var2 == null) {
         var0.world.removeEntity(var0);
      } else {
         World var3 = var0.world;
         EntityPlayer var4 = var3.getPlayerEntityByUUID(var2);
         var0.world.removeEntity(var0);
         h.a(var2);
         if (var4 != null) {
            PacketHandler.b.sendTo(new InformOfOwnershipPacket(false), (EntityPlayerMP)var4);
         }
      }
   }

   public static boolean c_clash849(UUID var0) {
      return h.c(var0) != null;
   }

   public static UUID f_clash850(UUID var0) {
      return h.b(var0);
   }

   public static UUID b_clash851(GalathEntity var0) {
      return var0 == null ? null : f_clash850(var0.getGirlId());
   }

   public static UUID a_clash852(UUID var0) {
      return h.c(var0);
   }

   public static UUID b_clash853(EntityPlayer var0) {
      return var0 == null ? null : a_clash852(var0.getPersistentID());
   }

   public static void a(UUID var0, UUID var1) {
      h.a(var0, var1);
   }

   public static void a(EntityPlayer var0, GalathEntity var1) {
      if (var0 != null) {
         if (var1 != null) {
            a(var0.getPersistentID(), var1.getGirlId());
         }
      }
   }

   public static void d_clash854(UUID var0) {
      h.a(var0);
   }

   public static void a_clash855(EntityPlayer var0) {
      if (var0 != null) {
         d_clash854(var0.getPersistentID());
      }
   }

   public static boolean a_clash856(UUID var0, World var1) {
      Long var2 = b.get(var0);
      if (!b_clash846(var0)) {
         return false;
      } else {
         return var2 == null ? true : var1.getTotalWorldTime() - var2 > 0L;
      }
   }

   public static void a(UUID var0, Long var1) {
      if (var0 == null) {
         Main.LOGGER.log(Level.WARN, "tried to save last cum dosage time on NULL player");
      } else {
         b.put(var0, var1);
      }
   }

   @SubscribeEvent
   public void a(ServerTickEvent var1) {
      if (var1.phase == Phase.END) {
         World var2 = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
         ArrayList var3 = new ArrayList();

         for (Entry var5 : h.c_clash766()) {
            UUID var6 = (UUID)var5.getKey();
            UUID var7 = (UUID)var5.getValue();
            EntityPlayer var8 = var2.getPlayerEntityByUUID(var6);
            if (var8 != null && BaseGirlEntity.getServerGirlEntity(var7) == null) {
               var3.add(var8);
            }
         }

         for (EntityPlayer var10 : (java.util.Collection<EntityPlayer>) (var3) ) {
            h.a(var10.getPersistentID());
            PacketHandler.b.sendTo(new InformOfOwnershipPacket(false), (EntityPlayerMP)var10);
         }
      }
   }

   @SubscribeEvent
   public void a(Save var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().setData("sexmod:galath_owner_ship", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void a(Load var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().getOrLoadData(GirlSavedData.class, "sexmod:galath_owner_ship");
   }

   public void readFromNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = var1.getCompoundTag("sexmod:ownershipdata");
      int var3 = var2.getInteger("amount");

      for (int var4 = 0; var4 < var3; var4++) {
         UUID var5 = var2.getUniqueId("master" + var4);
         UUID var6 = var2.getUniqueId("galath" + var4);
         long var7 = var2.getLong("lastcumdosage" + var4);
         if (var5 != null && var6 != null) {
            h.a(var5, var6);
            b.put(var5, var7);
         } else {
            Main.LOGGER.fatal("OMFG WHOOP WHOOP SAVING DIDNT WORK CORRECTLY AAAAAAAAAAA");
         }
      }

      NBTTagCompound var9 = var1.getCompoundTag("sexmod:mangownershipdata");

      for (int var10 = 0; var9.hasUniqueId("mang" + var10); var10++) {
         i.add(var9.getUniqueId("mang" + var10));
      }

      var1.setTag("sexmod:mangownershipdata", new NBTTagCompound());
      var1.setTag("sexmod:ownershipdata", new NBTTagCompound());
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = new NBTTagCompound();
      var2.setInteger("amount", h.e_clash765());
      int var3 = 0;

      for (Entry var5 : h.c_clash766()) {
         UUID var6 = (UUID)var5.getKey();
         UUID var7 = (UUID)var5.getValue();
         Long var8 = b.get(var6);
         if (var8 == null) {
            var8 = 0L;
         }

         var2.setUniqueId("galath" + var3, var7);
         var2.setUniqueId("master" + var3, var6);
         var2.setLong("lastcumdosage" + var3, var8);
         var3++;
      }

      NBTTagCompound var10 = new NBTTagCompound();
      var3 = 0;

      for (UUID var12 : i) {
         var10.setUniqueId("mang" + var3++, var12);
      }

      var1.setTag("sexmod:ownershipdata", var2);
      var1.setTag("sexmod:mangownershipdata", var10);
      return var1;
   }

}
