package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







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

public class SendChatMessagePacket implements IMessage {
   boolean b;
   String a;
   int d;
   UUID c;

   public SendChatMessagePacket(String var1, int var2, UUID var3) {
      this.a = var1;
      this.d = var2;
      this.c = var3;
      this.b = true;
   }

   public SendChatMessagePacket() {
      this.b = false;
   }

   public void fromBytes(ByteBuf var1) {
      try {
         int var2 = var1.readInt();
         byte[] var3 = new byte[var2];

         for (int var4 = 0; var4 < var2; var4++) {
            var3[var4] = var1.readByte();
         }

         this.a = new String(var3);
         this.d = var1.readInt();
         this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
         this.b = true;
      } catch (IndexOutOfBoundsException var5) {
         this.b = false;
         System.out.println("couldn't read bytes @SendChatMessage :(");
      }
   }

   public void toBytes(ByteBuf var1) {
      var1.writeInt(this.a.getBytes().length);
      var1.writeBytes(this.a.getBytes());
      var1.writeInt(this.d);
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
   }

   private static IndexOutOfBoundsException a(IndexOutOfBoundsException var0) {
      return var0;
   }

   public static class Handler implements IMessageHandler<SendChatMessagePacket, IMessage> {
      public IMessage onMessage(SendChatMessagePacket var1, MessageContext var2) {
         if (!var1.b) {
            System.out.println("recieved an unvalid message @SendChatMessage :(");
            return null;
         }

         if (var2.side.isClient()) {
            Minecraft.func_71410_x().field_71439_g.func_145747_a(new TextComponentString(var1.a));
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .func_152344_a(
                  () -> {
                     Vec3d var1x = BaseGirlEntity.g_clash524(var1.c).get(0).M_clash518();
                     PacketHandler.b
                        .sendToAllAround(
                           new SendChatMessagePacket(var1.a, var1.d, var1.c),
                           new TargetPoint(var1.d, var1x.field_72450_a, var1x.field_72448_b, var1x.field_72449_c, 40.0)
                        );
                  }
               );
         }

         return null;
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
