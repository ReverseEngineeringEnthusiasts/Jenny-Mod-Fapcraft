package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.client.gui.GenderSwapScreen;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SexPromptPacket implements IMessage {
   boolean isValid = false;
   String actionName;
   UUID playerUUID;
   UUID girlUUID;
   boolean accepted;

   public SexPromptPacket() {
   }

   public SexPromptPacket(String var1, UUID var2, UUID var3, boolean var4) {
      this.actionName = var1;
      this.playerUUID = var2;
      this.girlUUID = var3;
      this.accepted = var4;
   }

   public void fromBytes(ByteBuf var1) {
      this.actionName = ByteBufUtils.readUTF8String(var1);
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.accepted = var1.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.actionName);
      ByteBufUtils.writeUTF8String(var1, this.playerUUID.toString());
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      var1.writeBoolean(this.accepted);
   }

   public static class Handler implements IMessageHandler<SexPromptPacket, IMessage> {
      public IMessage onMessage(SexPromptPacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid message @SexPrompt :(");
            return null;
         } else if (var2.side.equals(Side.CLIENT)) {
            GenderSwapScreen.instance.a(new GenderSwapScreen.a(var1.actionName, var1.playerUUID, var1.girlUUID, var1.accepted));
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               World var2x = var2.getServerHandler().player.world;
               EntityPlayer var3 = var2x.getPlayerEntityByUUID(var1.girlUUID);
               EntityPlayer var4 = var2x.getPlayerEntityByUUID(var1.playerUUID);
               if (var3 == null) {
                  System.out.println("Sex prompt invalid -> female player not found");
               } else if (var4 == null) {
                  System.out.println("Sex prompt invalid -> male player not found");
               } else {
                  PacketHandler.networkWrapper.sendTo(new SexPromptPacket(var1.actionName, var1.playerUUID, var1.girlUUID, var1.accepted), (EntityPlayerMP)(var1.accepted ? var3 : var4));
               }
            });
            return null;
         }
      }

   }
}
