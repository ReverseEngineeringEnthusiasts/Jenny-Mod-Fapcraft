package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UnknownPacket implements IMessage {
   boolean a = false;
   HashMap<String, Float> b = new HashMap<>();

   public UnknownPacket() {
   }

   public UnknownPacket(HashMap<String, Float> var1) {
      this.b = var1;
   }

   public void fromBytes(ByteBuf var1) {
      if (!(null instanceof ClientProxy)) {
         this.a = true;
      } else if (ServerWhitelistManager.b_clash129()) {
         int var2;
         try {
            var2 = var1.readInt();
         } catch (IndexOutOfBoundsException var4) {
            this.a = true;
            return;
         }

         for (int var3 = 0; var3 < var2; var3++) {
            this.b.put(ByteBufUtils.readUTF8String(var1), var1.readFloat());
         }

         this.a = true;
      }
   }

   public void toBytes(ByteBuf var1) {
      if (!(null instanceof ClientProxy)) {
         var1.writeInt(this.b.size());

         for (Entry var3 : this.b.entrySet()) {
            ByteBufUtils.writeUTF8String(var1, (String)var3.getKey());
            var1.writeFloat((Float)var3.getValue());
         }
      }
   }


   public static class Handler implements IMessageHandler<UnknownPacket, IMessage> {
      public IMessage onMessage(UnknownPacket var1, MessageContext var2) {
         if (!var1.a) {
            System.out.println("received an invalid Message @RequestServerModelAvailability :(");
            return null;
         }

         if (var2.side.isClient()) {
            if (!ServerWhitelistManager.b_clash129()) {
               return null;
            }

            ArrayList var3 = new ArrayList();

            for (Entry var5 : var1.b.entrySet()) {
               String var6 = (String)var5.getKey();
               if (!ServerWhitelistManager.f_clash125(var6)) {
                  var3.add(var6);
               } else {
                  float var7 = ServerWhitelistManager.i(var6);
                  float var8 = (Float)var5.getValue();
                  if (var8 > var7) {
                     var3.add(var6);
                  }
               }
            }

            return new DownloadServerModelPacket(var3);
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(() -> PacketHandler.b.sendTo(new UnknownPacket(ServerWhitelistManager.e_clash144()), var2.getServerHandler().player));
            return null;
         }
      }

   }
}
