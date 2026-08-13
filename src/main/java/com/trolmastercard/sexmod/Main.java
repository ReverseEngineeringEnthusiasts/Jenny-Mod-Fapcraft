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
public class Main {
   public static final Logger LOGGER = LogManager.getLogger("sexmod");

   @Mod.Instance
   public static Main instance;

   @SidedProxy(clientSide = "com.trolmastercard.sexmod.proxy.ClientProxy", serverSide = "com.trolmastercard.sexmod.proxy.CommonProxy")
   public static CommonProxy proxy;

   @EventHandler
   public void preInit(FMLPreInitializationEvent var1) {
      GeckoLib.initialize();
      proxy.preInitRegistries(var1);
   }

   @EventHandler
   public void init(FMLInitializationEvent var1) {
      proxy.initRegistries(var1);
   }

   @EventHandler
   public void postInit(FMLPostInitializationEvent var1) {
      proxy.postInit(var1);
   }

   @EventHandler
   public static void onWorldClosed(FMLServerStoppedEvent var0) {
      BaseGirlEntity.getGirlEntityList().clear();
      KoboldManager.clearAll();
      KoboldEntity.aY.clear();
      GirlSavedData.clearAll();
      ConfigWorldGenHandler.getInstance().clear();
      GirlRegistry.clearAll();
      ServerWhitelistManager.e = false;
      GirlWorldData.clearAll();
      if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
         clientReset();
      }
   }

   @EventHandler
   public static void onWorldStart(FMLServerStartingEvent var0) {
      var0.registerServerCommand(CommandLocateGoblinLair.a);
      var0.registerServerCommand(CommandReloadCustomModels.a);
   }

   @SideOnly(Side.CLIENT)
   static void clientReset() {
      StructureMarkerRenderer.clearMarkers();
      GirlRendererBase.clearBoneColors();
   }

   @SideOnly(Side.CLIENT)
   @EventHandler
   public void registerReplacedRenderers(FMLInitializationEvent var1) {
      GeckoLib.initialize();
   }

   public static void setConfigs() throws IOException {
      File var0 = new File("config");
      var0.mkdir();
      File var1 = new File("config/sexmod.json");
      if (!var1.exists()) {
         var1.createNewFile();
         FileWriter var2 = new FileWriter(var1);
         var2.write("{\"shouldGenBuildings\":true,\"shouldLoadOtherSkins\":false,\"allowFlying\":true}");
         var2.close();
      }

      StringBuilder var13 = new StringBuilder();
      BufferedReader var3 = new BufferedReader(new FileReader(var1));
      try {
         String var16;
         while ((var16 = var3.readLine()) != null) {
            var13.append(var16);
         }
      } finally {
         var3.close();
      }

      String var14 = var13.toString();
      if (!var14.contains("shouldGenBuildings")) {
         var1.delete();
         var1 = new File("config/sexmod.json");
         var1.createNewFile();
         FileWriter var15 = new FileWriter(var1);
         var15.write("{\"shouldGenBuildings\":true,\"shouldLoadOtherSkins\":false,\"allowFlying\":true}");
         var15.close();
         ConfigWorldGenHandler.i = true;
         GirlModel.d = false;
         AbstractPlayerGirlEntity.ag = true;
      } else {
         int var4 = var14.indexOf("shouldGenBuildings");
         int var17 = var14.indexOf("shouldLoadOtherSkins");
         int var6 = var14.indexOf("allowFlying");
         ConfigWorldGenHandler.i = 't' == var14.charAt(var4 + 20);
         GirlModel.d = 't' == var14.charAt(var17 + 22);
         AbstractPlayerGirlEntity.ag = 't' == var14.charAt(var6 + 13);
      }
   }
}
