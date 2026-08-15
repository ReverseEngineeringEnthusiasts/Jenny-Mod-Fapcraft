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

/**
 * SERVER-bound packet: applies a velocity to a girl entity (impulses/knockback
 * from items and abilities).
 */
public class UpdateVelocityPacket implements IMessage {
   boolean isValid = false;
   Vec3d velocity;
   UUID girlUUID;

   public UpdateVelocityPacket(Vec3d velocity, UUID girlUUID) {
      this.velocity = velocity;
      this.girlUUID = girlUUID;
   }

   public UpdateVelocityPacket() {
   }

   public void fromBytes(ByteBuf buf) {
      this.velocity = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeDouble(this.velocity.x);
      buf.writeDouble(this.velocity.y);
      buf.writeDouble(this.velocity.z);
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<UpdateVelocityPacket, IMessage> {
      public IMessage onMessage(UpdateVelocityPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(packet.girlUUID);
               if (girl instanceof GalathEntity) {
                  GalathEntity galath = (GalathEntity)girl;
                  if (ctx.getServerHandler().player.equals(galath.getRidingPlayer())) {
                     galath.applyVelocityDelta(packet.velocity);
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
