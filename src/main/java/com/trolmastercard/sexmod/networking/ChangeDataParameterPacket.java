package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.SlimeEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChangeDataParameterPacket implements IMessage {
   boolean b;
   UUID d;
   String a;
   String c;

   public ChangeDataParameterPacket() {
      this.b = false;
   }

   public ChangeDataParameterPacket(UUID var1, String var2, String var3) {
      this.d = var1;
      this.a = var2;
      this.c = var3;
      this.b = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.d = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = ByteBufUtils.readUTF8String(var1);
      this.c = ByteBufUtils.readUTF8String(var1);
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.d.toString());
      ByteBufUtils.writeUTF8String(var1, this.a);
      ByteBufUtils.writeUTF8String(var1, this.c == null ? "null" : this.c);
   }


   public static class Handler implements IMessageHandler<ChangeDataParameterPacket, IMessage> {
      public IMessage onMessage(ChangeDataParameterPacket var1, MessageContext var2) {
         if (!var1.b) {
            System.out.println("received an invalid message @ChangeDataParameter :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               BaseGirlEntity var1x = BaseGirlEntity.getServerGirlEntity(var1.d);
               if (var1x != null) {
                  switch (var1.a) {
                     case "pregnant":
                        var1x.func_184212_Q().func_187227_b(SlimeEntity.U, Integer.valueOf(var1.c));
                        break;
                     case "currentModel":
                        var1x.func_184212_Q().func_187227_b(BaseGirlEntity.D, Integer.valueOf(var1.c));
                        break;
                     case "currentAction":
                        if (fp.valueOf(var1.c) != fp.ATTACK || var1x.getCurrentAction() == fp.NULL) {
                           var1x.b(fp.valueOf(var1.c));
                        }
                        break;
                     case "animationFollowUp":
                        var1x.func_184212_Q().func_187227_b(BaseGirlEntity.h, var1.c);
                        break;
                     case "playerSheHasSexWith":
                        if (var1.c.equals("null")) {
                           var1x.setInteractionPlayerUUID(null);
                        } else {
                           var1x.setInteractionPlayerUUID(UUID.fromString(var1.c));
                        }
                        break;
                     case "targetPos":
                        String[] var4 = var1.c.split("f");
                        Vec3d var5 = new Vec3d(Double.parseDouble(var4[0]), Double.parseDouble(var4[1]), Double.parseDouble(var4[2]));
                        var1x.setTargetPosition(var5);
                        break;
                     case "master":
                        var1x.func_184212_Q().func_187227_b(BaseGirlEntity.v, var1.c);
                        break;
                     case "walk speed":
                        var1x.func_184212_Q().func_187227_b(BaseGirlEntity.a, var1.c);
                        break;
                     case "shouldbeattargetpos":
                        var1x.func_184212_Q().func_187227_b(BaseGirlEntity.G, Boolean.valueOf(var1.c));
                  }
               }
            });
            return null;
         }
      }

   }
}
