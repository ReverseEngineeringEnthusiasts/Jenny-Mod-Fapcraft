package com.trolmastercard.sexmod;

import com.trolmastercard.sexmod.client.model.GirlModel;
import com.trolmastercard.sexmod.client.renderer.GirlRendererBase;
import com.trolmastercard.sexmod.command.CommandLocateGoblinLair;
import com.trolmastercard.sexmod.command.CommandReloadCustomModels;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.proxy.CommonProxy;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.GirlWorldData;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.GirlRegistry;
import com.trolmastercard.sexmod.util.StructureMarkerRenderer;
import com.trolmastercard.sexmod.worldgen.ConfigWorldGenHandler;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
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

@Mod(modid = "sexmod", name = "Fapcraft", version = "1.1.0", dependencies = "after:geckolib")
/**
 * <b>Role.</b> Mod entry point ({@code @Mod sexmod}). Wires the proxies through
 * the Forge lifecycle (pre-init/init/post-init), registers the server commands,
 * and resets all global state on world close: girl list, {@link KoboldManager},
 * tribe UI values, {@link GirlSavedData}, worldgen config, {@link GirlRegistry},
 * {@link ServerWhitelistManager} and {@link GirlWorldData}, plus client-side
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

   @Mod.Instance
   public static Main instance;

   @SidedProxy(clientSide = "com.trolmastercard.sexmod.proxy.ClientProxy", serverSide = "com.trolmastercard.sexmod.proxy.CommonProxy")
   public static CommonProxy proxy;

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      printBanner();
      GeckoLib.initialize();
      proxy.preInitRegistries(event);
   }

   private static void printBanner() {
      try (java.io.InputStream in = Main.class.getResourceAsStream("/banner.txt");
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
         String line;
         while ((line = reader.readLine()) != null) {
            LOGGER.info(line);
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
      KoboldEntity.aY.clear();
      GirlSavedData.clearAll();
      ConfigWorldGenHandler.getInstance().clear();
      GirlRegistry.clearAll();
      ServerWhitelistManager.isLoaded = false;
      GirlWorldData.clearAll();
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
         AbstractPlayerGirlEntity.ag = true;
      } else {
         int genIdx = json.indexOf("shouldGenBuildings");
         int skinIdx = json.indexOf("shouldLoadOtherSkins");
         int flyIdx = json.indexOf("allowFlying");
         ConfigWorldGenHandler.GENERATION_ENABLED = 't' == json.charAt(genIdx + 20);
         GirlModel.enableModelCache = 't' == json.charAt(skinIdx + 22);
         AbstractPlayerGirlEntity.ag = 't' == json.charAt(flyIdx + 13);
      }
   }
}
