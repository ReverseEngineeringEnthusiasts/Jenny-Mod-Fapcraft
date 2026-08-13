package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UploadInventoryToServerPacket2 implements IMessage {
   boolean isValid;
   UUID girlUUID;

   public UploadInventoryToServerPacket2() {
   }

   public UploadInventoryToServerPacket2(UUID var1) {
      this.girlUUID = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<UploadInventoryToServerPacket2, IMessage> {
      public IMessage onMessage(UploadInventoryToServerPacket2 var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.girlUUID)) {
                  if (!var3.world.isRemote) {
                     var3.world.removeEntity(var3);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UploadInventoryToServer :(");
            return null;
         }
      }

   }
}
