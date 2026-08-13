package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ForcePlayerGirlUpdatePacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   int modelVersion;
   Action action;

   public ForcePlayerGirlUpdatePacket() {
   }

   public ForcePlayerGirlUpdatePacket(UUID var1, int var2, Action var3) {
      this.girlUUID = var1;
      this.modelVersion = var2;
      this.action = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.modelVersion = var1.readInt();
      this.action = Action.valueOf(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      var1.writeInt(this.modelVersion);
      ByteBufUtils.writeUTF8String(var1, this.action.toString());
   }

   public static class Handler implements IMessageHandler<ForcePlayerGirlUpdatePacket, IMessage> {
      public IMessage onMessage(ForcePlayerGirlUpdatePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.CLIENT)) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.girlUUID);
            if (var3 == null) {
               return null;
            }

            var3.getDataManager().set(BaseGirlEntity.CUR_ACTION, var1.action.toString());
            var3.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, var1.modelVersion);
            return null;
         } else {
            System.out.println("received an invalid message @ForcePlayerGirlUpdate :(");
            return null;
         }
      }

   }
}
