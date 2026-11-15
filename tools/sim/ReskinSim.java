import software.bernie.shadowed.eliotlash.molang.MolangParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trolmastercard.sexmod.reskin.ReskinApplier;
import com.trolmastercard.sexmod.reskin.ReskinEntry;
import com.trolmastercard.sexmod.reskin.ReskinReader;
import com.trolmastercard.sexmod.reskin.ReskinScanner;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.util.json.JsonAnimationUtils;

/**
 * HEADLESS SIMULATION of the reskin pipeline (no Minecraft needed).
 *
 * Runs the EXACT same code path the mod will use:
 *   1. ReskinScanner  - recursive discovery at any depth (zip central dir /
 *                       rar headers, NO decompression)
 *   2. ReskinReader   - lazy bytes (zip direct / rar temp-extract)
 *   3. ReskinApplier  - archive entry -> sexmod: resource location mapping
 *   4. geckolib parse - Converter -> RawGeometryTree -> GeoBuilder (the real
 *                       code ReskinManager.apply uses)
 *   5. ImageIO texture decode + JsonAnimationUtils animation parse
 *
 * Usage:  java -cp <...> ReskinSim <custom_models_group_dir> [maxArchives]
 * Exit code 0 = all parsed packs validated, 1 = problems found.
 */
public class ReskinSim {

   static int geoOk = 0, texOk = 0, animOk = 0, prevOk = 0, packsTested = 0;
   static int problems = 0;

   public static void main(String[] args) throws Exception {
      String root = args.length > 0 ? args[0]
         : System.getProperty("user.home") + "/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/"
            + "More FPS [FORGE] - Increase FPS & Shaders (1.21 UPDATE)/minecraft/sexmod/custom_models/singleplayer";
      int maxArchives = args.length > 1 ? Integer.parseInt(args[1]) : Integer.MAX_VALUE;

      long t0 = System.nanoTime();
      List<ReskinEntry> entries = new ReskinScanner().scan(new File(root));
      long scanMs = (System.nanoTime() - t0) / 1_000_000L;

      System.out.println("============================================================");
      System.out.println(" RESKIN PIPELINE SIMULATION (headless, no Minecraft)");
      System.out.println(" root: " + root);
      System.out.println("============================================================");
      int zips = 0, rars = 0, usable = 0, reskins = 0;
      for (ReskinEntry e : entries) {
         if (e.kind == ReskinEntry.Kind.ZIP) zips++; else rars++;
         if (e.isUsable()) usable++;
         if (e.isReskin()) reskins++;
      }
      System.out.println("archives found:      " + entries.size() + "  (zip=" + zips + " rar=" + rars + ")");
      System.out.println("usable (has content): " + usable + "  resourcepack reskins: " + reskins);
      System.out.println("scan time:            " + scanMs + " ms  (metadata only, no decompression)");
      System.out.println();

      int limit = Math.min(maxArchives, entries.size());
      int shown = 0;
      for (int i = 0; i < entries.size() && i < limit; i++) {
         ReskinEntry e = entries.get(i);
         System.out.println("[" + (i + 1) + "/" + entries.size() + "] " + e.displayName
            + "  kind=" + e.kind + "  girls=" + e.girlSummary()
            + "  flags=" + e.flagSummary() + "  size=" + (e.totalSize / 1024) + "KB"
            + "  preview=" + (e.previewName == null ? "-" : e.previewName)
            + (e.error == null ? "" : "  ERROR: " + e.error));
         if (!e.isUsable()) {
            System.out.println("      -> skipped (nothing applicable)");
            continue;
         }
         if (e.error != null) { problems++; }

         List<ReskinApplier.Op> ops = ReskinApplier.plan(e);
         if (ops.isEmpty()) {
            System.out.println("      -> no reskin ops (legacy cfg/wear pack? out of scope)");
            continue;
         }
         packsTested++;
         for (ReskinApplier.Op op : ops) {
            try {
               byte[] bytes = ReskinReader.read(e, op.relName);
               if (bytes == null) {
                  System.out.println("      !! MISSING entry: " + op.relName);
                  problems++;
                  continue;
               }
               switch (op.kind) {
                  case GEO: {
                     String json = ReskinApplier.prepareGeoJson(new String(bytes, StandardCharsets.UTF_8));
                     if (json == null) {
                        System.out.println("      SKIP geo (decoy/misplaced) " + op.rlNs + ":" + op.rlPath);
                        break;
                     }
                     RawGeoModel raw = Converter.fromJsonString(json);
                     net.minecraft.util.ResourceLocation rl = new ResourceLocation(op.rlNs, op.rlPath);
                     RawGeometryTree tree = RawGeometryTree.parseHierarchy(raw, rl);
                     GeoModel model = GeoBuilder.getGeoBuilder(rl.getNamespace()).constructGeoModel(tree);
                     geoOk++;
                     if (shown < 40) {
                        System.out.println("      GEO  OK  " + op.rlNs + ":" + op.rlPath
                           + "  topBones=" + (model.topLevelBones == null ? 0 : model.topLevelBones.size())
                           + "  (" + (bytes.length / 1024) + "KB)");
                     }
                     shown++;
                     break;
                  }
                  case TEXTURE: {
                     BufferedImage img = ReskinReader.decodeImage(bytes);
                     if (img == null) {
                        System.out.println("      SKIP texture (undecodable/decoy) " + op.rlNs + ":" + op.rlPath);
                        break;
                     }
                     texOk++;
                     if (shown < 40) {
                        System.out.println("      TEX  OK  " + op.rlNs + ":" + op.rlPath
                           + "  " + img.getWidth() + "x" + img.getHeight() + "  (" + (bytes.length / 1024) + "KB)");
                     }
                     shown++;
                     break;
                  }
                  case ANIMATION: {
                     String json = new String(bytes, StandardCharsets.UTF_8);
                     if (!ReskinApplier.isAnimationJson(json)) {
                        System.out.println("      SKIP animation (decoy/not anim) " + op.rlNs + ":" + op.rlPath);
                        break;
                     }
                     JsonObject animRoot = new Gson().fromJson(json, JsonObject.class);
                     if (animRoot == null || !animRoot.has("animations")) {
                        System.out.println("      !! ANIMATION missing 'animations' key: " + op.relName);
                        problems++;
                        break;
                     }
                     AnimationFile af = new AnimationFile();
                     MolangParser parser = new MolangParser();
                     for (Map.Entry<String, JsonElement> en : JsonAnimationUtils.getAnimations(animRoot)) {
                        JsonAnimationUtils.deserializeJsonToAnimation(
                           JsonAnimationUtils.getAnimation(animRoot, en.getKey()), parser);
                     }
                     animOk++;
                     if (shown < 40) {
                        System.out.println("      ANIM OK  " + op.rlNs + ":" + op.rlPath
                           + "  animations=" + af.getAllAnimations().size());
                     }
                     shown++;
                     break;
                  }
               }
            } catch (Throwable t) {
               System.out.println("      !! " + op.kind + " FAILED " + op.rlNs + ":" + op.rlPath
                  + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
               problems++;
            }
         }
         // preview check (with fallback to other PNGs in the pack)
         List<String> cands = new java.util.ArrayList<>();
         if (e.previewName != null) cands.add(e.previewName);
         for (ReskinEntry.FileInfo fi : e.files) {
            String low = fi.name.toLowerCase(Locale.ROOT);
            if ((low.endsWith(".png") || low.endsWith(".jpg")) && !fi.name.equals(e.previewName)) {
               cands.add(fi.name);
               if (cands.size() >= 4) break;
            }
         }
         if (!cands.isEmpty()) {
            boolean ok = false;
            for (String cand : cands) {
               try {
                  byte[] p = ReskinReader.read(e, cand);
                  BufferedImage img = p == null ? null : ReskinReader.decodeImage(p);
                  if (img != null) { ok = true; break; }
               } catch (Throwable t) {
                  // try next
               }
            }
            if (ok) { prevOk++; }
            else { System.out.println("      !! preview failed to decode: " + e.previewName); problems++; }
         }
      }

      long totalMs = (System.nanoTime() - t0) / 1_000_000L;
      System.out.println();
      System.out.println("============================================================");
      System.out.println(" RESULT");
      System.out.println("  packs exercised:        " + packsTested);
      System.out.println("  geo models parsed+build: " + geoOk);
      System.out.println("  textures decoded:       " + texOk);
      System.out.println("  animations parsed:      " + animOk);
      System.out.println("  previews decoded:       " + prevOk);
      System.out.println("  total runtime:          " + totalMs + " ms");
      System.out.println("  problems:               " + problems);
      System.out.println("============================================================");
      if (entries.isEmpty()) {
         System.out.println("NO ARCHIVES FOUND - check the root path.");
         System.exit(2);
      }
      System.exit(problems == 0 ? 0 : 1);
   }
}