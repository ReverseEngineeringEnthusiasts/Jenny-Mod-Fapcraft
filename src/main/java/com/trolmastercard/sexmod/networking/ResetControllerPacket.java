package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ResetControllerPacket implements IMessage {
   public static final int b = 100;
   boolean d;
   UUID a;
   UUID c;

   public ResetControllerPacket() {
      this.d = false;
   }

   public ResetControllerPacket(UUID var1) {
      this.a = var1;
      this.d = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.d = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
   }

   public static class Handler implements IMessageHandler<ResetControllerPacket, IMessage> {
      public IMessage onMessage(ResetControllerPacket var1, MessageContext var2) {
         if (!var1.d) {
            System.out.println("received an invalid message @ResetController :(");
            return null;
         }

         if (var2.side.isServer()) {
            BaseGirlEntity var7 = BaseGirlEntity.a_clash523(var1.a);
            if (var7 == null) {
               return null;
            }

            UUID var4 = var2.getServerHandler().field_147369_b.getPersistentID();
            var7.y_clash492().ticksPlaying = new int[]{0, 0};

            for (EntityPlayerMP var6 : FMLCommonHandler.instance().getMinecraftServerInstance().func_184103_al().func_181057_v()) {
               if (!var4.equals(var6.getPersistentID()) && var6.func_70032_d(var7) < 100.0F) {
                  PacketHandler.b.sendTo(new ResetControllerPacket(var1.a), var6);
               }
            }

            return null;
         } else {
            BaseGirlEntity var3 = BaseGirlEntity.b_clash522(var1.a);
            if (var3 != null) {
               var3.ag();
            }

            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
