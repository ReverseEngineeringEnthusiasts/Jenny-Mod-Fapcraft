package com.trolmastercard.sexmod.network.scene;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;
import com.trolmastercard.sexmod.common.entity.galath.GalathEntity;
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
public class SetGirlVelocityPacket implements IMessage {
   boolean isValid = false;
   Vec3d velocity;
   UUID girlUUID;

   public SetGirlVelocityPacket(Vec3d velocity, UUID girlUUID) {
      this.velocity = velocity;
      this.girlUUID = girlUUID;
   }

   public SetGirlVelocityPacket() {
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

   public static class Handler implements IMessageHandler<SetGirlVelocityPacket, IMessage> {
      public IMessage onMessage(SetGirlVelocityPacket packet, MessageContext ctx) {
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
            Main.LOGGER.warn("received an invalid message @UpdateVelocity :(");
            return null;
         }
      }

   }
}
