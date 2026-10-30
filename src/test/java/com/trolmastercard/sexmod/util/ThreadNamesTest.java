package com.trolmastercard.sexmod.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Pins {@link ThreadNames#weightedRandomIndex(int)} to the best-first contract
 * its only caller relies on: {@code GalathFlightData} sorts flight candidates
 * by open air space (best first) and the pick must keep the best candidate
 * reachable, and the most likely.
 */
public class ThreadNamesTest {

   @Test
   public void degenerateBoundReturnsThatIndex() {
      assertEquals(0, ThreadNames.weightedRandomIndex(0));
   }

   @Test
   public void bestCandidateIsReachableAndFavoured() {
      int[] counts = new int[5];
      for (int i = 0; i < 4000; i++) {
         int index = ThreadNames.weightedRandomIndex(4);
         assertTrue("index out of range: " + index, index >= 0 && index <= 4);
         counts[index]++;
      }

      for (int index = 0; index < counts.length; index++) {
         assertTrue("index " + index + " was never picked", counts[index] > 0);
      }

      assertTrue("best index must outweigh the worst: " + counts[0] + " vs " + counts[4], counts[0] > counts[4]);
   }
}
