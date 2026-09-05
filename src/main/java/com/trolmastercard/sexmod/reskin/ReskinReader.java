package com.trolmastercard.sexmod.reskin;

import be.stef.rar.ExtractionResult;
import be.stef.rar.Unrar5j;
import be.stef.rar4.Rar4Extractor;
import be.stef.rar5.Rar5Extractor;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 * <b>Role.</b> MC-free lazy byte access into a {@link ReskinEntry}.
 * <p>
 * ZIP: entries are read straight out of the archive via {@link ZipFile} — no
 * temp files at all.
 * <p>
 * RAR: unrar5j only exposes whole-archive extraction, so the first read of any
 * file in a {@code .rar} extracts the entire archive once into a cache dir
 * under {@code java.io.tmpdir} (keyed by canonical path hash). Subsequent
 * reads are plain file reads. The user's own folders are NEVER touched.
 * <p>
 * Extraction is thread-safe and writes a {@code .extracted} marker so a second
 * run reuses the cache instead of re-extracting.
 */
public class ReskinReader {

   private static final Map<String, File> RAR_CACHE = new ConcurrentHashMap<>();
   private static final String MARKER = ".extracted";

   /** Read one entry's full bytes. Returns null if the entry doesn't exist. */
   public static byte[] read(ReskinEntry e, String relName) throws IOException {
      String actual = e.resolveName(relName);
      if (actual == null) return null;
      if (e.kind == ReskinEntry.Kind.ZIP) {
         try (ZipFile zf = new ZipFile(e.source)) {
            ZipEntry ze = findExact(zf, actual);
            if (ze == null) return null;
            try (InputStream is = zf.getInputStream(ze)) {
               return readAll(is);
            }
         }
      }
      File base = extractRar(e.source);
      File f = safeChild(base, actual);
      return f != null && f.isFile() ? Files.readAllBytes(f.toPath()) : null;
   }

   /** The cache dir for a RAR archive (extracting on first use). */
   public static File extractRar(File src) throws IOException {
      String key = canonical(src);
      File cached = RAR_CACHE.get(key);
      if (cached != null && cached.isDirectory()) return cached;

      synchronized (RAR_CACHE) {
         cached = RAR_CACHE.get(key);
         if (cached != null && cached.isDirectory()) return cached;

         File dir = new File(System.getProperty("java.io.tmpdir"),
            "sexmod_reskin_" + sha1(key));
         if (!new File(dir, MARKER).isFile()) {
            deleteTree(dir); // stale partial
            dir.mkdirs();
            // silence unrar5j's console progress bar
            Rar5Extractor.showProgress = false;
            Rar4Extractor.showProgress = false;
            try {
               Unrar5j.extract(src.getAbsolutePath(), dir.getAbsolutePath(), "");
            } catch (Throwable t) {
               // extraction failed (e.g. encrypted) — still mark so we don't retry forever
            }
            File marker = new File(dir, MARKER);
            try { Files.write(marker.toPath(), new byte[0]); } catch (IOException ignored) { }
         }
         RAR_CACHE.put(key, dir);
         return dir;
      }
   }

   /** Directory listing of an extracted RAR (null-safe). */
   public static java.util.List<File> extractedFiles(File src) throws IOException {
      File dir = extractRar(src);
      java.util.List<File> out = new java.util.ArrayList<>();
      collect(dir, out);
      return out;
   }

   private static void collect(File dir, java.util.List<File> out) {
      File[] kids = dir.listFiles();
      if (kids == null) return;
      for (File kid : kids) {
         if (kid.isDirectory()) collect(kid, out);
         else out.add(kid);
      }
   }

   private static File safeChild(File base, String rel) {
      File f = new File(base, rel);
      File normBase;
      File normF;
      try {
         normBase = base.getCanonicalFile();
         normF = f.getCanonicalFile();
      } catch (IOException e) {
         return null;
      }
      return normF.getPath().startsWith(normBase.getPath() + File.separator) ? normF : null;
   }

   /** Decode an image, tolerating decoy/junk files:
    * <ul>
    *   <li>salvages truncated PNGs (some decoys append garbage after the
    *       {@code IEND} chunk, which makes strict ImageIO fail);</li>
    *   <li>treats any file that decodes to a tiny (<=4px) image but is >4KB
    *       as a decoy and returns null.</li>
    * </ul>
    * Returns null for undecodable/decoy images.
    */
   public static BufferedImage decodeImage(byte[] bytes) {
      if (bytes == null || bytes.length == 0) return null;
      BufferedImage img = tryDecode(bytes);
      if (img == null) {
         // PNG decoys: tiny valid PNG followed by garbage; truncate at IEND
         int iend = indexOfAscii(bytes, "IEND");
         if (iend >= 8) {
            byte[] truncated = Arrays.copyOf(bytes, Math.min(bytes.length, iend + 8));
            img = tryDecode(truncated);
         }
      }
      if (img == null) return null;
      if (bytes.length > 4096 && img.getWidth() <= 4 && img.getHeight() <= 4) {
         return null; // 1x1 placeholder decoy
      }
      return img;
   }

   private static BufferedImage tryDecode(byte[] b) {
      try {
         return ImageIO.read(new ByteArrayInputStream(b));
      } catch (Throwable t) {
         return null;
      }
   }

   private static int indexOfAscii(byte[] haystack, String needle) {
      byte[] nb = needle.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
      outer:
      for (int i = 0; i <= haystack.length - nb.length; i++) {
         for (int j = 0; j < nb.length; j++) {
            if (haystack[i + j] != nb[j]) continue outer;
         }
         return i;
      }
      return -1;
   }

   private static ZipEntry findExact(ZipFile zf, String name) {
      java.util.Enumeration<? extends ZipEntry> en = zf.entries();
      while (en.hasMoreElements()) {
         ZipEntry ze = en.nextElement();
         if (ze.getName().equals(name)) return ze;
      }
      return null;
   }

   private static byte[] readAll(InputStream is) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
      byte[] buf = new byte[65536];
      int n;
      while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
      return bos.toByteArray();
   }

   private static String canonical(File f) {
      try { return f.getCanonicalPath(); } catch (IOException e) { return f.getAbsolutePath(); }
   }

   private static String sha1(String s) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         byte[] d = md.digest(s.getBytes("UTF-8"));
         StringBuilder sb = new StringBuilder();
         for (byte b : d) sb.append(String.format("%02x", b & 0xff));
         return sb.toString();
      } catch (Exception e) {
         return Integer.toHexString(s.hashCode());
      }
   }

   private static void deleteTree(File f) {
      if (f == null || !f.exists()) return;
      File[] kids = f.listFiles();
      if (kids != null) for (File k : kids) deleteTree(k);
      f.delete();
   }
}