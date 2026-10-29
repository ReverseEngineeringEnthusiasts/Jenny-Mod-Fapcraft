package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.LightingType;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UnknownPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.reskin.ReskinEntry;
import com.trolmastercard.sexmod.reskin.ReskinScanner;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

/**
 * <b>Role.</b> Custom-model management on both sides.
 * <p>
 * CLIENT: registers the whitelist file ({@code sexmod/custom_models/whitelisted_servers.txt}),
 * decides whether a server may push models ({@link #isGlobalRenderingDisabled()}
 * — the current server IP must be whitelisted), and loads custom models
 * ({@code .cfg} + {@code .png} + {@code .geo.json}) from
 * {@code sexmod/custom_models/<server>/} into GeckoLib
 * ({@link #registerModel}), exposing them via
 * {@link #getModelDataForGirl(String)}, {@link #getModelParts(BaseGirlEntity)},
 * {@link #getModelResource(String)} etc.
 * <p>
 * SERVER: {@link #loadCustomModels(false)} registers model *names* (no textures/
 * geo) so {@link DownloadServerModelPacket} can serve the files.
 * <p>
 * <b>Flow.</b> {@link ChatHandler} triggers a reload on server connect / world
 * join (via {@link UnknownPacket}) and a full cache clear on disconnect; the
 * editor wand and model-code commands read the registered {@link ModelData}.
 * <p>
 * <b>Pitfalls.</b> {@code "cross"} is a reserved model name (internal fallback)
 * and {@code #}/spaces are illegal in model names — validation lives in
 * {@link ModelData}'s constructor. {@link #getCurrentGroup()} differs per side
 * (client: per-server folder; server: shared folder).
 */
public class ServerWhitelistManager {
   public static final String CUSTOM_MODELS_DIR = "sexmod/custom_models";
   static final String WHITELIST_FILE = "sexmod/custom_models/whitelisted_servers.txt";
   public static final String CUSTOM_MODELS_KEY = "sexmod_custom_models";
   static Map<String, ServerWhitelistManager.ModelData> modelDataMap = new HashMap<>();
   static Map<String, File> zipFileMap = new HashMap<>();
   public static boolean isGlobalRenderingDisabled = false;
   public static boolean isLoaded = false;

   public static Map<String, ServerWhitelistManager.ModelData> getModelDataMap() {
      return modelDataMap;
   }

   public static boolean isModelDisabled(String modelName) {
      return modelDataMap.get(modelName) != null;
   }

   public static int getModelCount(boolean disableRendering) {
      setGlobalRenderingDisabled(disableRendering);
      return loadCustomModels(disableRendering);
   }

   static void logError(Level level, String message) {
      if (Main.proxy instanceof ClientProxy) {
         logInfo(level, message);
      } else {
         Main.LOGGER.log(level, message);
      }
   }

   public static void setGlobalRenderingDisabled(boolean disabled) {
      if (disabled) {
         syncModelData();
      }

      modelDataMap.clear();
   }

   public static void reloadCustomModels() {
      PacketHandler.networkWrapper.sendToServer(new UnknownPacket());
   }

   @SideOnly(Side.CLIENT)
   public static boolean isGlobalRenderingDisabled() {
      String customModelsKey = getCustomModelsKey();
      return customModelsKey == null ? false : isModelWhitelisted(customModelsKey);
   }

   public static void initWhitelistFile(String serverName) {
      File file = new File("sexmod/custom_models/whitelisted_servers.txt");
      file.mkdirs();
      HashSet whitelist = new HashSet();
      if (file.exists()) {
         whitelist = loadWhitelistedServers();
      }

      whitelist.add(serverName);
      file.delete();
      file = new File("sexmod/custom_models/whitelisted_servers.txt");

      try {
         FileWriter writer = new FileWriter(file);
         Object primaryExc = null;
         boolean suppressed = false;

         label77: {
            Throwable throwable;
            try {
               suppressed = true;

               for (String server : (java.util.Collection<String>) (whitelist) ) {
                  writer.write(server + "\n");
               }

               suppressed = false;
               break label77;
            } catch (Throwable caughtThrowable) {
               throwable = caughtThrowable;
               suppressed = false;
            } finally {
               if (suppressed) {
                  if (writer != null) {
                     if (primaryExc != null) {
                        try {
                           writer.close();
                        } catch (Throwable suppressedThrowable) {
                           ((Throwable) primaryExc).addSuppressed(suppressedThrowable);
                        }
                     } else {
                        writer.close();
                     }
                  }
               }
            }

            throw new RuntimeException(throwable);
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException ioException) {
         ioException.printStackTrace();
      }
   }

   public static boolean isModelWhitelisted(String serverName) {
      return loadWhitelistedServers().contains(serverName);
   }

   static HashSet<String> loadWhitelistedServers() {
      File file = new File("sexmod/custom_models/whitelisted_servers.txt");

      try {
         file.createNewFile();
      } catch (Exception exception) {
         exception.printStackTrace();
      }

      HashSet servers = new HashSet();

      try {
         BufferedReader reader = new BufferedReader(new FileReader(file));
         try {
            String line;
            while ((line = reader.readLine()) != null) {
               servers.add(line);
            }
         } finally {
            reader.close();
         }

         return servers;
      } catch (IOException ioException) {
         ioException.printStackTrace();
         return new HashSet<>();
      }
   }

   public static float getModelZOffset(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      return modelData == null ? 0.0F : modelData.getZOffset();
   }

   @SideOnly(Side.CLIENT)
   static void syncModelData() {
      for (Entry entry : modelDataMap.entrySet()) {
         ServerWhitelistManager.ModelData modelData = (ServerWhitelistManager.ModelData)entry.getValue();
         if (modelData != null) {
            ResourceLocation fallbackTex = modelData.getFallbackTexture();
            ResourceLocation textureLoc = modelData.getTextureLocation();
            if (fallbackTex != null) {
               GeckoLibCache.getInstance().getGeoModels().remove(fallbackTex);
            }

            if (textureLoc != null) {
               Minecraft.getMinecraft().renderEngine.deleteTexture(textureLoc);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   static void logInfo(Level level, String message) {
      EntityPlayerSP player = Minecraft.getMinecraft().player;
      if (player == null) {
         Main.LOGGER.log(level, message);
      } else {
         TextFormatting color;
         if (Level.DEBUG.equals(level)) {
            color = TextFormatting.DARK_GREEN;
         } else if (Level.ERROR.equals(level)) {
            color = TextFormatting.RED;
         } else {
            color = TextFormatting.WHITE;
         }

         player.sendMessage(new TextComponentString(color.toString() + message));
      }
   }

   public static String getCurrentGroup() {
      return Main.proxy instanceof ClientProxy ? getGlobalModelOverride() : "sexmod_custom_models";
   }

   /** Local mod version, e.g. "1.1.1". */
   public static String localVersion() {
      return "1.1.1";
   }

   /** Local build number for the release stream (matches GitHub "Fapcraft Build N" tags). */
   public static int localBuild() {
      return 8;
   }

   /** Whether the update check already ran this session (never pester per join). */
   private static volatile boolean updateCheckRanThisSession = false;

   /** Fired on the first world join: checks GitHub releases once per session and
    *  shows a chat message if a newer release is available (state file prevents
    *  re-pestering across sessions for the same version+release). */
   @SideOnly(Side.CLIENT)
   public static void checkForUpdatesOnce() {
      if (updateCheckRanThisSession) return;
      updateCheckRanThisSession = true;
      Thread t = new Thread(() -> {
         try {
            String latest = com.trolmastercard.sexmod.reskin.UpdateChecker.fetchLatestTag();
            if (latest == null) return; // offline / no release — silent
            final int localBuild = localBuild();
            boolean outdated = com.trolmastercard.sexmod.reskin.UpdateChecker.isOutdatedBuild(localBuild, latest);
            if (outdated) {
               if (!com.trolmastercard.sexmod.reskin.UpdateChecker.alreadyNotified("build" + localBuild, latest)) {
                  com.trolmastercard.sexmod.reskin.UpdateChecker.markNotified("build" + localBuild, latest);
                  String body = com.trolmastercard.sexmod.reskin.UpdateChecker.fetchLatestBody();
                  final String change = com.trolmastercard.sexmod.reskin.UpdateChecker.summarizeBody(body, 5);
                  StringBuilder msg = new StringBuilder();
                  msg.append("§eYour Fapcraft is outdated! (installed build ").append(localBuild).append(", latest ").append(latest).append(")");
                  msg.append("§7\nDownload: §bhttps://github.com/ReverseEngineeringEnthusiasts/Jenny-Mod-Fapcraft/releases");
                  if (change != null && !change.isEmpty()) {
                     msg.append("\n§7What's new in ").append(latest).append(":\n").append(change);
                  }
                  Minecraft.getMinecraft().addScheduledTask(() -> {
                     if (Minecraft.getMinecraft().player != null) {
                        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(msg.toString()));
                     }
                  });
               }
            }
         } catch (Throwable ex) {
            // silent — update check must never crash the game
         }
      }, "sexmod-update-check");
      t.setDaemon(true);
      t.start();
   }

   @SideOnly(Side.CLIENT)
   public static String getGlobalModelOverride() {
      String customModelsKey = getCustomModelsKey();
      return customModelsKey == null ? "sexmod/custom_models/singleplayer" : "sexmod/custom_models/" + customModelsKey;
   }

   @SideOnly(Side.CLIENT)
   @Nullable
   public static String getCustomModelsKey() {
      Minecraft minecraft = Minecraft.getMinecraft();
      ServerData serverData = minecraft.getCurrentServerData();
      if (serverData == null) {
         return null;
      }

      String serverIp = serverData.serverIP;
      int portIndex = serverIp.indexOf(":");
      if (portIndex != -1) {
         serverIp = serverIp.substring(0, portIndex);
      }

      return serverIp;
   }

   public static int loadCustomModels(boolean global) {
      logError(Level.INFO, "loading up custom models...");
      String group = getCurrentGroup();
      File dir = new File(group);
      dir.mkdirs();
      String[] modelNames = dir.list((subDir, subName) -> new File(subDir, subName).isDirectory());
      // collect zips as virtual models (read directly, no extraction)
      java.util.Set<String> zipModels = new java.util.HashSet<>();
      try {
         File[] zips = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".zip"));
         if (zips != null) for (File z : zips) {
            String base = z.getName().substring(0, z.getName().length() - 4);
            String safe = base.replaceAll("[\\s#\\$]+", "_").replaceAll("[^a-zA-Z0-9_\\-]", "");
            if (safe.isEmpty()) safe = "model";
            if (new File(dir, safe).exists()) continue; // dir takes precedence
            zipModels.add(safe);
            // map zip file for later direct read (store in temp map)
            zipFileMap.put(safe, z);
         }
      } catch (Exception ignored) {}
      // merge zip virtual names with directory names
      if (!zipModels.isEmpty()) {
         java.util.Set<String> merged = new java.util.HashSet<>(java.util.Arrays.asList(modelNames == null ? new String[0] : modelNames));
         merged.addAll(zipModels);
         modelNames = merged.toArray(new String[0]);
      }
      if (modelNames == null) {
         logError(
            Level.ERROR,
            String.format(
               "Something is wrong with the custom models folder at '%s'. Check if it exists, if not - make the directory yourself because Minecraft cannot do it itself for some reason",
               dir.getAbsolutePath()
            )
         );
         return -1;
      }

      logError(Level.INFO, String.format("found %s custom model(s)", modelNames.length));
      int count = 0;

      for (String modelName : modelNames) {
         String error = getPartName(modelName, group);
         if (!"".equals(error)) {
            // A single broken/container entry (e.g. the "Custom-Models" folder
            // that just HOLDS sub-packs, or an animation-only zip) must NOT kill
            // the whole load. Skip it and keep registering the valid models.
            File probeDir = new File(group, modelName);
            boolean looksLikeContainer = probeDir.isDirectory()
               && !new File(probeDir, modelName + ".cfg").isFile()
               && !new File(probeDir, modelName + ".geo.json").isFile();
            if (looksLikeContainer) {
               logError(Level.DEBUG, String.format("skipping folder '%s' (no model files inside - container folder)", modelName));
               continue;
            }
            logError(Level.DEBUG, String.format("skipping custom model '%s': %s", modelName, error));
            continue;
         }

         error = registerModel(modelName, group, global);
         if (!"".equals(error)) {
            logError(Level.DEBUG, String.format("skipping custom model '%s': %s", modelName, error));
            continue;
         }

         count++;
      }

      logError(Level.DEBUG, String.format("successfully registered %s custom models", count));
      // Recursive discovery: the legacy loader only reads IMMEDIATE children, but
      // real installs nest packs arbitrarily deep (Custom-Models/...). Scan the
      // whole tree (zip central dir + rar headers only, no decompression) and
      // report what's actually available so the reload reflects reality.
      try {
         java.util.List<ReskinEntry> packs = new ReskinScanner().scan(dir);
         int total = packs.size();
         int zipN = 0, rarN = 0, reskins = 0;
         for (ReskinEntry pe : packs) {
            if (pe.kind == ReskinEntry.Kind.ZIP) zipN++; else rarN++;
            if (pe.isReskin()) reskins++;
         }
         recursivePackCount = total;
         logError(Level.INFO, String.format(
            "recursive pack scan: %d pack(s) found (any depth): %d zip, %d rar; %d resourcepack reskins available (apply via the wardrobe's Reskins button)",
            total, zipN, rarN, reskins));
      } catch (Throwable t) {
         logError(Level.DEBUG, "recursive pack scan failed: " + t.getMessage());
      }
      isLoaded = true;
      return 0;
   }

   /** Total archives found by the last recursive pack scan (0 if none/not scanned). */
   public static int recursivePackCount = 0;

   public static String getPartName(String modelName, String group) {
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

   @SideOnly(Side.CLIENT)
   static ResourceLocation loadTexture(String name, File file) throws IOException {
      BufferedImage image = ImageIO.read(file);
      return Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(name, new DynamicTexture(image));
   }

   @SideOnly(Side.CLIENT)
   static RawGeoModel loadGeoModel(File file) throws IOException {
      StringBuilder content = new StringBuilder();
      BufferedReader reader = new BufferedReader(new FileReader(file));
      try {
         String line;
         while ((line = reader.readLine()) != null) {
            content.append(line);
         }
      } finally {
         reader.close();
      }

      String json = content.toString();
      return Converter.fromJsonString(json);
   }

   public static String registerModel(String modelName, String group, boolean textureFlag) {
      if (modelDataMap.get(modelName) != null) {
         return String.format("already registered '%s'... honestly, unsure how this could happen lol", modelName);
      }
      // zip-direct path: read .cfg/.png/.geo.json straight from the zip
      if (zipFileMap.containsKey(modelName)) {
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
                  } catch (Exception ignored2) {}
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
                  logError(Level.DEBUG, String.format("successfully registered custom model '%s' from zip", modelName));
                  return "";
               } catch (Exception e) {
                  return String.format("The geo model for the custom model '%s' inside zip '%s' appears to be corrupted.", modelName, zipFile.getName());
               }
            }
            modelData.setFallbackTexture(new ResourceLocation("sexmod", modelName + "Model"));
            modelData.setTextureLocation(textureLoc);
            modelDataMap.put(modelName, modelData);
            logError(Level.DEBUG, String.format("successfully registered custom model '%s' from zip", modelName));
            return "";
         } catch (IOException e) {
            return String.format("couldn't read zip for '%s': %s", modelName, e.getMessage());
         }
      }
      String modelDir = String.format("%s/%s/", group, modelName);
      String cfgPath = modelDir + modelName + ".cfg";
      File cfgFile = new File(cfgPath);
      if (!cfgFile.exists()) {
         return String.format("couldn't find cfg File for '%s'. It should have been at '%s'. Are you sure it exists?", modelName, cfgPath);
      }

      ServerWhitelistManager.ModelData modelData = new ServerWhitelistManager.ModelData(cfgFile, modelName);
      if (modelData.errorMessage != null) {
         return modelData.errorMessage;
      }

      String texturePath = modelDir + modelName + ".png";
      File textureFile = new File(texturePath);
      if (!textureFile.exists()) {
         return String.format("The texture for the custom model '%s' couldn't be found at '%s' are you sure it exists?", modelName, texturePath);
      }

      ResourceLocation textureLoc = null;
      if (textureFlag) {
         try {
            textureLoc = loadTexture(modelName, textureFile);
         } catch (IOException ioException) {
            return String.format("The texture for the custom model '%s' at '%s' appears to be corrupted. Try making a new one", modelName, texturePath);
         } catch (Exception exception) {
            return String.format(
               "Couldn't load the texture for the custom model '%s' at '%s'. Maybe try increasing the amount of RAM of ur Minecraft client", modelName, textureFile
            );
         }
      }

      ResourceLocation geoResource = new ResourceLocation("sexmod", modelName + "Model");
      String geoPath = modelDir + modelName + ".geo.json";
      File geoFile = new File(geoPath);
      if (!geoFile.exists()) {
         return String.format("The geo model for the custom model '%s' couldn't be found at '%s' are you sure it exists?", modelName, geoPath);
      }

      if (textureFlag) {
         RawGeoModel rawGeoModel;
         try {
            rawGeoModel = loadGeoModel(geoFile);
         } catch (IOException ioException2) {
            return String.format("The geo model for the custom model '%s' at '%s' appears to be corrupted. Try replacing it.", modelName, geoPath);
         }

         try {
            RawGeometryTree geometryTree = RawGeometryTree.parseHierarchy(rawGeoModel, geoResource);
            GeoModel geoModel = GeoBuilder.getGeoBuilder(geoResource.getNamespace()).constructGeoModel(geometryTree);
            GeckoLibCache.getInstance().getGeoModels().put(geoResource, geoModel);
         } catch (Exception exception2) {
            return String.format("The geo model for the custom model '%s' at '%s' appears to be corrupted. Try replacing it.", modelName, geoPath);
         }
      }

      if (textureFlag) {
         modelData.setFallbackTexture(geoResource);
         modelData.setTextureLocation(textureLoc);
      }

      modelDataMap.put(modelName, modelData);
      logError(Level.DEBUG, String.format("successfully registered custom model '%s'", modelName));
      return "";
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

   public static ResourceLocation getModelResource(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      if (modelData == null) {
         if (!modelName.equals("cross")) {
            System.out.printf("The custom model for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", modelName);
         }

         return null;
      } else {
         return modelData.getFallbackTexture();
      }
   }

   public static ResourceLocation getModelTexture(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      if (modelData == null) {
         if (!modelName.equals("cross")) {
            System.out.printf("The custom texture for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", modelName);
         }

         return null;
      } else {
         return modelData.getTextureLocation();
      }
   }

   public static GeoModel getGeoModel(String modelName) {
      return GeckoLibCache.getInstance().getGeoModels().get(getModelResource(modelName));
   }

   public static BoneType getBoneType(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      if (modelData == null) {
         if (!modelName.equals("cross")) {
            System.out.printf("The ClothingType for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", modelName);
         }

         return BoneType.HEAD;
      } else {
         return modelData.boneType;
      }
   }

   public static HashSet<NpcType> getAllowedNpcTypes(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      if (modelData == null) {
         if (!modelName.equals("cross")) {
            System.out.printf("The HashSet<GirlType> for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", modelName);
         }

         return null;
      } else {
         return modelData.allowedNpcTypes;
      }
   }

   public static HashSet<String> getCustomPartBones(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      if (modelData == null) {
         if (!modelName.equals("cross")) {
            System.out.printf("The HashSet<String> for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", modelName);
         }

         return new HashSet<>();
      } else {
         return modelData.customPartBones;
      }
   }

   public static String getModelCode(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      if (modelData == null) {
         if (!modelName.equals("cross")) {
            System.out.printf("The author for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", modelName);
         }

         return "";
      } else {
         return modelData.modelCode;
      }
   }

   @Nullable
   public static ServerWhitelistManager.ModelData getModelDataForGirl(String modelName) {
      return modelDataMap.get(modelName);
   }

   public static HashMap<BoneType, List<String>> getModelParts(BaseGirlEntity girl) {
      HashMap partsMap = new HashMap();

      for (BoneType boneType : BoneType.values()) {
         partsMap.put(boneType, new ArrayList());
      }

      for (Entry entry : modelDataMap.entrySet()) {
         String modelName = (String)entry.getKey();
         ServerWhitelistManager.ModelData modelData = (ServerWhitelistManager.ModelData)entry.getValue();
         BoneType boneType2 = modelData.boneType;
         List models = (List)partsMap.get(boneType2);
         if (modelData.allowedNpcTypes.isEmpty() || modelData.allowedNpcTypes.contains(NpcType.getNpcType(girl))) {
            models.add(modelName);
            partsMap.put(boneType2, models);
         }
      }

      return partsMap;
   }

   public static HashMap<String, Float> getModelScales() {
      HashMap scales = new HashMap();

      for (Entry entry : getModelDataMap().entrySet()) {
         scales.put(entry.getKey(), ((ServerWhitelistManager.ModelData)entry.getValue()).getZOffset());
      }

      return scales;
   }

   @SideOnly(Side.CLIENT)
   /**
 * <b>Role.</b> Client-side events for the custom-model lifecycle: the
 * {@code id} chat command (girl UUID lookup), model (re)load on server connect,
 * the model-download request when first joining a whitelisted server, and cache
 * cleanup on disconnect.
 */
public static class ChatHandler {
      boolean hasSentId = false;

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onClientChat(ClientChatEvent event) {
         String message = event.getOriginalMessage();
         if ("id".equals(message)) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            List nearbyGirls = player.world.getEntitiesWithinAABB(BaseGirlEntity.class, player.getEntityBoundingBox().grow(10.0));
            BaseGirlEntity closestGirl = null;

            for (BaseGirlEntity girl : (java.util.Collection<BaseGirlEntity>) (nearbyGirls) ) {
               if (closestGirl == null) {
                  closestGirl = girl;
               } else if (player.getDistance(girl) < player.getDistance(closestGirl)) {
                  closestGirl = girl;
               }
            }

            if (closestGirl != null) {
               player.sendStatusMessage(new TextComponentString(closestGirl.getGirlId().toString()), false);
               event.setCanceled(true);
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onServerConnect(ClientConnectedToServerEvent event) {
         Minecraft minecraft = Minecraft.getMinecraft();
         minecraft.addScheduledTask(() -> ServerWhitelistManager.loadCustomModels(true));
         this.hasSentId = false;
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onEntityJoinWorld(EntityJoinWorldEvent event) {
         if (event.getEntity().equals(Minecraft.getMinecraft().player)) {
            if (!this.hasSentId) {
               this.hasSentId = true;
               if (ServerWhitelistManager.isGlobalRenderingDisabled()) {
                  ServerWhitelistManager.reloadCustomModels();
               }
               ServerWhitelistManager.checkForUpdatesOnce();
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onServerDisconnect(ClientDisconnectionFromServerEvent event) {
         Minecraft.getMinecraft().addScheduledTask(() -> ServerWhitelistManager.setGlobalRenderingDisabled(true));
         this.hasSentId = false;
      }

   }

   /**
 * <b>Role.</b> Parsed representation of one custom model's {@code .cfg} file:
 * wear type ({@link BoneType}), allowed girls ({@link NpcType}s), lighting,
 * author/model-code, bones to hide, nude-mode enable, GUI scale/position and the
 * registered textures. The constructor validates every field and stores the
 * first problem in {@link #errorMessage} (callers abort registration on a
 * non-null message).
 * <p>
 * <b>Pitfall.</b> The cfg property names are part of the user-facing mod
 * format (docs/examples depend on them) — renaming a property breaks every
 * existing custom model.
 */
public static class ModelData {
      BoneType boneType;
      HashSet<NpcType> allowedNpcTypes = new HashSet<>();
      HashSet<String> customPartBones = new HashSet<>();
      String modelCode;
      String modelName;
      boolean disabled;
      LightingType lightingType;
      float scale = 1.0F;
      float xOffset = 0.0F;
      ResourceLocation textureLocation;
      ResourceLocation fallbackTexture;
      public String errorMessage = null;
      float zOffset;

      public ModelData(File file, String modelName) {
         if (modelName.contains(" ") || modelName.contains("#") || modelName.contains("$")) {
            this.errorMessage = String.format("You cannot call your custom model '%s'. '#', '$' and spaces are illegal characters", modelName);
         } else if ("cross".equalsIgnoreCase(modelName)) {
            this.errorMessage = "You cannot call your custom model 'cross'. Im sorry, but I need that specific name for internal stuff";
         } else {
            Properties properties = new Properties();

            FileInputStream inputStream;
            try {
               inputStream = new FileInputStream(file);
            } catch (FileNotFoundException notFoundExc) {
               this.errorMessage = String.format("couldn't find cfg File for '%s'. It should have been at '%s'. Are you sure it exists?", modelName, file.getAbsolutePath());
               return;
            }

            try {
               properties.load(inputStream);
            } catch (IOException ioExc) {
               this.errorMessage = String.format(
                  "couldn't read the cfg File for '%s' at '%s'. It appears to be corrupted. Try making a new one", modelName, file.getAbsolutePath()
               );
               return;
            }

            String wearType = properties.getProperty("wear_type");
            if (wearType == null) {
               this.errorMessage = String.format(
                  "The cfg File for the model '%s' at '%s' is missing the 'wear_type'. Go to the bottom of the cfg File and write 'wear_type=HEAD'. Check the cfg files of my examples to see what values for 'wear_type' are possible",
                  modelName,
                  file.getAbsolutePath()
               );
            } else {
               try {
                  wearType = wearType.replace(" ", "");
                  this.boneType = BoneType.valueOf(wearType);
               } catch (IllegalArgumentException illegalArg) {
                  this.errorMessage = String.format(
                     "you entered '%s' into the 'wear_type' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'wear_type",
                     wearType,
                     modelName,
                     file.getAbsolutePath()
                  );
                  return;
               }

               if (BoneType.CUSTOM_BONE.equals(this.boneType)) {
                  this.modelName = properties.getProperty("custom_bone");
                  if ("".equals(this.modelName)) {
                     this.errorMessage = String.format(
                        "You selected CUSTOM_BONE as the 'wear_type' in the cfg file for '%s' at '%s', yet you left the 'custom_bone' field right underneath it empty. If you want ur model to be parented to a specific bone, you have to enter the name of that bone at the field 'custom_bone'.",
                        modelName,
                        file.getAbsolutePath()
                     );
                     return;
                  }
               }

               String girlsStr = properties.getProperty("which_girls");
               if (girlsStr == null) {
                  girlsStr = "";
               }

               girlsStr = girlsStr.replace(" ", "");
               String[] girlNames = girlsStr.split(",");

               for (String girlName : girlNames) {
                  try {
                     if (!"".equals(girlName)) {
                        this.allowedNpcTypes.add(NpcType.valueOf(girlName));
                     }
                  } catch (IllegalArgumentException illegalArg2) {
                     this.errorMessage = String.format(
                        "you entered '%s' as one of the girls, you put into the 'which_girls' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'which_girls'.",
                        girlName,
                        modelName,
                        file.getAbsolutePath()
                     );
                     return;
                  }
               }

               String lightingStr = properties.getProperty("which_lighting");
               if (lightingStr == null) {
                  this.errorMessage = String.format(
                     "The %s's cfg file at '%s' doesn't contain the field 'which_lighting'. Go to the bottom of the cfg file and write either 'which_lighting=DEFAULT', 'which_lighting=SEXMOD', or 'which_lighting=NONE'.",
                     modelName,
                     file.getAbsolutePath()
                  );
               } else {
                  lightingStr = lightingStr.replace(" ", "");

                  try {
                     this.lightingType = LightingType.valueOf(lightingStr);
                  } catch (IllegalArgumentException illegalArg3) {
                     this.errorMessage = String.format(
                        "you entered '%s' into the 'which_lighting' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'which_lighting'.",
                        lightingStr,
                        modelName,
                        file.getAbsolutePath()
                     );
                  }

                  String author = properties.getProperty("author");
                  if (author != null && !"".equals(author)) {
                     this.modelCode = author;
                  } else {
                     this.modelCode = "anon";
                  }

                  String hideBonesStr = properties.getProperty("bones_to_hide");
                  if (hideBonesStr != null && !"".equals(hideBonesStr)) {
                     hideBonesStr = hideBonesStr.replace(" ", "");
                     String[] boneNames = hideBonesStr.split(",");
                     this.customPartBones.addAll(Arrays.asList(boneNames));
                  }

                  String nudeStr = properties.getProperty("enable_when_nude");
                  if (nudeStr == null) {
                     this.disabled = false;
                  } else {
                     nudeStr = nudeStr.replace(" ", "");
                     this.disabled = nudeStr.equalsIgnoreCase("yes");
                  }

                  String scaleStr = properties.getProperty("gui_size_factor");
                  if (scaleStr != null && !"".equals(scaleStr)) {
                     scaleStr = scaleStr.replace(" ", "");
                     scaleStr = scaleStr.replace(",", ".");

                     try {
                        this.scale = Float.parseFloat(scaleStr);
                     } catch (NumberFormatException numberExc) {
                        this.errorMessage = String.format(
                           "you entered '%s' into the 'gui_size_factor' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'gui_size_factor'.",
                           scaleStr,
                           modelName,
                           file.getAbsolutePath()
                        );
                     }
                  }

                  String posStr = properties.getProperty("gui_vertical_positioning");
                  if (posStr != null && !"".equals(posStr)) {
                     posStr = posStr.replace(" ", "");
                     posStr = posStr.replace(",", ".");

                     try {
                        this.xOffset = Float.parseFloat(posStr);
                     } catch (NumberFormatException numberExc2) {
                        this.errorMessage = String.format(
                           "you entered '%s' into the 'gui_vertical_positioning' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'gui_vertical_positioning'.",
                           posStr,
                           modelName,
                           file.getAbsolutePath()
                        );
                     }
                  }

                  String versionStr = properties.getProperty("version");
                  versionStr = versionStr.replace(" ", "");
                  versionStr = versionStr.replace(",", ".");

                  try {
                     this.zOffset = Float.parseFloat(versionStr);
                  } catch (NumberFormatException numberExc3) {
                     this.errorMessage = String.format(
                        "you entered '%s' into the 'versionString' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'versionString'.",
                        versionStr,
                        modelName,
                        file.getAbsolutePath()
                     );
                  }
               }
            }
         }
      }

      public String getModelName() {
         return this.modelName;
      }

      public LightingType getLightingType() {
         return this.lightingType;
      }

      public float getXOffset() {
         return this.xOffset;
      }

      public float getScale() {
         return this.scale;
      }

      public BoneType getBoneType() {
         return this.boneType;
      }

      public HashSet<NpcType> getAllowedNpcTypes() {
         return this.allowedNpcTypes;
      }

      public String getModelCode() {
         return this.modelCode;
      }

      public boolean isDisabled() {
         return this.disabled;
      }

      public HashSet<String> getCustomPartBones() {
         return this.customPartBones;
      }

      public ResourceLocation getTextureLocation() {
         return this.textureLocation;
      }

      public void setTextureLocation(ResourceLocation textureLoc) {
         this.textureLocation = textureLoc;
      }

      public ResourceLocation getFallbackTexture() {
         return this.fallbackTexture;
      }

      public void setFallbackTexture(ResourceLocation fallback) {
         this.fallbackTexture = fallback;
      }

      public float getZOffset() {
         return this.zOffset;
      }
   }
}
