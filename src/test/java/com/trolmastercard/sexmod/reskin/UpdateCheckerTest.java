package com.trolmastercard.sexmod.reskin;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Pure-logic checks for {@link UpdateChecker} version handling (no MC classes). */
public class UpdateCheckerTest {

   @Test
   public void parseVersionHandlesCommonTagFormats() {
      assertArrayEquals(new int[]{7, 0, 0}, UpdateChecker.parseVersion("v7"));
      assertArrayEquals(new int[]{7, 0, 0}, UpdateChecker.parseVersion("7"));
      assertArrayEquals(new int[]{7, 0, 0}, UpdateChecker.parseVersion("V7"));
      assertArrayEquals(new int[]{1, 2, 3}, UpdateChecker.parseVersion("1.2.3"));
      assertArrayEquals(new int[]{1, 2, 3}, UpdateChecker.parseVersion("v1.2.3"));
      assertArrayEquals(new int[]{1, 2, 3}, UpdateChecker.parseVersion("1.2.3-SNAPSHOT"));
      assertArrayEquals(new int[]{1, 2, 3}, UpdateChecker.parseVersion("1.2.3-beta.4"));
      assertArrayEquals(new int[]{1, 2, 1}, UpdateChecker.parseVersion("1.2 (1)"));
      assertArrayEquals(new int[]{0, 0, 0}, UpdateChecker.parseVersion(""));
      assertArrayEquals(new int[]{0, 0, 0}, UpdateChecker.parseVersion(null));
   }

   @Test
   public void compareIsNumeric() {
      assertEquals(0, UpdateChecker.compare(new int[]{1, 2, 3}, new int[]{1, 2, 3}));
      assertTrue(UpdateChecker.compare(new int[]{1, 2, 3}, new int[]{1, 2, 10}) < 0);
      assertTrue(UpdateChecker.compare(new int[]{1, 10, 0}, new int[]{1, 9, 9}) > 0);
      assertTrue(UpdateChecker.compare(new int[]{2, 0, 0}, new int[]{1, 99, 99}) > 0);
   }

   @Test
   public void isOutdatedComparesLocalAgainstLatest() {
      assertTrue(UpdateChecker.isOutdated("1.1.1", "v1.2.0"));
      assertTrue(UpdateChecker.isOutdated("1.1.1", "1.1.2"));
      assertFalse(UpdateChecker.isOutdated("1.2.0", "v1.2.0"));
      assertFalse(UpdateChecker.isOutdated("1.2.1", "v1.2.0"));
      assertFalse(UpdateChecker.isOutdated(null, "v1.2.0"));
      assertFalse(UpdateChecker.isOutdated("1.1.1", null));
   }

   @Test
   public void isOutdatedBuildUsesBuildTagsAndFallsBackToSemver() {
      assertTrue(UpdateChecker.isOutdatedBuild(8, "v9"));
      assertTrue(UpdateChecker.isOutdatedBuild(8, "9"));
      assertFalse(UpdateChecker.isOutdatedBuild(8, "v8"));
      assertFalse(UpdateChecker.isOutdatedBuild(9, "8"));
      assertFalse(UpdateChecker.isOutdatedBuild(8, null));
      // not build-shaped -> semver-ish fallback, localBuild is compared as major
      assertTrue(UpdateChecker.isOutdatedBuild(8, "v9.0.0"));
      assertFalse(UpdateChecker.isOutdatedBuild(8, "v1.2.3"));
   }
}
