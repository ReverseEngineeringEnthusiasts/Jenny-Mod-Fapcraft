package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerActionPacket implements IMessage {
   boolean c;
   UUID a;
   UUID b;

   public PlayerActionPacket() {
   }

   public PlayerActionPacket(UUID var1, UUID var2) {
      this.a = var1;
      this.b = var2;
      this.c = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      ByteBufUtils.writeUTF8String(var1, this.b.toString());
   }

   public static class Handler implements IMessageHandler<PlayerActionPacket, IMessage> {
      public IMessage onMessage(PlayerActionPacket var1, MessageContext var2) {
         if (var1.c && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     for (BaseGirlEntity var2x : BaseGirlEntity.getGirlEntityList()) {
                        if (!var2x.world.isRemote && var2x.getGirlId().equals(var1.a)) {
                           ((EntityPlayerMP)var2x.world.getPlayerEntityByUUID(var1.b))
                              .openGui(
                                 null,
                                 0,
                                 var2x.world,
                                 var2x.getPosition().getX(),
                                 var2x.getPosition().getY(),
                                 var2x.getPosition().getZ()
                              );
                        }
                     }
                  }
               );
            return null;
         } else {
            return null;
         }
      }

   }
}
