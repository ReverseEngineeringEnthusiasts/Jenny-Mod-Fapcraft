package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetTribeFollowModePacket implements IMessage {
   boolean a = false;
   boolean b;

   public SetTribeFollowModePacket() {
   }

   public SetTribeFollowModePacket(boolean var1) {
      this.b = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.b = var1.readBoolean();
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.b);
   }

   public static class Handler implements IMessageHandler<SetTribeFollowModePacket, IMessage> {
      public IMessage onMessage(SetTribeFollowModePacket var1, MessageContext var2) {
         if (var1.a && !var2.side.isClient()) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               UUID var2x = KoboldManager.getTribeUUID(var2.getServerHandler().player.getPersistentID());
               if (var2x != null) {
                  KoboldManager.setTribeFollowMode(var2x, var1.b);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @SetTribeFollowMode :(");
            return null;
         }
      }

   }
}
