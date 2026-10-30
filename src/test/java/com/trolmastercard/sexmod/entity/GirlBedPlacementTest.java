package com.trolmastercard.sexmod.entity;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.Test;

/**
 * Pins the bed-side table that moved out of Jenny, Luna and Bia into
 * {@link GirlBedPlacement}: a mistyped offset or yaw would silently move every
 * bed scene, and the scan itself needs a live world to exercise.
 */
public class GirlBedPlacementTest {

   @Test
   public void sideTableMatchesTheOriginalGirls() {
      assertArrayEquals(new int[]{0, 180, -90, 90}, GirlBedPlacement.SIDE_YAWS);
      assertEquals("one entry per bed side", 4, GirlBedPlacement.SIDE_OFFSETS.length);

      assertSide(0, 0.5, 0.0, -0.5, 0.0, 0.0, -1.0);
      assertSide(1, 0.5, 0.0, 1.5, 0.0, 0.0, 1.0);
      assertSide(2, -0.5, 0.0, 0.5, -1.0, 0.0, 0.0);
      assertSide(3, 1.5, 0.0, 0.5, 1.0, 0.0, 0.0);
   }

   private static void assertSide(int index, double standX, double standY, double standZ, double openX, double openY, double openZ) {
      Vec3d stand = GirlBedPlacement.SIDE_OFFSETS[index][0];
      Vec3d open = GirlBedPlacement.SIDE_OFFSETS[index][1];
      assertEquals("stand x of side " + index, standX, stand.x, 0.0);
      assertEquals("stand y of side " + index, standY, stand.y, 0.0);
      assertEquals("stand z of side " + index, standZ, stand.z, 0.0);
      assertEquals("open x of side " + index, openX, open.x, 0.0);
      assertEquals("open y of side " + index, openY, open.y, 0.0);
      assertEquals("open z of side " + index, openZ, open.z, 0.0);
   }
}
