package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.LightingType;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BoneType;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UnknownPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
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

public class ServerWhitelistManager {
   public static final String CUSTOM_MODELS_DIR = "sexmod/custom_models";
   static final String WHITELIST_FILE = "sexmod/custom_models/whitelisted_servers.txt";
   public static final String CUSTOM_MODELS_KEY = "sexmod_custom_models";
   static Map<String, ServerWhitelistManager.ModelData> modelDataMap = new HashMap<>();
   public static boolean isGlobalRenderingDisabled = false;
   public static boolean isLoaded = false;

   public static Map<String, ServerWhitelistManager.ModelData> getModelDataMap() {
      return modelDataMap;
   }

   public static boolean isModelDisabled(String var0) {
      return modelDataMap.get(var0) != null;
   }

   public static int getModelCount(boolean var0) {
      setGlobalRenderingDisabled(var0);
      return loadCustomModels(var0);
   }

   static void logError(Level var0, String var1) {
      if (null instanceof ClientProxy) {
         logInfo(var0, var1);
      } else {
         Main.LOGGER.log(var0, var1);
      }
   }

   public static void setGlobalRenderingDisabled(boolean var0) {
      if (var0) {
         syncModelData();
      }

      modelDataMap.clear();
   }

   public static void reloadCustomModels() {
      PacketHandler.networkWrapper.sendToServer(new UnknownPacket());
   }

   @SideOnly(Side.CLIENT)
   public static boolean isGlobalRenderingDisabled() {
      String var0 = getCustomModelsKey();
      return var0 == null ? false : isModelWhitelisted(var0);
   }

   public static void initWhitelistFile(String var0) {
      File var1 = new File("sexmod/custom_models/whitelisted_servers.txt");
      var1.mkdirs();
      HashSet var2 = new HashSet();
      if (var1.exists()) {
         var2 = loadWhitelistedServers();
      }

      var2.add(var0);
      var1.delete();
      var1 = new File("sexmod/custom_models/whitelisted_servers.txt");

      try {
         FileWriter var3 = new FileWriter(var1);
         Object var4 = null;
         boolean var13 = false;

         label77: {
            Throwable var5;
            try {
               var13 = true;

               for (String var6 : (java.util.Collection<String>) (var2) ) {
                  var3.write(var6 + "\n");
               }

               var13 = false;
               break label77;
            } catch (Throwable var15) {
               var5 = var15;
               var13 = false;
            } finally {
               if (var13) {
                  if (var3 != null) {
                     if (var4 != null) {
                        try {
                           var3.close();
                        } catch (Throwable var14) {
                           ((Throwable) var4).addSuppressed(var14);
                        }
                     } else {
                        var3.close();
                     }
                  }
               }
            }

            throw new RuntimeException(var5);
         }

         if (var3 != null) {
            var3.close();
         }
      } catch (IOException var17) {
         var17.printStackTrace();
      }
   }

   public static boolean isModelWhitelisted(String var0) {
      return loadWhitelistedServers().contains(var0);
   }

   static HashSet<String> loadWhitelistedServers() {
      File var0 = new File("sexmod/custom_models/whitelisted_servers.txt");

      try {
         var0.createNewFile();
      } catch (Exception var14) {
         var14.printStackTrace();
      }

      HashSet var1 = new HashSet();

      try {
         BufferedReader var2 = new BufferedReader(new FileReader(var0));
         try {
            String var18;
            while ((var18 = var2.readLine()) != null) {
               var1.add(var18);
            }
         } finally {
            var2.close();
         }

         return var1;
      } catch (IOException var17) {
         var17.printStackTrace();
         return new HashSet<>();
      }
   }

   public static float getModelZOffset(String var0) {
      ServerWhitelistManager.ModelData var1 = modelDataMap.get(var0);
      return var1 == null ? 0.0F : var1.getZOffset();
   }

   @SideOnly(Side.CLIENT)
   static void syncModelData() {
      for (Entry var1 : modelDataMap.entrySet()) {
         ServerWhitelistManager.ModelData var2 = (ServerWhitelistManager.ModelData)var1.getValue();
         if (var2 != null) {
            ResourceLocation var3 = var2.getFallbackTexture();
            ResourceLocation var4 = var2.getTextureLocation();
            if (var3 != null) {
               GeckoLibCache.getInstance().getGeoModels().remove(var3);
            }

            if (var4 != null) {
               Minecraft.getMinecraft().renderEngine.deleteTexture(var4);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   static void logInfo(Level var0, String var1) {
      EntityPlayerSP var2 = Minecraft.getMinecraft().player;
      if (var2 == null) {
         Main.LOGGER.log(var0, var1);
      } else {
         TextFormatting var3;
         if (Level.DEBUG.equals(var0)) {
            var3 = TextFormatting.DARK_GREEN;
         } else if (Level.ERROR.equals(var0)) {
            var3 = TextFormatting.RED;
         } else {
            var3 = TextFormatting.WHITE;
         }

         var2.sendMessage(new TextComponentString(var3.toString() + var1));
      }
   }

   public static String getCurrentGroup() {
      return null instanceof ClientProxy ? getGlobalModelOverride() : "sexmod_custom_models";
   }

   @SideOnly(Side.CLIENT)
   public static String getGlobalModelOverride() {
      String var0 = getCustomModelsKey();
      return var0 == null ? "sexmod/custom_models/singleplayer" : "sexmod/custom_models/" + var0;
   }

   @SideOnly(Side.CLIENT)
   @Nullable
   public static String getCustomModelsKey() {
      Minecraft var0 = Minecraft.getMinecraft();
      ServerData var1 = var0.getCurrentServerData();
      if (var1 == null) {
         return null;
      }

      String var2 = var1.serverIP;
      int var3 = var2.indexOf(":");
      if (var3 != -1) {
         var2 = var2.substring(0, var3);
      }

      return var2;
   }

   public static int loadCustomModels(boolean var0) {
      logError(Level.INFO, "loading up custom models...");
      String var1 = getCurrentGroup();
      File var2 = new File(var1);
      var2.mkdirs();
      String[] var3 = var2.list((var0x, var1x) -> new File(var0x, var1x).isDirectory());
      if (var3 == null) {
         logError(
            Level.ERROR,
            String.format(
               "Something is wrong with the custom models folder at '%s'. Check if it exists, if not - make the directory yourself because Minecraft cannot do it itself for some reason",
               var2.getAbsolutePath()
            )
         );
         return -1;
      }

      logError(Level.INFO, String.format("found %s custom model(s)", var3.length));
      int var4 = 0;

      for (String var8 : var3) {
         String var9 = getPartName(var8, var1);
         if (!"".equals(var9)) {
            logError(Level.ERROR, var9);
            return -1;
         }

         var9 = registerModel(var8, var1, var0);
         if (!"".equals(var9)) {
            logError(Level.ERROR, var9);
            return -1;
         }

         var4++;
      }

      logError(Level.DEBUG, String.format("successfully registered %s custom models", var4));
      isLoaded = true;
      return 0;
   }

   public static String getPartName(String var0, String var1) {
      String var2 = String.format("%s/%s", var1, var0);
      File var3 = new File(String.format("%s/%s.geo.json", var2, var0));
      File var4 = new File(String.format("%s/%s.png", var2, var0));
      File var5 = new File(String.format("%s/%s.cfg", var2, var0));
      if (!var3.exists()) {
         return String.format("couldn't find model File for '%s'. It should have been at '%s'. Are you sure it exists?", var0, var3.getAbsolutePath());
      } else if (!var4.exists()) {
         return String.format("couldn't find texture File for '%s'. It should have been at '%s'. Are you sure it exists?", var0, var4.getAbsolutePath());
      } else {
         return !var5.exists()
            ? String.format("couldn't find cfg File for '%s'. It should have been at '%s'. Are you sure it exists?", var0, var5.getAbsolutePath())
            : "";
      }
   }

   @SideOnly(Side.CLIENT)
   static ResourceLocation loadTexture(String var0, File var1) throws IOException {
      BufferedImage var2 = ImageIO.read(var1);
      return Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(var0, new DynamicTexture(var2));
   }

   @SideOnly(Side.CLIENT)
   static RawGeoModel loadGeoModel(File var0) throws IOException {
      StringBuilder var1 = new StringBuilder();
      BufferedReader var2 = new BufferedReader(new FileReader(var0));
      try {
         String var12;
         while ((var12 = var2.readLine()) != null) {
            var1.append(var12);
         }
      } finally {
         var2.close();
      }

      String var11 = var1.toString();
      return Converter.fromJsonString(var11);
   }

   public static String registerModel(String var0, String var1, boolean var2) {
      if (modelDataMap.get(var0) != null) {
         return String.format("already registered '%s'... honestly, unsure how this could happen lol", var0);
      }

      String var3 = String.format("%s/%s/", var1, var0);
      String var4 = var3 + var0 + ".cfg";
      File var5 = new File(var4);
      if (!var5.exists()) {
         return String.format("couldn't find cfg File for '%s'. It should have been at '%s'. Are you sure it exists?", var0, var4);
      }

      ServerWhitelistManager.ModelData var6 = new ServerWhitelistManager.ModelData(var5, var0);
      if (var6.errorMessage != null) {
         return var6.errorMessage;
      }

      String var8 = var3 + var0 + ".png";
      File var7 = new File(var8);
      if (!var7.exists()) {
         return String.format("The texture for the custom model '%s' couldn't be found at '%s' are you sure it exists?", var0, var8);
      }

      ResourceLocation var9 = null;
      if (var2) {
         try {
            var9 = loadTexture(var0, var7);
         } catch (IOException var18) {
            return String.format("The texture for the custom model '%s' at '%s' appears to be corrupted. Try making a new one", var0, var8);
         } catch (Exception var19) {
            return String.format(
               "Couldn't load the texture for the custom model '%s' at '%s'. Maybe try increasing the amount of RAM of ur Minecraft client", var0, var7
            );
         }
      }

      ResourceLocation var10 = new ResourceLocation("sexmod", var0 + "Model");
      String var12 = var3 + var0 + ".geo.json";
      File var13 = new File(var12);
      if (!var13.exists()) {
         return String.format("The geo model for the custom model '%s' couldn't be found at '%s' are you sure it exists?", var0, var12);
      }

      if (var2) {
         RawGeoModel var11;
         try {
            var11 = loadGeoModel(var13);
         } catch (IOException var17) {
            return String.format("The geo model for the custom model '%s' at '%s' appears to be corrupted. Try replacing it.", var0, var12);
         }

         try {
            RawGeometryTree var14 = RawGeometryTree.parseHierarchy(var11, var10);
            GeoModel var15 = GeoBuilder.getGeoBuilder(var10.getNamespace()).constructGeoModel(var14);
            GeckoLibCache.getInstance().getGeoModels().put(var10, var15);
         } catch (Exception var16) {
            return String.format("The geo model for the custom model '%s' at '%s' appears to be corrupted. Try replacing it.", var0, var12);
         }
      }

      if (var2) {
         var6.setFallbackTexture(var10);
         var6.setTextureLocation(var9);
      }

      modelDataMap.put(var0, var6);
      logError(Level.DEBUG, String.format("successfully registered custom model '%s'", var0));
      return "";
   }

   public static ResourceLocation getModelResource(String var0) {
      ServerWhitelistManager.ModelData var1 = modelDataMap.get(var0);
      if (var1 == null) {
         if (!var0.equals("cross")) {
            System.out.printf("The custom model for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", var0);
         }

         return null;
      } else {
         return var1.getFallbackTexture();
      }
   }

   public static ResourceLocation getModelTexture(String var0) {
      ServerWhitelistManager.ModelData var1 = modelDataMap.get(var0);
      if (var1 == null) {
         if (!var0.equals("cross")) {
            System.out.printf("The custom texture for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", var0);
         }

         return null;
      } else {
         return var1.getTextureLocation();
      }
   }

   public static GeoModel getGeoModel(String var0) {
      return GeckoLibCache.getInstance().getGeoModels().get(getModelResource(var0));
   }

   public static BoneType getBoneType(String var0) {
      ServerWhitelistManager.ModelData var1 = modelDataMap.get(var0);
      if (var1 == null) {
         if (!var0.equals("cross")) {
            System.out.printf("The ClothingType for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", var0);
         }

         return BoneType.HEAD;
      } else {
         return var1.boneType;
      }
   }

   public static HashSet<NpcType> getAllowedNpcTypes(String var0) {
      ServerWhitelistManager.ModelData var1 = modelDataMap.get(var0);
      if (var1 == null) {
         if (!var0.equals("cross")) {
            System.out.printf("The HashSet<GirlType> for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", var0);
         }

         return null;
      } else {
         return var1.allowedNpcTypes;
      }
   }

   public static HashSet<String> getCustomPartBones(String var0) {
      ServerWhitelistManager.ModelData var1 = modelDataMap.get(var0);
      if (var1 == null) {
         if (!var0.equals("cross")) {
            System.out.printf("The HashSet<String> for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", var0);
         }

         return new HashSet<>();
      } else {
         return var1.customPartBones;
      }
   }

   public static String getModelCode(String var0) {
      ServerWhitelistManager.ModelData var1 = modelDataMap.get(var0);
      if (var1 == null) {
         if (!var0.equals("cross")) {
            System.out.printf("The author for '%s', hasn't been registered, but gamers tried to use it anyways. Crash is imminent%n", var0);
         }

         return "";
      } else {
         return var1.modelCode;
      }
   }

   @Nullable
   public static ServerWhitelistManager.ModelData getModelDataForGirl(String var0) {
      return modelDataMap.get(var0);
   }

   public static HashMap<BoneType, List<String>> getModelParts(BaseGirlEntity var0) {
      HashMap var1 = new HashMap();

      for (BoneType var5 : BoneType.values()) {
         var1.put(var5, new ArrayList());
      }

      for (Entry var9 : modelDataMap.entrySet()) {
         String var10 = (String)var9.getKey();
         ServerWhitelistManager.ModelData var11 = (ServerWhitelistManager.ModelData)var9.getValue();
         BoneType var6 = var11.boneType;
         List var7 = (List)var1.get(var6);
         if (var11.allowedNpcTypes.isEmpty() || var11.allowedNpcTypes.contains(NpcType.getNpcType(var0))) {
            var7.add(var10);
            var1.put(var6, var7);
         }
      }

      return var1;
   }

   public static HashMap<String, Float> getModelScales() {
      HashMap var0 = new HashMap();

      for (Entry var2 : getModelDataMap().entrySet()) {
         var0.put(var2.getKey(), ((ServerWhitelistManager.ModelData)var2.getValue()).getZOffset());
      }

      return var0;
   }

   @SideOnly(Side.CLIENT)
   public static class ChatHandler {
      boolean hasSentId = false;

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onClientChat(ClientChatEvent var1) {
         String var2 = var1.getOriginalMessage();
         if ("id".equals(var2)) {
            EntityPlayerSP var3 = Minecraft.getMinecraft().player;
            List var4 = var3.world.getEntitiesWithinAABB(BaseGirlEntity.class, var3.getEntityBoundingBox().grow(10.0));
            BaseGirlEntity var5 = null;

            for (BaseGirlEntity var7 : (java.util.Collection<BaseGirlEntity>) (var4) ) {
               if (var5 == null) {
                  var5 = var7;
               } else if (var3.getDistance(var7) < var3.getDistance(var5)) {
                  var5 = var7;
               }
            }

            if (var5 != null) {
               var3.sendStatusMessage(new TextComponentString(var5.getGirlId().toString()), false);
               var1.setCanceled(true);
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onServerConnect(ClientConnectedToServerEvent var1) {
         Minecraft var2 = Minecraft.getMinecraft();
         var2.addScheduledTask(() -> ServerWhitelistManager.loadCustomModels(true));
         this.hasSentId = false;
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onEntityJoinWorld(EntityJoinWorldEvent var1) {
         if (var1.getEntity().equals(Minecraft.getMinecraft().player)) {
            if (!this.hasSentId) {
               this.hasSentId = true;
               if (ServerWhitelistManager.isGlobalRenderingDisabled()) {
                  ServerWhitelistManager.reloadCustomModels();
               }
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onServerDisconnect(ClientDisconnectionFromServerEvent var1) {
         Minecraft.getMinecraft().addScheduledTask(() -> ServerWhitelistManager.setGlobalRenderingDisabled(true));
         this.hasSentId = false;
      }

   }

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

      public ModelData(File var1, String var2) {
         if (var2.contains(" ") || var2.contains("#") || var2.contains("$")) {
            this.errorMessage = String.format("You cannot call your custom model '%s'. '#', '$' and spaces are illegal characters", var2);
         } else if ("cross".equalsIgnoreCase(var2)) {
            this.errorMessage = "You cannot call your custom model 'cross'. Im sorry, but I need that specific name for internal stuff";
         } else {
            Properties var3 = new Properties();

            FileInputStream var4;
            try {
               var4 = new FileInputStream(var1);
            } catch (FileNotFoundException var21) {
               this.errorMessage = String.format("couldn't find cfg File for '%s'. It should have been at '%s'. Are you sure it exists?", var2, var1.getAbsolutePath());
               return;
            }

            try {
               var3.load(var4);
            } catch (IOException var20) {
               this.errorMessage = String.format(
                  "couldn't read the cfg File for '%s' at '%s'. It appears to be corrupted. Try making a new one", var2, var1.getAbsolutePath()
               );
               return;
            }

            String var5 = var3.getProperty("wear_type");
            if (var5 == null) {
               this.errorMessage = String.format(
                  "The cfg File for the model '%s' at '%s' is missing the 'wear_type'. Go to the bottom of the cfg File and write 'wear_type=HEAD'. Check the cfg files of my examples to see what values for 'wear_type' are possible",
                  var2,
                  var1.getAbsolutePath()
               );
            } else {
               try {
                  var5 = var5.replace(" ", "");
                  this.boneType = BoneType.valueOf(var5);
               } catch (IllegalArgumentException var19) {
                  this.errorMessage = String.format(
                     "you entered '%s' into the 'wear_type' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'wear_type",
                     var5,
                     var2,
                     var1.getAbsolutePath()
                  );
                  return;
               }

               if (BoneType.CUSTOM_BONE.equals(this.boneType)) {
                  this.modelName = var3.getProperty("custom_bone");
                  if ("".equals(this.modelName)) {
                     this.errorMessage = String.format(
                        "You selected CUSTOM_BONE as the 'wear_type' in the cfg file for '%s' at '%s', yet you left the 'custom_bone' field right underneath it empty. If you want ur model to be parented to a specific bone, you have to enter the name of that bone at the field 'custom_bone'.",
                        var2,
                        var1.getAbsolutePath()
                     );
                     return;
                  }
               }

               String var6 = var3.getProperty("which_girls");
               var6 = var6.replace(" ", "");
               String[] var7 = var6.split(",");

               for (String var11 : var7) {
                  try {
                     if (!"".equals(var11)) {
                        this.allowedNpcTypes.add(NpcType.valueOf(var11));
                     }
                  } catch (IllegalArgumentException var22) {
                     this.errorMessage = String.format(
                        "you entered '%s' as one of the girls, you put into the 'which_girls' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'which_girls'.",
                        var11,
                        var2,
                        var1.getAbsolutePath()
                     );
                     return;
                  }
               }

               String var24 = var3.getProperty("which_lighting");
               if (var24 == null) {
                  this.errorMessage = String.format(
                     "The %s's cfg file at '%s' doesn't contain the field 'which_lighting'. Go to the bottom of the cfg file and write either 'which_lighting=DEFAULT', 'which_lighting=SEXMOD', or 'which_lighting=NONE'.",
                     var2,
                     var1.getAbsolutePath()
                  );
               } else {
                  var24 = var24.replace(" ", "");

                  try {
                     this.lightingType = LightingType.valueOf(var24);
                  } catch (IllegalArgumentException var18) {
                     this.errorMessage = String.format(
                        "you entered '%s' into the 'which_lighting' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'which_lighting'.",
                        var24,
                        var2,
                        var1.getAbsolutePath()
                     );
                  }

                  String var26 = var3.getProperty("author");
                  if (var26 != null && !"".equals(var26)) {
                     this.modelCode = var26;
                  } else {
                     this.modelCode = "anon";
                  }

                  String var27 = var3.getProperty("bones_to_hide");
                  if (var27 != null && !"".equals(var27)) {
                     var27 = var27.replace(" ", "");
                     String[] var29 = var27.split(",");
                     this.customPartBones.addAll(Arrays.asList(var29));
                  }

                  String var30 = var3.getProperty("enable_when_nude");
                  if (var30 == null) {
                     this.disabled = false;
                  } else {
                     var30 = var30.replace(" ", "");
                     this.disabled = var30.equalsIgnoreCase("yes");
                  }

                  String var12 = var3.getProperty("gui_size_factor");
                  if (var12 != null && !"".equals(var12)) {
                     var12 = var12.replace(" ", "");
                     var12 = var12.replace(",", ".");

                     try {
                        this.scale = Float.parseFloat(var12);
                     } catch (NumberFormatException var17) {
                        this.errorMessage = String.format(
                           "you entered '%s' into the 'gui_size_factor' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'gui_size_factor'.",
                           var12,
                           var2,
                           var1.getAbsolutePath()
                        );
                     }
                  }

                  String var13 = var3.getProperty("gui_vertical_positioning");
                  if (var13 != null && !"".equals(var13)) {
                     var13 = var13.replace(" ", "");
                     var13 = var13.replace(",", ".");

                     try {
                        this.xOffset = Float.parseFloat(var13);
                     } catch (NumberFormatException var16) {
                        this.errorMessage = String.format(
                           "you entered '%s' into the 'gui_vertical_positioning' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'gui_vertical_positioning'.",
                           var13,
                           var2,
                           var1.getAbsolutePath()
                        );
                     }
                  }

                  String var14 = var3.getProperty("version");
                  var14 = var14.replace(" ", "");
                  var14 = var14.replace(",", ".");

                  try {
                     this.zOffset = Float.parseFloat(var14);
                  } catch (NumberFormatException var15) {
                     this.errorMessage = String.format(
                        "you entered '%s' into the 'versionString' field of the %s's cfg file at '%s'. This is not a valid value. Check my examples on what valid values are to enter into the field 'versionString'.",
                        var14,
                        var2,
                        var1.getAbsolutePath()
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

      public void setTextureLocation(ResourceLocation var1) {
         this.textureLocation = var1;
      }

      public ResourceLocation getFallbackTexture() {
         return this.fallbackTexture;
      }

      public void setFallbackTexture(ResourceLocation var1) {
         this.fallbackTexture = var1;
      }

      public float getZOffset() {
         return this.zOffset;
      }

      private static FileNotFoundException wrapException(FileNotFoundException var0) {
         return var0;
      }
   }
}
