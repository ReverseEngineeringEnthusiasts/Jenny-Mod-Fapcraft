package com.trolmastercard.sexmod.api;

import net.minecraft.util.math.Vec3i;

/**
 * Skin colors of goblin NPCs/players, encoded as RGB triples.
 * Stored in the goblin DNA model-code string at index 7 ("variant"/"boob" bones).
 */
public enum SkinColor {
   LIGHT_GREEN(213, 239, 150),
   MEDIUM_GREEN(189, 165, 91),
   DARK_GREEN(160, 183, 135),
   LIGHT_YELLOW(234, 176, 102),
   LIGHT_BLUE(187, 203, 252);

   private final Vec3i color;

   SkinColor(int red, int green, int blue) {
      this.color = new Vec3i(red, green, blue);
   }

   /** @return the RGB color value of this skin tone */
   public Vec3i getColor() {
      return this.color;
   }

   /** @return the ordinal index of the given skin color within this enum */
   public static int indexOf(SkinColor color) {
      int i = 0;
      for (SkinColor value : values()) {
         if (color == value) {
            return i;
         }
         i++;
      }
      return i;
   }
}
