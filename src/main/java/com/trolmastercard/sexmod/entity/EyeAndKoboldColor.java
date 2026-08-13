package com.trolmastercard.sexmod.entity;


import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;

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

   EyeAndKoboldColor(int var3, int var4, int var5, int var6, int var7, int var8, int var9, TextFormatting var10) {
      this.mainColor = new Vec3i(var3, var4, var5);
      this.secondaryColor = new Vec3i(var6, var7, var8);
      this.woolMeta = var9;
      this.textColor = var10;
   }

   public static int indexOf(EyeAndKoboldColor var0) {
      int var1 = 0;

      for (EyeAndKoboldColor var5 : values()) {
         if (var0 == var5) {
            return var1;
         }

         var1++;
      }

      return var1;
   }

   public static EyeAndKoboldColor safeValueOf(String var0) {
      try {
         return valueOf(var0);
      } catch (IllegalArgumentException var1) {
         return KoboldEntity.aJ;
      }
   }

   public static EyeAndKoboldColor safeValueOf(Vec3i var0) {
      for (EyeAndKoboldColor var4 : values()) {
         if (var0.equals(var4.getMainColor())) {
            return var4;
         }
      }

      return KoboldEntity.aJ;
   }

   public static EyeAndKoboldColor getColorByWoolId(int var0) {
      for (EyeAndKoboldColor var4 : values()) {
         if (var4.getWoolMeta() == var0) {
            return var4;
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
