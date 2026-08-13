package com.trolmastercard.sexmod.util;

import net.minecraft.util.math.Vec3i;

/**
 * Eye colors of goblin NPCs/players, encoded as RGB triples.
 * Stored in the goblin DNA model-code string at index 8 ("eyeColor"/"eyeColor2" bones).
 */
public enum EyeColor {
   RED(255, 0, 0),
   VIOLET(132, 30, 156),
   YELLOW(243, 247, 0),
   BROWN(105, 60, 9),
   TURKEY(0, 206, 217),
   BLUE(0, 0, 255);

   private final Vec3i color;

   EyeColor(int red, int green, int blue) {
      this.color = new Vec3i(red, green, blue);
   }

   /** @return the RGB color value of this eye color */
   public Vec3i getColor() {
      return this.color;
   }

   /** @return the eye color matching the given RGB value, or RED if none matches */
   public static EyeColor fromColor(Vec3i rgb) {
      for (EyeColor eyeColor : values()) {
         if (rgb.equals(eyeColor.getColor())) {
            return eyeColor;
         }
      }
      return RED;
   }

   /** @return the ordinal index of the given eye color within this enum */
   public static int indexOf(EyeColor color) {
      int colorIndex = 0;
      for (EyeColor value : values()) {
         if (color == value) {
            return colorIndex;
         }
         colorIndex++;
      }
      return colorIndex;
   }
}
