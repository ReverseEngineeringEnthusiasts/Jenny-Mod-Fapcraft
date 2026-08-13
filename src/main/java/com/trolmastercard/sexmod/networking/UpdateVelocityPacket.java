package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UpdateVelocityPacket implements IMessage {
   boolean isValid = false;
   Vec3d velocity;
   UUID girlUUID;

   public UpdateVelocityPacket(Vec3d var1, UUID var2) {
      this.velocity = var1;
      this.girlUUID = var2;
   }

   public UpdateVelocityPacket() {
   }

   public void fromBytes(ByteBuf var1) {
      this.velocity = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeDouble(this.velocity.x);
      var1.writeDouble(this.velocity.y);
      var1.writeDouble(this.velocity.z);
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<UpdateVelocityPacket, IMessage> {
      public IMessage onMessage(UpdateVelocityPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity var2x = BaseGirlEntity.getServerGirlEntity(var1.girlUUID);
               if (var2x instanceof GalathEntity) {
                  GalathEntity var3 = (GalathEntity)var2x;
                  if (var2.getServerHandler().player.equals(var3.getRidingPlayer())) {
                     var3.d(var1.velocity);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UpdateVelocity :(");
            return null;
         }
      }

   }
}
