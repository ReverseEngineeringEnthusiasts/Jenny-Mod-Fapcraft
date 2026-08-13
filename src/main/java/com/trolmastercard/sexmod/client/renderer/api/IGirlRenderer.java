package com.trolmastercard.sexmod.client.renderer.api;

import com.trolmastercard.sexmod.entity.BodyParts;







import java.util.HashSet;
import software.bernie.geckolib3.geo.render.built.GeoBone;

public interface IGirlRenderer {
   default HashSet<String> a() {
      return BodyParts.CUSTOM_PART_BONES;
   }

   default boolean a(HashSet<String> var1, GeoBone var2) {
      while (var2.parent != null) {
         String var3 = var2.getName();
         if (var1.contains(var3)) {
            return false;
         }

         if (var3.startsWith("armor")) {
            return false;
         }

         var2 = var2.parent;
      }

      return true;
   }
}
