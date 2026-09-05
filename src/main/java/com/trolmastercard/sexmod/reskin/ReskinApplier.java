package com.trolmastercard.sexmod.reskin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <b>Role.</b> MC-free plan builder: decides, per archive entry, where it
 * belongs in the game's resource namespace.
 * <p>
 * A resourcepack-layout pack (<code>assets/sexmod/geo/... .
 * geo.json</code>, <code>assets/sexmod/textures/entity/... .
 * png</code>, <code>assets/sexmod/animations/... .
 * json</code>) is applied by overriding the SAME {@code sexmod:} resource
 * locations the girl models already use — that's how a reskin replaces the
 * girl's appearance without touching any file on disk.
 * <p>
 * Anything else (lang, sounds, structures, gui textures, pack.png, legacy
 * {@code .cfg} wear-model files) is NOT part of a reskin and is skipped.
 */
public class ReskinApplier {

   public enum Kind { GEO, TEXTURE, ANIMATION }

   /** One apply operation: read {@link #relName} from the archive, override RL {@link #rlPath}. */
   public static class Op {
      public final String relName;   // entry name inside the archive
      public final String rlPath;    // path after "assets/sexmod/" (the sexmod: RL path)
      public final String rlNs;      // resource namespace (always "sexmod" for these packs)
      public final Kind kind;
      public final String girl;

      public Op(String relName, String rlNs, String rlPath, Kind kind, String girl) {
         this.relName = relName;
         this.rlNs = rlNs;
         this.rlPath = rlPath;
         this.kind = kind;
         this.girl = girl;
      }

      @Override
      public String toString() {
         return kind + " " + rlNs + ":" + rlPath + " <- " + relName;
      }
   }

   /** The full apply plan for an entry (empty for non-reskin packs). */
   public static List<Op> plan(ReskinEntry e) {
      List<Op> ops = new ArrayList<>();
      for (ReskinEntry.FileInfo fi : e.files) {
         String n = fi.name.replace('\\', '/');
         String low = n.toLowerCase(Locale.ROOT);

         // resourcepack-layout paths: assets/sexmod/ may sit at ANY depth
         // (top-level "Elliot Pajama.zip" OR "SUKUNA/assets/sexmod/..." after
         // an extra folder). Find the anchor, then slice everything after it.
         String prefix = "assets/sexmod/";
         int anchor = low.indexOf(prefix);
         if (anchor < 0) continue;

         String rest = n.substring(anchor + prefix.length());
         String lowerRest = rest.toLowerCase(Locale.ROOT);
         String girl = ReskinScanner.detectGirl(low);

         // first path segment decides the type (geo/textures/animations)
         String firstSeg = lowerRest;
         int slash = lowerRest.indexOf('/');
         if (slash >= 0) firstSeg = lowerRest.substring(0, slash);

         if (firstSeg.equals("geo") && (lowerRest.endsWith(".geo.json") || lowerRest.endsWith(".json"))) {
            ops.add(new Op(fi.name, "sexmod", rest, Kind.GEO, girl));
         } else if (firstSeg.equals("textures") && (lowerRest.endsWith(".png") || lowerRest.endsWith(".jpg"))) {
            ops.add(new Op(fi.name, "sexmod", rest, Kind.TEXTURE, girl));
         } else if (firstSeg.equals("animations") && lowerRest.endsWith(".json")) {
            ops.add(new Op(fi.name, "sexmod", rest, Kind.ANIMATION, girl));
         }
         // lang/sounds/structures/pack.png/gui etc: skipped for reskins
      }
      return ops;
   }

   /**
    * Decoy/placeholder detection: some packs ship fake 2048-byte JSON files
    * (identifier {@code geometry.fake_NN} and a base64 {@code _pad} tail) to
    * pollute the folder with junk — they must be skipped, not applied or
    * reported as errors. Real model files never contain these markers.
    */
   public static boolean isDecoy(String json) {
      if (json == null) return false;
      String low = json.toLowerCase(Locale.ROOT);
      return low.contains("geometry.fake")
         || low.contains("\"fake")
         || low.contains("_pad");
   }

   /**
    * Sanitizes a geo JSON before the geckolib parse:
    * <ul>
    *   <li>returns null for decoys and for non-geo JSON (e.g. animation files
    *       that were misplaced into the geo/ folder — they lack the
    *       {@code "minecraft:geometry"} root key);</li>
    *   <li>normalizes {@code format_version} to "1.12.0" so old packs (1.8.0
    *       etc.) keep parsing with geckolib 3.</li>
    * </ul>
    */
   public static String prepareGeoJson(String json) {
      if (json == null || isDecoy(json)) return null;
      String low = json.toLowerCase(Locale.ROOT);
      if (!low.contains("\"minecraft:geometry\"")) return null;
      return json.replaceAll("(?i)\"format_version\"\\s*:\\s*\"[^\"]*\"", "\"format_version\": \"1.12.0\"");
   }

   /** True if the JSON is a usable animation file (has an "animations" root, not a decoy). */
   public static boolean isAnimationJson(String json) {
      if (json == null || isDecoy(json)) return false;
      return json.toLowerCase(Locale.ROOT).contains("\"animations\"");
   }
}