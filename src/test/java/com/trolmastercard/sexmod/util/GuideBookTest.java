package com.trolmastercard.sexmod.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Guards the guide-book copy: the first-join book is the only in-game
 * documentation, so it must keep covering the features it was added for and
 * stay within what a vanilla book page can show.
 */
public class GuideBookTest {

   @Test
   public void coversEveryFeatureInTheBook() {
      String book = String.join("\n", GuideBook.pages());
      assertTrue(book.contains("sexmod/player_skin"));
      assertTrue(book.contains("steve.png"));
      assertTrue(book.contains("alex.png"));
      assertTrue(book.contains("GIRL WAND"));
      assertTrue(book.contains("WARDROBE"));
      assertTrue(book.contains("YOUR GIRL"));
      assertTrue(book.contains("EQUIPMENT"));
      assertTrue(book.contains("KOBOLDS"));
      assertTrue(book.contains("GOBLINS"));
      assertTrue(book.contains("DRAGON STAFF"));
      assertTrue(book.contains("HORNY POTION"));
      assertTrue(book.contains("custom_models"));
   }

   @Test
   public void pagesFitTheVanillaBook() {
      for (String page : GuideBook.pages()) {
         assertTrue("page overflows a vanilla book page: " + page.length(), page.length() <= 260);
         assertFalse("em dash in book copy", page.contains("\u2014"));
         assertFalse("en dash in book copy", page.contains("\u2013"));
      }
   }
}
