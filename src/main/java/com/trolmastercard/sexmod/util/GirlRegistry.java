package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>Role.</b> Global girl UUID registry (girlId -> entity instance). Kept in
 * sync by {@link BaseGirlEntity} (register on spawn, unregister on removal) and
 * cleared on world close. Used by {@code setLocallyRegistered} and fast UUID
 * lookups that must not scan the entity list.
 * <p>
 * <b>Pitfall.</b> Entries must be removed when girls despawn or stale references
 * keep dead entities alive. Do not use it across worlds. The map is concurrent
 * because preview girls are (un)registered on the render thread while the
 * server tick reads it.
 */
public class GirlRegistry {
   static ConcurrentHashMap<UUID, BaseGirlEntity> girlsById = new ConcurrentHashMap<>();

   public static void registerGirl(BaseGirlEntity girl) {
      girlsById.put(girl.getGirlId(), girl);
   }

   public static void unregisterGirl(BaseGirlEntity girl) {
      girlsById.remove(girl.getGirlId());
   }

   public static void clearAll() {
      girlsById.clear();
   }

   public static BaseGirlEntity getGirl(UUID uuid) {
      return girlsById.get(uuid);
   }
}
