package com.trolmastercard.sexmod.reskin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

/**
 * Guards the archive index contract: only pack archives become entries, the
 * raw URL is percent-encoded per segment, and the list is stable (girl, then
 * pack name) so the GUI does not jump around.
 */
public class ArchiveIndexTest {

   private static final String TREE = "{\"tree\":["
      + "{\"type\":\"blob\",\"path\":\"Jenny/My Pack/My Pack.zip\",\"size\":1234},"
      + "{\"type\":\"blob\",\"path\":\"Allie/Old/pack.rar\",\"size\":99},"
      + "{\"type\":\"blob\",\"path\":\"Allie/Old/seven.7z\",\"size\":7},"
      + "{\"type\":\"blob\",\"path\":\"Allie/Old/preview.png\",\"size\":500},"
      + "{\"type\":\"tree\",\"path\":\"Allie\"},"
      + "{\"type\":\"blob\",\"path\":\"README.md\",\"size\":10}"
      + "]}";

   @Test
   public void keepsOnlyPackArchivesAndSortsThem() {
      List<ArchiveIndex.Pack> packs = ArchiveIndex.parseTree(TREE);
      assertEquals(3, packs.size());
      assertEquals("pack", packs.get(0).name);
      assertEquals("Allie", packs.get(0).group);
      assertEquals("seven", packs.get(1).name);
      assertEquals("My Pack", packs.get(2).name);
      assertEquals("Jenny", packs.get(2).group);
      assertEquals(1234L, packs.get(2).size);
   }

   @Test
   public void buildsAnEncodedRawUrl() {
      List<ArchiveIndex.Pack> packs = ArchiveIndex.parseTree(TREE);
      ArchiveIndex.Pack myPack = packs.get(2);
      assertTrue(myPack.url.startsWith(ArchiveIndex.RAW_BASE));
      assertTrue(myPack.url.endsWith("Jenny/My%20Pack/My%20Pack.zip"));
   }

   @Test
   public void recognisesPackExtensions() {
      assertTrue(ArchiveIndex.isPackFile("A/B.ZIP"));
      assertTrue(ArchiveIndex.isPackFile("A/B.rar"));
      assertTrue(ArchiveIndex.isPackFile("A/B.7z"));
      assertFalse(ArchiveIndex.isPackFile("A/B.png"));
      assertFalse(ArchiveIndex.isPackFile("A/B.zip.txt"));
   }

   @Test
   public void encodesEverySegmentWithoutLosingSlashes() {
      assertEquals("A%20b/%28c%29.zip", ArchiveIndex.encodePath("A b/(c).zip"));
   }
}
