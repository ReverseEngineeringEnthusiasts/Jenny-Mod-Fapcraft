package com.trolmastercard.sexmod.util;

import net.minecraft.util.math.Vec3i;

/**
 * Hair colors of goblin NPCs/players, encoded as RGB triples.
 * Stored in the goblin DNA model-code string at index 6 ("hair" bones).
 */
public enum HairColor {
   PURPLE(103, 39, 123),
   ORANGE(251, 153, 56),
   BLACK(30, 33, 38),
   BLUE(88, 83, 186),
   BROWN(63, 35, 34),
   PINK(247, 102, 109),
   RED(241, 69, 49),
   GREEN(75, 143, 106);

   private final Vec3i color;

   HairColor(int red, int green, int blue) {
      this.color = new Vec3i(red, green, blue);
   }

   /** @return the RGB color value of this hair color */
   public Vec3i getColor() {
      return this.color;
   }

   /** @return the ordinal index of the given hair color within this enum */
   public static int indexOf(HairColor color) {
      int i = 0;
      for (HairColor value : values()) {
         if (color == value) {
            return i;
         }
         i++;
      }
      return i;
   }
}
