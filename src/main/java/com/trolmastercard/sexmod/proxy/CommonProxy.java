package com.trolmastercard.sexmod.proxy;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.gui.GuiHandler;
import com.trolmastercard.sexmod.entity.SexModEntities;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.util.ForgeEventHandler;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.f9;
import com.trolmastercard.sexmod.worldgen.ConfigWorldGenHandler;







import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
   public static final CommonProxy PROXY = new CommonProxy();

   public void preInitRegistries(FMLPreInitializationEvent var1) {
      GameRegistry.registerWorldGenerator(ConfigWorldGenHandler.b_clash469(), 0);
      SexModEntities.a_clash150();
      f9.a_clash406();
   }

   public void initRegistries(FMLInitializationEvent var1) {
      try { Main.setConfigs(); } catch (java.io.IOException var2) { Main.LOGGER.error(var2); }
      SoundHandler.a_clash802();
      NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
      ForgeEventHandler.registerB(false);
      PacketHandler.register();
   }

   public void postInit(FMLPostInitializationEvent var1) {
      this.setUpCustomModelsOnServer();
   }

   void setUpCustomModelsOnServer() {
      if (FMLCommonHandler.instance().getMinecraftServerInstance().func_71262_S()) {
         ServerWhitelistManager.c_clash135(false);
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
