package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.SlimeEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChangeDataParameterPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   String parameterName;
   String value;

   public ChangeDataParameterPacket() {
      this.isValid = false;
   }

   public ChangeDataParameterPacket(UUID var1, String var2, String var3) {
      this.girlUUID = var1;
      this.parameterName = var2;
      this.value = var3;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.parameterName = ByteBufUtils.readUTF8String(var1);
      this.value = ByteBufUtils.readUTF8String(var1);
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(var1, this.parameterName);
      ByteBufUtils.writeUTF8String(var1, this.value == null ? "null" : this.value);
   }


   public static class Handler implements IMessageHandler<ChangeDataParameterPacket, IMessage> {
      public IMessage onMessage(ChangeDataParameterPacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid message @ChangeDataParameter :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity var1x = BaseGirlEntity.getServerGirlEntity(var1.girlUUID);
               if (var1x != null) {
                  switch (var1.parameterName) {
                     case "pregnant":
                        var1x.getDataManager().set(SlimeEntity.HORNY_LEVEL, Integer.valueOf(var1.value));
                        break;
                     case "currentModel":
                        var1x.getDataManager().set(BaseGirlEntity.OUTFIT_INDEX, Integer.valueOf(var1.value));
                        break;
                     case "currentAction":
                        if (Action.valueOf(var1.value) != Action.ATTACK || var1x.getCurrentAction() == Action.NULL) {
                           var1x.setCurrentAction(Action.valueOf(var1.value));
                        }
                        break;
                     case "animationFollowUp":
                        var1x.getDataManager().set(BaseGirlEntity.GIRL_HAND_STATES, var1.value);
                        break;
                     case "playerSheHasSexWith":
                        if (var1.value.equals("null")) {
                           var1x.setInteractionPlayerUUID(null);
                        } else {
                           var1x.setInteractionPlayerUUID(UUID.fromString(var1.value));
                        }
                        break;
                     case "targetPos":
                        String[] var4 = var1.value.split("f");
                        Vec3d var5 = new Vec3d(Double.parseDouble(var4[0]), Double.parseDouble(var4[1]), Double.parseDouble(var4[2]));
                        var1x.setTargetPosition(var5);
                        break;
                     case "master":
                        var1x.getDataManager().set(BaseGirlEntity.MASTER, var1.value);
                        break;
                     case "walk speed":
                        var1x.getDataManager().set(BaseGirlEntity.WALK_SPEED, var1.value);
                        break;
                     case "shouldbeattargetpos":
                        var1x.getDataManager().set(BaseGirlEntity.IS_ANCHORED, Boolean.valueOf(var1.value));
                  }
               }
            });
            return null;
         }
      }

   }
}
