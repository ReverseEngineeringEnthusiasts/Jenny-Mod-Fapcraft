package com.trolmastercard.sexmod.entity;

import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;

/**
 * <b>Role.</b> The shared kobold/goblin color palette: each entry packs a
 * main and secondary skin color, the matching wool metadata id and a chat
 * color. Used by the kobold/goblin entities for body/eye colors (stored as
 * {@link Vec3i} in data keys or by name) and by the tribe chat messages.
 * <p>
 * <b>Pitfalls.</b> The enum order IS the persisted part-index order for
 * kobold/goblin customization (see {@link KoboldPlayerEntity#setCustomPartList})
 * — never reorder. All "safe" lookups fall back to {@link KoboldEntity#aJ}
 * (the PURPLE kobold default) on unknown input.
 */
public enum EyeAndKoboldColor {
   GREEN(69, 141, 113, 91, 167, 128, 9, TextFormatting.DARK_GREEN),
   YELLOW(241, 177, 77, 255, 226, 170, 4, TextFormatting.YELLOW),
   RED(230, 27, 57, 253, 232, 239, 14, TextFormatting.RED),
   PURPLE(196, 148, 207, 246, 188, 96, 10, TextFormatting.DARK_PURPLE),
   LIGHT_GREEN(170, 208, 47, 230, 214, 104, 5, TextFormatting.GREEN),
   OLD_BLUE(173, 138, 128, 118, 151, 180, 2, TextFormatting.LIGHT_PURPLE),
   DARK_GREY(92, 92, 110, 198, 193, 165, 7, TextFormatting.DARK_GRAY),
   BROWN(200, 145, 112, 253, 228, 198, 12, TextFormatting.GOLD),
   DARK_BLUE(65, 84, 116, 104, 137, 146, 11, TextFormatting.DARK_BLUE),
   LIGHT_BLUE(100, 163, 206, 138, 235, 242, 3, TextFormatting.DARK_AQUA),
   SILVER(136, 136, 134, 255, 255, 255, 0, TextFormatting.GRAY);

   private final Vec3i mainColor;
   private final Vec3i secondaryColor;
   private final int woolMeta;
   private final TextFormatting textColor;

   EyeAndKoboldColor(int mainColorR, int mainColorG, int mainColorB, int secondaryColorR, int secondaryColorG, int secondaryColorB, int woolMeta, TextFormatting textFormat) {
      this.mainColor = new Vec3i(mainColorR, mainColorG, mainColorB);
      this.secondaryColor = new Vec3i(secondaryColorR, secondaryColorG, secondaryColorB);
      this.woolMeta = woolMeta;
      this.textColor = textFormat;
   }

   public static int indexOf(EyeAndKoboldColor color) {
      int index = 0;

      for (EyeAndKoboldColor entry : values()) {
         if (color == entry) {
            return index;
         }

         index++;
      }

      return index;
   }

   public static EyeAndKoboldColor safeValueOf(String name) {
      try {
         return valueOf(name);
      } catch (IllegalArgumentException ex) {
         return KoboldEntity.aJ;
      }
   }

   public static EyeAndKoboldColor safeValueOf(Vec3i colorVec) {
      for (EyeAndKoboldColor entry : values()) {
         if (colorVec.equals(entry.getMainColor())) {
            return entry;
         }
      }

      return KoboldEntity.aJ;
   }

   public static EyeAndKoboldColor getColorByWoolId(int woolMeta) {
      for (EyeAndKoboldColor entry : values()) {
         if (entry.getWoolMeta() == woolMeta) {
            return entry;
         }
      }

      return KoboldEntity.aJ;
   }

   public Vec3i getMainColor() {
      return this.mainColor;
   }

   public Vec3i getSecondaryColor() {
      return this.secondaryColor;
   }

   public int getWoolMeta() {
      return this.woolMeta;
   }

   public TextFormatting getTextColor() {
      return this.textColor;
   }

}
