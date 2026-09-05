package com.trolmastercard.sexmod.reskin;

import be.stef.rar.Unrar5j;
import be.stef.rar4.Rar4HeaderParser;
import be.stef.rar4.blocks.Rar4FileBlock;
import be.stef.rar5.Rar5Reader;
import be.stef.rar5.blocks.Rar5FileBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <b>Role.</b> MC-free recursive scanner for custom-model packs.
 * <p>
 * Walks the custom-models tree at ANY nesting depth and indexes every
 * {@code .zip} (via the central directory — no decompression) and {@code .rar}
 * (via the unrar5j header parser — no decompression). For each archive it
 * records geo/texture/animation/cfg presence, the target girl(s), and the best
 * preview PNG — everything needed for a responsive GUI with zero lag at
 * startup.
 * <p>
 * <b>Read-only.</b> This never moves, renames or extracts anything into the
 * user's folder; the user's directory layout is left exactly as-is.
 */
public class ReskinScanner {

   /** Known girl names (the canonical {@code assets/sexmod/geo/<girl>/...} folder names). */
   public static final List<String> GIRLS = Arrays.asList(
      "jenny", "ellie", "allie", "bee", "bia", "galath", "goblin", "kobold", "luna", "slime", "cat", "manglelie"
   );

   public List<ReskinEntry> scan(File root) {
      List<ReskinEntry> out = new ArrayList<>();
      if (root != null && root.isDirectory()) {
         walk(root, out);
      }
      return out;
   }

   private void walk(File dir, List<ReskinEntry> out) {
      File[] kids = dir.listFiles();
      if (kids == null) return;
      for (File kid : kids) {
         if (kid.isDirectory()) {
            walk(kid, out);
         } else {
            String lower = kid.getName().toLowerCase(Locale.ROOT);
            if (lower.endsWith(".zip") || lower.endsWith(".rar")) {
               ReskinEntry e = index(kid);
               if (e != null) out.add(e);
            }
         }
      }
   }

   /** Index one archive file into a {@link ReskinEntry} (metadata only). */
   public static ReskinEntry index(File file) {
      String lower = file.getName().toLowerCase(Locale.ROOT);
      if (lower.endsWith(".zip")) return indexZip(file);
      if (lower.endsWith(".rar")) return indexRar(file);
      return null;
   }

   private static ReskinEntry indexZip(File file) {
      ReskinEntry e = new ReskinEntry(file, ReskinEntry.Kind.ZIP);
      try (ZipFile zf = new ZipFile(file)) {
         java.util.Enumeration<? extends ZipEntry> en = zf.entries();
         while (en.hasMoreElements()) {
            ZipEntry ze = en.nextElement();
            if (ze.isDirectory()) continue;
            String name = ze.getName();
            long size = ze.getSize();
            e.files.add(new ReskinEntry.FileInfo(name, size));
            e.totalSize += Math.max(0L, size);
            classify(e, name);
         }
      } catch (IOException ex) {
         e.error = ex.getMessage();
      } catch (Throwable t) {
         e.error = t.getClass().getSimpleName() + ": " + t.getMessage();
      }
      finish(e);
      return e;
   }

   private static ReskinEntry indexRar(File file) {
      ReskinEntry e = new ReskinEntry(file, ReskinEntry.Kind.RAR);
      try {
         int fmt = Unrar5j.detectFormat(file.getAbsolutePath());
         if (fmt == Unrar5j.FORMAT_RAR5) {
            Rar5Reader reader = new Rar5Reader();
            if (!reader.read(file)) {
               e.error = "RAR5 header parse failed (corrupted / encrypted headers?)";
               return finish(e);
            }
            List<Rar5FileBlock> blocks = reader.getFileBlocks();
            if (blocks == null || blocks.isEmpty()) {
               e.error = "no file blocks (encrypted headers?)";
               return finish(e);
            }
            for (Rar5FileBlock b : blocks) {
               if (b == null || b.isDirectory()) continue;
               String name = b.getFileName();
               if (name == null || name.isEmpty()) continue;
               long size = safeSize(b.getUnpackedSize());
               e.files.add(new ReskinEntry.FileInfo(name, size));
               e.totalSize += Math.max(0L, size);
               classify(e, name);
            }
         } else if (fmt == Unrar5j.FORMAT_RAR4) {
            Rar4HeaderParser parser = new Rar4HeaderParser();
            if (!parser.parse(file)) {
               e.error = "RAR4 header parse failed (corrupted / encrypted headers?)";
               return finish(e);
            }
            List<Rar4FileBlock> blocks = parser.getFileBlocks();
            if (blocks == null || blocks.isEmpty()) {
               e.error = "no file blocks (encrypted headers?)";
               return finish(e);
            }
            for (Rar4FileBlock b : blocks) {
               if (b == null || b.isDirectory()) continue;
               String name = b.getFileName();
               if (name == null || name.isEmpty()) continue;
               long size = safeSize(b.getUnpackedSize());
               e.files.add(new ReskinEntry.FileInfo(name, size));
               e.totalSize += Math.max(0L, size);
               classify(e, name);
            }
         } else {
            e.error = "unknown RAR format";
         }
      } catch (Throwable t) {
         e.error = t.getClass().getSimpleName() + ": " + t.getMessage();
      }
      return finish(e);
   }

   private static long safeSize(long v) {
      return v < 0 ? 0L : v;
   }

   /** Classify one archive entry name into geo/texture/animation/cfg + girl + preview. */
   static void classify(ReskinEntry e, String name) {
      String n = name.replace('\\', '/');
      String low = n.toLowerCase(Locale.ROOT);
      String girl = detectGirl(low);

      if (low.endsWith(".cfg")) {
         e.hasCfg = true;
      }
      if (low.endsWith(".png") || low.endsWith(".jpg") || low.endsWith(".jpeg")) {
         if (low.endsWith("pack.png") || low.endsWith("pack.jpg")) {
            // pack.png wins over anything else as the preview thumbnail
            e.previewName = name;
         } else if (e.previewName == null) {
            e.previewName = name; // first PNG seen = fallback preview
         }
      }
      if ((low.endsWith(".geo.json") || (low.endsWith(".json") && low.contains("/geo/")))) {
         e.hasGeo = true;
         if (girl != null) e.girls.add(girl);
      }
      if (low.contains("/textures/") && (low.endsWith(".png") || low.endsWith(".jpg"))) {
         e.hasTexture = true;
         if (girl != null) e.girls.add(girl);
      }
      if (low.contains("/animations/") && low.endsWith(".json")) {
         e.hasAnimation = true;
         if (girl != null) e.girls.add(girl);
      }
      // old flat layout: assets/<girl>.geo.json (no assets/sexmod/...)
      if (low.matches("assets/[a-z0-9]+[^/]*\\.geo\\.json") && !low.contains("/sexmod/")) {
         e.hasLegacyFlat = true;
         e.hasGeo = true;
         int slash = low.indexOf('/');
         int dot = low.indexOf('.', slash);
         if (slash >= 0 && dot > slash) e.girls.add(low.substring(slash + 1, dot));
      }
   }

   /** Find a known girl token in the path (folder segment equals a girl name). */
   static String detectGirl(String lowPath) {
      String[] segs = lowPath.split("/");
      for (String seg : segs) {
         String s = seg.toLowerCase(Locale.ROOT);
         if (GIRLS.contains(s)) return s;
      }
      return null;
   }

   private static ReskinEntry finish(ReskinEntry e) {
      if (e.previewName == null && !e.files.isEmpty()) {
         // last resort: any png in the archive
         for (ReskinEntry.FileInfo fi : e.files) {
            String low = fi.name.toLowerCase(Locale.ROOT);
            if (low.endsWith(".png")) { e.previewName = fi.name; break; }
         }
      }
      return e;
   }
}