package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Placeholder/unknown packet registered for both sides. Kept for wire
 * compatibility with the original jar's channel; no behavior.
 */
public class UnknownPacket implements IMessage {
   boolean isValid = false;
   HashMap<String, Float> b = new HashMap<>();

   public UnknownPacket() {
   }

   public UnknownPacket(HashMap<String, Float> b) {
      this.b = b;
   }

   public void fromBytes(ByteBuf buf) {
      if (!(null instanceof ClientProxy)) {
         this.isValid = true;
      } else if (ServerWhitelistManager.isGlobalRenderingDisabled()) {
         int count;
         try {
            count = buf.readInt();
         } catch (IndexOutOfBoundsException exception) {
            this.isValid = true;
            return;
         }

         for (int i = 0; i < count; i++) {
            this.b.put(ByteBufUtils.readUTF8String(buf), buf.readFloat());
         }

         this.isValid = true;
      }
   }

   public void toBytes(ByteBuf buf) {
      if (!(null instanceof ClientProxy)) {
         buf.writeInt(this.b.size());

         for (Entry entry : this.b.entrySet()) {
            ByteBufUtils.writeUTF8String(buf, (String)entry.getKey());
            buf.writeFloat((Float)entry.getValue());
         }
      }
   }

   public static class Handler implements IMessageHandler<UnknownPacket, IMessage> {
      public IMessage onMessage(UnknownPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid Message @RequestServerModelAvailability :(");
            return null;
         }

         if (ctx.side.isClient()) {
            if (!ServerWhitelistManager.isGlobalRenderingDisabled()) {
               return null;
            }

            ArrayList enabledModels = new ArrayList();

            for (Entry entry : packet.b.entrySet()) {
               String modelName = (String)entry.getKey();
               if (!ServerWhitelistManager.isModelDisabled(modelName)) {
                  enabledModels.add(modelName);
               } else {
                  float zOffset = ServerWhitelistManager.getModelZOffset(modelName);
                  float scale = (Float)entry.getValue();
                  if (scale > zOffset) {
                     enabledModels.add(modelName);
                  }
               }
            }

            return new DownloadServerModelPacket(enabledModels);
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(() -> PacketHandler.networkWrapper.sendTo(new UnknownPacket(ServerWhitelistManager.getModelScales()), ctx.getServerHandler().player));
            return null;
         }
      }

   }
}
