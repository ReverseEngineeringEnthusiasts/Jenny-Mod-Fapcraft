package com.trolmastercard.sexmod.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

/**
 * <b>Role.</b> The archive ("zip model") side of custom-model registration for
 * {@link ServerWhitelistManager}: spots {@code .zip} packs as virtual model
 * names, checks that a zip contains a usable geo/texture pair, and registers a
 * model by reading {@code .cfg}/{@code .png}/{@code .geo.json} straight out of
 * the archive (a missing cfg is generated from the geo bones).
 * <p>
 * <b>Pitfalls.</b> Zip entries are matched case-insensitively by suffix — the
 * first hit wins, so a pack can shadow files of the same name. The generated
 * cfg is written to a temp file and deleted straight after parsing, so a
 * {@code ModelData} that holds that path is only valid inside the call. The
 * texture/geo branches are client-only; with {@code textureFlag == false}
 * (dedicated server) only the model name is registered.
 */
final class ModelArchive {
   private ModelArchive() {
   }

   static String getPartName(String modelName, Map<String, File> zipFileMap, String group) {
      if (zipFileMap.containsKey(modelName)) {
         File zip = zipFileMap.get(modelName);
         try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(zip)) {
            boolean hasGeo = false, hasPng = false, hasCfg = false;
            java.util.Enumeration<? extends java.util.zip.ZipEntry> en = zf.entries();
            while (en.hasMoreElements()) {
               String n = en.nextElement().getName().toLowerCase();
               if (n.endsWith(modelName.toLowerCase() + ".geo.json") || n.endsWith("jennydressed.geo.json") || n.endsWith("jennynude.geo.json")) hasGeo = true;
               if (n.endsWith(modelName.toLowerCase() + ".png") || n.endsWith("jenny.png")) hasPng = true;
               if (n.endsWith(modelName.toLowerCase() + ".cfg")) hasCfg = true;
            }
            // cfg auto-generated if missing for full-mod zips
            if (!hasGeo) return String.format("couldn't find model File for '%s' inside zip '%s'.", modelName, zip.getAbsolutePath());
            if (!hasPng) return String.format("couldn't find texture File for '%s' inside zip '%s'.", modelName, zip.getAbsolutePath());
            return "";
         } catch (IOException e) {
            return String.format("couldn't read zip for '%s': %s", modelName, e.getMessage());
         }
      }

      String path = String.format("%s/%s", group, modelName);
      File geoFile = new File(String.format("%s/%s.geo.json", path, modelName));
      File textureFile = new File(String.format("%s/%s.png", path, modelName));
      File cfgFile = new File(String.format("%s/%s.cfg", path, modelName));
      if (!geoFile.exists()) {
         return String.format("couldn't find model File for '%s'. It should have been at '%s'. Are you sure it exists?", modelName, geoFile.getAbsolutePath());
      } else if (!textureFile.exists()) {
         return String.format("couldn't find texture File for '%s'. It should have been at '%s'. Are you sure it exists?", modelName, textureFile.getAbsolutePath());
      } else {
         return !cfgFile.exists()
            ? String.format("couldn't find cfg File for '%s'. It should have been at '%s'. Are you sure it exists?", modelName, cfgFile.getAbsolutePath())
            : "";
      }
   }

   /**
    * Registers a zip-backed model. Returns {@code ""} on success or the
    * user-facing error to surface, exactly like
    * {@link ServerWhitelistManager#registerModel}.
    */
   static String registerFromZip(String modelName, Map<String, File> zipFileMap, Map<String, ServerWhitelistManager.ModelData> modelDataMap, boolean textureFlag) {
      File zipFile = zipFileMap.get(modelName);
      try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(zipFile)) {
         java.util.zip.ZipEntry cfgEntry = findZipEntry(zf, modelName + ".cfg");
         java.util.zip.ZipEntry pngEntry = findZipEntry(zf, modelName + ".png");
         if (pngEntry == null) pngEntry = findZipEntry(zf, "jenny.png");
         java.util.zip.ZipEntry geoEntry = findZipEntry(zf, modelName + ".geo.json");
         if (geoEntry == null) geoEntry = findZipEntry(zf, "jennydressed.geo.json");
         if (geoEntry == null) geoEntry = findZipEntry(zf, "jennynude.geo.json");
         ServerWhitelistManager.ModelData modelData;
         if (cfgEntry != null) {
            try (java.io.InputStream is = zf.getInputStream(cfgEntry)) {
               java.util.Properties props = new java.util.Properties(); props.load(is);
               File tmp = File.createTempFile("model", ".cfg");
               try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp)) { props.store(fos, null); }
               modelData = new ServerWhitelistManager.ModelData(tmp, modelName);
               tmp.delete();
            }
         } else {
            // auto-detect full outfit vs head accessory from geo bones
            String wear = "HEAD";
            String extra = "";
            if (geoEntry != null) {
               try (java.io.InputStream gis = zf.getInputStream(geoEntry); java.io.BufferedReader gbr = new java.io.BufferedReader(new java.io.InputStreamReader(gis))) {
                  StringBuilder gsb = new StringBuilder(); String gl; while ((gl = gbr.readLine()) != null) gsb.append(gl);
                  String gj = gsb.toString().toLowerCase();
                  if (gj.contains("\"body\"") || gj.contains("upperbody") || gj.contains("\"hip\"")) {
                     wear = "CUSTOM_BONE";
                     extra = "custom_bone=body\n";
                  } else if (gj.contains("custom")) {
                     wear = "CUSTOM_BONE";
                     extra = "custom_bone=body\n";
                  }
               } catch (Exception ignored2) {
                  // ignored: unparsable geo json keeps the default HEAD wear
               }
            }
            float scale = (wear.equals("GIRL_SPECIFIC") || wear.equals("CUSTOM_BONE")) ? 0.35F : 1.0F;
            float yOff = (wear.equals("GIRL_SPECIFIC") || wear.equals("CUSTOM_BONE")) ? -5.0F : 0.0F;
            String hide = wear.equals("CUSTOM_BONE") && extra.contains("body") ? "body,upperBody,torso,top,hipbend,head,armL,armR,legL,legR" : "";
            File tmp = File.createTempFile("model", ".cfg");
            try (FileWriter w = new FileWriter(tmp)) { w.write("wear_type=" + wear + "\n" + extra + "which_girls=JENNY\nwhich_lighting=DEFAULT\nauthor=auto\nbones_to_hide=" + hide + "\nenable_when_nude=yes\ngui_size_factor=" + scale + "\ngui_vertical_positioning=" + yOff + "\nversion=1.0\n"); }
            modelData = new ServerWhitelistManager.ModelData(tmp, modelName);
            tmp.delete();
         }
         if (modelData.errorMessage != null) return modelData.errorMessage;
         ResourceLocation textureLoc = null;
         if (textureFlag) {
            if (pngEntry == null) return String.format("The texture for the custom model '%s' couldn't be found inside zip '%s'", modelName, zipFile.getName());
            try (java.io.InputStream is = zf.getInputStream(pngEntry)) {
               BufferedImage img = ImageIO.read(is);
               if (img == null) return String.format("The texture for the custom model '%s' inside zip '%s' appears to be corrupted.", modelName, zipFile.getName());
               textureLoc = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(modelName, new DynamicTexture(img));
            } catch (Exception e) {
               return String.format("The texture for the custom model '%s' inside zip '%s' appears to be corrupted. %s", modelName, zipFile.getName(), e.getMessage());
            }
         }
         if (geoEntry == null) return String.format("The geo model for the custom model '%s' couldn't be found inside zip '%s'", modelName, zipFile.getName());
         if (textureFlag) {
            RawGeoModel raw;
            try (java.io.InputStream is = zf.getInputStream(geoEntry); java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
               StringBuilder sb = new StringBuilder(); String line; while ((line = br.readLine()) != null) sb.append(line);
               raw = Converter.fromJsonString(sb.toString());
            } catch (IOException e) {
               return String.format("The geo model for the custom model '%s' inside zip '%s' appears to be corrupted.", modelName, zipFile.getName());
            }
            try {
               ResourceLocation geoRes = new ResourceLocation("sexmod", modelName + "Model");
               RawGeometryTree tree = RawGeometryTree.parseHierarchy(raw, geoRes);
               GeoModel geoModel = GeoBuilder.getGeoBuilder(geoRes.getNamespace()).constructGeoModel(tree);
               GeckoLibCache.getInstance().getGeoModels().put(geoRes, geoModel);
               modelData.setFallbackTexture(geoRes);
               modelData.setTextureLocation(textureLoc);
               modelDataMap.put(modelName, modelData);
               ModelNotifications.logError(Level.DEBUG, String.format("successfully registered custom model '%s' from zip", modelName));
               return "";
            } catch (Exception e) {
               return String.format("The geo model for the custom model '%s' inside zip '%s' appears to be corrupted.", modelName, zipFile.getName());
            }
         }
         modelData.setFallbackTexture(new ResourceLocation("sexmod", modelName + "Model"));
         modelData.setTextureLocation(textureLoc);
         modelDataMap.put(modelName, modelData);
         ModelNotifications.logError(Level.DEBUG, String.format("successfully registered custom model '%s' from zip", modelName));
         return "";
      } catch (IOException e) {
         return String.format("couldn't read zip for '%s': %s", modelName, e.getMessage());
      }
   }

   private static java.util.zip.ZipEntry findZipEntry(java.util.zip.ZipFile zf, String name) {
      String low = name.toLowerCase();
      java.util.Enumeration<? extends java.util.zip.ZipEntry> en = zf.entries();
      while (en.hasMoreElements()) {
         java.util.zip.ZipEntry e = en.nextElement();
         if (e.isDirectory()) continue;
         String ename = e.getName().toLowerCase();
         if (ename.endsWith("/" + low) || ename.equals(low) || ename.endsWith(low)) return e;
      }
      return null;
   }
}
