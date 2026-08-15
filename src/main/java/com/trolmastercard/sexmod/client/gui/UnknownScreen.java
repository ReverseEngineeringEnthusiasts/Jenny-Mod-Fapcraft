package com.trolmastercard.sexmod.client.gui;
/**
 * Plain RGBA color holder used by the mod's custom model/part rendering code
 * (e.g. tinting custom parts on the {@link ClothingScreen}). No logic — just a
 * value object with a public color tuple.
 */
public class UnknownScreen {
   public int red;
   public int green;
   public int blue;
   public int alpha;

   public UnknownScreen(int red, int green, int blue, int alpha) {
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
   }
}
