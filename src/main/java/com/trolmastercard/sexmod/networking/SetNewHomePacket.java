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

   public SetNewHomePacket(UUID var1, Vec3d var2) {
      this.girlUUID = var1;
      this.homePos = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.homePos = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      var1.writeDouble(this.homePos.x);
      var1.writeDouble(this.homePos.y);
      var1.writeDouble(this.homePos.z);
   }

   public static class Handler implements IMessageHandler<SetNewHomePacket, IMessage> {
      public IMessage onMessage(SetNewHomePacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid message @SetNewHome :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               ArrayList var1x = BaseGirlEntity.girlList(var1.girlUUID);
               if (!var1x.isEmpty()) {
                  for (BaseGirlEntity var3 : (java.util.Collection<BaseGirlEntity>) (var1x) ) {
                     var3.homePos = new Vec3d(var1.homePos.x, Math.floor(var1.homePos.y), var1.homePos.z);
                  }
               }
            });
            return null;
         }
      }

   }
}
