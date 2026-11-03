package com.trolmastercard.sexmod.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.image.BufferedImage;
import org.junit.Test;

/**
 * Guards the skin-override image contract: the geo models bake their UVs for a
 * 64x64 skin, so every accepted image must come out 64x64 and anything else
 * (legacy 64x32, tiny sheets) must be rejected instead of rendering garbage.
 */
public class PlayerSkinTest {

   private static BufferedImage image(int width, int height) {
      return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
   }

   @Test
   public void passesThroughModelSizedSkins() {
      BufferedImage skin = image(64, 64);
      assertEquals(skin, PlayerSkinOverride.normalize(skin));
   }

   @Test
   public void downscalesHdSkins() {
      assertEquals(64, PlayerSkinOverride.normalize(image(128, 128)).getWidth());
      assertEquals(64, PlayerSkinOverride.normalize(image(512, 512)).getHeight());
   }

   @Test
   public void rejectsLegacyAndTinySheets() {
      assertNull("64x32 legacy layout is not the 64x64 model layout", PlayerSkinOverride.normalize(image(64, 32)));
      assertNull(PlayerSkinOverride.normalize(image(32, 32)));
      assertNull(PlayerSkinOverride.normalize(null));
   }
}
