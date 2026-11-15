import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;

/**
 * HEADLESS SIMULATION of ServerWhitelistManager.loadCustomModels AFTER the
 * skip-invalid fix. Mirrors the exact file checks of getPartName() +
 * registerModel() (dir: name.cfg/.png/.geo.json; zip: jenny geo/png), but
 * WITHOUT Minecraft. Proves:
 *   - BEFORE fix: Custom-Models container folder aborts the whole load (-1)
 *   - AFTER  fix: containers are skipped, valid models register, return 0
 *
 * Usage: java -cp . LegacyLoadSim <custom_models_group_dir>
 */
public class LegacyLoadSim {
   public static void main(String[] a) throws Exception {
      String root = a.length > 0 ? a[0]
         : System.getProperty("user.home") + "/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/"
            + "More FPS [FORGE] - Increase FPS & Shaders (1.21 UPDATE)/minecraft/sexmod/custom_models/singleplayer";
      File dir = new File(root);

      // collect IMMEDIATE children exactly like loadCustomModels does
      List<String> modelNames = new ArrayList<>();
      File[] kids = dir.listFiles();
      if (kids != null) for (File k : kids) {
         if (k.isDirectory()) modelNames.add(k.getName());
      }
      // top-level zips (legacy virtual models, same sanitize rule)
      java.util.Map<String, File> zipMap = new java.util.HashMap<>();
      List<String> zipModels = new ArrayList<>();
      if (kids != null) for (File k : kids) {
         String n = k.getName().toLowerCase(Locale.ROOT);
         if (k.isFile() && n.endsWith(".zip")) {
            String base = k.getName().substring(0, k.getName().length() - 4);
            String safe = base.replaceAll("[\\s#\\$]+", "_").replaceAll("[^a-zA-Z0-9_\\-]", "");
            if (safe.isEmpty()) safe = "model";
            if (new File(dir, safe).exists()) continue;
            zipModels.add(safe);
            zipMap.put(safe, k);
         }
      }
      modelNames.addAll(zipModels);

      System.out.println("=== legacy loader simulation on " + root + " ===");
      System.out.println("immediate models found (dirs + top-level zips): " + modelNames.size());

      // ---------- BEFORE fix ----------
      System.out.println("\n--- BEFORE fix (abort on first invalid) ---");
      int before = 0;
      for (String m : modelNames) {
         String err = partError(m, dir, zipMap);
         if (!err.isEmpty()) {
            System.out.println("  ABORT at '" + m + "' -> " + err);
            System.out.println("  => loadCustomModels returns -1, NOTHING registered (" + before + " models)");
            break;
         }
         before++;
      }
      if (before == modelNames.size())
         System.out.println("  all valid, would register " + before);

      // ---------- AFTER fix ----------
      System.out.println("\n--- AFTER fix (skip invalid, register valid) ---");
      int registered = 0;
      int skipped = 0;
      for (String m : modelNames) {
         String err = partError(m, dir, zipMap);
         if (!err.isEmpty()) {
            File probe = new File(dir, m);
            boolean container = probe.isDirectory()
               && !new File(probe, m + ".cfg").isFile()
               && !new File(probe, m + ".geo.json").isFile();
            System.out.println("  SKIP '" + m + "' " + (container ? "(container folder, no model files)" : "-> " + err));
            skipped++;
            continue;
         }
         registered++;
         System.out.println("  REGISTER '" + m + "'");
      }
      System.out.println("  => registered=" + registered + " skipped=" + skipped + " return=0 (UI opens)");

      // ---------- girl filter (reskin browser) ----------
      System.out.println("\n--- reskin browser girl filter (from any-depth scan) ---");
      int jenny = 0, ellie = 0, all = 0, multi = 0;
      for (File k : allArchives(dir)) {
         all++;
         ReskinEntryProbe e = probeZip(k);
         if (e == null) continue;
         if (e.girls.contains("jenny")) jenny++;
         if (e.girls.contains("ellie")) ellie++;
         if (e.girls.size() > 1) multi++;
      }
      System.out.println("  total archives (any depth): " + all);
      System.out.println("  packs tagged jenny: " + jenny + "   ellie: " + ellie + "   multi-girl: " + multi);
      System.out.println("  => set Filter to JENNY in the browser and you see " + jenny + " packs, not 2");
   }

   /** Mirror of ServerWhitelistManager.getPartName (dir + legacy zip checks). */
   static String partError(String m, File dir, java.util.Map<String, File> zipMap) {
      File zip = zipMap.get(m);
      if (zip != null && zip.isFile()) {
         try (ZipFile zf = new ZipFile(zip)) {
            boolean hasGeo = false, hasPng = false;
            java.util.Enumeration<? extends java.util.zip.ZipEntry> en = zf.entries();
            while (en.hasMoreElements()) {
               String n = en.nextElement().getName().toLowerCase(Locale.ROOT);
               if (n.endsWith(m.toLowerCase() + ".geo.json") || n.endsWith("jennydressed.geo.json") || n.endsWith("jennynude.geo.json")) hasGeo = true;
               if (n.endsWith(m.toLowerCase() + ".png") || n.endsWith("jenny.png")) hasPng = true;
            }
            if (!hasGeo) return "no geo";
            if (!hasPng) return "no png";
            return "";
         } catch (Exception e) { return "bad zip"; }
      }
      File p = new File(dir, m);
      if (!new File(p, m + ".geo.json").isFile()) return "no geo file";
      if (!new File(p, m + ".png").isFile()) return "no png file";
      if (!new File(p, m + ".cfg").isFile()) return "no cfg file";
      return "";
   }

   static List<File> allArchives(File dir) {
      List<File> out = new ArrayList<>();
      File[] kids = dir.listFiles();
      if (kids == null) return out;
      for (File k : kids) {
         if (k.isDirectory()) out.addAll(allArchives(k));
         else { String n = k.getName().toLowerCase(Locale.ROOT);
                if (n.endsWith(".zip") || n.endsWith(".rar")) out.add(k); }
      }
      return out;
   }

   /** Minimal girl-tag reader from archive entry names (zip only, header). */
   static ReskinEntryProbe probeZip(File f) {
      ReskinEntryProbe e = new ReskinEntryProbe();
      if (f.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
         try (ZipFile zf = new ZipFile(f)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> en = zf.entries();
            String[] girls = {"jenny","ellie","allie","bee","bia","galath","goblin","kobold","luna","slime","cat","manglelie"};
            while (en.hasMoreElements()) {
               String n = en.nextElement().getName().toLowerCase(Locale.ROOT);
               for (String g : girls) if (n.contains("/" + g + "/")) e.girls.add(g);
            }
         } catch (Exception ex) { return null; }
      } else {
         e.girls.add("?");
      }
      return e;
   }

   static class ReskinEntryProbe {
      java.util.Set<String> girls = new java.util.HashSet<>();
   }
}