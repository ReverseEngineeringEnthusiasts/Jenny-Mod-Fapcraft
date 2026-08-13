package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class InformOfOwnershipPacket implements IMessage {
   boolean a = false;
   boolean b;

   public InformOfOwnershipPacket() {
   }

   public InformOfOwnershipPacket(boolean var1) {
      this.b = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.b = var1.readBoolean();
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.b);
   }

   public static class Handler implements IMessageHandler<InformOfOwnershipPacket, IMessage> {
      public IMessage onMessage(InformOfOwnershipPacket var1, MessageContext var2) {
         if (var1.a && var2.side.equals(Side.CLIENT)) {
            GirlSavedData.f = var1.b;
            return null;
         } else {
            System.out.println("received an invalid message @InformOfOwnership :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
