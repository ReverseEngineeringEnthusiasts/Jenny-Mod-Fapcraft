package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.EllieEntity;
import com.trolmastercard.sexmod.entity.JennyEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.api.IEllie;







import io.netty.buffer.ByteBuf;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class KoboldStatePacket implements IMessage {
   boolean c;
   UUID a;
   boolean b;
   boolean d;
   UUID e = null;

   public KoboldStatePacket() {
      this.c = false;
   }

   public KoboldStatePacket(UUID var1, UUID var2, boolean var3, boolean var4) {
      this.a = var1;
      this.b = var3;
      this.e = var2;
      this.d = var4;
      this.c = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = var1.readBoolean();
      this.d = var1.readBoolean();
      String var2 = ByteBufUtils.readUTF8String(var1);
      this.e = var2.equals("null") ? null : UUID.fromString(var2);
      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      var1.writeBoolean(this.b);
      var1.writeBoolean(this.d);
      ByteBufUtils.writeUTF8String(var1, this.e == null ? "null" : this.e.toString());
   }


   public static class Handler implements IMessageHandler<KoboldStatePacket, IMessage> {
      public static void a(UUID var0, UUID var1, boolean var2, boolean var3) {
         try {
            for (BaseGirlEntity var5 : BaseGirlEntity.girlList(var0)) {
               if (!var5.world.isRemote) {
                  if (var5 instanceof JennyEntity || var5 instanceof EllieEntity || var5 instanceof LunaEntity) {
                     var5.tasks.removeTask(var5.watchClosestGirlGoal);
                     var5.tasks.removeTask(var5.wanderGoal);
                  }

                  var5.getNavigator().clearPath();
                  var5.motionX = 0.0;
                  var5.motionY = 0.0;
                  var5.motionZ = 0.0;
                  if (var5.getInteractionPlayerUUID() == null) {
                     var5.setInteractionPlayerUUID(var1);
                  }

                  if (var3) {
                     var5.setTargetPosition(var5.getFrontOffsetVector());
                  }

                  var5.snapPlayerToPosition(var5.getInteractionPlayerUUID());
                  if (!var2) {
                     return;
                  }

                  if (!(var5 instanceof IEllie)) {
                     return;
                  }

                  IEllie var6 = (IEllie)var5;
                  var6.b_clash158();
               }
            }
         } catch (ConcurrentModificationException var7) {
         }
      }

      public IMessage onMessage(KoboldStatePacket var1, MessageContext var2) {
         if (var1.c && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> a(var1.a, var1.e, var1.b, var1.d));
         }

         return null;
      }
   }
}
