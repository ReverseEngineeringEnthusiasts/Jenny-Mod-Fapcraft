package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.an;







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

public class DownloadServerModelPacket implements IMessage {
   boolean d;
   List<String> c = new ArrayList<>();
   byte[] b;
   DownloadServerModelPacket.DownloadServerModelPacketType f;
   String e;
   int a = 0;

   public DownloadServerModelPacket() {
   }

   public DownloadServerModelPacket(List<String> var1) {
      this.c = var1;
   }

   public DownloadServerModelPacket(byte[] var1, DownloadServerModelPacket.DownloadServerModelPacketType var2, String var3) {
      this.b = var1;
      this.f = var2;
      this.e = var3;
   }

   public int a_clash350() {
      return this.a;
   }

   public void a_clash351(int var1) {
      this.a = var1;
   }

   public void fromBytes(ByteBuf var1) {
      if (null instanceof ClientProxy) {
         if (ServerWhitelistManager.b_clash129()) {
            this.e = ByteBufUtils.readUTF8String(var1);
            this.f = DownloadServerModelPacket.DownloadServerModelPacketType.valueOf(ByteBufUtils.readUTF8String(var1));
            this.a = var1.readInt();
            int var4 = var1.readInt();
            this.b = new byte[var4];

            for (int var5 = 0; var5 < var4; var5++) {
               this.b[var5] = var1.readByte();
            }

            this.d = true;
         }
      } else {
         int var2 = var1.readInt();

         for (int var3 = 0; var3 < var2; var3++) {
            this.c.add(ByteBufUtils.readUTF8String(var1));
         }

         this.d = true;
      }
   }

   public void toBytes(ByteBuf var1) {
      if (null instanceof ClientProxy) {
         var1.writeInt(this.c.size());

         for (String var7 : this.c) {
            ByteBufUtils.writeUTF8String(var1, var7);
         }
      } else {
         ByteBufUtils.writeUTF8String(var1, this.e);
         ByteBufUtils.writeUTF8String(var1, this.f.toString());
         var1.writeInt(this.a);
         var1.writeInt(this.b.length);

         for (byte var5 : this.b) {
            var1.writeByte(var5);
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   public static class Handler implements IMessageHandler<DownloadServerModelPacket, IMessage> {
      static int a = 0;

      @SideOnly(Side.CLIENT)
      void a_clash159(String var1) {
         Minecraft.func_71410_x().field_71439_g.func_145747_a(new TextComponentString(var1));
      }

      @SideOnly(Side.CLIENT)
      void a_clash160() {
         Minecraft.func_71410_x().func_152343_a(() -> ServerWhitelistManager.b_clash126(true));
      }

      public IMessage onMessage(DownloadServerModelPacket var1, MessageContext var2) {
         if (!var1.d) {
            System.out.println("received an invalid Message @DownloadServerModel :(");
            return null;
         }

         if (!var2.side.isClient()) {
            MinecraftServer var24 = FMLCommonHandler.instance().getMinecraftServerInstance();
            var24.func_152344_a(() -> {
               List var3x = var1.c;
               ArrayList var4x = new ArrayList();

               for (String var6x : (java.util.Collection<String>) (var3x) ) {
                  String var7x = "sexmod_custom_models/" + var6x;

                  for (DownloadServerModelPacket.DownloadServerModelPacketType var11x : DownloadServerModelPacket.DownloadServerModelPacketType.values()) {
                     File var12 = new File(var7x + "/" + var6x + var11x.ending);
                     if (!var12.exists()) {
                        System.out.println(var12.getAbsolutePath() + " doesnt exist lol");
                     } else {
                        byte[] var13;
                        try {
                           var13 = FileUtils.readFileToByteArray(var12);
                        } catch (IOException var15) {
                           throw new RuntimeException(var15);
                        }

                        if (var13 != null) {
                           var4x.add(new DownloadServerModelPacket(var13, var11x, var6x));
                        }
                     }
                  }
               }

               int var16 = var4x.size();

               for (DownloadServerModelPacket var18 : (java.util.Collection<DownloadServerModelPacket>) (var4x) ) {
                  var18.a_clash351(var16);
                  var24.func_152344_a(() -> PacketHandler.b.sendTo(var18, var2.getServerHandler().field_147369_b));
               }
            });
            return null;
         }

         if (!ServerWhitelistManager.b_clash129()) {
            return null;
         }

         String var3 = var1.e;
         DownloadServerModelPacket.DownloadServerModelPacketType var4 = var1.f;
         byte[] var5 = var1.b;
         String var6 = ServerWhitelistManager.h_clash132() + "/" + var3;
         File var7 = new File(var6);
         var7.mkdirs();
         File var8 = new File(var6 + "/" + var3 + var4.ending);

         try {
            FileOutputStream var9 = new FileOutputStream(var8);
            Object var10 = null;
            boolean var19 = false;

            label106: {
               Throwable var11;
               try {
                  var19 = true;
                  var9.write(var5);
                  var19 = false;
                  break label106;
               } catch (Throwable var21) {
                  var11 = var21;
                  var19 = false;
               } finally {
                  if (var19) {
                     if (var9 != null) {
                        if (var10 != null) {
                           try {
                              var9.close();
                           } catch (Throwable var20) {
                              ((Throwable) var10).addSuppressed(var20);
                           }
                        } else {
                           var9.close();
                        }
                     }
                  }
               }

               throw var11;
            }

            if (var9 != null) {
               var9.close();
            }
         } catch (Throwable var23) {
            var23.printStackTrace();
         }

         int var25 = 0;
         int var26 = DownloadServerModelPacket.DownloadServerModelPacketType.values().length;

         for (DownloadServerModelPacket.DownloadServerModelPacketType var14 : DownloadServerModelPacket.DownloadServerModelPacketType.values()) {
            if (new File(var6 + "/" + var3 + var14.ending).exists()) {
               var25++;
            }
         }

         if (var25 == var26) {
            this.a_clash159(
               String.format("%sSuccessfully downloaded the custom model '%s%s%s'!", TextFormatting.GREEN, TextFormatting.YELLOW, var3, TextFormatting.GREEN)
            );
         } else {
            this.a_clash159(
               String.format(
                  "%sdownloading custom model '%s%s%s' (%s/%s)...", TextFormatting.GRAY, TextFormatting.YELLOW, var3, TextFormatting.GRAY, var25, var26
               )
            );
         }

         if (++a < var1.a) {
            return null;
         }

         a = 0;
         this.a_clash160();
         return null;
      }

      private static Throwable a(Throwable var0) {
         return var0;
      }
   }

   public enum DownloadServerModelPacketType {
      CFG(".cfg"),
      PNG(".png"),
      GEO(".geo.json");

      public String ending;

      DownloadServerModelPacketType(String var3) {
         this.ending = var3;
      }
   }
}
