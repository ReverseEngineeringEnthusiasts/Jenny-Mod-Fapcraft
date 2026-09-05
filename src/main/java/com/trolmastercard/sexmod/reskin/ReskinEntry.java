package com.trolmastercard.sexmod.reskin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>Role.</b> MC-free metadata for one custom-model pack archive
 * ({@code .zip} or {@code .rar}) found anywhere under the custom-models tree.
 * Produced by {@link ReskinScanner} from the archive header/central directory
 * ONLY — no entry data is decompressed here, so indexing hundreds of packs is
 * fast and non-laggy.
 * <p>
 * <b>Lazy contract.</b> Everything in this class is metadata. Byte content is
 * only read via {@link ReskinReader} when the user explicitly asks for a
 * preview or an Apply.
 * <p>
 * This class has NO Minecraft imports by design (headless-testable); the MC
 * layer lives in {@link ReskinManager}/{@link ReskinBrowserScreen}.
 */
public class ReskinEntry {
   public enum Kind { ZIP, RAR }

   /** One file inside the archive: path + uncompressed size from the header. */
   public static class FileInfo {
      public final String name;
      public final long size;
      public FileInfo(String name, long size) {
         this.name = name;
         this.size = size;
      }
   }

   public final File source;
   public final Kind kind;
   public final String displayName;
   public final List<FileInfo> files = new ArrayList<>();
   public final Set<String> girls = new LinkedHashSet<>();

   public boolean hasGeo;
   public boolean hasTexture;
   public boolean hasAnimation;
   public boolean hasCfg;
   public boolean hasLegacyFlat;
   public String previewName;
   public String error;
   public long totalSize;

   public ReskinEntry(File source, Kind kind) {
      this.source = source;
      this.kind = kind;
      this.displayName = source.getName();
   }

   /** Whether this archive contains anything we can apply (reskin or legacy wear). */
   public boolean isUsable() {
      return hasGeo || hasTexture || hasAnimation || hasCfg || hasLegacyFlat;
   }

   /** Whether this is a resourcepack-layout reskin (assets/sexmod/geo|textures|animations...). */
   public boolean isReskin() {
      return hasGeo && (hasTexture || hasAnimation);
   }

   public String girlSummary() {
      return girls.isEmpty() ? "?" : String.join("/", girls);
   }

   public String flagSummary() {
      StringBuilder sb = new StringBuilder();
      if (hasGeo) sb.append("geo ");
      if (hasTexture) sb.append("tex ");
      if (hasAnimation) sb.append("anim ");
      if (hasCfg) sb.append("cfg ");
      if (hasLegacyFlat) sb.append("legacy ");
      return sb.toString().trim();
   }

   /** Case-insensitive lookup of an entry by exact name; falls back to suffix match. */
   public String resolveName(String wanted) {
      String low = wanted.toLowerCase();
      for (FileInfo fi : files) {
         String n = fi.name.toLowerCase();
         if (n.equals(low)) return fi.name;
      }
      for (FileInfo fi : files) {
         String n = fi.name.toLowerCase();
         if (n.endsWith("/" + low) || n.endsWith(low)) return fi.name;
      }
      return null;
   }

   @Override
   public String toString() {
      return displayName + " [" + flagSummary() + "] girls=" + girlSummary() + " files=" + files.size()
         + " size=" + totalSize + (error == null ? "" : " ERROR=" + error);
   }
}