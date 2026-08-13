package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UploadInventoryToServerPacket2 implements IMessage {
   boolean a;
   UUID b;

   public UploadInventoryToServerPacket2() {
   }

   public UploadInventoryToServerPacket2(UUID var1) {
      this.b = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.b = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.b.toString());
   }

   public static class Handler implements IMessageHandler<UploadInventoryToServerPacket2, IMessage> {
      public IMessage onMessage(UploadInventoryToServerPacket2 var1, MessageContext var2) {
         if (var1.a && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.g_clash524(var1.b)) {
                  if (!var3.field_70170_p.field_72995_K) {
                     var3.field_70170_p.func_72900_e(var3);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UploadInventoryToServer :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
