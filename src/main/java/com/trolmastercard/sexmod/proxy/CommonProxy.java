package com.trolmastercard.sexmod.proxy;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.gui.GuiHandler;
import com.trolmastercard.sexmod.registry.ModEntities;
import com.trolmastercard.sexmod.network.PacketHandler;
import com.trolmastercard.sexmod.common.event.ForgeEventHandler;
import com.trolmastercard.sexmod.common.model.ServerWhitelistManager;
import com.trolmastercard.sexmod.registry.SoundHandler;
import com.trolmastercard.sexmod.registry.ModItems;
import com.trolmastercard.sexmod.common.world.gen.ConfigWorldGenHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * <b>Role.</b> Common (server-capable) half of the mod bootstrap: registers the
 * world generator, entities, items, sounds, the GUI handler and
 * {@link PacketHandler}; on a dedicated server also loads the custom-model
 * registry ({@link ServerWhitelistManager#loadCustomModels(false)}) so model
 * files can be served to clients.
 */
public class CommonProxy {
   public static final CommonProxy PROXY = new CommonProxy();

   public void preInitRegistries(FMLPreInitializationEvent event) {
      GameRegistry.registerWorldGenerator(ConfigWorldGenHandler.getInstance(), 0);
      ModEntities.registerEntities();
      ModItems.registerAll();
   }

   public void initRegistries(FMLInitializationEvent event) {
      try { Main.setConfigs(); } catch (java.io.IOException e) { Main.LOGGER.error(e); }
      SoundHandler.registerSounds();
      NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
      ForgeEventHandler.registerAll(false);
      PacketHandler.register();
   }

   public void postInit(FMLPostInitializationEvent event) {
      this.setUpCustomModelsOnServer();
   }

   void setUpCustomModelsOnServer() {
      if (FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
         ServerWhitelistManager.loadCustomModels(false);
      }
   }

}
