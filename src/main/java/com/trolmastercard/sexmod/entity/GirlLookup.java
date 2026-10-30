package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.util.ClientServerCheck;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Registry-free lookups over the live girl entities (all worlds on the
 * integrated/dedicated server, the client world otherwise).
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}; every method keeps its
 * original {@code BaseGirlEntity} static delegate, so call sites elsewhere do
 * not change.
 */
final class GirlLookup {
   private GirlLookup() {
   }

   static List<BaseGirlEntity> getGirlEntityList() {
      if (!ClientServerCheck.getInstance()) {
         return getClientGirls();
      }

      WorldServer[] worlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
      if (worlds.length == 0) {
         return new ArrayList<>();
      }

      ArrayList girls = new ArrayList();

      for (WorldServer world : worlds) {
         girls.addAll(world.getEntities(BaseGirlEntity.class, entity -> true));
      }

      return girls;
   }

   @SideOnly(Side.CLIENT)
   private static List<BaseGirlEntity> getClientGirls() {
      WorldClient world = Minecraft.getMinecraft().world;
      return world == null ? new ArrayList<>() : world.getEntities(BaseGirlEntity.class, entity -> true);
   }

   static BaseGirlEntity getClientGirlEntity(UUID girlId) {
      if (girlId == null) {
         return null;
      }

      for (BaseGirlEntity girl : girlList(girlId)) {
         if (girl.world.isRemote) {
            return girl;
         }
      }

      return null;
   }

   static BaseGirlEntity getServerGirlEntity(UUID girlId) {
      if (girlId == null) {
         return null;
      }

      for (BaseGirlEntity girl : girlList(girlId)) {
         if (!girl.world.isRemote) {
            return girl;
         }
      }

      return null;
   }

   static ArrayList<BaseGirlEntity> girlList(UUID girlId) {
      ArrayList girls = new ArrayList();

      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (girl != null && girl.getGirlId().equals(girlId)) {
               girls.add(girl);
            }
         }
      } catch (ConcurrentModificationException ex) {
         Main.LOGGER.warn("had a ConcurrentModificationException while cycling through the girl list... hopefully nothin borke owo", ex);
      }

      return girls;
   }

   static BaseGirlEntity getCompanionInteractingWithPlayer(EntityPlayer player) {
      return player == null ? null : getGirlByUUID(player.getPersistentID());
   }

   static BaseGirlEntity getGirlByUUID(@Nonnull UUID uuid) {
      return getGirlByUUID(uuid, (Boolean)null);
   }

   static BaseGirlEntity getGirlByUUID(@Nonnull UUID uuid, Boolean serverOnly) {
      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (!girl.isDead && uuid.equals(girl.getInteractionPlayerUUID())) {
               if (serverOnly == null) {
                  return girl;
               }

               boolean isRemote = girl.world.isRemote;
               if (isRemote && !serverOnly) {
                  return girl;
               }

               if (!isRemote && serverOnly) {
                  return girl;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
         // ignored: entity list mutated during iteration; retried on the next tick
      }

      return null;
   }

   @Nullable
   static BaseGirlEntity getActiveSceneInfo(@Nonnull UUID uuid) {
      boolean noServer = FMLCommonHandler.instance().getMinecraftServerInstance() == null;

      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (!girl.isDead) {
               boolean isRemote = girl.world.isRemote;
               if (isRemote == noServer && uuid.equals(girl.getInteractionPlayerUUID())) {
                  return girl;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
         // ignored: entity list mutated during iteration; retried on the next tick
      }

      return null;
   }

   static BaseGirlEntity getActiveSceneInfo(@Nonnull EntityPlayer player) {
      return getActiveSceneInfo(player.getPersistentID());
   }
}
