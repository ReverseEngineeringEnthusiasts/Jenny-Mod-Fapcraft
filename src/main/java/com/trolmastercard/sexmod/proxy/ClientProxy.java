package com.trolmastercard.sexmod.proxy;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.GenderSwapScreen;
import com.trolmastercard.sexmod.client.gui.GuiHandler;
import com.trolmastercard.sexmod.client.particle.DragonBreathParticle;
import com.trolmastercard.sexmod.command.CommandFuta;
import com.trolmastercard.sexmod.command.CommandSetModelCode;
import com.trolmastercard.sexmod.command.CommandWhitelistServer;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.util.ForgeEventHandler;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * <b>Role.</b> Client-side half of the mod bootstrap
 * ({@link CommonProxy} subclass): registers the render handler, keybindings
 * (goblin interact / customization / leave scene), client commands, sounds, the
 * GUI handler and {@link PacketHandler}. Also pre-renders every NPC type into a
 * {@link SexWorldClient} preload world (with {@link #IS_PRELOADING} set) so
 * first-contact rendering has no load hitch, and registers the dragon-breath
 * particle.
 * <p>
 * <b>Pitfall.</b> {@code IS_PRELOADING} gates render code that must not run
 * against a real world (see {@link WorldUtils#getEntityLookVector}) — keep the
 * flag around the whole preload loop.
 */
public class ClientProxy extends CommonProxy {
   public static boolean IS_PRELOADING = false;
   public static KeyBinding[] keyBindings;

   @Override
   public void postInit(FMLPostInitializationEvent var1) {
   }

   @Override
   public void preInitRegistries(FMLPreInitializationEvent var1) {
      super.preInitRegistries(var1);
      RenderHandler.register();
   }

   @Override
   public void initRegistries(FMLInitializationEvent var1) {
      keyBindings = new KeyBinding[3];
      keyBindings[0] = new KeyBinding("Interact with your goblin", 34, "Sex mod");
      keyBindings[1] = new KeyBinding("open character customisation menu", 76, "Sex mod");
      keyBindings[2] = new KeyBinding("Leave sex scene", 54, "Sex mod");

      for (KeyBinding var5 : keyBindings) {
         ClientRegistry.registerKeyBinding(var5);
      }

      try { Main.setConfigs(); } catch (java.io.IOException var6) { Main.LOGGER.error(var6); }
      SoundHandler.registerSounds();
      NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler(true));
      ForgeEventHandler.registerB(true);
      PacketHandler.register();
      Minecraft var10 = Minecraft.getMinecraft();
      RenderManager var11 = var10.getRenderManager();
      SexWorldClient var12 = new SexWorldClient();
      IS_PRELOADING = true;

      try {
         for (NpcType var8 : NpcType.values()) {
            var11.renderEntity((Entity)var8.npcClass.getDeclaredConstructor(World.class).newInstance(var12), 0.0, 0.0, 0.0, 0.0F, 0.0F, false);
         }
      } catch (Exception var9) {
         System.out.println("error while preloading:");
         var9.printStackTrace();
      }

      IS_PRELOADING = false;
      GenderSwapScreen.instance = new GenderSwapScreen();
      ClientCommandHandler.instance.registerCommand(CommandWhitelistServer.WHITELIST_SERVER_COMMAND);
      ClientCommandHandler.instance.registerCommand(CommandSetModelCode.SET_MODEL_CODE_COMMAND);
      ClientCommandHandler.instance.registerCommand(CommandFuta.FUTA_COMMAND);
      Minecraft.getMinecraft()
         .effectRenderer
         .registerParticle(625115, (var0, var1x, var2, var4, var6, var8x, var10x, var12x, var14) -> new DragonBreathParticle(var1x, var2, var4, var6));
   }
}
