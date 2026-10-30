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
import java.io.File;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
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
      ModelNotifications.logError(level, message);
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
      ServerWhitelistFile.addServer(serverName);
   }

   public static boolean isModelWhitelisted(String serverName) {
      return ServerWhitelistFile.contains(serverName);
   }

   static HashSet<String> loadWhitelistedServers() {
      return ServerWhitelistFile.read();
   }

   public static float getModelZOffset(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      return modelData == null ? 0.0F : modelData.getZOffset();
   }

   @SideOnly(Side.CLIENT)
   static void syncModelData() {
      ModelResources.releaseAll(modelDataMap);
   }

   @SideOnly(Side.CLIENT)
   static void logInfo(Level level, String message) {
      ModelNotifications.logInfo(level, message);
   }

   public static String getCurrentGroup() {
      return Main.proxy instanceof ClientProxy ? getGlobalModelOverride() : "sexmod_custom_models";
   }

   /** Local mod version, e.g. "1.1.1". */
   public static String localVersion() {
      return Reference.MOD_VERSION;
   }

   /** Local build number for the release stream (matches GitHub "Fapcraft Build N" tags). */
   public static int localBuild() {
      return 8;
   }

   /** Fired on the first world join: checks GitHub releases once per session and
    *  shows a chat message if a newer release is available (state file prevents
    *  re-pestering across sessions for the same version+release). */
   @SideOnly(Side.CLIENT)
   public static void checkForUpdatesOnce() {
      UpdateNotice.checkOnce();
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
      } catch (Exception ignored) {
         // ignored: a broken zip entry just hides that model
      }
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
      return ModelArchive.getPartName(modelName, zipFileMap, group);
   }

   @SideOnly(Side.CLIENT)
   static ResourceLocation loadTexture(String name, File file) throws IOException {
      return ModelResources.loadTexture(name, file);
   }

   @SideOnly(Side.CLIENT)
   static RawGeoModel loadGeoModel(File file) throws IOException {
      return ModelResources.loadGeoModel(file);
   }

   public static String registerModel(String modelName, String group, boolean textureFlag) {
      if (modelDataMap.get(modelName) != null) {
         return String.format("already registered '%s'... honestly, unsure how this could happen lol", modelName);
      }
      // zip-direct path: read .cfg/.png/.geo.json straight from the zip
      if (zipFileMap.containsKey(modelName)) {
         return ModelArchive.registerFromZip(modelName, zipFileMap, modelDataMap, textureFlag);
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
   public static ResourceLocation getModelResource(String modelName) {
      ServerWhitelistManager.ModelData modelData = modelDataMap.get(modelName);
      if (modelData == null) {
         if (!modelName.equals("cross")) {
            Main.LOGGER.warn("The custom model for '{}', hasn't been registered, but gamers tried to use it anyways. Crash is imminent", modelName);
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
            Main.LOGGER.warn("The custom texture for '{}', hasn't been registered, but gamers tried to use it anyways. Crash is imminent", modelName);
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
            Main.LOGGER.warn("The ClothingType for '{}', hasn't been registered, but gamers tried to use it anyways. Crash is imminent", modelName);
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
            Main.LOGGER.warn("The HashSet<GirlType> for '{}', hasn't been registered, but gamers tried to use it anyways. Crash is imminent", modelName);
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
            Main.LOGGER.warn("The HashSet<String> for '{}', hasn't been registered, but gamers tried to use it anyways. Crash is imminent", modelName);
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
            Main.LOGGER.warn("The author for '{}', hasn't been registered, but gamers tried to use it anyways. Crash is imminent", modelName);
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
         ModelCfgParser.parse(file, modelName, this);
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
