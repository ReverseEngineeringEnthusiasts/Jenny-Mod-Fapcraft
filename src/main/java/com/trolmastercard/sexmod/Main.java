package com.trolmastercard.sexmod;


import com.trolmastercard.sexmod.client.model.base.GirlModel;
import com.trolmastercard.sexmod.client.render.base.GirlRendererBase;
import com.trolmastercard.sexmod.common.command.CommandLocateGoblinLair;
import com.trolmastercard.sexmod.common.command.CommandReloadCustomModels;
import com.trolmastercard.sexmod.common.entity.base.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldEntity;
import com.trolmastercard.sexmod.proxy.CommonProxy;
import com.trolmastercard.sexmod.common.world.data.OwnershipSavedData;
import com.trolmastercard.sexmod.common.world.data.CustomModelCodeData;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldManager;
import com.trolmastercard.sexmod.common.model.ServerWhitelistManager;
import com.trolmastercard.sexmod.common.entity.base.GirlRegistry;
import com.trolmastercard.sexmod.client.render.effects.StructureMarkerRenderer;
import com.trolmastercard.sexmod.common.world.gen.ConfigWorldGenHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Random;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod(modid = Main.MOD_ID, name = Main.MOD_NAME, version = Main.DEFAULT_VERSION, dependencies = "after:geckolib")
/**
 * <b>Role.</b> Mod entry point ({@code @Mod sexmod}). Wires the proxies through
 * the Forge lifecycle (pre-init/init/post-init), registers the server commands,
 * and resets all global state on world close: girl list, {@link KoboldManager},
 * tribe UI values, {@link OwnershipSavedData}, worldgen config, {@link GirlRegistry},
 * {@link ServerWhitelistManager} and {@link CustomModelCodeData}, plus client-side
 * marker/bone-color caches.
 * <p>
 * <b>Pitfall.</b> {@link #onWorldClosed} must clear everything that holds
 * entity references — stale statics across world reloads cause ghost girls and
 * tribe desyncs. {@link #setConfigs()} reads/writes {@code config/sexmod.json}
 * (worldgen toggle, skin loading, flying) and must keep its exact JSON layout,
 * since the values are parsed by character offset.
 */
public class Main {
   public static final Logger LOGGER = LogManager.getLogger("sexmod");

   /** Fallback version for unfiltered builds; keep in step with pom.xml. */
   public static final String DEFAULT_VERSION = "1.1.3";

   /** Effective mod version: filtered pom version, else {@link #DEFAULT_VERSION}. */
   public static final String VERSION = loadVersion();

   private static final String BUILD_RESOURCE = "/sexmod-build.properties";

   public static final String MOD_ID = "sexmod";
   public static final String MOD_NAME = "Fapcraft";
   public static final String MOD_VERSION = VERSION;
   public static final String CLIENT_PROXY = "com.trolmastercard.sexmod.proxy.ClientProxy";
   public static final String COMMON_PROXY = "com.trolmastercard.sexmod.proxy.CommonProxy";
   public static final Random RANDOM = new Random();
   public static int BUTTON_ID_COUNTER = 0;

   /** Written by the structure-marker client tick, read by render interpolation. */
   public static Vec3d cameraPosCurrent = Vec3d.ZERO;
   public static Vec3d cameraPosPrevious = Vec3d.ZERO;

   /** Reads the pom-filtered build resource; falls back to the literal. */
   private static String loadVersion() {
      try (InputStream in = Main.class.getResourceAsStream(BUILD_RESOURCE)) {
         if (in == null) {
            return DEFAULT_VERSION;
         }
         Properties props = new Properties();
         props.load(in);
         String value = props.getProperty("version");
         if (value == null) {
            return DEFAULT_VERSION;
         }
         value = value.trim();
         if (value.isEmpty() || value.contains("${")) {
            return DEFAULT_VERSION;
         }
         return value;
      } catch (Throwable ignored) {
         return DEFAULT_VERSION;
      }
   }

   @Mod.Instance
   public static Main instance;

   // CleanroomGradle's userdev loads the mod with a different annotation
   // classloader than ProxyInjector, which makes @SidedProxy resolve to null.
   // Build the proxy by side in preInit instead; the sided delegate is not set
   // yet when the @Mod class is first loaded. The client class is looked up
   // lazily so a dedicated server never loads it.
   public static CommonProxy proxy;

   private static CommonProxy createProxy() {
      if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
         try {
            return (CommonProxy)Class.forName(CLIENT_PROXY).newInstance();
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create the client proxy", e);
         }
      }

      return new CommonProxy();
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      if (proxy == null) {
         proxy = createProxy();
      }

      printBanner();
      GeckoLib.initialize();
      proxy.preInitRegistries(event);
   }

   private static void printBanner() {
      try (java.io.InputStream in = Main.class.getResourceAsStream("/banner.txt");
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
         String line;
         while ((line = reader.readLine()) != null) {
            LOGGER.info(line.replace("%VERSION%", VERSION));
         }
      } catch (Exception e) {
         LOGGER.warn("Could not read banner.txt: {}", e.toString());
      }
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      proxy.initRegistries(event);
   }

   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {
      proxy.postInit(event);
   }

   @EventHandler
   public static void onWorldClosed(FMLServerStoppedEvent event) {
      BaseGirlEntity.getGirlEntityList().clear();
      KoboldManager.clearAll();
      KoboldEntity.tribeMembers.clear();
      OwnershipSavedData.clearAll();
      ConfigWorldGenHandler.getInstance().clear();
      GirlRegistry.clearAll();
      ServerWhitelistManager.isLoaded = false;
      CustomModelCodeData.clearAll();
      if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
         clientReset();
      }
   }

   @EventHandler
   public static void onWorldStart(FMLServerStartingEvent event) {
      event.registerServerCommand(CommandLocateGoblinLair.LOCATE_GOBLIN_LAIR_COMMAND);
      event.registerServerCommand(CommandReloadCustomModels.RELOAD_CUSTOM_MODELS_COMMAND);
   }

   @SideOnly(Side.CLIENT)
   static void clientReset() {
      StructureMarkerRenderer.clearMarkers();
      GirlRendererBase.clearBoneColors();
   }

   @SideOnly(Side.CLIENT)
   @EventHandler
   public void registerReplacedRenderers(FMLInitializationEvent event) {
      GeckoLib.initialize();
   }

   public static void setConfigs() throws IOException {
      File configDir = new File("config");
      configDir.mkdir();
      File configFile = new File("config/sexmod.json");
      if (!configFile.exists()) {
         configFile.createNewFile();
         FileWriter writer = new FileWriter(configFile);
         writer.write("{\"shouldGenBuildings\":true,\"shouldLoadOtherSkins\":false,\"allowFlying\":true}");
         writer.close();
      }

      StringBuilder jsonBuilder = new StringBuilder();
      BufferedReader reader = new BufferedReader(new FileReader(configFile));
      try {
         String line;
         while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
         }
      } finally {
         reader.close();
      }

      String json = jsonBuilder.toString();
      if (!json.contains("shouldGenBuildings")) {
         configFile.delete();
         configFile = new File("config/sexmod.json");
         configFile.createNewFile();
         FileWriter writer2 = new FileWriter(configFile);
         writer2.write("{\"shouldGenBuildings\":true,\"shouldLoadOtherSkins\":false,\"allowFlying\":true}");
         writer2.close();
         ConfigWorldGenHandler.GENERATION_ENABLED = true;
         GirlModel.enableModelCache = false;
         AbstractPlayerGirlEntity.flightGrantEnabled = true;
      } else {
         int genIdx = json.indexOf("shouldGenBuildings");
         int skinIdx = json.indexOf("shouldLoadOtherSkins");
         int flyIdx = json.indexOf("allowFlying");
         ConfigWorldGenHandler.GENERATION_ENABLED = 't' == json.charAt(genIdx + 20);
         GirlModel.enableModelCache = 't' == json.charAt(skinIdx + 22);
         AbstractPlayerGirlEntity.flightGrantEnabled = 't' == json.charAt(flyIdx + 13);
      }
   }
}
