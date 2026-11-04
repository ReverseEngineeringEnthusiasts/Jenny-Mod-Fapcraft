package com.trolmastercard.sexmod.reskin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Test;

/**
 * Guards the downloader's path/partial helpers: a pack path may never escape
 * the models folder, a .part file must be resumable from its length, and sizes
 * must render readably in the GUI.
 */
public class ModelDownloadManagerTest {

   @Test
   public void mapsArchivePathsUnderTheRoot() throws Exception {
      File root = new File(System.getProperty("java.io.tmpdir"), "sexmod-dl-test-root");
      File dest = ModelDownloadManager.destFor(root, "Jenny/Pack/Pack.zip");
      assertTrue(dest.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator));
   }

   @Test
   public void rejectsTraversal() throws Exception {
      File root = new File(System.getProperty("java.io.tmpdir"), "sexmod-dl-test-root");
      try {
         ModelDownloadManager.destFor(root, "../outside.zip");
         fail("traversal must be rejected");
      } catch (IOException expected) {
         // expected
      }
   }

   @Test
   public void readsTheResumeOffsetFromThePartFile() throws Exception {
      File part = File.createTempFile("sexmod-part", ".part");
      part.deleteOnExit();
      try (FileOutputStream out = new FileOutputStream(part)) {
         out.write(new byte[4096]);
      }
      assertEquals(4096L, ModelDownloadManager.resumeOffset(part));
      assertEquals(0L, ModelDownloadManager.resumeOffset(new File(part.getPath() + ".missing")));
   }

   @Test
   public void formatsBytes() {
      assertEquals("512 B", ModelDownloadManager.formatBytes(512));
      assertEquals("1.5 KB", ModelDownloadManager.formatBytes(1536));
      assertEquals("2.0 MB", ModelDownloadManager.formatBytes(2L * 1024 * 1024));
   }
}
