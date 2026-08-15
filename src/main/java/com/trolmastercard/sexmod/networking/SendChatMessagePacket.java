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

   public SendChatMessagePacket(String message, int channel, UUID playerUUID) {
      this.message = message;
      this.channel = channel;
      this.playerUUID = playerUUID;
      this.isValid = true;
   }

   public SendChatMessagePacket() {
      this.isValid = false;
   }

   public void fromBytes(ByteBuf buf) {
      try {
         int length = buf.readInt();
         byte[] bytes = new byte[length];

         for (int i = 0; i < length; i++) {
            bytes[i] = buf.readByte();
         }

         this.message = new String(bytes);
         this.channel = buf.readInt();
         this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
         this.isValid = true;
      } catch (IndexOutOfBoundsException exception) {
         this.isValid = false;
         System.out.println("couldn't read bytes @SendChatMessage :(");
      }
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.message.getBytes().length);
      buf.writeBytes(this.message.getBytes());
      buf.writeInt(this.channel);
      ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
   }

   public static class Handler implements IMessageHandler<SendChatMessagePacket, IMessage> {
      public IMessage onMessage(SendChatMessagePacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("recieved an unvalid message @SendChatMessage :(");
            return null;
         }

         if (ctx.side.isClient()) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(packet.message));
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     Vec3d pos = BaseGirlEntity.girlList(packet.playerUUID).get(0).getPreviousPosition();
                     PacketHandler.networkWrapper
                        .sendToAllAround(
                           new SendChatMessagePacket(packet.message, packet.channel, packet.playerUUID),
                           new TargetPoint(packet.channel, pos.x, pos.y, pos.z, 40.0)
                        );
                  }
               );
         }

         return null;
      }

   }
}
