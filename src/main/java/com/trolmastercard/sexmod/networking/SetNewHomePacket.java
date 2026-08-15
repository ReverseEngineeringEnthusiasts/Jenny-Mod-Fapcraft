package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>Role.</b> CLIENT->SERVER "set this girl's home" command from the dragon-staff
 * UI. Sets the girl's respawn point ({@code homePos}) — the y coordinate is
 * floored to the block grid.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread; applies to all
 * entities matching the girl UUID in {@link BaseGirlEntity#girlList}. Used by
 * {@code goHome()}, {@link SendCompanionHomePacket} and general girl AI.
 */
public class SetNewHomePacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   Vec3d homePos;

   public SetNewHomePacket() {
   }

   public SetNewHomePacket(UUID girlUUID, Vec3d homePos) {
      this.girlUUID = girlUUID;
      this.homePos = homePos;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.homePos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      buf.writeDouble(this.homePos.x);
      buf.writeDouble(this.homePos.y);
      buf.writeDouble(this.homePos.z);
   }

   public static class Handler implements IMessageHandler<SetNewHomePacket, IMessage> {
      public IMessage onMessage(SetNewHomePacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid message @SetNewHome :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               ArrayList girls = BaseGirlEntity.girlList(packet.girlUUID);
               if (!girls.isEmpty()) {
                  for (BaseGirlEntity girl : (java.util.Collection<BaseGirlEntity>) (girls) ) {
                     girl.homePos = new Vec3d(packet.homePos.x, Math.floor(packet.homePos.y), packet.homePos.z);
                  }
               }
            });
            return null;
         }
      }

   }
}
