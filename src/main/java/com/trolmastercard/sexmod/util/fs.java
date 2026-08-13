package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import java.util.HashMap;
import java.util.UUID;

public class fs {
   static HashMap<UUID, BaseGirlEntity> a = new HashMap<>();

   public static void b_clash710(BaseGirlEntity var0) {
      a.put(var0.f_clash491(), var0);
   }

   public static void a_clash711(BaseGirlEntity var0) {
      a.remove(var0.f_clash491());
   }

   public static void a_clash712() {
      a.clear();
   }

   public static BaseGirlEntity a_clash713(UUID var0) {
      return a.get(var0);
   }
}
