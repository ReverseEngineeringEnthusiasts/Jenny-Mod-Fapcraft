package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.SexNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.EnumPacketDirection;

/**
 * Stand-in {@link NetHandlerPlayClient} wired to a {@link SexNetworkManager}
 * stub. Used solely to satisfy the constructor of {@link SexWorldClient}, which
 * needs a fully constructed client network handler even though it never joins a
 * server.
 * <p>
 * CLIENT-side only; never connected to a real server (see
 * {@link SexNetworkManager} — do not make this handler functional).
 */
public class ClientNetHandlerOverride extends NetHandlerPlayClient {
   public ClientNetHandlerOverride(Minecraft var1) {
      super(var1, var1.currentScreen, new SexNetworkManager(EnumPacketDirection.CLIENTBOUND), var1.getSession().getProfile());
   }
}
