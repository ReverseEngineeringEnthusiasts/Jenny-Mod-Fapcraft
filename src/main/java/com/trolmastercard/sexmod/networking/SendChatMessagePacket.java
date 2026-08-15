package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>Role.</b> Proximity chat for girls — a girl's line is broadcast to all
 * players within 40 blocks of her.
 * <p>
 * CLIENT->SERVER: the sender is the girl's client (her speech line); the
 * SERVER-side handler (scheduled on the main thread) re-broadcasts the message
 * SERVER->CLIENT via {@code sendToAllAround} centered on the girl's previous
 * position with a 40-block radius. CLIENT-side handler: prints the message into
 * the receiving player's chat.
 * <p>
 * <b>Note.</b> {@code channel} is repurposed as the TargetPoint dimension id;
 * keep it consistent with the dimension the girl is in.
 */
public class SendChatMessagePacket implements IMessage {
   boolean isValid;
   String message;
   int channel;
   UUID playerUUID;

   public SendChatMessagePacket(String var1, int var2, UUID var3) {
      this.message = var1;
      this.channel = var2;
      this.playerUUID = var3;
      this.isValid = true;
   }

   public SendChatMessagePacket() {
      this.isValid = false;
   }

   public void fromBytes(ByteBuf var1) {
      try {
         int var2 = var1.readInt();
         byte[] var3 = new byte[var2];

         for (int var4 = 0; var4 < var2; var4++) {
            var3[var4] = var1.readByte();
         }

         this.message = new String(var3);
         this.channel = var1.readInt();
         this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
         this.isValid = true;
      } catch (IndexOutOfBoundsException var5) {
         this.isValid = false;
         System.out.println("couldn't read bytes @SendChatMessage :(");
      }
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.message.getBytes().length);
      var1.writeBytes(this.message.getBytes());
      var1.writeInt(this.channel);
      ByteBufUtils.writeUTF8String(var1, this.playerUUID.toString());
   }

   public static class Handler implements IMessageHandler<SendChatMessagePacket, IMessage> {
      public IMessage onMessage(SendChatMessagePacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("recieved an unvalid message @SendChatMessage :(");
            return null;
         }

         if (var2.side.isClient()) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(var1.message));
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     Vec3d var1x = BaseGirlEntity.girlList(var1.playerUUID).get(0).getPreviousPosition();
                     PacketHandler.networkWrapper
                        .sendToAllAround(
                           new SendChatMessagePacket(var1.message, var1.channel, var1.playerUUID),
                           new TargetPoint(var1.channel, var1x.x, var1x.y, var1x.z, 40.0)
                        );
                  }
               );
         }

         return null;
      }

   }
}
