import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * HEADLESS RECURSIVE-DISCOVERY SIMULATION.
 * Proves what the loader SHOULD find when scanning the custom-models tree
 * recursively (any depth) instead of only looking at immediate children.
 *
 * Usage: java -cp . RecursiveScanSim <root_dir>
 */
public class RecursiveScanSim {
   static final String[] GIRLS = {"jenny","ellie","allie","bee","bia","galath","goblin","kobold","luna","slime","cat","manglelie"};

   public static void main(String[] a) throws Exception {
      String root = a.length > 0 ? a[0]
         : "/home/user/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/"
            + "More FPS [FORGE] - Increase FPS & Shaders (1.21 UPDATE)/minecraft/sexmod/custom_models/singleplayer";
      long t0 = System.nanoTime();
      List<File> archives = scan(new File(root));
      long scanMs = (System.nanoTime() - t0) / 1_000_000L;

      System.out.println("=== RECURSIVE SCAN of " + root + " ===");
      System.out.println("total archives found (any depth): " + archives.size()
         + "  in " + scanMs + " ms");

      int zip = 0, rar = 0, withGeo = 0, withTex = 0, withAnim = 0, reskin = 0, noPreview = 0;
      for (File f : archives) {
         String n = f.getName().toLowerCase(Locale.ROOT);
         if (n.endsWith(".zip")) zip++; else rar++;
      }

      // per-girl counts + reskin flag + preview presence
      Map<String, Integer> girlCounts = new LinkedHashMap<>();
      List<String> reskins = new ArrayList<>();
      List<String> noPrev = new ArrayList<>();
      for (File f : archives) {
         Pack p = index(f);
         if (p == null) continue;
         if (p.hasGeo && (p.hasTex || p.hasAnim)) { reskin++; reskins.add(f.getPath()); }
         if (p.preview == null) { noPreview++; noPrev.add(f.getName()); }
         for (String g : p.girls) girlCounts.merge(g, 1, Integer::sum);
         if (p.hasGeo) withGeo++;
         if (p.hasTex) withTex++;
         if (p.hasAnim) withAnim++;
      }

      System.out.println("zips: " + zip + "   rars: " + rar);
      System.out.println("reskin packs (geo+tex/anim): " + reskin);
      System.out.println("with geo: " + withGeo + "  with texture: " + withTex + "  with animation: " + withAnim);
      System.out.println("packs WITHOUT any preview png: " + noPreview);
      System.out.println("\nper-girl pack counts (folder token in archive path):");
      for (Map.Entry<String, Integer> e : girlCounts.entrySet()) {
         System.out.printf("   %-10s %d%n", e.getKey(), e.getValue());
      }
      System.out.println("\nfirst 15 reskin packs:");
      int c = 0;
      for (String r : reskins) {
         if (c++ >= 15) break;
         System.out.println("   " + r.replace(root, "."));
      }
      if (!noPrev.isEmpty()) {
         System.out.println("\npacks with no preview png (aria/anim-only): " + noPrev.size());
      }
   }

   static List<File> scan(File dir) {
      List<File> out = new ArrayList<>();
      File[] kids = dir.listFiles();
      if (kids == null) return out;
      for (File k : kids) {
         if (k.isDirectory()) out.addAll(scan(k));
         else { String n = k.getName().toLowerCase(Locale.ROOT);
                if (n.endsWith(".zip") || n.endsWith(".rar")) out.add(k); }
      }
      return out;
   }

   static Pack index(File f) {
      Pack p = new Pack();
      String low = f.getName().toLowerCase(Locale.ROOT);
      if (low.endsWith(".zip")) {
         try (ZipFile zf = new ZipFile(f)) {
            java.util.Enumeration<? extends ZipEntry> en = zf.entries();
            while (en.hasMoreElements()) {
               String n = en.nextElement().getName().toLowerCase(Locale.ROOT);
               if (n.endsWith(".geo.json") || (n.endsWith(".json") && n.contains("/geo/"))) p.hasGeo = true;
               if (n.contains("/textures/") && (n.endsWith(".png") || n.endsWith(".jpg"))) p.hasTex = true;
               if (n.contains("/animations/") && n.endsWith(".json")) p.hasAnim = true;
               if (n.endsWith(".png") && p.preview == null) p.preview = n;
               for (String g : GIRLS) if (n.contains("/" + g + "/")) p.girls.add(g);
            }
         } catch (Exception e) { return null; }
      } else {
         p.hasAnim = true; // assume rar has anim (header parse skipped here)
      }
      return p;
   }

   static class Pack {
      boolean hasGeo, hasTex, hasAnim;
      String preview;
      java.util.Set<String> girls = new java.util.HashSet<>();
   }
}