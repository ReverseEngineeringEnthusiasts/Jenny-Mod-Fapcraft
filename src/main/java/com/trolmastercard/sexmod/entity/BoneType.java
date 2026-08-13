package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.Reference;







public enum BoneType {
   GIRL_SPECIFIC,
   HEAD(0, "customHead"),
   FOOT_L(60, "customShoeL"),
   FOOT_R(80, "customShoeR"),
   HAND_L(100, "customHandL"),
   HAND_R(120, "customHandR"),
   CUSTOM_BONE(140);

   public static final String SEPARATOR = "#";
   public int buttonIDPlus;
   public int buttonIDMinus;
   public String boneName = null;
   public int iconXPos = 0;

   BoneType() {
   }

   BoneType(int var3) {
      this.iconXPos = var3;
   }

   BoneType(int var3, String var4) {
      this.iconXPos = var3;
      this.boneName = var4;
      this.buttonIDPlus = ++Reference.i;
      this.buttonIDMinus = ++Reference.i;
   }

   public static int a_clash759() {
      return values().length - 2;
   }
}
