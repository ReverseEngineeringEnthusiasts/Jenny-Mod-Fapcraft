package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RequestRidingPacket implements IMessage {
   boolean a = false;

   public void fromBytes(ByteBuf var1) {
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
   }

   public static class Handler implements IMessageHandler<RequestRidingPacket, IMessage> {
      public IMessage onMessage(RequestRidingPacket var1, MessageContext var2) {
         if (var1.a && var2.side.equals(Side.SERVER)) {
            EntityPlayerMP var3 = var2.getServerHandler().field_147369_b;
            UUID var4 = GirlSavedData.b_clash853(var3);
            BaseGirlEntity var5 = BaseGirlEntity.getServerGirlEntity(var4);
            if (var5 == null) {
               return null;
            }

            var3.func_184205_a(var5, true);
            var5.b(fp.CONTROLLED_FLIGHT);
            var5.setInteractionPlayer(var3);
            var5.field_70181_x = 0.25;
            var3.field_70170_p.func_175726_f(var5.func_180425_c()).func_76622_b(var5);
            return null;
         } else {
            System.out.println("received an invalid message @RequestRiding :(");
            return null;
         }
      }

   }
}
