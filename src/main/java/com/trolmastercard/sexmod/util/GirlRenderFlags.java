package com.trolmastercard.sexmod.util;
/**
 * <b>Role.</b> Per-frame render flags shared between the girl renderers and the
 * scene camera code. The {@code flag*} booleans are written by renderers and
 * read by {@code GirlRenderer} layers to decide what to draw (hair, clothing,
 * extra parts). The single {@link #instance} is reassigned each render frame —
 * do not cache it across frames.
 */
public class GirlRenderFlags {
   public static GirlRenderFlags instance;
   public boolean flagC;
   public boolean flagD;
   public boolean flagB;

   public GirlRenderFlags(boolean var1, boolean var2, boolean var3) {
      this.flagC = var1;
      this.flagD = var2;
      this.flagB = var3;
   }
}
