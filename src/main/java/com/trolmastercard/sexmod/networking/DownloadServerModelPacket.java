package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

/**
 * <b>Role.</b> Bidirectional custom-model transfer for whitelisted servers.
 * <p>
 * CLIENT->SERVER: the client sends the list of model names it still wants
 * (missing model dirs). The SERVER-side handler (scheduled on the main thread)
 * reads each model's {@code .cfg}/{@code .png}/{@code .geo.json} trio from
 * {@code sexmod_custom_models/<name>/} and sends one SERVER->CLIENT packet per
 * file, tagging each with a {@code modelIndex} = total number of packets to
 * expect.
 * <p>
 * CLIENT-side handler: writes the received file into
 * {@code sexmod/custom_models/<server>/<name>/} and prints download progress;
 * after the last file ({@code packetCounter} reaching {@code modelIndex}) it
 * reloads the custom-model registry
 * ({@link ServerWhitelistManager#getModelCount(boolean)}). The whole exchange is
 * gated by {@link ServerWhitelistManager#isGlobalRenderingDisabled()} — a server
 * the player did not whitelist never triggers or accepts downloads.
 * <p>
 * <b>Pitfall.</b> {@code fromBytes}/{@code toBytes} branch on the *sender's*
 * side being a client vs. a server — keep those two wire formats distinct.
 */
public class DownloadServerModelPacket implements IMessage {
   boolean isValid;
   List<String> modelNames = new ArrayList<>();
   byte[] modelData;
   DownloadServerModelPacket.DownloadServerModelPacketType packetType;
   String modelName;
   int modelIndex = 0;

   public DownloadServerModelPacket() {
   }

   public DownloadServerModelPacket(List<String> modelNames) {
      this.modelNames = modelNames;
   }

   public DownloadServerModelPacket(byte[] modelData, DownloadServerModelPacket.DownloadServerModelPacketType packetType, String modelName) {
      this.modelData = modelData;
      this.packetType = packetType;
      this.modelName = modelName;
   }

   public int getModelIndex() {
      return this.modelIndex;
   }

   public void setModelIndex(int modelIndex) {
      this.modelIndex = modelIndex;
   }

   public void fromBytes(ByteBuf buf) {
      if (null instanceof ClientProxy) {
         if (ServerWhitelistManager.isGlobalRenderingDisabled()) {
            this.modelName = ByteBufUtils.readUTF8String(buf);
            this.packetType = DownloadServerModelPacket.DownloadServerModelPacketType.valueOf(ByteBufUtils.readUTF8String(buf));
            this.modelIndex = buf.readInt();
            int dataLength = buf.readInt();
            this.modelData = new byte[dataLength];

            for (int i = 0; i < dataLength; i++) {
               this.modelData[i] = buf.readByte();
            }

            this.isValid = true;
         }
      } else {
         int nameCount = buf.readInt();

         for (int i = 0; i < nameCount; i++) {
            this.modelNames.add(ByteBufUtils.readUTF8String(buf));
         }

         this.isValid = true;
      }
   }

   public void toBytes(ByteBuf buf) {
      if (null instanceof ClientProxy) {
         buf.writeInt(this.modelNames.size());

         for (String name : this.modelNames) {
            ByteBufUtils.writeUTF8String(buf, name);
         }
      } else {
         ByteBufUtils.writeUTF8String(buf, this.modelName);
         ByteBufUtils.writeUTF8String(buf, this.packetType.toString());
         buf.writeInt(this.modelIndex);
         buf.writeInt(this.modelData.length);

         for (byte b : this.modelData) {
            buf.writeByte(b);
         }
      }
   }

   public static class Handler implements IMessageHandler<DownloadServerModelPacket, IMessage> {
      static int packetCounter = 0;

      @SideOnly(Side.CLIENT)
      void sendModelMessage(String message) {
         Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
      }

      @SideOnly(Side.CLIENT)
      void reloadServerModels() {
         Minecraft.getMinecraft().addScheduledTask(() -> ServerWhitelistManager.getModelCount(true));
      }

      public IMessage onMessage(DownloadServerModelPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid Message @DownloadServerModel :(");
            return null;
         }

         if (!ctx.side.isClient()) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            server.addScheduledTask(() -> {
               List requested = packet.modelNames;
               ArrayList packets = new ArrayList();

               for (String typeEnding : (java.util.Collection<String>) (requested) ) {
                  String serverDir = "sexmod_custom_models/" + typeEnding;

                  for (DownloadServerModelPacket.DownloadServerModelPacketType fileType : DownloadServerModelPacket.DownloadServerModelPacketType.values()) {
                     File modelFile = new File(serverDir + "/" + typeEnding + fileType.ending);
                     if (!modelFile.exists()) {
                        System.out.println(modelFile.getAbsolutePath() + " doesnt exist lol");
                     } else {
                        byte[] fileBytes;
                        try {
                           fileBytes = FileUtils.readFileToByteArray(modelFile);
                        } catch (IOException ioEx) {
                           throw new RuntimeException(ioEx);
                        }

                        if (fileBytes != null) {
                           packets.add(new DownloadServerModelPacket(fileBytes, fileType, typeEnding));
                        }
                     }
                  }
               }

               int totalPackets = packets.size();

               for (DownloadServerModelPacket chunkPacket : (java.util.Collection<DownloadServerModelPacket>) (packets) ) {
                  chunkPacket.setModelIndex(totalPackets);
                  server.addScheduledTask(() -> PacketHandler.networkWrapper.sendTo(chunkPacket, ctx.getServerHandler().player));
               }
            });
            return null;
         }

         if (!ServerWhitelistManager.isGlobalRenderingDisabled()) {
            return null;
         }

         String modelName = packet.modelName;
         DownloadServerModelPacket.DownloadServerModelPacketType packetType = packet.packetType;
         byte[] modelData = packet.modelData;
         String groupPath = ServerWhitelistManager.getCurrentGroup() + "/" + modelName;
         File dirFile = new File(groupPath);
         dirFile.mkdirs();
         File modelFile = new File(groupPath + "/" + modelName + packetType.ending);

         try {
            FileOutputStream outputStream = new FileOutputStream(modelFile);
            Object suppressible = null;
            boolean completed = false;

            label106: {
               Throwable caught;
               try {
                  completed = true;
                  outputStream.write(modelData);
                  completed = false;
                  break label106;
               } catch (Throwable exception) {
                  caught = exception;
                  completed = false;
               } finally {
                  if (completed) {
                     if (outputStream != null) {
                        if (suppressible != null) {
                           try {
                              outputStream.close();
                           } catch (Throwable closeEx) {
                              ((Throwable) suppressible).addSuppressed(closeEx);
                           }
                        } else {
                           outputStream.close();
                        }
                     }
                  }
               }

               throw caught;
            }

            if (outputStream != null) {
               outputStream.close();
            }
         } catch (Throwable ioEx) {
            ioEx.printStackTrace();
         }

         int downloadedCount = 0;
         int totalTypes = DownloadServerModelPacket.DownloadServerModelPacketType.values().length;

         for (DownloadServerModelPacket.DownloadServerModelPacketType type : DownloadServerModelPacket.DownloadServerModelPacketType.values()) {
            if (new File(groupPath + "/" + modelName + type.ending).exists()) {
               downloadedCount++;
            }
         }

         if (downloadedCount == totalTypes) {
            this.sendModelMessage(
               String.format("%sSuccessfully downloaded the custom model '%s%s%s'!", TextFormatting.GREEN, TextFormatting.YELLOW, modelName, TextFormatting.GREEN)
            );
         } else {
            this.sendModelMessage(
               String.format(
                  "%sdownloading custom model '%s%s%s' (%s/%s)...", TextFormatting.GRAY, TextFormatting.YELLOW, modelName, TextFormatting.GRAY, downloadedCount, totalTypes
               )
            );
         }

         if (++packetCounter < packet.modelIndex) {
            return null;
         }

         packetCounter = 0;
         this.reloadServerModels();
         return null;
      }

   }

   public enum DownloadServerModelPacketType {
      CFG(".cfg"),
      PNG(".png"),
      GEO(".geo.json");

      public String ending;

      DownloadServerModelPacketType(String ending) {
         this.ending = ending;
      }
   }
}
