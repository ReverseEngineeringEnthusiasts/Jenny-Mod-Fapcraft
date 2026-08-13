package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.SexNetworkManager;


import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.EnumPacketDirection;

public class ClientNetHandlerOverride extends NetHandlerPlayClient {
   public ClientNetHandlerOverride(Minecraft var1) {
      super(var1, var1.field_71462_r, new SexNetworkManager(EnumPacketDirection.CLIENTBOUND), var1.func_110432_I().func_148256_e());
   }
}
