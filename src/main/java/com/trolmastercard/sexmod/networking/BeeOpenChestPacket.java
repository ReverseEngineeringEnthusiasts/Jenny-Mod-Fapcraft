package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeeEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BeeOpenChestPacket implements IMessage {
   boolean b = false;
   UUID a;
   UUID c;

   public BeeOpenChestPacket() {
   }

   public BeeOpenChestPacket(UUID var1, UUID var2) {
      this.a = var1;
      this.c = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
   }

   public static class Handler implements IMessageHandler<BeeOpenChestPacket, IMessage> {
      public IMessage onMessage(BeeOpenChestPacket var1, MessageContext var2) {
         if (!var1.b) {
            System.out.println("received an invalid message @BeeOpenChest :(");
            return null;
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .func_152344_a(
                  () -> {
                     for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.a)) {
                        if (!var3.field_70170_p.field_72995_K && var3 instanceof BeeEntity) {
                           BeeEntity var4 = (BeeEntity)var3;
                           if ((Boolean)var4.func_184212_Q().func_187225_a(BeeEntity.M)) {
                              EntityPlayerMP var5 = (EntityPlayerMP)var4.field_70170_p.func_152378_a(var1.c);
                              if (var5 != null) {
                                 var5.openGui(
                                    null,
                                    1,
                                    var3.field_70170_p,
                                    var3.func_180425_c().func_177958_n(),
                                    var3.func_180425_c().func_177956_o(),
                                    var3.func_180425_c().func_177952_p()
                                 );
                                 return;
                              }
                           }
                        }
                     }
                  }
               );
            return null;
         }
      }

   }
}
