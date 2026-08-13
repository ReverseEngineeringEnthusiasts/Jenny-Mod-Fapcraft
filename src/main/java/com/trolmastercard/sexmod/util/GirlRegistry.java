package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import java.util.HashMap;
import java.util.UUID;

public class GirlRegistry {
   static HashMap<UUID, BaseGirlEntity> a = new HashMap<>();

   public static void b_clash710(BaseGirlEntity var0) {
      a.put(var0.getGirlId(), var0);
   }

   public static void a_clash711(BaseGirlEntity var0) {
      a.remove(var0.getGirlId());
   }

   public static void clearAll() {
      a.clear();
   }

   public static BaseGirlEntity getGirl(UUID var0) {
      return a.get(var0);
   }
}
