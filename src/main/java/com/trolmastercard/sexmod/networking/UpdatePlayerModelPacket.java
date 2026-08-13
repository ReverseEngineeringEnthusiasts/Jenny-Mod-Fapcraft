package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.util.an;







import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Constructor;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UpdatePlayerModelPacket implements IMessage {
   boolean b = false;
   NpcType a;

   public UpdatePlayerModelPacket() {
   }

   public UpdatePlayerModelPacket(NpcType var1) {
      this.a = var1;
   }

   public void fromBytes(ByteBuf var1) {
      String var2 = ByteBufUtils.readUTF8String(var1);
      if ("player".equals(var2)) {
         this.a = null;
      } else {
         this.a = NpcType.valueOf(var2);
      }

      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      if (this.a == null) {
         ByteBufUtils.writeUTF8String(var1, "player");
      } else {
         ByteBufUtils.writeUTF8String(var1, this.a.toString());
      }
   }


   public static class Handler implements IMessageHandler<UpdatePlayerModelPacket, IMessage> {
      public IMessage onMessage(UpdatePlayerModelPacket var1, MessageContext var2) {
         if (var1.b && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               EntityPlayerMP var2x = var2.getServerHandler().field_147369_b;
               World var3 = var2x.field_70170_p;
               UUID var4 = var2.getServerHandler().field_147369_b.getPersistentID();
               AbstractPlayerGirlEntity var5 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var4);
               if (var5 != null) {
                  try {
                     for (BaseGirlEntity var7 : BaseGirlEntity.getGirlEntityList()) {
                        if (!var7.field_70170_p.field_72995_K && var7.getGirlId().equals(var5.getGirlId())) {
                           var3.func_72900_e(var7);
                        }
                     }
                  } catch (ConcurrentModificationException var10) {
                  }

                  var5.y_clash234();
                  AbstractPlayerGirlEntity.al.remove(var4);
                  BaseGirlEntity.getGirlEntityList().remove(var5);
                  var5.a(Optional.absent());
               }

               NpcType var12 = var1.a;
               if (var12 != null) {
                  AbstractPlayerGirlEntity var11;
                  try {
                     Constructor var8 = var12.playerClass.getConstructor(World.class, UUID.class);
                     var11 = (AbstractPlayerGirlEntity)var8.newInstance(var3, var2.getServerHandler().field_147369_b.getPersistentID());
                  } catch (Exception var9) {
                     var9.printStackTrace();
                     return;
                  }

                  var11.func_189654_d(true);
                  var11.field_70145_X = true;
                  var11.field_70159_w = 0.0;
                  var11.field_70181_x = 0.0;
                  var11.field_70179_y = 0.0;
                  var11.func_70107_b(var2x.field_70165_t, var2x.field_70163_u + 69.0, var2x.field_70161_v);
                  var3.func_72838_d(var11);
                  var11.B_clash233();
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UpdatePlayerModel :(");
            return null;
         }
      }

   }
}
