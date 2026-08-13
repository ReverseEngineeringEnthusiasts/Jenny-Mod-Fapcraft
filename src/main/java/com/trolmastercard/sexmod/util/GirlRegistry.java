package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import java.util.HashMap;
import java.util.UUID;

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
