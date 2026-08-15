package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.Reference;

/**
 * <b>Role.</b> Classifies girl model bones for the custom-model system:
 * which bones are part-specific (head, feet, hands) and which are fully
 * custom ({@link #CUSTOM_BONE}). Also owns the icon sprite X positions used by
 * the wardrobe GUI and allocates the +/- button ids via
 * {@link Reference#BUTTON_ID_COUNTER}.
 * <p>
 * <b>Pitfalls.</b> The enum declaration ORDER matters: {@link #GIRL_SPECIFIC}
 * must stay first (it has no bone name) and {@link #getCustomBoneCount()}
 * assumes the last two entries are the custom ones. The button ids are
 * allocated at class-load time — do not reorder the enum or existing saves
 * map to different buttons.
 */
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
      this.buttonIDPlus = ++Reference.BUTTON_ID_COUNTER;
      this.buttonIDMinus = ++Reference.BUTTON_ID_COUNTER;
   }

   public static int getCustomBoneCount() {
      return values().length - 2;
   }
}
