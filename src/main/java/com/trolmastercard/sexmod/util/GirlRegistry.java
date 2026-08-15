package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import java.util.HashMap;
import java.util.UUID;

/**
 * <b>Role.</b> Global girl UUID registry (girlId -> entity instance). Kept in
 * sync by {@link BaseGirlEntity} (register on spawn, unregister on removal) and
 * cleared on world close. Used by {@code setLocallyRegistered} and fast UUID
 * lookups that must not scan the entity list.
 * <p>
 * <b>Pitfall.</b> The registry is a plain static map — entries must be removed
 * when girls despawn or stale references keep dead entities alive. Do not use it
 * across worlds.
 */
public class GirlRegistry {
   static HashMap<UUID, BaseGirlEntity> a = new HashMap<>();

   public static void registerGirl(BaseGirlEntity var0) {
      a.put(var0.getGirlId(), var0);
   }

   public static void unregisterGirl(BaseGirlEntity var0) {
      a.remove(var0.getGirlId());
   }

   public static void clearAll() {
      a.clear();
   }

   public static BaseGirlEntity getGirl(UUID var0) {
      return a.get(var0);
   }
}
